package com.example.ichat.Model;

import android.webkit.JavascriptInterface;

import com.example.ichat.Home.Call.IChatVideoCallActivity;

public class InterfaceJava {
    IChatVideoCallActivity iChatVideoCallActivity;
    public InterfaceJava( IChatVideoCallActivity iChatVideoCallActivity) {
        this.iChatVideoCallActivity = iChatVideoCallActivity;
    }
    @JavascriptInterface
    public void onPeerConnected() {
        iChatVideoCallActivity.onPeerConnected();      //create this method in the video call activity...
    }
}
