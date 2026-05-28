package com.example.capstone_mbti;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class MbtiSelectActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvEmail;
    private AppCompatButton btnComplete;

    private String kakaoEmail;
    private String selectedMbti = "";

    private AppCompatButton selectedButton = null;

    private final int[] mbtiButtonIds = {
            R.id.btnINTJ, R.id.btnINTP, R.id.btnENTJ, R.id.btnENTP,
            R.id.btnINFJ, R.id.btnINFP, R.id.btnENFJ, R.id.btnENFP,
            R.id.btnISTJ, R.id.btnISTP, R.id.btnESTJ, R.id.btnESTP,
            R.id.btnISFJ, R.id.btnISFP, R.id.btnESFJ, R.id.btnESFP
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mbti_select);

        btnBack = findViewById(R.id.btnBack);
        tvEmail = findViewById(R.id.tvEmail);
        btnComplete = findViewById(R.id.btnComplete);

        kakaoEmail = getIntent().getStringExtra("kakao_email");

        if (kakaoEmail == null || kakaoEmail.isEmpty()) {
            kakaoEmail = "user@kakao.com";
        }

        tvEmail.setText(kakaoEmail);

        btnComplete.setVisibility(View.GONE);

        btnBack.setOnClickListener(v -> finish());

        // MBTI 버튼 선택
        for (int id : mbtiButtonIds) {

            AppCompatButton button = findViewById(id);

            button.setOnClickListener(v -> {

                selectMbti(button);

            });
        }

        // 완료 버튼 클릭
        btnComplete.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MbtiSelectActivity.this,
                    MbtiResultActivity.class
            );

            intent.putExtra("kakao_email", kakaoEmail);
            intent.putExtra("mbti", selectedMbti);

            startActivity(intent);

        });
    }

    private void selectMbti(AppCompatButton button) {

        // 이전 버튼 원상복구
        if (selectedButton != null) {

            selectedButton.setBackgroundResource(R.drawable.bg_mbti_button);

            selectedButton.setTextColor(
                    Color.parseColor("#374151")
            );
        }

        // 현재 선택 버튼
        selectedButton = button;

        selectedMbti = button.getText().toString();

        // 선택된 버튼 보라색
        selectedButton.setBackgroundResource(
                R.drawable.bg_purple_button
        );

        selectedButton.setTextColor(Color.WHITE);

        // 완료 버튼 보이기
        btnComplete.setVisibility(View.VISIBLE);
    }
}
