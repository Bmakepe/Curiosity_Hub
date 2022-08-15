package com.makepe.curiosityhubls.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.database.Query;
import com.google.protobuf.StringValue;
import com.makepe.curiosityhubls.Adapters.PostAdapter;
import com.makepe.curiosityhubls.Adapters.RecyclerAdapter;
import com.makepe.curiosityhubls.CustomClasses.PaginationListener;
import com.makepe.curiosityhubls.Models.PostModel;
import com.makepe.curiosityhubls.Models.User;
import com.makepe.curiosityhubls.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.ButterKnife;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.makepe.curiosityhubls.CustomClasses.PaginationListener.PAGE_START;

public class HomeFragment extends Fragment{


    private RecyclerView postRecycler;
    private ArrayList<PostModel> postList;
    private PostAdapter adapterPost;

    //for followers
    private List<String> followingList;

    private List<String> tutorIDs;

    FirebaseUser firebaseUser;
    ProgressBar homeLoader;

    public static final int ITEM_PER_AD = 6;
    public static final String BANNER_AD_ID = "ca-app-pub-2193593343976198/8383166486";
    public static final String ITEM_ADMOB_ID = "ca-app-pub-2193593343976198~1171740936";

    private List<Object> recyclerItems = new ArrayList<>();
    RecyclerAdapter adapter;

    String gradeLevel;

    DatabaseReference reference;

    SwipeRefreshLayout refreshLayout;
    private int currentPage = PAGE_START;
    private boolean isLastPage = false;
    private int totalPage = 10;
    private boolean isLoading = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ButterKnife.bind(getActivity());

        postRecycler = view.findViewById(R.id.home_recycler);
        homeLoader = view.findViewById(R.id.homeLoader);
        refreshLayout = view.findViewById(R.id.homeRefresher);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        doApiCall();
                        refreshLayout.setRefreshing(false);
                    }
                }, 5000);

            }
        });

        refreshLayout.setColorSchemeResources(
                android.R.color.holo_green_dark,
                android.R.color.holo_green_light,
                R.color.identity,
                R.color.identity_dark
        );

        //MobileAds.initialize(getActivity(), "ca-app-pub-3940256099942544~3347511713");
        //MobileAds.initialize(getContext(), ITEM_ADMOB_ID);

        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {

            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Students");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);

                    try{
                        if(user.getUserId().equals(firebaseUser.getUid())){
                            gradeLevel = user.getGrade();
                        }
                    }catch (NullPointerException ignored){}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        postList = new ArrayList<>();

        checkFollowing();

        postRecycler.setHasFixedSize(true);
        postRecycler.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postRecycler.setLayoutManager(layoutManager);

        adapterPost = new PostAdapter(getActivity(), postList);
        postRecycler.setAdapter(adapterPost);

        getBannerAds();
        loadBannerAds();

        postRecycler.addOnScrollListener(new PaginationListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage++;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(!isLastItemDisplayed(postRecycler)){
                    refreshLayout.setEnabled(false);
                }else{
                    refreshLayout.setEnabled(true);
                }

            }
        });


        return view;
    }

    private void checkFollowing(){
        followingList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("following");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    followingList.add(snapshot.getKey());
                }

                getTutors();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "there was an error with loading posts of people you follow ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTutors() {
        tutorIDs = new ArrayList<>();

        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("Students");
        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tutorIDs.clear();
                try{
                    for(DataSnapshot ds : snapshot.getChildren()){
                        User user = ds.getValue(User.class);
                        assert user != null;

                        if(gradeLevel.equals("Class 1") | gradeLevel.equals("Class 2") | gradeLevel.equals("Class 3")
                                | gradeLevel.equals("Class 4") | gradeLevel.equals("Class 5") | gradeLevel.equals("Class 6") | gradeLevel.equals("Class 7")){

                            if (user.getRole().equals("PSLE Tutor")) {
                                tutorIDs.add(user.getUserId());
                            }

                        }else if(gradeLevel.equals("Form A") | gradeLevel.equals("Form B") | gradeLevel.equals("Form C")){

                            if (user.getRole().equals("JC Tutor")) {
                                tutorIDs.add(user.getUserId());
                            }

                        }else if(gradeLevel.equals("Form D") | gradeLevel.equals("Form E")){

                            if (user.getRole().equals("COSC Tutor") | user.getRole().equals("LGCSE Tutor")) {
                                tutorIDs.add(user.getUserId());
                            }

                        }else if(gradeLevel.equals("For Parents") | gradeLevel.equals("For Teachers") | gradeLevel.equals("Not Applicable")){

                            if(!user.getRole().equals("Student"))
                                tutorIDs.add(user.getUserId());

                        }else if(gradeLevel.equals("ECCD")){

                            if(user.getRole().equals("ECCD Tutor")){
                                tutorIDs.add(user.getUserId());
                            }
                        }

                    }
                }catch (NullPointerException ignored){}

                loadPosts();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "There was an error loading tutors", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPosts() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.keepSynced(true);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                try{
                    if(dataSnapshot.exists()) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            PostModel modelPost = ds.getValue(PostModel.class);

                            for (String id : followingList) {
                                assert modelPost != null;
                                if (modelPost.getUid().equals(id)) {
                                    postList.add(modelPost);
                                }
                            }
                            for (String ID : tutorIDs) {
                                assert modelPost != null;
                                if (modelPost.getUid().equals(ID)) {
                                    postList.add(modelPost);
                                }
                            }

                            assert modelPost != null;
                            if (modelPost.getUid().equals(firebaseUser.getUid())) {
                                postList.add(modelPost);
                            }

                            // adapter = new RecyclerAdapter(recyclerItems, postList, getActivity());
                            // postRecycler.setAdapter(adapter);

                            refreshLayout.setRefreshing(false);
                            homeLoader.setVisibility(View.GONE);
                        }
                    }else {
                        homeLoader.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "No Posts Are Available", Toast.LENGTH_SHORT).show();
                    }
                }catch (NullPointerException ignored){}
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "There was an error loading required posts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getBannerAds(){
        recyclerItems.clear();

        for(int i = 0; i < recyclerItems.size(); i += ITEM_PER_AD){
            final AdView adView = new AdView(getActivity());
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(BANNER_AD_ID);
            recyclerItems.add(i, adView);

        }

    }

    private void loadBannerAds(){
        for(int i = 0; i< recyclerItems.size(); i++){
            Object item = recyclerItems.get(i);

            if(item instanceof AdView){
                final AdView adView = (AdView) item;
                adView.loadAd(new AdRequest.Builder().build());
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void doApiCall() {

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (refreshLayout != null){
                    refreshLayout.setRefreshing(true);
                }
                loadPosts();
                adapterPost.notifyDataSetChanged();
            }
        }, 1500);
    }

    private boolean isLastItemDisplayed(RecyclerView recyclerView){
        if(recyclerView.getAdapter().getItemCount() != 0){
            int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            if(lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == recyclerView.getAdapter().getItemCount() - 1){
                return true;
            }
        }
        return false;
    }
}
