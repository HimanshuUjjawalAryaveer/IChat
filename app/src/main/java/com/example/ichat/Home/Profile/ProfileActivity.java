package com.example.ichat.Home.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.ichat.CustomDialog.CustomProgressDialog;
import com.example.ichat.Home.HomeActivity.HomeActivity;
import com.example.ichat.Model.User;
import com.example.ichat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView image;
    private TextView userName, mail, aboutText, educationText, addressText;
    private LinearLayout about, education, address;
    private Intent intent;
    private String userId, aboutInfo, educationInfo, addressInfo;
    private CustomProgressDialog dialog;
    private AppCompatButton profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ///   use to hide the status bar color...
        setStatusBarColor();
        ///   use to set the name of the action bar...
        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ///   use to initialize the view...
        init();
        ///   use to read the data from the database and load this data to the view...
        readData();
        ///   use for to show button only when the current user can watch the profile and only current user can edit the profile...
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        if(!firebaseUser.getUid().equals(getIntent().getStringExtra("userID"))) {
            profileButton.setVisibility(View.GONE);
        }

        ///   use to switch to the update activity...
        profileButton.setOnClickListener(v -> {
            intent = new Intent(ProfileActivity.this, ProfileUpdate.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        ///   use to update the about section...
        about.setOnClickListener((View v) -> {
            if(Objects.equals(userId, Objects.requireNonNull(firebaseUser.getUid()))) {
                intent = new Intent(ProfileActivity.this, ProfileInfoUpdateActivity.class);
                switchActivity(userId, intent, aboutInfo, "about", "About");
            }
        });

        ///   use to update the education section...
        education.setOnClickListener((View v) -> {
            if(Objects.equals(userId, Objects.requireNonNull(firebaseUser.getUid()))) {
                intent = new Intent(ProfileActivity.this, ProfileInfoUpdateActivity.class);
                switchActivity(userId, intent, educationInfo, "education", "Education");
            }
        });

        ///   use to update the address section...
        address.setOnClickListener((View v) -> {
            if(Objects.equals(userId, Objects.requireNonNull(firebaseUser.getUid()))) {
                intent = new Intent(ProfileActivity.this, ProfileInfoUpdateActivity.class);
                switchActivity(userId, intent, addressInfo, "address", "Address");
            }
        });
    }

    ///   use to initialize the view...
    private void init() {
        image = findViewById(R.id.profile_image);
        userName = findViewById(R.id.userName);
        mail = findViewById(R.id.mail);
        profileButton = findViewById(R.id.edit_profile);

        about = findViewById(R.id.aboutGroup);
        education = findViewById(R.id.educationGroup);
        address = findViewById(R.id.addressGroup);

        aboutText = findViewById(R.id.aboutText);
        educationText = findViewById(R.id.educationText);
        addressText = findViewById(R.id.addressText);
    }

    ///   use to switch from profile activity to the update activity...
    private void switchActivity(final String userId, final Intent intent, String info, String key, String name) {
        intent.putExtra("name", name);
        intent.putExtra("userId", userId);
        intent.putExtra("info", info);
        intent.putExtra("key", key);
        startActivity(intent);
    }

    ///   use to read the data from the database and load this data to the view...
    private void readData() {
        dialog = new CustomProgressDialog(this);
        dialog.setTitle("Loading info...");
        dialog.setMessage("please wait...");
        dialog.setCancelable(false);
        Intent intent = getIntent();
        userId = intent.getStringExtra("userID");
        if(userId != null) {
           DatabaseReference reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(userId);
           reference.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {
                   User user = snapshot.getValue(User.class);
                   assert user != null;
                   aboutInfo = user.getAbout();
                   educationInfo = user.getEducation();
                   addressInfo = user.getAddress();
                   userName.setText(getCapitalText(user.getUsername()));
                   aboutText.setText(aboutInfo);
                   educationText.setText(educationInfo);
                   addressText.setText(addressInfo);
                   mail.setText(user.getEmail());
                   if(user.getImageUrl() != null) {
                       Glide.with(getApplicationContext()).load(user.getImageUrl()).into(image);
                   }
                   dialog.dismiss();
               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {
                   Toast.makeText(ProfileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                   dialog.dismiss();
               }
           });
        }
    }

    ///   use to set the status bar color...
    private void setStatusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.light_blue));
    }

    ///   use to set the functionality of the back button in the action bar...
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
            finishAffinity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    ///   use to convert the first letter of the string to capital...
    private static String getCapitalText(String string) {
        if (!Objects.equals(string, "") && string != null) {
            String[] str =string.split(" ");
            for(int i=0; i<str.length; i++) {
                str[i] = str[i].substring(0,1).toUpperCase() + str[i].substring(1).toLowerCase();
            }
            return String.join(" ", str);
        }
        return "";
    }
}