package com.hncc.connect;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;


public class HomeActivity extends AppCompatActivity {
    
    private FirebaseAuth mAuth;
    private ChipNavigationBar chipNavigationBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        /* we have added  android:windowSoftInputMode="adjustPan"  in manifest file so that
           bottom navigation bar does not come up while typing post message */

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mAuth = FirebaseAuth.getInstance();
        
        InitializeFields();

        chipNavigationBar.setItemSelected(R.id.home, true);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        BottomMenu(); //for handling clicks on chipNavigationBar


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);

    }

    private void BottomMenu() {

        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                Fragment fragment = null;
                switch (i){
                    case R.id.home:
                        fragment = new HomeFragment();
                        break;
                    case R.id.people:
                        fragment = new PeopleFragment();
                        break;
                    case R.id.me:
                        fragment = new MeFragment();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            }
        });

    }


    private void InitializeFields() {
        chipNavigationBar = findViewById(R.id.bottom_nav_menu);
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
            // that is, currentUser has no data,i.e, not created his account yet
            SendUserToLoginActivity();
        }else {
            if (!currentUser.isEmailVerified()) {
                Toast.makeText(this,"Please verify your E-mail first",Toast.LENGTH_LONG).show();
                mAuth.signOut();
                SendUserToLoginActivity();
            }
        }
    }


    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

}