package com.makepe.curiosityhubls.Fragments.MaterialFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.makepe.curiosityhubls.Adapters.MaterialAdapter;
import com.makepe.curiosityhubls.Models.Material;
import com.makepe.curiosityhubls.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class JC_Fragment extends Fragment {

    TabLayout JC_CLASSES;

    RecyclerView formA_Recycler, formB_Recycler, formC_Recycler;
    MaterialAdapter materialAdapter;
    List<Material> materialList;

    DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.recycler_fragment, container, false);

        formA_Recycler = view.findViewById(R.id.FormA_Recycler);
        formB_Recycler = view.findViewById(R.id.FormB_Recycler);
        formC_Recycler = view.findViewById(R.id.FormC_Recycler);
        JC_CLASSES = view.findViewById(R.id.JC_Classes);

        JC_CLASSES.setVisibility(View.VISIBLE);
        formA_Recycler.setVisibility(View.VISIBLE);

        String activeSub = getArguments().getString("subject_name");

        String selectedClass = "Form A";

        formA_Recycler.setHasFixedSize(true);
        formA_Recycler.hasFixedSize();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        formA_Recycler.setLayoutManager(layoutManager);

        materialList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Subjects").child(activeSub);
        Query query = reference.orderByChild("grade").equalTo(selectedClass);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    materialList.clear();

                    for(DataSnapshot ds : snapshot.getChildren()){
                        Material material = ds.getValue(Material.class);
                        materialList.add(material);

                        materialAdapter = new MaterialAdapter(getContext(), materialList);
                        formA_Recycler.setAdapter(materialAdapter);

                    }

                    materialAdapter.notifyDataSetChanged();

                }catch(NullPointerException ignored){}

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        JC_CLASSES.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0:
                        String selectedClass = "Form A";

                        formA_Recycler.setVisibility(View.VISIBLE);

                        formA_Recycler.setHasFixedSize(true);
                        formA_Recycler.hasFixedSize();

                        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                        formA_Recycler.setLayoutManager(layoutManager);

                        materialList = new ArrayList<>();

                        reference = FirebaseDatabase.getInstance().getReference("Subjects").child(activeSub);
                        Query query = reference.orderByChild("grade").equalTo(selectedClass);

                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                try{
                                    materialList.clear();

                                    for(DataSnapshot ds : snapshot.getChildren()){
                                        Material material = ds.getValue(Material.class);
                                        materialList.add(material);

                                        materialAdapter = new MaterialAdapter(getContext(), materialList);
                                        formA_Recycler.setAdapter(materialAdapter);

                                    }

                                    materialAdapter.notifyDataSetChanged();

                                }catch(NullPointerException ignored){}

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        break;
                    case 1:
                        String gradeSelected = "Form B";
                        formB_Recycler.setVisibility(View.VISIBLE);

                        formB_Recycler.setHasFixedSize(true);
                        formB_Recycler.hasFixedSize();

                        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getActivity());
                        formB_Recycler.setLayoutManager(layoutManager1);

                        materialList = new ArrayList<>();

                        reference = FirebaseDatabase.getInstance().getReference("Subjects").child(activeSub);
                        Query query1 = reference.orderByChild("grade").equalTo(gradeSelected);

                        query1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                try{
                                    materialList.clear();

                                    for(DataSnapshot ds : snapshot.getChildren()){
                                        Material material = ds.getValue(Material.class);
                                        materialList.add(material);

                                        materialAdapter = new MaterialAdapter(getContext(), materialList);
                                        formB_Recycler.setAdapter(materialAdapter);

                                    }

                                    materialAdapter.notifyDataSetChanged();

                                }catch(NullPointerException ignored){}

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        break;
                    case 2:
                        String gradeChosen = "Form C";

                        formB_Recycler.setVisibility(View.VISIBLE);

                        formB_Recycler.setHasFixedSize(true);
                        formB_Recycler.hasFixedSize();

                        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getActivity());
                        formB_Recycler.setLayoutManager(layoutManager2);

                        materialList = new ArrayList<>();

                        reference = FirebaseDatabase.getInstance().getReference("Subjects").child(activeSub);
                        Query query2 = reference.orderByChild("grade").equalTo(gradeChosen);

                        query2.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                try{
                                    materialList.clear();

                                    for(DataSnapshot ds : snapshot.getChildren()){
                                        Material material = ds.getValue(Material.class);
                                        materialList.add(material);

                                        materialAdapter = new MaterialAdapter(getContext(), materialList);
                                        formB_Recycler.setAdapter(materialAdapter);

                                    }

                                    materialAdapter.notifyDataSetChanged();

                                }catch(NullPointerException ignored){}

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        break;

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0:
                        formA_Recycler.setVisibility(View.GONE);
                        break;
                    case 1:
                        formB_Recycler.setVisibility(View.GONE);
                        break;
                    case 2:
                        formC_Recycler.setVisibility(View.GONE);
                        break;

                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return view;
    }
}