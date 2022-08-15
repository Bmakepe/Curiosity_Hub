package com.makepe.curiosityhubls;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.Query;
import com.makepe.curiosityhubls.Adapters.ContactsAdapter;
import com.makepe.curiosityhubls.Adapters.ImageAdapter;
import com.makepe.curiosityhubls.Adapters.MaterialAdapter;
import com.makepe.curiosityhubls.Adapters.PostAdapter;
import com.makepe.curiosityhubls.Adapters.SchoolsAdapter;
import com.makepe.curiosityhubls.Adapters.UserAdapter;
import com.makepe.curiosityhubls.CustomClasses.ContactsList;
import com.makepe.curiosityhubls.Models.ContactsModel;
import com.makepe.curiosityhubls.Models.Material;
import com.makepe.curiosityhubls.Models.PostModel;
import com.makepe.curiosityhubls.Models.Schools;
import com.makepe.curiosityhubls.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    ImageView backBTN, optionsMenu;
    CircleImageView profilePic;
    TextView profileName, profileDescription, followersNo, followingNo, postsCounter, mediaCounter;

    Dialog savedMaterialDialog, savedPostsDialog, postsDialog, followersDialog, eventsDialog, searchDialog, contactsDialog, fullScreenDialog, followingDialog, mediaDialog;

    //firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private FirebaseAuth firebaseAuth;
    DatabaseReference subjectRef;

    //for following list
    private List<String> allFollowing;
    private List<User> followingList;
    private List<Schools> schoolsList;

    private List<String> allFollowers;
    private List<User> followersList;

    private UserAdapter userAdapter;
    private PostAdapter postAdapter;
    SchoolsAdapter schoolsAdapter;

    //for saved material
    List<String> savedList, subjectList;
    List<Material> savedMaterial;
    MaterialAdapter materialAdapter;
    ImageAdapter imageAdapter, mediaPopAdapter;

    List<String> postSaves;
    List<PostModel> postListSaves, mediaPopUpList, mediaList;

    String image;

    RecyclerView savedRecycler, mediaRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        backBTN = findViewById(R.id.silhouetteBackBTN);
        optionsMenu = findViewById(R.id.menuBTN);
        profilePic = findViewById(R.id.profilePicture);
        profileName = findViewById(R.id.profileName);
        profileDescription = findViewById(R.id.profileDescription);
        followersNo = findViewById(R.id.proFollowers);
        followingNo = findViewById(R.id.proFollowing);
        postsCounter = findViewById(R.id.postsCounter);
        mediaRecycler = findViewById(R.id.myMedia);
        mediaCounter = findViewById(R.id.MediaNo);

        //initiate firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        assert firebaseUser != null;
        String uid = firebaseUser.getUid();

        iniSavedMaterialDialog();
        iniPostsDialog();
        iniSavedPostsDialog();
        iniFollowersDialog();
        iniFollowingDialog();
        iniEventsDialog();
        iniMediaDialog();
        getFollowersNo(uid);
        //iniSearchDialog();
        iniContactsDialog();
        getNrPosts();
        iniFullScreenProPicDialog();
        getMedia();

        subjectRef = FirebaseDatabase.getInstance().getReference("Subjects");

        reference = FirebaseDatabase.getInstance().getReference("Students").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                image = user.getProfileImg();

                profileName.setText(user.getName());
                profileDescription.setText(user.getBio());

                try{
                    Picasso.get().load(image).into(profilePic);
                }catch (NullPointerException ignored){}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullScreenDialog.show();
            }
        });
        
        optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions();
            }
        });

        findViewById(R.id.myPosts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postsDialog.show();
            }
        });

        findViewById(R.id.followers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followersDialog.show();
            }
        });

        findViewById(R.id.following).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followingDialog.show();
            }
        });

        findViewById(R.id.notificationsBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventsDialog.show();
            }
        });

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.viewMyMedia).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaDialog.show();
            }
        });

        findViewById(R.id.newPostFAB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, PostActivity.class));
            }
        });

    }

    private void iniMediaDialog() {
        mediaDialog = new Dialog(this);
        mediaDialog.setContentView(R.layout.recyclerview_layout);
        mediaDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mediaDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        mediaDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        TextView mediaHeader = mediaDialog.findViewById(R.id.dialogHeader);

        mediaHeader.setText("My Media");

        ImageView searchBTN = mediaDialog.findViewById(R.id.savedSearchBTN);
        searchBTN.setVisibility(View.GONE);

        RecyclerView mediaPopRecycler = mediaDialog.findViewById(R.id.savedRecycler);

        mediaDialog.findViewById(R.id.savedBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaDialog.dismiss();
            }
        });

        getPopMedia(mediaPopRecycler);
    }

    private void getPopMedia(RecyclerView mediaPopRecycler) {
        mediaPopRecycler.setHasFixedSize(true);
        mediaPopRecycler.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(this, 3);
        mediaPopRecycler.setLayoutManager(linearLayoutManager);

        mediaPopUpList = new ArrayList<>();

        mediaPopAdapter = new ImageAdapter(ProfileActivity.this, mediaPopUpList);
        mediaPopRecycler.setAdapter(mediaPopAdapter);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.keepSynced(true);
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mediaPopUpList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel modelPost = snapshot.getValue(PostModel.class);
                    if(!modelPost.getPostImage().equals("noImage") && user.getUid().equals(modelPost.getUid())){
                        mediaPopUpList.add(modelPost);
                    }

                }

                Collections.reverse(mediaPopUpList);
                mediaPopAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMedia() {
        mediaRecycler.setHasFixedSize(true);
        mediaRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(ProfileActivity.this,LinearLayoutManager.HORIZONTAL, false);
        //StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL);
        mediaRecycler.setLayoutManager(layoutManager);
        mediaRecycler.setItemAnimator(new DefaultItemAnimator());

        mediaList = new ArrayList<>();

        imageAdapter = new ImageAdapter(ProfileActivity.this, mediaList);
        mediaRecycler.setAdapter(imageAdapter);

        loadMedia();
    }

    private void loadMedia() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.keepSynced(true);
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mediaList.clear();
                int mediaCount = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel modelPost = snapshot.getValue(PostModel.class);
                    if(!modelPost.getPostImage().equals("noImage") && user.getUid().equals(modelPost.getUid())){
                        mediaList.add(modelPost);
                        mediaCount++;
                    }

                }
                mediaCounter.setText("My Media [" + mediaCount + "]");
                Collections.reverse(mediaList);
                imageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniFullScreenProPicDialog() {
        fullScreenDialog = new Dialog(this);
        fullScreenDialog.setContentView(R.layout.fullscreen_image_view);
        fullScreenDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        fullScreenDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        fullScreenDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        ImageView fullScreenPic = fullScreenDialog.findViewById(R.id.fullScreenPic);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Students").child(firebaseUser.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                assert user != null;
                Picasso.get().load(user.getProfileImg()).into(fullScreenPic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        fullScreenDialog.findViewById(R.id.fullScreenBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullScreenDialog.dismiss();
            }
        });
    }

    private void iniContactsDialog() {

        contactsDialog = new Dialog(this);
        contactsDialog.setContentView(R.layout.recyclerview_layout);
        contactsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        contactsDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        contactsDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        TextView heading = contactsDialog.findViewById(R.id.dialogHeader);
        RelativeLayout searchToolBar = contactsDialog.findViewById(R.id.dialogSearchHeader);
        RelativeLayout dialogHeader = contactsDialog.findViewById(R.id.savedToolbar);
        ImageView searchBTN = contactsDialog.findViewById(R.id.savedSearchBTN);
        EditText searchET = contactsDialog.findViewById(R.id.dialogSearchEd);
        ImageView searchBackBTN = contactsDialog.findViewById(R.id.dialogSearchBackBTN);
        heading.setText("Invite Friends");

        RecyclerView contactsRecycler = contactsDialog.findViewById(R.id.savedRecycler);

        contactsRecycler.setHasFixedSize(true);
        contactsRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        contactsRecycler.setLayoutManager(layoutManager);

        List<ContactsModel> contactsList = new ArrayList<>();
        ContactsList contactList1 = new ContactsList(contactsList, this);

        contactList1.readContacts();

        ContactsAdapter contactsAdapter = new ContactsAdapter(contactsList, this);
        contactsRecycler.setAdapter(contactsAdapter);

        contactsDialog.findViewById(R.id.savedBackBTN).setOnClickListener(view -> contactsDialog.dismiss());

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

    private void getFollowersNo(String uid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
            .child("Follow").child(uid).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                followersNo.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });//function to get number of followers

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(uid).child("following");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingNo.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });//function to get number of user i follow
    }

    private void iniEventsDialog() {
        eventsDialog = new Dialog(this);
        eventsDialog.setContentView(R.layout.recyclerview_layout);
        eventsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        eventsDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        eventsDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        TextView dialogHeader = eventsDialog.findViewById(R.id.dialogHeader);
        dialogHeader.setText("Events");

        eventsDialog.findViewById(R.id.savedBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventsDialog.dismiss();
            }
        });
    }

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(ProfileActivity.this, optionsMenu, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0,0,"Invite Friends");
        popupMenu.getMenu().add(Menu.NONE, 1,0,"Saved Posts");
        popupMenu.getMenu().add(Menu.NONE, 2,0,"Settings");
        popupMenu.getMenu().add(Menu.NONE, 3,0,"Log Out");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if(id == 0){
                    contactsDialog.show();
                }else if(id == 1){
                    showCategories();
                }else if(id == 2){
                    startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
                }else if(id == 3){
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(ProfileActivity.this, RegisterActivity.class));
                    finish();
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void showCategories() {
        String[] options = {"Saved Material", "Saved Posts"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0){
                    savedMaterialDialog.show();
                }else if(i == 1){
                    savedPostsDialog.show();
                }
            }
        });
        builder.create().show();
    }

    private void iniFollowingDialog() {
        followingDialog = new Dialog(this);
        followingDialog.setContentView(R.layout.recyclerview_layout);
        followingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        followingDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        followingDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        RecyclerView followingRecycler = followingDialog.findViewById(R.id.savedRecycler);
        TextView header = followingDialog.findViewById(R.id.dialogHeader);

        ImageView searchBTN = followingDialog.findViewById(R.id.savedSearchBTN);
        searchBTN.setVisibility(View.GONE);

        followingRecycler.setVisibility(View.VISIBLE);
        header.setText("Following");
        getFollowingList(followingRecycler);

        followingDialog.findViewById(R.id.savedBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followingDialog.dismiss();
            }
        });
    }

    private void iniFollowersDialog() {
        followersDialog = new Dialog(this);
        followersDialog.setContentView(R.layout.recyclerview_layout);
        followersDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        followersDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        followersDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        RecyclerView followingRecycler = followersDialog.findViewById(R.id.savedRecycler);
        TextView header = followersDialog.findViewById(R.id.dialogHeader);

        ImageView searchBTN = followersDialog.findViewById(R.id.savedSearchBTN);
        searchBTN.setVisibility(View.GONE);

        followingRecycler.setVisibility(View.VISIBLE);
        header.setText("Followers");
        getFollowersList(followingRecycler);

        followersDialog.findViewById(R.id.savedBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followersDialog.dismiss();
            }
        });

    }

    private void getNrPosts(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel post = snapshot.getValue(PostModel.class);
                    assert post != null;
                    if(post.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        i++;
                    }
                }

                try{
                    postsCounter.setText(i+"");
                }catch (NullPointerException ignored){ }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowersList(RecyclerView followersRecycler) {
        allFollowers = new ArrayList<>();

        followersRecycler.setHasFixedSize(true);
        followersRecycler.hasFixedSize();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        followersRecycler.setLayoutManager(layoutManager);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Follow").child(user.getUid()).child("followers");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allFollowers.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    allFollowers.add(ds.getKey());
                }
                loadFollowers(followersRecycler);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadFollowers(RecyclerView followersRecycler) {
        followersList = new ArrayList<>();

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Students");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followersList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    for(String id : allFollowers){
                        assert user != null;
                        if(user.getUserId().equals(id))
                            followersList.add(user);

                    }
                    userAdapter = new UserAdapter(ProfileActivity.this, followersList);
                    followersRecycler.setAdapter(userAdapter);

                    Collections.sort(followersList, new Comparator<User>() {
                        @Override
                        public int compare(User contactsModel, User t1) {
                            return contactsModel.getName().compareTo(t1.getName());
                        }
                    });
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowingList(RecyclerView followingRecycler) {
        allFollowing = new ArrayList<>();

        followingRecycler.setHasFixedSize(true);
        followingRecycler.hasFixedSize();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        followingRecycler.setLayoutManager(layoutManager);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Follow")
                .child(user.getUid()).child("following");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allFollowing.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    allFollowing.add(ds.getKey());
                }
                loadFollowing(followingRecycler);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadFollowing(RecyclerView followingRecycler) {
        followingList = new ArrayList<>();
        schoolsList = new ArrayList<>();

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Students");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);

                    for(String id : allFollowing){
                        assert user != null;
                        if(user.getUserId().equals(id))
                            followingList.add(user);
                        else{
                            final DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Schools");
                            reference1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    schoolsList.clear();
                                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                        Schools user = dataSnapshot.getValue(Schools.class);
                                        for(String id : allFollowing){
                                            assert user != null;
                                            if(user.getSchool_ID().equals(id))
                                                schoolsList.add(user);
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }

                    }

                    userAdapter = new UserAdapter(ProfileActivity.this, followingList);
                    schoolsAdapter = new SchoolsAdapter(ProfileActivity.this, schoolsList);

                    followingRecycler.setAdapter(userAdapter);

                    Collections.sort(followingList, (contactsModel, t1) -> contactsModel.getName().compareTo(t1.getName()));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void iniPostsDialog() {
        postsDialog = new Dialog(this);
        postsDialog.setContentView(R.layout.recyclerview_layout);
        postsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        postsDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        postsDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        TextView Header = postsDialog.findViewById(R.id.dialogHeader);
        RecyclerView postRecycler = postsDialog.findViewById(R.id.savedRecycler);
        Header.setText("My Posts");

        ImageView searchBTN = postsDialog.findViewById(R.id.savedSearchBTN);
        searchBTN.setVisibility(View.GONE);

        postRecycler.setHasFixedSize(true);
        postRecycler.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        postRecycler.setLayoutManager(linearLayoutManager);

        List<PostModel> postList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel modelPost = snapshot.getValue(PostModel.class);
                    if(user.getUid().equals(modelPost.getUid())){
                        postList.add(modelPost);
                    }

                    postAdapter = new PostAdapter(ProfileActivity.this, postList);
                    postRecycler.setAdapter(postAdapter);

                }
                Collections.reverse(postList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        postsDialog.findViewById(R.id.savedBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postsDialog.dismiss();
            }
        });
    }

    private void iniSavedMaterialDialog() {
        savedMaterialDialog = new Dialog(this);
        savedMaterialDialog.setContentView(R.layout.recyclerview_layout);
        savedMaterialDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        savedMaterialDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        savedMaterialDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        savedRecycler = savedMaterialDialog.findViewById(R.id.savedRecycler);
        getSavedMaterial();

        ImageView searchBTN = savedMaterialDialog.findViewById(R.id.savedSearchBTN);
        searchBTN.setVisibility(View.GONE);

        savedRecycler.setHasFixedSize(true);
        savedRecycler.hasFixedSize();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        savedRecycler.setLayoutManager(layoutManager);

        savedMaterialDialog.findViewById(R.id.savedBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savedMaterialDialog.dismiss();
            }
        });

        savedMaterialDialog.findViewById(R.id.savedSearchBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchDialog.show();
            }
        });
    }

    private void getSavedMaterial() {

        savedList = new ArrayList<>();
        subjectList = new ArrayList<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Saves").child(user.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                savedList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    savedList.add(ds.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference subjectRefer = FirebaseDatabase.getInstance().getReference("Subjects");
        subjectRefer.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjectList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    subjectList.add(ds.getKey());
                }
                loadMaterial();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadMaterial() {
        savedMaterial = new ArrayList<>();
        savedMaterial.clear();

        for(String SubjectID : subjectList){

            final DatabaseReference subReference = FirebaseDatabase.getInstance().getReference("Subjects").child(SubjectID);

                subReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot ds : snapshot.getChildren()) {

                            Material material = ds.getValue(Material.class);

                            for (String id : savedList) {

                                assert material != null;
                                if (material.getMaterial_ID().equals(id)) {
                                    savedMaterial.add(material);
                                }
                            }

                            materialAdapter = new MaterialAdapter(ProfileActivity.this, savedMaterial);
                            savedRecycler.setAdapter(materialAdapter);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        }

    }

    private void iniSavedPostsDialog() {
        savedPostsDialog = new Dialog(this);
        savedPostsDialog.setContentView(R.layout.recyclerview_layout);
        savedPostsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        savedPostsDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        savedPostsDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        RecyclerView savedRecycler = savedPostsDialog.findViewById(R.id.savedRecycler);
        TextView heading = savedPostsDialog.findViewById(R.id.dialogHeader);
        heading.setText("Saved Posts");

        ImageView searchBTN = savedPostsDialog.findViewById(R.id.savedSearchBTN);
        searchBTN.setVisibility(View.GONE);

        getSavedPosts(savedRecycler);

        savedPostsDialog.findViewById(R.id.savedBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savedPostsDialog.dismiss();
            }
        });

        savedPostsDialog.findViewById(R.id.savedSearchBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchDialog.show();
            }
        });

    }

    private void getSavedPosts(RecyclerView savedRecycler) {
        postSaves = new ArrayList<>();

        savedRecycler.setHasFixedSize(true);
        savedRecycler.hasFixedSize();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        savedRecycler.setLayoutManager(layoutManager);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Saves").child(user.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postSaves.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    postSaves.add(ds.getKey());
                }
                loadSavedPosts(savedRecycler);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadSavedPosts(RecyclerView savedRecycler) {
        postListSaves = new ArrayList<>();

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    postListSaves.clear();
                    for(DataSnapshot ds: snapshot.getChildren()){
                        PostModel post = ds.getValue(PostModel.class);
                        for(String id : postSaves){

                            assert post != null;
                            try{
                                if(post.getpId().equals(id)) {
                                    postListSaves.add(post);
                                }
                            }catch (NullPointerException e){
                                Toast.makeText(ProfileActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        postAdapter = new PostAdapter(ProfileActivity.this, postListSaves);
                        savedRecycler.setAdapter(postAdapter);
                    }
                    postAdapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(ProfileActivity.this, "Could not retrieve any saved material", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}