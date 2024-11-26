package com.example.engu_pension_verification_application.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2

class DashboardActivity : BaseActivity() {

    private lateinit var dashboard_navController: NavController
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        dashboard_navController = Navigation.findNavController(this, R.id.nav_host_fragment_dashboard)
        initViewModel()
        observeLiveData()
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
    private fun observeLiveData() {
        tokenRefreshViewModel2.tokenRefreshError.observe(this) { error ->
            if (error != null) {
                onTokenRefreshFailure(error)
            }
        }
    }
    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        tokenRefreshViewModel2 = ViewModelProviders.of(
            this,
            EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }
    private fun onTokenRefreshFailure(error: String) {
        dismissLoader()
        if (error.isNotEmpty()) Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        SharedPref.logout()
        val intent = Intent(this, SignUpActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}