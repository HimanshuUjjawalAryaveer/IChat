package com.example.ichat.Home.Profile;

import android.app.ProgressDialog;
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
    private ProgressDialog pd;
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
        setStatusBarColor();
        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
        readData();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;

        //   use for to display button only when the current user can watch the profile
        if(!firebaseUser.getUid().equals(getIntent().getStringExtra("userID"))) {
            profileButton.setVisibility(View.GONE);
        }

        //  use to switch to the update activity
        profileButton.setOnClickListener(v -> {
            intent = new Intent(ProfileActivity.this, ProfileUpdate.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        about.setOnClickListener((View v) -> {
            if(Objects.equals(userId, Objects.requireNonNull(firebaseUser.getUid()))) {
                intent = new Intent(ProfileActivity.this, ProfileInfoUpdateActivity.class);
                intent.putExtra("name", "About");
                intent.putExtra("userId", userId);
                intent.putExtra("info", aboutInfo);
                intent.putExtra("key", "about");
                startActivity(intent);
                finish();
            }
        });

        education.setOnClickListener((View v) -> {
            if(Objects.equals(userId, Objects.requireNonNull(firebaseUser.getUid()))) {
                intent = new Intent(ProfileActivity.this, ProfileInfoUpdateActivity.class);
                intent.putExtra("name", "Education");
                intent.putExtra("userId", userId);
                intent.putExtra("info", educationInfo);
                intent.putExtra("key", "education");
                startActivity(intent);
                finish();
            }
        });

        address.setOnClickListener((View v) -> {
            if(Objects.equals(userId, Objects.requireNonNull(firebaseUser.getUid()))) {
                intent = new Intent(ProfileActivity.this, ProfileInfoUpdateActivity.class);
                intent.putExtra("name", "Address");
                intent.putExtra("userId", userId);
                intent.putExtra("info", addressInfo);
                intent.putExtra("key", "address");
                startActivity(intent);
                finish();
            }
        });
    }
    private void init() {
        image = findViewById(R.id.image_image);
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
    private void readData() {
        pd = new ProgressDialog(this);
        pd.setTitle("Loading info...");
        pd.setMessage("please wait...");
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
                   pd.dismiss();
               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {
                   Toast.makeText(ProfileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                   pd.dismiss();
               }
           });
        }
    }
    private void setStatusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.light_blue));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
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