package com.websistems.wallpaperhub.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.websistems.wallpaperhub.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Handler handler = new Handler();
        handler.postDelayed(r, 3000);
    }

    Runnable r = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }
    };
}
