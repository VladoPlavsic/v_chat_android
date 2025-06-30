package com.vessenger.security

import android.content.Context
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyPairGenerator
import java.security.SecureRandom
import kotlin.concurrent.Volatile

class CertStoreSingleton constructor() {
    companion object {
        @Volatile
        private var instance: CertStoreSingleton? = null

        fun getInstance() =
            instance ?: synchronized(this) { // synchronized to avoid concurrency problem
                instance ?: CertStoreSingleton().also { instance = it }
            }
    }

    private val cryptoManager: CryptoManager = CryptoManagerImpl()

    private lateinit var context: Context
    private var privateKey: PrivateKey? = null
    private var publicKey: PublicKey? = null

    private val privateKeyName: String = "privateKey"
    private val publicKeyName: String = "publicKey"

    fun readKeys() {
        try {
            this.doReadKeys();
        } catch (e: Exception) {
            Log.d("CertStore:Init", "Couldn't read keys.", e)
        }
    }

    fun putApplicationContext(applicationContext: Context) {
        this.context = applicationContext
    }

    fun hasAuthorization() : Boolean {
        return this.privateKey != null && this.publicKey != null
    }

    fun getPublicKey() : PublicKey? {
        return this.publicKey
    }

    fun setupNewKeysPair() {
        var keys = this.generateKeys()

        this.writeFileContents(this.privateKeyName, keys[0])
        this.writeFileContents(this.publicKeyName, keys[1])
    }

    private fun generateKeys() : Array<String> {
        val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
        generator.initialize(2048, SecureRandom())

        val keyPair =  generator.generateKeyPair()

        return arrayOf(
            Base64.encodeToString(keyPair.private.encoded, Base64.DEFAULT),
            Base64.encodeToString(keyPair.public.encoded, Base64.DEFAULT)
        )
    }

    private fun writeFileContents(fileName: String, fileContent: String) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            this.cryptoManager.encrypt(fileContent.toByteArray(), it)
        }
    }

    private fun doReadKeys() {
        this.privateKey = PrivateKey(this.doReadKey(this.privateKeyName))
        this.publicKey = PublicKey(this.doReadKey(this.publicKeyName))
    }

    private fun doReadKey(fileName: String) : ByteArray {
        return context.openFileInput(fileName).use { inputStream ->
            val v = this.cryptoManager.decrypt(inputStream)
            return Base64.decode(v, Base64.DEFAULT)
        }
    }

    fun decode(encryptedData: ByteArray) : String {
        return this.privateKey!!.decode(encryptedData)
    }

    fun encode(message: String) : String {
        return this.publicKey!!.encode(message)
    }

}