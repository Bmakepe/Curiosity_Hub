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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.curiosityhubls.Adapters.FirebaseContactsAdapter;
import com.makepe.curiosityhubls.Models.ContactsModel;
import com.makepe.curiosityhubls.Models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ContactsActivity extends AppCompatActivity {

    RecyclerView contactsRecycler;

    List<String> followingList;
    List<String> tutorsList;
    List<User> userList;
    //List<ContactsModel> userList;

    String gradeLevel;

    FirebaseContactsAdapter userAdapter;

    FirebaseUser firebaseUser;
    DatabaseReference ref;
    ProgressBar contactsLoader;

    ImageView searchBTN, searchBackBTN;
    EditText searchET;

    RelativeLayout searchToolBar, dialogHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview_layout);

        TextView heading = findViewById(R.id.dialogHeader);
        contactsRecycler = findViewById(R.id.savedRecycler);
        contactsLoader = findViewById(R.id.recyclerLoader);
        searchToolBar = findViewById(R.id.dialogSearchHeader);
        dialogHeader = findViewById(R.id.savedToolbar);
        searchBTN = findViewById(R.id.savedSearchBTN);
        searchET = findViewById(R.id.dialogSearchEd);
        searchBackBTN = findViewById(R.id.dialogSearchBackBTN);

        heading.setText("My Contacts");
        contactsLoader.setVisibility(View.VISIBLE);

        userList = new ArrayList<>();
        tutorsList = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference("Students");
        ref.addValueEventListener(new ValueEventListener() {
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

        contactsRecycler.setHasFixedSize(true);
        contactsRecycler.setNestedScrollingEnabled(false);
        contactsRecycler.hasFixedSize();
        contactsRecycler.setLayoutManager(new LinearLayoutManager(this));

        checkFollowing();

        userAdapter = new FirebaseContactsAdapter(ContactsActivity.this, userList);
        contactsRecycler.setAdapter(userAdapter);

        findViewById(R.id.savedBackBTN).setOnClickListener(view -> finish());

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

    private void checkFollowing(){
        followingList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
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
                Toast.makeText(ContactsActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
                    }catch(NullPointerException ignored){}

                    loadContacts();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ContactsActivity.this, "There was an error loading tutors", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadContacts() {

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Students");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    User contacts = ds.getValue(User.class);

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

                    Collections.sort(userList, (contactsModel, t1) -> contactsModel.getName().compareTo(t1.getName()));

                }
                userAdapter.notifyDataSetChanged();
                contactsLoader.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ContactsActivity.this, "Could not retrieve your contacts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }
}