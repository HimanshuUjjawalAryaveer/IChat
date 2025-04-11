package com.example.ichat.Home.Call;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

public class IChatVideoCallConnectingActivity extends AppCompatActivity {

    private CircleImageView callEndButton, profileImage;
    private TextView username;                                                                      //  add the call status...


    private boolean isOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ichat_video_call_connecting);
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
        ///   this is use to check the video call status...
        checkVideoCallStatus();
        callEndButton.setOnClickListener(v -> callEnd());                                           //  use to the instruction after the call end...
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
                    Glide.with(IChatVideoCallConnectingActivity.this).load(user.getImageUrl()).into(profileImage);
                    username.setText(user.getUsername());
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    ///   this function is use to check the video call status...
    private void checkVideoCallStatus() {
        ///   this is the receiver reference...
        DatabaseReference referenceReceiver = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(Objects.requireNonNull(getIntent().getStringExtra("userID")));
        ///   this is the sender reference...
        DatabaseReference referenceSender = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        referenceReceiver.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isReceiverAvailable = Boolean.TRUE.equals(snapshot.child("videoCallStatus").getValue(Boolean.class));
                    Map<String, Object> setVideoCallStatus = new HashMap<>();
                    setVideoCallStatus.put("videoCallStatus", true);
                    referenceSender.updateChildren(setVideoCallStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            ///   checking that receiver is available or not...
                            if(!isReceiverAvailable) {
                                ///   if the receiver is available then make the call...
                                referenceReceiver.updateChildren(setVideoCallStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(IChatVideoCallConnectingActivity.this, "Ringing...", Toast.LENGTH_SHORT).show();
                                        /// playRingTone();

                                        ///   this is use to set the video call status...
                                        setVideoCallStatus();
//                                        new Handler().postDelayed(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                Toast.makeText(IChatVideoCallConnectingActivity.this, "Not answering...", Toast.LENGTH_LONG).show();
//                                                setVideoCallStatus.put("videoCallStatus", false);
//                                                referenceReceiver.updateChildren(setVideoCallStatus).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                    @Override
//                                                    public void onSuccess(Void unused) {
//                                                        referenceSender.updateChildren(setVideoCallStatus).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                            @Override
//                                                            public void onSuccess(Void unused) {
//                                                                finish();
//                                                            }
//                                                        });
//                                                    }
//                                                });
//                                            }
//                                        }, 30000);
                                    }
                                });
                            } else {
                                ///   else finish the call...
                                Toast.makeText(IChatVideoCallConnectingActivity.this, "Busy on another call...", Toast.LENGTH_LONG).show();
                                setVideoCallStatus.put("videoCallStatus", false);
                                referenceSender.updateChildren(setVideoCallStatus).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        finish();
                                    }
                                });
                            }
                        }
                    });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setVideoCallStatus() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String username = mAuth.getUid();                                                           //  use to get the current user id...;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("VideoCallStatus")
                .orderByChild("status")
                .equalTo(0).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getChildrenCount() > 0) {
                            ///   waiting for second user pick the call...
                            isOk = true;
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                if(Objects.equals(dataSnapshot.child("sendTo").getValue(String.class), username)) {
                                    reference.child("VideoCallStatus")
                                            .child(Objects.requireNonNull(dataSnapshot.getKey()))
                                            .child("incoming")
                                            .setValue(username);
                                    reference.child("users")
                                            .child(Objects.requireNonNull(dataSnapshot.getKey()))
                                            .child("status")
                                            .setValue(1);
                                    ///   this will execute after the second user pick the call...
                                    switchOneActivityToAnother(dataSnapshot, username);
                                }
                            }
                        } else {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("incoming", username);   // let remove...
                            map.put("createdBy", username);
                            map.put("sendTo", getIntent().getStringExtra("userID"));   //  let add...
                            map.put("isAvailable", true);
                            map.put("status", 0);
                            assert username != null;
                            reference.child("VideoCallStatus")
                                    .child(username)
                                    .setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {      //   from here the caller send the request...
                                            reference.child("VideoCallStatus")
                                                    .child(username).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if(snapshot.child("status").exists()) {
                                                                Integer status = snapshot.child("status").getValue(Integer.class);
                                                                if(status != null && status.equals(1)) {
                                                                    if(isOk)
                                                                        return;
                                                                    isOk = true;
                                                                    ///   this use when the first user set the status for the video call...
                                                                    switchOneActivityToAnother(snapshot, username);
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void switchOneActivityToAnother(DataSnapshot snapshot, String username) {
        Intent intent = new Intent(IChatVideoCallConnectingActivity.this, IChatVideoCallActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("incoming", snapshot.child("incoming").getValue(String.class));
        intent.putExtra("createdBy", snapshot.child("createdBy").getValue(String.class));
        intent.putExtra("isAvailable", snapshot.child("isAvailable").getValue(Boolean.class));
        startActivity(intent);
        finish();
    }
    private void playRingTone() {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        mediaPlayer.start();
    }

    ///   this is use to end the call and set the data to the firebase database...
    private void callEnd() {
        DatabaseReference referenceSender = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        DatabaseReference referenceReceiver = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(Objects.requireNonNull(getIntent().getStringExtra("userID")));
        Map<String, Object> setVideoCallStatus = new HashMap<>();
        setVideoCallStatus.put("videoCallStatus", false);
        referenceReceiver.updateChildren(setVideoCallStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                referenceSender.updateChildren(setVideoCallStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(IChatVideoCallConnectingActivity.this, "Call ended...",Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }
        });
    }
}