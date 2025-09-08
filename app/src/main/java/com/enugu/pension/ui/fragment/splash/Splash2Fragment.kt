package com.enugu.pension.ui.fragment.splash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.enugu.pension.databinding.FragmentSplash2Binding
import com.enugu.pension.ui.activity.DashboardActivity
import com.enugu.pension.ui.activity.ProcessDashboardActivity
import com.enugu.pension.ui.activity.ServiceActivity
import com.enugu.pension.ui.activity.SignUpActivity
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.common.util.OnboardingStage
import com.enugu.pension.data.local.SharedPref


class Splash2Fragment : BaseFragment() {
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
        binding.root.setOnClickListener {
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

