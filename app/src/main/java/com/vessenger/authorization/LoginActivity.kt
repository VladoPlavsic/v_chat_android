package com.vessenger.authorization

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity

import com.vessenger.R

class LoginActivity : ComponentActivity() {
    private lateinit var etUsername: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsername = findViewById<EditText>(R.id.et_username)
        btnLogin = findViewById<Button>(R.id.btn_login)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val intent = Intent(this, OpenChatActivity::class.java)
            intent.putExtra("username",username);
            startActivity(intent)
        }
    }
}

