package com.vessenger.security

import java.io.InputStream
import java.io.OutputStream

interface CryptoManager {
    fun setupEncryptionKeys()
    fun encrypt(byteArray: ByteArray, outputStream: OutputStream): ByteArray
    fun decrypt(inputStream: InputStream): ByteArray
}