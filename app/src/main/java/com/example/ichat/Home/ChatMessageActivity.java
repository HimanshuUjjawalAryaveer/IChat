package com.example.ichat.Home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ichat.Adapter.MessageAdapter;
import com.example.ichat.Home.Call.IChatVideoCallConnectingActivity;
import com.example.ichat.Home.Profile.ProfileActivity;
import com.example.ichat.Model.Chats;
import com.example.ichat.Model.User;
import com.example.ichat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatMessageActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView image;
    private TextView username, status;
    private EditText message;
    private ImageButton messageSendBtn;
    private RecyclerView recyclerView;
    private String uID;
    private FirebaseUser user;
    private DatabaseReference reference;
    private MessageAdapter messageAdapter;
    private ArrayList<Chats> chats;
    private ValueEventListener valueEventListener;
    private final String[] permissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_CODE = 3;
    private LinearLayout call, callAnswer, callDecline;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_message);
        init();
        setStatusBar();
        setToolbar();
        toolbar.setNavigationOnClickListener((View v) -> finish());
        setRecyclerView(getApplicationContext());
        loadData();
        checkVideoCall();
        messageSendBtn.setOnClickListener((View v) -> {
                String chatMessage = message.getText().toString();
                if(!chatMessage.isEmpty()) {
                    sendMessage(user.getUid(), uID, chatMessage, setTime()[0], setTime()[1]);
                }
                message.setText("");
            });

        callAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPermissionGranted()) {
                    setVideoCallStatus();
                } else {
                    askPermission();
                }
            }
        });
    }

    private void checkVideoCall() {
        reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user1 = snapshot.getValue(User.class);
                assert user1 != null;
                if(user1.isVideoCallStatus()) {
                    call.setVisibility(View.VISIBLE);
                } else {
                    call.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String[] setTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");
        Date date = new Date();
        return new String[]{timeFormatter.format(date), dateFormatter.format(date)};
    }
    private void loadData() {
        Intent intent = getIntent();
        uID = intent.getStringExtra("userId");
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(uID != null) {
            reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(uID);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user1 = snapshot.getValue(User.class);
                    assert user1 != null;
                    username.setText(getCapitalText(user1.getUsername()));
                    status.setText(user1.getStatus());
                    if (user1.getImageUrl() != null) {
                        if(!ChatMessageActivity.this.isDestroyed() && ! ChatMessageActivity.this.isFinishing()) {
                            Glide.with(ChatMessageActivity.this).load(user1.getImageUrl()).into(image);
                        }
                    }
                    readMessage(user.getUid(), uID);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            messageSeen(uID);
        }
    }
    private static String getCapitalText(String string) {
        if (!Objects.equals(string, "") && string != null) {
            String[] str =string.split(" ");
            for(int i=0; i<str.length; i++) {
                str[i] = str[i].substring(0,1).toUpperCase() + str[i].substring(1).toLowerCase();
            }
            return String.join(" ", str);
        }
        return "";
    }

    private void init() {
        toolbar = findViewById(R.id.toolbar);
        image = findViewById(R.id.image);
        username = findViewById(R.id.userName);
        status = findViewById(R.id.status);
        message = findViewById(R.id.message);
        messageSendBtn = findViewById(R.id.message_send_btn);
        recyclerView = findViewById(R.id.recycler_view);
        call = findViewById(R.id.call);
        callAnswer = findViewById(R.id.call_answer);
        callDecline = findViewById(R.id.call_decline);
    }
    private void setRecyclerView(Context context) {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
    }
    private void sendMessage(String sender, String receiver, String message, String time, String date) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(receiver);
        HashMap<String, Object> userChat = new HashMap<>();
        HashMap<String, Object> userLastChatTime = new HashMap<>();
        userChat.put("sender", sender);
        userChat.put("receiver", receiver);
        userChat.put("message", message);
        userChat.put("seen", false);
        userChat.put("time", time);
        userChat.put("date", date);
        userChat.put("timestamp", System.currentTimeMillis());
        reference.child("Chats").push().setValue(userChat);
        userLastChatTime.put("timestamp", System.currentTimeMillis());
        userReference.updateChildren(userLastChatTime);
    }
    private void setToolbar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    private void setStatusBar() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.light_blue));
    }
    private void readMessage(final String myId, final String userId) {
        chats = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chats.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chats chat = dataSnapshot.getValue(Chats.class);
                    assert chat != null;
                    if(chat.getReceiver().equals(myId) && chat.getSender().equals(userId) || chat.getReceiver().equals(userId) && chat.getSender().equals(myId)) {
                        chats.add(chat);
                    }
                }
                messageAdapter = new MessageAdapter(ChatMessageActivity.this, chats, userId);
                recyclerView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        new MenuInflater(ChatMessageActivity.this).inflate(R.menu.user_profile_menu, menu);
//        return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.user_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();
        if(itemID == R.id.profile) {
            if(!uID.isEmpty()) {
                Intent intent1 = new Intent(ChatMessageActivity.this, ProfileActivity.class);
                intent1.putExtra("userID", uID);
                startActivity(intent1);
            }
        } else {
            if(isPermissionGranted()) {
                Toast.makeText(ChatMessageActivity.this, "set status", Toast.LENGTH_LONG).show();
                setVideoCallStatus();   //  set status for video call...
            } else {
                askPermission();
            }
        }
        return super.onOptionsItemSelected(item);
    }


    private void setStatus(String status) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(user.getUid());
        Map<String, Object> updateChild = new HashMap<>();
        updateChild.put("status", status);
        reference.updateChildren(updateChild);
    }

    private void messageSeen(final String userId) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        valueEventListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chats chats1 = dataSnapshot.getValue(Chats.class);
                    assert chats1 != null;
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    if(chats1.getReceiver().equals(user.getUid()) && chats1.getSender().equals(userId)) {
                        Map<String, Object> updateChild = new HashMap<>();
                        updateChild.put("seen", true);
                        dataSnapshot.getRef().updateChildren(updateChild);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(valueEventListener != null) {
            reference.removeEventListener(valueEventListener);
        }
        setStatus("offline");
    }

    private boolean isPermissionGranted() {
        for(String permission : permissions) {
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }
    private void askPermission() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
    }

    private void setVideoCallStatus() {
        if(uID != null) {
            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(uID);
            Map<String, Object> setVideoCallStatus = new HashMap<>();
            setVideoCallStatus.put("videoCallStatus", true);
            reference1.updateChildren(setVideoCallStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Intent intent = new Intent(ChatMessageActivity.this, IChatVideoCallConnectingActivity.class);
                    intent.putExtra("userID", uID);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
}