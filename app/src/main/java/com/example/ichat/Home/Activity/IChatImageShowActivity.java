package com.example.ichat.Home.Activity;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.ichat.Model.User;
import com.example.ichat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class IChatImageShowActivity extends AppCompatActivity {

    private ImageView image;
    private Toolbar toolbar;
    private CircleImageView profileImage;
    private TextView userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ichat_image_show);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        setStatusBarColor();
        Objects.requireNonNull(getSupportActionBar()).setIcon(R.drawable.user_profile);
//        setToolbar();

//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user));
//        reference.child(Objects.requireNonNull(getIntent().getStringExtra("userId"))).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                User user = snapshot.getValue(User.class);
//                assert user != null;
//                Glide.with(getApplicationContext()).load(user.getImageUrl()).into(profileImage);
//                userName.setText(getCapitalText(user.getUsername()));
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        //  get the image url from the intent...
        String imageUrl = getIntent().getStringExtra("imageUrl");
        Glide.with(this).load(imageUrl).into(image);
    }

    private void init() {
        image = findViewById(R.id.image);
        toolbar = findViewById(R.id.toolbar);
        profileImage = findViewById(R.id.profile_image);
        userName = findViewById(R.id.userName);
    }

    private void setStatusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.transparent_black));
    }

//    private void setToolbar() {
//        setSupportActionBar(toolbar);
//        Objects.requireNonNull(getSupportActionBar()).setTitle("");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//    }

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