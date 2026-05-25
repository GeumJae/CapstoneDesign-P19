package com.example.capstone_mbti;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class MbtiQuestionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvQuestionCount;
    private ViewGroup.LayoutParams progressParams;
    private android.view.View viewProgress;
    private TextView tvQuestion;
    private AppCompatButton btnAnswerA;
    private AppCompatButton btnAnswerB;

    private int currentIndex = 0;

    private int e = 0;
    private int i = 0;
    private int s = 0;
    private int n = 0;
    private int t = 0;
    private int f = 0;
    private int j = 0;
    private int p = 0;

    private String kakaoEmail;

    private final Question[] questions = {
            new Question("처음 보는 사람들과의 모임에서 나는?",
                    "먼저 말을 걸고 적극적으로 대화한다", "E",
                    "상대방이 말을 걸 때까지 기다린다", "I"),

            new Question("새로운 일을 시작할 때 나는?",
                    "구체적인 사실과 경험을 중시한다", "S",
                    "전체적인 가능성과 의미를 생각한다", "N"),

            new Question("결정을 내릴 때 나는?",
                    "논리와 원칙을 우선시한다", "T",
                    "사람과 감정을 우선시한다", "F"),

            new Question("계획을 세울 때 나는?",
                    "미리 계획하고 일정을 지킨다", "J",
                    "유연하게 상황에 맞춰 변경한다", "P"),

            new Question("주말을 보낼 때 나는?",
                    "친구들과 활발하게 활동하며 에너지를 얻는다", "E",
                    "혼자만의 시간을 가지며 에너지를 충전한다", "I"),

            new Question("문제를 해결할 때 나는?",
                    "현실적이고 실용적인 방법을 찾는다", "S",
                    "창의적이고 새로운 방법을 시도한다", "N"),

            new Question("갈등 상황에서 나는?",
                    "객관적인 사실과 논리로 해결한다", "T",
                    "상대방의 감정을 이해하고 공감한다", "F"),

            new Question("업무 스타일은?",
                    "계획적이고 체계적으로 진행한다", "J",
                    "융통성 있게 즉흥적으로 진행한다", "P")
    };

    static class Question {
        String question;
        String answerA;
        String typeA;
        String answerB;
        String typeB;

        Question(String question, String answerA, String typeA, String answerB, String typeB) {
            this.question = question;
            this.answerA = answerA;
            this.typeA = typeA;
            this.answerB = answerB;
            this.typeB = typeB;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mbti_question);

        btnBack = findViewById(R.id.btnBack);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        viewProgress = findViewById(R.id.viewProgress);
        tvQuestion = findViewById(R.id.tvQuestion);
        btnAnswerA = findViewById(R.id.btnAnswerA);
        btnAnswerB = findViewById(R.id.btnAnswerB);

        kakaoEmail = getIntent().getStringExtra("kakao_email");
        if (kakaoEmail == null || kakaoEmail.isEmpty()) {
            kakaoEmail = "user@kakao.com";
        }

        btnBack.setOnClickListener(v -> finish());

        btnAnswerA.setOnClickListener(v -> {
            addScore(questions[currentIndex].typeA);
            goNextQuestion();
        });

        btnAnswerB.setOnClickListener(v -> {
            addScore(questions[currentIndex].typeB);
            goNextQuestion();
        });

        showQuestion();
    }

    private void showQuestion() {
        Question q = questions[currentIndex];

        tvQuestionCount.setText((currentIndex + 1) + " / " + questions.length);
        tvQuestion.setText(q.question);
        btnAnswerA.setText(q.answerA);
        btnAnswerB.setText(q.answerB);

        viewProgress.post(() -> {
            ViewGroup parent = (ViewGroup) viewProgress.getParent();
            int totalWidth = parent.getWidth();

            int progressWidth = totalWidth * (currentIndex + 1) / questions.length;

            progressParams = viewProgress.getLayoutParams();
            progressParams.width = progressWidth;
            viewProgress.setLayoutParams(progressParams);
        });
    }

    private void addScore(String type) {
        switch (type) {
            case "E":
                e++;
                break;
            case "I":
                i++;
                break;
            case "S":
                s++;
                break;
            case "N":
                n++;
                break;
            case "T":
                t++;
                break;
            case "F":
                f++;
                break;
            case "J":
                j++;
                break;
            case "P":
                p++;
                break;
        }
    }

    private void goNextQuestion() {
        if (currentIndex < questions.length - 1) {
            currentIndex++;
            showQuestion();
        } else {
            String resultMbti = calculateMbti();

            Intent intent = new Intent(MbtiQuestionActivity.this, MbtiResultActivity.class);
            intent.putExtra("kakao_email", kakaoEmail);
            intent.putExtra("mbti", resultMbti);
            startActivity(intent);
            finish();
        }
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