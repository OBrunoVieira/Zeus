package com.brunovieiracruz.zeus.domain

import android.annotation.TargetApi
import android.arch.lifecycle.MutableLiveData
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.*
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import com.brunovieiracruz.zeus.ZeusApplication
import com.brunovieiracruz.zeus.common.FingerPrintHandler
import com.brunovieiracruz.zeus.model.data.Fingerprint
import com.brunovieiracruz.zeus.model.data.Fingerprint.Companion.AUTHENTICATED
import com.brunovieiracruz.zeus.model.data.Fingerprint.Companion.ERROR
import com.brunovieiracruz.zeus.model.data.Fingerprint.Companion.NOT_SUPPORTED
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class FingerprintInteractor {
    private var keyGenerator: KeyGenerator? = null
    private var keyStore: KeyStore? = null
    private var cryptoObject: FingerprintManagerCompat.CryptoObject? = null
    private var fingerPrintHandler: FingerPrintHandler? = null
    private var context = ZeusApplication.instance.applicationContext

    val observableFingerprint: MutableLiveData<Fingerprint> = MutableLiveData()

    companion object {
        private const val PROVIDER_KEY = "AndroidKeyStore"
        private const val KEY_NAME = "key_name"
    }

    init {
        val fingerprintManager = FingerprintManagerCompat.from(context)

        if (SDK_INT >= M && fingerprintManager.isHardwareDetected && fingerprintManager.hasEnrolledFingerprints()) {
            generateKey()

            val cipher = try {
                Cipher.getInstance("$KEY_ALGORITHM_AES/$BLOCK_MODE_CBC/$ENCRYPTION_PADDING_PKCS7")
            } catch (exception: Exception) {
                throw RuntimeException("Failed to get Cipher", exception)
            }

            if (cipherInit(cipher)) {
                cryptoObject = FingerprintManagerCompat.CryptoObject(cipher)
                fingerPrintHandler = FingerPrintHandler(context, object : FingerPrintHandler.Callback {
                    override fun onAuthenticated() {
                        observableFingerprint.postValue(Fingerprint(AUTHENTICATED))
                    }

                    override fun onError(stringError: String) {
                        observableFingerprint.postValue(Fingerprint(ERROR, stringError))
                    }

                })
            }
        } else {
            observableFingerprint.postValue(Fingerprint(NOT_SUPPORTED))
        }

    }

    fun startListeningFingerprint() {
        cryptoObject?.let {
            fingerPrintHandler?.startListening(it)
        }
    }

    fun stopListeningFingerprint() {
        fingerPrintHandler?.stopListening()
    }

    @TargetApi(M)
    private fun cipherInit(cipher: Cipher): Boolean {
        return try {
            keyStore?.load(null)
            val key = keyStore?.getKey(KEY_NAME, null) as SecretKey
            cipher.init(Cipher.ENCRYPT_MODE, key)

            true
        } catch (exception: KeyPermanentlyInvalidatedException) {
            false
        } catch (exception: Exception) {
            throw RuntimeException("Failed to init Cipher", exception)
        }
    }

    @TargetApi(M)
    private fun generateKey() {
        try {
            keyStore = KeyStore.getInstance(PROVIDER_KEY)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        try {
            keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM_AES, PROVIDER_KEY)
        } catch (exception: Exception) {
            throw RuntimeException("Failed to get KeyGenerator instance", exception)
        }

        try {
            keyStore?.load(null)
            keyGenerator?.let {
                it.init(KeyGenParameterSpec.Builder(KEY_NAME,
                        PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
                        .setBlockModes(BLOCK_MODE_CBC)
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build())
                it.generateKey()
            }
        } catch (exception: Exception) {
            throw RuntimeException(exception)
        }
    }
}