package com.example.engu_pension_verification_application.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.engu_pension_verification_application.R

class ServiceActivity : BaseActivity() {

    private lateinit var service_navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)

        service_navController = Navigation.findNavController(this, R.id.nav_host_fragment_service)
        setBackCallback()
    }

    private fun setBackCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (service_navController.currentDestination?.id == R.id.navigation_choose_service) {
                    finish()
                } else {
                    service_navController.popBackStack(R.id.navigation_choose_service, false)
                }
            }
        })
    }
}