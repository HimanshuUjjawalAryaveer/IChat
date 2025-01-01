package com.example.ichat.login;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ichat.Home.Activity.HomeActivity;
import com.example.ichat.Model.User;
import com.example.ichat.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class IChatSignUpNewActivity extends AppCompatActivity {


    private static final int PICK_IMAGES = 100;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private static final int RC_CODE = 11;
    private EditText username, userEmail, userPassword;
    private CircleImageView userProfileImage;
    private Uri imageUri;
    private FirebaseStorage storage;
    private ProgressDialog pd;
    private boolean isPasswordVisible;

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

        // use to hide the action bar
        Objects.requireNonNull(getSupportActionBar()).hide();

        init();

        userPassword.setOnTouchListener((v, event) -> showAndHidePassword(event, userPassword));

        // code for the google sign-in button..

        findViewById(R.id.google_signin_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(intent,RC_CODE);
            }
        });

         // use to create the account using email and password...

        findViewById(R.id.create_account_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = username.getText().toString().trim();
                String email = userEmail.getText().toString().trim();
                String password = userPassword.getText().toString().trim();
                if(name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(IChatSignUpNewActivity.this, getString(R.string.fill_detail), Toast.LENGTH_LONG).show();
                } else {
                    // sign up with email and password;
                    createUserUsingEmailAndPassword(name, email, password);
                }
            }
        });

        // use to open the gallery and pick the image..
        userProfileImage.setOnClickListener(v -> {
            openGallery();
        });
    }


    // use to initialize the some important values...
    private void init() {

        username = findViewById(R.id.user_name);
        userEmail = findViewById(R.id.user_email);
        userPassword = findViewById(R.id.user_password);
        userProfileImage = findViewById(R.id.user_profile_image);

        isPasswordVisible = false;

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        database = FirebaseDatabase.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }


    // use to picking the image from the gallery...
    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGES);
    }


    // use for the mapping of the different matched value
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            userProfileImage.setImageURI(imageUri);
        } else if(requestCode == RC_CODE) {
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
                Toast.makeText(IChatSignUpNewActivity.this, getString(R.string.google_sign_failed), Toast.LENGTH_LONG).show();
                pd.dismiss();
            }
        }
    }

    // After getting the token progress further
    private void authenticationWithGoogle(String token) {
        AuthCredential credential = GoogleAuthProvider.getCredential(token, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            FirebaseUser fUser = mAuth.getCurrentUser();
                            assert fUser != null;
                            uploadDataToTheRealtimeDatabase(Objects.requireNonNull(fUser.getDisplayName()).toLowerCase(), fUser.getEmail(), "Known by Google", String.valueOf(fUser.getPhotoUrl()), fUser.getUid());
                        } else {
                            Toast.makeText(IChatSignUpNewActivity.this, getString(R.string.google_sign_failed), Toast.LENGTH_LONG).show();
                            pd.dismiss();
                        }
                    }
                });
    }

    private void createUserUsingEmailAndPassword(String name, String email, String password) {
        // first upload the image to the firebase storage...
        if(imageUri != null) {
            uploadImageToFirebase(name, email, password, imageUri);
        } else {
            // create profile without image...
            createProfileUsingEmailAndPassword(name, email, password, null);
        }
    }

    private void createProfileUsingEmailAndPassword(String name, String email, String password, String imageUrl) {
        pd = new ProgressDialog(IChatSignUpNewActivity.this);
        pd.setTitle("Logging In...");
        pd.setMessage("Please wait...");
        pd.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            // upload the data on realtime firebase database...
                            uploadDataToTheRealtimeDatabase(name.toLowerCase(), email, password, imageUrl, mAuth.getUid());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(IChatSignUpNewActivity.this, "sign up failed...", Toast.LENGTH_LONG).show();
                        pd.dismiss();
                    }
                });
    }

    // use to upload the details to the firebase realtime database...

    private void uploadDataToTheRealtimeDatabase(String name, String email, String password, String imageUrl, String uid) {
        User user = new User(name, email, password, uid, imageUrl, "Your Address", "Hey there, I am using IChat.", "graduation", "offline", false);
        database.getReference(getString(R.string.User)).child(uid).setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(IChatSignUpNewActivity.this, "data uploaded", Toast.LENGTH_LONG).show();
                            pd.dismiss();

                            // redirect to the home activity...
                            startActivity(new Intent(IChatSignUpNewActivity.this, HomeActivity.class));
                            finishAffinity();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(IChatSignUpNewActivity.this, "failed to upload data", Toast.LENGTH_LONG).show();
                        pd.dismiss();
                    }
                });
    }

    private void uploadImageToFirebase(String name, String email, String password, Uri imageUri) {
        // Initialize progress dialog
        final ProgressDialog pd = new ProgressDialog(this, R.style.customColorOfProgressDialog);
        pd.setTitle("Creating account");
        pd.show();

        // Create unique file reference
        StorageReference storeRef = storage.getReference().child("images/" + System.currentTimeMillis() + ".jpg");

        // Upload file to Firebase Storage
        storeRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the download URL after successful upload
                        storeRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Get the URL as a string
                                String imageUrl = uri.toString();
                                pd.dismiss();

                                // create the profile using email and password...

                                createProfileUsingEmailAndPassword(name, email, password, imageUrl);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(IChatSignUpNewActivity.this, "Failed to get URL: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(IChatSignUpNewActivity.this, "Upload failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        // Update progress dialog
                        double percent = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        pd.setMessage("Progress: " + (int) percent + "%");
                    }
                });
    }


    // use to handle the view and un-view options to the edittext view...
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