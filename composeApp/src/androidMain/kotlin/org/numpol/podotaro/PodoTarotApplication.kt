package org.numpol.podotaro

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.numpol.podotaro.di.initKoin

class PodoTarotApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@PodoTarotApplication)
        }
    }
}