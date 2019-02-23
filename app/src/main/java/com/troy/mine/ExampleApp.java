package com.troy.mine;

import android.app.Application;

import timber.log.Timber;
import timber.log.Timber.DebugTree;

public class ExampleApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        }
    }
}
