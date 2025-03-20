package com.example.ichat.Fragments.Status;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ichat.Model.StatusData;
import com.example.ichat.Model.User;
import com.example.ichat.R;
import com.example.ichat.login.IChatSignUpNewActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IChatStatusImageActivity extends AppCompatActivity {

    private ImageView statusImage;
    private ImageButton statusSendBtn;
    private EditText message;
    private DatabaseReference reference;
    private FirebaseStorage storage;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ichat_status_image);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        setStatusBar();
        Objects.requireNonNull(getSupportActionBar()).setTitle("IChat Status");
        String imageUri = getIntent().getStringExtra("imageUri");
        statusImage.setImageURI(Uri.parse(imageUri));
        statusSendBtn.setOnClickListener(v -> setStatus(imageUri));
    }

    private void setStatus(String imageUri) {

        final ProgressDialog pd = new ProgressDialog(this, R.style.customColorOfProgressDialog);
        pd.setTitle("Uploading Status...");
        pd.show();
        StorageReference storeRef = storage.getReference().child("StatusImages/"+ user.getUid() + "/" + System.currentTimeMillis() + ".jpg");

        // Upload file to Firebase Storage
        storeRef.putFile(Uri.parse(imageUri))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the download URL after successful upload
                        storeRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                uploadStatusToDatabase(imageUrl);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(IChatStatusImageActivity.this, "Failed to get URL: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(IChatStatusImageActivity.this, "Upload failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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

    private void uploadStatusToDatabase(String imageUrl) {
        reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user));
        reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    ArrayList<StatusData> list = user.getStatusData();
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    StatusData data = new StatusData(imageUrl);
                    list.add(data);
                    Map<String, Object> updateStatus = new HashMap<>();
                    updateStatus.put("statusData", list);
                    reference.child(user.getUserID()).updateChildren(updateStatus);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void init() {
        statusImage = findViewById(R.id.image);
        statusSendBtn = findViewById(R.id.status_send_btn);
        message = findViewById(R.id.message);

        //   use for the database...
        user = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
    }

    private void setStatusBar() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.light_blue));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}