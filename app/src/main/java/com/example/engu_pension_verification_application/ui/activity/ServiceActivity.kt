package com.example.engu_pension_verification_application.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.engu_pension_verification_application.R

class ServiceActivity : AppCompatActivity() {

    private lateinit var service_navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)

        service_navController = Navigation.findNavController(this, R.id.nav_host_fragment_service)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (findNavController(R.id.nav_host_fragment_service).currentDestination?.id == R.id.navigation_choose_service) {
           // ExitAppDialog.showDialog(this, this)
            super.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }
}