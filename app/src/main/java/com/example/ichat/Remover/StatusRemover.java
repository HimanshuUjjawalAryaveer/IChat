package com.example.ichat.Remover;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.ichat.Model.StatusData;
import com.example.ichat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class StatusRemover {
    private final Context context;
    public StatusRemover(Context context) {
        this.context = context;
    }

    ///   this is use to remove the status data automatically after 24 hours...
    public void cleanStatusData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(context.getString(R.string.User)).child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        long currentTime = System.currentTimeMillis();
        databaseReference.child("statusData").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    StatusData data = dataSnapshot.getValue(StatusData.class);
                    if(data != null && ((currentTime - data.getTimestamp()) >= (24 * 60 * 60 * 1000))) {
                        ///   if the time of 24 hours is completed then the task is deleted automatically...
                        dataSnapshot.getRef().setValue(null).addOnCompleteListener(task -> {
                            ///   done...
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
