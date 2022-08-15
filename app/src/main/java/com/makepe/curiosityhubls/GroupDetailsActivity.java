package com.makepe.curiosityhubls;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.makepe.curiosityhubls.Adapters.GroupContactsAdapter;
import com.makepe.curiosityhubls.CustomClasses.GetTimeAgo;
import com.makepe.curiosityhubls.Models.ContactsModel;
import com.makepe.curiosityhubls.Models.Groups;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupDetailsActivity extends AppCompatActivity {

    TextView gName, membersCounter, groupPrivacyTV, groupTimeStamp;
    CircleImageView gIcon, edit_GD_Icon;
    ImageView menuIcon;

    Dialog EditDetailsDialog, searchDialog, fullScreenDialog;

    FirebaseUser user;
    StorageReference storageReference;
    DatabaseReference reference;

    Uri groupPicUri;
    StorageTask uploadTask;
    String groupUri = "", groupID, groupName, groupIcon, groupRole, privacyLock, timestamp, myGroupRole;

    RadioGroup groupPrivacyBTN;
    RadioButton selectedPrivacy;

    ProgressDialog pd;
    ProgressBar membersLoader;

    GroupContactsAdapter userAdapter;
    List<ContactsModel> groupMembers;
    RecyclerView activeMembersRecycler;

    GetTimeAgo getTimeAgo = new GetTimeAgo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        gName = findViewById(R.id.GD_Subject);
        gIcon = findViewById(R.id.GD_Icon);
        menuIcon = findViewById(R.id.GD_MenuBTN);
        membersCounter = findViewById(R.id.GM_TV);
        membersLoader = findViewById(R.id.membersLoader);
        activeMembersRecycler = findViewById(R.id.GD_Member_Recycler);
        groupPrivacyTV = findViewById(R.id.groupPrivacyTV);
        groupTimeStamp = findViewById(R.id.groupTimeStamp);

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");

        getMyDetails();

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Groups").child(groupID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Groups groups = snapshot.getValue(Groups.class);

                try{
                    groupRole = groups.getRole();
                    groupIcon = groups.getGroupIcon();
                    groupName = groups.getGroupName();
                    privacyLock = groups.getGroupPrivacy();
                    timestamp = groups.getTimestamp();

                    gName.setText(groups.getGroupName());
                    groupPrivacyTV.setText(groups.getGroupPrivacy());

                    String PostTime = getTimeAgo.getTimeAgo(Long.parseLong(groups.getTimestamp()), GroupDetailsActivity.this);
                    groupTimeStamp.setText(PostTime);

                    Picasso.get().load(groups.getGroupIcon()).into(gIcon);

                }catch (NullPointerException e){
                    Picasso.get().load(R.drawable.person_img).into(gIcon);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        activeMembersRecycler.setHasFixedSize(true);
        activeMembersRecycler.setNestedScrollingEnabled(false);
        activeMembersRecycler.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        activeMembersRecycler.setLayoutManager(layoutManager);

        iniEditDetailsDialog(groupID);
        iniSearchDialog();
        iniFullScreenPic(groupID);
        getGroupMembers();

        gIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullScreenDialog.show();
            }
        });

        findViewById(R.id.GD_BackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                /*Intent intent = new Intent(GroupDetailsActivity.this, GroupChatActivity.class);
                intent.putExtra("groupID", groupID);
                startActivity(intent);*/
            }
        });

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions();
            }
        });

    }

    private void getMyDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupID).child("Participants")
                .orderByChild("uid").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            Groups groups = ds.getValue(Groups.class);

                            myGroupRole = groups.getRole();

                            if(!myGroupRole.equals("participant")){
                                menuIcon.setVisibility(View.VISIBLE);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getGroupMembers() {
        groupMembers = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupID).child("Participants")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        groupMembers.clear();
                        for(DataSnapshot ds : snapshot.getChildren()){

                            String uid = "" + ds.child("uid").getValue();

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Students");
                            reference.orderByChild("userId").equalTo(uid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for(DataSnapshot ds : snapshot.getChildren()){
                                        ContactsModel user = ds.getValue(ContactsModel.class);

                                        groupMembers.add(user);
                                    }
                                    userAdapter = new GroupContactsAdapter(groupID, myGroupRole, GroupDetailsActivity.this, groupMembers );
                                    activeMembersRecycler.setAdapter(userAdapter);

                                    membersCounter.setText("Group Members (" + groupMembers.size() + ")");
                                    membersLoader.setVisibility(View.GONE);
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

    private void iniFullScreenPic(String groupID) {
        fullScreenDialog = new Dialog(this);
        fullScreenDialog.setContentView(R.layout.fullscreen_image_view);
        fullScreenDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        fullScreenDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        fullScreenDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        ImageView fullScreenPic = fullScreenDialog.findViewById(R.id.fullScreenPic);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups").child(groupID);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Groups groups = snapshot.getValue(Groups.class);

                try{
                    Picasso.get().load(groups.getGroupIcon()).into(fullScreenPic);
                }catch (NullPointerException e){
                    Picasso.get().load(R.drawable.person_img).into(fullScreenPic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        fullScreenDialog.findViewById(R.id.fullScreenBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullScreenDialog.dismiss();
            }
        });
    }

    private void iniSearchDialog() {
        searchDialog = new Dialog(this);
        searchDialog.setContentView(R.layout.search_pop_up);
        searchDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        searchDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        searchDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        searchDialog.findViewById(R.id.searchBackBTN).setOnClickListener(view -> searchDialog.dismiss());
    }

    private void iniEditDetailsDialog(String groupID) {
        EditDetailsDialog = new Dialog(this);
        EditDetailsDialog.setContentView(R.layout.edit_group_details);
        EditDetailsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        EditDetailsDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        EditDetailsDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        edit_GD_Icon = EditDetailsDialog.findViewById(R.id.editGroupIcon);
        EditText edit_GD_name = EditDetailsDialog.findViewById(R.id.editGroupName);
        groupPrivacyBTN = EditDetailsDialog.findViewById(R.id.editGroupPrivacyBTN);

        storageReference = FirebaseStorage.getInstance().getReference("GroupIcons");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups").child(groupID);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Groups groups = snapshot.getValue(Groups.class);

                try{
                    edit_GD_name.setText(groups.getGroupName());

                    Picasso.get().load(groups.getGroupIcon()).into(edit_GD_Icon);
                }catch (NullPointerException e){
                    Picasso.get().load(R.drawable.person_img).into(edit_GD_Icon);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        EditDetailsDialog.findViewById(R.id.editGroupBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditDetailsDialog.dismiss();
            }
        });

        EditDetailsDialog.findViewById(R.id.changePicArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .start(GroupDetailsActivity.this);
            }
        });

        EditDetailsDialog.findViewById(R.id.doneBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //EditDetailsDialog.dismiss();
                pd = new ProgressDialog(GroupDetailsActivity.this);

                updateGroupDetails(edit_GD_name);
            }
        });
    }

    private void updateGroupDetails(EditText edit_GD_name) {
        String groupName = edit_GD_name.getText().toString().trim();

        if(groupName.isEmpty()){

            edit_GD_name.setError("Enter The Group Name Here");
            edit_GD_name.requestFocus();

        }else{
            pd.setMessage("Updating Group Details");
            pd.show();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups").child(groupID);

            if(groupUri != null) {
                final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                        + "." + getFileExtension(groupPicUri));

                uploadTask = fileReference.putFile(groupPicUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return fileReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            groupUri = downloadUri.toString();

                            StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(groupIcon);
                            picRef.delete();

                            HashMap<String, Object> result = new HashMap<>();
                            result.put("groupName", groupName);
                            result.put("groupIcon", groupUri);

                            databaseReference.updateChildren(result).
                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            edit_GD_name.setText(null);
                                            pd.dismiss();
                                            EditDetailsDialog.dismiss();
                                            Toast.makeText(GroupDetailsActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(GroupDetailsActivity.this, "Error updating details. Please try again later", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }else{
                HashMap<String, Object> result = new HashMap<>();
                result.put("groupName", groupName);
                result.put("groupIcon", groupIcon);

                databaseReference.updateChildren(result).
                        addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                edit_GD_name.setText(null);
                                pd.dismiss();
                                EditDetailsDialog.dismiss();
                                Toast.makeText(GroupDetailsActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupDetailsActivity.this, "Error updating details. Please try again later", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private String getFileExtension(Uri uri){
        try {
            ContentResolver contentResolver = getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getExtensionFromMimeType(contentResolver.getType(uri));
        }catch (NullPointerException ignored){}
        return null;
    }

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(GroupDetailsActivity.this, menuIcon, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE, 0,0,"Edit Group Details");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id == 0){
                    EditDetailsDialog.show();
                }
                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            groupPicUri = result.getUri();

            edit_GD_Icon.setImageURI(groupPicUri);

        }
    }
}