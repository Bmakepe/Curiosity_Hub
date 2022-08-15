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


public class PSLE_Fragment extends Fragment {

    TabLayout PSLE_CLASSES;

    RecyclerView class1_Recycler, class2_Recycler, class3_Recycler, class4_Recycler, class5_Recycler, class6_Recycler, class7_Recycler;
    MaterialAdapter materialAdapter;
    List<Material> materialList;

    DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.recycler_fragment, container, false);

        class1_Recycler = view.findViewById(R.id.FormA_Recycler);
        class2_Recycler = view.findViewById(R.id.FormB_Recycler);
        class3_Recycler = view.findViewById(R.id.FormC_Recycler);
        class4_Recycler = view.findViewById(R.id.FormD_Recycler);
        class5_Recycler = view.findViewById(R.id.FormE_Recycler);
        class6_Recycler = view.findViewById(R.id.FormG_Recycler);
        class7_Recycler = view.findViewById(R.id.subjectRecycler);

        PSLE_CLASSES = view.findViewById(R.id.PSLE_Classes);

        PSLE_CLASSES.setVisibility(View.VISIBLE);
        class1_Recycler.setVisibility(View.VISIBLE);

        String activeSub = getArguments().getString("subject_name");
        String tabTitle = getArguments().getString("tabTitle");

        String selectedClass = "Class 1";

        class1_Recycler.setHasFixedSize(true);
        class1_Recycler.hasFixedSize();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        class1_Recycler.setLayoutManager(layoutManager);

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
                        class1_Recycler.setAdapter(materialAdapter);

                    }

                    materialAdapter.notifyDataSetChanged();

                }catch(NullPointerException ignored){
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        PSLE_CLASSES.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0:
                        String selectedClass = "Class 1";

                        class1_Recycler.setVisibility(View.VISIBLE);

                        class1_Recycler.setHasFixedSize(true);
                        class1_Recycler.hasFixedSize();

                        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                        class1_Recycler.setLayoutManager(layoutManager);

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
                                        class1_Recycler.setAdapter(materialAdapter);

                                    }

                                    materialAdapter.notifyDataSetChanged();

                                }catch(NullPointerException ignored){
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        break;
                    case 1:
                        String chosenClass = "Class 2";

                        class2_Recycler.setVisibility(View.VISIBLE);

                        class2_Recycler.setHasFixedSize(true);
                        class2_Recycler.hasFixedSize();

                        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getActivity());
                        class2_Recycler.setLayoutManager(layoutManager1);

                        materialList = new ArrayList<>();

                        reference = FirebaseDatabase.getInstance().getReference("Subjects").child(activeSub);
                        Query query1 = reference.orderByChild("grade").equalTo(chosenClass);

                        query1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                try{
                                    materialList.clear();

                                    for(DataSnapshot ds : snapshot.getChildren()){
                                        Material material = ds.getValue(Material.class);
                                        materialList.add(material);

                                        materialAdapter = new MaterialAdapter(getContext(), materialList);
                                        class2_Recycler.setAdapter(materialAdapter);

                                    }

                                    materialAdapter.notifyDataSetChanged();

                                }catch(NullPointerException ignored){
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        break;
                    case 2:
                        String classChoice = "Class 3";

                        class3_Recycler.setVisibility(View.VISIBLE);

                        class3_Recycler.setHasFixedSize(true);
                        class3_Recycler.hasFixedSize();

                        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getActivity());
                        class3_Recycler.setLayoutManager(layoutManager2);

                        materialList = new ArrayList<>();

                        reference = FirebaseDatabase.getInstance().getReference("Subjects").child(activeSub);
                        Query query2 = reference.orderByChild("grade").equalTo(classChoice);

                        query2.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                try{
                                    materialList.clear();

                                    for(DataSnapshot ds : snapshot.getChildren()){
                                        Material material = ds.getValue(Material.class);
                                        materialList.add(material);

                                        materialAdapter = new MaterialAdapter(getContext(), materialList);
                                        class3_Recycler.setAdapter(materialAdapter);

                                    }

                                    materialAdapter.notifyDataSetChanged();

                                }catch(NullPointerException ignored){}

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        break;
                    case 3:
                        String preferredClass = "Class 4";

                        class4_Recycler.setVisibility(View.VISIBLE);

                        class4_Recycler.setHasFixedSize(true);
                        class4_Recycler.hasFixedSize();

                        LinearLayoutManager layoutManager3 = new LinearLayoutManager(getActivity());
                        class4_Recycler.setLayoutManager(layoutManager3);

                        materialList = new ArrayList<>();

                        reference = FirebaseDatabase.getInstance().getReference("Subjects").child(activeSub);
                        Query query3 = reference.orderByChild("grade").equalTo(preferredClass);

                        query3.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                try{
                                    materialList.clear();

                                    for(DataSnapshot ds : snapshot.getChildren()){
                                        Material material = ds.getValue(Material.class);
                                        materialList.add(material);

                                        materialAdapter = new MaterialAdapter(getContext(), materialList);
                                        class4_Recycler.setAdapter(materialAdapter);

                                    }

                                    materialAdapter.notifyDataSetChanged();

                                }catch(NullPointerException ignored){}

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        break;
                    case 4:
                        String pickedClass = "Class 5";

                        class5_Recycler.setVisibility(View.VISIBLE);

                        class5_Recycler.setHasFixedSize(true);
                        class5_Recycler.hasFixedSize();

                        LinearLayoutManager layoutManager4 = new LinearLayoutManager(getActivity());
                        class5_Recycler.setLayoutManager(layoutManager4);

                        materialList = new ArrayList<>();

                        reference = FirebaseDatabase.getInstance().getReference("Subjects").child(activeSub);
                        Query query4 = reference.orderByChild("grade").equalTo(pickedClass);

                        query4.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                try{
                                    materialList.clear();

                                    for(DataSnapshot ds : snapshot.getChildren()){
                                        Material material = ds.getValue(Material.class);
                                        materialList.add(material);

                                        materialAdapter = new MaterialAdapter(getContext(), materialList);
                                        class5_Recycler.setAdapter(materialAdapter);

                                    }

                                    materialAdapter.notifyDataSetChanged();

                                }catch(NullPointerException ignored){}

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        break;
                    case 5:
                        String specialClass = "Class 6";

                        class6_Recycler.setVisibility(View.VISIBLE);

                        class6_Recycler.setHasFixedSize(true);
                        class6_Recycler.hasFixedSize();

                        LinearLayoutManager layoutManager5 = new LinearLayoutManager(getActivity());
                        class6_Recycler.setLayoutManager(layoutManager5);

                        materialList = new ArrayList<>();

                        reference = FirebaseDatabase.getInstance().getReference("Subjects").child(activeSub);
                        Query query5 = reference.orderByChild("grade").equalTo(specialClass);

                        query5.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                try{
                                    materialList.clear();

                                    for(DataSnapshot ds : snapshot.getChildren()){
                                        Material material = ds.getValue(Material.class);
                                        materialList.add(material);

                                        materialAdapter = new MaterialAdapter(getContext(), materialList);
                                        class6_Recycler.setAdapter(materialAdapter);

                                    }

                                    materialAdapter.notifyDataSetChanged();

                                }catch(NullPointerException ignored){}

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        break;
                    case 6:
                        String primeClass = "Class 7";

                        class7_Recycler.setVisibility(View.VISIBLE);

                        class7_Recycler.setHasFixedSize(true);
                        class7_Recycler.hasFixedSize();

                        LinearLayoutManager layoutManager6 = new LinearLayoutManager(getActivity());
                        class7_Recycler.setLayoutManager(layoutManager6);

                        materialList = new ArrayList<>();

                        reference = FirebaseDatabase.getInstance().getReference("Subjects").child(activeSub);
                        Query query6 = reference.orderByChild("grade").equalTo(primeClass);

                        query6.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                try{
                                    materialList.clear();

                                    for(DataSnapshot ds : snapshot.getChildren()){
                                        Material material = ds.getValue(Material.class);
                                        materialList.add(material);

                                        materialAdapter = new MaterialAdapter(getContext(), materialList);
                                        class7_Recycler.setAdapter(materialAdapter);

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
                        class1_Recycler.setVisibility(View.GONE);
                        break;
                    case 1:
                        class2_Recycler.setVisibility(View.GONE);
                        break;
                    case 2:
                        class3_Recycler.setVisibility(View.GONE);
                        break;
                    case 3:
                        class4_Recycler.setVisibility(View.GONE);
                        break;
                    case 4:
                        class5_Recycler.setVisibility(View.GONE);
                        break;
                    case 5:
                        class6_Recycler.setVisibility(View.GONE);
                        break;
                    case 6:
                        class7_Recycler.setVisibility(View.GONE);
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