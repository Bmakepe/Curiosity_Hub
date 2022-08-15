package com.makepe.curiosityhubls;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.makepe.curiosityhubls.Adapters.CommentsAdapter;
import com.makepe.curiosityhubls.Adapters.UserAdapter;
import com.makepe.curiosityhubls.Models.Comment;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {

    TextView materialName, mdLikeCounter, mdCommentCounter, postBTN, materialType,
            materialYear, materialSubject, materialGrade, views;
    ImageView mdLikeBTN, mdSaveBTN, mdShareBTN;
    RecyclerView commentsRecycler;
    EditText commentED;
    CircleImageView comProPic;
    LinearLayout documentDetails;
    Dialog likesDialog;

    String materialID, userID, material, type, year, subject, grade, document;

    CommentsAdapter commentsAdapter;

    List<Comment> commentList = new ArrayList<>();
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_pop_up);

        materialName = findViewById(R.id.post_body);
        mdLikeCounter = findViewById(R.id.mdLikeCounter);
        mdCommentCounter = findViewById(R.id.mdCommentsNum);
        postBTN = findViewById(R.id.md_post_comment);
        mdLikeBTN = findViewById(R.id.mdLikeBTN);
        mdSaveBTN = findViewById(R.id.mdSavePost);
        mdShareBTN = findViewById(R.id.mdShareBTN);
        commentsRecycler = findViewById(R.id.md_recycler_comments);
        commentED = findViewById(R.id.md_add_comment);
        comProPic = findViewById(R.id.md_commenter_image);
        materialType = findViewById(R.id.mdMaterialType);
        materialYear = findViewById(R.id.mdMaterialYear);
        materialSubject = findViewById(R.id.mdMaterialSubject);
        materialGrade = findViewById(R.id.mdMaterialGrade);
        documentDetails = findViewById(R.id.documentDetails);
        views = findViewById(R.id.comViewsCounter);

        Intent intent = getIntent();
        materialID = intent.getStringExtra("materialID");
        userID = intent.getStringExtra("userID");
        material = intent.getStringExtra("materialName");
        type = intent.getStringExtra("type");
        subject = intent.getStringExtra("subject");
        grade = intent.getStringExtra("grade");
        document = intent.getStringExtra("document");
        year = intent.getStringExtra("year");

        getUserDetails(userID);
        loadComments(materialID);
        isLiked(materialID, mdLikeBTN);
        nrLikes(mdLikeCounter, materialID);
        isSaved(materialID, mdSaveBTN);
        seenNumber(materialID);
        iniLikesDialog(materialID);

        materialName.setText(material);
        materialType.setText(type);
        materialYear.setText(year);
        materialSubject.setText(subject);
        materialGrade.setText(grade);

        commentsRecycler.setHasFixedSize(true);
        commentsRecycler.hasFixedSize();
        commentsRecycler.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        commentsRecycler.setLayoutManager(layoutManager);

        commentsAdapter = new CommentsAdapter(CommentActivity.this, commentList);

        commentsRecycler.setAdapter(commentsAdapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        postBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = commentED.getText().toString().trim();

                if(TextUtils.isEmpty(comment)){
                    Toast.makeText(CommentActivity.this, "Can not post empty comment", Toast.LENGTH_SHORT).show();
                }else{
                    postComment(comment, postBTN, commentED);
                }
            }
        });

        findViewById(R.id.backToMaterial).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        documentDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(document), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Intent intent1 = Intent.createChooser(intent, "Open File");

                try{
                    startActivity(intent1);
                }catch (ActivityNotFoundException e){

                }
            }
        });

        mdSaveBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mdSaveBTN.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(materialID).setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(materialID).removeValue();
                }
            }
        });

        mdShareBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CommentActivity.this, "You will share this post", Toast.LENGTH_SHORT).show();

            }
        });

        mdLikeBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mdLikeBTN.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(materialID)
                            .child(firebaseUser.getUid()).setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(materialID)
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        mdLikeCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likesDialog.show();
            }
        });

    }

    private void iniLikesDialog(String materialID) {
        likesDialog = new Dialog(CommentActivity.this);
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
        reference.child(materialID).addValueEventListener(new ValueEventListener() {
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

        userAdapter = new UserAdapter(CommentActivity.this, userList);
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

    private void seenNumber(String materialID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("MaterialViews")
                .child(materialID);

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

    private void isSaved(String materialID, ImageView mdSaveBTN) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(materialID).exists()){
                    mdSaveBTN.setImageResource(R.drawable.ic_baseline_bookmark_24);
                    mdSaveBTN.setTag("saved");
                }else{
                    mdSaveBTN.setImageResource(R.drawable.ic_baseline_bookmark_border_24);
                    mdSaveBTN.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getCommentCount(String materialID, TextView mdCommentCounter) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(materialID);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 1)
                    mdCommentCounter.setText(dataSnapshot.getChildrenCount() + " comment");
                else
                    mdCommentCounter.setText(+ dataSnapshot.getChildrenCount() +" comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CommentActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void nrLikes(TextView mdLikeCounter, String materialID) {
        DatabaseReference reference =  FirebaseDatabase.getInstance().getReference().child("Likes")
                .child(materialID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount() == 1)
                    mdLikeCounter.setText(snapshot.getChildrenCount() + " like");
                else
                    mdLikeCounter.setText(snapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void isLiked(String materialID, ImageView mdLikeBTN) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(materialID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(firebaseUser.getUid()).exists()){
                    mdLikeBTN.setImageResource(R.drawable.ic_baseline_liked);
                    mdLikeBTN.setTag("liked");
                }else{
                    mdLikeBTN.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    mdLikeBTN.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void postComment(String comment, TextView sendBTN, EditText commentED) {
        String commentTimestamp = String.valueOf(System.currentTimeMillis());

        //each post will have a child "Comments" that will contain comments of that post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comments").child(materialID);
        String commentID = ref.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        //put info in hashmap
        hashMap.put("cID", commentID);
        hashMap.put("comment", comment);
        hashMap.put("Timestamp", commentTimestamp);
        hashMap.put("uid", firebaseUser.getUid());

        //put this data in db
        ref.child(commentTimestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onSuccess(Void aVoid) {
                        sendBTN.setVisibility(View.VISIBLE);
                        //sendNotification(comCommentED);
                        commentED.setText(null);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onFailure(@NonNull Exception e) {

                sendBTN.setVisibility(View.VISIBLE);
                Toast.makeText(CommentActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComments(String materialID) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comments").child(materialID);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    commentList.clear();
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        Comment commentModel = ds.getValue(Comment.class);
                        commentList.add(commentModel);

                    }
                    commentsAdapter.notifyDataSetChanged();
                    getCommentCount(materialID, mdCommentCounter);

                }else{
                    Toast.makeText(CommentActivity.this, "No comments", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CommentActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserDetails(String userID) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Students");
        Query query = reference.orderByChild("userId").equalTo(userID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        User user = ds.getValue(User.class);
                        assert user != null;
                        String name = user.getName();
                        String proPic = user.getProfileImg();

                        try{
                            Picasso.get().load(proPic).placeholder(R.drawable.person_img).into(comProPic);
                        }catch (NullPointerException ignored){
                            Picasso.get().load(R.drawable.person_img).into(comProPic);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });// for  updating my details in the pop up
    }
}