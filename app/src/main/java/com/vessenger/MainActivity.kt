package com.vessenger

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.vessenger.authorization.LoginActivity
import com.vessenger.security.CertStoreSingleton
import org.java_websocket.client.WebSocketClient

class MainActivity : ComponentActivity() {

    private lateinit var certStore: CertStoreSingleton
    private lateinit var webSocketClient: WebSocketClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Figure out what needs to be done in scope of authorization.
        //  We do not want login as such, but we do want to check if there are
        //  certificates setup under specific folder.
        //  If not we need to suggest user to create a new account, or to load certificates
        //  Also, we need to provide interface for logging in on BC and allowing user to upload
        //  public key signed with his BC account.

        // Initialize CertStorage on start application
        this.certStore = CertStoreSingleton.getInstance()
        this.certStore.putApplicationContext(applicationContext)
        this.certStore.readKeys()

        this.initiateFlowBasedOnCertStore()
    }

    private fun initiateFlowBasedOnCertStore() {
        if(this.certStore.hasAuthorization())
            this.hasAuthFlow()
        else
            this.noAuthFlow()
    }

    private fun hasAuthFlow() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun noAuthFlow() {
        this.certStore.setupNewKeysPair()
        this.initiateFlowBasedOnCertStore()
    }
}