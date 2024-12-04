package com.example.engu_pension_verification_application.ui.fragment.splash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.databinding.FragmentSplash1Binding
import com.example.engu_pension_verification_application.databinding.FragmentSplash2Binding
import com.example.engu_pension_verification_application.ui.activity.DashboardActivity
import com.example.engu_pension_verification_application.ui.activity.ProcessDashboardActivity
import com.example.engu_pension_verification_application.ui.activity.ServiceActivity
import com.example.engu_pension_verification_application.ui.activity.SignUpActivity
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.OnboardingStage
import com.example.engu_pension_verification_application.util.SharedPref


class Splash2 : BaseFragment() {
    private lateinit var binding:FragmentSplash2Binding
    val prefs = SharedPref

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplash2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onClicked()
    }

    private fun onClicked() {
        binding.imgNext2.setOnClickListener {
            if (prefs.isLogin) {
                val intent = when (prefs.onboardingStage) {
                    OnboardingStage.DASHBOARD -> Intent(context, DashboardActivity::class.java)
                    OnboardingStage.PROCESSING -> Intent(context, ProcessDashboardActivity::class.java)
                    else -> Intent(context, ServiceActivity::class.java)
                }.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)

                //false - service
                /*val intent = Intent(context, ServiceActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)*/

               /* val intent = Intent(context, DashboardActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)*/
            } else {
                val intent = Intent(context, SignUpActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}

