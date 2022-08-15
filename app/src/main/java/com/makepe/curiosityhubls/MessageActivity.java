package com.makepe.curiosityhubls;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.makepe.curiosityhubls.Adapters.ChatAdapter;
import com.makepe.curiosityhubls.Adapters.MediaAdapter;
import com.makepe.curiosityhubls.Models.Chat;
import com.makepe.curiosityhubls.Models.User;
import com.makepe.curiosityhubls.Notifications.Data;
import com.makepe.curiosityhubls.Notifications.Sender;
import com.makepe.curiosityhubls.Notifications.Token;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profileIV;
    TextView nameTV;
    EditText messageET;
    ImageButton sendBTN, voiceBTN, attachFiles;
    RecyclerView chatRecycler, mediaRecycler;
    ImageView chatback, chatMenu;

    FirebaseUser user;
    DatabaseReference reference, userRefForSeen;
    StorageReference storageReference;

    ValueEventListener seenListener;

    List<Chat> chatList;
    ChatAdapter chatAdapter;
    MediaAdapter mediaAdapter;

    String senderID, receiverID, hisImage;

    //volley request queue for notification
    private RequestQueue requestQueue;

    private boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        profileIV = findViewById(R.id.profileCIV);
        nameTV = findViewById(R.id.hisNameTV);
        messageET = findViewById(R.id.messageEt);
        sendBTN = findViewById(R.id.sendBTN);
        voiceBTN = findViewById(R.id.voiceBTN);
        chatRecycler = findViewById(R.id.chatRecyclerView);
        chatback = findViewById(R.id.chatBackBTN);
        chatMenu = findViewById(R.id.chatMenu);
        attachFiles = findViewById(R.id.attachFiles);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Students");
        storageReference = FirebaseStorage.getInstance().getReference();
        assert user != null;
        senderID = user.getUid();

        Intent intent = getIntent();
        receiverID = intent.getStringExtra("receiverID");

        //layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        //recyclerview properties
        chatRecycler.setHasFixedSize(true);
        chatRecycler.setLayoutManager(layoutManager);

        getUserInfo(receiverID, user);
        readMessages();
        seenMessages();

        messageET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if(charSequence.length() != 0){
                    //voiceBTN.setImageResource(R.drawable.ic_baseline_send_24);
                    sendBTN.setVisibility(View.VISIBLE);
                    voiceBTN.setVisibility(View.GONE);

                }else{
                    sendBTN.setVisibility(View.GONE);
                    voiceBTN.setVisibility(View.VISIBLE);
                    //voiceBTN.setImageResource(R.drawable.ic_baseline_mic_24);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        chatback.setOnClickListener(v -> finish());

        chatMenu.setOnClickListener(v -> showMoreOptions());

        nameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MessageActivity.this, ViewProfileActivity.class);
                intent.putExtra("uid", receiverID);
                startActivity(intent);
            }
        });

        sendBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageET.getText().toString().trim();

                if(TextUtils.isEmpty(message)){
                    Toast.makeText(MessageActivity.this, "Cant send empty message", Toast.LENGTH_SHORT).show();
                }else{
                    sendMessage(message);
                }
            }
        });

        attachFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(MessageActivity.this, "This function is under development", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(MessageActivity.this, chatMenu, Gravity.END);
        popupMenu.getMenu().add(Menu.NONE, 0,0,"View Profile");
        popupMenu.getMenu().add(Menu.NONE, 1,0,"Send Alert");
        popupMenu.getMenu().add(Menu.NONE, 2,0,"Search");
        popupMenu.getMenu().add(Menu.NONE, 3,0,"Send Files");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id == 0){

                    Intent intent = new Intent(MessageActivity.this, ViewProfileActivity.class);
                    intent.putExtra("uid", receiverID);
                    startActivity(intent);

                }else if(id == 1){

                    sendMessage("PING!!!");

                }else if (id == 2){
                    //searchDialog.show();
                    Toast.makeText(MessageActivity.this, "Search the chat", Toast.LENGTH_SHORT).show();

                }else if(id == 3){
                    Toast.makeText(MessageActivity.this, "Send Media to your friend", Toast.LENGTH_SHORT).show();
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_GET_CONTENT);
                    shareIntent.setType("*/*");
                    startActivity(shareIntent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void seenMessages() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    assert chat != null;
                    if(chat.getReceiver().equals(senderID) && chat.getSender().equals(receiverID)){
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", senderID);
        hashMap.put("receiver", receiverID);
        hashMap.put("message", message);
        hashMap.put("msg_type", "text");
        hashMap.put("audio", "noAudio");
        hashMap.put("isSeen", false);
        hashMap.put("timeStamp", timeStamp);

        databaseReference.child("Chats").push().setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                messageET.setText(null);
                messageET.setHint("Type Your Message Here");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MessageActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Students")
                .child(user.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotification(receiverID, user.getName(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //create chatlist node/child in firebase database
        final DatabaseReference senderReference = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(senderID)
                .child(receiverID);
        senderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    senderReference.child("id").setValue(receiverID);
                    senderReference.child("timeStamp").setValue(String.valueOf(System.currentTimeMillis()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference receiverReference = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(receiverID)
                .child(senderID);
        receiverReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    receiverReference.child("id").setValue(senderID);
                    senderReference.child("timeStamp").setValue(String.valueOf(System.currentTimeMillis()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendNotification(String receiverID, String name, String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiverID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);

                    Data data = new Data(
                            "" + user.getUid(),
                            ""+ name + ": " + message,
                            "New Message",
                            ""+ receiverID,
                            "ChatNotification",
                             R.mipmap.ic_launcher);

                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());

                    //fcm json object request
                    try{
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        //response of the request
                                        Log.d("JSON_RESPONSE", "onResponse: " + response.toString());

                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE", "onResponse: " + error.toString());

                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                //put params
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAAQ0baSm4:APA91bE20XhDHxSO25ld1dMwmcPI9GBRFll5Znl2POLSGQq5IbuvdNPgJHOFU5Ce0z6xBJzM3OLAK33O3dSar6j0EWxCr9J5p-m0hAcYQ2VlAzkJpLn5Pwy1_kBr5q55JLae-rLIZDUh");

                                return headers;
                            }
                        };

                        requestQueue.add(jsonObjectRequest);

                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserInfo(String receiverID, FirebaseUser user) {
        Query query = reference.orderByChild("userId").equalTo(receiverID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot ds : snapshot.getChildren()){
                        User user1 = ds.getValue(User.class);

                        assert user1 != null;
                        hisImage = user1.getProfileImg();

                        nameTV.setText(user1.getName());
                        try{
                            Picasso.get().load(user1.getProfileImg()).placeholder(R.drawable.person_img).into(profileIV);
                        }catch (NullPointerException e){

                            Picasso.get().load(R.drawable.person_img).into(profileIV);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessages() {
        chatList = new ArrayList<>();
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);

                    assert chat != null;
                    if(chat.getReceiver().equals(senderID) && chat.getSender().equals(receiverID) ||
                            chat.getReceiver().equals(receiverID) && chat.getSender().equals(senderID)){
                        chatList.add(chat);
                    }
                    chatAdapter = new ChatAdapter(MessageActivity.this, chatList, hisImage);
                    chatAdapter.notifyDataSetChanged();
                    chatRecycler.setAdapter(chatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}