package com.makepe.curiosityhubls.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.makepe.curiosityhubls.Adapters.ChatlistAdapter;
import com.makepe.curiosityhubls.Models.Chat;
import com.makepe.curiosityhubls.Models.ChatlistModel;
import com.makepe.curiosityhubls.Models.User;
import com.makepe.curiosityhubls.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatlistFragment extends Fragment {

    private RecyclerView chatlistRecycler;
    private List<ChatlistModel> chatlist;
    private List<User> userList;

    private DatabaseReference reference;
    private FirebaseUser currentUser;
    private ChatlistAdapter chatlistAdapter;

    ProgressBar chatListLoader;

        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chatlist, container, false);

        chatlistRecycler = view.findViewById(R.id.activeChats);
        chatListLoader = view.findViewById(R.id.chatListLoader);

        chatlistRecycler.setHasFixedSize(true);
        chatlistRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        chatlist = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatlist.clear();
                if(dataSnapshot.exists()){
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        ChatlistModel cm = ds.getValue(ChatlistModel.class);
                        chatlist.add(cm);
                    }
                    loadChats();
                }else{

                    chatListLoader.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "You have no active chats", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

       // updateToken(FirebaseInstanceId.getInstance().getToken());

        return view;
    }

    private void loadChats() {
        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Students");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                if(dataSnapshot.exists()){
                    //Collections.sort(chatlist, ChatlistFragment.this::compare);
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        User user = ds.getValue(User.class);
                        for(ChatlistModel chatList : chatlist){
                            assert user != null;
                            if (user.getUserId().equals(chatList.getId())){
                                userList.add(user);
                            }
                        }
                        chatlistAdapter = new ChatlistAdapter(getContext(), userList);
                        chatlistRecycler.setAdapter(chatlistAdapter);

                        //set Last message
                        for(int i = 0; i < userList.size(); i++){
                            lastMessage(userList.get(i).getUserId());
                        }

                        chatListLoader.setVisibility(View.GONE);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void lastMessage(String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String theLastMessage = "def" +
                        "ault";
                String lastTimeStamp = "default";

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Chat chat = ds.getValue(Chat.class);
                    if(chat == null){
                        continue;
                    }

                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();

                    if(sender == null || receiver == null){
                        continue;
                    }

                    if(chat.getReceiver().equals(currentUser.getUid()) &&
                            chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) &&
                                    chat.getSender().equals(currentUser.getUid())){
                        if(chat.getMsg_type().equals("image")){
                            theLastMessage = "Sent a photo";
                        }else{
                            theLastMessage = chat.getMessage();
                        }
                        lastTimeStamp = chat.getTimeStamp();
                    }
                }
                chatlistAdapter.setLastMessageMap(userId, theLastMessage);
                chatlistAdapter.setLastTimeStamp(userId, lastTimeStamp);

                chatlistAdapter.notifyDataSetChanged();

                chatListLoader.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    /*public int compare(ChatlistModel o1, ChatlistModel o2)
    {
            //return Long.compare(o2.getTimeStamp(), o1.getTimeStamp());
        //return o1.getTimeStamp() < o2.getTimeStamp() ? 1 : (o1.getTimeStamp() == o2.getTimeStamp() ? 0 : -1);

    }*/
}