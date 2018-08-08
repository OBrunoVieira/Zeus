package com.brunovieiracruz.zeus.model

import android.annotation.TargetApi
import android.content.Context
import android.os.Build.VERSION_CODES.M
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v4.os.CancellationSignal

@TargetApi(M)
class FingerPrintHandler(context: Context,
        private val callback: Callback) : FingerprintManagerCompat.AuthenticationCallback() {

    private val fingerprintManager: FingerprintManagerCompat = FingerprintManagerCompat.from(context)
    private var cancellationSignal: CancellationSignal? = null
    private var selfCancelled = false

    private val isFingerPrintAuthAvailable = fingerprintManager.isHardwareDetected
            && fingerprintManager.hasEnrolledFingerprints()

    fun startListening(cryptoObject: FingerprintManagerCompat.CryptoObject) {
//        if (ActivityCompat.checkSelfPermission(context, USE_FINGERPRINT) == PERMISSION_GRANTED) {
//        }

        if (isFingerPrintAuthAvailable) {
            selfCancelled = false
            cancellationSignal = CancellationSignal()
            fingerprintManager.authenticate(cryptoObject, 0, cancellationSignal, this, null)
        }
    }

    fun stopListening() {
        cancellationSignal?.let {
            selfCancelled = true
            it.cancel()
            cancellationSignal = null
        }
    }

    override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
        callback.onAuthenticated()
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
        callback.onError("Authentication error\n$errString")
    }

    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
        callback.onError("Authentication error\n$helpString")
    }

    override fun onAuthenticationFailed() {
        callback.onError("Authentication failed")
    }

    interface Callback {
        fun onAuthenticated()
        fun onError(stringError: String)
    }
}