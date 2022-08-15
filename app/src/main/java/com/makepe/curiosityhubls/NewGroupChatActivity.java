package com.makepe.curiosityhubls;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewGroupChatActivity extends AppCompatActivity {

    CircleImageView groupIcon;
    EditText groupSubject;

    FirebaseUser firebaseUser;
    DatabaseReference reference;
    StorageReference storageReference;

    ProgressBar GC_Loader;

    ProgressDialog pd;

    Uri groupPicUri;
    StorageTask uploadTask;
    String groupUri = "", groupId, groupRole = "creator", privacyLock;

    RadioGroup groupPrivacyBTN;
    RadioButton selectedPrivacy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group_chat);

        groupIcon = findViewById(R.id.groupIcon);
        groupSubject = findViewById(R.id.groupSubject);
        GC_Loader = findViewById(R.id.GC_Loader);
        groupPrivacyBTN = findViewById(R.id.groupPrivacyBTN);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Students");
        storageReference = FirebaseStorage.getInstance().getReference("GroupIcons");

        groupPrivacyBTN.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.privateGP:
                        privacyLock = "Private";
                        Toast.makeText(NewGroupChatActivity.this, privacyLock, Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.publicGP:
                        privacyLock = "Public";
                        Toast.makeText(NewGroupChatActivity.this, privacyLock, Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        Toast.makeText(NewGroupChatActivity.this, "Please select group privacy", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        pd = new ProgressDialog(this);

        groupIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setAspectRatio(1,1)
                        .start(NewGroupChatActivity.this);
            }
        });

        findViewById(R.id.addGroupBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subject = groupSubject.getText().toString().trim();

                if(subject.isEmpty()){
                    groupSubject.setError("Type the group name");
                    groupSubject.requestFocus();
                }else if(!validatePrivacy()){
                    Toast.makeText(NewGroupChatActivity.this, "You need to select privacy", Toast.LENGTH_SHORT).show();
                }else{

                    pd.setMessage("Creating Group");
                    createNewGroup(subject);
                }
            }
        });

        findViewById(R.id.newGroupBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private boolean validatePrivacy() {

        if(groupPrivacyBTN.getCheckedRadioButtonId() == -1){
            Toast.makeText(this, "Please select privacy", Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }
    }

    private void createNewGroup(String subject) {
        pd.show();
        selectedPrivacy = findViewById(groupPrivacyBTN.getCheckedRadioButtonId());
        privacyLock = selectedPrivacy.getText().toString();

        Toast.makeText(this, privacyLock, Toast.LENGTH_SHORT).show();

        final DatabaseReference groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        DatabaseReference push = groupNameRef.push();
        groupId = push.getKey();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(groupPicUri));

        uploadTask = fileReference.putFile(groupPicUri);
        uploadTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    groupUri = downloadUri.toString();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("groupID", groupId);
                    hashMap.put("groupAdmin", firebaseUser.getUid());
                    hashMap.put("groupName", subject);
                    hashMap.put("role", groupRole);
                    hashMap.put("groupIcon", groupUri);
                    hashMap.put("groupPrivacy", privacyLock);
                    hashMap.put("timestamp", "" + System.currentTimeMillis());

                    databaseReference.child(groupId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            loadMyParticipation();
                            Intent intent = new Intent(NewGroupChatActivity.this, GroupChatActivity.class);
                            intent.putExtra("groupID", groupId);
                            startActivity(intent);

                            pd.dismiss();

                        }
                    });
                }
            }
        });

    }

    private void loadMyParticipation() {
        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", firebaseUser.getUid());
        hashMap.put("role", groupRole);
        hashMap.put("timestamp", ""+timestamp);
        hashMap.put("groupID", groupId);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(firebaseUser.getUid()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewGroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            groupPicUri = result.getUri();

            groupIcon.setImageURI(groupPicUri);

        }
    }
}