package com.example.capstone_mbti

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 카카오 SDK 초기화
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
    }
}