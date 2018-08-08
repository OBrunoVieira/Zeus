package com.brunovieiracruz.zeus.presentation.activities

import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate

open class BaseActivity : AppCompatActivity() {
    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }
}