package com.example.ichat.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ichat.R;

import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity {

    private AppCompatButton createAccount, login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ///   this is use to hide the action bar...
        hideActionBar();
        ///   this is use to initialize the value...
        init();
        ///   this is use to navigate to the CreateAccountActivity...
        createAccount.setOnClickListener(view -> startActivity(new Intent(CreateAccountActivity.this, IChatSignUpNewActivity.class)));
        ///   this is use to navigate to the SignInActivity...
        login.setOnClickListener(view -> startActivity(new Intent(CreateAccountActivity.this, IChatSignInNewActivity.class)));
    }
    ///   this is use to initialize the value...
    private void init() {
        createAccount = findViewById(R.id.createAccount);
        login = findViewById(R.id.login);
    }

    ///   this is use to hide the action bar...
    private void hideActionBar() {
        Objects.requireNonNull(getSupportActionBar()).hide();
    }
}