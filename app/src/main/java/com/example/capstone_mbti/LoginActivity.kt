package com.example.capstone_mbti

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.*

class LoginActivity : AppCompatActivity() {
    private val TAG = "KakaoLogin"

    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager = AuthManager(this)

        val sessionId = authManager.getLoginSession()

        if (sessionId != null) {
            val kakaoId = sessionId.replace("kakao_", "")

            if (authManager.isMbtiCompleted()) {
                moveToMain()
                return
            }

            setContentView(R.layout.activity_login)

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val dbUser = SupabaseClient.client.postgrest["User"]
                        .select { eq("kakao_id", kakaoId) }
                        .decodeSingleOrNull<User>()

                    withContext(Dispatchers.Main) {
                        if (dbUser != null && !dbUser.mbti.isNullOrEmpty()) {
                            Log.i(TAG, "자동 로그인 - DB에서 MBTI 확인 완료: ${dbUser.mbti}")
                            authManager.saveSignupCompleted(true)
                            moveToMain()
                        } else {
                            Log.i(TAG, "자동 로그인 - MBTI 미등록 유저 검사 화면 유도")
                            authManager.saveSignupCompleted(false)
                            moveToMbtiIntro()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "자동 로그인 중 Supabase 통신 실패: ${e.message}")
                    withContext(Dispatchers.Main) {
                        moveToMbtiIntro()
                    }
                }
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
                val nickname = user.kakaoAccount?.profile?.nickname ?: "사용자"
                val kakaoEmail = user.kakaoAccount?.email ?: "user@kakao.com"

                authManager.saveLoginSession("kakao_$kakaoId")
                authManager.saveNickname(nickname)

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val dbUser = SupabaseClient.client.postgrest["User"]
                            .select { eq("kakao_id", kakaoId) }
                            .decodeSingleOrNull<User>()

                        if (dbUser != null) {
                            if (!dbUser.mbti.isNullOrEmpty()) {
                                withContext(Dispatchers.Main) {
                                    Log.i(TAG, "기존 유저 로그인 성공 MBTI: ${dbUser.mbti}")
                                    authManager.saveSignupCompleted(true)
                                    moveToMain(kakaoEmail)
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Log.i(TAG, "가입은 되어있으나 MBTI 미완료 유저")
                                    authManager.saveSignupCompleted(false)
                                    moveToMbtiIntro(kakaoEmail)
                                }
                            }
                        } else {
                            val newUser = User(kakao_id = kakaoId, nickname = nickname)
                            SupabaseClient.client.postgrest["User"].insert(newUser)
                            withContext(Dispatchers.Main) {
                                Log.i(TAG, "신규 유저 가입 및 DB 저장 완료")
                                authManager.saveSignupCompleted(false)
                                moveToMbtiIntro(kakaoEmail)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Supabase 통신 실패: ${e.message}")
                        withContext(Dispatchers.Main) {
                            if (authManager.isMbtiCompleted()) {
                                moveToMain(kakaoEmail)
                            } else {
                                moveToMbtiIntro(kakaoEmail)
                            }
                        }
                    }
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