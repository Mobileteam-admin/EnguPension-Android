package com.example.engu_pension_verification_application.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.engu_pension_verification_application.R


class SplashActivity : BaseActivity() {

    private lateinit var splash_navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        splash_navController = Navigation.findNavController(this, R.id.nav_host_fragment_splash)
    }

}