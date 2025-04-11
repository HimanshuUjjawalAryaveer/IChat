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

    ///   this is use to show the progress dialog...
    public void show() {
        dialog.show();
    }

    ///   this is use to dismiss the progress dialog...
    public void dismiss() {
        dialog.dismiss();
    }

    ///   this is use to set the title of the dialog box...
    public void setTitle(String title) {
        titleView.setText(title);
    }

    ///   this is use to set the message of the dialog box...
    public void setMessage(String message) {
        messageView.setText(message);
    }

    ///   this is use to set the cancelable of the dialog box...
    public void setCancelable(boolean cancelable) {
        dialog.setCancelable(cancelable);
    }
}
