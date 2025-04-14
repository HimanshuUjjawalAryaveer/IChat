package com.example.ichat.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.devlomi.circularstatusview.CircularStatusView;
import com.example.ichat.Home.Home.HomeActivity;
import com.example.ichat.Model.StatusData;
import com.example.ichat.Model.User;
import com.example.ichat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class UserStatusAdapter extends RecyclerView.Adapter<UserStatusAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<User> userInfoList;

    ///   this is use to initialize the class variable by constructor...
    public UserStatusAdapter(Context context, ArrayList<User> userInfoList) {
        this.context = context;
        this.userInfoList = userInfoList;
    }

    ///   this is use to override the default view by the customize view...
    @NonNull
    @Override
    public UserStatusAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.ichat_status_layout, parent, false);
        return new ViewHolder(view);
    }

    ///   this is use to set the data to the view at the run time...
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull UserStatusAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Glide.with(context).load(userInfoList.get(position).getImageUrl()).into(holder.profileImage);
        getStatusData(context, userInfoList.get(position).getUserID(), new OnStatusDataLoaded() {

            ///   this is use to get the data only when the data is loaded successfully...
            @Override
            public void onDataLoaded(ArrayList<StatusData> statusDataList) {
               if(!statusDataList.isEmpty()) {
                   holder.circularStatusView.setPortionsCount(statusDataList.size());
                   Glide.with(context)
                           .load(statusDataList.get(statusDataList.size() - 1).getStatusImageUrl())
                           .into(holder.statusLastImage);

                   ///   this is use to set the story view to the status image...
                   holder.mainStatusBox.setOnClickListener(v -> {

                       ArrayList<MyStory> myStories = new ArrayList<>();
                       for (StatusData statusData : statusDataList) {
                           myStories.add(new MyStory(statusData.getStatusImageUrl(), null, statusData.getCaption()));
                       }

                       new StoryView.Builder(((HomeActivity) context).getSupportFragmentManager())
                               .setStoriesList(myStories)
                               .setStoryDuration(5000)
                               .setTitleText(getCapitalText(userInfoList.get(position).getUsername()))
                               .setSubtitleText("")
                               .setTitleLogoUrl(userInfoList.get(position).getImageUrl())
                               .setStoryClickListeners(new StoryClickListeners() {
                                   @Override
                                   public void onDescriptionClickListener(int pos) {
                                       // Your work
                                   }

                                   @Override
                                   public void onTitleIconClickListener(int position) {
                                       // Your work
                                   }
                               })
                               .build()  // This line is important
                               .show();  // This actually shows the story view
                   });
               }
            }
        });
    }

    ///   this is use to count the number of items in the list...
    @Override
    public int getItemCount() {
        return userInfoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView profileImage;
        private final CircularStatusView circularStatusView;
        private final ImageView statusLastImage;
        private final ConstraintLayout mainStatusBox;

        ///   this is the initialize the view by the constructor of the class...
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            circularStatusView = itemView.findViewById(R.id.circular_status_view);
            statusLastImage = itemView.findViewById(R.id.status_last_image);
            mainStatusBox = itemView.findViewById(R.id.main_status_box);
        }
    }

    ///   this is use to get the status data from the database...
    private void getStatusData(Context context, final String userId, final OnStatusDataLoaded callback) {
        final ArrayList<StatusData> statusDataList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(context.getString(R.string.User));
        databaseReference.child(userId).child("statusData").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    statusDataList.clear();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        StatusData statusData = dataSnapshot.getValue(StatusData.class);
                        statusDataList.add(statusData);
                    }
                }
                callback.onDataLoaded(statusDataList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    ///   this is use to fetch the data from the database and the pass to the adapter...
    public interface OnStatusDataLoaded {
        void onDataLoaded(ArrayList<StatusData> statusDataList);
    }

    ///   this is use to set the first letter of each word capital...
    private static String getCapitalText(String string) {
        if (!Objects.equals(string, "") && string != null) {
            String[] str =string.split(" ");
            for(int i=0; i<str.length; i++) {
                str[i] = str[i].substring(0,1).toUpperCase() + str[i].substring(1).toLowerCase();
            }
            return String.join(" ", str);
        }
        return "";
    }
}
