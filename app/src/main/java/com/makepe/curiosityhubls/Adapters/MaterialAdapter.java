package com.makepe.curiosityhubls.Adapters;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.makepe.curiosityhubls.CommentActivity;
import com.makepe.curiosityhubls.Models.Material;
import com.makepe.curiosityhubls.Models.User;
import com.makepe.curiosityhubls.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.ViewHolder> {

    Context context;
    List<Material> materialList;

    String name, type, subject, grade, document, uniqueKey, year;

    String uid;

    FirebaseUser user;

    public MaterialAdapter(Context context, List<Material> materialList) {
        this.context = context;
        this.materialList = materialList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.material_item_layout, parent, false);
        return new MaterialAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();
        Material material = materialList.get(position);

        getMaterial(material, holder);
        iniLikesDialog(material.getMaterial_ID(), holder);

        seenNumber(material.getMaterial_ID(), holder.views);

        try{
            isLiked(material.getMaterial_ID(), holder.likeBTN);
        }catch (NullPointerException ignored){}
        try{
            nrLikes(holder.likeCounter, material.getMaterial_ID());
        }catch (NullPointerException e){}
        try{
            getCommentsCount(material.getMaterial_ID(), holder.commentCounter);
        }catch (NullPointerException y){}

        saveMaterial(material.getMaterial_ID(), holder.saveBTN);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addView(material.getMaterial_ID());

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(document), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Intent intent1 = Intent.createChooser(intent, "Open File");

                try{
                    context.startActivity(intent1);
                }catch (ActivityNotFoundException e){

                }
            }
        });

        holder.likesArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.likeBTN.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(material.getMaterial_ID())
                            .child(firebaseUser.getUid()).setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(material.getMaterial_ID())
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.saveBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.saveBTN.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(material.getMaterial_ID()).setValue(true);
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(material.getMaterial_ID()).removeValue();
                }
            }
        });

        holder.shareBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Sharing this post", Toast.LENGTH_SHORT).show();
            }
        });

        holder.commentArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addView(material.getMaterial_ID());

                Intent intent = new Intent(context, CommentActivity.class);

                intent.putExtra("materialID", material.getMaterial_ID());
                intent.putExtra("userID", uid);
                intent.putExtra("materialName", material.getName());
                intent.putExtra("type", material.getType());
                intent.putExtra("subject", material.getSubject());
                intent.putExtra("grade", material.getGrade());
                intent.putExtra("document", material.getDocument());
                intent.putExtra("year", material.getYear());

                context.startActivity(intent);
            }
        });

        holder.likeCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.likesDialog.show();
            }
        });
    }

    private void iniLikesDialog(String material_id, ViewHolder holder) {
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
        reference.child(material_id).addValueEventListener(new ValueEventListener() {
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
        FirebaseDatabase.getInstance().getReference("MaterialViews")
                .child(getpId).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(true);
    }

    private void seenNumber(String getpId, TextView views) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("MaterialViews")
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

    private void isLiked(String material_id, ImageView likeBTN) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(material_id);

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

    private void nrLikes(final TextView likes, String Material_ID){
        DatabaseReference reference =  FirebaseDatabase.getInstance().getReference().child("Likes")
                .child(Material_ID);
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

    private void saveMaterial(final String materialID, final ImageView imageView){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(materialID).exists()){
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

    private void getCommentsCount(String materialID, final TextView commentCountTV) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(materialID);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 1)
                    commentCountTV.setText("View " + dataSnapshot.getChildrenCount() + " comment");
                else
                    commentCountTV.setText("View all " + dataSnapshot.getChildrenCount() +" comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMaterial(Material material, ViewHolder holder) {

        name = material.getName();
        type = material.getType();
        year = material.getYear();
        subject = material.getSubject();
        grade = material.getGrade();
        document = material.getDocument();
        uniqueKey = material.getMaterial_ID();

        holder.materialName.setText(name);
        holder.materialType.setText(type);
        holder.materialSubject.setText(subject);
        holder.materialGrade.setText(grade);
        holder.materialYear.setText(year);

    }

    @Override
    public int getItemCount() {
        return materialList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView materialName, views , materialType, materialYear, materialSubject, materialGrade, likeCounter, commentCounter;
        ImageView likeBTN, saveBTN, shareBTN;

        LinearLayout linearLayout;
        RelativeLayout likesArea, commentArea;

        Dialog likesDialog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            materialName = itemView.findViewById(R.id.materialName);
            materialType = itemView.findViewById(R.id.materialType);
            materialYear = itemView.findViewById(R.id.materialYear);
            materialSubject = itemView.findViewById(R.id.materialSubject);
            materialGrade = itemView.findViewById(R.id.materialGrade);
            shareBTN = itemView.findViewById(R.id.shareBTN);

            linearLayout = itemView.findViewById(R.id.materialDetails);
            likesArea = itemView.findViewById(R.id.likesLayout);
            commentArea = itemView.findViewById(R.id.commentLayout);

            likeBTN = itemView.findViewById(R.id.postLikeBTN);
            likeCounter = itemView.findViewById(R.id.likeCounter);
            commentCounter = itemView.findViewById(R.id.commentCounter);
            saveBTN = itemView.findViewById(R.id.saveBTN);

            views = itemView.findViewById(R.id.mViewsCounter);
        }
    }

}
