package com.enugu.pension.ui.fragment.service

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.enugu.pension.R
import com.enugu.pension.databinding.FragmentChooseServiceBinding
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.util.OnboardingStage
import com.enugu.pension.util.SharedPref
import com.enugu.pension.viewmodel.ActiveServiceViewModel
import com.enugu.pension.viewmodel.ChooseServiceViewModel
import com.enugu.pension.viewmodel.RetireeServiceViewModel


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

                else -> {}
            }
            viewModel.allowAutoNavigation = false
        }

    }

    private fun setClickListeners() {
        binding.llAccountService.setOnClickListener {
            SharedPref.onboardingStage = OnboardingStage.ACTIVE_BASIC_DETAILS
            navigateToActiveService(true)
        }
        binding.llRetiree.setOnClickListener {
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