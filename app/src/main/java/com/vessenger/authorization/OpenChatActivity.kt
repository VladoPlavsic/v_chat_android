package com.vessenger.authorization

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import com.vessenger.R
import com.vessenger.chat.ChatClientActivity


class OpenChatActivity  : ComponentActivity()  {

    private lateinit var etUsername : EditText
    private lateinit var btnStartChat: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_chat)

        etUsername = findViewById<EditText>(R.id.et_peer_username)
        btnStartChat = findViewById<Button>(R.id.btn_start_chat)

        val username = intent.getStringExtra("username")


        btnStartChat.setOnClickListener {
            val intent = Intent(this, ChatClientActivity::class.java)
            intent.putExtra("username", username)
            intent.putExtra("peerUsername", etUsername.text.toString())
            startActivity(intent)
        }
    }


}