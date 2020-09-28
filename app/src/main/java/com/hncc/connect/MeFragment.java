package com.hncc.connect;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class MeFragment extends Fragment {

    private FirebaseAuth mAuth;
    public View meFragmentView;
    private CircleImageView myProfileImage;
    public TextView logout, settings, myUsername, myAbout, myFollowersNo, myFollowingNo, myPostsNo, createPost, connectionMessage;
    private RelativeLayout postsRelativeLayout,followingRelativeLayout;
    private DatabaseReference usersRef;
    private String currentUserId;
    private String retrieveMyProfileImage = "no_img";
    private String name;
    private AlertDialog.Builder builder;

    public MeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        meFragmentView = inflater.inflate(R.layout.fragment_me, container, false);

        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        InitializeFields();

        RetrieveUserProfileInfo();

        followingRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent followingIntent = new Intent(getContext(),FollowingActivity.class);
                followingIntent.putExtra("following_user_id", currentUserId);
                startActivity(followingIntent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.setMessage("Do you want to log out ?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mAuth.signOut();
                                SendUserToLoginActivity();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                builder.show();
            }
        });

        createPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createPostIntent = new Intent(getContext(), CreatePostActivity.class);
                createPostIntent.putExtra("imageUrl", retrieveMyProfileImage);
                createPostIntent.putExtra("userName", name);
                startActivity(createPostIntent);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(getContext(), SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        postsRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent postsIntent = new Intent(getContext(), PostsActivity.class);
               // postsIntent.putExtra("userId",currentUserId);
                startActivity(postsIntent);
            }
        });

        myProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(retrieveMyProfileImage.equals("no_img")){
                    Toast.makeText(getContext(),"No profile image available", Toast.LENGTH_SHORT).show();
                }else {
                    Intent imageViewerIntent = new Intent(getContext(), ImageViewerActivity.class);
                    imageViewerIntent.putExtra("imageUrl", retrieveMyProfileImage);
                    startActivity(imageViewerIntent);
                }
            }
        });

        return meFragmentView;
    }


    private void RetrieveUserProfileInfo() {
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.exists()) && (snapshot.hasChild("image"))){
                    retrieveMyProfileImage = snapshot.child("image").getValue().toString();
                    Picasso.get().load(retrieveMyProfileImage).into(myProfileImage);
                }
                if(snapshot.exists()) {
                    name = snapshot.child("name").getValue().toString();
                    myUsername.setText(name);
                    String about = snapshot.child("about").getValue().toString();
                    myAbout.setText(about);
                    String followers = snapshot.child("followers").getValue().toString();
                    myFollowersNo.setText(followers);
                    String following = snapshot.child("following").getValue().toString();
                    myFollowingNo.setText(following);
                    String posts = snapshot.child("posts").getValue().toString();
                    myPostsNo.setText(posts);

                    createPost.setVisibility(View.VISIBLE);
                    settings.setVisibility(View.VISIBLE);
                    connectionMessage.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(getContext(), LoginActivity.class);
        startActivity(loginIntent);
    }

    private void InitializeFields() {
        logout = meFragmentView.findViewById(R.id.log_out);
        createPost = meFragmentView.findViewById(R.id.post);
        settings = meFragmentView.findViewById(R.id.settings);
        myUsername = meFragmentView.findViewById(R.id.user_name);
        myAbout = meFragmentView.findViewById(R.id.user_about);
        myFollowersNo = meFragmentView.findViewById(R.id.followers_no_text_view);
        myFollowingNo = meFragmentView.findViewById(R.id.following_no_text_view);
        myPostsNo = meFragmentView.findViewById(R.id.posts_no_text_view);
        connectionMessage = meFragmentView.findViewById(R.id.connection_message);

        followingRelativeLayout = meFragmentView.findViewById(R.id.following_relative_layout);
        postsRelativeLayout = meFragmentView.findViewById(R.id.posts_relative_layout);

        myProfileImage = meFragmentView.findViewById(R.id.user_image);

        builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
    }
}