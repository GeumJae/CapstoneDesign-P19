package com.example.capstone_mbti

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

class LoginActivity : AppCompatActivity() {
    private val TAG = "KakaoLogin"

    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authManager = AuthManager(this)

        if (authManager.getLoginSession() != null) {
            if (authManager.isMbtiCompleted()) {
                moveToMain()
            } else {
                moveToMbtiIntro()
            }
            return
        }

        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<android.view.View>(R.id.btn_login)

        btnLogin.setOnClickListener {
            startKakaoLogin()
        }
    }

    private fun startKakaoLogin() {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡 로그인 실패", error)

                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    loginWithAccount()
                } else if (token != null) {
                    Log.i(TAG, "카카오톡 로그인 성공")
                    getUserInfo()
                }
            }
        } else {
            loginWithAccount()
        }
    }

    private fun loginWithAccount() {
        UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오계정 로그인 실패: ${error.message}", error)

                Toast.makeText(
                    this,
                    "로그인 실패\n${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            } else if (token != null) {
                Log.i(TAG, "카카오계정 로그인 성공")
                getUserInfo()
            }
        }
    }

    private fun getUserInfo() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error)
            } else if (user != null) {
                val kakaoId = user.id.toString()
                val kakaoEmail = user.kakaoAccount?.email ?: "user@kakao.com"

                authManager.saveLoginSession("kakao_$kakaoId")

                if (authManager.isMbtiCompleted()) {
                    moveToMain(kakaoEmail)
                } else {
                    moveToMbtiIntro(kakaoEmail)
                }
            }
        }
    }

    private fun moveToMbtiIntro(kakaoEmail: String = "user@kakao.com") {
        val intent = Intent(this, MbtiTestIntroActivity::class.java)
        intent.putExtra("kakao_email", kakaoEmail)
        startActivity(intent)
        finish()
    }

    private fun moveToMain(kakaoEmail: String = "user@kakao.com") {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("kakao_email", kakaoEmail)
        startActivity(intent)
        finish()
    }
}