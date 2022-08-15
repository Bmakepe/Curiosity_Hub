package com.makepe.curiosityhubls;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.Query;
import com.makepe.curiosityhubls.Adapters.GroupAdapter;
import com.makepe.curiosityhubls.Adapters.UserAdapter;
import com.makepe.curiosityhubls.CustomClasses.Permissions;
import com.makepe.curiosityhubls.Fragments.DiscussionsFragment;
import com.makepe.curiosityhubls.Fragments.NotificationFragment;
import com.makepe.curiosityhubls.Fragments.HomeFragment;
import com.makepe.curiosityhubls.Fragments.MaterialFragment;
import com.makepe.curiosityhubls.Models.Groups;
import com.makepe.curiosityhubls.Models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.curiosityhubls.Notifications.Token;
import com.makepe.curiosityhubls.Privacy.BlockedActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    CircleImageView imgProfile;
    DatabaseReference reference, caution;
    FirebaseUser firebaseUser;

    ImageView searchBTN;
    Dialog searchDialog;
    ProgressBar progressBar;

    Permissions permissions;

    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;

    GroupAdapter groupAdapter;
    UserAdapter userAdapter;
    EditText queryArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        imgProfile = findViewById(R.id.user_profile);
        searchBTN = findViewById(R.id.searchBTN);
        progressBar = findViewById(R.id.homePicLoader);

        iniSearchDialog();
        permissions = new Permissions(this);
        permissions.verifyPermissions();

        reference = FirebaseDatabase.getInstance().getReference("Students").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                try {
                    if(user.getProfileImg().equals("default")) {
                        imgProfile.setImageResource(R.drawable.person_img);
                    } else {
                        Picasso.get().load(user.getProfileImg()).into(imgProfile);
                    }
                    progressBar.setVisibility(View.GONE);
                }catch (NullPointerException ignored){}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        imgProfile.setOnClickListener(view -> {

            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        searchBTN.setOnClickListener(view -> searchDialog.show());

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(itemSelectedListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();

        //updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    public void updateToken(String token){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(firebaseUser.getUid()).setValue(mToken);
    }

    @SuppressLint("NonConstantResourceId")
    private final BottomNavigationView.OnNavigationItemSelectedListener itemSelectedListener =
            item -> {

                switch (item.getItemId()){
                    case R.id.nav_home:
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.nav_material:
                        selectedFragment = new MaterialFragment();
                        break;
                    case R.id.nav_new_post:
                        startActivity(new Intent(MainActivity.this, PostActivity.class));
                        break;
                    case R.id.nav_notification:
                        selectedFragment = new NotificationFragment();
                        break;
                    case R.id.nav_messages:
                        selectedFragment = new DiscussionsFragment();
                        break;
                }
                if (selectedFragment != null){
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                }

                return true;
            };

    private void iniSearchDialog() {
        searchDialog = new Dialog(this);
        searchDialog.setContentView(R.layout.search_pop_up);
        searchDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        searchDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        searchDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        queryArea =  searchDialog.findViewById(R.id.searchEd);
        queryArea.setHint("Search Curiosity Hub");
        TabLayout searchTabs = searchDialog.findViewById(R.id.searchTabs);
        RelativeLayout resultsArea = searchDialog.findViewById(R.id.resultsArea);
        ImageView cancelSearch = searchDialog.findViewById(R.id.cancelSearch);

        ArrayList<User> users = new ArrayList<>();
        ArrayList<User> tutors = new ArrayList<>();
        ArrayList<Groups> groups = new ArrayList<>();

        RecyclerView usersRecycler = searchDialog.findViewById(R.id.usersRecycler);
        RecyclerView tutorsRecycler = searchDialog.findViewById(R.id.tutorsRecycler);
        RecyclerView groupsRecycler = searchDialog.findViewById(R.id.groupsRecycler);

        searchTabs.addTab(searchTabs.newTab().setText("All"));
        searchTabs.addTab(searchTabs.newTab().setText("People"));
        searchTabs.addTab(searchTabs.newTab().setText("Tutors"));
        searchTabs.addTab(searchTabs.newTab().setText("Posts"));
        searchTabs.addTab(searchTabs.newTab().setText("Groups"));
        searchTabs.addTab(searchTabs.newTab().setText("Material"));

        searchTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //searchPager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()){
                    case 0:
                        Toast.makeText(MainActivity.this, "Search EVERYTHING", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(MainActivity.this, "Search PEOPLE", Toast.LENGTH_SHORT).show();
                        usersRecycler.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        tutorsRecycler.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, "Search TUTORS", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(MainActivity.this, "Search POSTS", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        groupsRecycler.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, "Search GROUPS", Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        Toast.makeText(MainActivity.this, "Search MATERIAL", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch(tab.getPosition()){
                    case 0:

                    case 1:
                        usersRecycler.setVisibility(View.GONE);
                        break;
                    case 2:
                        tutorsRecycler.setVisibility(View.GONE);
                        break;

                    case 3:

                    case 4:
                        groupsRecycler.setVisibility(View.GONE);
                        break;

                    case 5:

                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                switch(tab.getPosition()){
                    case 0:

                    case 1:
                        usersRecycler.setVisibility(View.GONE);
                        break;
                    case 2:
                        tutorsRecycler.setVisibility(View.GONE);
                        break;

                    case 3:

                    case 4:
                        groupsRecycler.setVisibility(View.GONE);
                        break;

                    case 5:

                }
            }
        });

        queryArea.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                if(count == 0){
                    usersRecycler.setAdapter(null);
                }
                if(charSequence.length() >= 0){
                    resultsArea.setVisibility(View.VISIBLE);
                    cancelSearch.setVisibility(View.VISIBLE);

                    searchUsers(charSequence.toString(), users, usersRecycler);
                    searchTutors(charSequence.toString(), tutors, tutorsRecycler);
                    searchGroups(charSequence.toString(), groups, groupsRecycler);

                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        searchDialog.findViewById(R.id.searchBackBTN).setOnClickListener(view -> searchDialog.dismiss());

        cancelSearch.setOnClickListener(view -> {
            queryArea.setText(null);
            cancelSearch.setVisibility(View.GONE);
            users.clear();
        });
    }

    private void searchGroups(String queryText, ArrayList<Groups> groups, RecyclerView groupsRecycler) {
        groupsRecycler.setHasFixedSize(true);
        groupsRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        groupsRecycler.setLayoutManager(layoutManager);

        groupAdapter = new GroupAdapter(MainActivity.this, groups);
        groupsRecycler.setAdapter(userAdapter);

        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.keepSynced(true);

        Query query = reference.orderByChild("groupName").startAt(queryText).endAt(queryText+"\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groups.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    if(ds.child("Participants").child(fUser.getUid()).exists()) {

                        Groups group = ds.getValue(Groups.class);
                        if(ds.child(group.getGroupName()).toString().toLowerCase().contains(queryText.toLowerCase()))
                        {
                            try{
                                if(group.getGroupPrivacy().equals("Public")) {
                                    groups.add(group);
                                }
                            }catch (NullPointerException ignored){}
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchUsers(String queryString, ArrayList<User> users, RecyclerView usersRecycler) {

        usersRecycler.setHasFixedSize(true);
        usersRecycler.hasFixedSize();
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
        usersRecycler.setLayoutManager(layoutManager1);

        userAdapter = new UserAdapter(MainActivity.this, users);
        usersRecycler.setAdapter(userAdapter);

        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Students");
        reference.keepSynced(true);

        Query query = reference.orderByChild("name").startAt(queryArea.getText().toString()).endAt(queryArea.getText().toString() + "\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);

                    assert user != null;
                    assert fUser != null;
                    if (!user.getUserId().equals(fUser.getUid())) {

                        if(user.getName().toLowerCase().contains(queryString.toLowerCase())) {
                            try{
                                if(user.getRole().equals("Student"))
                                    users.add(user);
                            }catch (NullPointerException ignored){}
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchTutors(String queryString, ArrayList<User> tutors, RecyclerView tutorsRecycler) {

        tutorsRecycler.setHasFixedSize(true);
        tutorsRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        tutorsRecycler.setLayoutManager(layoutManager);

        userAdapter = new UserAdapter(MainActivity.this, tutors);
        tutorsRecycler.setAdapter(userAdapter);

        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Students");
        reference.keepSynced(true);

        Query query = reference.orderByChild("name").startAt(queryString).endAt(queryString+"\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tutors.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);

                    assert user != null;
                    assert fUser != null;
                    if (!user.getUserId().equals(fUser.getUid())) {

                        if(user.getName().toLowerCase().contains(queryString.toLowerCase())) {
                            try{
                                if(user.getRole().equals("PSLE Tutor") |user.getRole().equals("JC Tutor") | user.getRole().equals("LGCSE Tutor"))
                                    tutors.add(user);
                            }catch (NullPointerException ignored){}
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", user.getUid());
            editor.apply();

        }else{
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

        caution = FirebaseDatabase.getInstance().getReference("Announcements").child("Cautions");
        caution.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(firebaseUser != null) {
                        String message = "" + snapshot.child("cautionStatus").getValue();

                        if (!message.equals("Online")) {
                            startActivity(new Intent(MainActivity.this, BlockedActivity.class));
                        }
                    }
                }else{
                    startActivity(new Intent(MainActivity.this, BlockedActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}