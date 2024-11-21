package com.example.engu_pension_verification_application.ui.fragment.splash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.ui.activity.DashboardActivity
import com.example.engu_pension_verification_application.ui.activity.ProcessDashboardActivity
import com.example.engu_pension_verification_application.ui.activity.ServiceActivity
import com.example.engu_pension_verification_application.ui.activity.SignUpActivity
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.OnboardingStage
import com.example.engu_pension_verification_application.util.SharedPref
import kotlinx.android.synthetic.main.fragment_splash2.*


class Splash2 : BaseFragment() {

    val prefs = SharedPref

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onClicked()
    }

    private fun onClicked() {
        img_next2.setOnClickListener {

            if (prefs.isLogin) {
                //check account completion true or not with local storage
                //true - dashboar
                /* val intent = Intent(context, DashboardActivity::class.java)
                 intent.flags =
                     Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                 startActivity(intent)*/

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

