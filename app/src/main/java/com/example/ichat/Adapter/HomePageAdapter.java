package com.example.ichat.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.ichat.Fragments.UsersFragment;
import com.example.ichat.Fragments.ChatFragment;
import com.example.ichat.Fragments.StatusFragment;

public class HomePageAdapter extends FragmentPagerAdapter {
    public HomePageAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            return new ChatFragment();
        } else if (position == 1) {
            return new StatusFragment();
        } else {
            return new UsersFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0) {
            return "Chats";
        } else if(position == 1) {
            return "Status";
        } else {
            return "Users";
        }
    }
}
