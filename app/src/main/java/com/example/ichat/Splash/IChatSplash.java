package com.example.ichat.Splash;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ichat.Home.Activity.HomeActivity;
import com.example.ichat.R;
import com.example.ichat.login.CreateAccountActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Objects;

public class IChatSplash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ichat_splash);

        Objects.requireNonNull(getSupportActionBar()).hide();   /* hides the action bar */
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();   /* checks if the user is logged in or not */
        Intent intent;

        if(firebaseUser != null) {
            intent = new Intent(IChatSplash.this, HomeActivity.class);
        } else {
            intent = new Intent(IChatSplash.this, CreateAccountActivity.class);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                finishAffinity();
            }
        }, 3000);
    }

}