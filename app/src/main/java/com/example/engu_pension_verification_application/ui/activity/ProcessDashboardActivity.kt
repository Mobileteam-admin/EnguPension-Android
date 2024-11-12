package com.example.engu_pension_verification_application.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.commons.Loader
import com.example.engu_pension_verification_application.model.response.ResponseActiveProcessingVerify
import com.example.engu_pension_verification_application.model.response.ResponseRefreshToken
import com.example.engu_pension_verification_application.ui.fragment.service.ProcessingVerify.Processing_Verify_CallBack
import com.example.engu_pension_verification_application.ui.fragment.service.ProcessingVerify.Processing_Verify_Presenter
import com.example.engu_pension_verification_application.ui.fragment.tokenrefresh.TokenRefreshCallBack
import com.example.engu_pension_verification_application.ui.fragment.tokenrefresh.TokenRefreshViewModel
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ProcessDashboardActivity : AppCompatActivity(), Processing_Verify_CallBack,
    TokenRefreshCallBack {
    lateinit var processingVerifyPresenter: Processing_Verify_Presenter
    private lateinit var tokenRefreshViewModel: TokenRefreshViewModel
    val prefs = SharedPref

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process_dashboard)

        processingVerifyPresenter = Processing_Verify_Presenter(this)
        tokenRefreshViewModel = TokenRefreshViewModel(this)
        initCall()

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

    private fun initCall() {
        if (this?.isConnectedToNetwork()!!) {
            //Bank Loader commented for Loader issue
            // Loader.showLoader(requireContext())
            processingVerifyPresenter.getProcessing_Verify()
        } else {
            Toast.makeText(this, "Please connect to internet", Toast.LENGTH_LONG).show()
        }
    }


    fun Context.isConnectedToNetwork(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting() ?: false
    }

    private fun clearLogin() {
        prefs.isLogin = false
        prefs.user_id = ""
        prefs.user_name = ""
        prefs.email = ""
        prefs.access_token = ""
        prefs.refresh_token = ""
    }


    private  val SPLASH_TIME: Long= 3000

    override fun onProcessingVerifySuccess(response: ResponseActiveProcessingVerify) {
        Toast.makeText(this, response.detail?.message, Toast.LENGTH_SHORT).show()
        Handler().postDelayed({
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        },SPLASH_TIME)
    }

    override fun onProcessingVerifyFailure(response: ResponseActiveProcessingVerify) {
        Loader.hideLoader()

        if (response.detail?.tokenStatus.equals("expired")) {
            Toast.makeText(this, "Please wait.....", Toast.LENGTH_SHORT).show()
            Loader.hideLoader()
            //refresh api call
            tokenRefreshViewModel.getTokenRefresh()
            processingVerifyPresenter.getProcessing_Verify()
        } else {
            Loader.hideLoader()
            Toast.makeText(
                this, response.detail?.message, Toast.LENGTH_LONG
            ).show()

        }
    }

    override fun onTokenRefreshSuccess(response: ResponseRefreshToken) {
        Loader.hideLoader()
        Toast.makeText(this, "Please wait.....", Toast.LENGTH_SHORT).show()
        initCall()
    }


    override fun onTokenRefreshFailure(response: ResponseRefreshToken) {
        Loader.hideLoader()
        Toast.makeText(
            this, response.token_detail?.message, Toast.LENGTH_LONG
        ).show()

        clearLogin()
        val intent = Intent(this, SignUpActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}