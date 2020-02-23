package com.rifkifi.insta.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rifkifi.insta.R;

public class SplashActivity extends AppCompatActivity {
    Handler handler;

    FirebaseUser userLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler = new Handler();
        userLogin = FirebaseAuth.getInstance().getCurrentUser();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (userLogin != null) {
                    Intent intent=new Intent(SplashActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent=new Intent(SplashActivity.this, StartActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        },5000);
    }
}
