package com.hncc.connect;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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

public class PostsActivity extends AppCompatActivity {

    private Toolbar MyPostToolbar;
    //private FirebaseAuth mAuth;
    private String currentUserId;
    //private String whosePostsUserId;
    private DatabaseReference postsRef;
    private RecyclerView myPostsRecyclerView;
    ArrayList<feed> postsActivityList;
    homeFragmentAdapter myPostsActivityAdapter;

    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

//        Intent intent = getIntent();
//        whosePostsUserId = intent.getExtras().get("userId").toString();
//        InitializeFields();

        MyPostToolbar = findViewById(R.id.my_post_toolbar);
        setSupportActionBar(MyPostToolbar);
        getSupportActionBar().setTitle("My Posts");

        myPostsRecyclerView = findViewById(R.id.myPostsRecyclerView);
        myPostsRecyclerView.setLayoutManager(new LinearLayoutManager(PostsActivity.this));

        postsActivityList = new ArrayList<>();

        builder = new AlertDialog.Builder(PostsActivity.this, R.style.AlertDialogTheme);

        postsRef = FirebaseDatabase.getInstance().getReference().child("posts");



        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds : snapshot.getChildren()){
                    if(ds.hasChild("postProfileImage") && ds.hasChild("userName") && ds.hasChild("dateAndTime") &&
                            ds.hasChild("postMessageText") && ds.hasChild("likes") && ds.hasChild("uid") &&
                            ds.hasChild("postPushID")) {
                        String uidOfPost = ds.child("uid").getValue().toString();
                        if(uidOfPost.equals(currentUserId)) {
                            String profileImageUrl = ds.child("postProfileImage").getValue().toString();
                            String profileName = ds.child("userName").getValue().toString();
                            String postDateAndTime = ds.child("dateAndTime").getValue().toString();
                            String postImageUrl;
                            if (ds.hasChild("postMessageImage")) {
                                postImageUrl = ds.child("postMessageImage").getValue().toString();
                            }else {
                                postImageUrl = "no_post_img";
                            }
                            String postMessage = ds.child("postMessageText").getValue().toString();
                            String postLikes = ds.child("likes").getValue().toString();
                            String postUid = ds.child("uid").getValue().toString();
                            String postPushId = ds.child("postPushID").getValue().toString();

                            feed eachFeed = new feed(profileImageUrl, profileName, postDateAndTime, postImageUrl, postMessage, postLikes, postUid, postPushId);
                            postsActivityList.add(eachFeed);
                        }
                    }
                }

                Comparator<feed> compareByDateAndTime = new Comparator<feed>() {
                    @Override
                    public int compare(feed f1, feed f2) {
                        return f2.getPostDateAndTime().compareToIgnoreCase(f1.getPostDateAndTime());
                    }
                };
                Collections.sort(postsActivityList, compareByDateAndTime);

                myPostsActivityAdapter = new homeFragmentAdapter(postsActivityList, PostsActivity.this);
                myPostsRecyclerView.setAdapter(myPostsActivityAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

}