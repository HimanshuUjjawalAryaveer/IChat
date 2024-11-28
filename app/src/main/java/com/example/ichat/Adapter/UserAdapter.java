package com.example.ichat.Adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ichat.Home.ChatMessageActivity;
import com.example.ichat.Model.Chats;
import com.example.ichat.R;
import com.example.ichat.Model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    Context context;
    ArrayList<Users> list;
    boolean isChat;
    private String lastMessage, lastDate;
//    private static final String CHANNEL_ID = "HUA";
//    private static final int NOTIFICATION_ID = 10;
    public UserAdapter(Context context, ArrayList<Users> list, boolean isChat) {
        this.context = context;
        this.list = list;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_block, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users users = list.get(position);
        holder.username.setText(getCapitalText(users.getUserName()));
        holder.lastTime.setText("09:00 AM");

        if(isChat) {
            lastMessage(users.getId(), holder.lastMessage, holder.lastTime);
        } else {
            holder.lastMessage.setVisibility(View.GONE);
            holder.lastTime.setVisibility(View.GONE);
        }

        if(users.getImage() == null) {
            holder.image.setImageResource(R.drawable.user_profile);
        } else {
            Glide.with(context).load(users.getImage()).into(holder.image);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatMessageActivity.class);
                intent.putExtra("userId", users.getId());
                context.startActivity(intent);
            }
        });
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.image_show_block);
                ImageView imageView = dialog.findViewById(R.id.show_image);
                TextView textView = dialog.findViewById(R.id.userName);
                textView.setText(getCapitalText(users.getUserName()));
                Glide.with(context).load(users.getImage()).into(imageView);
                dialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView username, lastMessage, lastTime;
        CircleImageView image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            image = itemView.findViewById(R.id.image);
            lastMessage = itemView.findViewById(R.id.last_message);
            lastTime = itemView.findViewById(R.id.last_time);
        }
    }
    private static String getCapitalText(String string) {
        if (!Objects.equals(string, "") && string != null) {
            String[] str =string.split(" ");
            for(int i=0; i<str.length; i++) {
                str[i] = str[i].substring(0,1).toUpperCase() + str[i].substring(1);
            }
            return String.join(" ", str);
        }
        return "";
    }

    private void lastMessage(final String userId, final TextView last_message, final TextView last_time) {
        lastMessage = "default";
        lastDate = "12/12/12";
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chats chats = dataSnapshot.getValue(Chats.class);
                    assert chats != null;
                    assert user != null;
                    if(chats.getReceiver().equals(user.getUid()) && chats.getSender().equals(userId) || chats.getReceiver().equals(userId) && chats.getSender().equals(user.getUid())) {
                        lastMessage = chats.getMessage();
                        lastDate = chats.getDate();
                    }
                }
                if (lastMessage.equals("default")) {
                    last_message.setText("No Message");
                } else {
                    last_message.setText(lastMessage);
                    last_time.setText(checkDate(lastDate));
                }
                lastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private String checkDate(String date) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date yesterday = calendar.getTime();
        if(formatter.format(today).equals(date)) {
            return "today";
        } else if(formatter.format(yesterday).equals(date)) {
            return "yesterday";
        } else {
            return date;
        }
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
