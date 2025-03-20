package com.example.ichat.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ichat.Home.Activity.IChatImageShowActivity;
import com.example.ichat.Model.Chats;
import com.example.ichat.R;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Chats chats = list.get(position);
        int[] reactions = new int[] {
                R.drawable.reaction1,
                R.drawable.reaction2,
                R.drawable.reaction3,
                R.drawable.reaction4,
                R.drawable.reaction5
            };
        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Chats");
            Map<String, Object> update = new HashMap<>();
            update.put("feeling", pos);
            reference.child(chats.getChatId()).updateChildren(update);
            return true; // true is closing popup, false is requesting a new selection
        });
        holder.showTime.setText(chats.getTime());
        if(chats.getMessageType().equals("text")) {
            holder.showMessage.setText(chats.getMessage());
            if(chats.getFeeling() < 0) {
                holder.feeling.setVisibility(View.GONE);
            } else {
                holder.feeling.setVisibility(View.VISIBLE);
                holder.feeling.setBackgroundResource(reactions[chats.getFeeling()]);
            }

            //  use for long touch listener...

            holder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    holder.linearLayout.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            popup.onTouch(v, event);
                            holder.linearLayout.setOnTouchListener(null);
                            return false;
                        }
                    });
                    return true;
                }
            });
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
        ImageButton doubleTick, feeling;
        ImageView showImage;
        LinearLayout linearLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            showMessage = itemView.findViewById(R.id.show_message);
            showTime = itemView.findViewById(R.id.show_time);
            doubleTick = itemView.findViewById(R.id.double_tick);
            showImage = itemView.findViewById(R.id.show_image);
            linearLayout = itemView.findViewById(R.id.linearLayout);
            feeling = itemView.findViewById(R.id.feeling);
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

