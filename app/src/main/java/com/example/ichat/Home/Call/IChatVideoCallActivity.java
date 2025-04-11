package com.example.ichat.Home.Call;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.ichat.Home.HomeActivity.ChatMessageActivity;
import com.example.ichat.Model.InterfaceJava;
import com.example.ichat.Model.User;
import com.example.ichat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class IChatVideoCallActivity extends AppCompatActivity {

    private WebView webView;
    private ImageView micButton, videoButton, callEndButton;
    private CircleImageView userProfileImage;
    private TextView userName;
    private GridLayout controls;
    private boolean isAudio = true;
    private boolean isVideo = true;
    String username = "";
    String friendsUsername = "";
    boolean isPeerConnected = false;
    DatabaseReference reference;
    String createdBy;
    String incoming;
    boolean isPageExit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ichat_video_call);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ///   this is use to hide the status bar and action bar...
        HideStatusBar();
        ///   this is use to initialize the values of the views...
        init();
        ///   this is use to setup the web view and enables the javascript in the activity...
        setupWebView();
        ///   this is use to enable or disable the mic button...
        micButton.setOnClickListener(v -> setMicButtonFunctionality());
        ///   this is use to enable or disable the video button...
        videoButton.setOnClickListener(v -> setVideoButtonFunctionality());
        ///   this is use to set the functionality of the call end button...
        callEndButton.setOnClickListener(v -> checkStatusForCallEnd());
        ///   this is use to exit the page automatically if the status become null and this function check this continuously...
        checkDatabaseStatus();
    }

    ///   this is use to initialize the values of the views and values...
    private void init() {
        webView = findViewById(R.id.web_view);
        micButton = findViewById(R.id.mic_button);
        videoButton = findViewById(R.id.video_call_btn);
        callEndButton = findViewById(R.id.call_end_btn);
        userProfileImage = findViewById(R.id.user_profile_image);
        userName = findViewById(R.id.user_name);
        controls = findViewById(R.id.controls);

        reference = FirebaseDatabase.getInstance().getReference().child("videoCallStatus");         //  use to initialize the value of the database...
        username = getIntent().getStringExtra("currentUser");
        incoming = getIntent().getStringExtra("sendTo");
        createdBy = getIntent().getStringExtra("sendBy");
        friendsUsername = incoming;
    }

    ///   this is use to setup the web view and enables the javascript in the activity...
    @SuppressLint("SetJavaScriptEnabled")
    void setupWebView() {
        ///   here only set web view functionality...
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        ///   here to write "Android" is mandatory because it is the Password...
        webView.addJavascriptInterface(new InterfaceJava(this), "Android");
        ///   now load the video call interface to the activity...
        loadVideoCall();
    }

    ///   this is use to load the video call interface to the activity...
    public void loadVideoCall() {
        webView.loadUrl(getString(R.string.filePath));
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                ///   this is use to initialize the peer to peer connection between the two users...
                initializedPeer();
            }
        });
    }

    ///   this is use to initialize the peer to peer connection between the two users...
    private void initializedPeer() {
        ///   this getUniqueId is use to generate the random id for the user...
        String uniqueId = getUniqueId();
        ///   this callJavascriptFunction is use to call the javascript function...
        callJavascriptFunction("javascript:init(\"" + uniqueId + "\")");
        ///    here username contains the id of the currentUser and createdBy is also contain the id of the current user..
        if(username.equalsIgnoreCase(createdBy)) {
            if (isPageExit)
                return;
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("VideoCallStatus");
            Map<String, Object> status = new HashMap<>();
            status.put("sendTo", incoming);
            status.put("sendBy", username);
            status.put("connId", uniqueId);
            status.put("isAvailable", true);
            reference.child(username).setValue(status);
            controls.setVisibility(View.VISIBLE);
            ///   here friendsUsername contains the id of the other user...
            ///   the following code is use to fetch the details of the other user from the database...
            FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.user))
                    .child(friendsUsername)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            assert user != null;
                            Glide.with(getApplicationContext()).load(user.getImageUrl()).into(userProfileImage);
                            userName.setText(user.getUsername());
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
        else {
            new Handler().postDelayed(() -> {
                friendsUsername = createdBy;
                ///   here friendsUsername contains the id of the other user...
                ///   the following code is use to fetch the details of the current user from the database...
                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.user))
                        .child(friendsUsername)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                assert user != null;
                                Glide.with(getApplicationContext()).load(user.getImageUrl()).into(userProfileImage);
                                userName.setText(user.getUsername());
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                ///   if the connId is setup once successfully then request for the video call...
                FirebaseDatabase.getInstance().getReference()
                        .child("VideoCallStatus")
                        .child(friendsUsername)
                        .child("connId")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.getValue() != null) {
                                    sendCallRequest();                                          //  use to send the call request to the another user...
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }, 2000);
        }
    }

    ///   this is used to send the call request to the another user...
    private void sendCallRequest() {
        if(!isPeerConnected) {
            Toast.makeText(this, "Not connected. Please check your internet connection.", Toast.LENGTH_LONG).show();
            return;
        }
        listenConnId();                                                                             //  use to searching the connection id of the another user...
    }

    ///   now the listen the connection id of the another user...
    private void listenConnId() {
        FirebaseDatabase.getInstance().getReference()
                .child("VideoCallStatus")
                .child(friendsUsername)
                .child("connId")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.getValue() == null) {
                            return;
                        }
                        ///   this is code is runs only if the connection id is setup successfully...
                        controls.setVisibility(View.VISIBLE);
                        String connId = snapshot.getValue(String.class);
                        callJavascriptFunction("javascript:startCall(\"" +connId+ "\")");
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    ///   this is use to generate the random id for the user...
    @NonNull
    private String getUniqueId() {
        return UUID.randomUUID().toString();                                                        //  use to get the random id of the user...
    }

    ///   this function is use when the peer is connected...
    ///   and if the peer is connected then the isPeerConnected is set to true...
    public void onPeerConnected() {
        isPeerConnected = true;
    }

    ///   this is use to set the functionality of the mic button...
    private void setMicButtonFunctionality() {
        isAudio = !isAudio;
        callJavascriptFunction("javascript:toggleAudio(\""+isAudio+"\")");
        if(isAudio) {
            micButton.setImageResource(R.drawable.mic_unmute);
        } else {
            micButton.setImageResource(R.drawable.mic_mute);
        }
    }

    ///   this is use to set the functionality of the video button...
    private void setVideoButtonFunctionality() {
        isVideo = !isVideo;
        callJavascriptFunction("javascript:toggleVideo(\""+isVideo+"\")");
        if(isVideo) {
            videoButton.setImageResource(R.drawable.video_unmute);
        } else {
            videoButton.setImageResource(R.drawable.video_mute);
        }
    }

    ///   this is use to call the javascript function...
    private void callJavascriptFunction(String function) {
        webView.post(() -> webView.evaluateJavascript(function, null));
    }

    ///   this is use to hide the status bar and action bar...
    private void HideStatusBar() {
        Objects.requireNonNull(getSupportActionBar()).hide();
        View decoderView = getWindow().getDecorView();
        decoderView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        |View.SYSTEM_UI_FLAG_FULLSCREEN
                        |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    ///   this is use to set the database data null and come back to the previous activity from the last activity...
    private void checkStatusForCallEnd() {
        callJavascriptFunction("javascript:toggleAudio(\""+ false +"\")");
        isPageExit = true;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(getString(R.string.User)).child(createdBy).child("videoCallStatus").setValue(false).addOnCompleteListener(comp1 -> databaseReference.child(getString(R.string.User)).child(incoming).child("videoCallStatus").setValue(false).addOnCompleteListener(comp2 -> {
            databaseReference.child("VideoCallStartStatus").child(createdBy).setValue(null);
            databaseReference.child("VideoCallStatus").child(createdBy).setValue(null);
            Intent intent = new Intent(IChatVideoCallActivity.this, ChatMessageActivity.class);
            intent.putExtra("userId", getIntent().getStringExtra("trackLastActivity"));
            startActivity(intent);
            finishAffinity();
        }));
    }

    ///   this is use to exit the page automatically if the status become null and this function check this continuously...
    private void checkDatabaseStatus() {
        callJavascriptFunction("javascript:toggleAudio(\""+ false +"\")");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("VideoCallStartStatus").child(createdBy);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null) {
                    Intent intent = new Intent(IChatVideoCallActivity.this, ChatMessageActivity.class);
                    intent.putExtra("userId", getIntent().getStringExtra("trackLastActivity"));
                    startActivity(intent);
                    finishAffinity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}