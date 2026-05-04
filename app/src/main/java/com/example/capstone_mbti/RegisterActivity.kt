package com.example.capstone_mbti

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var etId: EditText
    private lateinit var etPw: EditText
    private lateinit var btnRegister: Button

    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etId = findViewById(R.id.et_id)
        etPw = findViewById(R.id.et_pw)
        btnRegister = findViewById(R.id.btn_register)

        authManager = AuthManager(this)

        btnRegister.setOnClickListener {
            val id = etId.text.toString()
            val pw = etPw.text.toString()

            if (authManager.register(id, pw)) {
                Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "이미 존재하는 계정입니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
