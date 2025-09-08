package com.enugu.pension.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.enugu.pension.R
import com.enugu.pension.common.constant.AppConstants
import com.enugu.pension.data.repository.NetworkRepo
import com.enugu.pension.databinding.DialogBookingDetailsBinding
import com.enugu.pension.data.remote.api.ApiClient
import com.enugu.pension.viewmodel.BookingDetailsViewModel
import com.enugu.pension.viewmodel.DashboardViewModel
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BookingDetailsDialog private constructor() : BaseDialog() {
    private lateinit var binding:DialogBookingDetailsBinding
    private lateinit var date: String
    private lateinit var slot: String
    private lateinit var viewModel: BookingDetailsViewModel
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        date = arguments?.getString(ARG_DATE)!!
        slot = arguments?.getString(ARG_SLOT)!!
    }

    companion object {
        private const val ARG_DATE = "arg_date"
        private const val ARG_SLOT = "arg_slot"

        fun newInstance(date: String, slot: String): BookingDetailsDialog {
            val fragment = BookingDetailsDialog()
            fragment.arguments = Bundle().apply {
                putString(ARG_DATE, date)
                putString(ARG_SLOT, slot)
            }
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissLoader()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DialogBookingDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initViews()
        observeLiveData()
    }

    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        viewModel = ViewModelProviders.of(
            this, EnguViewModelFactory(networkRepo)
        ).get(BookingDetailsViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
        dashboardViewModel = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(DashboardViewModel::class.java)
    }

    private fun observeLiveData() {
        viewModel.transferApiResult.observe(viewLifecycleOwner) { pair ->
            val request = pair.first
            val response = pair.second
            if (response.detail?.status == AppConstants.SUCCESS) {
                showAlertDialog(
                    message = response.detail?.message ?: "Payment completed successfully",
                    positiveTextId = R.string.ok,
                    onPositiveClick = {
                        showLoader()
                        dashboardViewModel.fetchDashboardDetails()
                        dismiss()
                    },
                    isCancellable = false,
                )
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.transferToFinalAccount(request)
                        }
                    }
                } else {
                    dismissLoader()
                    response.detail?.message?.let { showToast(it) }
                }
            }
        }
    }

    private fun initViews() {
        binding.tvBookingDate.text = date
        binding.tvBookingSlot.text = slot
        binding.llBookingDetailsBack.setOnClickListener {
            dismiss()
        }
        binding.llBookingPayNow.setOnClickListener {
            showLoader()
            viewModel.transferToFinalAccount(binding.etDescription.text.toString())
        }
    }
}