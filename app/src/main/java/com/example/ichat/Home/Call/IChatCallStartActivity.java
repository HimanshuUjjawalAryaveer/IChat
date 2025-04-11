package com.example.ichat.Home.Call;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.ichat.Model.User;
import com.example.ichat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class IChatCallStartActivity extends AppCompatActivity {

    private CircleImageView callEndButton, profileImage;
    private TextView username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ichat_call_start);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ///   this is use to hide the support action bar...
        Objects.requireNonNull(getSupportActionBar()).hide();
        ///   this is use to initialize the values of the views...
        init();
        ///   this is use to set the user profile...
        setUserProfile(getIntent().getStringExtra("userID"));
        ///   this is use to check the current status of the Receiver end...
        checkReceiverStatus(getIntent().getStringExtra("userID"));
        ///   this is use to end the call...
        callEndButton.setOnClickListener(v -> callEnd(getIntent().getStringExtra("userID")));
        ///   this function continuously check that the receiver end is available or not...
        checkForCallEnd();
    }

    ///   this is use to initialize the view...
    private void init() {
        callEndButton = findViewById(R.id.call_end_btn);
        profileImage = findViewById(R.id.user_profile_image);
        username = findViewById(R.id.username);
    }

    ///   this is use to set the profile at the calling time...
    private void setUserProfile(String uId) {
        if(uId != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(uId);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    Glide.with(IChatCallStartActivity.this).load(user.getImageUrl()).into(profileImage);
                    username.setText(user.getUsername());
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    ///   this is use to check the current status of the Receiver end...
    private void checkReceiverStatus(String UID) {
        final String currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference referenceReceiver = FirebaseDatabase.getInstance().getReference(getString(R.string.User)).child(UID);
        DatabaseReference referenceSender = FirebaseDatabase.getInstance().getReference(getString(R.string.User)).child(currentUser);
        referenceReceiver.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ///   now check the user is available or not for picking the call...
                boolean isReceiverAvailable = Boolean.TRUE.equals(snapshot.child("videoCallStatus").getValue(Boolean.class));
                if(!isReceiverAvailable) {
                    Map<String, Object> videoCallStatus = new HashMap<>();
                    videoCallStatus.put("videoCallStatus", true);
                    referenceSender.updateChildren(videoCallStatus).addOnSuccessListener(unused -> referenceReceiver.updateChildren(videoCallStatus).addOnSuccessListener(unused1 -> {
                        ///   now set the status for the video call to the Firebase database...
                        setStatusForVideoCall(currentUser, UID);
                    }));
                } else {
                    new Handler().postDelayed(() -> {
                        Toast.makeText(IChatCallStartActivity.this, "busy on other call", Toast.LENGTH_LONG).show();
                        finish();
                    }, 2000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    ///   this is use to set the status for the video call...
    private void setStatusForVideoCall(final String currentUser, final String UID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> videoCallStatusInformation = new HashMap<>();
        videoCallStatusInformation.put("sendBy", currentUser);
        videoCallStatusInformation.put("sendTo", UID);
        videoCallStatusInformation.put("isStart", false);
        reference.child("VideoCallStartStatus").child(currentUser).setValue(videoCallStatusInformation).addOnSuccessListener(unused -> reference.child("VideoCallStartStatus").child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isAvailable = Boolean.TRUE.equals(snapshot.child("isStart").getValue(Boolean.class));
                if(isAvailable) {
                    Toast.makeText(IChatCallStartActivity.this, "both avail", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(IChatCallStartActivity.this, IChatVideoCallActivity.class);
                    intent.putExtra("currentUser", currentUser);
                    intent.putExtra("sendTo", UID);
                    intent.putExtra("sendBy", currentUser);
                    intent.putExtra("trackLastActivity", UID);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));
    }

    ///   this is use to end the call...
    private void callEnd(final String UID) {
        final String currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(getString(R.string.User));
        Map<String, Object> videoCallStatus = new HashMap<>();
        videoCallStatus.put("videoCallStatus", false);
        reference.child(UID).updateChildren(videoCallStatus).addOnSuccessListener(unused -> reference.child(currentUser).updateChildren(videoCallStatus).addOnSuccessListener(unused1 -> {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("VideoCallStartStatus");
            databaseReference.child(currentUser).setValue(null).addOnSuccessListener(unused2 -> finish());
        }));
    }

    ///   this automatically end the activity if the receiver end user decline the call...
    private void checkForCallEnd() {
        new Handler().postDelayed(() -> {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            databaseReference.child("VideoCallStartStatus").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.getValue() == null)
                        finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }, 2000);
    }
}