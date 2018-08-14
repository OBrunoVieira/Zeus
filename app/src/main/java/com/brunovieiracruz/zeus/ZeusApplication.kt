package com.brunovieiracruz.zeus

import android.app.Application

class ZeusApplication : Application() {
    companion object {
        lateinit var instance: ZeusApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}