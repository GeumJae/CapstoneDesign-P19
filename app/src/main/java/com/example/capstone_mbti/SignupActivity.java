package com.example.capstone_mbti;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class SignupActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvEmail;
    private AppCompatButton btnKnowMBTI;

    private String kakaoEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        btnBack = findViewById(R.id.btnBack);
        tvEmail = findViewById(R.id.tvEmail);
        btnKnowMBTI = findViewById(R.id.btnKnowMBTI);
        AppCompatButton btnTestMBTI = findViewById(R.id.btnTestMBTI);

        kakaoEmail = getIntent().getStringExtra("kakao_email");

        if (kakaoEmail == null || kakaoEmail.isEmpty()) {
            kakaoEmail = "user@kakao.com";
        }

        tvEmail.setText(kakaoEmail);

        btnBack.setOnClickListener(v -> finish());

        btnKnowMBTI.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, MbtiSelectActivity.class);
            intent.putExtra("kakao_email", kakaoEmail);
            startActivity(intent);
        });

        btnTestMBTI.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, MbtiTestIntroActivity.class);
            intent.putExtra("kakao_email", kakaoEmail);
            startActivity(intent);
        });
    }
}