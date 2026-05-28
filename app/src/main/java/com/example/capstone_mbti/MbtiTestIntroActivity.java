package com.example.capstone_mbti;

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
    private AuthManager authManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authManager = new AuthManager(this);

        kakaoEmail = getIntent().getStringExtra("kakao_email");

        if (kakaoEmail == null || kakaoEmail.isEmpty()) {
            kakaoEmail = "user@kakao.com";
        }

        // MBTI 이미 등록되어 있으면 검사 소개 화면 생략
        if (authManager.isMbtiCompleted()) {
            Intent intent = new Intent(MbtiTestIntroActivity.this, MainActivity.class);
            intent.putExtra("kakao_email", kakaoEmail);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_mbti_test_intro);

        btnBack = findViewById(R.id.btnBack);
        tvEmail = findViewById(R.id.tvEmail);
        btnStartTest = findViewById(R.id.btnStartTest);

        tvEmail.setText(kakaoEmail);

        btnBack.setOnClickListener(v -> finish());

        btnStartTest.setOnClickListener(v -> {
            Intent intent = new Intent(MbtiTestIntroActivity.this, MbtiQuestionActivity.class);
            intent.putExtra("kakao_email", kakaoEmail);
            startActivity(intent);
        });

    }}