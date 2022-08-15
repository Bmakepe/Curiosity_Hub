package com.makepe.curiosityhubls.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makepe.curiosityhubls.Models.User;
import com.makepe.curiosityhubls.ProfileActivity;
import com.makepe.curiosityhubls.R;
import com.makepe.curiosityhubls.ViewProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    FirebaseUser firebaseUser;

    Context context;
    List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String name = user.getName();
        String propic = user.getProfileImg();
        String userid = user.getUserId();

        holder.userName.setText(name);
        isFollowing(userid, holder.followBTN);

        try{
            Picasso.get().load(propic).into(holder.userProPic);
        }catch (Exception e){
            Picasso.get().load(R.drawable.person_img).into(holder.userProPic);
        }

        if(user.getUserId().equals(firebaseUser.getUid())){
            holder.followBTN.setVisibility(View.GONE);
        }else if(!user.getRole().equals("Student")){
            holder.followBTN.setVisibility(View.GONE);
            holder.roleTV.setVisibility(View.VISIBLE);
            holder.roleTV.setText(user.getRole());
        }else{
            holder.followBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(user.getUserId().equals(firebaseUser.getUid())){
                        Toast.makeText(context, "Can not follow yourself", Toast.LENGTH_SHORT).show();
                        Intent intent1 = new Intent(context, ProfileActivity.class);
                        context.startActivity(intent1);
                    }else{
                        if(holder.followBTN.getText().equals("Follow")){
                            FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                                    .child("following").child(user.getUserId()).setValue(true);
                            FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUserId())
                                    .child("followers").child(firebaseUser.getUid()).setValue(true);

                            addNotifications(user.getUserId());
                        }else{
                            FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                                    .child("following").child(user.getUserId()).removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUserId())
                                    .child("followers").child(firebaseUser.getUid()).removeValue();
                        }
                    }
                }
            });
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(userid.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                    Intent intent1 = new Intent(context, ProfileActivity.class);
                    context.startActivity(intent1);
                }else{
                    Intent intent = new Intent(context, ViewProfileActivity.class);
                    intent.putExtra("uid", userid);
                    context.startActivity(intent);
                }
            }
        });
    }

    private void addNotifications(String userId) {
        if(firebaseUser.getUid().equals(userId)){

        }else{
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userId);
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
    }

    private void isFollowing(String userid, TextView inviteBTN) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(userid).exists()){

                    inviteBTN.setText("Following");

                }else{

                    inviteBTN.setText("Follow");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void clearAll() {
        userList.clear();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView userProPic;
        TextView userName, followBTN, roleTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userProPic = itemView.findViewById(R.id.contactProPic);
            userName = itemView.findViewById(R.id.contactName);
            followBTN = itemView.findViewById(R.id.followBTN);
            roleTV = itemView.findViewById(R.id.roleTV);
        }
    }
}
