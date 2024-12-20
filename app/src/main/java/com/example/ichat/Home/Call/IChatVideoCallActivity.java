package com.example.ichat.Home.Call;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.ichat.Model.InterfaceJava;
import com.example.ichat.Model.User;
import com.example.ichat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        HideStatusBar();
        init();                                             //  use to initialize the views value as well as database value...
        setupWebView();                                     //  use to setup the web view and enables the javascript in the activity...
        micButton.setOnClickListener(v -> {
            isAudio = !isAudio;
            callJavascriptFunction("javascript:toggleAudio(\""+isAudio+"\")");
            if(isAudio) {
                micButton.setImageResource(R.drawable.mic_unmute);
            } else {
                micButton.setImageResource(R.drawable.mic_mute);
            }
        });           //  use to set the functionality of the mic button...
        videoButton.setOnClickListener(v -> {
            isVideo = !isVideo;
            callJavascriptFunction("javascript:toggleVideo(\""+isVideo+"\")");
            if(isVideo) {
                videoButton.setImageResource(R.drawable.video_unmute);
            } else {
                videoButton.setImageResource(R.drawable.video_mute);
            }
        });         //  use to set the functionality of the video button...
        callEndButton.setOnClickListener(v -> finish());    //  use to set the functionality of the call end button...
    }

    private void init() {
        webView = findViewById(R.id.web_view);              //  use to initialize the values of the views...
        micButton = findViewById(R.id.mic_button);
        videoButton = findViewById(R.id.video_call_btn);
        callEndButton = findViewById(R.id.call_end_btn);
        userProfileImage = findViewById(R.id.user_profile_image);
        userName = findViewById(R.id.user_name);
        controls = findViewById(R.id.controls);

        reference = FirebaseDatabase.getInstance().getReference().child("users");         //  use to initialize the value of the database...
        username = getIntent().getStringExtra("username");
        incoming = getIntent().getStringExtra("incoming");
        createdBy = getIntent().getStringExtra("createdBy");
        friendsUsername = incoming;
    }

    @SuppressLint("SetJavaScriptEnabled")
    void setupWebView() {
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.addJavascriptInterface(new InterfaceJava(this), "Android");                 //  to write Android is mandatory...
        loadVideoCall();
    }

    public void loadVideoCall() {
        webView.loadUrl(getString(R.string.filePath));
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initializedPeer();
            }
        });
    }

    private void initializedPeer() {
        String uniqueId = getUniqueId();
        callJavascriptFunction("javascript:init(\"" + uniqueId + "\")");

        if(createdBy.equalsIgnoreCase(username)) {
            if (isPageExit)
                return;
            reference.child(username).child("connId").setValue(uniqueId);                 //  code to set the values of the data required in the database...
            reference.child(username).child("isAvailable").setValue(true);
            controls.setVisibility(View.VISIBLE);                                                   //  code to set the visibility of the views...
            FirebaseDatabase.getInstance().getReference()                                           //  code to fetch the user details from the database...
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
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    friendsUsername = createdBy;
                    FirebaseDatabase.getInstance().getReference()                                   //  code to fetch the user details from the database...
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

                    FirebaseDatabase.getInstance().getReference()                                   //  code use to send the call request to the another user...
                            .child("users")
                            .child(friendsUsername)
                            .child("connId")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
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
                }
            }, 2000);
        }
    }

    private void sendCallRequest() {
        if(!isPeerConnected) {
            Toast.makeText(this, "Not connected. Please check your internet connection.", Toast.LENGTH_LONG).show();
            return;
        }
        listenConnId();                                                                             //  use to searching the connection id of the another user...
    }

    private void listenConnId() {
        reference.child(friendsUsername).child("connId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null)
                    return;

                controls.setVisibility(View.VISIBLE);                                               //  use to set the visibility of the layout if the call is connected...
                String connId = snapshot.getValue(String.class);
                callJavascriptFunction("javascript:startCall(\"" +connId+ "\")");                   //  use to connect the call to this particular connection id...
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getUniqueId() {
        return UUID.randomUUID().toString();                                                        //  use to get the random id of the user...
    }

    public void onPeerConnected() {
        isPeerConnected = true;
    }

    private void callJavascriptFunction(String function) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript(function, null);
            }
        });
    }


    protected void onDestroy() {                                                                    //  use to set finish the call activity...
        super.onDestroy();
        isPageExit = true;
        reference.child(createdBy).setValue(null);
        setFirebaseData();
    }

    private void HideStatusBar() {
        Objects.requireNonNull(getSupportActionBar()).hide();                                       //  use to hide the action bar...
        View decoderView = getWindow().getDecorView();
        decoderView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                |View.SYSTEM_UI_FLAG_FULLSCREEN
                |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void setFirebaseData() {
        reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user));
        reference.child(incoming).child("videoCallStatus").setValue(false);
        reference.child(createdBy).child("videoCallStatus").setValue(false);
    }
}