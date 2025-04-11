package com.example.ichat.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.devlomi.circularstatusview.CircularStatusView;
import com.example.ichat.Model.User;
import com.example.ichat.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserStatusAdapter extends RecyclerView.Adapter<UserStatusAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<User> userInfoList;

    public UserStatusAdapter(Context context, ArrayList<User> userInfoList) {
        this.context = context;
        this.userInfoList = userInfoList;
    }

    @NonNull
    @Override
    public UserStatusAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.ichat_status_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull UserStatusAdapter.ViewHolder holder, int position) {
        Glide.with(context).load(userInfoList.get(position).getImageUrl()).into(holder.profileImage);
    }

    @Override
    public int getItemCount() {
        return userInfoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView profileImage;
        private final CircularStatusView circularStatusView;
        private final ImageView statusLastImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            circularStatusView = itemView.findViewById(R.id.circular_status_view);
            statusLastImage = itemView.findViewById(R.id.status_last_image);

        }
    }
}
