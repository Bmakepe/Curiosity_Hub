package com.makepe.curiosityhubls.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.makepe.curiosityhubls.Adapters.SchoolsAdapter;
import com.makepe.curiosityhubls.Models.Schools;
import com.makepe.curiosityhubls.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SchoolFragment extends Fragment {

    RecyclerView schoolsRecycler;
    List<Schools> schoolsList;
    SchoolsAdapter schoolsAdapter;

    DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_school, container, false);

        schoolsRecycler = view.findViewById(R.id.SchoolsRecycler);

        schoolsRecycler.setHasFixedSize(true);
        schoolsRecycler.hasFixedSize();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        schoolsRecycler.setLayoutManager(layoutManager);

        schoolsList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Schools");
        Query query = reference.orderByChild("school");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                schoolsList.clear();

                for(DataSnapshot ds : snapshot.getChildren()){
                    Schools schools = ds.getValue(Schools.class);
                    schoolsList.add(schools);

                    schoolsAdapter = new SchoolsAdapter(getContext(), schoolsList);
                    schoolsRecycler.setAdapter(schoolsAdapter);
                }

                schoolsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }
}