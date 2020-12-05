package com.hncc.connect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class homeFragmentAdapter extends RecyclerView.Adapter {

    ArrayList<feed> homeFragmentList;
    Context myContext;
    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    DatabaseReference postRef;

    public homeFragmentAdapter(ArrayList<feed> homeFragmentList, Context context) {
        this.homeFragmentList = homeFragmentList;
        this.myContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_feed, parent, false);
        return new homeFragmentAdapter.FeedHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        postRef = FirebaseDatabase.getInstance().getReference().child("posts");

        final FeedHolder FeedHolder = (FeedHolder)holder;

        final feed feed = homeFragmentList.get(position);

        // Set the data to the views here
        String ProfileImageUrl = feed.getProfileImageUrl();
        if(!ProfileImageUrl.equals("no_img")){
            Picasso.get().load(ProfileImageUrl).into(FeedHolder.feedProfileImage);
        }

        FeedHolder.feedProfileName.setText(feed.getProfileName());
        FeedHolder.feedDateAndTime.setText(feed.getPostDateAndTime());

        String PostImageUrl = feed.getPostImageUrl();
        if(!PostImageUrl.equals("no_post_img")){
            Picasso.get().load(PostImageUrl).into(FeedHolder.feedImage);
        }else {
            FeedHolder.feedImage.setVisibility(View.GONE);
        }

        FeedHolder.feedText.setText(feed.getPostMessage());

        final String postPushId = feed.getPostPushId();

        FeedHolder.feedLikes.setText(feed.getPostLikes());
        final boolean[] isPostLiked = {false};
        postRef.child(postPushId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("likedBy")){
                    if(snapshot.child("likedBy").hasChild(currentUserId)){
                        isPostLiked[0] = true;
                        FeedHolder.feedLikeImg.setImageResource(R.drawable.vector_like);
                        //FeedHolder.feedLikes.setTextColor(Color.parseColor("#318fb5"));
                    }else isPostLiked[0] = false;
                }else isPostLiked[0] = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        String postUid = feed.getPostUid();
        if (postUid.equals(currentUserId)){
            FeedHolder.feedDelete.setVisibility(View.VISIBLE);
        }

        FeedHolder.feedLikeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                postRef.child(postPushId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChild("likedBy")){
                            if(snapshot.child("likedBy").hasChild(currentUserId)){
                                isPostLiked[0] = true;
                            }else isPostLiked[0] = false;
                        }else isPostLiked[0] = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                if(isPostLiked[0]) {
                    final String postLikes = String.valueOf(Integer.parseInt(feed.getPostLikes()) - 1);
                    feed.setPostLikes(postLikes);

                    FirebaseDatabase.getInstance().getReference()
                            .child("posts")
                            .child(postPushId)
                            .child("likes")
                            .setValue(postLikes)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("posts").child(postPushId).child("likedBy").child(currentUserId).removeValue();

                                    FeedHolder.feedLikeImg.setImageResource(R.drawable.vector_unlike);
                                    FeedHolder.feedLikes.setText(postLikes);


                                }
                            });

                }
                else {
                    final String postLikes2 = String.valueOf(Integer.parseInt(feed.getPostLikes()) + 1);
                    feed.setPostLikes(postLikes2);
                    FirebaseDatabase.getInstance().getReference()
                            .child("posts").child(postPushId).child("likes")
                            .setValue(postLikes2)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("posts").child(postPushId).child("likedBy").child(currentUserId).setValue(currentUserId);

                                    FeedHolder.feedLikeImg.setImageResource(R.drawable.vector_like);
                                    FeedHolder.feedLikes.setText(postLikes2);
                                }
                            });

                }


            }
        });

        FeedHolder.feedDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(), R.style.AlertDialogTheme);
                builder.setMessage("Delete this post ?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {


                                FirebaseDatabase.getInstance().getReference()
                                        .child("posts").child(postPushId).removeValue();
                                ///////postsNumber[0] = postsNumber[0] - 1;
                                int currentPostsNumber = homeFragmentList.size() - 1;
                                FirebaseDatabase.getInstance().getReference().child("users")
                                        .child(currentUserId).child("posts").setValue(String.valueOf(currentPostsNumber));
                                ((Activity)myContext).finish();

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

    }

    @Override
    public int getItemCount() {
        return homeFragmentList == null? 0: homeFragmentList.size();
    }


    public class FeedHolder extends RecyclerView.ViewHolder {

        CircleImageView feedProfileImage;
        ImageView feedLikeImg;
        TextView feedProfileName;
        TextView feedDateAndTime;
        ImageView feedImage;
        TextView feedText;
        TextView feedLikes;
        TextView feedDelete;

        public FeedHolder(View itemView) {
            super(itemView);
            feedProfileImage = itemView.findViewById(R.id.feed_profile_img);
            feedProfileName = itemView.findViewById(R.id.each_post_name_text_view);
            feedDateAndTime= itemView.findViewById(R.id.each_post_date_and_time_text_view);
            feedImage = itemView.findViewById(R.id.each_post_image);
            feedText = itemView.findViewById(R.id.feed_text);
            feedLikeImg = itemView.findViewById(R.id.like_button);
            feedLikes = itemView.findViewById(R.id.feed_likes);
            feedDelete = itemView.findViewById(R.id.feed_delete);
        }

    }




}
