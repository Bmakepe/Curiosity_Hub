package com.makepe.curiosityhubls.CustomClasses;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SearchTabPagerAdapter extends FragmentPagerAdapter {

    int tabCount;
    Context context;

    public SearchTabPagerAdapter(@NonNull FragmentManager fm, int tabCount, Context context) {
        super(fm);
        this.tabCount = tabCount;
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        if(position == 0){
            Toast.makeText(context, "you have selected posts", Toast.LENGTH_SHORT).show();
        }else if(position == 1){
            Toast.makeText(context, "you have selected people", Toast.LENGTH_SHORT).show();
        }else if(position == 2){
            Toast.makeText(context, "you have selected tutors", Toast.LENGTH_SHORT).show();
        }else if(position == 3){
            Toast.makeText(context, "you have selected groups", Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }
}
