package com.example.ichat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ichat.Home.Activity.IChatImageShowActivity;
import com.example.ichat.Model.Chats;
import com.example.ichat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<Chats> list;
    public static final int MSG_TYPE_TEXT_LEFT = 0;
    public static final int MSG_TYPE_TEXT_RIGHT = 1;
    public static final int MSG_TYPE_IMAGE_LEFT = 2;
    public static final int MSG_TYPE_IMAGE_RIGHT = 3;
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
        if(viewType == MSG_TYPE_TEXT_RIGHT) {
            view = LayoutInflater.from(context).inflate(R.layout.right_chat, parent, false);
        } else if(viewType == MSG_TYPE_TEXT_LEFT) {
            view = LayoutInflater.from(context).inflate(R.layout.left_chat, parent, false);
        } else if(viewType == MSG_TYPE_IMAGE_RIGHT) {
            view = LayoutInflater.from(context).inflate(R.layout.right_image_chat, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.left_image_chat, parent, false);
        }
        return new MessageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Chats chats = list.get(position);
        holder.showTime.setText(chats.getTime());
        if(chats.getMessageType().equals("text")) {
            holder.showMessage.setText(chats.getMessage());
        } else {
            Glide.with(context).load(chats.getMessage()).into(holder.showImage);

            //  set the zoom functionality of the image...
            holder.showImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, IChatImageShowActivity.class);
                    intent.putExtra("imageUrl", chats.getMessage());
                    intent.putExtra("userId", userId);
                    context.startActivity(intent);
                }
            });

        }
        if(chats.getSeen()) {
            setColor(holder.doubleTick, context.getResources().getColor(R.color.double_tick));
        } else {
            setColor(holder.doubleTick, context.getResources().getColor(R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView showMessage, showTime;
        ImageButton doubleTick;
        ImageView showImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            showMessage = itemView.findViewById(R.id.show_message);
            showTime = itemView.findViewById(R.id.show_time);
            doubleTick = itemView.findViewById(R.id.double_tick);
            showImage = itemView.findViewById(R.id.show_image);
        }
    }

    @Override
    public int getItemViewType(int position) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(list.get(position).getSender().equals(user.getUid())) {
            if(list.get(position).getMessageType().equals("text"))
                return MSG_TYPE_TEXT_RIGHT;
            else
                return MSG_TYPE_IMAGE_RIGHT;
        } else {
            if(list.get(position).getMessageType().equals("text"))
                return MSG_TYPE_TEXT_LEFT;
            else
                return MSG_TYPE_IMAGE_LEFT;
        }
    }
    private void setColor(ImageButton btn, int color) {
        btn.setBackgroundTintList(ColorStateList.valueOf(color));
    }
}

