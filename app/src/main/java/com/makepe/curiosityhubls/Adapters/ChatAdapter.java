package com.makepe.curiosityhubls.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makepe.curiosityhubls.CustomClasses.GetTimeAgo;
import com.makepe.curiosityhubls.Models.Chat;
import com.makepe.curiosityhubls.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    Context context;
    List<Chat> chatList;
    String imageUrl;

    FirebaseUser user;

    GetTimeAgo getTimeAgo;

    public ChatAdapter(Context context, List<Chat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
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

            default:
                throw new IllegalStateException("Unexpected value: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        getTimeAgo = new GetTimeAgo();

        getChatDetails(chat, holder, position);


        holder.itemView.setOnClickListener(v -> showChatOptions(holder.itemView, chat.getSender(), position));

    }

    private void getChatDetails(Chat chat, ViewHolder holder, int position) {
        if(chat.getMsg_type().equals("image")){
            holder.imageCard.setVisibility(View.VISIBLE);
            holder.showMessages.setVisibility(View.GONE);
            Picasso.get().load(chat.getMessage()).into(holder.msgImage);
        }else{
            holder.showMessages.setText(chat.getMessage());
        }

        String PostTime = getTimeAgo.getTimeAgo(Long.parseLong(chat.getTimeStamp()), context);
        holder.timeStamp.setText(PostTime);

        try{
            Picasso.get().load(imageUrl).into(holder.chatPic);
        }catch (NullPointerException e){
            Picasso.get().load(R.drawable.person_img).into(holder.chatPic);
        }

        //setSeen
        if(position == chatList.size() - 1 ){
            if(chatList.get(position).isSeen()){
                holder.ticks.setImageResource(R.drawable.ic_baseline_remove_red_eye_24);
            }else{
                holder.ticks.setImageResource(R.drawable.ic_baseline_done_all_24);
            }
        }else{
            holder.ticks.setImageResource(R.drawable.ic_baseline_done_24);
            holder.ticks.setColorFilter(R.color.identity);
        }
    }

    private void showChatOptions(View itemView, String uid, int position) {

        final PopupMenu popupMenu = new PopupMenu(context, itemView, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0,0,"Reply");
        popupMenu.getMenu().add(Menu.NONE, 1,0,"Copy");
        popupMenu.getMenu().add(Menu.NONE, 2,0,"Forward");
        if(uid.equals(user.getUid())){
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

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Delete");
                        builder.setMessage("Are you sure to delete this message");

                        //delete btn
                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteMessage(position);
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

    private void deleteMessage(int position) {
        String msgTimeStamp = chatList.get(position).getTimeStamp();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        Query query = reference.orderByChild("timeStamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.child("sender").getValue().equals(user.getUid())) {
                        ds.getRef().removeValue();
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
        return chatList.size();
    }
/*
    @Override
    public int getItemViewType(int position) {
        user = FirebaseAuth.getInstance().getCurrentUser();

        if(chatList.get(position).getSender().equals(user.getUid())){
            if(chatList.get(position).getMsg_type().equals("image"))
                return MSG_TYPE_IMAGE_RIGHT;
            else
                return MSG_TYPE_RIGHT;
        }else{
            if(chatList.get(position).getMsg_type().equals("image"))
            return MSG_TYPE_IMAGE_LEFT;
        else
            return MSG_TYPE_LEFT;
        }
    }*/

    @Override
    public int getItemViewType(int position) {
        user = FirebaseAuth.getInstance().getCurrentUser();

        if(chatList.get(position).getSender().equals(user.getUid())){

                return MSG_TYPE_RIGHT;
        }else{

                return MSG_TYPE_LEFT;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView chatPic;
        TextView showMessages, timeStamp;
        ImageView ticks, msgImage;
        CardView imageCard;

        ImageButton playBTN;
        TextView voiceTimeText, voiceTimeDuration;
        SeekBar voiceNoteSeekbar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            chatPic = itemView.findViewById(R.id.chatProPic);
            showMessages = itemView.findViewById(R.id.showMessages);
            timeStamp = itemView.findViewById(R.id.chatTimeStamp);
            ticks = itemView.findViewById(R.id.greyTick);
            imageCard = itemView.findViewById(R.id.imageCard);

            playBTN = itemView.findViewById(R.id.playVoiceBTN);
            voiceTimeText = itemView.findViewById(R.id.postTimeText);
            voiceTimeDuration = itemView.findViewById(R.id.postTimeDuration);
            voiceNoteSeekbar = itemView.findViewById(R.id.voiceNoteSeekbar);
            msgImage = itemView.findViewById(R.id.chatImage);
        }
    }
}
