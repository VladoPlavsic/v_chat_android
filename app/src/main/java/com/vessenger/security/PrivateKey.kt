package com.vessenger.security

import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher

class PrivateKey constructor(rawKey: ByteArray){
    private val key: PKCS8EncodedKeySpec = PKCS8EncodedKeySpec(rawKey)
    private val privateSpec: PrivateKey = KeyFactory.getInstance("RSA").generatePrivate(key)
    private val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")

    init {
        this.cipher.init(Cipher.DECRYPT_MODE, privateSpec)
    }

    fun decode(encoded: ByteArray) : String{
        return String(this.cipher.doFinal(encoded))
    }
}