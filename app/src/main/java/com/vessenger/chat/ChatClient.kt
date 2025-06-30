package com.vessenger.chat

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.vessenger.R
import com.vessenger.api.websocket_client.WebSocketClient
import com.vessenger.security.CertStoreSingleton
import com.vessenger.security.PublicKey
import java.net.URI

class ChatClientActivity : ComponentActivity() {
    private var listViewItems: ArrayList<HashMap<String, Any>> = ArrayList()

    private lateinit var webSocketClient: WebSocketClient
    private lateinit var certStore: CertStoreSingleton

    private lateinit var serverUri: URI
    private lateinit var btnSend: Button
    private lateinit var editMsg: EditText
    private lateinit var listView: ListView

    private lateinit var serverPub: PublicKey
    private lateinit var peerPub: PublicKey

    private lateinit var username: String
    private lateinit var peerUsername: String

    private fun openConnection() {
        this.webSocketClient = WebSocketClient(serverUri) { message ->
            // display incoming message in ListView
            runOnUiThread {
                // On WS push callback
                run {
                    authorizeOrAddMessage(message)
                }
                this.listView.adapter = ListViewAdapter(listViewItems)
            }
        }
        // connect to websocket server
        this.webSocketClient.connect()
    }

    private fun collectComponents() {
        this.btnSend = findViewById(R.id.btn_send)
        this.editMsg = findViewById(R.id.edit_msg)
        this.listView = findViewById(R.id.list_view_chat)
    }

    private fun addMessage(message: Any) {
        val item = HashMap<String, Any>()
        item["message"] = message
        this.listViewItems.add(item)
    }

    private fun bindSendButton() {

        this.btnSend.setOnClickListener {
            try {
                // send message to websocket server
                val rawMessage = this.editMsg.text.toString()
                val encodedMessage = this.peerPub.encode(rawMessage)
                this.webSocketClient.sendMessage("send:" + this.peerUsername + ":" + encodedMessage)
                this.editMsg.setText("")
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        username = intent.getStringExtra("username")!!
        peerUsername = intent.getStringExtra("peerUsername")!!

        serverUri = URI(getString(R.string.server_url) + "/chat/" + username)

        this.certStore = CertStoreSingleton.getInstance()

        this.collectComponents()
        this.openConnection()
        this.bindSendButton()
    }

    private fun authorizeOrAddMessage(message: Any) {
        if (message is String && message.toString().take("authorize:".length) == "authorize:" ) {
            this.authorize(message)
        } else if (message is String && message.toString().take("send_public_key:".length) == "send_public_key:") {
            this.sendPublicKey()
        } else if (message is String && message.toString().take("authorized:".length) == "authorized:") {
            this.getPeerPublicKey()
        } else if (message is String && message.toString().take("peer_public_key:".length) == "peer_public_key:") {
            this.savePeerPublicKey(message)
        } else if (message is String && message.toString().take("push_message:".length) == "push_message:") {
            val b64EncodedMessage = message.subSequence("push_message:".length, message.length).toString().toByteArray()
            val encryptedMessage = Base64.decode(b64EncodedMessage, Base64.DEFAULT)
            val decodedMessage = this.certStore.decode(encryptedMessage)
            this.addMessage(decodedMessage)
        }
        else {
            this.addMessage(message)
        }
    }

    private fun savePeerPublicKey(message: String) {
        val base64PeerPubKey = message.subSequence("peer_public_key:".length, message.length).toString().toByteArray()
        this.peerPub = PublicKey(Base64.decode(base64PeerPubKey, Base64.DEFAULT))
    }

    private fun getPeerPublicKey() {
        this.sendPeerPublicKeyRequest()
    }

    private fun sendPeerPublicKeyRequest() {
        val request = buildGetPeerPublicKeyRequest()
        this.webSocketClient.sendMessage(request)
    }

    private fun buildGetPeerPublicKeyRequest() : String {
        val stringBuilder = StringBuilder("get_peer_public_key:")
        return stringBuilder.append(this.peerUsername).toString().trim()
    }

    private fun sendPublicKey() {
        this.sendPublicKeyRequest(this.certStore.getPublicKey()!!.keyAsBase64())
    }

    private fun sendPublicKeyRequest(key: String) {
        Log.d("Key", key)
        val sendPublicKeyRequest = this.buildSendPublicKeyRequest(key)
        Log.d("Public", sendPublicKeyRequest)
        this.webSocketClient.sendMessage(sendPublicKeyRequest)
    }

    private fun buildSendPublicKeyRequest(key: String) : String {
        val stringBuilder = StringBuilder("send_public_key:")
        return stringBuilder.append(key).toString().trim()
    }

    private fun authorize(message: String) {
        val code = this.getCodeFromMessage(message)
        this.getServerPublicKeyFromMessage(message)
        // TODO: Remove this
            this.addMessage(code)
        // END TODO
        this.sendAuthorizeRequest(code)
    }

    private fun getServerPublicKeyFromMessage(message: String) {
        val publicKeyAt = message.indexOf("@")
        val base64EncryptedPublicKey = message.subSequence(publicKeyAt, message.length).toString().toByteArray()
        this.serverPub = PublicKey(Base64.decode(base64EncryptedPublicKey, Base64.DEFAULT))
    }

    private fun getCodeFromMessage(message: String) : String {
        val publicKeyAt = message.indexOf("@")
        val base64EncryptedCode = message.subSequence(10, publicKeyAt).toString().toByteArray()
        val encryptedCode = Base64.decode(base64EncryptedCode, Base64.DEFAULT)
        return this.certStore.decode(encryptedCode)
    }

    private fun sendAuthorizeRequest(code: String) {
        val authorizeString = this.buildAuthorizeRequest(code)
        this.webSocketClient.sendMessage(authorizeString)
    }

    private fun buildAuthorizeRequest(code: String) : String {
        val encryptedCode = this.serverPub.encode(code)
        val stringBuilder = StringBuilder("authorize:")
        return stringBuilder.append(encryptedCode).toString().trim()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.webSocketClient.close()
    }
}