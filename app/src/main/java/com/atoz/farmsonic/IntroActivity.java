package com.atoz.farmsonic;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro); // XML과 Java 소스 연결

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent); // 인트로 실행 후 MainActivity로 넘어감
                finish(); // IntroActivity 종료
            }
        }, 1800); // 1.8초 후 인트로 실행
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish(); // 화면이 다른 화면으로 넘어가면 이 화면 종료
    }
}
