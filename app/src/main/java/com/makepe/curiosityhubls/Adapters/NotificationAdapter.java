package com.makepe.curiosityhubls.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makepe.curiosityhubls.CustomClasses.ContactsList;
import com.makepe.curiosityhubls.CustomClasses.GetTimeAgo;
import com.makepe.curiosityhubls.Models.ContactsModel;
import com.makepe.curiosityhubls.Models.NotiModel;
import com.makepe.curiosityhubls.Models.PostModel;
import com.makepe.curiosityhubls.Models.User;
import com.makepe.curiosityhubls.PostDetailsActivity;
import com.makepe.curiosityhubls.R;
import com.makepe.curiosityhubls.ViewProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context mContext;
    private List<NotiModel> mNotifications;

    GetTimeAgo getTimeAgo;

    public NotificationAdapter(Context mContext, List<NotiModel> mNotifications) {
        this.mContext = mContext;
        this.mNotifications = mNotifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final NotiModel notification = mNotifications.get(position);
        getTimeAgo = new GetTimeAgo();
        final String pTimeStamp = mNotifications.get(position).getTimeStamp();

        holder.text.setText(notification.getText());

        getUserinfo(holder.image_profile, holder.username, notification.getUserid());

        String PostTime = getTimeAgo.getTimeAgo(Long.parseLong(pTimeStamp), mContext);
        holder.timeStamp.setText(" - " + PostTime);

        if(notification.isIspost()){
            holder.post_image.setVisibility(View.VISIBLE);
            getPostImage(holder.post_image, notification.getPostid());
        }else{
            holder.post_image.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(notification.isIspost()){
                    Intent intent = new Intent(mContext, PostDetailsActivity.class);
                    intent.putExtra("postID", notification.getPostid());

                    mContext.startActivity(intent);
                }else if(notification.isStory()){
                    Toast.makeText(mContext, "Story Notification", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent(mContext, ViewProfileActivity.class);
                    intent.putExtra("uid", notification.getUserid());
                    mContext.startActivity(intent);
                }
            }
        });
    }

    private void getPostImage(ImageView post_image, String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                PostModel post = dataSnapshot.getValue(PostModel.class);
                try{
                    Picasso.get().load(post.getPostImage()).into(post_image);
                }catch (NullPointerException ignore){}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserinfo(ImageView image_profile, TextView username, String uid) {
        List<ContactsModel> phoneBook = new ArrayList<>();
        ContactsList contactsList = new ContactsList(phoneBook, mContext);
        contactsList.readContacts();
        final List<ContactsModel> phoneContacts = contactsList.getContactsList();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Students");
        Query query = reference.orderByChild("userId").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        User user = ds.getValue(User.class);

                        String proPic = user.getProfileImg();
                        String username1 = user.getName();
                        String number = user.getPhoneNumber();

                        try {
                            Picasso.get().load(proPic).into(image_profile);
                        } catch (NullPointerException ignored) {}

                        //username.setText(username1);

                        for(ContactsModel contactsModel : phoneContacts){
                            if(uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                username.setText("Me");
                            }else if(contactsModel.getPhoneNumber().equals(number)){
                                username.setText(contactsModel.getName());
                            }else{
                                username.setText(username1);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image_profile, post_image;
        public TextView username, text, timeStamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.notiProIMG);
            post_image = itemView.findViewById(R.id.post_image);
            username = itemView.findViewById(R.id.notiUsername);
            text = itemView.findViewById(R.id.notiComment);
            timeStamp = itemView.findViewById(R.id.notiTimeStamp);

        }
    }
}
