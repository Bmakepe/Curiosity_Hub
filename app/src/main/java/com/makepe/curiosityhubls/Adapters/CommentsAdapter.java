package com.makepe.curiosityhubls.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makepe.curiosityhubls.CustomClasses.GetTimeAgo;
import com.makepe.curiosityhubls.Models.Comment;
import com.makepe.curiosityhubls.Models.User;
import com.makepe.curiosityhubls.PostDetailsActivity;
import com.makepe.curiosityhubls.ProfileActivity;
import com.makepe.curiosityhubls.R;
import com.makepe.curiosityhubls.ViewProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skyhope.showmoretextview.ShowMoreTextView;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
    private Context context;
    private List<Comment> commentList;
    private String materialID;

    GetTimeAgo getTimeAgo;

    public CommentsAdapter(Context context, List<Comment> commentList, String materialID) {
        this.context = context;
        this.commentList = commentList;
        this.materialID = materialID;
    }

    public CommentsAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comments = commentList.get(position);
        getTimeAgo = new GetTimeAgo();

        String timeStamp = comments.getTimestamp();

        String PostTime = getTimeAgo.getTimeAgo(Long.parseLong(timeStamp), context);
        holder.commentTime.setText(PostTime);

        holder.commentCaption.setText(comments.getComment());

        holder.commentCaption.setShowingLine(2);
        holder.commentCaption.addShowMoreText("Continue Reading");
        holder.commentCaption.addShowLessText("Show Less");
        holder.commentCaption.setShowMoreColor(R.color.identity_dark);
        holder.commentCaption.setShowLessTextColor(R.color.identity);

        getCommentAuthor(comments, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(comments.getUid().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                    //showCommentOptions(holder.itemView, comments.getcID(), position);

                    Intent intent1 = new Intent(context, ProfileActivity.class);
                    context.startActivity(intent1);
                }else{
                    Intent intent = new Intent(context, ViewProfileActivity.class);
                    intent.putExtra("uid", comments.getUid());
                    context.startActivity(intent);

                }
            }
        });

        holder.replyComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.replyArea.setVisibility(View.VISIBLE);
                Toast.makeText(context, "You will be able to reply to a comment. The owners id is " + comments.getUid() , Toast.LENGTH_SHORT).show();
            }
        });

        holder.sendReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comReply = holder.commentReply.getText().toString().trim();

                if(comReply.isEmpty()){
                    Toast.makeText(context, "can not reply emply comment", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "Comment will be send", Toast.LENGTH_SHORT).show();
                    holder.commentReply.setText(null);
                    holder.commentReply.setHint("Reply Comment");
                    holder.replyArea.setVisibility(View.GONE);
                }
            }
        });
    }

    private void getCommentAuthor(Comment comment, ViewHolder holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Students").child(comment.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    User user = snapshot.getValue(User.class);

                    assert user != null;
                    holder.commentOwner.setText(user.getName());

                    try{
                        Picasso.get().load(user.getProfileImg()).placeholder(R.drawable.user_icon).into(holder.comProPic);
                    }catch (NullPointerException e){
                        Picasso.get().load(R.drawable.person_img).into(holder.comProPic);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Could not retrieve comment owner details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCommentOptions(View itemView, String cID, int position) {

        final PopupMenu popupMenu = new PopupMenu(context, itemView, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0,0,"Delete Comment");
        popupMenu.getMenu().add(Menu.NONE, 1,0,"Profile");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                switch (id){
                    case 0:
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Delete");
                        builder.setMessage("Are you sure to delete this message");

                        //delete btn
                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                deleteComment(cID);
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
                    case 1:
                        Intent intent1 = new Intent(context, ProfileActivity.class);
                        context.startActivity(intent1);
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + id);
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void deleteComment(String cID) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comments");
        ref.child(cID).removeValue();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String comments = "" + dataSnapshot.child("pComments").getValue();
                try{
                    int newCommentVal = Integer.parseInt(comments) -1;
                    ref.child("pComments").setValue("" + newCommentVal);
                }catch (NumberFormatException ignored){}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView commentOwner, commentTime;
        ShowMoreTextView commentCaption;
        ImageView replyComment;
        CircleImageView comProPic;
        RelativeLayout replyArea;
        ImageButton sendReply;
        EditText commentReply;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            commentCaption = itemView.findViewById(R.id.commentCaption);
            commentOwner = itemView.findViewById(R.id.commentOwner);
            commentTime = itemView.findViewById(R.id.commentTime);
            comProPic = itemView.findViewById(R.id.comProPic);
            replyComment = itemView.findViewById(R.id.replyComment);
            replyArea = itemView.findViewById(R.id.commentArea);
            sendReply = itemView.findViewById(R.id.sendReply);
            commentReply = itemView.findViewById(R.id.replyCommentET);
        }
    }
}
