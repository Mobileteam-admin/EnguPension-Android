package com.enugu.pension.ui.fragment.kin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.enugu.pension.R
import com.enugu.pension.constant.AppConstants
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.databinding.FragmentKinProfileBinding
import com.enugu.pension.model.misc.KeyValue
import com.enugu.pension.model.response.NextOfKinResponse
import com.enugu.pension.network.ApiClient
import com.enugu.pension.ui.adapter.KinProfileAdapter
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.NextOfKinProfileViewModel
import com.enugu.pension.viewmodel.TokenRefreshViewModel2

class KinProfileFragment : BaseFragment() {
    private lateinit var binding:FragmentKinProfileBinding
    private lateinit var viewModel: NextOfKinProfileViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentKinProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        initViews()
        observeLiveData()
        showLoader()
        viewModel.fetchProfileDetails()
    }

    private fun initViewModels() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        viewModel = ViewModelProviders.of(
            this, EnguViewModelFactory(networkRepo)
        ).get(NextOfKinProfileViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }

    private fun initViews() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeLiveData() {
        viewModel.nexOfKinApiResult.observe(viewLifecycleOwner) { response ->
            dismissLoader()
            if (response?.detail?.status == AppConstants.SUCCESS) {
                populateViews()
            }
        }
    }

    private fun populateViews() {
        val kinDetails = viewModel.nexOfKinApiResult.value?.detail?.nextOfKinDetails?: NextOfKinResponse.Detail.NextOfKinDetails()
        binding.tvKinName.text = kinDetails.nextOfKinName
        val items = listOf(
            KeyValue(getString(R.string.email_id), kinDetails.nextOfKinEmail),
            KeyValue(getString(R.string.phone), kinDetails.nextOfKinPhoneNumber),
            KeyValue(getString(R.string.address), kinDetails.nextOfKinAddress)
        )
        binding.rvProfile.adapter = KinProfileAdapter(items)
    }
}