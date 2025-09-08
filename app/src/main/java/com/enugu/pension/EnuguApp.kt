package com.enugu.pension

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.enugu.pension.common.util.AppUtils
import com.enugu.pension.data.local.SharedPref


class EnuguApp: Application() {
    companion object {
        lateinit var instance: EnuguApp
            private set
    }
    private val prefs = SharedPref

    override fun onCreate() {
        super.onCreate()
        instance = this
        AppUtils.init(this)
        prefs.with(applicationContext)
        setWindowInsets()
    }
    private fun setWindowInsets() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                val statusBarColor = ContextCompat.getColor(applicationContext, R.color.white_light)
                val windowController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
                windowController.isAppearanceLightStatusBars = true
                activity.window.decorView?.let { decorView ->
                    val rootView = decorView.findViewById<View>(android.R.id.content)
                    ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
                        val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                        view.setPadding(
                            systemBarsInsets.left,
                            systemBarsInsets.top,
                            systemBarsInsets.right,
                            systemBarsInsets.bottom
                        )
                        view.setBackgroundColor(statusBarColor)
                        insets
                    }
                    rootView.requestApplyInsets()
                }
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

}