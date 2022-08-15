package com.makepe.curiosityhubls.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.makepe.curiosityhubls.Adapters.SubjectAdapter;
import com.makepe.curiosityhubls.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MaterialFragment extends Fragment {

    RecyclerView subjectsRecycler;
    SubjectAdapter subjectAdapter;
    List<String> subjectsList;

    private DatabaseReference reference;
    ProgressBar materialLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_material, container, false);

        subjectsRecycler = view.findViewById(R.id.materialRecycler);
        materialLoader = view.findViewById(R.id.materialLoader);

        subjectsRecycler.setHasFixedSize(true);
        subjectsRecycler.hasFixedSize();
        subjectsRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2));

        subjectsList = new ArrayList<String>();

        reference = FirebaseDatabase.getInstance().getReference("Subjects");

        Query query = reference.orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                subjectsList.clear();
                if(snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String cm = ds.getKey();
                        subjectsList.add(cm);
                    }

                    subjectAdapter = new SubjectAdapter(getContext(), subjectsList);
                    subjectsRecycler.setAdapter(subjectAdapter);
                    materialLoader.setVisibility(View.GONE);
                }else {
                    Toast.makeText(getActivity(), "Theres is no material uploaded", Toast.LENGTH_SHORT).show();
                    materialLoader.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }
}