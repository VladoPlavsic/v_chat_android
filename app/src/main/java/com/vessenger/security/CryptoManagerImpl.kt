package com.vessenger.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import com.vessenger.security.Cipher as CipherConfig


class CryptoManagerImpl(
    private val cipherConfig: CipherConfig? = null
): CryptoManager {

    private lateinit var cipher: CipherConfig
    private lateinit var algorithm: String
    private lateinit var blockMode: String
    private lateinit var padding: String
    private lateinit var transformation: String

    init {
        setupEncryptionKeys()
    }

    override fun setupEncryptionKeys() {
        cipher = cipherConfig?.let { return@let it } ?: CipherConfig.Builder().build()

        algorithm = cipher.algorithm
        blockMode = cipher.blockMode
        padding = cipher.padding
        transformation = "$algorithm/$blockMode/$padding"
    }

    override fun encrypt(byteArray: ByteArray, outputStream: OutputStream): ByteArray {
        val encryptedBytes = encryptCipher(byteArray)

        outputStream.use {
            it.write(encryptedBytes)
        }
        return encryptedBytes
    }

    override fun decrypt(inputStream: InputStream): ByteArray {
        return decryptCipher(inputStream.readBytes())
    }

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private fun encryptCipher(bytes: ByteArray) : ByteArray{
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, getKey())

        // Combine IV and ciphertext into a single array
        return cipher.iv + cipher.doFinal(bytes)
    }

    private fun decryptCipher(data: ByteArray) : ByteArray {
        // Extract the IV (first 12 bytes)
        val iv = data.sliceArray(0 until 12)

        // Extract the ciphertext (remaining bytes)
        val bytes : ByteArray = data.sliceArray(12 until data.size)

        val cipher = Cipher.getInstance(transformation)
        val key = getKey()
        Log.d("RawPub", data.toString())

        // Initialize with the extracted IV
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

        // Decrypt the ciphertext
        return cipher.doFinal(bytes)
    }

    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry("vessengerKey", null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey() = KeyGenerator.getInstance(algorithm).apply {
        init(
            KeyGenParameterSpec.Builder(
                "vessengerKey",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(blockMode)
                .setEncryptionPaddings(padding)
                .setUserAuthenticationRequired(false)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
    }.generateKey()
}