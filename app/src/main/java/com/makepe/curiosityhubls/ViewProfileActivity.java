package com.makepe.curiosityhubls;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.makepe.curiosityhubls.Adapters.ImageAdapter;
import com.makepe.curiosityhubls.Adapters.PostAdapter;
import com.makepe.curiosityhubls.Adapters.UserAdapter;
import com.makepe.curiosityhubls.CustomClasses.ContactsList;
import com.makepe.curiosityhubls.Models.ContactsModel;
import com.makepe.curiosityhubls.Models.PostModel;
import com.makepe.curiosityhubls.Models.Schools;
import com.makepe.curiosityhubls.Models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity {

    FloatingActionButton chatBTN;
    FirebaseUser firebaseUser;
    String hisid;
    String school, number, userRole;
    CircleImageView hisProfilePic;
    TextView hisNameTV, followingNo, followersNo, schoolDescription, postsCounter, seeMore, mediaCounter, followTV;

    ImageView postNotificationBTN, followBTN;
    Dialog postsDialog, followersDialog, followingDialog, fullScreenDialog, mediaDialog;

    LinearLayout posts, followers, following;

    private List<String> allFollowers;
    private List<User> followersList;

    private List<String> allFollowing;
    private List<User> followingList;

    private UserAdapter userAdapter;
    private PostAdapter postAdapter;

    RelativeLayout btnFollow, notificationBTN;
    boolean flag = false;

    SwitchCompat postSwitch;
    SharedPreferences sp;       //use shared preferences to save the state of switch
    SharedPreferences.Editor editor;    //to edit value f shared pref

    public static final String TOPIC_POST_NOTIFICATION = "POST";    //to assign any value but use sme for this kind notification

    //for his media
    RecyclerView hisMediaRecycler, mediaPopRecycler;
    List<PostModel> mediaList, mediaPopUpList;
    ImageAdapter mediaAdapter, mediaPopAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);

        chatBTN = findViewById(R.id.messageFAB);
        postNotificationBTN = findViewById(R.id.postNotificationBTN);
        followBTN = findViewById(R.id.followUserBTN);
        hisProfilePic = findViewById(R.id.school_Image);
        hisNameTV = findViewById(R.id.schoolName);
        followersNo = findViewById(R.id.followersNo);
        followingNo = findViewById(R.id.followingNo);
        posts = findViewById(R.id.posts);
        followers = findViewById(R.id.followers);
        following  = findViewById(R.id.following);
        schoolDescription = findViewById(R.id.schoolDescription);
        postsCounter = findViewById(R.id.hisPostsCounter);
        seeMore = findViewById(R.id.viewHisMedia);
        mediaCounter = findViewById(R.id.hisMediaNo);
        hisMediaRecycler = findViewById(R.id.hisMedia);
        followTV = findViewById(R.id.followTV);
        btnFollow = findViewById(R.id.btnFollow);
        notificationBTN = findViewById(R.id.btnNotification);
        postSwitch = findViewById(R.id.postSwitch);

        final Intent intent = getIntent();
        hisid = intent.getStringExtra("uid");
        sp = getSharedPreferences("Notification_SP", MODE_PRIVATE);
        boolean isPostEnabled = sp.getBoolean("" + TOPIC_POST_NOTIFICATION, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getUserInfo(hisid);
        getFollowers();
        isFollowing(hisid, followBTN);
        iniPostsDialog(hisid);
        iniFollowersDialog(hisid);
        iniFollowingDialog(hisid);
        getNrPosts(hisid);
        iniFullScreenPic(hisid);
        iniMediaDialog(hisid);
        getHisMedia(hisid);

        //if post notification is enabled check switch, otherwise uncheck switch - by default unchecked/false
        postSwitch.setChecked(isPostEnabled);

        chatBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(ViewProfileActivity.this, ChatActivity.class);
                Intent intent = new Intent(ViewProfileActivity.this, MessageActivity.class);
                intent.putExtra("receiverID", hisid);
                startActivity(intent);
            }
        });

        posts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postsDialog.show();
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followingDialog.show();
            }
        });

        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followersDialog.show();
            }
        });

        findViewById(R.id.profileBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        notificationBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(ViewProfileActivity.this, "You will be able to switch post notifications on or off", Toast.LENGTH_SHORT).show();
                if(!flag){
                    postNotificationBTN.setImageResource(R.drawable.ic_baseline_notifications_off_24);
                    flag = true;
                    Toast.makeText(ViewProfileActivity.this, "Notifications On", Toast.LENGTH_SHORT).show();
                }else{
                    postNotificationBTN.setImageResource(R.drawable.ic_baseline_notifications_24);
                    flag = true;
                    Toast.makeText(ViewProfileActivity.this, "Notifications Off", Toast.LENGTH_SHORT).show();
                }
            }
        });

        postSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {

            editor = sp.edit();
            editor.putBoolean("" + TOPIC_POST_NOTIFICATION, isChecked);
            editor.apply();

            if(isChecked){
                subscribePostNotification();
            }else{
                unsubscribePostNotification();
            }
        });

        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(followBTN.getTag().equals("Follow") && userRole.equals("Student")){

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(hisid).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(hisid)
                            .child("followers").child(firebaseUser.getUid()).setValue(true);

                    addNotifications();

                    btnFollow.setVisibility(View.VISIBLE);
                    notificationBTN.setVisibility(View.VISIBLE);
                    chatBTN.setVisibility(View.VISIBLE);
                }else{

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(hisid).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(hisid)
                            .child("followers").child(firebaseUser.getUid()).removeValue();

                    btnFollow.setVisibility(View.VISIBLE);
                    notificationBTN.setVisibility(View.GONE);
                    chatBTN.setVisibility(View.GONE);
                }
            }
        });

        hisProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullScreenDialog.show();
            }
        });
        seeMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaDialog.show();
            }
        });

    }

    private void unsubscribePostNotification() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("" + TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You will not receive post notifications";
                        if(!task.isSuccessful()){
                            msg = "Unsubscription Failed";
                        }
                        Toast.makeText(ViewProfileActivity.this, msg, Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void subscribePostNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic("" + TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "You will receive post notifications";
                        if(!task.isSuccessful()){
                            msg = "Subscription Failed";
                        }
                        Toast.makeText(ViewProfileActivity.this, msg, Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void getHisMedia(String hisid) {
        hisMediaRecycler.setHasFixedSize(true);
        hisMediaRecycler.setNestedScrollingEnabled(false);
        hisMediaRecycler.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ViewProfileActivity.this,LinearLayoutManager.HORIZONTAL, false);
        hisMediaRecycler.setLayoutManager(linearLayoutManager);
        hisMediaRecycler.setItemAnimator(new DefaultItemAnimator());

        mediaList = new ArrayList<>();

        mediaAdapter = new ImageAdapter(ViewProfileActivity.this, mediaList);
        hisMediaRecycler.setAdapter(mediaAdapter);

        loadHisMedia(hisid);
    }

    private void loadHisMedia(String hisid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.keepSynced(true);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mediaList.clear();
                int mediaCount = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel modelPost = snapshot.getValue(PostModel.class);
                    if(!modelPost.getPostImage().equals("noImage") && hisid.equals(modelPost.getUid())){
                        mediaList.add(modelPost);
                        mediaCount++;
                    }

                }
                mediaCounter.setText("Media[" + mediaCount + "]");
                Collections.reverse(mediaList);
                mediaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniMediaDialog(String hisid) {
        mediaDialog = new Dialog(this);
        mediaDialog.setContentView(R.layout.recyclerview_layout);
        mediaDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mediaDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        mediaDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        TextView mediaHeader = mediaDialog.findViewById(R.id.dialogHeader);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Students");
        ref.child(hisid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);

                    mediaHeader.setText(user.getName() + "'s Media Posts");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ImageView searchBTN = mediaDialog.findViewById(R.id.savedSearchBTN);
        searchBTN.setVisibility(View.GONE);

        mediaPopRecycler = mediaDialog.findViewById(R.id.savedRecycler);

        mediaDialog.findViewById(R.id.savedBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaDialog.dismiss();
            }
        });

        getPopMedia(mediaPopRecycler, hisid);

    }

    private void getPopMedia(RecyclerView mediaPopRecycler, String hisid) {
        mediaPopRecycler.setHasFixedSize(true);
        mediaPopRecycler.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(this, 3);
        mediaPopRecycler.setLayoutManager(linearLayoutManager);

        mediaPopUpList = new ArrayList<>();

        mediaPopAdapter = new ImageAdapter(this, mediaPopUpList);
        mediaPopRecycler.setAdapter(mediaPopAdapter);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mediaPopUpList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel modelPost = snapshot.getValue(PostModel.class);
                    if(!modelPost.getPostImage().equals("noImage") && hisid.equals(modelPost.getUid())){
                        mediaPopUpList.add(modelPost);
                    }

                }

                Collections.reverse(mediaPopUpList);
                mediaPopAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniFollowingDialog(String hisid) {
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
        getFollowingList(followingRecycler, hisid);

        followingDialog.findViewById(R.id.savedBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followingDialog.dismiss();
            }
        });

    }

    private void iniFollowersDialog(String hisid) {
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
        getFollowersList(followingRecycler, hisid);

        followersDialog.findViewById(R.id.savedBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followersDialog.dismiss();
            }
        });

    }

    private void iniFullScreenPic(String hisid) {
        fullScreenDialog = new Dialog(this);
        fullScreenDialog.setContentView(R.layout.fullscreen_image_view);
        fullScreenDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        fullScreenDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        fullScreenDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        ImageView fullScreenPic = fullScreenDialog.findViewById(R.id.fullScreenPic);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Students").child(hisid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    Picasso.get().load(user.getProfileImg()).into(fullScreenPic);

                } else {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Schools").child(hisid);
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Schools schools = snapshot.getValue(Schools.class);

                            Picasso.get().load(schools.getImgProfile()).into(fullScreenPic);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
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

    private void addNotifications() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(hisid);
        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "started following you");
        hashMap.put("postid", "");
        hashMap.put("ispost", false);
        hashMap.put("isStory", false);
        hashMap.put("timeStamp", timeStamp);

        reference.push().setValue(hashMap);
    }

    private void getFollowingList(RecyclerView followingRecycler, String hisid) {
        allFollowing = new ArrayList<>();

        followingRecycler.setHasFixedSize(true);
        followingRecycler.hasFixedSize();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        followingRecycler.setLayoutManager(layoutManager);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Follow").child(hisid).child("following");
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

                    }
                    userAdapter = new UserAdapter(ViewProfileActivity.this, followingList);
                    followingRecycler.setAdapter(userAdapter);

                    Collections.sort(followingList, new Comparator<User>() {
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

    private void getFollowersList(RecyclerView followersRecycler, String hisid) {
        allFollowers = new ArrayList<>();

        followersRecycler.setHasFixedSize(true);
        followersRecycler.hasFixedSize();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        followersRecycler.setLayoutManager(layoutManager);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Follow").child(hisid).child("followers");
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
                    userAdapter = new UserAdapter(ViewProfileActivity.this, followersList);
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

    private void iniPostsDialog(String hisid) {
        postsDialog = new Dialog(this);
        postsDialog.setContentView(R.layout.recyclerview_layout);
        postsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        postsDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        postsDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        TextView Header = postsDialog.findViewById(R.id.dialogHeader);

        ImageView searchBTN = postsDialog.findViewById(R.id.savedSearchBTN);
        searchBTN.setVisibility(View.GONE);

        RecyclerView postRecycler = postsDialog.findViewById(R.id.savedRecycler);
        Header.setText("Posts");


        postRecycler.setHasFixedSize(true);
        postRecycler.hasFixedSize();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        postRecycler.setLayoutManager(linearLayoutManager);

        List<PostModel> postList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts"); 
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel modelPost = snapshot.getValue(PostModel.class);
                    if(hisid.equals(modelPost.getUid())){
                        postList.add(modelPost);
                    }

                    postAdapter = new PostAdapter(ViewProfileActivity.this, postList);
                    postRecycler.setAdapter(postAdapter);

                }
                Collections.reverse(postList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        postsDialog.findViewById(R.id.savedBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postsDialog.dismiss();
            }
        });

    }

    private void checkFollow() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
            .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(hisid).exists()){
                    followBTN.setTag("Following");
                    followTV.setText("FOLLOWING");
                    btnFollow.setVisibility(View.VISIBLE);
                    notificationBTN.setVisibility(View.VISIBLE);
                    chatBTN.setVisibility(View.VISIBLE);
                }else{
                    followBTN.setTag("Follow");
                    followTV.setText("FOLLOW");
                    btnFollow.setVisibility(View.VISIBLE);
                    notificationBTN.setVisibility(View.GONE);
                    chatBTN.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isFollowing(String userid, ImageView followBTN) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(userid).exists()){
                    //executes if following user
                    followBTN.setImageResource(R.drawable.person_img);
                    followBTN.setTag("Following");
                }else{
                    followBTN.setImageResource(R.drawable.ic_baseline_person_add_24);
                    followBTN.setTag("Follow");
                    //executes if you are not following the user
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getFollowers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
            .child("Follow").child(hisid).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                followersNo.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(hisid).child("following");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                followingNo.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserInfo(String hisid) {

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Students");
        Query query = reference.orderByChild("userId").equalTo(hisid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);

                        school = user.getSchool();
                        number = user.getPhoneNumber();
                        userRole = user.getRole();

                        if(user.getRole().equals("Student")){
                            checkFollow();
                        }else{
                            btnFollow.setVisibility(View.GONE);
                            chatBTN.setVisibility(View.VISIBLE);
                        }

                        hisNameTV.setText(user.getName());

                        schoolDescription.setText(user.getBio());

                        try{
                            Picasso.get().load(user.getProfileImg()).networkPolicy(NetworkPolicy.OFFLINE).into(hisProfilePic);
                        }catch (Exception e){
                            Picasso.get().load(R.drawable.person_img).into(hisProfilePic);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewProfileActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getNrPosts(String hisid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    PostModel post = snapshot.getValue(PostModel.class);
                    assert post != null;
                    if(post.getUid().equals(hisid)){
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

}