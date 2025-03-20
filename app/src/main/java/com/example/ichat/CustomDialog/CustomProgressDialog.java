package com.example.ichat.CustomDialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import com.example.ichat.R;

public class CustomProgressDialog {
    private final Dialog dialog;
    private final TextView titleView;
    private final TextView messageView;
    public CustomProgressDialog(Context context) {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_progress_dialog);

        titleView = dialog.findViewById(R.id.title);
        messageView = dialog.findViewById(R.id.message);
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public void setTitle(String title) {
        titleView.setText(title);
    }

    public void setMessage(String message) {
        messageView.setText(message);
    }

    public void setCancelable(boolean cancelable) {
        dialog.setCancelable(cancelable);
    }
}
