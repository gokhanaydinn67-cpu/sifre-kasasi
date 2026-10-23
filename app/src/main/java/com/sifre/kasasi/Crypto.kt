package com.sifre.kasasi

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object Crypto {
    private const val ALIAS = "sifre_kasasi_key"
    private const val PROVIDER = "AndroidKeyStore"
    private const val TRANSFORM = "AES/GCM/NoPadding"

    private fun getKey(): SecretKey {
        val ks = KeyStore.getInstance(PROVIDER)
        ks.load(null)
        val existing = ks.getKey(ALIAS, null) as? SecretKey
        if (existing != null) return existing

        val gen = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, PROVIDER
        )
        gen.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return gen.generateKey()
    }

    fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance(TRANSFORM)
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val iv = cipher.iv
        val data = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        val all = iv + data
        return Base64.encodeToString(all, Base64.NO_WRAP)
    }

    fun decrypt(encoded: String): String {
        val all = Base64.decode(encoded, Base64.NO_WRAP)
        val iv = all.copyOfRange(0, 12)
        val data = all.copyOfRange(12, all.size)
        val cipher = Cipher.getInstance(TRANSFORM)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(data), Charsets.UTF_8)
    }
}
