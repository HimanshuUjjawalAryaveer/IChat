package com.example.ichat.login;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.ichat.Home.Activity.HomeActivity;
import com.example.ichat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class IChatLoginActivity extends AppCompatActivity {

    private TextView createAccount;
    private EditText email, password, confirmPassword;
    private AppCompatButton loginBtn;
    private FirebaseAuth firebaseAuth;
    private final boolean[] isPasswordVisible = {false, false};
//    final ProgressDialog pd = new ProgressDialog(this);
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ichat_login);

        hideActionBar();
        init();
        createAccount.setOnClickListener((View v) -> {
                startActivity(new Intent(IChatLoginActivity.this, IChatSignUpActivity.class));
                finish();
        });
        loginBtn.setOnClickListener((View v) -> loginWithEmailAndPassword());
        password.setOnTouchListener((v, event) -> showAndHidePassword(event, 0, password));
        confirmPassword.setOnTouchListener((v, event) -> showAndHidePassword(event, 1, confirmPassword));
    }
    private void hideActionBar() {
        Objects.requireNonNull(getSupportActionBar()).hide();
    }

    private void init() {
        createAccount = findViewById(R.id.createAccount);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        loginBtn = findViewById(R.id.loginBtn);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private boolean showAndHidePassword(@NonNull MotionEvent event, int n, EditText editText) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {
                isPasswordVisible[n] = !isPasswordVisible[n];

                if (isPasswordVisible[n]) {
                    // Show password
                    editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pass, 0, R.drawable.open_eye, 0);
                } else {
                    // Hide password
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pass, 0, R.drawable.close_eye, 0);
                }
                editText.setSelection(editText.getText().length());

                return true;
            }
        }
        return false;
    }

    private void loginWithEmailAndPassword() {
        final ProgressDialog pd = new ProgressDialog(this);
        String stringEmail = email.getText().toString();
        String stringPassword = password.getText().toString();
        String stringConfirmPassword = confirmPassword.getText().toString();

        if(stringEmail.isEmpty() || stringPassword.isEmpty() || stringConfirmPassword.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Fill all details", Toast.LENGTH_LONG).show();
        } else {
            if(stringPassword.equals(stringConfirmPassword)) {
                showProgressDialog(pd);
                loginStart(stringEmail, stringPassword, pd);
            } else {
                Toast.makeText(getApplicationContext(), "Confirm password again", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void showProgressDialog(ProgressDialog pd) {
        pd.setTitle("Logging in");
        pd.setMessage("please wait...");
        pd.setCancelable(false);
        pd.show();
    }
    private void loginStart(String stringEmail, String stringPassword, ProgressDialog pd) {
        firebaseAuth.signInWithEmailAndPassword(stringEmail, stringPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            pd.dismiss();
                            Toast.makeText(getApplicationContext(), "login successfully", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(IChatLoginActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            pd.dismiss();
                            Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}