package com.enugu.pension.ui.activity

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.enugu.pension.R


class SplashActivity : BaseActivity() {

    private lateinit var splash_navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        splash_navController = Navigation.findNavController(this, R.id.nav_host_fragment_splash)
    }

}