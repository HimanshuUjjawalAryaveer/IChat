package com.example.ichat.login;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ichat.Home.HomeActivity;
import com.example.ichat.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class IChatSignInNewActivity extends AppCompatActivity {

    private static final int RC_CODE = 3;
    private EditText userPassword, userEmail;
    private AppCompatButton loginButton;
    private LinearLayout googleLoginButton;
    private boolean isPasswordVisible = false;
    private ProgressDialog pd;

    private GoogleSignInClient mGoogleSignInClient;


    private FirebaseAuth mAuth;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ichat_sign_in_new);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Objects.requireNonNull(getSupportActionBar()).hide();
        init();

//         use to show and hide the password according to the user...
        userPassword.setOnTouchListener((v, event) -> showAndHidePassword(event, userPassword));

//        sign in using the login button...
        loginButton.setOnClickListener(v -> signInUsingEmailAndPassword());

//        sign in using the google...
        googleLoginButton.setOnClickListener(v -> googleSignIn());
    }


    private void init() {
        userPassword = findViewById(R.id.user_password);
        userEmail = findViewById(R.id.user_email);
        loginButton = findViewById(R.id.login_btn);
        googleLoginButton = findViewById(R.id.google_login_btn);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }


//    sign in using the google...
    private void googleSignIn() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent,RC_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_CODE) {
            pd = new ProgressDialog(this);
            pd.setTitle("Logging In...");
            pd.setMessage("Please wait...");
            pd.show();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                authenticationWithGoogle(account.getIdToken());
                // Successfully signed in
            } catch (ApiException e) {
                Toast.makeText(IChatSignInNewActivity.this, getString(R.string.google_sign_failed), Toast.LENGTH_LONG).show();
                pd.dismiss();
            }
        }
    }

    private void authenticationWithGoogle(String token) {
        AuthCredential credential = GoogleAuthProvider.getCredential(token, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            // redirect to the home activity...
                            pd.dismiss();
                            startActivity(new Intent(IChatSignInNewActivity.this, HomeActivity.class));
                            finishAffinity();
                        } else {
                            Toast.makeText(IChatSignInNewActivity.this, getString(R.string.google_sign_failed), Toast.LENGTH_LONG).show();
                            pd.dismiss();
                        }
                    }
                });
    }


    private void signInUsingEmailAndPassword() {
        pd = new ProgressDialog(this);
        pd.setTitle("Logging in...");
        pd.setMessage("please wait...");
        pd.show();
        String email = userEmail.getText().toString().trim();
        String password = userPassword.getText().toString().trim();

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_detail), Toast.LENGTH_LONG).show();
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                // redirect to the home activity...
                                pd.dismiss();
                                startActivity(new Intent(IChatSignInNewActivity.this, HomeActivity.class));
                                finishAffinity();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            // else show the error
                            Toast.makeText(IChatSignInNewActivity.this, "sign in failed", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private boolean showAndHidePassword(@NonNull MotionEvent event, EditText editText) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {
                isPasswordVisible = !isPasswordVisible;

                if (isPasswordVisible) {
                    // Show password
                    editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.open_eye, 0);
                } else {
                    // Hide password
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.close_eye, 0);
                }
                editText.setSelection(editText.getText().length());

                return true;
            }
        }
        return false;
    }

}