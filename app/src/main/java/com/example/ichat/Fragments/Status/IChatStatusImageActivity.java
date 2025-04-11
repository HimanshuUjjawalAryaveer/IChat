package com.example.ichat.Fragments.Status;

import android.net.Uri;
import android.os.Bundle;
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

import com.example.ichat.CustomDialog.CustomProgressDialog;
import com.example.ichat.Model.StatusData;
import com.example.ichat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IChatStatusImageActivity extends AppCompatActivity {

    private ImageView statusImage;
    private ImageButton statusSendBtn;
    private EditText message;
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

        ///   this is use to initialize the views and viewGroup...
        init();
        ///   this is use to set the action bar...
        setStatusBar();
        Objects.requireNonNull(getSupportActionBar()).setTitle("IChat Status");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        ///   this is use to show the image on the imageview and send the image to the database...
        String imageUri = getIntent().getStringExtra("imageUri");
        ///   this is use to set the image to the image view...
        statusImage.setImageURI(Uri.parse(imageUri));
        ///   this is use to send the image to the database...
        statusSendBtn.setOnClickListener(v -> setStatus(imageUri));
    }

    ///   this is use to initialize the view...
    private void init() {
        statusImage = findViewById(R.id.image);
        statusSendBtn = findViewById(R.id.status_send_btn);
        message = findViewById(R.id.message);

        //   use for the database...
        user = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
    }

    ///   this is use to send the image to the database storage...
    private void setStatus(String imageUri) {

        final CustomProgressDialog dialog = new CustomProgressDialog(this);
        dialog.setTitle("Uploading Status...");
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.show();
        //  this is use to upload the data to the firebase storage...
        StorageReference storeRef = storage.getReference().child("StatusImages/"+ user.getUid() + "/" + System.currentTimeMillis() + ".jpg");
        storeRef.putFile(Uri.parse(imageUri))
                .addOnSuccessListener(taskSnapshot -> {
                    //  Get the download URL after successful upload
                    storeRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        //  this is use to upload the image url to the realtime firebase database...
                        uploadStatusToDatabase(imageUrl);
                    }).addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(IChatStatusImageActivity.this, "Failed to get URL: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    });
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(IChatStatusImageActivity.this, "Upload failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                })
                .addOnProgressListener(snapshot -> {
                    // Update progress dialog
                    double percent = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    dialog.setMessage("Progress: " + (int) percent + "%");
                });
    }

    ///   this is use to send the data to the realtime firebase database...
    private void uploadStatusToDatabase(String imageUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(getString(R.string.User)).child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        databaseReference.child("statusData").addListenerForSingleValueEvent(new ValueEventListener() {
            final ArrayList<StatusData> list = new ArrayList<>();
            final String caption = message.getText().toString().trim();
            final long timeStamp = System.currentTimeMillis();
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                if(snapshot.exists()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        StatusData data = dataSnapshot.getValue(StatusData.class);
                        list.add(data);
                    }
                    list.add(new StatusData(imageUrl, caption, timeStamp));
                } else {
                    list.add(new StatusData(imageUrl, caption, timeStamp));
                }
                Map<String, Object> statusData = new HashMap<>();
                statusData.put("statusData", list);
                databaseReference.updateChildren(statusData).addOnCompleteListener(task -> finish());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    ///   this is use to set the action bar...
    private void setStatusBar() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.light_blue));
    }

}