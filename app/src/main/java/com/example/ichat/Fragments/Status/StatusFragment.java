package com.example.ichat.Fragments.Status;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.ichat.Adapter.UserAdapter;
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
import java.util.Collections;

public class StatusFragment extends Fragment {

    private static final int RC_CODE = 3;
    private RecyclerView recyclerView;
    private CardView cardView;
    private Uri imageUri;
    private ArrayList<String> userList;
    private ArrayList<User> userInfoList;


    private FirebaseUser fuser;
    private DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_status, container, false);
        init(view);
        setRecyclerView();
        getData(getContext());

        //  use for the calling add status button
        cardView.setOnClickListener(v -> openGallery());
        return view;
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery, RC_CODE);
    }


    private void init(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        cardView = view.findViewById(R.id.add_status);
    }

    private void setRecyclerView() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            Intent intent = new Intent(getActivity(), IChatStatusImageActivity.class);
            intent.putExtra("imageUri", imageUri.toString());
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Something went wrong...", Toast.LENGTH_LONG).show();
        }
    }

    private void getData(Context context) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        userList = new ArrayList<>();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chats chats = dataSnapshot.getValue(Chats.class);
                    if(chats != null) {
                        if(chats.getSender().equals(fuser.getUid())) {
                            userList.add(chats.getReceiver());
                        }
                        if(chats.getReceiver().equals(fuser.getUid())) {
                            userList.add(chats.getSender());
                        }
                    }
                }
                readChats(context);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readChats(Context context) {
        userInfoList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user));

        reference.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userInfoList.clear(); // Clear the list before populating

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && userList.contains(user.getUserID())) {
                        // Check if the user is already in the list before adding
                        boolean userExists = false;
                        for (User users1 : userInfoList) {
                            if (users1.getUserID().equals(user.getUserID())) {
                                userExists = true;
                                break;
                            }
                        }

                        if (!userExists && user.getStatusData() != null) {
                            userInfoList.add(user);
                        }
                    }
                }

                Collections.reverse(userInfoList);
                // Set the adapter with the updated list
//                UserAdapter userAdapter = new UserAdapter(context, userInfoList, true);
//                User user = reference.addValueEventListener(new ValueEventListener() {

                //  add the current user to the first position...
                reference.child(fuser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        userInfoList.add(0,user);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                UserStatusAdapter adapter = new UserStatusAdapter(getContext(), userInfoList);
                recyclerView.setAdapter(adapter);
//                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error if needed
//                pd.dismiss();
            }
        });
    }
}