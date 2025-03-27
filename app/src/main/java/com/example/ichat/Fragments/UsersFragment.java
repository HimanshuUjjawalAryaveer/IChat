package com.example.ichat.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.ichat.Adapter.UserAdapter;
import com.example.ichat.CustomDialog.CustomProgressDialog;
import com.example.ichat.Model.User;
import com.example.ichat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText editText;
    private UserAdapter userAdapter;
    private ArrayList<User> list;
    private CustomProgressDialog dialog;
    private boolean flag = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ///   get the layout file through the view...
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        ///   initialize the view...
        init(view);
        ///   set the recycler view...
        setRecyclerView();
        ///   check the data is load or not...
        if(flag) {
            showProgressDialog();
            flag = false;
        }
        ///   read the user data from the database...
        readUsers();
        ///   use to search the user from the database through the search dialog...
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    ///   use to initialize the view...
    private void init(View view) {
        recyclerView = view.findViewById(R.id.user_recycler_view);
        editText = view.findViewById(R.id.search_user);


        list = new ArrayList<>();
        dialog = new CustomProgressDialog(getContext());
    }

    ///   use to search the user from the database...
    private void searchUser(String s) {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        ///   this query is used to search the user from the database...
        Query query = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).orderByChild("username").startAt(s).endAt(s+"\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    assert fUser != null;
                    ///   this condition uses to check that the current user is not added in the list...
                    if(!user.getUserID().equals(fUser.getUid())) {
                        list.add(user);
                    }
                }
                userAdapter = new UserAdapter(getContext(), list, false);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    ///   read the user data from the database...
    private void readUsers() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(getString(R.string.user));

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (editText.getText().toString().isEmpty()) {
                    list.clear();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        User user = snapshot1.getValue(User.class);
                        assert firebaseUser != null;
                        assert user != null;
                        ///   this condition uses to check that the current user is not added in the list...
                        if (!firebaseUser.getUid().equals(user.getUserID())) {
                            list.add(user);
                        }
                    }
                    dialog.dismiss();
                    userAdapter = new UserAdapter(getContext(), list, false);
                    recyclerView.setAdapter(userAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
            }
        });
    }

    ///   use to set the recycler view...
    private void setRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    ///   use to show the progress dialog...
    private void showProgressDialog() {
        dialog.setTitle("Loading Data");
        dialog.setMessage("please wait...");
        dialog.setCancelable(false);
        dialog.show();
    }
}