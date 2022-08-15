package com.makepe.curiosityhubls.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.makepe.curiosityhubls.CustomClasses.ContactsList;
import com.makepe.curiosityhubls.CustomClasses.GetTimeAgo;
import com.makepe.curiosityhubls.MessageActivity;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatlistAdapter extends RecyclerView.Adapter<ChatlistAdapter.ViewHolder> {
    Context context;
    List<User> chatList;
    private HashMap<String, String> lastMessageMap;
    private HashMap<String, String> lastTimeStampMap;

    GetTimeAgo getTimeAgo;

    public ChatlistAdapter(Context context, List<User> chatList) {
        this.context = context;
        this.chatList = chatList;
        lastMessageMap = new HashMap<>();
        lastTimeStampMap = new HashMap<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.raw_user_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //get data;
        User user = chatList.get(position);
        getTimeAgo = new GetTimeAgo();

        final String hisUid = user.getUserId();
        String lastmessage = lastMessageMap.get(hisUid);
        String lastTime = lastTimeStampMap.get(hisUid);

        getUserDetails(hisUid, holder);
        iniPopUpProPic(context, holder, user.getUserId());
        iniFullScreenProPicDialog(context, holder, user.getUserId());

        //set Data

        if(lastmessage == null || lastmessage.equals("default")){
            holder.chatlistMessage.setVisibility(View.GONE);
        }else{
            holder.chatlistMessage.setVisibility(View.VISIBLE);
            holder.chatlistMessage.setText(lastmessage);

            String postTime = getTimeAgo.getTimeAgo(Long.parseLong(lastTime), context);
            holder.timestamp.setText("- " + postTime);

        }

        holder.chatlistProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.popAddPost.show();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start chat activity with that user
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("receiverID", hisUid);
                context.startActivity(intent);
            }
        });
    }

    private void getUserDetails(String hisUid, ViewHolder holder) {

        List<ContactsModel> phoneBook = new ArrayList<>();
        ContactsList contactsList = new ContactsList(phoneBook, context);
        contactsList.readContacts();
        final List<ContactsModel> phoneContacts = contactsList.getContactsList();

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Students");
        reference.keepSynced(true);
        Query query = reference.orderByChild("userId").equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot ds : dataSnapshot.getChildren()){

                        User user = ds.getValue(User.class);

                        String username = user.getName();
                        String proPic = user.getProfileImg();
                        String number = user.getPhoneNumber();

                        for(ContactsModel contactsModel : phoneContacts){
                            if(contactsModel.getPhoneNumber().equals(number)){

                                String hisPhoneName = contactsModel.getName();
                                holder.chatListUser.setText(hisPhoneName);
                            }else{
                                holder.chatListUser.setText(username);
                            }
                        }

                        try{
                            Picasso.get().load(proPic).into(holder.chatlistProPic);
                        }catch (NullPointerException e){
                            Picasso.get().load(R.drawable.person_img).into(holder.chatlistProPic);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setLastMessageMap(String userid, String lastMessage){
        lastMessageMap.put(userid, lastMessage);
    }

    public void setLastTimeStamp(String userId, String lastTimeStamp) {

        lastTimeStampMap.put(userId, lastTimeStamp);
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

    @Override
    public int getItemCount() {
        return chatList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView chatlistProPic;// chatlistCover;
        TextView chatListUser, chatlistMessage, typingTV, timestamp;// unreadTexts;
        Dialog popAddPost, fullScreenDialog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            chatlistProPic = itemView.findViewById(R.id.chatListPropic);;
            chatListUser = itemView.findViewById(R.id.chatlistUsername);
            chatlistMessage = itemView.findViewById(R.id.chatlistMessage);
            typingTV = itemView.findViewById(R.id.typingTV);
            timestamp = itemView.findViewById(R.id.cl_message_timeStamp);

        }
    }
}
