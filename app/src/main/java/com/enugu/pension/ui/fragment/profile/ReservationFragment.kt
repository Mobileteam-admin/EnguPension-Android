package com.enugu.pension.ui.fragment.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.enugu.pension.constant.AppConstants
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.network.ApiClient
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.viewmodel.DashboardViewModel
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.enugu.pension.databinding.FragmentReservationBinding
import com.enugu.pension.ui.activity.VideoCallActivity
import com.enugu.pension.util.CalendarUtils
import com.enugu.pension.viewmodel.ReservationViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay


class ReservationFragment : BaseFragment() {
    companion object {
        const val CALL_ACTIVATION_MINUTES = 5
    }

    private lateinit var binding: FragmentReservationBinding
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var viewModel: ReservationViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    private var callActivationJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentReservationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initViews()
        observeLiveData()
    }

    override fun onResume() {
        super.onResume()
        initLoadData()
    }

    override fun onPause() {
        super.onPause()
        callActivationJob?.cancel()
    }

    private fun initLoadData() {
        if (confirmInternet()) {
            showLoader()
            viewModel.fetchReservationDetails()
        } else {
            findNavController().navigateUp()
        }
    }


    private fun startTimeChecking() {
        callActivationJob = lifecycleScope.launch(Dispatchers.Main) {
            while (true) {
                checkCallActivationTime()
                delay(1000)
            }
        }
    }

    private fun checkCallActivationTime() {
        if (viewModel.startTime != null) {
            val secondsFromNow = CalendarUtils.getSecondsFromNow(viewModel.startTime!!)
            if (secondsFromNow <= CALL_ACTIVATION_MINUTES * 60) {
                viewModel.isCallButtonEnabled = true
                callActivationJob?.cancel()
            } else{
                viewModel.isCallButtonEnabled = false
            }
        }
        binding.incReservation.llVideoCall.isEnabled = viewModel.isCallButtonEnabled
    }


    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        dashboardViewModel = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(DashboardViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
        viewModel = ViewModelProviders.of(
            this, EnguViewModelFactory(networkRepo)
        ).get(ReservationViewModel::class.java)
    }

    private fun initViews() {
        binding.tvNoReservation.isGone = true
        binding.svContent.isInvisible = true
        binding.imgWalletBack.setOnClickListener { findNavController().navigateUp() }
        binding.incReservation.llVideoCall.setOnClickListener {
            if (confirmInternet()) {
                viewModel.fetchVideoCallLink()
                showLoader()
            }
        }
    }


    private fun observeLiveData() {
        dashboardViewModel.dashboardDetailsResult.observe(viewLifecycleOwner) { response ->
            if (response?.detail?.status == AppConstants.SUCCESS) populateHeader()
        }
        viewModel.reservationApiResult.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.detail?.status == AppConstants.SUCCESS) {
                    dismissLoader()
                    populateContent()
                } else {
                    if (response.detail?.tokenStatus == AppConstants.EXPIRED) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (tokenRefreshViewModel2.fetchRefreshToken()) {
                                viewModel.fetchReservationDetails()
                            }
                        }
                    } else {
                        dismissLoader()
                        binding.tvNoReservation.isVisible =
                            true//TODO: show error instead, after updating API
                    }
                }
                viewModel.resetReservationApiResult()
            }
        }
        viewModel.videoCallApiResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                val intent = Intent(requireActivity(), VideoCallActivity::class.java).apply {
                    putExtra(VideoCallActivity.EXTRA_URL, response.detail.roomUrl)
                }
                startActivity(intent)
                findNavController().popBackStack()
            } else {
                if (response.detail?.tokenStatus == AppConstants.EXPIRED) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.fetchVideoCallLink()
                        }
                    }
                } else {
                    dismissLoader()
                    response.detail?.message?.let { showToast(it) }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun populateHeader() {
        dashboardViewModel.dashboardDetailsResult.value?.detail?.let {
            val walletText = "${it.walletBalanceCurrency} ${it.walletBalanceAmount}"
            binding.tvWalletAmount.text = walletText
            binding.ivNaira.isGone = true
        }
    }
    private fun populateContent() {
        binding.incReservation.llVideoCall.isEnabled = false
        viewModel.reservationApiResult.value?.detail?.bookingData?.let {
            binding.svContent.isVisible = true
            val slot = "${it.slotStartTime} - ${it.slotEndTime}"
            val formattedDate = CalendarUtils.getFormattedString(CalendarUtils.DATE_FORMAT_1, CalendarUtils.DATE_FORMAT_3, it.bookingDate)
            binding.incReservation.tvBookingDate.text = formattedDate
            binding.incReservation.tvBookingSlot.text = slot
            binding.incReservation.tvPaidAmount.text = it.totalPayableAmount.toString()
            viewModel.startTime = CalendarUtils.getCalendar(
                CalendarUtils.DATE_TIME_FORMAT_3,
                "${it.bookingDate} ${it.slotStartTime}"
            )
            startTimeChecking()
        }
        val hasReserved = viewModel.reservationApiResult.value?.detail?.bookingData != null
        binding.tvNoReservation.isGone = hasReserved
        binding.svContent.isInvisible = !hasReserved
    }
}