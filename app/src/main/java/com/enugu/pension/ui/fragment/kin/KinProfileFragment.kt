package com.enugu.pension.ui.fragment.kin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.enugu.pension.R
import com.enugu.pension.common.constant.AppConstants
import com.enugu.pension.data.repository.NetworkRepo
import com.enugu.pension.databinding.FragmentKinProfileBinding
import com.enugu.pension.data.remote.dto.request.NextOfKinRequest
import com.enugu.pension.data.remote.api.ApiClient
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.common.util.AppUtils
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KinProfileFragment : BaseFragment() {
    private lateinit var binding:FragmentKinProfileBinding
    private lateinit var viewModel: com.enugu.pension.viewmodel.NextOfKinProfileViewModel
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
        loadDetails()
    }

    private fun initViewModels() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        viewModel = ViewModelProviders.of(
            this, EnguViewModelFactory(networkRepo)
        ).get(com.enugu.pension.viewmodel.NextOfKinProfileViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }

    private fun initViews() {
        binding.ccpPhone.registerPhoneNumberTextView(binding.etPhone)
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.llBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.llSubmit.setOnClickListener {
            if (isValidInput()) {
                showLoader()
                viewModel.updateNextOfKinDetails(
                    NextOfKinRequest(
                        nextOfKinName = binding.etKinName.text.toString(),
                        nextOfKinEmail = binding.etEmail.text.toString(),
                        nextOfKinPhoneNumber = binding.ccpPhone.fullNumberWithPlus,
                        nextOfKinAddress = binding.etAddress.text.toString(),
                        nextOfKinPinCode = binding.etPin.text.toString(),
                    )
                )
            }
        }
    }

    private fun observeLiveData() {
        viewModel.nexOfKinFetchResult.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response?.detail?.status == AppConstants.SUCCESS) {
                    dismissLoader()
                    populateViews()
                } else {
                    if (response?.detail?.tokenStatus == AppConstants.EXPIRED) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (tokenRefreshViewModel2.fetchRefreshToken()) {
                                viewModel.fetchProfileDetails()
                            }
                        }
                    } else {
                        dismissLoader()
                        showToast(response.detail?.message ?: getString(R.string.common_error_msg))
                    }
                }
            }
        }
        viewModel.nexOfKinSubmitResult.observe(viewLifecycleOwner) { pair ->
            if (pair != null) {
                val request = pair.first
                val response = pair.second
                if (response.detail?.status == AppConstants.SUCCESS) {
                    dismissLoader()
                    showToast(response.detail.message ?: "")
                    findNavController().navigateUp()
                } else {
                    if (response.detail?.tokenStatus == AppConstants.EXPIRED) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (tokenRefreshViewModel2.fetchRefreshToken()) {
                                viewModel.updateNextOfKinDetails(request)
                            }
                        }
                    } else {
                        dismissLoader()
                        showToast(response.detail?.message ?: getString(R.string.common_error_msg))
                    }
                }
            }
        }
    }

    private fun loadDetails() {
        if (confirmInternet()) {
            showLoader()
            viewModel.fetchProfileDetails()
        }
    }

    private fun populateViews() {
        viewModel.nexOfKinFetchResult.value?.detail?.nextOfKinDetails?.let {
            binding.etKinName.setText(it.nextOfKinName)
            binding.etEmail.setText(it.nextOfKinEmail)
            binding.etAddress.setText(it.nextOfKinAddress)
            binding.etPin.setText(it.nextOfKinPinCode)
            if (it.nextOfKinPhoneNumber.isNotEmpty()) binding.ccpPhone.fullNumber =
                it.nextOfKinPhoneNumber
        }
    }

    private fun isValidInput(): Boolean {
        var errorMessage: String? = null
        if (!AppUtils.isValidFullName(binding.etKinName.text?.toString())) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.name))
        } else if (!AppUtils.isValidEmailAddress(binding.etEmail.text?.toString())) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.email_id))
        } else if (binding.etPhone.text.isNullOrEmpty() || !binding.ccpPhone.isValid) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.phone_number))
        } else if (binding.etPin.text?.length != resources.getInteger(R.integer.pin_code_length)) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.pincode))
        } else if (binding.etAddress.text.isNullOrEmpty()) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.address))
        }
        errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
        return errorMessage == null
    }
}