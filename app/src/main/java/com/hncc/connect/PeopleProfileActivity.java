package com.hncc.connect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PeopleProfileActivity extends AppCompatActivity {

    private CircleImageView peopleProfileImage;
    private TextView peopleProfileName, peopleProfileAbout, peopleProfileFollowers, peopleProfileFollowing, peopleProfilePosts;
    private Button peopleProfileFollowButton, peopleProfileUnfollowButton;
    private RelativeLayout postsRelativeLayout, followingRelativeLayout;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private String peopleProfileUserId, currentUserId;
    private int myFollowingNum = -1, peopleProfileFollowersNum = -1;
    private String retrieveProfileImage = "no_img";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_profile);

        Intent intent = getIntent();
        peopleProfileUserId= intent.getExtras().get("visit_user_id").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    myFollowingNum = Integer.parseInt(snapshot.child("following").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        
        InitializeFields();

        RetrieveUserProfileInfo();

        peopleProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(retrieveProfileImage.equals("no_img")){
                    Toast.makeText(PeopleProfileActivity.this,"No profile image available", Toast.LENGTH_SHORT).show();
                }else {
                    Intent imageViewerIntent = new Intent(PeopleProfileActivity.this, ImageViewerActivity.class);
                    imageViewerIntent.putExtra("imageUrl", retrieveProfileImage);
                    startActivity(imageViewerIntent);
                }
            }
        });

        followingRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent followingIntent = new Intent(PeopleProfileActivity.this, FollowingActivity.class);
                followingIntent.putExtra("following_user_id", peopleProfileUserId);
                startActivity(followingIntent);
            }
        });

//        postsRelativeLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent postsIntent = new Intent(PeopleProfileActivity.this, PostsActivity.class);
//                postsIntent.putExtra("userId", peopleProfileUserId);
//                startActivity(postsIntent);
//            }
//        });

        peopleProfileFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                peopleProfileFollowButton.setVisibility(View.GONE);
                peopleProfileUnfollowButton.setVisibility(View.VISIBLE);
                FollowUser(peopleProfileUserId);
            }
        });

        peopleProfileUnfollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                peopleProfileUnfollowButton.setVisibility(View.GONE);
                peopleProfileFollowButton.setVisibility(View.VISIBLE);
                UnfollowUser(peopleProfileUserId);
            }
        });

    }

    private void UnfollowUser(String peopleProfileUserId) {
        usersRef.child(currentUserId).child("followingIds").child(peopleProfileUserId).removeValue();
        usersRef.child(peopleProfileUserId).child("followersIds").child(currentUserId).removeValue();
        // 1. Decrease number of followers of peopleProfileUserId by one
        // 2. Decrease number of following of currentUserId by one
        peopleProfileFollowersNum = peopleProfileFollowersNum - 1;
        myFollowingNum = myFollowingNum - 1;
        usersRef.child(peopleProfileUserId).child("followers").setValue(peopleProfileFollowersNum);
        usersRef.child(currentUserId).child("following").setValue(myFollowingNum);
    }


    private void FollowUser(String peopleProfileUserId) {
        usersRef.child(currentUserId).child("followingIds").child(peopleProfileUserId).setValue(peopleProfileUserId);
        usersRef.child(peopleProfileUserId).child("followersIds").child(currentUserId).setValue(currentUserId);
        // 1. Increase number of followers of peopleProfileUserId by one
        // 2. Increase number of following of currentUserId by one
        peopleProfileFollowersNum = peopleProfileFollowersNum + 1;
        myFollowingNum = myFollowingNum + 1;
        usersRef.child(peopleProfileUserId).child("followers").setValue(peopleProfileFollowersNum);
        usersRef.child(currentUserId).child("following").setValue(myFollowingNum);

    }


    private void RetrieveUserProfileInfo() {

        usersRef.child(peopleProfileUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.exists()) && (snapshot.hasChild("image"))){
                    retrieveProfileImage = snapshot.child("image").getValue().toString();
                    Picasso.get().load(retrieveProfileImage).into(peopleProfileImage);
                }
                if(snapshot.exists()) {
                    peopleProfileFollowersNum = Integer.parseInt(snapshot.child("followers").getValue().toString());
                    String name = snapshot.child("name").getValue().toString();
                    peopleProfileName.setText(name);
                    String about = snapshot.child("about").getValue().toString();
                    peopleProfileAbout.setText(about);
                    String followers = snapshot.child("followers").getValue().toString();
                    peopleProfileFollowers.setText(followers);
                    String following = snapshot.child("following").getValue().toString();
                    peopleProfileFollowing.setText(following);
                    String posts = snapshot.child("posts").getValue().toString();
                    peopleProfilePosts.setText(posts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(!currentUserId.equals(peopleProfileUserId)) {
            usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if ((snapshot.exists()) && (snapshot.hasChild("followingIds"))) {
                        if (snapshot.child("followingIds").hasChild(peopleProfileUserId)) {
                            peopleProfileUnfollowButton.setVisibility(View.VISIBLE);
                        } else {
                            peopleProfileFollowButton.setVisibility(View.VISIBLE);
                        }
                    } else {
                        peopleProfileFollowButton.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    private void InitializeFields() {
        peopleProfileImage = findViewById(R.id.people_profile_user_image);
        peopleProfileName = findViewById(R.id.people_profile_user_name);
        peopleProfileAbout = findViewById(R.id.people_profile_user_about);
        peopleProfileFollowers = findViewById(R.id.people_profile_followers_no_text_view);
        peopleProfileFollowing = findViewById(R.id.people_profile_following_no_text_view);
        peopleProfilePosts = findViewById(R.id.people_profile_posts_no_text_view);
        peopleProfileFollowButton = findViewById(R.id.people_profile_follow_button);
        peopleProfileUnfollowButton = findViewById(R.id.people_profile_unfollow_button);
        postsRelativeLayout = findViewById(R.id.people_profile_posts_relative_layout);
        followingRelativeLayout = findViewById(R.id.people_profile_following_relative_layout);
    }
}
