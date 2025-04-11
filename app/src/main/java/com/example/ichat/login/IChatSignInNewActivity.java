package com.example.ichat.login;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.ichat.CustomDialog.CustomProgressDialog;
import com.example.ichat.Home.HomeActivity.HomeActivity;
import com.example.ichat.R;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;
import java.util.concurrent.Executors;

public class IChatSignInNewActivity extends AppCompatActivity {

    private EditText userPassword, userEmail;
    private AppCompatButton loginButton;
    private LinearLayout googleLoginButton;
    private boolean isPasswordVisible = false;
    private CustomProgressDialog dialog;
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

        ///   this is use to hide the action bar...
        Objects.requireNonNull(getSupportActionBar()).hide();
        ///   this is use to initialize the view...
        init();
        ///   this is use to show and hide the password according to the user...
        userPassword.setOnTouchListener((v, event) -> showAndHidePassword(event, userPassword));
        ///   this is use to sign in using the login button using email and password...
        loginButton.setOnClickListener(v -> signInUsingEmailAndPassword());
        ///   this is use to sign in using the google...
        googleLoginButton.setOnClickListener(v -> signInWithGoogle());
    }

    ///   this is use for the initialization of the view...
    private void init() {
        userPassword = findViewById(R.id.user_password);
        userEmail = findViewById(R.id.user_email);
        loginButton = findViewById(R.id.login_btn);
        googleLoginButton = findViewById(R.id.google_login_btn);

        mAuth = FirebaseAuth.getInstance();
    }

    ///   this is use to sign in using the google...
    private void signInWithGoogle() {

        ///   this is use for the google authentication...
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest credentialRequest = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        CancellationSignal cancellationSignal = new CancellationSignal();

        CredentialManager credentialManager = CredentialManager.create(this);
        dialog = new CustomProgressDialog(IChatSignInNewActivity.this);
        dialog.setTitle("Logging In...");
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.show();
        credentialManager.getCredentialAsync(this, credentialRequest, cancellationSignal, Executors.newSingleThreadExecutor(), new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
            @Override
            public void onResult(GetCredentialResponse response) {
                handleSignIn(response.getCredential());
            }
            @Override
            public void onError(@NonNull GetCredentialException e) {
                Toast.makeText(IChatSignInNewActivity.this, "google-sign-in-failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    ///   this use for the perform the next task for google sign in...
    public void handleSignIn(Credential credential) {
        if(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType()) && credential instanceof CustomCredential) {
            Bundle credentialData = credential.getData();
            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);
            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
        } else {
            Toast.makeText(this, "google-sign-in-failed", Toast.LENGTH_LONG).show();
        }
    }

    ///   this is use for the firebase authentication using google...
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        dialog.dismiss();
                        startActivity(new Intent(IChatSignInNewActivity.this, HomeActivity.class));
                        finishAffinity();
                    } else {
                        Toast.makeText(IChatSignInNewActivity.this, "google-sign-in-failed", Toast.LENGTH_LONG).show();
                    }
                });
    }

    ///   this is use to signIn using the email and password...
    private void signInUsingEmailAndPassword() {
        dialog = new CustomProgressDialog(IChatSignInNewActivity.this);
        dialog.setTitle("Logging in...");
        dialog.setMessage("please wait...");
        dialog.setCancelable(false);
        dialog.show();
        String email = userEmail.getText().toString().trim();
        String password = userPassword.getText().toString().trim();

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_detail), Toast.LENGTH_LONG).show();
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            // redirect to the home activity...
                            dialog.dismiss();
                            startActivity(new Intent(IChatSignInNewActivity.this, HomeActivity.class));
                            finishAffinity();
                        }
                    }).addOnFailureListener(e -> {
                        dialog.dismiss();
                        // else show the error
                        Toast.makeText(IChatSignInNewActivity.this, "sign in failed", Toast.LENGTH_LONG).show();
                    });
        }
    }

    ///   this is use for the show and hide the password...
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