package com.hncc.connect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class LoginActivity extends AppCompatActivity {

    private TextView signUp;
    private CheckBox checkBoxLogin;
    private EditText passwordInput, emailInput;
    private Button signInButton;

    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;

    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("users");

        InitializeFields();

        checkBoxLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserSignIn();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signUpIntent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);

    }


    private void InitializeFields() {
        signUp = (TextView) findViewById(R.id.sign_up);
        checkBoxLogin = (CheckBox) findViewById(R.id.check_box_login);
        emailInput = (EditText) findViewById(R.id.user_email_input);
        passwordInput = (EditText) findViewById(R.id.user_password_input);
        signInButton = (Button) findViewById(R.id.sign_in_button);

        loadingBar = new ProgressDialog(LoginActivity.this);

        builder = new AlertDialog.Builder(LoginActivity.this, R.style.AlertDialogTheme);
    }




    private void AllowUserSignIn() {


        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please fill e-mail and password",Toast.LENGTH_SHORT).show();
        }else {
            loadingBar.setTitle("Loading...");
            loadingBar.setMessage("Verifying your email and password, please wait.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {

                                final String currentUserID = mAuth.getCurrentUser().getUid();
                                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(LoginActivity.this, new OnSuccessListener<InstanceIdResult>() {
                                    @Override
                                    public void onSuccess(InstanceIdResult instanceIdResult) {
                                        String DeviceToken = instanceIdResult.getToken();
                                        Log.e("newToken",DeviceToken);

                                        UsersRef.child(currentUserID).child("device_token")
                                                .setValue(DeviceToken)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){


                                                            if(mAuth.getCurrentUser().isEmailVerified()){
                                                                loadingBar.dismiss();
                                                                SendUserToHomeActivity();
                                                                finish();
                                                            }else{
                                                                builder.setMessage("Your email address is not verified yet. Do you want us to resend verification link to your email address?")
                                                                        .setCancelable(true)
                                                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                            public void onClick(DialogInterface dialog, int id) {

                                                                                mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if(task.isSuccessful()){
                                                                                            Toast.makeText(LoginActivity.this, "Verification E-mail sent successfully...", Toast.LENGTH_SHORT).show();
                                                                                        }else{
                                                                                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                                                        }
                                                                                    }
                                                                                });

                                                                            }
                                                                        })
                                                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                                            public void onClick(DialogInterface dialog, int id) {
                                                                                dialog.cancel();
                                                                            }
                                                                        });
                                                                loadingBar.dismiss();
                                                                builder.show();


                                                            }


                                                        }
                                                    }
                                                });

                                    }
                                });





                            } else {
                                loadingBar.dismiss();
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                            }
                           // loadingBar.dismiss();
                        }
                    });
        }


    }


    private void SendUserToHomeActivity() {
        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(homeIntent);
        finish();
    }

}