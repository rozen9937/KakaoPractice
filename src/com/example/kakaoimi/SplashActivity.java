package com.example.kakaoimi;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

public class SplashActivity extends Activity {
    private long splashDelay = 150;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Intent kakaoIntent = new Intent().setClass(SplashActivity.this, KakaoActivity.class);
                startActivity(kakaoIntent);
                overridePendingTransition(R.anim.splash_fade, R.anim.splash_hold);
                finish();
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, splashDelay);
    }
}
