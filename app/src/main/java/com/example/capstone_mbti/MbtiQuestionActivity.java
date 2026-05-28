package com.example.capstone_mbti; // ⚠️ 본인 패키지명 확인!

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.util.ArrayList;
import java.util.List;

public class MbtiQuestionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvQuestionCount;
    private View viewProgress;
    private TextView tvQuestion;
    private AppCompatButton btnAnswerA;
    private AppCompatButton btnAnswerB;

    private int currentIndex = 0;

    // 누적 점수 변수
    private int e = 0, i = 0, s = 0, n = 0, t = 0, f = 0, j = 0, p = 0;

    private String kakaoEmail;
    private String kakaoId;
    private List<MBTITest> dbQuestions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mbti_question);

        AuthManager authManager = new AuthManager(this);
        String sessionId = authManager.getLoginSession();
        kakaoId = (sessionId != null) ? sessionId.replace("kakao_", "") : "";
        kakaoEmail = getIntent().getStringExtra("kakao_email");
        btnBack = findViewById(R.id.btnBack);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        viewProgress = findViewById(R.id.viewProgress);
        tvQuestion = findViewById(R.id.tvQuestion);
        btnAnswerA = findViewById(R.id.btnAnswerA);
        btnAnswerB = findViewById(R.id.btnAnswerB);

        btnBack.setOnClickListener(v -> finish());

        loadQuestionsFromDB();
    }

    private void loadQuestionsFromDB() {
        SupabaseHelper.fetchMbtiQuestions(questions -> {
            if (questions != null && !questions.isEmpty()) {
                dbQuestions = questions;
                currentIndex = 0;
                showQuestion();
            } else {
                Toast.makeText(this, "DB에서 질문을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showQuestion() {
        MBTITest currentQ = dbQuestions.get(currentIndex);

        tvQuestionCount.setText((currentIndex + 1) + " / " + dbQuestions.size());
        tvQuestion.setText(currentQ.getItem());

        String mbtiType = currentQ.getMbti_type();
        String typeA = mbtiType.split("/")[0]; // "E"
        String typeB = mbtiType.split("/")[1]; // "I"

        btnAnswerA.setText(currentQ.getAnswer_a());
        btnAnswerB.setText(currentQ.getAnswer_b());

        btnAnswerA.setOnClickListener(v -> handleAnswer(currentQ, typeA));
        btnAnswerB.setOnClickListener(v -> handleAnswer(currentQ, typeB));
    }

    private void handleAnswer(MBTITest question, String selectedType) {
        if (question.getId() != null) {
            SupabaseHelper.insertUserSelect(kakaoId, question.getId(), selectedType);
        }
        addScore(selectedType);
        if (currentIndex < dbQuestions.size() - 1) {
            currentIndex++;
            showQuestion();
        } else {
            finishTest();
        }
    }

    private void addScore(String type) {
        switch (type) {
            case "E": e++; break;
            case "I": i++; break;
            case "S": s++; break;
            case "N": n++; break;
            case "T": t++; break;
            case "F": f++; break;
            case "J": j++; break;
            case "P": p++; break;
        }
    }

    private void finishTest() {
        String resultMbti = calculateMbti();

        SupabaseHelper.updateUserMbti(kakaoId, resultMbti, () -> {
            Intent intent = new Intent(MbtiQuestionActivity.this, MbtiResultActivity.class);
            intent.putExtra("kakao_email", kakaoEmail);
            intent.putExtra("mbti", resultMbti);
            startActivity(intent);
            finish();
        });
    }

    private String calculateMbti() {
        String result = "";
        result += e >= i ? "E" : "I";
        result += s >= n ? "S" : "N";
        result += t >= f ? "T" : "F";
        result += j >= p ? "J" : "P";
        return result;
    }
}