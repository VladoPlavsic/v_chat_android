package com.vessenger.security

import android.util.Base64
import java.security.PublicKey
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher


class PublicKey constructor(private val rawKey: ByteArray){
    private val key: X509EncodedKeySpec = X509EncodedKeySpec(rawKey)
    private val publicSpec: PublicKey = KeyFactory.getInstance("RSA").generatePublic(key)
    private val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")

    init {
        this.cipher.init(Cipher.ENCRYPT_MODE, publicSpec)
    }

    fun encode(message: String) : String{
        return Base64.encodeToString(this.cipher.doFinal(message.toByteArray()), Base64.DEFAULT)
    }

    fun keyAsBase64() : String {
        return Base64.encodeToString(this.rawKey, Base64.DEFAULT)
    }
}