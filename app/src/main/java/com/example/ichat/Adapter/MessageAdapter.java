package com.example.ichat.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ichat.Home.HomeActivity.IChatImageShowActivity;
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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<Chats> list;
    public static final int MSG_TYPE_TEXT_LEFT = 0;
    public static final int MSG_TYPE_TEXT_RIGHT = 1;
    public static final int MSG_TYPE_IMAGE_LEFT = 2;
    public static final int MSG_TYPE_IMAGE_RIGHT = 3;
    private final String userId;
    public MessageAdapter(final Context context, final ArrayList<Chats> list, final String userId) {
        this.context = context;
        this.list = list;
        this.userId = userId;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        ///   this is use to set right view, left view (with image and text)...
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


        ///   this is used to set the reactions on the message...
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
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(context.getString(R.string.Chats));
            Map<String, Object> updateFeelings = new HashMap<>();
            updateFeelings.put("feeling", pos);
            reference.child(chats.getChatId()).updateChildren(updateFeelings);
            return true; // true is closing popup, false is requesting a new selection
        });
        ///   this is use to set the time of the message...
        holder.showTime.setText(chats.getTime());

        ///   this is use to set the Feelings and text message  of the message...
        if(chats.getMessageType().equals("text")) {
            holder.showMessage.setText(chats.getMessage());
            if(chats.getFeeling() < 0) {
                holder.feeling.setVisibility(View.GONE);
            } else {
                holder.feeling.setVisibility(View.VISIBLE);
                holder.feeling.setBackgroundResource(reactions[chats.getFeeling()]);
            }

            ///  use for long touch listener...
            holder.linearLayout.setOnLongClickListener(v -> {
                holder.linearLayout.setOnTouchListener((v1, event) -> {
                    popup.onTouch(v1, event);
                    holder.linearLayout.setOnTouchListener(null);
                    return false;
                });
                return true;
            });

        } else {
            ///   use to load the image to the recycler view...
            Glide.with(context).load(chats.getMessage()).into(holder.showImage);
            ///   set the zoom functionality of the image...
            holder.showImage.setOnClickListener(v -> {
                Intent intent = new Intent(context, IChatImageShowActivity.class);
                intent.putExtra("imageUrl", chats.getMessage());
                intent.putExtra("userId", userId);
                context.startActivity(intent);
            });
        }

        ///   use to set the color of the tick...
        if(chats.getSeen()) {
            setColor(holder.doubleTick, ContextCompat.getColor(context, R.color.double_tick));
        } else {
            setColor(holder.doubleTick, ContextCompat.getColor(context, R.color.white));
        }
    }

    ///   return the size of the list...
    @Override
    public int getItemCount() {
        return list.size();
    }

    ///   use to initialize the view...
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView showMessage;
        private final TextView showTime;
        private final ImageButton doubleTick, feeling;
        private final ImageView showImage;
        private final LinearLayout linearLayout;
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

    ///   use to get the view in left or right...
    @Override
    public int getItemViewType(int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
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

    ///   use to set the color of the tick...
    private void setColor(ImageButton btn, int color) {
        btn.setBackgroundTintList(ColorStateList.valueOf(color));
    }
}

