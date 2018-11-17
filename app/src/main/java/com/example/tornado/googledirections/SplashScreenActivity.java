package com.example.tornado.googledirections;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //choose handler.os
        Handler handler= new Handler();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreenActivity.this,DirectionActivity.class));
                finish();
            }
        };
        handler.postDelayed(runnable , 2000);
    }
}
