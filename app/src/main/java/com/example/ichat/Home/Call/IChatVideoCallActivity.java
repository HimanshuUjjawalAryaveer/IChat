package com.example.ichat.Home.Call;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.ichat.Model.InterfaceJava;
import com.example.ichat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.UUID;

public class IChatVideoCallActivity extends AppCompatActivity {

    private WebView webView;
    private ImageView micButton, videoButton, callEndButton;
    private boolean isAudio = true;
    private boolean isVideo = true;
    private String uniqueId;

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

        Objects.requireNonNull(getSupportActionBar()).hide();  //  use to hide the action bar...
        init();    //  use to initialize the views...
        setupWebView();     //  use to setup the web view...

//  use to control the toggling function of the mic button...
        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAudio = !isAudio;
                callJavascriptFunction("javascript:toggleAudio(\""+isAudio+"\")");
                if(isAudio) {
                    micButton.setImageResource(R.drawable.mic_unmute);
                } else {
                    micButton.setImageResource(R.drawable.mic_mute);
                }
            }
        });

//   same use for the video call btn...
        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVideo = !isVideo;
                callJavascriptFunction("javascript:toggleVideo(\""+isVideo+"\")");
                if(isVideo) {
                    videoButton.setImageResource(R.drawable.video_unmute);
                } else {
                    videoButton.setImageResource(R.drawable.video_mute);
                }
            }
        });
    }

    private void init() {
        webView = findViewById(R.id.web_view);
        micButton = findViewById(R.id.mic_button);
        videoButton = findViewById(R.id.video_call_btn);
        callEndButton = findViewById(R.id.call_end_btn);
    }


    // use to set up the web view the activity...
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
        webView.addJavascriptInterface(new InterfaceJava(this), "HUA Brothers...");
        loadVideoCall();        //  for loading the video call...
    }


    //  use to load the data from html and javascript files...
    public void loadVideoCall() {
        String filePath = "file:android_asset/call.html";
        webView.loadUrl(filePath);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initializedPeer();
            }
        });
    }

    //  this is error code start...


    //  this code used to initialized peer
    private void initializedPeer() {
        uniqueId = getUniqueId();
        callJavascriptFunction("javascript:init(\"" + uniqueId + "\")");

//        if(createdBy.equalsIgnoreCase(username)) {
//            if (isPageExit)
//                return;
//            reference.child(username).child("connId").setValue(uniqueId);
//            reference.child(username).child("isAvailable").setValue(true);
//            // code for visibility
//            loading.setVisibility(View.GONE);
//            controls.setVisibility(View.VISIBLE);
//
//            // here error will occur
//            // then fetch the data and set to the user profile
//
//            FirebaseDatabase.getInstance().getReference()
//                    .child("User")
//                    .child(friendsUsername)
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            User user = snapshot.getValue(User.class);
//                            assert user != null;
//                            Glide.with(getApplicationContext()).load(user.getImageUrl()).into(userProfileImage);
//                            userName.setText(user.getUserName());
//                            userAddress.setText(user.getAddress());
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//        } else {
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    friendsUsername = createdBy;
//                    FirebaseDatabase.getInstance().getReference()
//                            .child("User")
//                            .child(friendsUsername)
//                            .addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    User user = snapshot.getValue(User.class);
//                                    assert user != null;
//                                    Glide.with(getApplicationContext()).load(user.getImageUrl()).into(userProfileImage);
//                                    userName.setText(user.getUserName());
//                                    userAddress.setText(user.getAddress());
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//
//                                }
//                            });
//                    FirebaseDatabase.getInstance().getReference()
//                            .child("users")
//                            .child(friendsUsername)
//                            .child("connId")
//                            .addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    if(snapshot.getValue() != null) {
//                                        // send call request
////                                        sendCallRequest();
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//
//                                }
//                            });
//                }
//            }, 2000);
//        }

    }

    //  this is error code end...

//  use to generate the random id...
    private String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    //  use to call the javascript functions...
    private void callJavascriptFunction(String function) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript(function, null);
            }
        });
    }
}