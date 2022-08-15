package com.makepe.curiosityhubls.Adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makepe.curiosityhubls.CustomClasses.ContactsList;
import com.makepe.curiosityhubls.MessageActivity;
import com.makepe.curiosityhubls.Models.ContactsModel;
import com.makepe.curiosityhubls.Models.User;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupContactsAdapter extends RecyclerView.Adapter<GroupContactsAdapter.ViewHolder> {

    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    private String fireProPic, userid, number, name, proImg;
    String groupId, myGroupRole;

    private Context context;
    private List<ContactsModel> firebaseContacts;

    public GroupContactsAdapter() {
    }

    public GroupContactsAdapter(String groupId, String myGroupRole, Context context, List<ContactsModel> firebaseContacts) {
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
        this.context = context;
        this.firebaseContacts = firebaseContacts;
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
        final ContactsModel user = firebaseContacts.get(position);

        userid = user.getUserId();
        number = user.getPhoneNumber();
        name = user.getName();
        proImg = user.getProfileImg();

        getContactDetails(holder, user);
        iniPopUpProPic(context, holder, user.getUserId());
        iniFullScreenProPicDialog(context, holder, user.getUserId());

        checkIfAlreadyExists(user, holder);

        holder.contactProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.popAddPost.show();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupId).child("Participants").child(user.getUserId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){//user Exists/ is participant

                                    String hisPreviousRole = "" + snapshot.child("role").getValue();

                                    String[] options;

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Choose Option");

                                    if(myGroupRole.equals("creator")){
                                        if(hisPreviousRole.equals("admin")){
                                            options = new String[]{"Remove Admin", "Remove User", "View Profile"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int which) {
                                                    if(which == 0){
                                                        //remove admin clicked
                                                        removeAdmin(user);

                                                    }else if(which == 1){
                                                        //remove user clicked
                                                        removeParticipant(user);

                                                    }else{
                                                        if(userid.equals(firebaseUser.getUid())){

                                                            Intent intent = new Intent(context, ProfileActivity.class);
                                                            context.startActivity(intent);
                                                            holder.popAddPost.dismiss();

                                                        }else{
                                                            Intent intent = new Intent(context, ViewProfileActivity.class);
                                                            intent.putExtra("uid", userid);
                                                            context.startActivity(intent);
                                                            holder.popAddPost.dismiss();
                                                        }
                                                    }

                                                }
                                            }).show();
                                        }
                                        else if(hisPreviousRole.equals("participant")){
                                            //normal participant
                                            options = new String[]{"Make Admin", "Remove User", "View Profile"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int which) {
                                                    if(which == 0){
                                                        //remove admin clicked
                                                        makeAdmin(user);

                                                    }else if(which == 1){
                                                        //remove user clicked
                                                        removeParticipant(user);

                                                    }else{
                                                        if(userid.equals(firebaseUser.getUid())){

                                                            Intent intent = new Intent(context, ProfileActivity.class);
                                                            context.startActivity(intent);
                                                            holder.popAddPost.dismiss();

                                                        }else{
                                                            Intent intent = new Intent(context, ViewProfileActivity.class);
                                                            intent.putExtra("uid", userid);
                                                            context.startActivity(intent);
                                                            holder.popAddPost.dismiss();
                                                        }
                                                    }

                                                }
                                            }).show();
                                        }
                                    }
                                    else if(myGroupRole.equals("admin")){
                                        if(hisPreviousRole.equals("creator")){
                                            if(userid.equals(firebaseUser.getUid())){

                                                Intent intent = new Intent(context, ProfileActivity.class);
                                                context.startActivity(intent);
                                                holder.popAddPost.dismiss();

                                            }else{
                                                Intent intent = new Intent(context, ViewProfileActivity.class);
                                                intent.putExtra("uid", user.getUserId());
                                                context.startActivity(intent);
                                                holder.popAddPost.dismiss();
                                            }
                                        }
                                        else if(hisPreviousRole.equals("admin")){
                                            options = new String[]{"Remove Admin", "Remove User", "View Profile"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int which) {
                                                    if(which == 0){
                                                        //remove admin clicked
                                                        removeAdmin(user);

                                                    }else if(which == 1){
                                                        //remove user clicked
                                                        removeParticipant(user);

                                                    }else{
                                                        if(userid.equals(firebaseUser.getUid())){

                                                            Intent intent = new Intent(context, ProfileActivity.class);
                                                            context.startActivity(intent);
                                                            holder.popAddPost.dismiss();

                                                        }else{
                                                            Intent intent = new Intent(context, ViewProfileActivity.class);
                                                            intent.putExtra("uid", user.getUserId());
                                                            context.startActivity(intent);
                                                            holder.popAddPost.dismiss();
                                                        }
                                                    }

                                                }
                                            }).show();
                                        }
                                        else if(hisPreviousRole.equals("participant")){
                                            options = new String[]{"Make Admin", "Remove User", "View Profile"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int which) {
                                                    if(which == 0){
                                                        //remove admin clicked
                                                        makeAdmin(user);

                                                    }else if(which == 1){
                                                        //remove user clicked
                                                        removeParticipant(user);

                                                    }else{
                                                        if(userid.equals(firebaseUser.getUid())){

                                                            Intent intent = new Intent(context, ProfileActivity.class);
                                                            context.startActivity(intent);
                                                            holder.popAddPost.dismiss();

                                                        }else{
                                                            Intent intent = new Intent(context, ViewProfileActivity.class);
                                                            intent.putExtra("uid", user.getUserId());
                                                            context.startActivity(intent);
                                                            holder.popAddPost.dismiss();
                                                        }
                                                    }

                                                }
                                            }).show();
                                        }
                                    }else if(myGroupRole.equals("participant")){
                                        if(userid.equals(firebaseUser.getUid())){

                                            Intent intent = new Intent(context, ProfileActivity.class);
                                            context.startActivity(intent);
                                            holder.popAddPost.dismiss();

                                        }else{
                                            Intent intent = new Intent(context, ViewProfileActivity.class);
                                            intent.putExtra("uid", user.getUserId());
                                            context.startActivity(intent);
                                            holder.popAddPost.dismiss();
                                        }
                                    }

                                }else{//user doesn't exist/not participant : add
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Add Participant")
                                            .setMessage("Add this user in this group")
                                            .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    addParticipant(user);
                                                }
                                            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });

    }

    private void getContactDetails(ViewHolder holder, ContactsModel user) {

        holder.contactName.setText(user.getName());
        try{
            Picasso.get().load(user.getProfileImg()).placeholder(R.drawable.person_img).into(holder.contactProPic);
        }catch (NullPointerException ignored){}
    }

    private void iniFullScreenProPicDialog(Context context, ViewHolder holder, String userId) {
        holder.fullScreenDialog = new Dialog(context);
        holder.fullScreenDialog.setContentView(R.layout.fullscreen_image_view);
        holder.fullScreenDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        holder.fullScreenDialog.getWindow().setLayout(androidx.appcompat.widget.Toolbar.LayoutParams.MATCH_PARENT, androidx.appcompat.widget.Toolbar.LayoutParams.MATCH_PARENT);
        holder.fullScreenDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        ImageView fullScreenPic = holder.fullScreenDialog.findViewById(R.id.fullScreenPic);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Students").child(userId);
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

    private void iniPopUpProPic(Context context, ViewHolder holder, String userId) {
        holder.popAddPost = new Dialog(context);
        holder.popAddPost.setContentView(R.layout.profile_pic_pop_up_layout);
        holder.popAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        holder.popAddPost.getWindow().setLayout(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        holder.popAddPost.getWindow().getAttributes().gravity = Gravity.CENTER;

        ImageView viewProfile = holder.popAddPost.findViewById(R.id.popUP_ViewProfile);
        ImageView sendMessage = holder.popAddPost.findViewById(R.id.popUP_SendMessage);
        final ImageView superProPic = holder.popAddPost.findViewById(R.id.popUP_ProPic);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Students");
        Query query = reference.orderByChild("userId").equalTo(userId);
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
        if(userId.equals(user.getUid())){
            sendMessage.setVisibility(View.GONE);

        }

        viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(userId.equals(user.getUid())){

                    Intent intent = new Intent(context, ProfileActivity.class);
                    context.startActivity(intent);
                    holder.popAddPost.dismiss();

                }else{
                    Intent intent = new Intent(context, ViewProfileActivity.class);
                    intent.putExtra("uid", userId);
                    context.startActivity(intent);
                    holder.popAddPost.dismiss();
                }

            }
        });

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent1 = new Intent(context, MessageActivity.class);
                intent1.putExtra("receiverID", userId);
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

    private void checkIfAlreadyExists(ContactsModel user, ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Participants").child(user.getUserId());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            //already exists
                            String hisRole = "" + snapshot.child("role").getValue();
                            holder.statusTV.setVisibility(View.VISIBLE);
                            holder.statusTV.setText(hisRole);
                        }else{
                            //doesn't exist
                            holder.statusTV.setVisibility(View.VISIBLE);
                            holder.statusTV.setText("ADD MEMBER");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void addParticipant(ContactsModel user) {
        //setup user data
        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", user.getUserId());
        hashMap.put("role", "participant");
        hashMap.put("timestamp", ""+timestamp);
        hashMap.put("groupID", groupId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(user.getUserId()).setValue(hashMap)
        //ref.push().setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Added Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeAdmin(ContactsModel user) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "admin");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(user.getUserId()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, user.getName() + " is now an admin", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeParticipant(ContactsModel user) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(user.getUserId()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, user.getName() + " has been removed from the group", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeAdmin(ContactsModel user) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "participant");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(user.getUserId()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, user.getName() + " admin rights revoked", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        if(firebaseContacts == null){
            return 0;
        }
        return firebaseContacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView contactName, contactNumber, statusTV;
        CircleImageView contactProPic;
        Dialog popAddPost, fullScreenDialog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.contactName);
            contactNumber = itemView.findViewById(R.id.personalNumber);
            contactProPic = itemView.findViewById(R.id.contactProPic);
            statusTV = itemView.findViewById(R.id.statusTV);
        }
    }
}
