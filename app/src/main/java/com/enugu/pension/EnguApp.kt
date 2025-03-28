package com.enugu.pension

import android.app.Application
import com.enugu.pension.util.AppUtils
import com.enugu.pension.util.SharedPref


class EnguApp: Application() {
    companion object {
        lateinit var instance: EnguApp
            private set
    }
    private val prefs = SharedPref

    override fun onCreate() {
        super.onCreate()
        instance = this
        AppUtils.init(this)
        prefs.with(applicationContext)



    }





}