package com.hncc.connect;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput, nameInput;
    private Button signUpButton;
    private CheckBox checkbox;

    private int followers = 0, posts = 0, following = 0;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ProgressDialog loadingBar;

    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        InitializeFields();


        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (!isChecked) {
                    // show password
                    passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    // hide password
                    passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });


        
    }



    private void CreateNewAccount() {

        final String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        final String username = nameInput.getText().toString();
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username)){
            Toast.makeText(this,"Please fill all of the above fields",Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Loading...");
            loadingBar.setMessage("Please wait, we creating your account.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()) {
                                String currentUserID = mAuth.getCurrentUser().getUid();

                                HashMap<String, Object> profileMap = new HashMap<>();
                                profileMap.put("email", email);
                                profileMap.put("followers", followers);
                                profileMap.put("following", following);
                                profileMap.put("about", "Hi there! I am using Connect.");
                                profileMap.put("posts", posts);
                                profileMap.put("uid", currentUserID);
                                profileMap.put("name", username);

                                RootRef.child("users").child(currentUserID).updateChildren(profileMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(SignUpActivity.this, "Account created successfully...", Toast.LENGTH_SHORT).show();
                                                }else{
                                                    String message = task.getException().toString();
                                                    Toast.makeText(SignUpActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){
                                            loadingBar.dismiss();
                                            builder.setMessage("An E-mail has been sent to you. Please click the link given in it to verify your E-mail address.")
                                                    .setCancelable(false)
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            SendUserToLoginActivity();
                                                        }
                                                    });
                                            builder.show();

                                        }else{
                                            loadingBar.dismiss();
                                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });


                            }else {
                                loadingBar.dismiss();
                                String message = task.getException().toString();
                                Toast.makeText(SignUpActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                            }


                        }
                    });


        }

    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void InitializeFields() {

        emailInput = (EditText) findViewById(R.id.user_email_input);
        passwordInput = (EditText) findViewById(R.id.user_password_input);
        signUpButton = (Button) findViewById(R.id.sign_up_button);
        checkbox = (CheckBox) findViewById(R.id.check_box);
        nameInput = (EditText) findViewById(R.id.user_name_input);

        builder = new AlertDialog.Builder(SignUpActivity.this, R.style.AlertDialogTheme);

        loadingBar = new ProgressDialog(SignUpActivity.this);
    }

}