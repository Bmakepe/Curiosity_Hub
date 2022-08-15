package com.makepe.curiosityhubls.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makepe.curiosityhubls.GroupChatActivity;
import com.makepe.curiosityhubls.GroupDetailsActivity;
import com.makepe.curiosityhubls.Models.GroupChat;
import com.makepe.curiosityhubls.Models.Groups;
import com.makepe.curiosityhubls.Models.User;
import com.makepe.curiosityhubls.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    Context context;
    List<Groups> groupsList;

    public GroupAdapter(Context context, List<Groups> groupsList) {
        this.context = context;
        this.groupsList = groupsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_chatlist_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Groups group = groupsList.get(position);

        getLastMessage(group, holder);

        holder.contactName.setText(group.getGroupName());
        iniPopUpProPic(context, holder, group);
        iniFullScreenProPicDialog(context, holder, group);

        try{
            Picasso.get().load(group.getGroupIcon()).into(holder.groupProPic);
        }catch (NullPointerException e){
            Picasso.get().load(R.drawable.person_img).into(holder.groupProPic);
        }

        holder.groupProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.popAddPost.show();
            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupID", group.getGroupID());
                context.startActivity(intent);
            }
        });
    }

    private void getLastMessage(Groups group, ViewHolder holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("GroupChat");
        ref.child(group.getGroupID()).child("message").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            GroupChat groups = ds.getValue(GroupChat.class);

                            try{//convert timestamp to dd/MM/yyyy hh:mm am/pm & set it to textview
                                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                calendar.setTimeInMillis(Long.parseLong(groups.getTimeStamp()));
                                String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                                holder.timeStampTV.setVisibility(View.VISIBLE);
                                holder.timeStampTV.setText(pTime);
                            }catch (NumberFormatException n) {
                                Toast.makeText(context, "Could not format time", Toast.LENGTH_SHORT).show();
                            }

                            holder.groupMessage.setText(groups.getMessage());

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Students");
                            reference.orderByChild("userId").equalTo(groups.getSenderID())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for(DataSnapshot ds : snapshot.getChildren()){
                                                User user = ds.getValue(User.class);

                                                holder.senderName.setVisibility(View.VISIBLE);
                                                holder.senderName.setText(user.getName());

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void iniFullScreenProPicDialog(Context context, ViewHolder holder, Groups group) {
        holder.fullScreenDialog = new Dialog(context);
        holder.fullScreenDialog.setContentView(R.layout.fullscreen_image_view);
        holder.fullScreenDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        holder.fullScreenDialog.getWindow().setLayout(androidx.appcompat.widget.Toolbar.LayoutParams.MATCH_PARENT, androidx.appcompat.widget.Toolbar.LayoutParams.MATCH_PARENT);
        holder.fullScreenDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        ImageView fullScreenPic = holder.fullScreenDialog.findViewById(R.id.fullScreenPic);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups").child(group.getGroupID());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Groups user = dataSnapshot.getValue(Groups.class);

                try{
                    Picasso.get().load(user.getGroupIcon()).placeholder(R.drawable.person_img).into(fullScreenPic);
                }catch (Exception e){
                    Picasso.get().load(R.drawable.person_img).into(fullScreenPic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.fullScreenDialog.findViewById(R.id.fullScreenBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.fullScreenDialog.dismiss();
            }
        });
    }

    private void iniPopUpProPic(Context context, ViewHolder holder, Groups group) {
        holder.popAddPost = new Dialog(context);
        holder.popAddPost.setContentView(R.layout.profile_pic_pop_up_layout);
        holder.popAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        holder.popAddPost.getWindow().setLayout(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        holder.popAddPost.getWindow().getAttributes().gravity = Gravity.CENTER;

        ImageView viewProfile = holder.popAddPost.findViewById(R.id.popUP_ViewProfile);
        ImageView sendMessage = holder.popAddPost.findViewById(R.id.popUP_SendMessage);
        final ImageView superProPic = holder.popAddPost.findViewById(R.id.popUP_ProPic);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups").child(group.getGroupID());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Groups user = dataSnapshot.getValue(Groups.class);

                try{
                    Picasso.get().load(user.getGroupIcon()).placeholder(R.drawable.person_img).into(superProPic);
                }catch (Exception e){
                    Picasso.get().load(R.drawable.person_img).into(superProPic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupID", group.getGroupID());
                context.startActivity(intent);
            }
        });

        viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, GroupDetailsActivity.class);
                intent.putExtra("groupID", group.getGroupID());
                context.startActivity(intent);
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
        return groupsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView contactName, groupMessage, timeStampTV, senderName;
        CircleImageView groupProPic;
        Dialog popAddPost, fullScreenDialog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.groupChatlistName);
            groupMessage = itemView.findViewById(R.id.groupchatlistMessage);
            groupProPic = itemView.findViewById(R.id.groupChatListPropic);
            timeStampTV = itemView.findViewById(R.id.groupLastTimastamp);
            senderName = itemView.findViewById(R.id.groupLastSender);
        }
    }
}
