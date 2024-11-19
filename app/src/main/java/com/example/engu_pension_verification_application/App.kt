package com.example.engu_pension_verification_application

import android.app.Application
import com.example.engu_pension_verification_application.util.SharedPref


class App: Application() {
    private val prefs = SharedPref

    override fun onCreate() {
        super.onCreate()

        prefs.with(applicationContext)



    }





}