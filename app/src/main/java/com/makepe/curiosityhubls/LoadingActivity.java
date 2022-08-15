package com.makepe.curiosityhubls;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class LoadingActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 2000;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        textView = findViewById(R.id.appName);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LoadingActivity.this, RegisterActivity.class);
                intent.putExtra("change_number", "no");
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME_OUT);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LoadingActivity.this, RegisterActivity.class);
                intent.putExtra("change_number", "no");
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}