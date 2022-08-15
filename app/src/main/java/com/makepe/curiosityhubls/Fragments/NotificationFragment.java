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

import com.makepe.curiosityhubls.Adapters.NotificationAdapter;
import com.makepe.curiosityhubls.Models.NotiModel;
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
import java.util.List;

public class NotificationFragment extends Fragment {

    RecyclerView notiRecycler;
    NotificationAdapter notificationAdapter;
    List<NotiModel> notificationList;

    ProgressBar notiLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        notiRecycler = view.findViewById(R.id.notiRecycler);
        notiLoader = view.findViewById(R.id.notiLoader);

        notiRecycler.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        notiRecycler.setLayoutManager(linearLayoutManager);

        notificationList = new ArrayList<>();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationList.clear();

                if(dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        NotiModel notiModel = snapshot.getValue(NotiModel.class);
                        notificationList.add(notiModel);
                    }
                    notificationAdapter = new NotificationAdapter(getActivity(), notificationList);
                    notiRecycler.setAdapter(notificationAdapter);

                    Collections.reverse(notificationList);
                    notiLoader.setVisibility(View.GONE);
                    notificationAdapter.notifyDataSetChanged();
                }else {
                    Toast.makeText(getActivity(), "You have no notifications", Toast.LENGTH_SHORT).show();
                    notiLoader.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }
}