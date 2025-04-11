package com.example.ichat.Home.Call;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ichat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class IChatCallReceiveActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ichat_call_recieve);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ///   this is use to hide the support action bar...
        Objects.requireNonNull(getSupportActionBar()).hide();
        ///   switch to the main call activity if the both user are available...
        setVideoCallStatusByReceiverSide(getIntent().getStringExtra("userID"));
    }

    ///   this is use to switch to the main call activity if the both user are available...
    private void setVideoCallStatusByReceiverSide(final String UID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("VideoCallStartStatus").child(UID);
        reference.child("isStart").setValue(true).addOnSuccessListener(unused -> {
            ///   activate if the both user are available...
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Intent intent = new Intent(IChatCallReceiveActivity.this, IChatVideoCallActivity.class);
                    intent.putExtra("currentUser", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                    intent.putExtra("sendTo", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                    intent.putExtra("sendBy", UID);
                    intent.putExtra("trackLastActivity", UID);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
    }
}