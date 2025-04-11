package com.example.ichat.Home.HomeActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ichat.Adapter.MessageAdapter;
import com.example.ichat.Home.Call.IChatCallReceiveActivity;
import com.example.ichat.Home.Call.IChatCallStartActivity;
import com.example.ichat.Home.Profile.ProfileActivity;
import com.example.ichat.Model.Chats;
import com.example.ichat.Model.User;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatMessageActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView image, userProfile, incomingCallImage;
    private ImageView sendingImage;
    private TextView username, status, incomingCallUsername, incomingCallStatus;
    private EditText message;
    private ImageButton messageSendBtn, cancelButton, sendButton;
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
    private User callUser;
    private ConstraintLayout imageBlock;
    private Uri imageUri;
    private ActivityResultLauncher<Intent> galleryLauncher;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_message);

        ///   use to initialize the view...
        init();
        ///   use to show the status bar...
        setStatusBar();
        ///   use to set the toolbar or action bar...
        setToolbar();
        ///  This is used to set the back button on the toolbar...
        toolbar.setNavigationOnClickListener((View v) -> finish());
        ///  this is use to set the recycler view horizontally for the message...
        setRecyclerView(getApplicationContext());
        ///  this is use to load the data from the database...
        loadData();
        ///  check it is video call or not...
        checkVideoCall();

        ///  use to send the text message...
        messageSendBtn.setOnClickListener((View v) -> {
                String chatMessage = message.getText().toString();
                if(!chatMessage.isEmpty()) {
                    sendMessage(user.getUid(), uID, chatMessage, setTime()[0], setTime()[1], "text");
                }
                message.setText("");
            });
        ///  use to answer the call...
        callAnswer.setOnClickListener((View v) -> {
            if(isPermissionGranted()) {
                startVideoCallByReceiverSide();
            } else {
                askPermission();
            }
        });
        ///  use to decline the call...
        callDecline.setOnClickListener(v -> callEndButtonFunctionality());
        ///  use to send the photo message...
        message.setOnTouchListener((v, event) -> setClickOnTheCamera(event, message));
        ///  to set the cancel button functionality...
        cancelButton.setOnClickListener(v -> setCancelButtonFunctionality());
        ///  use to send the image message...
        sendButton.setOnClickListener(v -> sendImageMessage(user.getUid(), uID, imageUri, setTime()[0], setTime()[1]));
    }

    ///  initialization function...
    private void init() {
        ///  this is use to initialize the view...
        toolbar = findViewById(R.id.toolbar);
        image = findViewById(R.id.profile_image);
        username = findViewById(R.id.userName);
        status = findViewById(R.id.status);
        message = findViewById(R.id.message);
        messageSendBtn = findViewById(R.id.message_send_btn);
        recyclerView = findViewById(R.id.recycler_view);
        call = findViewById(R.id.call);
        callAnswer = findViewById(R.id.call_answer);
        callDecline = findViewById(R.id.call_decline);
        incomingCallImage = findViewById(R.id.incoming_call_img);
        incomingCallUsername = findViewById(R.id.incoming_call_username);
        incomingCallStatus = findViewById(R.id.incoming_call_status);
        sendingImage = findViewById(R.id.sending_image);
        imageBlock = findViewById(R.id.image_block);
        cancelButton = findViewById(R.id.cancel_button);
        sendButton = findViewById(R.id.send_button);
        userProfile = findViewById(R.id.user_profile);

        ///  this is use to open the gallery and pick the images...
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                imageUri = result.getData().getData();
                readyToSendImage(imageUri);
            }
        });

        ///  this is use to get the data of the another user in advance...
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(getString(R.string.User)).child(Objects.requireNonNull(getIntent().getStringExtra("userId")));
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callUser = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    ///  use to show the status popup of the video call...
    private void checkVideoCall() {
        reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user1 = snapshot.getValue(User.class);
                assert user1 != null;
                if(user1.isVideoCallStatus()) {
                    Glide.with(getApplicationContext()).load(callUser.getImageUrl()).into(incomingCallImage);
                    String name = getCapitalText(callUser.getUsername());
                    incomingCallUsername.setText(name);
                    incomingCallStatus.setText("incoming call from " + name);
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

    ///  use to load the data from the database like previous chat message and the user credentials...
    private void loadData() {
        ///  this is use to get the data of that particular use on which the use is clicked in the HomeActivity recycler view ...
        Intent intent = getIntent();
        ///  this is the user by which the user is clicked...
        uID = intent.getStringExtra("userId");
        ///  this is use to get the current user...
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
                    ///  this is use to read the previous message from the database...
                    readMessage(user.getUid(), uID);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            ///  this is use the check that the user seen the message or not...
            messageSeen(uID);
        }
    }

    ///  this is use to read the previous message from the database...
    private void readMessage(final String myId, final String userId) {
        chats = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference(getString(R.string.Chats));
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
                ///  now the value of the previous chat is stored in the chats Arraylist and now it is sent to the message adapter to set in the recycler view...
                messageAdapter = new MessageAdapter(ChatMessageActivity.this, chats, userId);
                recyclerView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    ///  this is used to check that the user seen the message or not...
    private void messageSeen(final String userId) {
        reference = FirebaseDatabase.getInstance().getReference(getString(R.string.Chats));
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


    ///   this is use to send the message and photos to the database...
    private void sendMessage(String sender, String receiver, String message, String time, String date, String messageType) {

        DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.Chats));
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(receiver);

        ///  getting the unique id for the chat and then using it to send the message to the database...
        String uniqueId = chatReference.push().getKey();
        HashMap<String, Object> userChat = new HashMap<>();
        userChat.put("chatId", uniqueId);
        userChat.put("sender", sender);
        userChat.put("receiver", receiver);
        userChat.put("message", message);
        userChat.put("seen", false);
        userChat.put("messageType", messageType);
        userChat.put("time", time);
        userChat.put("date", date);
        userChat.put("timestamp", System.currentTimeMillis());
        userChat.put("feeling", -1);
        assert uniqueId != null;
        chatReference.child(uniqueId).setValue(userChat);

        ///  now update the last chatting time of the User...
        HashMap<String, Object> userLastChatTime = new HashMap<>();
        userLastChatTime.put("timestamp", System.currentTimeMillis());
        userReference.updateChildren(userLastChatTime);
    }


    ///   use to sent the photo message...
    private boolean setClickOnTheCamera(@NonNull MotionEvent event, EditText editText) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {
                openGallery();
                return true;
            }
        }
        return false;
    }

    ///  use to open the gallery and pick the images...
    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(gallery);
    }

    ///  make ready the image uri to send the data to the database...
    private void readyToSendImage(Uri imageUri) {
        ///  this is use to get the current user image...
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(getString(R.string.user));
        reference1.child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user1 = snapshot.getValue(User.class);
                assert user1 != null;
                Glide.with(getApplicationContext()).load(user1.getImageUrl()).into(userProfile);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        imageBlock.setVisibility(View.VISIBLE);
        sendingImage.setImageURI(imageUri);
    }

    ///  to set the cancel button functionality...
    private void setCancelButtonFunctionality() {
        imageBlock.setVisibility(View.GONE);
    }

    ///  this is use to upload the image to the firebase storage then generate the url and then store the data to the database...
    private void sendImageMessage(String sender, String receiver, Uri imageUri, String time, String date) {

        imageBlock.setVisibility(View.GONE);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create unique file reference
        StorageReference storeRef = storage.getReference("ChatImage").child("images/" + sender + System.currentTimeMillis() + ".jpg");
        // Upload file to Firebase Storage
        storeRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL after successful upload
                    storeRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Get the URL as a string
                        String imageUrl = uri.toString();

                        sendMessage(sender, receiver, imageUrl, time, date, "image");

                    }).addOnFailureListener(e -> Toast.makeText(ChatMessageActivity.this, "Failed to get URL: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e -> Toast.makeText(ChatMessageActivity.this, "Upload failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }


    ///  use to create the option menu...
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_profile_menu, menu);
        return true;
    }

    ///  use to set the functionality of the menu options...
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
                Toast.makeText(ChatMessageActivity.this, "call send...", Toast.LENGTH_LONG).show();
                startVideoCallBySenderSide();
            } else {
                askPermission();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    ///   this is use to start the video call by the sender side...
    private void startVideoCallBySenderSide() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("VideoCallStartStatus").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null) {
                    databaseReference.setValue(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Intent intent = new Intent(ChatMessageActivity.this, IChatCallStartActivity.class);
        intent.putExtra("userID", uID);
        startActivity(intent);
        finish();
    }

    ///  this is use when the user resume the app...
    @Override
    protected void onResume() {
        super.onResume();
        setStatus("online");
    }

    ///  this is use to pause the app...
    @Override
    protected void onPause() {
        super.onPause();
        if(valueEventListener != null) {
            reference.removeEventListener(valueEventListener);
        }
        setStatus("offline");
    }

    ///  use to check that the user has the camera or mic permission or not...
    private boolean isPermissionGranted() {
        for(String permission : permissions) {
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    ///  use to ask for the permission if the user has not the permission...
    private void askPermission() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
    }

    ///  use to set the video call status in the firebase database...
    private void startVideoCallByReceiverSide() {
        Intent intent = new Intent(ChatMessageActivity.this, IChatCallReceiveActivity.class);
        intent.putExtra("userID", uID);
        startActivity(intent);
        finish();
    }

    ///  this is use to set the color and other functionality of the status bar...
    private void setStatusBar() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.light_blue));
    }

    ///  this is use to set the toolbar or action bar...
    private void setToolbar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    ///  this is mainly use to capitalize the first letter of the string...
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

    ///  this is use to set the time and date...
    private String[] setTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");
        Date date = new Date();
        return new String[]{timeFormatter.format(date), dateFormatter.format(date)};
    }

    ///  use to set the recycler view horizontally for the message...
    private void setRecyclerView(Context context) {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    ///  use to set the current status(online/offline) of the user...
    private void setStatus(String status) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(user.getUid());
        Map<String, Object> updateChild = new HashMap<>();
        updateChild.put("status", status);
        reference.updateChildren(updateChild);
    }

    ///  this is use to call end...
    private void callEndButtonFunctionality() {
        DatabaseReference referenceUser = FirebaseDatabase.getInstance().getReference(getString(R.string.User));
        referenceUser.child(Objects.requireNonNull(getIntent().getStringExtra("userId"))).child("videoCallStatus").setValue(false).addOnCompleteListener(task -> referenceUser.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("videoCallStatus").setValue(false).addOnCompleteListener(task1 -> {
            call.setVisibility(View.GONE);
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            databaseReference.child("VideoCallStartStatus").child(Objects.requireNonNull(getIntent().getStringExtra("userId"))).setValue(null);
        }));
    }

}