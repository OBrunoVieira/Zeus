package com.brunovieiracruz.zeus.presentation.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.*
import android.support.design.widget.Snackbar
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.brunovieiracruz.zeus.R
import com.brunovieiracruz.zeus.model.FingerPrintHandler
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.Cipher.ENCRYPT_MODE
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


class UserRegistrationActivity : BaseActivity(), FingerPrintHandler.Callback {
    private lateinit var viewContentRoot: View
    private lateinit var textViewFingerPrintHint: TextView
    private lateinit var textViewFingerPrintError: TextView

    private var cryptoObject: FingerprintManagerCompat.CryptoObject? = null
    private var fingerPrintHandler: FingerPrintHandler? = null
    private var keyGenerator: KeyGenerator? = null
    private var keyStore: KeyStore? = null

    companion object {
        private const val PROVIDER_KEY = "AndroidKeyStore"
        private const val KEY_NAME = "WHATEVER"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_registration)
        viewContentRoot = findViewById(R.id.activity_user_registration_content_root)
        textViewFingerPrintHint = findViewById(R.id.activity_user_registration_text_view_fingerprint_hint)
        textViewFingerPrintError = findViewById(R.id.activity_user_registration_text_view_fingerprint_error)

        val fingerprintManager = FingerprintManagerCompat.from(this)
        if (SDK_INT >= M && fingerprintManager.isHardwareDetected && fingerprintManager.hasEnrolledFingerprints()) {
            generateKey()

            val cipher = try {
                Cipher.getInstance("$KEY_ALGORITHM_AES/$BLOCK_MODE_CBC/$ENCRYPTION_PADDING_PKCS7")
            } catch (exception: Exception) {
                throw RuntimeException("Failed to get Cipher", exception)
            }

            if (cipherInit(cipher)) {
                cryptoObject = FingerprintManagerCompat.CryptoObject(cipher)
                fingerPrintHandler = FingerPrintHandler(this, this)
            }
        } else {
            Snackbar.make(viewContentRoot, "Fingerprint authentication is not supported.", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        cryptoObject?.let {
            fingerPrintHandler?.startListening(it)
        }
    }

    override fun onPause() {
        super.onPause()
        fingerPrintHandler?.stopListening()
    }

    override fun onAuthenticated() {
        startActivity(Intent(this, AuthUserActivity::class.java))
    }

    override fun onError(stringError: String) {
        textViewFingerPrintError.text = stringError
        textViewFingerPrintError.visibility = VISIBLE

        val objectAnimatorAngle = ObjectAnimator
                .ofFloat(textViewFingerPrintHint, "rotation", -3f, 3f, 0f).apply {
                    repeatCount = 3
                    duration = 100
                }

        textViewFingerPrintError.postDelayed({
            textViewFingerPrintError.visibility = GONE

        }, 2000)

        AnimatorSet().apply {
            play(objectAnimatorAngle)
            start()
        }
    }

    @TargetApi(M)
    private fun cipherInit(cipher: Cipher): Boolean {
        return try {
            keyStore?.load(null)
            val key = keyStore?.getKey(KEY_NAME, null) as SecretKey
            cipher.init(ENCRYPT_MODE, key)

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
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
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
