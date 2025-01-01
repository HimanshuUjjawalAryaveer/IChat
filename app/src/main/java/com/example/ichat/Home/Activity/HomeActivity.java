package com.example.ichat.Home.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import com.example.ichat.Adapter.HomePageAdapter;
import com.example.ichat.Home.Profile.ProfileActivity;
import com.example.ichat.R;
import com.example.ichat.login.CreateAccountActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        Objects.requireNonNull(getSupportActionBar()).show();
        setStatusBarColor();
        setActionBar();
        init();
        setTabLayout();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();
        if(itemID == R.id.logout) {
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if(firebaseUser != null) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(firebaseUser.getUid());
                Map<String, Object> updateChild = new HashMap<>();
                updateChild.put("status", "offline");
                reference.updateChildren(updateChild)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    signOut();
                                }
                            }
                        });
            }
        } else if (itemID == R.id.profile) {
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            intent.putExtra("userID", firebaseUser.getUid());
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), CreateAccountActivity.class));
        finish();
    }

    private void setStatusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.light_blue));
    }
    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle("IChat");
            actionBar.setSubtitle("Personal");
        }
    }
    private void init() {
        tabLayout = findViewById(R.id.tab);
        viewPager = findViewById(R.id.viewPager);
    }
    private void setTabLayout() {
        HomePageAdapter adapter = new HomePageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }
    private void setStatus(String status) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(firebaseUser.getUid());
            Map<String, Object> updateChild = new HashMap<>();
            updateChild.put("status", status);
            reference.updateChildren(updateChild);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        setStatus("offline");
    }
}