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
import androidx.recyclerview.widget.LinearLayoutManager
import com.enugu.pension.constant.AppConstants
import com.enugu.pension.R
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.databinding.FragmentAccountBinding
import com.enugu.pension.databinding.FragmentGratuityBinding
import com.enugu.pension.model.ui.GratuityItem
import com.enugu.pension.network.ApiClient
import com.enugu.pension.ui.adapter.GratuityAdapter
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.ui.fragment.kin.KinProfileFragment
import com.enugu.pension.viewmodel.AccountViewModel
import com.enugu.pension.viewmodel.DashboardViewModel
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.GratuityViewModel
import com.enugu.pension.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class GratuityFragment : BaseFragment() {
    private lateinit var binding: FragmentGratuityBinding
    private lateinit var viewModel: GratuityViewModel
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGratuityBinding.inflate(inflater, container, false)
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
        ).get(GratuityViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }

    private fun initViews() {
        binding.tvEmptyMessage.isGone = true
        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeLiveData() {
        viewModel.accountDetailsResult.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.detail?.status == AppConstants.SUCCESS) {
                    dismissLoader()
                    populateViews2()
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

    private fun populateViews2() {
        val gratuityList = mutableListOf<GratuityItem>()
        binding.ivNaira.isGone = true
        viewModel.accountDetailsResult.value?.detail?.accountData?.gratuity?.forEach {
            gratuityList.add(
                GratuityItem(
                    id = it.id,
                    amount = it.amount,
                    paymentDate = it.paymentDate,
                    paymentStatus = it.paymentStatus,
                    description = it.description,
                )
            )
        }
        binding.tvGratuity.text = if (gratuityList.isNotEmpty()) "NGN "+ gratuityList[0].amount.toString() else ""
        binding.rvGratuity.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = GratuityAdapter(gratuityList)
        }
        binding.tvEmptyMessage.isGone = gratuityList.isNotEmpty()
    }

    private fun fetchAccountDetails() {
        if (viewModel.accountDetailsResult.value == null && confirmInternet()) {
            showLoader()
            viewModel.fetchDashboardDetails()
        }
    }

}