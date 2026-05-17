package com.syu.mbti_esme;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class MbtiTestIntroActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvEmail;
    private AppCompatButton btnStartTest;

    private String kakaoEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mbti_test_intro);

        btnBack = findViewById(R.id.btnBack);
        tvEmail = findViewById(R.id.tvEmail);
        btnStartTest = findViewById(R.id.btnStartTest);

        kakaoEmail = getIntent().getStringExtra("kakao_email");

        if (kakaoEmail == null || kakaoEmail.isEmpty()) {
            kakaoEmail = "user@kakao.com";
        }

        tvEmail.setText(kakaoEmail);

        btnBack.setOnClickListener(v -> finish());

        btnStartTest.setOnClickListener(v -> {
            Intent intent = new Intent(MbtiTestIntroActivity.this, MbtiQuestionActivity.class);
            intent.putExtra("kakao_email", kakaoEmail);
            startActivity(intent);
        });
    }
}