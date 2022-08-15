package com.makepe.curiosityhubls.CustomClasses;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.makepe.curiosityhubls.Fragments.MaterialFragments.COSC_Fragment;
import com.makepe.curiosityhubls.Fragments.MaterialFragments.JC_Fragment;
import com.makepe.curiosityhubls.Fragments.MaterialFragments.LGCSE_Fragment;
import com.makepe.curiosityhubls.Fragments.MaterialFragments.PSLE_Fragment;

public class TabPagerAdapter extends FragmentPagerAdapter {
    int tabCount;
    String SubjectName;

    public TabPagerAdapter(@NonNull FragmentManager fm, int numberOfTabs, String SubjectName) {
        super(fm);

        this.tabCount = numberOfTabs;
        this.SubjectName = SubjectName;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        if(position == 0){

            PSLE_Fragment psle_fragment = new PSLE_Fragment();
            Bundle bundle1 = new Bundle();
            bundle1.putString("subject_name", SubjectName);
            psle_fragment.setArguments(bundle1);
            return psle_fragment;

        }else if(position == 1){

            JC_Fragment jc_fragment = new JC_Fragment();
            Bundle bundle2 = new Bundle();
            bundle2.putString("subject_name", SubjectName);
            jc_fragment.setArguments(bundle2);
            return jc_fragment;

        }else if(position == 2){

            COSC_Fragment cosc_fragment = new COSC_Fragment();
            Bundle bundle3 = new Bundle();
            bundle3.putString("subject_name", SubjectName);
            cosc_fragment.setArguments(bundle3);
            return cosc_fragment;

        }else if(position == 3){

            LGCSE_Fragment lgcse_fragment = new LGCSE_Fragment();
            Bundle bundle4 = new Bundle();
            bundle4.putString("subject_name", SubjectName);
            lgcse_fragment.setArguments(bundle4);
            return lgcse_fragment;

        }else{
            return null;
        }

    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
