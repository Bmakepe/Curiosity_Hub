package com.makepe.curiosityhubls;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.makepe.curiosityhubls.CustomClasses.Permissions;
import com.makepe.curiosityhubls.Models.User;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends AppCompatActivity {

    CircleImageView postActivityProPic;

    FirebaseAuth firebaseAuth; //firebase auth
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    StorageReference storageReference;

    private ImageView pickToUpload;
    private EditText captionArea;
    TextView taggedPeople;
    private String image, caption, postOwner;

    ProgressDialog pd;

    TextView postBTN;
    private Dialog searchDialog;

    Uri imageUri;
    String myUri = "";
    StorageTask uploadTask;

    Permissions permissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postActivityProPic = findViewById(R.id.addPostProPic);
        postBTN = findViewById(R.id.postFAB);
        TextView goToCam = findViewById(R.id.goToCamera);
        pickToUpload = findViewById(R.id.picToUpload);
        ImageView backBTN = findViewById(R.id.backBTN);
        captionArea = findViewById(R.id.addPostArea);
        taggedPeople = findViewById(R.id.taggedPeople);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Students");
        reference.keepSynced(true);
        storageReference = FirebaseStorage.getInstance().getReference("Post_Pics");

        iniSearchDialog();

        pd = new ProgressDialog(this);
        permissions = new Permissions(this);
        permissions.verifyPermissions();

        Query query = reference.orderByChild("userId").equalTo(firebaseUser.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    assert user != null;
                    postOwner = user.getName(); //get owner
                    image = user.getProfileImg(); //get profile picture

                    try{
                        Picasso.get().load(image).into(postActivityProPic);
                    }catch (NullPointerException e){

                        Picasso.get().load(R.drawable.person_img).into(postActivityProPic);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        postBTN.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                //Button for posting to the database
                caption = captionArea.getText().toString().trim();
                pd.setMessage("Loading...");

                if(TextUtils.isEmpty(caption)){
                    captionArea.setError("Please write something to post");
                    return;
                }

                if(imageUri == null){
                    //post text posts along with no pictures attached
                    postBTN.setVisibility(View.INVISIBLE);
                    uploadData(caption, "noImage");
                }else{
                    //posts text posts with pictures attached
                    postBTN.setVisibility(View.INVISIBLE);
                    uploadData(caption, String.valueOf(imageUri));

                }

                finish();
                //startActivity(new Intent(PostActivity.this, MainActivity.class));
            }
        });

        goToCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //used to access the gallery and camera options
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .start(PostActivity.this);

            }
        });

        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void iniSearchDialog() {
        searchDialog = new Dialog(this);
        searchDialog.setContentView(R.layout.search_pop_up);
        searchDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        searchDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        searchDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        searchDialog.findViewById(R.id.searchBackBTN).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchDialog.dismiss();
            }
        });
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)  {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            assert result != null;
            imageUri = result.getUri();

            pickToUpload.setVisibility(View.VISIBLE);
            pickToUpload.setImageURI(imageUri);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    } //below is the handler for recording voice status

    private void uploadData(final String caption, String uri) {//for post image name, post id, post timestamp

        final String timeStamp = String.valueOf(System.currentTimeMillis());

        if(!uri.equals("noImage")){
            //posts with images - noImage is not parsed into the function

            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation(){
                @Override
                public Object then(@NonNull Task task) throws Exception{
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
                        myUri = downloadUri.toString();

                        //url is received upload post to firebase database

                        HashMap<Object, String> hashMap = new HashMap<>();

                        //Path to store post data
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        String postId = ref.push().getKey();

                        //put the post info
                        hashMap.put("uid", firebaseUser.getUid());//usersID
                        hashMap.put("pId", postId); //the id of the post is the time at which the post has been added
                        hashMap.put("Caption", caption); // the post caption
                        hashMap.put("PostImage", myUri); //the post image which has been send to firebase storage and only the uri is stored
                        hashMap.put("PostTime", timeStamp);// the time at which the post has been posted

                        //put data in this reference
                        assert postId != null;
                        ref.child(postId).setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @SuppressLint("RestrictedApi")
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //executes when the post has successfully been posted to firebase database
                                        postBTN.setVisibility(View.VISIBLE);
                                        //pd.dismiss();

                                        //reset view after posting
                                        captionArea.setText("");
                                        pickToUpload.setImageURI(null);
                                        imageUri = null;

                                        //send notification
                                        prepareNotification(
                                                ""+ postId,
                                                ""+ postOwner + " added a new post",
                                                ""+caption,
                                                "PostNotification",
                                                "POST"
                                        );
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(PostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else{
            //executes when there is not image added to the post

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

            String postId = ref.push().getKey();

            HashMap<Object, String> hashMap = new HashMap<>();
            //put the post info
            hashMap.put("uid", firebaseUser.getUid());//usersID
            hashMap.put("pId", postId); //the id of the post is the time at which the post has been added
            hashMap.put("Caption", caption); // the post caption
            hashMap.put("PostImage", "noImage"); //the post image which has been send to firebase storage and only the uri is stored
            hashMap.put("PostTime", timeStamp);// the time at which the post has been posted

            assert postId != null;
            ref.child(postId).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @SuppressLint("RestrictedApi")
                        @Override
                        public void onSuccess(Void aVoid) {
                            //executes when the post with no image has been posted successfully to firebase
                            postBTN.setVisibility(View.VISIBLE);
                            pd.dismiss();

                            //reset view after posting
                            captionArea.setText("");
                            pickToUpload.setImageURI(null);
                            imageUri = null;

                            //send notification
                            prepareNotification(
                                    ""+ postId,
                                    ""+ postOwner + " added a new post",
                                    ""+caption,
                                    "PostNotification",
                                    "POST"
                            );
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

    private void prepareNotification(String postID, String title, String caption, String notificationType, String notificationTopic){
        //prepare fata for notifications

        String NOTIFICATION_TOPIC = "/topics/" + notificationTopic;       //topic must match with what the receiver subscribed
        String NOTIFICATION_TITLE = title;      //for e.g Bokang Makepe added a new post
        String NOTIFICATION_CAPTION = caption;  //content of the post
        String NOTIFICATION_TYPE = notificationType; //now ther are two notification types chat & post, so to differentiate in FirebaseMessaging.java class

        //prepare json what to send, and where to send it
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();

        try{
            //what to send
            notificationBodyJo.put("notificationType" , NOTIFICATION_TYPE);
            notificationBodyJo.put("sender", firebaseUser.getUid()); //userid of current use/sender
            notificationBodyJo.put("postID", postID);   //post id
            notificationBodyJo.put("title", NOTIFICATION_TITLE);
            notificationBodyJo.put("caption", NOTIFICATION_CAPTION);

            //where to send
            notificationJo.put("to", NOTIFICATION_TOPIC);
            notificationJo.put("data", notificationBodyJo);     //combine data to be sent

        }catch (JSONException e){
            Toast.makeText(this, ""+ e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendPostNotification(notificationJo);

    }

    private void sendPostNotification(JSONObject notificationJo) {
        //send volley object request

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo,
                response -> {
                    Log.d("FCM_RESPONSE", "onResponse: " + response.toString());
                    Toast.makeText(PostActivity.this, "Notifications Send", Toast.LENGTH_SHORT).show();
                }, error -> {
                    //error occured
                    Toast.makeText(PostActivity.this, "" + error.toString(), Toast.LENGTH_SHORT).show();
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put required headers

                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=AAAAQ0baSm4:APA91bE20XhDHxSO25ld1dMwmcPI9GBRFll5Znl2POLSGQq5IbuvdNPgJHOFU5Ce0z6xBJzM3OLAK33O3dSar6j0EWxCr9J5p-m0hAcYQ2VlAzkJpLn5Pwy1_kBr5q55JLae-rLIZDUh");

                return headers;
            }
        };

        //enqueue the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

}