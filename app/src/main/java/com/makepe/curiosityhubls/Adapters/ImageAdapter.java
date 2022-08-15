package com.makepe.curiosityhubls.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makepe.curiosityhubls.Models.PostModel;
import com.makepe.curiosityhubls.PostDetailsActivity;
import com.makepe.curiosityhubls.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private Context context;
    private List<PostModel> mPosts;

    private FirebaseUser firebaseUser;
    String pid, myUID;

    public ImageAdapter(Context context, List<PostModel> mPosts) {
        this.context = context;
        this.mPosts = mPosts;
    }

    public ImageAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_item_view, parent, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final PostModel modelPost = mPosts.get(position);
        pid = mPosts.get(position).getpId();
        final String uid = modelPost.getUid();

        isSaved(modelPost.getpId(), holder.savedBTN);

        Glide.with(context)
                .load(modelPost.getPostImage())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .error(R.drawable.person_img)
                .into(holder.post_image);

        if(!uid.equals(firebaseUser.getUid())){
            holder.savedBTN.setVisibility(View.VISIBLE);
        }

        holder.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addView(modelPost.getpId());

                Intent intent = new Intent(context, PostDetailsActivity.class);
                intent.putExtra("postID", modelPost.getpId());

                context.startActivity(intent);

            }
        });

        holder.savedBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(holder.savedBTN.getTag().equals("save")){
                        FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                                .child(modelPost.getpId()).setValue(true);
                    }else{
                        FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                                .child(modelPost.getpId()).removeValue();
                    }
                }catch (NullPointerException e)
                {}
            }
        });
    }


    private void addView(String getpId) {
        FirebaseDatabase.getInstance().getReference("PostViews")
                .child(getpId).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(true);
    }

    private void isSaved(String postid, ImageView imageView) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid());
        reference.keepSynced(true);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postid).exists()){
                    imageView.setImageResource(R.drawable.ic_baseline_bookmark_24);
                    imageView.setTag("saved");
                }else{
                    imageView.setImageResource(R.drawable.ic_baseline_bookmark_border_24);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView post_image, savedBTN;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            post_image = itemView.findViewById(R.id.post_image);
            savedBTN = itemView.findViewById(R.id.mediaSaveBTN);
        }
    }
}
