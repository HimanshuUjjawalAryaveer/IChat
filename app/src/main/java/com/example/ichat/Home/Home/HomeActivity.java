package com.example.ichat.Home.Home;

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
import androidx.viewpager2.widget.ViewPager2;

import com.example.ichat.Adapter.HomePageAdapter;
import com.example.ichat.Home.Profile.ProfileActivity;
import com.example.ichat.R;
import com.example.ichat.Remover.StatusRemover;
import com.example.ichat.login.CreateAccountActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ///   use to show the status bar...
        Objects.requireNonNull(getSupportActionBar()).show();
        ///   use to set the status bar color(where we can see the time)...
        setStatusBarColor();
        ///   use to set the action bar title and subtitle...
        setActionBar();
        ///   view and view group initialization...
        init();
        ///   set the tab layout to the home fragment...
        setTabLayout();
        ///   use to remove the status automatically after 24 hours...
        removeStatusAuto();
    }

    ///   use to initialize the view...
    private void init() {
        tabLayout = findViewById(R.id.tab);
        viewPager = findViewById(R.id.viewPager);
    }

    ///   this is use to remove the status automatically after 24 hours...
    private void removeStatusAuto() {
        StatusRemover st = new StatusRemover(this);
        st.cleanStatusData();
    }

    ///   use to set and show the menu bar...
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
                reference.updateChildren(updateChild).addOnCompleteListener(task -> {if(task.isSuccessful())  signOut();});

            }
        } else if (itemID == R.id.profile) {
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            intent.putExtra("userID", firebaseUser.getUid());
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    ///   use to sign out the user from the IChat application...
    private void
    signOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), CreateAccountActivity.class));
        finish();
    }

    ///   use to show the status bar and also set the status bar color...
    private void setStatusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.light_blue));
    }

    ///   use to set the action bar color...
    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle("IChat");
            actionBar.setSubtitle("Personal");
        }
    }

    ///   use to show and set the tab layout to the home fragment...
    private void setTabLayout() {
        HomePageAdapter adapter = new HomePageAdapter(this);
        viewPager.setAdapter(adapter);
        ///   use to set the name of the fragments...
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if(position == 0)
                tab.setText("Chats");
            else if(position == 1)
                tab.setText("Status");
            else
                tab.setText("Users");
        }).attach();
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

    ///   use to set the status online or offline...
    private void setStatus(String status) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(getString(R.string.user)).child(firebaseUser.getUid());
            Map<String, Object> updateChild = new HashMap<>();
            updateChild.put("status", status);
            reference.updateChildren(updateChild);
        }
    }
}