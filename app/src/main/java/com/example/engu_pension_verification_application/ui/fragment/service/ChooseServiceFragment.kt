package com.example.engu_pension_verification_application.ui.fragment.service

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.OnboardingStage
import com.example.engu_pension_verification_application.util.SharedPref
import kotlinx.android.synthetic.main.fragment_choose_service.*


class ChooseServiceFragment : BaseFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_service, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when(SharedPref.onboardingStage){
            OnboardingStage.SERVICES -> setClickListeners()
            OnboardingStage.ACTIVE_BASIC_DETAILS,
            OnboardingStage.ACTIVE_DOCUMENTS,
            OnboardingStage.ACTIVE_BANK_INFO
                -> navigate(R.id.action_chooseservice_to_ActiveService, allowAnimation = false)
            OnboardingStage.RETIREE_BASIC_DETAILS,
            OnboardingStage.RETIREE_DOCUMENTS,
            OnboardingStage.RETIREE_BANK_INFO
                -> navigate(R.id.action_chooseservice_to_Retiree, allowAnimation = false)
        }

    }

    private fun setClickListeners() {
        cv_activeservice.setOnClickListener {
            SharedPref.onboardingStage = OnboardingStage.ACTIVE_BASIC_DETAILS
            navigate(R.id.action_chooseservice_to_ActiveService)
        }
        cv_retiree.setOnClickListener {
            SharedPref.onboardingStage = OnboardingStage.RETIREE_BASIC_DETAILS
            navigate(R.id.action_chooseservice_to_Retiree)
        }
    }

}