package com.enugu.pension.ui.activity

import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.enugu.pension.common.constant.AppConstants
import com.enugu.pension.R
import com.enugu.pension.data.repository.NetworkRepo
import com.enugu.pension.databinding.ActivityProcessDashboardBinding
import com.enugu.pension.data.remote.api.ApiClient
import com.enugu.pension.common.util.NetworkUtils
import com.enugu.pension.common.util.OnboardingStage
import com.enugu.pension.data.local.SharedPref
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.ProcessDashboardViewModel
import com.enugu.pension.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ProcessDashboardActivity : BaseActivity() {
    private lateinit var binding:ActivityProcessDashboardBinding
    private lateinit var viewModel: ProcessDashboardViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    val prefs = SharedPref

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProcessDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        binding.btnRetry.setOnClickListener {
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
                    binding.llProcessing.isGone = true
                    binding.btnRetry.isVisible = true
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
            // showLoader()
            binding.btnRetry.isGone = true
            binding.llProcessing.isVisible = true
            viewModel.getGovtVerificationStatus()
        } else {
            Toast.makeText(this, "Please connect to internet", Toast.LENGTH_LONG).show()
            binding.llProcessing.isGone = true
            binding.btnRetry.isVisible = true
        }
    }


    private  val SPLASH_TIME: Long= 3000

    private fun onProcessingVerifySuccess(response: com.enugu.pension.data.remote.dto.response.ResponseActiveProcessingVerify) {
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