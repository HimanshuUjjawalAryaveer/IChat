package com.example.ichat.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.ichat.Fragments.UsersFragment;
import com.example.ichat.Fragments.ChatFragment;
import com.example.ichat.Fragments.Status.StatusFragment;

import java.util.Objects;

public class HomePageAdapter extends FragmentStateAdapter {
    public HomePageAdapter(@NonNull FragmentActivity fm) {
        super(Objects.requireNonNull(fm));
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 0) {
            return new ChatFragment();
        } else if (position == 1) {
            return new StatusFragment();
        } else {
            return new UsersFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
