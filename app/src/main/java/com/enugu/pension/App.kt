package com.enugu.pension

import android.app.Application
import com.enugu.pension.util.AppUtils
import com.enugu.pension.util.SharedPref


class App: Application() {
    private val prefs = SharedPref

    override fun onCreate() {
        super.onCreate()
        AppUtils.init(this)
        prefs.with(applicationContext)



    }





}