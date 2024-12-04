package com.example.engu_pension_verification_application.ui.fragment.service

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.databinding.FragmentChooseServiceBinding
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.OnboardingStage
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.ActiveServiceViewModel
import com.example.engu_pension_verification_application.viewmodel.ChooseServiceViewModel
import com.example.engu_pension_verification_application.viewmodel.RetireeServiceViewModel


class ChooseServiceFragment : BaseFragment() {
    private lateinit var binding:FragmentChooseServiceBinding
    private val viewModel by activityViewModels<ChooseServiceViewModel>()
    private val activeServiceViewModel by activityViewModels<ActiveServiceViewModel>()
    private val retireeServiceViewModel by activityViewModels<RetireeServiceViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChooseServiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListeners()
        if (viewModel.allowAutoNavigation) {
            when (SharedPref.onboardingStage) {
                OnboardingStage.ACTIVE_BASIC_DETAILS,
                OnboardingStage.ACTIVE_DOCUMENTS,
                OnboardingStage.ACTIVE_BANK_INFO -> navigateToActiveService(false)

                OnboardingStage.RETIREE_BASIC_DETAILS,
                OnboardingStage.RETIREE_DOCUMENTS,
                OnboardingStage.RETIREE_BANK_INFO -> navigateToRetireService(false)
            }
            viewModel.allowAutoNavigation = false
        }

    }

    private fun setClickListeners() {
        binding.cvActiveservice.setOnClickListener {
            SharedPref.onboardingStage = OnboardingStage.ACTIVE_BASIC_DETAILS
            navigateToActiveService(true)
        }
        binding.cvRetiree.setOnClickListener {
            SharedPref.onboardingStage = OnboardingStage.RETIREE_BASIC_DETAILS
            navigateToRetireService(true)
        }
    }

    private fun navigateToActiveService(allowAnimation: Boolean) {
        activeServiceViewModel.currentTabPos.value = 0
        navigate(R.id.action_chooseservice_to_ActiveService, allowAnimation = allowAnimation)
    }

    private fun navigateToRetireService(allowAnimation: Boolean) {
        retireeServiceViewModel.currentTabPos.value = 0
        navigate(R.id.action_chooseservice_to_Retiree, allowAnimation = allowAnimation)
    }

}