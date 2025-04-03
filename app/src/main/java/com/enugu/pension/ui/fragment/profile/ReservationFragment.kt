package com.enugu.pension.ui.fragment.profile

import android.annotation.SuppressLint
import android.content.Intent
import java.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
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
    var ss="*"


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
//        startCheckingTime()

//        test()
    }

    private fun test() { // TODO: remove
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, 125)

//        val calendar = CalendarUtils.getCalendar()


        viewModel.startTime = calendar
        binding.incReservation.tvBookingSlot.text = CalendarUtils.getFormattedString(CalendarUtils.DATE_TIME_FORMAT_2, calendar)


    }

    private fun startCheckingTime() {
        callActivationJob = lifecycleScope.launch(Dispatchers.Main) {
            while (true) {
                checkCallActivationTime()
                delay(1000)
            }
        }
    }

    private fun checkCallActivationTime() {
        if (viewModel.startTime!=null){
            val minutesFromNow = CalendarUtils.getMinutesFromNow(viewModel.startTime!!)
            binding.incReservation.tvPaidAmount.text = minutesFromNow.toString() +"   $ss"
            ss=if (ss=="*") "***" else "*"
            if (minutesFromNow <= CALL_ACTIVATION_MINUTES) {
                viewModel.isCallButtonEnabled = true
                callActivationJob?.cancel()
            }

        }
        binding.incReservation.llVideoCall.isEnabled = viewModel.isCallButtonEnabled
    }


    override fun onDestroyView() {
        super.onDestroyView()
        callActivationJob?.cancel() // Cancel the coroutine when Fragment view is destroyed
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
        binding.incReservation.llVideoCall.isEnabled = false
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
            if (response?.detail?.status == AppConstants.SUCCESS) populateViews()
        }
        viewModel.videoCallApiResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                val intent = Intent(requireActivity(), VideoCallActivity::class.java).apply {
                    putExtra(VideoCallActivity.EXTRA_URL, response.detail.roomUrl)
                }
                startActivity(intent)
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

    @SuppressLint("NotifyDataSetChanged")
    private fun populateViews() {
        dashboardViewModel.dashboardDetailsResult.value?.detail?.let {
            val walletText = "${it.walletBalanceCurrency} ${it.walletBalanceAmount.toString()}"
            binding.tvWalletAmount.text = walletText
            binding.ivNaira.isGone = true
        }
    }

}