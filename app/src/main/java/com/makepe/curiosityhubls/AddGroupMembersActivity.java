package com.makepe.curiosityhubls;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.makepe.curiosityhubls.Adapters.GroupContactsAdapter;
import com.makepe.curiosityhubls.Models.ContactsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.curiosityhubls.Models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AddGroupMembersActivity extends AppCompatActivity {

    RecyclerView groupRecycler;

    GroupContactsAdapter userAdapter;
    List<String> followingList;
    List<String> tutorsList;
    List<ContactsModel> userList;

    String groupID, groupRole, gradeLevel;
    FirebaseUser firebaseUser;

    ProgressBar pd;
    DatabaseReference reference;

    //for search
    ImageView searchBTN, searchBackBTN;
    EditText searchET;

    RelativeLayout searchToolBar, dialogHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview_layout);
        //setContentView(R.layout.group_members_layout);

        TextView header = findViewById(R.id.dialogHeader);
        groupRecycler = findViewById(R.id.savedRecycler);
        pd = findViewById(R.id.recyclerLoader);
        searchToolBar = findViewById(R.id.dialogSearchHeader);
        dialogHeader = findViewById(R.id.savedToolbar);
        searchBTN = findViewById(R.id.savedSearchBTN);
        searchET = findViewById(R.id.dialogSearchEd);
        searchBackBTN = findViewById(R.id.dialogSearchBackBTN);

        header.setText("Manage Group Members");
        pd.setVisibility(View.VISIBLE);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Students");
        groupID = getIntent().getStringExtra("groupID");
        groupRole = getIntent().getStringExtra("groupRole");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    if(user.getUserId().equals(firebaseUser.getUid())){
                        gradeLevel = user.getGrade();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        checkFollowing(groupRecycler);

        findViewById(R.id.savedBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        searchBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchToolBar.setVisibility(View.VISIBLE);
                dialogHeader.setVisibility(View.GONE);
                searchET.setHint("Search Contacts");
            }
        });

        searchBackBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchToolBar.setVisibility(View.GONE);
                dialogHeader.setVisibility(View.VISIBLE);

            }
        });

    }

    private void checkFollowing(RecyclerView groupRecycler) {
        followingList = new ArrayList<>();
        groupRecycler.setHasFixedSize(true);
        groupRecycler.setNestedScrollingEnabled(false);
        groupRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        groupRecycler.setLayoutManager(layoutManager);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseUser.getUid())
                .child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    followingList.add(snapshot.getKey());
                }

                loadTutors();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddGroupMembersActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTutors() {
        tutorsList = new ArrayList<>();

        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("Students");
        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tutorsList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    assert user != null;

                    try{
                        if(gradeLevel.equals("Class 1") | gradeLevel.equals("Class 2") | gradeLevel.equals("Class 3")
                                | gradeLevel.equals("Class 4") | gradeLevel.equals("Class 5") | gradeLevel.equals("Class 6") | gradeLevel.equals("Class 7")){
                            if (user.getRole().equals("PSLE Tutor")) {
                                tutorsList.add(user.getUserId());
                            }
                        }else if(gradeLevel.equals("Form A") | gradeLevel.equals("Form B") | gradeLevel.equals("Form C")){
                            if (user.getRole().equals("JC Tutor")) {
                                tutorsList.add(user.getUserId());
                            }
                        }else if(gradeLevel.equals("Form D") | gradeLevel.equals("Form E")){
                            if (user.getRole().equals("COSC Tutor") | user.getRole().equals("LGCSE Tutor")) {
                                tutorsList.add(user.getUserId());
                            }
                        }
                    }catch (NullPointerException ignored){}

                }

                loadContacts(groupRecycler);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddGroupMembersActivity.this, "There was an error loading tutors", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadContacts(RecyclerView groupRecycler) {
        userList = new ArrayList<>();
        userList.clear();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ContactsModel contacts = ds.getValue(ContactsModel.class);

                    for(String id : followingList){
                        assert contacts != null;
                        if(contacts.getUserId().equals(id)){
                            userList.add(contacts);
                        }
                    }

                    for(String id : tutorsList){
                        if(contacts.getUserId().equals(id)){
                            userList.add(contacts);
                        }
                    }

                    userAdapter = new GroupContactsAdapter(groupID, groupRole,AddGroupMembersActivity.this, userList);
                    groupRecycler.setAdapter(userAdapter);

                    pd.setVisibility(View.GONE);

                    Collections.sort(userList, new Comparator<ContactsModel>() {
                        @Override
                        public int compare(ContactsModel contactsModel, ContactsModel t1) {
                            return contactsModel.getName().compareTo(t1.getName());
                        }
                    });

                }
//                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}