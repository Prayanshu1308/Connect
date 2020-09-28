package com.hncc.connect;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.hdodenhof.circleimageview.CircleImageView;


public class HomeFragment extends Fragment {

    private RecyclerView homeRecyclerView;
    private DatabaseReference postsRef;
    private View homeFragmentView;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private ArrayList<String> myFollowingIds;
    ArrayList<feed> homeFragmentList;
    homeFragmentAdapter homeFragmentAdapter;

    public HomeFragment() {
        //Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        homeFragmentView = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser==null){
            Intent loginIntent = new Intent(getContext(), LoginActivity.class);
            startActivity(loginIntent);
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
        }else {

            homeRecyclerView = homeFragmentView.findViewById(R.id.homeRecyclerView);
            homeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            homeFragmentList = new ArrayList<>();

            postsRef = FirebaseDatabase.getInstance().getReference().child("posts");
            currentUserID = mAuth.getCurrentUser().getUid();

            InitializeArrayList();

            postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for (DataSnapshot ds : snapshot.getChildren()) {
                        if (ds.hasChild("postProfileImage") && ds.hasChild("userName") && ds.hasChild("dateAndTime") &&
                                ds.hasChild("postMessageText") && ds.hasChild("likes") && ds.hasChild("uid") &&
                                ds.hasChild("postPushID")) {
                            String profileImageUrl = ds.child("postProfileImage").getValue().toString();
                            String profileName = ds.child("userName").getValue().toString();
                            String postDateAndTime = ds.child("dateAndTime").getValue().toString();
                            String postImageUrl;
                            if (ds.hasChild("postMessageImage")) {
                                postImageUrl = ds.child("postMessageImage").getValue().toString();
                            } else {
                                postImageUrl = "no_post_img";
                            }
                            String postMessage = ds.child("postMessageText").getValue().toString();
                            String postLikes = ds.child("likes").getValue().toString();
                            String postUid = ds.child("uid").getValue().toString();
                            String postPushId = ds.child("postPushID").getValue().toString();
                            if (myFollowingIds.contains(postUid)) {
                                feed eachFeed = new feed(profileImageUrl, profileName, postDateAndTime, postImageUrl, postMessage, postLikes, postUid, postPushId);
                                homeFragmentList.add(eachFeed);
                            }
                        }
                    }

                    Comparator<feed> compareByDateAndTime = new Comparator<feed>() {
                        @Override
                        public int compare(feed f1, feed f2) {
                            return f2.getPostDateAndTime().compareToIgnoreCase(f1.getPostDateAndTime());
                        }
                    };
                    Collections.sort(homeFragmentList, compareByDateAndTime);

                    homeFragmentAdapter = new homeFragmentAdapter(homeFragmentList, getContext());
                    homeRecyclerView.setAdapter(homeFragmentAdapter);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }




        return homeFragmentView;
    }


    private void InitializeArrayList() {
        myFollowingIds = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("users")
                .child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("followingIds")){
                    DataSnapshot idsSnapshot = snapshot.child("followingIds");
                    Iterable<DataSnapshot> followingIdsChildren = idsSnapshot.getChildren();
                    for(DataSnapshot id : followingIdsChildren){
                        String data = id.getValue().toString();
                        myFollowingIds.add(data);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}