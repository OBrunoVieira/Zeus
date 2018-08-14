package com.brunovieiracruz.zeus.ui.view_model

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.brunovieiracruz.zeus.domain.FingerprintInteractor
import com.brunovieiracruz.zeus.model.data.Fingerprint

public class UserRegistrationViewModel : ViewModel() {
    private val fingerPrintInteractor = FingerprintInteractor()
    var observableFingerPrint: LiveData<Fingerprint> = fingerPrintInteractor.observableFingerprint
        private set

    fun startListeningFingerprint() {
        fingerPrintInteractor.startListeningFingerprint()
    }

    fun stopListeningFingerprint() {
        fingerPrintInteractor.stopListeningFingerprint()
    }

    public class Factory : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return UserRegistrationViewModel() as T
        }
    }
}