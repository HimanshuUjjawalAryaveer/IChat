package com.example.ichat.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.ichat.Home.Home.HomeActivity;
import com.example.ichat.Model.User;
import com.example.ichat.R;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class IChatSignUpNewActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private EditText username, userEmail, userPassword;
    private CircleImageView userProfileImage;
    private Uri imageUri;
    private FirebaseStorage storage;
    private CustomProgressDialog dialog;
    private boolean isPasswordVisible;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ichat_sign_up_new);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ///   this is use to hide the action bar...
        Objects.requireNonNull(getSupportActionBar()).hide();
        ///   this is use to initialize view & view group...
        init();
        ///   this is used to show and hide the password...
        userPassword.setOnTouchListener((v, event) -> showAndHidePassword(event, userPassword));
        ///   this is use for the google sign-in button..
        findViewById(R.id.google_signin_btn).setOnClickListener(view -> signInWithGoogle());
        ///   this is use to create the account using email and password...
        findViewById(R.id.create_account_btn).setOnClickListener(v -> {
            String name = username.getText().toString().trim();
            String email = userEmail.getText().toString().trim();
            String password = userPassword.getText().toString().trim();
            if(name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(IChatSignUpNewActivity.this, getString(R.string.fill_detail), Toast.LENGTH_LONG).show();
            } else {
                createUserUsingEmailAndPassword(name, email, password);     // sign up with email and password;
            }
        });
        ///   this is use to open the gallery and pick the image...
        userProfileImage.setOnClickListener(v -> openGallery());
    }

    ///   this is use to initialize the views and the viewGroup...
    private void init() {

        username = findViewById(R.id.user_name);
        userEmail = findViewById(R.id.user_email);
        userPassword = findViewById(R.id.user_password);
        userProfileImage = findViewById(R.id.user_profile_image);

        isPasswordVisible = false;

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                imageUri = result.getData().getData();
                userProfileImage.setImageURI(imageUri);
            }
        });
    }

    ///   this is use for the google authentication...
    private void signInWithGoogle() {

        //   use for the google authentication...
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest credentialRequest = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        CancellationSignal cancellationSignal = new CancellationSignal();

        CredentialManager credentialManager = CredentialManager.create(this);
        credentialManager.getCredentialAsync(this, credentialRequest, cancellationSignal, Executors.newSingleThreadExecutor(), new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
            @Override
            public void onResult(GetCredentialResponse response) {
                handleSignIn(response.getCredential());
            }
            @Override
            public void onError(@NonNull GetCredentialException e) {
                Toast.makeText(IChatSignUpNewActivity.this, "google-sign-in-failed", Toast.LENGTH_LONG).show();
            }
        });
    }
    ///   this is used for the next task of the google sign in...
    public void handleSignIn(Credential credential) {
        if(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType()) && credential instanceof CustomCredential) {
            Bundle credentialData = credential.getData();
            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);
            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
        } else {
            Toast.makeText(this, "google-sign-in-failed", Toast.LENGTH_LONG).show();
        }
    }
    ///   this is use to firebase authentication using google...
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        dialog = new CustomProgressDialog(IChatSignUpNewActivity.this);
                        dialog.setTitle("Logging In...");
                        dialog.setMessage("Please wait...");
                        dialog.setCancelable(false);
                        dialog.show();

                        FirebaseUser fUser = mAuth.getCurrentUser();
                        assert fUser != null;
                        uploadDataToTheRealtimeDatabase(Objects.requireNonNull(fUser.getDisplayName()).toLowerCase(), fUser.getEmail(), "Known by Google", String.valueOf(fUser.getPhotoUrl()), fUser.getUid());
                    } else {
                        Toast.makeText(IChatSignUpNewActivity.this, "google-sign-in-failed", Toast.LENGTH_LONG).show();
                    }
                });
    }

    ///   this is use to picking the image from the gallery...
    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(gallery);
    }

    ///   this is use to create the account using email and password...
    private void createUserUsingEmailAndPassword(String name, String email, String password) {
        // first upload the image to the firebase storage...
        if(imageUri != null) {
            uploadImageToFirebase(name, email, password, imageUri);
        } else {
            // create profile without image...
            createProfileUsingEmailAndPassword(name, email, password, null);
        }
    }

    ///   this is use to create the profile using email and password and upload the data to the firebase realtime database...
    private void createProfileUsingEmailAndPassword(String name, String email, String password, String imageUrl) {
        dialog = new CustomProgressDialog(IChatSignUpNewActivity.this);
        dialog.setTitle("Logging In...");
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        // upload the data on realtime firebase database...
                        uploadDataToTheRealtimeDatabase(name.toLowerCase(), email, password, imageUrl, mAuth.getUid());
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(IChatSignUpNewActivity.this, "sign up failed...", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                });
    }

    ///   this use to upload the details to the firebase realtime database...
    private void uploadDataToTheRealtimeDatabase(String name, String email, String password, String imageUrl, String uid) {
        User user = new User(name, email, password, uid, imageUrl, "Your Address", "Hey there, I am using IChat.", "graduation", "offline", false);
        database.getReference(getString(R.string.User)).child(uid).setValue(user)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(IChatSignUpNewActivity.this, "data uploaded", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        // redirect to the home activity...
                        startActivity(new Intent(IChatSignUpNewActivity.this, HomeActivity.class));
                        finishAffinity();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(IChatSignUpNewActivity.this, "failed to upload data", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                });
    }

    ///   this is use to upload the image to the firebase storage...
    private void uploadImageToFirebase(String name, String email, String password, Uri imageUri) {
        // Initialize progress dialog
        dialog = new CustomProgressDialog(IChatSignUpNewActivity.this);
        dialog.setTitle("Creating account");
        dialog.setCancelable(false);
        dialog.show();

        // Create unique file reference
        StorageReference storeRef = storage.getReference().child("images/" + System.currentTimeMillis() + ".jpg");

        // Upload file to Firebase Storage
        storeRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL after successful upload
                    storeRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Get the URL as a string
                        String imageUrl = uri.toString();
                        dialog.dismiss();

                        // create the profile using email and password...
                        createProfileUsingEmailAndPassword(name, email, password, imageUrl);
                    }).addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(IChatSignUpNewActivity.this, "Failed to get URL: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    });
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(IChatSignUpNewActivity.this, "Upload failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                })
                .addOnProgressListener(snapshot -> {
                    // Update progress dialog
                    double percent = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    dialog.setMessage("Progress: " + (int) percent + "%");
                });
    }

    ///   use to show and hide the password...
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