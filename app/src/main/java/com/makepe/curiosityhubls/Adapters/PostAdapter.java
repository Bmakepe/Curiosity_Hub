package com.makepe.curiosityhubls.Adapters;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.makepe.curiosityhubls.CustomClasses.GetTimeAgo;
import com.makepe.curiosityhubls.MessageActivity;
import com.makepe.curiosityhubls.Models.PostModel;
import com.makepe.curiosityhubls.Models.User;
import com.makepe.curiosityhubls.PostDetailsActivity;
import com.makepe.curiosityhubls.ProfileActivity;
import com.makepe.curiosityhubls.R;
import com.makepe.curiosityhubls.ViewProfileActivity;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.skyhope.showmoretextview.ShowMoreTextView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context context;
    private List<PostModel> postList;
    private FirebaseUser firebaseUser;

    GetTimeAgo getTimeAgo;

    public String pId, Caption, PostImage, uid, myUID, PostTime;

    private boolean isLoaderVisible = false;
    private boolean loading;

    public PostAdapter(Context context, List<PostModel> postList) {
        this.context = context;
        this.postList = postList;
        myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.raw_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final PostModel post = postList.get(position);

        pId = post.getpId();
        Caption = post.getCaption();
        PostImage = post.getPostImage();
        uid = post.getUid();

        getPostDetails(post, holder);
        iniPicPopUp(context, holder, uid);
        updateUserInfo(holder, uid);
        iniFullScreenProPicDialog(context, holder, uid);

        isLiked(post.getpId(), holder.postLikeBTN);
        iniLikesDialog(post.getpId(), holder);
        nrLikes(holder.likeCounter, post.getpId());
        isSaved(post.getpId(), holder.savePostBTN);

        holder.postUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open the profile of post owner
                if(uid.equals(firebaseUser.getUid())){
                    Intent intent1 = new Intent(context, ProfileActivity.class);
                    context.startActivity(intent1);
                }else{
                    Intent intent = new Intent(context, ViewProfileActivity.class);
                    intent.putExtra("uid", post.getUid());
                    context.startActivity(intent);

                }
            }
        });//for displaying post owner profile

        holder.postMenuBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuOptions(holder.postMenuBTN, post.getUid(), post.getpId(), post.getPostImage());
            }
        });

        holder.postProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.popAddPost.show();
            }
        });//popping up profile picture

        holder.likesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.postLikeBTN.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getpId())
                            .child(firebaseUser.getUid()).setValue(true);
                    if(!uid.equals(firebaseUser.getUid()))
                        addNotifications(uid, post.getpId());
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getpId())
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.likeCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.likesDialog.show();
            }
        });

        holder.savePostBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.savePostBTN.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getpId()).setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getpId()).removeValue();
                }
            }
        });

        holder.shareBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable)holder.postImage.getDrawable();
                if(bitmapDrawable == null){
                    //share posts without image
                    shareTextOnly(Caption);
                }else{
                    //share posts with image
                    //convert image to bitmap
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(Caption, bitmap);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addView(post.getpId());

                Intent intent = new Intent(context, PostDetailsActivity.class);
                intent.putExtra("postID", post.getpId());

                context.startActivity(intent);
            }
        });

    }

    private void iniLikesDialog(String postID, ViewHolder holder) {
        holder.likesDialog = new Dialog(context);
        holder.likesDialog.setContentView(R.layout.recyclerview_layout);
        holder.likesDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        holder.likesDialog.getWindow().setLayout(androidx.appcompat.widget.Toolbar.LayoutParams.MATCH_PARENT, androidx.appcompat.widget.Toolbar.LayoutParams.MATCH_PARENT);
        holder.likesDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        TextView dialogHeader = holder.likesDialog.findViewById(R.id.dialogHeader);
        dialogHeader.setText("People who liked this post");

        UserAdapter userAdapter;
        ArrayList<User> userList = new ArrayList<>();
        ArrayList<String> likesList = new ArrayList<>();

        RecyclerView likesRecycler = holder.likesDialog.findViewById(R.id.savedRecycler);
        likesRecycler.setHasFixedSize(true);
        likesRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        likesRecycler.setLayoutManager(layoutManager);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Likes");
        reference.child(postID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likesList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    likesList.add(ds.getKey());
                    getUsers(userList, likesList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        userAdapter = new UserAdapter(context, userList);
        likesRecycler.setAdapter(userAdapter);

        holder.likesDialog.findViewById(R.id.savedBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.likesDialog.dismiss();
            }
        });

    }

    private void getUsers(ArrayList<User> userList, ArrayList<String> likesList) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Students");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    for(String ID : likesList){
                        if(user.getUserId().equals(ID)){
                            userList.add(user);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addView(String getpId) {
        FirebaseDatabase.getInstance().getReference("PostViews")
                .child(getpId).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(true);
    }

    private void seenNumber(String getpId, TextView views) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("PostViews")
                .child(getpId);

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

    private void iniFullScreenProPicDialog(Context context, ViewHolder holder, String uid) {
        holder.fullScreenDialog = new Dialog(context);
        holder.fullScreenDialog.setContentView(R.layout.fullscreen_image_view);
        holder.fullScreenDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        holder.fullScreenDialog.getWindow().setLayout(androidx.appcompat.widget.Toolbar.LayoutParams.MATCH_PARENT, androidx.appcompat.widget.Toolbar.LayoutParams.MATCH_PARENT);
        holder.fullScreenDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        ImageView fullScreenPic = holder.fullScreenDialog.findViewById(R.id.fullScreenPic);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Students").child(uid);
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

        holder.fullScreenDialog.findViewById(R.id.fullScreenBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.fullScreenDialog.dismiss();
            }
        });
    }

    private void shareImageAndText(String caption, Bitmap bitmap) {
        //first we will save this image in cache, get the save image uri

        Uri uri = saveImageToShare(bitmap);

        //share intent
        Intent sIntent = new Intent();
        sIntent.setAction(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, caption);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");

        Intent shareIntent = Intent.createChooser(sIntent, "Share Via");
        context.startActivity(shareIntent);
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;

        try{
            imageFolder.mkdirs();//create if it doesnt exist
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "android.support.v4.content.FileProvider", file);
        }catch (Exception e){
            Toast.makeText(context, "There was an error creating URI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void shareTextOnly(String caption) {
        String shareBody = caption + "\n";

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here"); //in case you share via an email app
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(sIntent, "Share Via"));//message to show in share dialog
    }

    private void getPostDetails(PostModel post, final ViewHolder holder) {

        getTimeAgo = new GetTimeAgo();

        holder.postCaption.setText(Caption);
        String timeStamp = post.getPostTime();

        holder.postCaption.setShowingLine(2);
        holder.postCaption.addShowMoreText("Continue Reading");
        holder.postCaption.addShowLessText("Show Less");
        holder.postCaption.setShowMoreColor(R.color.identity_dark);
        holder.postCaption.setShowLessTextColor(R.color.identity);

        PostTime = getTimeAgo.getTimeAgo(Long.parseLong(timeStamp), context);
        holder.postTimeStamp.setText(PostTime);

        try{
            if(PostImage.equals("noImage")){
                holder.postImage.setVisibility(View.GONE);
                holder.postCaption.setVisibility(View.VISIBLE);
            }else{
                holder.postImage.setVisibility(View.VISIBLE);
                holder.postPicLoader.setVisibility(View.VISIBLE);
                holder.postCaption.setVisibility(View.VISIBLE);

                try{
                    Picasso.get().load(PostImage).into(holder.postImage);
                    holder.postPicLoader.setVisibility(View.GONE);
                }catch (Exception e){
                    Toast.makeText(context, "could not upload post pic", Toast.LENGTH_SHORT).show();
                }
            }
        }catch (NullPointerException ignored){}

        getComments(pId, holder.commentCounter);
        seenNumber(post.getpId(), holder.views);

    }

    private void updateUserInfo(final ViewHolder holder, final String uid) {

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Students");
        ref.keepSynced(true);
        Query query = ref.orderByChild("userId").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        User user = ds.getValue(User.class);
                        String name = user.getName();
                        String number = user.getPhoneNumber();
                        final String proPic = user.getProfileImg();

                        holder.postUsername.setText(name);

                        try{
                            Picasso.get().load(proPic).placeholder(R.drawable.person_img). into(holder.postProPic);
                            holder.proPicProgress.setVisibility(View.GONE);
                        }catch (NullPointerException ignored){
                            Picasso.get().load(R.drawable.person_img).into(holder.postProPic);

                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void iniPicPopUp(final Context context, final ViewHolder holder, final String uid) {
        holder.popAddPost = new Dialog(context);
        holder.popAddPost.setContentView(R.layout.profile_pic_pop_up_layout);
        holder.popAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        holder.popAddPost.getWindow().setLayout(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        holder.popAddPost.getWindow().getAttributes().gravity = Gravity.CENTER;

        ImageView viewProfile = holder.popAddPost.findViewById(R.id.popUP_ViewProfile);
        ImageView sendMessage = holder.popAddPost.findViewById(R.id.popUP_SendMessage);
        final ImageView superProPic = holder.popAddPost.findViewById(R.id.popUP_ProPic);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Students");
        Query query = reference.orderByChild("userId").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        User user = ds.getValue(User.class);
                        String image = user.getProfileImg();

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
        if(uid.equals(user.getUid())){
            sendMessage.setVisibility(View.GONE);

        }

        viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(uid.equals(user.getUid())){

                    Intent intent = new Intent(context, ProfileActivity.class);
                    context.startActivity(intent);
                    holder.popAddPost.dismiss();

                }else{
                    Intent intent = new Intent(context, ViewProfileActivity.class);
                    intent.putExtra("uid", uid);
                    context.startActivity(intent);
                    holder.popAddPost.dismiss();
                }

            }
        });

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent1 = new Intent(context, MessageActivity.class);
                intent1.putExtra("receiverID", uid);
                context.startActivity(intent1);
                holder.popAddPost.dismiss();
            }
        });

        superProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.fullScreenDialog.show();
                holder.popAddPost.dismiss();
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

    private void isSaved(final String postid, final ImageView imageView){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postid).exists()){
                    imageView.setImageResource(R.drawable.ic_baseline_bookmark_24);
                    imageView.setTag("saved");
                }else{
                    imageView.setImageResource(R.drawable.ic_baseline_bookmark_border_24);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getComments(String postID, final TextView commentCountTV) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postID);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 1)
                    commentCountTV.setText("View " + dataSnapshot.getChildrenCount() + " comment");
                else
                    commentCountTV.setText("View All "+ dataSnapshot.getChildrenCount() +" comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNotifications(String userid, String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "liked your post");
        hashMap.put("postid", postid);
        hashMap.put("ispost", true);
        hashMap.put("isStory", false);
        hashMap.put("timeStamp", timeStamp);

        reference.push().setValue(hashMap);
    }

    private void menuOptions(ImageView menuIcon, final String uid, final String pId, final String postImage) {
        PopupMenu popupMenu = new PopupMenu(context, menuIcon, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0,0,"View Profile");

        if(uid.equals(firebaseUser.getUid())){
            popupMenu.getMenu().add(Menu.NONE, 1,0,"Delete Post");
            popupMenu.getMenu().add(Menu.NONE, 2,0,"Edit Post");
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if(id == 0){
                    if(uid.equals(firebaseUser.getUid())){
                        Intent intent = new Intent(context, ProfileActivity.class);
                        context.startActivity(intent);
                    }else{
                        Intent intent = new Intent(context, ViewProfileActivity.class);
                        intent.putExtra("uid", uid);
                        context.startActivity(intent);

                    }
                }else if(id == 1){
                    beginDelete(pId, postImage);
                }else if(id == 2){
                    Toast.makeText(context, "Edit Post Details", Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });
        popupMenu.show();
    }

    public void beginDelete(String pId, String postImage) {
        if(postImage.equals("noImage")){
            deleteWithoutImage(pId);
        }else{
            deleteWithImage(pId, postImage);
        }
    }

    private void deleteWithImage(final String pId, String postImage) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(postImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds : dataSnapshot.getChildren()){
                                    ds.getRef().removeValue();
                                }
                                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteWithoutImage(String pId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void clear() {
        postList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //-----Views from row post.xml
        //---------------view for post owner details
        CircleImageView postProPic;
        TextView postUsername;

        //--------------views for post details
        TextView postTimeStamp;
        ShowMoreTextView postCaption;
        ImageView postImage, shareBTN;

        //--------------views for post buttons
        ImageView postLikeBTN, postMenuBTN, savePostBTN;
        TextView likeCounter, commentCounter;

        //------------other
        RelativeLayout commentLayout, likesLayout;
        Dialog popAddPost, fullScreenDialog, likesDialog;
        ProgressBar proPicProgress, postPicLoader;
        TextView views;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            postProPic = itemView.findViewById(R.id.postProPic);
            postUsername = itemView.findViewById(R.id.postUsername);

            postTimeStamp = itemView.findViewById(R.id.postTimeStamp);
            postCaption = itemView.findViewById(R.id.postCaption);

            postImage = itemView.findViewById(R.id.rPostImage);

            postLikeBTN = itemView.findViewById(R.id.postLikeBTN);
            postMenuBTN = itemView.findViewById(R.id.postMenuBTN);
            savePostBTN = itemView.findViewById(R.id.savePostBTN);
            shareBTN = itemView.findViewById(R.id.postShareBTN);

            likeCounter = itemView.findViewById(R.id.likeCounter);
            commentCounter = itemView.findViewById(R.id.commentCounter);

            commentLayout = itemView.findViewById(R.id.commentLayout);
            likesLayout = itemView.findViewById(R.id.likesLayout);
            proPicProgress = itemView.findViewById(R.id.propicloader);
            views = itemView.findViewById(R.id.viewsCounter);
            postPicLoader = itemView.findViewById(R.id.progress_load_photo);

        }
    }
}
