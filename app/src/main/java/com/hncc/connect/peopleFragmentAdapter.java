package com.hncc.connect;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class peopleFragmentAdapter extends RecyclerView.Adapter {

    ArrayList<person> peopleFragmentList;

    public peopleFragmentAdapter(ArrayList<person> peopleFragmentList) {
        this.peopleFragmentList = peopleFragmentList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_people, parent, false);
        return new PeopleHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        PeopleHolder peopleHolder = (PeopleHolder)holder;

        final person people = peopleFragmentList.get(position);

        // Set the data to the views here
        peopleHolder.setPeopleName(people.getName());
        peopleHolder.setFollowersNumber(people.getFollowers());

        peopleHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(view.getContext(),PeopleProfileActivity.class);
                profileIntent.putExtra("visit_user_id",people.getUid());
                view.getContext().startActivity(profileIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return peopleFragmentList == null? 0: peopleFragmentList.size();
    }


    public class PeopleHolder extends RecyclerView.ViewHolder {

        private TextView txtName;
        private TextView txtFollowersNumber;

        public PeopleHolder(View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.each_people_name_text_view);
            txtFollowersNumber = itemView.findViewById(R.id.each_people_followers_text_view);

        }

        public void setPeopleName(String name) {
            txtName.setText(name);
        }

        public void setFollowersNumber(int number) {
            txtFollowersNumber.setText("Followers: "+ number);
        }
    }

}
