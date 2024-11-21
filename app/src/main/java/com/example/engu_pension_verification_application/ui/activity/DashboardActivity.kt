package com.example.engu_pension_verification_application.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.util.SharedPref

class DashboardActivity : BaseActivity() {

    private lateinit var dashboard_navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        dashboard_navController = Navigation.findNavController(this, R.id.nav_host_fragment_dashboard)
    }

    override fun onStop() {
        super.onStop()
    // Save the state indicating that DashboardActivity was the last activity
        SharedPref.with(this)
    }

    override fun onStart() {
        super.onStart()
        // Initialize SharedPref
        SharedPref.with(this)
    }
}