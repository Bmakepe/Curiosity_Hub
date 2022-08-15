package com.makepe.curiosityhubls.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.makepe.curiosityhubls.Adapters.GroupAdapter;
import com.makepe.curiosityhubls.Models.GroupChat;
import com.makepe.curiosityhubls.Models.Groups;
import com.makepe.curiosityhubls.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupsFragment extends Fragment {

    List<Groups> groupsList;
    GroupAdapter groupAdapter;

    RecyclerView groupRecycler;

    DatabaseReference reference;
    FirebaseUser user;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        groupRecycler = view.findViewById(R.id.groupsRecycler);

        groupRecycler.setHasFixedSize(true);
        groupRecycler.setNestedScrollingEnabled(false);
        groupRecycler.hasFixedSize();
        groupRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        groupsList = new ArrayList<>();

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Groups");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                groupsList.clear();

                for(DataSnapshot ds : snapshot.getChildren()){
                    if(ds.child("Participants").child(user.getUid()).exists()) {
                        Groups groups = ds.getValue(Groups.class);
                        groupsList.add(groups);
                    }
                }

                //set Last group Message
                for(int i = 0; i < groupsList.size(); i++)
                    //lastMessage(groupsList.get(i).getGroupID());

                groupAdapter = new GroupAdapter(getContext(), groupsList);
                groupRecycler.setAdapter(groupAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

}