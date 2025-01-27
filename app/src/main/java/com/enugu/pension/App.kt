package com.enugu.pension

import android.app.Application
import com.enugu.pension.util.SharedPref


class App: Application() {
    private val prefs = SharedPref

    override fun onCreate() {
        super.onCreate()

        prefs.with(applicationContext)



    }





}