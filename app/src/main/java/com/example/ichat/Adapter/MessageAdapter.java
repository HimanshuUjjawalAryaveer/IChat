package com.example.ichat.Adapter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ichat.Model.Chats;
import com.example.ichat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<Chats> list;
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
//    private static final String CHANNEL_ID = "HUA";
//    private static final int NOTIFICATION_ID = 10;
    FirebaseUser user;
    String userId;
    public MessageAdapter(Context context, ArrayList<Chats> list, String userId) {
        this.context = context;
        this.list = list;
        this.userId = userId;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(context).inflate(R.layout.right_chat, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.left_chat, parent, false);
        }
        return new MessageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Chats chats = list.get(position);
        holder.showTime.setText(chats.getTime());
        holder.showMessage.setText(chats.getMessage());
        if(chats.getSeen()) {
            setColor(holder.doubleTick, context.getResources().getColor(R.color.double_tick));
        } else {
            setColor(holder.doubleTick, context.getResources().getColor(R.color.white));
        }


//        if(position == list.size()-1) {
//            if(chats.getReceiver().equals(user.getUid()) && chats.getSender().equals(userId)) {
//                // here is the last message from the another person
//                        sendNotification();
//            }
//
//        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView showMessage, showTime;
        ImageButton doubleTick;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            showMessage = itemView.findViewById(R.id.show_message);
            showTime = itemView.findViewById(R.id.show_time);
            doubleTick = itemView.findViewById(R.id.double_tick);
        }
    }

    @Override
    public int getItemViewType(int position) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(list.get(position).getSender().equals(user.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
    private void setColor(ImageButton btn, int color) {
        btn.setBackgroundTintList(ColorStateList.valueOf(color));
    }

//    private void sendNotification() {
////        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.user_profile, null);
//        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
//        assert bitmapDrawable != null;
//        Bitmap largeIcon = bitmapDrawable.getBitmap();
////        Notification notification = new Notification.Builder(this)
////                .setLargeIcon(largeIcon)
////                .setSmallIcon(R.drawable.chat)
////                .setContentText("Himanshu")
////                .setSubText("Ujjawal")
////                .build();
////        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
////            notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "Aryaveer", NotificationManager.IMPORTANCE_HIGH));
////        }
////        notificationManager.notify(NOTIFICATION_ID, notification);
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        Notification notification;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            notification = new Notification.Builder(context)
//                    .setLargeIcon(largeIcon)
//                    .setSmallIcon(R.drawable.chat)
//                    .setContentText("Himanshu")
//                    .setSubText("Ujjawal")
//                    .setAutoCancel(false)
//                    .setOngoing(true)
//                    .setChannelId(CHANNEL_ID)
//                    .build();
//            notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "Arayveer", NotificationManager.IMPORTANCE_HIGH));
//        } else {
//            notification = new Notification.Builder(context)
//                    .setLargeIcon(largeIcon)
//                    .setSmallIcon(R.drawable.chat)
//                    .setContentText("Himanshu")
//                    .setSubText("Ujjawal")
//                    .setAutoCancel(false)
//                    .setOngoing(true)
//                    .build();
//        }
//        notificationManager.notify(NOTIFICATION_ID, notification);
//    }
}

