package com.makepe.curiosityhubls;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.makepe.curiosityhubls.Adapters.CommentsAdapter;
import com.makepe.curiosityhubls.Adapters.UserAdapter;
import com.makepe.curiosityhubls.CustomClasses.GetTimeAgo;
import com.makepe.curiosityhubls.Models.Comment;
import com.makepe.curiosityhubls.Models.PostModel;
import com.makepe.curiosityhubls.Models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.skyhope.showmoretextview.ShowMoreTextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostDetailsActivity extends AppCompatActivity {

    private ImageView backBtn, postImg, commenterImg, postLike, savePostBTN, pdShareBTN;
    private CircleImageView postOwnerImg;
    private String authorId, postID, imgPost, date_post, description;
    private TextView authorName, postDate, commentPost, likesCounter, commentCounter, views;
    private ShowMoreTextView postDesc;
    private FirebaseUser firebaseUser;
    private EditText addComment;
    RelativeLayout pdPicArea;
    Dialog likesDialog;

    private RecyclerView recyclerView;
    private CommentsAdapter commentAdapter;
    private List<Comment> commentList;
    
    ProgressBar picLoader;

    GetTimeAgo getTimeAgo;

    Dialog fullScreenPostPicDialog, popProPicDialog, fullScreenProPicDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        backBtn = findViewById(R.id.backToFeed);
        postOwnerImg = findViewById(R.id.postOwnerProfile);
        authorName = findViewById(R.id.author);
        postImg = findViewById(R.id.postDetailImg);
        postDate = findViewById(R.id.post_date);
        postDesc = findViewById(R.id.post_body);
        commenterImg = findViewById(R.id.commenter_image);
        addComment = findViewById(R.id.add_comment);
        commentPost = findViewById(R.id.post_comment);
        postLike = findViewById(R.id.likePost);
        likesCounter = findViewById(R.id.postLikes);
        commentCounter = findViewById(R.id.commentsNum);
        savePostBTN = findViewById(R.id.savePost);
        pdShareBTN = findViewById(R.id.pdShareBTN);
        views = findViewById(R.id.pdViewsCounter);
        picLoader = findViewById(R.id.picLoader);
        pdPicArea = findViewById(R.id.pdPicArea);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        postID = getIntent().getExtras().getString("postID");

        getTimeAgo = new GetTimeAgo();

        recyclerView = findViewById(R.id.recycler_comments);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();
        commentAdapter = new CommentsAdapter(this, commentList);
        recyclerView.setAdapter(commentAdapter);

        getPostDetails(postID);
        isLiked(postID, postLike);
        nrLikes(likesCounter, postID);
        iniLikesDialog(postID);
        readComments(postID);
        getUserInfo(commenterImg, firebaseUser.getUid());
        isSaved(postID, savePostBTN);
        iniFullScreenPostPicDialog(postID);
        seenNumber(postID);

        postDesc.setShowingLine(3);
        postDesc.addShowMoreText("Continue Reading");
        postDesc.addShowLessText("Show Less");
        postDesc.setShowMoreColor(R.color.identity_dark);
        postDesc.setShowLessTextColor(R.color.identity);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        authorName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(authorId.equals(firebaseUser.getUid())){
                    Intent intent1 = new Intent(PostDetailsActivity.this, ProfileActivity.class);
                    startActivity(intent1);
                }else{
                    Intent intent = new Intent(PostDetailsActivity.this, ViewProfileActivity.class);
                    intent.putExtra("uid", authorId);
                    startActivity(intent);

                }
            }
        });

        commentPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(addComment.getText().toString().equals("")){
                    Toast.makeText(PostDetailsActivity.this, "You can't send empty comment", Toast.LENGTH_SHORT).show();
                } else {
                    addComment(postID);
                }
            }
        });

        savePostBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(savePostBTN.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(postID).setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(postID).removeValue();
                }
            }
        });

        postLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(postLike.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postID)
                            .child(firebaseUser.getUid()).setValue(true);
                    if(!authorId.equals(firebaseUser.getUid()))
                        addNotifications(authorId, postID);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postID)
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        pdShareBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(PostDetailsActivity.this, "You will share this post", Toast.LENGTH_SHORT).show();
            }
        });

        postImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullScreenPostPicDialog.show();
            }
        });

        postOwnerImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popProPicDialog.show();
            }
        });

        likesCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likesDialog.show();
            }
        });

    }

    private void iniLikesDialog(String postID) {
        likesDialog = new Dialog(PostDetailsActivity.this);
        likesDialog.setContentView(R.layout.recyclerview_layout);
        likesDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        likesDialog.getWindow().setLayout(androidx.appcompat.widget.Toolbar.LayoutParams.MATCH_PARENT, androidx.appcompat.widget.Toolbar.LayoutParams.MATCH_PARENT);
        likesDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        TextView dialogHeader = likesDialog.findViewById(R.id.dialogHeader);
        dialogHeader.setText("People who liked this post");

        UserAdapter userAdapter;
        ArrayList<User> userList = new ArrayList<>();
        RecyclerView likesRecycler = likesDialog.findViewById(R.id.savedRecycler);
        likesRecycler.setHasFixedSize(true);
        likesRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        likesRecycler.setLayoutManager(layoutManager);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Likes");
        reference.child(postID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    //User user = ds.getValue(User.class);

                    String hisUID = "" + ds.getRef().getKey();

                    getUsers(hisUID, userList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        userAdapter = new UserAdapter(PostDetailsActivity.this, userList);
        likesRecycler.setAdapter(userAdapter);

        likesDialog.findViewById(R.id.savedBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likesDialog.dismiss();
            }
        });
    }

    private void getUsers(String hisUID, ArrayList<User> userList) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Students");
        ref.orderByChild("userId").equalTo(hisUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds1 : snapshot.getChildren()){
                            User user1 = ds1.getValue(User.class);
                            userList.add(user1);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void seenNumber(String postID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("PostViews")
                .child(postID);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount() == 1)
                    views.setText(snapshot.getChildrenCount() + " View");
                else
                    views.setText(snapshot.getChildrenCount() + " Views");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addNotifications(String authorId, String postId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(authorId);
        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "liked your post");
        hashMap.put("postid", postId);
        hashMap.put("ispost", true);
        hashMap.put("isStory", false);
        hashMap.put("timeStamp", timeStamp);

        reference.push().setValue(hashMap);
    }

    private void iniFullScreenProPicDialog(String authorId) {
        fullScreenProPicDialog = new Dialog(this);
        fullScreenProPicDialog.setContentView(R.layout.fullscreen_image_view);
        fullScreenProPicDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        fullScreenProPicDialog.getWindow().setLayout(androidx.appcompat.widget.Toolbar.LayoutParams.MATCH_PARENT, androidx.appcompat.widget.Toolbar.LayoutParams.MATCH_PARENT);
        fullScreenProPicDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        ImageView fullScreenPic = fullScreenProPicDialog.findViewById(R.id.fullScreenPic);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Students").child(authorId);
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

        fullScreenProPicDialog.findViewById(R.id.fullScreenBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullScreenProPicDialog.dismiss();
            }
        });
    }

    private void iniPopProPicDialog(String authorId) {
        popProPicDialog = new Dialog(this);
        popProPicDialog.setContentView(R.layout.profile_pic_pop_up_layout);
        popProPicDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popProPicDialog.getWindow().setLayout(android.widget.Toolbar.LayoutParams.WRAP_CONTENT, android.widget.Toolbar.LayoutParams.WRAP_CONTENT);
        popProPicDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        ImageView viewProfile = popProPicDialog.findViewById(R.id.popUP_ViewProfile);
        ImageView sendMessage = popProPicDialog.findViewById(R.id.popUP_SendMessage);
        final ImageView superProPic = popProPicDialog.findViewById(R.id.popUP_ProPic);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Students");
        Query query = reference.orderByChild("userId").equalTo(authorId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        User user = ds.getValue(User.class);
                        String image = user.getProfileImg();
                        //authorID = user.getUserId();

                        try{
                            Picasso.get().load(image).placeholder(R.drawable.person_img).into(superProPic);
                        }catch (Exception e){
                            Picasso.get().load(R.drawable.person_img).into(superProPic);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final FirebaseUser user =  FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        if(authorId.equals(user.getUid())){
            sendMessage.setVisibility(View.GONE);

        }

        viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(authorId.equals(user.getUid())){

                    Intent intent = new Intent(PostDetailsActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    popProPicDialog.dismiss();

                }else{
                    Intent intent = new Intent(PostDetailsActivity.this, ViewProfileActivity.class);
                    intent.putExtra("uid", authorId);
                    startActivity(intent);
                    popProPicDialog.dismiss();
                }

            }
        });

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent1 = new Intent(PostDetailsActivity.this, MessageActivity.class);
                intent1.putExtra("receiverID", authorId);
                startActivity(intent1);
                popProPicDialog.dismiss();
            }
        });

        superProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreenProPicDialog.show();
                popProPicDialog.dismiss();
            }
        });

    }

    private void getPostDetails(String postId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = reference.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    PostModel post = ds.getValue(PostModel.class);

                    assert post != null;
                    imgPost = post.getPostImage();
                    date_post = post.getPostTime();
                    description = post.getCaption();
                    authorId = post.getUid();

                    postDesc.setText(description);

                    publisherInfo(authorId);
                    iniPopProPicDialog(authorId);
                    iniFullScreenProPicDialog(authorId);

                    if(imgPost.equals("noImage")){
                        postImg.setVisibility(View.GONE);
                        pdPicArea.setVisibility(View.GONE);
                        picLoader.setVisibility(View.GONE);

                    }else{
                        try{
                            Picasso.get().load(imgPost).placeholder(R.drawable.add_sign).into(postImg);
                            picLoader.setVisibility(View.GONE);
                        }catch (NullPointerException ignored){}
                    }

                    String PostTime = getTimeAgo.getTimeAgo(Long.parseLong(date_post), PostDetailsActivity.this);
                    postDate.setText(PostTime);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void iniFullScreenPostPicDialog(String postId) {
        fullScreenPostPicDialog = new Dialog(this);
        fullScreenPostPicDialog.setContentView(R.layout.fullscreen_image_view);
        fullScreenPostPicDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        fullScreenPostPicDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        fullScreenPostPicDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        ImageView fullScreenPic = fullScreenPostPicDialog.findViewById(R.id.fullScreenPic);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PostModel user = snapshot.getValue(PostModel.class);

                assert user != null;
                Picasso.get().load(user.getPostImage()).into(fullScreenPic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        fullScreenPostPicDialog.findViewById(R.id.fullScreenBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullScreenPostPicDialog.dismiss();
            }
        });
    }

    private void isSaved(String postId, ImageView savePostBTN) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postId).exists()){
                    savePostBTN.setImageResource(R.drawable.ic_baseline_bookmark_24);
                    savePostBTN.setTag("saved");
                }else{
                    savePostBTN.setImageResource(R.drawable.ic_baseline_bookmark_border_24);
                    savePostBTN.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getCommentCount(String postId, TextView commentCounter) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 1)
                    commentCounter.setText(dataSnapshot.getChildrenCount() + " comment");
                else
                    commentCounter.setText(+ dataSnapshot.getChildrenCount() +" comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PostDetailsActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void publisherInfo(String authorId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Students").child(authorId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;

                if(user.getProfileImg().equals("")){
                    postOwnerImg.setImageResource(R.drawable.user_icon);
                }else {
                    Glide.with(getApplicationContext()).load(user.getProfileImg()).into(postOwnerImg);
                }
                authorName.setText(user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserInfo(final ImageView imageView, String publisherId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Students").child(publisherId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                Glide.with(getApplicationContext()).load(user.getProfileImg()).into(imageView);
                //username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addComment(final String postID){
        String commentTimestamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postID);
        String commentId = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", addComment.getText().toString());
        hashMap.put("cID", commentId);
        hashMap.put("Timestamp", commentTimestamp);
        hashMap.put("uid", firebaseUser.getUid());

        reference.push().setValue(hashMap)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if(authorId.equals(firebaseUser.getUid())){
                        Toast.makeText(PostDetailsActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                    }else {
                        sendNotification(addComment);
                    }
                    addComment.setText("");
                }
            }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void sendNotification(EditText addComment) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "commented: " + addComment.getText().toString());
        hashMap.put("postid", postID);
        hashMap.put("ispost", true);
        hashMap.put("timeStamp", timeStamp);

        reference.push().setValue(hashMap);
    }

    private void readComments(final String momentId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(momentId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Comment comment = snapshot.getValue(Comment.class);
                    commentList.add(comment);
                }

                commentAdapter.notifyDataSetChanged();
                getCommentCount(postID, commentCounter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isLiked(String postid, final ImageView likeBTN){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(firebaseUser.getUid()).exists()){
                    likeBTN.setImageResource(R.drawable.ic_baseline_liked);
                    likeBTN.setTag("liked");
                }else{
                    likeBTN.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    likeBTN.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void nrLikes(final TextView likes, String postid){
        DatabaseReference reference =  FirebaseDatabase.getInstance().getReference().child("Likes")
                .child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount() == 1)
                    likes.setText(snapshot.getChildrenCount() + " like");
                else
                    likes.setText(snapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}