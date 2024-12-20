package com.example.ichat.Home.Call;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.ichat.Home.ChatMessageActivity;
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

    private DatabaseReference reference;


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

        Objects.requireNonNull(getSupportActionBar()).hide();                                       //  use to hide the action bar...
        init();                                                                                     //  use to initialize the values of the views...
        setUserProfile(getIntent().getStringExtra("userID"));                                 //  use to set the user profile...
        callEndButton.setOnClickListener(v -> callEnd());                                           //  use to the instruction after the call end...


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String username = mAuth.getUid();                                                           //  use to get the current user id...;
        reference = FirebaseDatabase.getInstance().getReference();
        reference.child("users")
                .orderByChild("status")
                .equalTo(0).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getChildrenCount() > 0) {
//                            Room available
                            isOk = true;
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                if(Objects.equals(dataSnapshot.child("sendTo").getValue(String.class), username)) {
                                    reference.child("users")
                                            .child(Objects.requireNonNull(dataSnapshot.getKey()))
                                            .child("incoming")
                                            .setValue(username);
                                    reference.child("users")
                                            .child(Objects.requireNonNull(dataSnapshot.getKey()))
                                            .child("status")
                                            .setValue(1);
                                    Intent intent = new Intent(IChatVideoCallConnectingActivity.this, IChatVideoCallActivity.class);
                                    intent.putExtra("username", username);
                                    intent.putExtra("incoming", dataSnapshot.child("incoming").getValue(String.class));
                                    intent.putExtra("createdBy", dataSnapshot.child("createdBy").getValue(String.class));
                                    intent.putExtra("isAvailable", dataSnapshot.child("isAvailable").getValue(Boolean.class));
                                    startActivity(intent);
                                    finish();
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
                            reference.child("users")
                                    .child(username)
                                    .setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {      //   from here the caller send the request...
                                            reference.child("users")
                                                    .child(username).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if(snapshot.child("status").exists()) {
                                                                Integer status = snapshot.child("status").getValue(Integer.class);
                                                                if(status != null && status.equals(1)) {
                                                                    if(isOk)
                                                                        return;
                                                                    isOk = true;
                                                                    Intent intent = new Intent(IChatVideoCallConnectingActivity.this, IChatVideoCallActivity.class);
                                                                    intent.putExtra("username", username);
                                                                    intent.putExtra("incoming", snapshot.child("incoming").getValue(String.class));
                                                                    intent.putExtra("createdBy", snapshot.child("createdBy").getValue(String.class));
                                                                    intent.putExtra("isAvailable", snapshot.child("isAvailable").getValue(Boolean.class));
                                                                    startActivity(intent);
                                                                    finish();
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

    private void setUserProfile(String uId) {
        if(uId != null) {
            reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(uId);
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

    private void callEnd() {
        reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(Objects.requireNonNull(getIntent().getStringExtra("userID")));
        Map<String, Object> setVideoCallStatus = new HashMap<>();
        setVideoCallStatus.put("videoCallStatus", false);
        reference.updateChildren(setVideoCallStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent = new Intent(IChatVideoCallConnectingActivity.this, ChatMessageActivity.class);
                intent.putExtra("userId", getIntent().getStringExtra("userID"));
                startActivity(intent);
                finish();
            }
        });
    }

    private void init() {
        callEndButton = findViewById(R.id.call_end_btn);
        profileImage = findViewById(R.id.user_profile_image);
        username = findViewById(R.id.username);
    }
}