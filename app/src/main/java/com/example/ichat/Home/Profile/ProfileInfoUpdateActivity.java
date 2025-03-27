package com.example.ichat.Home.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ichat.CustomDialog.CustomProgressDialog;
import com.example.ichat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileInfoUpdateActivity extends AppCompatActivity {

    private Intent intent;
    private DatabaseReference reference;
    private AppCompatButton cancel, save;
    private EditText editText;
    private CustomProgressDialog dialog;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_info_update);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ///   use to set the status bar color...
        setStatusBarColor();
        ///   use initialize the view...
        init();
        ///   use to set the toolbar/action bar...
        setToolbar();
        ///   use to get the already set data from the intent...
        editText.setText(getIntent().getStringExtra("info"));

        ///   use to cancel the update and go back to the profile activity...
        cancel.setOnClickListener(view -> {
            String path = getIntent().getStringExtra("userId");
            intent = new Intent(ProfileInfoUpdateActivity.this, ProfileActivity.class);
            intent.putExtra("userID", path);
            startActivity(intent);
            finish();
        });

        ///   use to save the updates to the database and go to the Profile activity...
        save.setOnClickListener(view -> {
            dialog = new CustomProgressDialog(this);
            dialog.setTitle("Uploading info...");
            dialog.setMessage("please wait...");
            dialog.setCancelable(false);
            dialog.show();
            path = getIntent().getStringExtra("userId");
            if(path != null) {
                reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(path);
                Map<String, Object> updateMap = new HashMap<>();
                updateMap.put(intent.getStringExtra("key"), editText.getText().toString().trim());
                reference.updateChildren(updateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "successfully updated", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        intent = new Intent(ProfileInfoUpdateActivity.this, ProfileActivity.class);
                        intent.putExtra("userID", path);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                });
            }
        });

    }

    ///   use to initialize the view...
    private void init() {
        editText = findViewById(R.id.edit);
        cancel = findViewById(R.id.cancel);
        save = findViewById(R.id.save);

        intent = getIntent();
    }

    ///   use to set the toolbar/action bar...
    private void setToolbar() {
        Objects.requireNonNull(getSupportActionBar()).setTitle(intent.getStringExtra("name"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    ///   use to set the status bar color...
    private void setStatusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.light_blue));
    }

    ///   use to set the functionality of the action bar back button...
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
};