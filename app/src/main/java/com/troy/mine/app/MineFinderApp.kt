package com.troy.mine.app

import android.app.Application
import com.troy.mine.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import timber.log.Timber.DebugTree

class MineFinderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
        startKoin {
            androidContext(this@MineFinderApp)
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            modules(appModule)
        }
    }
}
