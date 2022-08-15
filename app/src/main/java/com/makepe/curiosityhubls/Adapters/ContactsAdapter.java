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

import com.makepe.curiosityhubls.Models.ContactsModel;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    List<ContactsModel> contactList;
    Context context;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    User user;

    String fireName, fireNumber, fireID, fireProPic;

    public ContactsAdapter(List<ContactsModel> contactList, Context context) {
        this.contactList = contactList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_display_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final String name = contactList.get(position).getName();
        final String number = contactList.get(position).getPhoneNumber();
        final String userid = contactList.get(position).getUserId();

        holder.contactName.setText(name);
        holder.contactNumber.setText(number);
        holder.inviteBTN.setVisibility(View.VISIBLE);
        holder.inviteBTN.setText("INVITE");

        getAllUserList(contactList, holder);

        if(number.equals(fireNumber)){

            isFollowing(fireID, holder.inviteBTN);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        if(fireNumber.equals(number)){
                            if(fireID.equals(firebaseUser.getUid())) {
                                Intent intent1 = new Intent(context, ProfileActivity.class);
                                context.startActivity(intent1);
                            }else{
                                Intent intent = new Intent(context, ViewProfileActivity.class);
                                intent.putExtra("uid", fireID);
                                context.startActivity(intent);
                            }
                        }
                    }catch(NullPointerException ignored){}

                }
            });
        }

        if(!number.equals(fireNumber)){
            holder.inviteBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, name + " has been invited to join", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            holder.inviteBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(holder.inviteBTN.getText().equals("Follow")){
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
            });
        }
    }

    private void addNotifications(String userId) {
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

    private void isFollowing(String fireID, TextView inviteBTN) {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    if(dataSnapshot.child(fireID).exists()){

                        inviteBTN.setText("Following");

                    }else{

                        inviteBTN.setText("Follow");

                    }
                }catch (NullPointerException e){
                    Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAllUserList(List<ContactsModel> contactsList, ViewHolder holder) {

        reference = FirebaseDatabase.getInstance().getReference().child("Students");
        reference.keepSynced(true);
        for(int i = 0; i < contactsList.size(); i++){
            checkingMatchedData(contactsList.get(i).getPhoneNumber(), holder);
        }

    }

    private void checkingMatchedData(final String number, final ViewHolder holder) {
        try{
            Query query = reference.orderByChild("number").equalTo(holder.contactNumber.getText().toString());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        if(number.equals(holder.contactNumber.getText().toString())){

                            holder.inviteBTN.setText("Exists");

                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                User user1 = ds.getValue(User.class);
                                assert user1 != null;
                                fireID = user1.getUserId();
                                fireName = user1.getName();
                                fireNumber = user1.getPhoneNumber();
                                fireProPic = user1.getProfileImg();

                                try{
                                    Picasso.get().load(fireProPic).into(holder.contactProPic);
                                }catch (NullPointerException e){
                                    Picasso.get().load(R.drawable.person_img).into(holder.contactProPic);
                                }
                            }

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }catch (NumberFormatException ignored){

        }

    }

    @Override
    public int getItemCount() {
        if(contactList == null){
            return 0;
        }
        return contactList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView contactName, contactNumber, inviteBTN;
        CircleImageView contactProPic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.contactName);
            contactNumber = itemView.findViewById(R.id.personalNumber);
            contactProPic = itemView.findViewById(R.id.contactProPic);
            inviteBTN = itemView.findViewById(R.id.statusTV);
        }
    }
}
