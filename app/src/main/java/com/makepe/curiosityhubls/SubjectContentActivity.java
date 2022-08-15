package com.makepe.curiosityhubls;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.makepe.curiosityhubls.CustomClasses.TabPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class SubjectContentActivity extends AppCompatActivity {

    ImageView contentBackBTN;
    TabLayout tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_content);

        contentBackBTN = findViewById(R.id.subjectsBackBTN);
        tabs = findViewById(R.id.materialTabs);

        tabs.addTab(tabs.newTab().setText("PSLE"));
        tabs.addTab(tabs.newTab().setText("JC"));
        tabs.addTab(tabs.newTab().setText("COSC"));
        tabs.addTab(tabs.newTab().setText("LGCSE"));

        tabs.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);

        Intent intent1 = getIntent();
        String subjectName = intent1.getStringExtra("subjectName");

        final ViewPager viewPager = findViewById(R.id.materialPager);
        final PagerAdapter adapter = new TabPagerAdapter(getSupportFragmentManager(), tabs.getTabCount(), subjectName);

        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));

        tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
               @Override
               public void onTabSelected(TabLayout.Tab tab) {
                   viewPager.setCurrentItem(tab.getPosition());
               }

               @Override
               public void onTabUnselected(TabLayout.Tab tab) {

               }

               @Override
               public void onTabReselected(TabLayout.Tab tab) {

               }

           });

        contentBackBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}