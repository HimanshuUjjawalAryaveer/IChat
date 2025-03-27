package com.example.ichat.Home.Profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.ichat.CustomDialog.CustomProgressDialog;
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
    private Uri imageUri;
    private ActivityResultLauncher<Intent> galleryLauncher;

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

        ///   use to set the status bar color...
        setStatusBarColor();
        ///   use to initialize the view...
        init();
        ///   use to set the name of the action bar...
        Objects.requireNonNull(getSupportActionBar()).setTitle("Update Profile");
        ///   use to fetch the data at very first time...
        setData(getIntent().getStringExtra("userId"));
        ///   use to save the data to the firebase and come back to the profile activity...
        updateButton.setOnClickListener(v -> uploadImageToFirebase(imageUri, getIntent().getStringExtra("userId")));
        ///   use to get the image from the gallery and set it to the image view...
        profileImage.setOnClickListener(v -> openGallery());
    }

    ///   use to initialize the view...
    private void init() {
        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        email = findViewById(R.id.emil);
        updateButton = findViewById(R.id.update_button);

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                imageUri = result.getData().getData();
                profileImage.setImageURI(imageUri);
            }
        });
    }

    ///   use to fetch the data from the firebase at first time...
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

    ///   use to get the image from gallery and also get the image uri of the image...
    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(gallery);
    }

    ///   use to upload the image to the firebase storage...
    private void uploadImageToFirebase(Uri imageUri, final String UID) {
        if (imageUri != null) {
            final CustomProgressDialog dialog = new CustomProgressDialog(this);
            dialog.setTitle("Updating profile");
            dialog.setCancelable(false);
            dialog.show();

            ///   Create unique file reference in the firebase database...
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storeRef = storage.getReference().child("ProfileImages/" + System.currentTimeMillis() + ".jpg");
            ///   Upload file to Firebase Storage...
            storeRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ///   Get the download URL after successful upload...
                            storeRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    dialog.dismiss();
                                    ///   here the uri contains the url of the image...
                                    updateDataToTheFirebase(uri.toString(), UID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    Toast.makeText(ProfileUpdate.this, "Failed to get URL: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(ProfileUpdate.this, "Upload failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            ///   Update progress dialog...
                            double percent = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                            dialog.setMessage("Progress: " + (int) percent + "%");
                        }
                    });
        }
        else {
            updateDataToTheFirebase(null, UID);
        }
    }

    ///   use to upload the getting data to the firebase database...
    private void updateDataToTheFirebase(String imageUrl, final String UID) {
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

    ///   use to set the color of the status bar...
    private void setStatusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.light_blue));
    }
}