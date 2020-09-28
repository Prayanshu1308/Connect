package com.hncc.connect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class PeopleFragment extends Fragment {

    ArrayList<person> peopleFragmentList;
    RecyclerView peopleRecyclerView;
    peopleFragmentAdapter peopleFragmentAdapter;

    DatabaseReference usersRef;

    public PeopleFragment() {
        //Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View peopleFragmentView = inflater.inflate(R.layout.fragment_people, container, false);

        peopleRecyclerView = peopleFragmentView.findViewById(R.id.peopleRecyclerView);
        peopleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        peopleFragmentList = new ArrayList<>();

        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds : snapshot.getChildren()){
                    String userName = ds.child("name").getValue().toString();
                    int userFollowers = Integer.parseInt(ds.child("followers").getValue().toString());
                    String userUid = ds.child("uid").getValue().toString();
                    person eachPerson = new person(userName, userFollowers, userUid);
                    peopleFragmentList.add(eachPerson);
                }

                Comparator<person> compareById = new Comparator<person>() {
                    @Override
                    public int compare(person p1, person p2) {
                        return p1.getName().toLowerCase().compareTo(p2.getName().toLowerCase());
                    }
                };
                Collections.sort(peopleFragmentList, compareById);

                peopleFragmentAdapter = new peopleFragmentAdapter(peopleFragmentList);
                peopleRecyclerView.setAdapter(peopleFragmentAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return peopleFragmentView;
    }


}