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

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar SettingsToolbar;
    private CircleImageView mySettingsProfileImage;
    private EditText settingsNameEditText, settingsAboutEditText;
    private Button updateProfileButton;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private StorageReference UserProfileImageRef;

    private String currentUserId;

    private ProgressDialog loadingBar;

    private static final int GalleryPic = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        InitializeFields();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        RetrieveUserInfo();

        mySettingsProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPic);
            }
        });

        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String setUserName = settingsNameEditText.getText().toString();
                String setAbout = settingsAboutEditText.getText().toString();

                if (TextUtils.isEmpty(setAbout)){

                }else{
                    HashMap<String, Object> profileMap = new HashMap<>();
                    profileMap.put("uid", currentUserId);
                   // profileMap.put("name", setUserName);
                    profileMap.put("about", setAbout);
                    usersRef.child(currentUserId).updateChildren(profileMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        SendUserToHomeActivity();
                                        Toast.makeText(SettingsActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                    }else{
                                        String message = task.getException().toString();
                                        Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

    }

    private void SendUserToHomeActivity() {
        Intent homeIntent = new Intent(SettingsActivity.this,HomeActivity.class);
        startActivity(homeIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GalleryPic && resultCode==RESULT_OK && data!=null){
            Uri ImageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode==RESULT_OK){
                loadingBar.setTitle("Set profile image");
                loadingBar.setMessage("Uploading your profile image...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                Uri resultUri = result.getUri();

                final StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String downloadUrl = uri.toString();
                                        // complete the rest of your code


                                        Toast.makeText(SettingsActivity.this, "Profile image uploaded successfully...", Toast.LENGTH_SHORT).show();

                                        usersRef.child(currentUserId).child("image")
                                                .setValue(downloadUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            Toast.makeText(SettingsActivity.this, "Image saved in database...", Toast.LENGTH_SHORT).show();
                                                        }else {
                                                            String message = task.getException().toString();
                                                            Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
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

    private void RetrieveUserInfo() {
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.exists()) && (snapshot.hasChild("image"))){
                    String retrieveProfileImage = snapshot.child("image").getValue().toString();
                    Picasso.get().load(retrieveProfileImage).into(mySettingsProfileImage);
                }
                if(snapshot.exists()) {
                    String about = snapshot.child("about").getValue().toString();
                    settingsAboutEditText.setText(about);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void InitializeFields() {
        SettingsToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setTitle("Settings");

        mySettingsProfileImage = findViewById(R.id.myProfileImage);

        settingsAboutEditText = findViewById(R.id.settings_about_edit_text);
        updateProfileButton = findViewById(R.id.settings_update_button);

        loadingBar = new ProgressDialog(this);
    }

}