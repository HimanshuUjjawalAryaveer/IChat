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
import android.widget.TextView;
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
import com.example.ichat.Model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import im.zego.zegoexpress.ZegoExpressEngine;

public class IChatSignUpActivity extends AppCompatActivity {

    private TextView haveAnAccount;
    private EditText userName, email, password, confirmPassword;
    private CircleImageView profileImage;
    private AppCompatButton signUpButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private final boolean[] isPasswordVisible = {false, false};
    Uri imageUri;
    private String imageUrl;
    private static final int PICK_IMAGES = 100;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    String stringUserName, stringEmail, stringPassword;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ichat_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Objects.requireNonNull(getSupportActionBar()).hide();
        init();

        haveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(IChatSignUpActivity.this, IChatLoginActivity.class));
                finish();
            }
        });

        signUpButton.setOnClickListener(v -> signUpWithEmail());

        password.setOnTouchListener((v, event) -> showAndHidePassword(event, 0, password));

        confirmPassword.setOnTouchListener((v, event) -> showAndHidePassword(event, 1, confirmPassword));

        profileImage.setOnClickListener(v -> openGallery());
    }


    private void init() {
        haveAnAccount = findViewById(R.id.haveAccount);
        userName = findViewById(R.id.userName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        profileImage = findViewById(R.id.profileImage);
        signUpButton = findViewById(R.id.signUpButton);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
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
    private void signUpWithEmail() {
        stringUserName = userName.getText().toString();
        stringEmail = email.getText().toString();
        stringPassword = password.getText().toString();
        String stringConfirmPassword = confirmPassword.getText().toString();

        if(stringUserName.isEmpty() || stringEmail.isEmpty() || stringPassword.isEmpty() || stringConfirmPassword.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Fill all details", Toast.LENGTH_LONG).show();
        } else {
            if(stringPassword.equals(stringConfirmPassword)) {
                if(imageUri == null) {
                    authenticationUsingEmailAndPassword(stringUserName, stringEmail, stringPassword);   // make change
                } else {
                    uploadImageToFirebase(imageUri);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Confirm password again", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void authenticationUsingEmailAndPassword(String stringUserName, String stringEmail, String stringPassword) {
        final ProgressDialog pd = new ProgressDialog(this, R.style.customColorOfProgressDialog);
        pd.setTitle("Creating account");
        pd.setMessage("please wait...");
        pd.setCancelable(false);
        pd.show();
        firebaseAuth.createUserWithEmailAndPassword(stringEmail, stringPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            pd.dismiss();
                            Users users;
                            if(imageUrl == null) {
                                users = new Users(stringUserName.toLowerCase(), stringEmail, stringPassword, "Hey there, I am using IChat", "Graduation", "Your address", firebaseAuth.getUid(), "offline");   // make change
                            } else {
                                users = new Users(stringUserName.toLowerCase(), stringEmail, stringPassword, imageUrl, "Hey there, I am using IChat", "Graduation", "Your address", firebaseAuth.getUid(), "offline");     // make change
                            }
                            firebaseDatabase.getReference("Users").child(Objects.requireNonNull(firebaseAuth.getUid())).setValue(users);
                            Toast.makeText(getApplicationContext(), "sign in successfully", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(IChatSignUpActivity.this, HomeActivity.class));
                            finish();
//                            startCallService(firebaseAuth.getUid(), stringUserName);
                        } else {
                            Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGES);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }
    private void uploadImageToFirebase(Uri imageUri) {
        final ProgressDialog pd = new ProgressDialog(this, R.style.customColorOfProgressDialog);
        pd.setTitle("Creating account");
        pd.show();
        StorageReference storeRef = storage.getReference().child("images/"+System.currentTimeMillis()+"jpg");
        storeRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storeRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                imageUrl = uri.toString();
                                pd.dismiss();
                                authenticationUsingEmailAndPassword(stringUserName, stringEmail, stringPassword);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(IChatSignUpActivity.this, "failed", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double percent = (100.00 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        pd.setMessage("progress : " + (int)percent + "%");
                    }
                });
    }

//    private void startCallService(String userID, String userName) {
//
//        long appID = 1821245422;
//        String appSign = "4489f0faf0cb9eb4d3f172e4620ae325555a1d8813254f3cda18715a34d20f7e";
//
//
//        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();
//        ZegoNotificationConfig notificationConfig = new ZegoNotificationConfig();
//        notificationConfig.sound = "zego_uikit_sound_call";
//        notificationConfig.channelID = "CallInvitation";
//        notificationConfig.channelName = "CallInvitation";
//        ZegoUIKitPrebuiltCallInvitationService.init(getApplication(), appID, appSign, userID, userName, callInvitationConfig);
//    }
}

