package com.example.ichat.Home.Profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.ichat.Model.User;
import com.example.ichat.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileUpdate extends AppCompatActivity {

    private CircleImageView profileImage;
    private EditText username, email;
    private AppCompatButton updateButton;
    private Intent intent;
    private String UID;
    private static final int PICK_IMAGES = 100;
    private Uri imageUri;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_update);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setStatusBarColor();
        init();
        Objects.requireNonNull(getSupportActionBar()).setTitle("Update Profile");
        setData(UID);

        //  set to come back to the profile activity
        updateButton.setOnClickListener(v -> {
            uploadImageToFirebase(imageUri);
        });

        //  use to set the image to the profile
        profileImage.setOnClickListener(v -> openGallery());
    }

    private void updateDataToTheFirebase(String imageUrl) {
        String name = username.getText().toString().toLowerCase().trim();
        if(name.isEmpty()) {
            Toast.makeText(this, "Please enter the Username", Toast.LENGTH_LONG).show();
        } else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user));
            if(imageUrl != null) {
                reference.child(UID).child("username").setValue(name);
                reference.child(UID).child("imageUrl").setValue(imageUrl);
            } else {
                reference.child(UID).child("username").setValue(name);
            }
            finish();
        }
    }

    private void init() {
        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        email = findViewById(R.id.emil);
        updateButton = findViewById(R.id.update_button);

        UID = getIntent().getStringExtra("userId");

        storage = FirebaseStorage.getInstance();
    }
    private void setData(String UID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user));
        reference.child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                username.setText(user.getUsername());
                email.setText(user.getEmail());
                if(user.getImageUrl() != null) {
                    Glide.with(getApplicationContext()).load(user.getImageUrl()).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void setStatusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.light_blue));
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
        if (imageUri != null) {
            // Initialize progress dialog
            final ProgressDialog pd = new ProgressDialog(this, R.style.customColorOfProgressDialog);
            pd.setTitle("Updating profile");
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

                                    updateDataToTheFirebase(imageUrl);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(ProfileUpdate.this, "Failed to get URL: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(ProfileUpdate.this, "Upload failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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
        else {
            updateDataToTheFirebase(null);
        }
    }
}