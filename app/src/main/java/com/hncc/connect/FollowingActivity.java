package com.hncc.connect;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FollowingActivity extends AppCompatActivity {

    private RecyclerView followingsRecyclerView;
    private ArrayList<String> myFollowingIds;
    private String peopleProfileUserId;
    private DatabaseReference usersRef;
    private ArrayList<person> followingList;
    private peopleFragmentAdapter followingActivityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);

        Intent intent = getIntent();
        peopleProfileUserId = intent.getExtras().get("following_user_id").toString();

        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        InitializeFields();
        followingList = new ArrayList<>();
        followingsRecyclerView = findViewById(R.id.followingsRecyclerView);
        followingsRecyclerView.setLayoutManager(new LinearLayoutManager(FollowingActivity.this));

        InitializeArrayList(peopleProfileUserId);


        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds : snapshot.getChildren()){
                    String userName = ds.child("name").getValue().toString();
                    int userFollowers = Integer.parseInt(ds.child("followers").getValue().toString());
                    String userUid = ds.child("uid").getValue().toString();
                    if(myFollowingIds.contains(userUid)){
                        person eachPerson = new person(userName, userFollowers, userUid);
                        followingList.add(eachPerson);
                    }

                }

                Comparator<person> compareById = new Comparator<person>() {
                    @Override
                    public int compare(person p1, person p2) {
                        return p1.getName().toLowerCase().compareTo(p2.getName().toLowerCase());
                    }
                };
                Collections.sort(followingList, compareById);

                /////////
                followingActivityAdapter = new peopleFragmentAdapter(followingList);
                followingsRecyclerView.setAdapter(followingActivityAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    private void InitializeArrayList(String peopleProfileUserId) {

        myFollowingIds = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("users")
                .child(peopleProfileUserId).addListenerForSingleValueEvent(new ValueEventListener() {
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


    private void InitializeFields() {

        Toolbar followingsToolbar = findViewById(R.id.followings_toolbar);
        setSupportActionBar(followingsToolbar);
        getSupportActionBar().setTitle("Followings");

    }
}