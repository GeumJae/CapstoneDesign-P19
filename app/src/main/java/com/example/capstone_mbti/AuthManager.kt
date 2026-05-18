package com.example.capstone_mbti

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {

    private val pref: SharedPreferences =
        context.getSharedPreferences("user", Context.MODE_PRIVATE)

    fun register(id: String, pw: String): Boolean {
        if (pref.contains(id)) return false
        pref.edit().putString(id, pw).apply()
        return true
    }

    fun login(id: String, pw: String): Boolean {
        val saved = pref.getString(id, null)
        return saved != null && saved == pw
    }

    fun deleteKakaoUser(kakaoId: String) {
        val key = "kakao_$kakaoId"
        pref.edit().remove(key).apply()
    }

    fun saveLoginSession(userId: String) {
        pref.edit().putString("login_user", userId).apply()
    }

    fun getLoginSession(): String? {
        return pref.getString("login_user", null)
    }

    fun logout() {
        pref.edit().remove("login_user").apply()
    }

    fun saveNickname(nickname: String) {
        pref.edit().putString("user_nickname", nickname).apply()
    }

    fun getNickname(): String {
        return pref.getString("user_nickname", "사용자") ?: "사용자"
    }
}
