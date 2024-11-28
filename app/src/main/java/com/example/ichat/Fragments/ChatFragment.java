package com.example.ichat.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ichat.Adapter.UserAdapter;
import com.example.ichat.Model.Chats;
import com.example.ichat.Model.Users;
import com.example.ichat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;


public class ChatFragment extends Fragment {

    public ChatFragment() {
        // Required empty public constructor
    }

    private RecyclerView recyclerView;
    private Intent intent;
    private FirebaseUser fuser;
    private DatabaseReference reference;
    private ArrayList<Users> userInfoList;
    private ArrayList<String> userList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        // here we initilize the recycler view and others
        init(view);
        // here we set the property of recycler view
        setRecyclerView(getContext());
        // here we get the data from the firebase using the firebaseAuth
        getData(getContext());
        return view;
    }
    private void getData(Context context) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        userList = new ArrayList<>();
        reference.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
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
        reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userInfoList.clear(); // Clear the list before populating

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    if (users != null && userList.contains(users.getId())) {
                        // Check if the user is already in the list before adding
                        boolean userExists = false;
                        for (Users users1 : userInfoList) {
                            if (users1.getId().equals(users.getId())) {
                                userExists = true;
                                break;
                            }
                        }

                        if (!userExists) {
                            userInfoList.add(users);
                        }
                    }
                }
                // Set the adapter with the updated list
                UserAdapter userAdapter = new UserAdapter(context, userInfoList, true);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error if needed
            }
        });
    }
    private void init(View view) {
        recyclerView = view.findViewById(R.id.chat_recycler_view);
    }
    private void setRecyclerView(Context context) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }
}