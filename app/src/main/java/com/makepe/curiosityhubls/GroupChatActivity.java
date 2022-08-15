package com.makepe.curiosityhubls;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makepe.curiosityhubls.Adapters.GroupChatAdapter;
import com.makepe.curiosityhubls.Adapters.MediaAdapter;
import com.makepe.curiosityhubls.Models.GroupChat;
import com.makepe.curiosityhubls.Models.Groups;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {

    TextView gName;
    CircleImageView gIcon;
    ImageView menuIcon;

    DatabaseReference reference;
    FirebaseUser firebaseUser;

    List<GroupChat> groupChat;

    String groupID, groupName, groupIcon, groupAdmin, myGroupRole, groupPrivacy, groupTimeStamp;

    RecyclerView groupChatsRecycler;
    EditText groupChatET;
    ImageButton groupSendBTN, attachMedia;

    MediaAdapter mediaAdapter;
    GroupChatAdapter groupChatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        gName = findViewById(R.id.gName);
        gIcon = findViewById(R.id.gIcon);
        menuIcon = findViewById(R.id.groupChatMenu);
        groupChatsRecycler = findViewById(R.id.groupRecyclerView);
        groupChatET = findViewById(R.id.groupMessageEt);
        groupSendBTN = findViewById(R.id.groupVoiceBTN);
        attachMedia = findViewById(R.id.attachMedia);

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Groups").child(groupID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Groups groups = snapshot.getValue(Groups.class);

                try {
                    groupAdmin = groups.getRole();
                    groupIcon = groups.getGroupIcon();
                    groupName = groups.getGroupName();
                    groupPrivacy = groups.getGroupPrivacy();
                    groupTimeStamp = groups.getTimestamp();

                    gName.setText(groups.getGroupName());
                    Picasso.get().load(groups.getGroupIcon()).into(gIcon);
                } catch (NullPointerException e) {
                    Picasso.get().load(R.drawable.person_img).into(gIcon);
                }

                readMessages(firebaseUser.getUid(), groupID);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        loadMyGroupRole();

        groupChatET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() != 0){
                    groupSendBTN.setImageResource(R.drawable.ic_baseline_send_24);
                }else{
                    groupSendBTN.setImageResource(R.drawable.ic_baseline_mic_24);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        groupChatsRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        groupChatsRecycler.setLayoutManager(layoutManager);

        groupSendBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = groupChatET.getText().toString().trim();

                if(TextUtils.isEmpty(msg)){
                    Toast.makeText(GroupChatActivity.this, "Can't send empty message...", Toast.LENGTH_SHORT).show();
                }else {
                    sendGroupMessage(firebaseUser.getUid(), groupID, msg);
                }
            }
        });

        attachMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(GroupChatActivity.this, "This function is under development", Toast.LENGTH_SHORT).show();
            }
        });

        gName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent G_intent = new Intent(GroupChatActivity.this, GroupDetailsActivity.class);
                G_intent.putExtra("groupID", groupID);
                startActivity(G_intent);
            }
        });

        findViewById(R.id.groupChatBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //finish();
                startActivity(new Intent(GroupChatActivity.this, MainActivity.class));
            }
        });

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 showMoreOptions(myGroupRole);
            }
        });
    }

    private void loadMyGroupRole() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupID).child("Participants")
                .orderByChild("uid").equalTo(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            Groups groups = ds.getValue(Groups.class);

                            myGroupRole = groups.getRole();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void readMessages(String uid, String groupID) {
        groupChat = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("GroupChats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChat.clear();

                for(DataSnapshot ds : snapshot.getChildren()){
                    GroupChat chat = ds.getValue(GroupChat.class);

                    try{
                        if(chat.getGroupID().equals(uid) && chat.getSenderID().equals(groupID) ||
                            chat.getGroupID().equals(groupID) && chat.getSenderID().equals(uid)){
                            groupChat.add(chat);
                        }
                    }catch (NullPointerException ignored){}

                    groupChatAdapter = new GroupChatAdapter(GroupChatActivity.this, groupChat);
                    groupChatsRecycler.setAdapter(groupChatAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendGroupMessage(String uid, String groupID, String msg) {

        String timeStamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference groupDB = FirebaseDatabase.getInstance().getReference("GroupChats");

        String chatID = groupDB.push().getKey();

        final DatabaseReference ref = groupDB.child(chatID);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("groupID", groupID);
        hashMap.put("senderID", uid);
        hashMap.put("message", msg);
        hashMap.put("timeStamp", timeStamp);
        hashMap.put("media", "");
        hashMap.put("audio", "noAudio");
        hashMap.put("msg_type", "text");

        ref.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                groupChatET.setText("");
                groupChatET.setHint("Write Your Message");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showMoreOptions(String myGroupRole) {
        PopupMenu popupMenu = new PopupMenu(GroupChatActivity.this, menuIcon, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0,0,"View Group Details");

        if(!myGroupRole.equals("participant"))
            popupMenu.getMenu().add(Menu.NONE, 1,0,"Manage Group Members");

        popupMenu.getMenu().add(Menu.NONE, 2,0,"Exit Group");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id == 0){
                    Intent G_intent = new Intent(GroupChatActivity.this, GroupDetailsActivity.class);
                    G_intent.putExtra("groupID", groupID);
                    startActivity(G_intent);
                }else if(id == 1){
                    Intent intent = new Intent(GroupChatActivity.this, AddGroupMembersActivity.class);
                    intent.putExtra("groupID", groupID);
                    intent.putExtra("groupRole", GroupChatActivity.this.myGroupRole);
                    startActivity(intent);
                }else if(id == 2){
                    String dialogTitle = "";
                    String dialogDescription = "";
                    String positiveButtonTitle = "";

                    if(myGroupRole.equals("creator")){
                        dialogTitle = "Delete Group";
                        dialogDescription = "Are You Sure You Want To Delete Group Permanently";
                        positiveButtonTitle = "DELETE";
                    }else{
                        dialogTitle = "Leave Group";
                        dialogDescription = "Are You Sure You Want To Leave Group Permanently";
                        positiveButtonTitle = "LEAVE";
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
                    builder.setTitle(dialogTitle)
                            .setMessage(dialogDescription)
                            .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if(myGroupRole.equals("creator")){
                                        deleteGroup();
                                    }else{
                                        leaveGroup();
                                    }
                                }
                            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
                    Toast.makeText(GroupChatActivity.this, "Exit Group", Toast.LENGTH_SHORT).show();
                    Toast.makeText(GroupChatActivity.this, "Exit Group", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void leaveGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupID).child("Participants").child(firebaseUser.getUid())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupChatActivity.this, "Group Left Successfully", Toast.LENGTH_SHORT).show();
                        //startActivity(new Intent(GroupDetailsActivity.this, MainActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteGroup() {
        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(groupIcon);
        picRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(GroupChatActivity.this, "Group Icon Deleted Successfully", Toast.LENGTH_SHORT).show();
            }
        });
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupID)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupChatActivity.this, "Group Deleted Successfully", Toast.LENGTH_SHORT).show();
                        //startActivity(new Intent(GroupDetailsActivity.this, MainActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}