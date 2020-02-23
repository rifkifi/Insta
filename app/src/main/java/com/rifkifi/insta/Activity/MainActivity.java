package com.rifkifi.insta.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rifkifi.insta.Fragment.HomeFragment;
import com.rifkifi.insta.Fragment.NotificationFragment;
import com.rifkifi.insta.Fragment.ProfileFragment;
import com.rifkifi.insta.Fragment.SearchFragment;
import com.rifkifi.insta.R;

public class MainActivity extends AppCompatActivity {
    FirebaseUser userLogin;
    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userLogin = FirebaseAuth.getInstance().getCurrentUser();
        bottomNavigationView = findViewById(R.id.nav_bottom);


        Bundle intent = getIntent().getExtras();
        if (intent != null) {
            String publisher = intent.getString("publisherId");

            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileId", publisher);
            editor.apply();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_main, new ProfileFragment()).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_main, new HomeFragment()).commit();
        }


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        selectedFragment = new HomeFragment();
                        break;
                    case R.id.nav_search:
                        selectedFragment = new SearchFragment();
                        break;
                    case R.id.nav_add:
                        selectedFragment = null;
                        startActivity(new Intent(MainActivity.this, PostActivity.class));
                        break;
                    case R.id.nav_notification:
                        selectedFragment = new NotificationFragment();
                        break;
                    case R.id.nav_profile:
                        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                        editor.putString("profileId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        editor.apply();
                        selectedFragment = new ProfileFragment();
                        break;
                }
                if (selectedFragment != null){
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_main, selectedFragment).commit();
                }
                return true;
            }




        });
    }
}
