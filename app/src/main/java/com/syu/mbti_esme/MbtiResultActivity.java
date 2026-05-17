package com.syu.mbti_esme;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.util.HashMap;

public class MbtiResultActivity extends AppCompatActivity {

    private ImageView imgMbtiEmoji;
    private TextView tvMbti;
    private TextView tvName;
    private TextView tvDesc;
    private AppCompatButton btnStartCommunity;

    private final HashMap<String, MbtiInfo> mbtiMap = new HashMap<>();

    static class MbtiInfo {
        String name;
        String description;
        int imageRes;

        MbtiInfo(String name, String description, int imageRes) {
            this.name = name;
            this.description = description;
            this.imageRes = imageRes;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mbti_result);

        imgMbtiEmoji = findViewById(R.id.imgMbtiEmoji);
        tvMbti = findViewById(R.id.tvMbti);
        tvName = findViewById(R.id.tvName);
        tvDesc = findViewById(R.id.tvDesc);
        btnStartCommunity = findViewById(R.id.btnStartCommunity);

        initMbtiData();

        String mbti = getIntent().getStringExtra("mbti");

        if (mbti == null || mbti.isEmpty()) {
            mbti = "INTJ";
        }

        MbtiInfo info = mbtiMap.get(mbti);

        if (info != null) {
            imgMbtiEmoji.setImageResource(info.imageRes);
            tvMbti.setText(mbti);
            tvName.setText(info.name);
            tvDesc.setText(info.description);
        }

        startResultAnimation();

        btnStartCommunity.setOnClickListener(v -> {
            Intent intent = new Intent(MbtiResultActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void initMbtiData() {
        mbtiMap.put("INTJ", new MbtiInfo("전략가", "상상력이 풍부하고 전략적인 사고를 하는 계획가", R.drawable.emoji_intj));
        mbtiMap.put("INTP", new MbtiInfo("논리술사", "지식이 풍부하고 창의적인 사색가", R.drawable.emoji_intp));
        mbtiMap.put("ENTJ", new MbtiInfo("통솔자", "대담하고 상상력이 풍부한 리더", R.drawable.emoji_entj));
        mbtiMap.put("ENTP", new MbtiInfo("변론가", "영리하고 호기심이 많은 사색가", R.drawable.emoji_entp));

        mbtiMap.put("INFJ", new MbtiInfo("옹호자", "선의를 가지고 있는 이상주의자", R.drawable.emoji_infj));
        mbtiMap.put("INFP", new MbtiInfo("중재자", "이상주의적이고 시적인 영혼의 소유자", R.drawable.emoji_infp));
        mbtiMap.put("ENFJ", new MbtiInfo("선도자", "카리스마 있고 영감을 주는 리더", R.drawable.emoji_enfj));
        mbtiMap.put("ENFP", new MbtiInfo("활동가", "열정적이고 창의적인 자유로운 영혼", R.drawable.emoji_enfp));

        mbtiMap.put("ISTJ", new MbtiInfo("현실주의자", "사실에 기반한 신뢰할 수 있는 실용주의자", R.drawable.emoji_istj));
        mbtiMap.put("ISFJ", new MbtiInfo("수호자", "헌신적이고 따뜻한 수호자", R.drawable.emoji_isfj));
        mbtiMap.put("ESTJ", new MbtiInfo("경영자", "뛰어난 관리 능력을 가진 경영자", R.drawable.emoji_estj));
        mbtiMap.put("ESFJ", new MbtiInfo("집정관", "배려심이 많고 사교적인 협력자", R.drawable.emoji_esfj));

        mbtiMap.put("ISTP", new MbtiInfo("장인", "대담하고 실용적인 실험가", R.drawable.emoji_istp));
        mbtiMap.put("ISFP", new MbtiInfo("모험가", "유연하고 매력적인 예술가", R.drawable.emoji_isfp));
        mbtiMap.put("ESTP", new MbtiInfo("사업가", "영리하고 활동적인 모험가", R.drawable.emoji_estp));
        mbtiMap.put("ESFP", new MbtiInfo("연예인", "즉흥적이고 열정적인 엔터테이너", R.drawable.emoji_esfp));
    }

    private void startResultAnimation() {
        imgMbtiEmoji.setAlpha(0f);
        tvMbti.setAlpha(0f);
        tvName.setAlpha(0f);
        tvDesc.setAlpha(0f);
        btnStartCommunity.setAlpha(0f);

        imgMbtiEmoji.setTranslationY(80f);
        tvMbti.setTranslationY(80f);
        tvName.setTranslationY(80f);
        tvDesc.setTranslationY(80f);
        btnStartCommunity.setTranslationY(80f);

        imgMbtiEmoji.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(100)
                .start();

        tvMbti.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(250)
                .start();

        tvName.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(350)
                .start();

        tvDesc.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(500)
                .start();

        btnStartCommunity.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(650)
                .start();
    }
}