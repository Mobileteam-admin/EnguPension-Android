package com.enugu.pension.ui.fragment.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.enugu.pension.constant.AppConstants
import com.enugu.pension.R
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.databinding.FragmentAccountBinding
import com.enugu.pension.network.ApiClient
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.ui.fragment.kin.KinProfileFragment
import com.enugu.pension.util.AppUtils
import com.enugu.pension.viewmodel.AccountViewModel
import com.enugu.pension.viewmodel.DashboardViewModel
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AccountFragment : BaseFragment() {
    private lateinit var binding: FragmentAccountBinding
    private lateinit var viewModel: AccountViewModel
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        initViews()
        observeLiveData()
        fetchAccountDetails()
    }

    private fun initViewModels() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        dashboardViewModel = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(DashboardViewModel::class.java)
        viewModel = ViewModelProviders.of(
            this, EnguViewModelFactory(networkRepo)
        ).get(AccountViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }

    private fun initViews() {
        binding.imgAccountBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.clStatement.setOnClickListener {
            if (confirmInternet()) navigate(R.id.action_account_to_accountstatement)
        }
        binding.clGratuity.setOnClickListener {
            if (confirmInternet()) navigate(R.id.action_navigation_account_to_gratuity)
        }
        binding.txtKinprofile.setOnClickListener {
            viewModel.accountDetailsResult.value?.detail?.accountData?.nextOfKin?.let {
                navigate(R.id.action_account_to_kinprofile)
            }
        }
    }

    private fun observeLiveData() {
        dashboardViewModel.dashboardDetailsResult.observe(viewLifecycleOwner) { response ->
            if (response?.detail?.status == AppConstants.SUCCESS) {
                response.detail.let {
                    binding.tvWalletAmount.text = it.getWalletBalanceAmount()
                    binding.ivNaira.isGone = true
                }
            }
        }
        viewModel.accountDetailsResult.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.detail?.status == AppConstants.SUCCESS) {
                    dismissLoader()
                    populateViews()
                } else {
                    if (response.detail?.tokenStatus == AppConstants.EXPIRED) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (tokenRefreshViewModel2.fetchRefreshToken()) {
                                viewModel.fetchDashboardDetails()
                            }
                        }
                    } else {
                        dismissLoader()
                        response.detail?.message?.let { showToast(it) }
                    }
                }
            }
        }
    }

    private fun populateViews() {
        viewModel.accountDetailsResult.value?.detail?.let {
//            binding.clGratuity.isGone = it.accountData.gratuity.isEmpty()
            val status = it.accountData.currentMonthStatus ?: ""
            binding.tvStatus.text = status
            binding.tvStatus.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (status == "Active") R.color.green_middle else R.color.red
                )
            )
            if (it.pensionAmount > 0) {
                binding.tvAmountLabel.text = getString(R.string.pension_amount)
                binding.tvPensionOrSalary.text = AppUtils.getFormattedMoney(it.pensionAmount)
            } else if (it.salaryAmount > 0) {
                binding.tvAmountLabel.text = getString(R.string.salary_amount)
                binding.tvPensionOrSalary.text = AppUtils.getFormattedMoney(it.salaryAmount)
            } else {
                binding.llPensionOrSalary.isGone = true
            }

        }
    }

    private fun fetchAccountDetails() {
        if (viewModel.accountDetailsResult.value == null && confirmInternet()) {
            showLoader()
            viewModel.fetchDashboardDetails()
        }
    }

    private fun getVerificationRecordText(status: String) =
        when (status) {
            "NOT_VERIFIED" -> getString(R.string.not_verified)
            "VERIFIED" -> getString(R.string.verified)
            else -> status
        }

}