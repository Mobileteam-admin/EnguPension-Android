package com.example.engu_pension_verification_application.ui.activity

import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.response.ResponseActiveProcessingVerify
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.util.NetworkUtils
import com.example.engu_pension_verification_application.util.OnboardingStage
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.ProcessDashboardViewModel
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2
import kotlinx.android.synthetic.main.activity_process_dashboard.btn_retry
import kotlinx.android.synthetic.main.activity_process_dashboard.ll_processing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ProcessDashboardActivity : AppCompatActivity() {
    private lateinit var viewModel: ProcessDashboardViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    val prefs = SharedPref

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process_dashboard)
        initViewModels()
        observeData()
        initCall()
        initViews()
        CoroutineScope(SupervisorJob()).launch(Dispatchers.IO)  {
            val source = ImageDecoder.createSource(
                resources, R.drawable.processing
            )
            val drawable = ImageDecoder.decodeDrawable(source)

            val imageView = findViewById<ImageView>(R.id.image_view)
            imageView.post {
                imageView.setImageDrawable(drawable)
                (drawable as? AnimatedImageDrawable)?.start()
            }
        }
    }

    private fun initViews() {
        btn_retry.setOnClickListener {
            initCall()
        }
    }
    private fun initViewModels() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        viewModel = ViewModelProviders.of(
            this,
            EnguViewModelFactory(networkRepo)
        ).get(ProcessDashboardViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            this,
            EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }
    private fun observeData() {
        viewModel.verificationStatus.observe(this) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                onProcessingVerifySuccess(response)
            } else {
                if (response.detail?.status == AppConstants.EXPIRED) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.getGovtVerificationStatus()
                        }
                    }
                } else {
                    Toast.makeText(this, response.detail?.message, Toast.LENGTH_LONG).show()
                    ll_processing.isGone = true
                    btn_retry.isVisible = true
                }
            }
        }
        tokenRefreshViewModel2.tokenRefreshError.observe(this) { error ->
            if (error != null) {
                if (error.isNotEmpty()) Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                prefs.logout()
                val intent = Intent(this, SignUpActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
    private fun initCall() {
        if (NetworkUtils.isConnectedToNetwork(this)) {
            //Bank Loader commented for Loader issue
            // Loader.showLoader(requireContext())
            btn_retry.isGone = true
            ll_processing.isVisible = true
            viewModel.getGovtVerificationStatus()
        } else {
            Toast.makeText(this, "Please connect to internet", Toast.LENGTH_LONG).show()
            ll_processing.isGone = true
            btn_retry.isVisible = true
        }
    }


    private  val SPLASH_TIME: Long= 3000

    private fun onProcessingVerifySuccess(response: ResponseActiveProcessingVerify) {
        prefs.onboardingStage = OnboardingStage.DASHBOARD
        Toast.makeText(this, response.detail?.message, Toast.LENGTH_SHORT).show()
        Handler().postDelayed({
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        },SPLASH_TIME)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}