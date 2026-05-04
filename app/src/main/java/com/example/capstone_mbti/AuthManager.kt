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
}
