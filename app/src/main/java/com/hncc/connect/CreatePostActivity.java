package com.hncc.connect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CreatePostActivity extends AppCompatActivity {

    private Toolbar CreatePostToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private StorageReference UserPostImageRef;

    private String currentUserId;
    private static final int GalleryPic = 1;
    private int currentUserPostsNo = 0;

    private ImageView postImageView;
    private Button postAddImageButton, postButton;
    private EditText postMessageEditText;

    private ProgressDialog loadingBar;

    private String postPushID = "noId", userName;
    private String profileImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        Intent intent = getIntent();
        profileImageUrl = intent.getExtras().get("imageUrl").toString();
        userName = intent.getExtras().get("userName").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        UserPostImageRef = FirebaseStorage.getInstance().getReference().child("Post Images").child(currentUserId);

        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currentUserPostsNo = Integer.parseInt(snapshot.child("posts").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        InitializeFields();

//        getUserName();

        postAddImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPic);
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String postMessageText = postMessageEditText.getText().toString();


                if(!TextUtils.isEmpty(postMessageText)) {
                    
                    Calendar calendar = Calendar.getInstance();

                    SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
                    String saveCurrentDate = currentDate.format(calendar.getTime());

                    SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                    String saveCurrentTime = currentTime.format(calendar.getTime());

                    if (postPushID.equals("noId")) {
                        //this means post does not have an image and this will create a key then
                        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("posts").push();
                        postPushID = postsRef.getKey();
                    }

                    HashMap<String, Object> postMap = new HashMap<>();
                    postMap.put("uid", currentUserId);
                    postMap.put("dateAndTime", saveCurrentDate + "  " + saveCurrentTime);
                    postMap.put("postMessageText", postMessageText);
                    postMap.put("postProfileImage", profileImageUrl);
                    postMap.put("likes", "0");
                    postMap.put("userName", userName);
                    postMap.put("postPushID", postPushID);

                    FirebaseDatabase.getInstance().getReference().child("posts").child(postPushID)
                            .updateChildren(postMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                increaseUserPostsNumber();
                                postMessageEditText.setText("");
                                postImageView.setImageResource(R.drawable.no_image);
                                Toast.makeText(CreatePostActivity.this, "Post uploaded successfully", Toast.LENGTH_SHORT).show();
                            }else{
                                String message = task.getException().toString();
                                Toast.makeText(CreatePostActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                            }
                        }
                    });


                    postPushID = "noId"; //so that we can add new posts

                }else {
                    Toast.makeText(CreatePostActivity.this,"Please write something about your post", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void increaseUserPostsNumber() {

        usersRef.child(currentUserId).child("posts").setValue(String.valueOf(currentUserPostsNo + 1));
        currentUserPostsNo = currentUserPostsNo + 1;

    }

    private void InitializeFields() {
        CreatePostToolbar = findViewById(R.id.create_post_toolbar);
        setSupportActionBar(CreatePostToolbar);
        getSupportActionBar().setTitle("Create Post");

        postImageView = findViewById(R.id.post_image);
        postAddImageButton = findViewById(R.id.add_image_button);
        postMessageEditText = findViewById(R.id.post_message_edit_text);
        postButton = findViewById(R.id.post_button);

        loadingBar = new ProgressDialog(CreatePostActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GalleryPic && resultCode==RESULT_OK && data!=null){
            Uri ImageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(3,2)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode==RESULT_OK){
                loadingBar.setTitle("Uploading image");
                loadingBar.setMessage("please wait while we are uploading image for your post");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                Uri resultUri = result.getUri();

                DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("posts").push();
                //this will create a key
                postPushID = postsRef.getKey();

                final StorageReference filePath = UserPostImageRef.child(postPushID + ".jpg");

                filePath.putFile(resultUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String downloadUrl = uri.toString();

                                        FirebaseDatabase.getInstance().getReference().child("posts")
                                                .child(postPushID).child("postMessageImage")
                                                .setValue(downloadUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            Toast.makeText(CreatePostActivity.this, "Image saved", Toast.LENGTH_SHORT).show();
                                                            Picasso.get().load(downloadUrl).into(postImageView);
                                                        }else {
                                                            String message = task.getException().toString();
                                                            Toast.makeText(CreatePostActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                                                        }
                                                        loadingBar.dismiss();
                                                    }
                                                });
                                    }

                                });
                            }
                        });
            }
        }

    }

}