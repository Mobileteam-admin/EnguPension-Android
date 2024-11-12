package com.example.engu_pension_verification_application.ui

import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.util.Log
import com.example.engu_pension_verification_application.utils.SharedPref


class App: Application() {
    private val prefs = SharedPref

    override fun onCreate() {
        super.onCreate()

        prefs.with(applicationContext)



    }





}