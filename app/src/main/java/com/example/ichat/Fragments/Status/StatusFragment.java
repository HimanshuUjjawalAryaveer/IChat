package com.example.ichat.Fragments.Status;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ichat.Adapter.UserStatusAdapter;
import com.example.ichat.Model.Chats;
import com.example.ichat.Model.User;
import com.example.ichat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class StatusFragment extends Fragment {

    private RecyclerView recyclerView;
    private CardView cardView;
    private Uri imageUri;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ///   Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_status, container, false);
        ///   this is use to initialize the views and viewGroup...
        init(view);
        ///   this is use to set the recycler view...
        setRecyclerView();
        ///   this is use to get the data from the database for that user who can set the status...
        getData(requireContext());
        ///   this is use for the calling add status button
        cardView.setOnClickListener(v -> openGallery());
        return view;
    }

    ///   this is use to initialize the view...
    private void init(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        cardView = view.findViewById(R.id.add_status);

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                imageUri = result.getData().getData();
                Intent intent = new Intent(getActivity(), IChatStatusImageActivity.class);
                intent.putExtra("imageUri", imageUri.toString());
                startActivity(intent);
            }
        });
    }

    ///   this is use to open the gallery and pick the image...
    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(gallery);
    }

    ///   this is use to get the data from the database for that user who can set the status...
    ///   this is use to find how many user are involved in the chat to the current user...
    private void getData(Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(context.getString(R.string.Chats));
        final ArrayList<String> userList = new ArrayList<>();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chats chats = dataSnapshot.getValue(Chats.class);
                    if(chats != null) {
                        assert user != null;
                        if(chats.getSender().equals(user.getUid())) {
                            userList.add(chats.getReceiver());
                        }
                        if(chats.getReceiver().equals(user.getUid())) {
                            userList.add(chats.getSender());
                        }
                    }
                }
                readChats(context, userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    ///   this is use to find that user id who involved in the chat to the current user...
    private void readChats(Context context, ArrayList<String> userList) {
        ArrayList<User> userInfoList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(context.getString(R.string.User));

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userInfoList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && userList.contains(user.getUserID())) {
                        ///   Check if the user is already in the list before adding
                        boolean userExists = false;
                        for (User users1 : userInfoList) {
                            if (users1.getUserID().equals(user.getUserID())) {
                                userExists = true;
                                break;
                            }
                        }
                        ///   if the user is exist in the userInfoList then do not add it again...
                        if (!userExists) {
                            userInfoList.add(user);
                        }
                    }
                }
                getStatusData(context, userInfoList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    ///   this is use to find the status data...
    private void getStatusData(Context context, ArrayList<User> userInfoList) {
        final ArrayList<User> listInfo = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(context.getString(R.string.User));
        ///    this is use to add the current user at the first position...
        databaseReference.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                listInfo.add(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        for(User user : userInfoList) {
            databaseReference.child(user.getUserID()).child("statusData").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        listInfo.add(user);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        new Handler().postDelayed(() -> {
            UserStatusAdapter userStatusAdapter = new UserStatusAdapter(context, listInfo);
            recyclerView.setAdapter(userStatusAdapter);
        }, 500);
    }

    ///   this is use to set the recycler view...
    private void setRecyclerView() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
    }
}