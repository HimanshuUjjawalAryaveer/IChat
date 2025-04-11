package com.example.ichat.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ichat.Adapter.UserAdapter;
import com.example.ichat.CustomDialog.CustomProgressDialog;
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


public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseReference reference;
    private CustomProgressDialog dialog;
    private boolean isFlag = true;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ///   get the layout file through the view...
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        ///   initialize the view...
        init(view);
        ///   check the data is load or not...
        if(isFlag) {
            progressDialog();
            isFlag = false;
        }
        ///   set the recycler view...
        setRecyclerView(getContext());
        ///   get the data from the database...
        getData(requireContext());
        return view;
    }

    ///   use to initialize the view...
    private void init(View view) {
        recyclerView = view.findViewById(R.id.chat_recycler_view);
    }

    ///   this section is used to get that user by which the current user will send or receive the message
    private void getData(Context context) {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference(context.getString(R.string.Chats));
        ///   this list contain that data of the user in which the current user is involved...
        ArrayList<String> userList = new ArrayList<>();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chats chats = dataSnapshot.getValue(Chats.class);
                    if(chats != null) {
                        assert fUser != null;
                        if(chats.getSender().equals(fUser.getUid())) {
                            userList.add(chats.getReceiver());
                        }
                        if(chats.getReceiver().equals(fUser.getUid())) {
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

    ///   this section fetch the data only for that user which is involved in the chat...
    private void readChats(final Context context, final ArrayList<String> userList) {
        ArrayList<User> userInfoList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user));
        reference.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ///   Clear the list before populating...
                userInfoList.clear();

                ///   this section is used to fetch only that data which can match the usesList data and then add it in the userInfoList...
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

                        if (!userExists) {
                            userInfoList.add(user);
                        }
                    }
                }

                ///   use to reverse the userInfoList...
                Collections.reverse(userInfoList);

                ///   Set the adapter with the updated list
                UserAdapter userAdapter = new UserAdapter(context, userInfoList, true);
                recyclerView.setAdapter(userAdapter);
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error if needed
                dialog.dismiss();
            }
        });
    }

    ///   use to set the recycler view...
    private void setRecyclerView(Context context) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    ///   use to show the progress dialog...
    private void progressDialog() {
        dialog = new CustomProgressDialog(getContext());
        dialog.setTitle("Loading data...");
        dialog.setMessage("please wait");
        dialog.setCancelable(false);
        dialog.show();
    }
}