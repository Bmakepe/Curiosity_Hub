package com.makepe.curiosityhubls.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.makepe.curiosityhubls.CustomClasses.GetTimeAgo;
import com.makepe.curiosityhubls.Models.GroupChat;
import com.makepe.curiosityhubls.Models.User;
import com.makepe.curiosityhubls.PostDetailsActivity;
import com.makepe.curiosityhubls.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.ViewHolder>{

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_AUDIO_LEFT = 2;
    public static final int MSG_TYPE_AUDIO_RIGHT = 3;
    public static final int MSG_TYPE_IMAGE_RIGHT = 4;
    public static final int MSG_TYPE_IMAGE_LEFT = 5;

    Context context;
    List<GroupChat> groupChats;

    FirebaseUser user;
    DatabaseReference reference;

    GetTimeAgo getTimeAgo;

    public GroupChatAdapter(Context context, List<GroupChat> groupChats) {
        this.context = context;
        this.groupChats = groupChats;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;

        switch (viewType) {
            case MSG_TYPE_RIGHT:
                view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
                return new ViewHolder(view);

            case MSG_TYPE_LEFT:
                view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
                return new ViewHolder(view);

            case MSG_TYPE_AUDIO_LEFT:
                view = LayoutInflater.from(context).inflate(R.layout.msg_item_audio_left, parent, false);
                return new ViewHolder(view);

            case MSG_TYPE_AUDIO_RIGHT:
                view = LayoutInflater.from(context).inflate(R.layout.msg_item_audio_right, parent, false);
                return new ViewHolder(view);

            case MSG_TYPE_IMAGE_RIGHT:
                view = LayoutInflater.from(context).inflate(R.layout.msg_item_media_right, parent, false);
                return new ViewHolder(view);

            case MSG_TYPE_IMAGE_LEFT:
                view = LayoutInflater.from(context).inflate(R.layout.msg_item_media_left, parent, false);
                return new ViewHolder(view);

            default:
                throw new IllegalStateException("Unexpected value: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        reference = FirebaseDatabase.getInstance().getReference("Students");
        getTimeAgo = new GetTimeAgo();
        GroupChat chat = groupChats.get(position);

        final String senderID = chat.getSenderID();

        getGroupChats(chat, holder);
        getUserInfo(holder, senderID);

        holder.itemView.setOnClickListener(v -> showChatOptions(holder.itemView, senderID, position));
    }

    private void showChatOptions(View itemView, String senderID, int position) {
        final PopupMenu popupMenu = new PopupMenu(context, itemView, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0,0,"Reply");
        popupMenu.getMenu().add(Menu.NONE, 1,0,"Copy");
        popupMenu.getMenu().add(Menu.NONE, 2,0,"Forward");
        if(senderID.equals(user.getUid())){
            popupMenu.getMenu().add(Menu.NONE, 3,0,"Delete");
        }


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                switch (id){
                    case 0:
                        Toast.makeText(context, "Reply Chat", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(context, "Copy Chat", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(context, "Forward Chat", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(context, "Delete Chat", Toast.LENGTH_SHORT).show();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Delete");
                        builder.setMessage("Are you sure to delete this message");

                        //delete btn
                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //deleteMessage(position);
                            }
                        });
                        //cancel delete btn
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss dialog
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + id);
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void getUserInfo(ViewHolder holder, String senderID) {
        Query query = reference.orderByChild("userId").equalTo(senderID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    holder.chatUsername.setVisibility(View.VISIBLE);
                    holder.chatUsername.setText(user.getName());

                    try{
                        Picasso.get().load(user.getProfileImg()).into(holder.chatPic);
                    }catch (NullPointerException ignored){
                        Picasso.get().load(R.drawable.person_img).into(holder.chatPic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getGroupChats(GroupChat chat, ViewHolder holder) {
        String audioType = "audio", imgType = "image";

        if (chat.getMsg_type().equals(imgType)){
            String imgUrl = chat.getMedia();
            Glide.with(context).load(imgUrl).into(holder.msgImage);
            if(!chat.getMessage().isEmpty()){
                holder.showMessages.setVisibility(View.VISIBLE);
                holder.chatUsername.setVisibility(View.VISIBLE);
                holder.showMessages.setText(chat.getMessage());

            }
            holder.msgImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }else {
            try{
                if(!chat.getMessage().isEmpty()){
                    holder.showMessages.setVisibility(View.VISIBLE);
                    holder.showMessages.setText(chat.getMessage());
                }
            }catch (NullPointerException ignored){}
        }

        String PostTime = getTimeAgo.getTimeAgo(Long.parseLong(chat.getTimeStamp()), context);
        holder.timeStamp.setText(PostTime);
    }

    @Override
    public int getItemCount() {
        return groupChats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView chatPic;
        TextView showMessages, timeStamp, chatUsername;
        ImageView ticks, msgImage;

        ImageButton playBTN;
        TextView voiceTimeText, voiceTimeDuration;
        SeekBar voiceNoteSeekbar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            chatPic = itemView.findViewById(R.id.chatProPic);
            showMessages = itemView.findViewById(R.id.showMessages);
            timeStamp = itemView.findViewById(R.id.chatTimeStamp);
            ticks = itemView.findViewById(R.id.greyTick);
            chatUsername = itemView.findViewById(R.id.chatUsername);

            playBTN = itemView.findViewById(R.id.playVoiceBTN);
            voiceTimeText = itemView.findViewById(R.id.postTimeText);
            voiceTimeDuration = itemView.findViewById(R.id.postTimeDuration);
            voiceNoteSeekbar = itemView.findViewById(R.id.voiceNoteSeekbar);
            msgImage = itemView.findViewById(R.id.chatImage);

        }
    }

    @Override
    public int getItemViewType(int position) {

        user = FirebaseAuth.getInstance().getCurrentUser();

        try{
            if(groupChats.get(position).getSenderID().equals(user.getUid())){
                if(groupChats.get(position).getMsg_type().equals("audio")){
                    return MSG_TYPE_AUDIO_RIGHT;
                }else if(groupChats.get(position).getMsg_type().equals("image")){
                    return MSG_TYPE_IMAGE_RIGHT;
                }else {
                    return MSG_TYPE_RIGHT;
                }
            }else {
                if(groupChats.get(position).getMsg_type().equals("audio")){
                    return MSG_TYPE_AUDIO_LEFT;
                }else if(groupChats.get(position).getMsg_type().equals("image")){
                    return MSG_TYPE_IMAGE_LEFT;
                }else {
                    return MSG_TYPE_LEFT;
                }
            }
        }catch (NullPointerException ignored){}

        return 0;
    }
}
