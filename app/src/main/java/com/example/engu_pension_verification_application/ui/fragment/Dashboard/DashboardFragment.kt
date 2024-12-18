package com.example.engu_pension_verification_application.ui.fragment.Dashboard

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.databinding.FragmentDashboardBinding
import com.example.engu_pension_verification_application.model.response.ResponseLogout
import com.example.engu_pension_verification_application.model.response.VideoCallResponse
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.activity.SignUpActivity
import com.example.engu_pension_verification_application.ui.dialog.AddBankDialog
import com.example.engu_pension_verification_application.ui.dialog.AppointmentDialog
import com.example.engu_pension_verification_application.ui.dialog.EnguDialog
import com.example.engu_pension_verification_application.ui.dialog.LogoutConfirmDialog
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.NetworkUtils
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.DashboardViewModel
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.LogoutConfirmViewModel
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.webrtc.PeerConnectionFactory
import java.net.URL


class DashboardFragment : BaseFragment() {
    private lateinit var binding:FragmentDashboardBinding
    private lateinit var logoutConfirmDialog: LogoutConfirmDialog
    private lateinit var addBankDialog: AddBankDialog
    private lateinit var appointmentDialog: AppointmentDialog
    private lateinit var viewModel: DashboardViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    private val logoutConfirmViewModel by activityViewModels<LogoutConfirmViewModel>()
    val prefs = SharedPref
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClicked()
        initViewModel()
        initViews()
        initCall()
        observeLiveData()
    }

    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(),
            EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
        viewModel = ViewModelProviders.of(
            requireActivity(),
            EnguViewModelFactory(networkRepo)
        ).get(DashboardViewModel::class.java)
    }

    private fun observeLiveData() {
        logoutConfirmViewModel.logout.observe(viewLifecycleOwner) { logout ->
            if (logout != null) callLogout()
        }
        viewModel.logoutResult.observe(viewLifecycleOwner) { response ->
            if (response.logout_detail?.status == AppConstants.SUCCESS) {
                onLogoutSuccess(response)
            } else {
                if (response.logout_detail?.tokenStatus == AppConstants.EXPIRED) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.logout()
                        }
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, response.logout_detail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        viewModel.videoCallApiResult.observe(viewLifecycleOwner) { pair ->
            val request = pair.first
            val response = pair.second
            if (response.detail?.status == AppConstants.SUCCESS) {
                startJitsiMeet(response)
            } else {
                if (response.detail?.tokenStatus == AppConstants.EXPIRED) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.fetchVideoCallLink(request)
                        }
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        viewModel.dashboardDetailsResult.observe(viewLifecycleOwner) { response ->
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
                    showFetchErrorDialog(
                        ::initCall,
                        response.detail?.message ?: getString(R.string.common_error_msg_2)
                    )
                }
            }
        }
    }

    private fun initViews() {
        logoutConfirmDialog = LogoutConfirmDialog()
        addBankDialog = AddBankDialog()
        appointmentDialog = AppointmentDialog()
    }

    private fun initCall() {
        if (NetworkUtils.isConnectedToNetwork(requireContext())) {
            showLoader()
            viewModel.fetchDashboardDetails()
        } else {
            showFetchErrorDialog(::initCall,R.string.no_internet_error)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onClicked() {
        binding.imgBell.setOnClickListener { // TODO: remove after video call api completion
//            val callLink =
//                "https://project-one.org/v_call_ser/meeting_6c91334d-5a44-4e79-8a23-599458093cd6?jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJqaXRzaSIsImlzcyI6InByb2plY3Qtb25lIiwic3ViIjoicHJvamVjdC1vbmUub3JnIiwicm9vbSI6Im1lZXRpbmdfNmM5MTMzNGQtNWE0NC00ZTc5LThhMjMtNTk5NDU4MDkzY2Q2IiwiZXhwIjoxNzMzNzI5ODEwLCJjb250ZXh0Ijp7InVzZXIiOnsiZW1haWwiOiJtdWhhbW1hZC5mYWlzYWxAdGVjaHZlcnNhbnRpbmZvdGVjaC5jb20iLCJuYW1lIjoibXVoYW1tYWQuZmFpc2FsIiwibW9kZXJhdG9yIjp0cnVlfX19.WAyx2DjNui38M3Sh2plLI2HphzqLUIElMnV3QrfF-r8"
//
//            startJitsiMeetCall(callLink)
//            if (NetworkUtils.isConnectedToNetwork(requireContext())) {
//                val videoCallRequest = VideoCallRequest(
//                    govtOfficialEmail = "8adm3eqs29@zlorkun.com",
//                    userEmail = "avin@techversantinfo.com",
//                    callDay = "10/12/2024",
//                    slotId = 33
//                )
//                viewModel.fetchVideoCallLink(videoCallRequest)
//                showLoader()
//            } else {
//            }
        }
        binding.tvProfile.setOnClickListener {
            navigate(R.id.action_dashboard_to_profile)
        }
        binding.imgAddAmount.setOnClickListener {
            navigate(R.id.action_dashboard_to_wallet)
        }
        binding.llAccount.setOnClickListener {
            navigate(R.id.action_dashboard_to_account)
        }
        binding.llAddBank.setOnClickListener {
            showDialog(addBankDialog)
        }
        binding.llAppoinment.setOnClickListener {
            showDialog(appointmentDialog)
        }
        binding.txtLogout.setOnClickListener {
            showDialog(logoutConfirmDialog)
        }
    }

    private fun callLogout() {
        showLoader()
        if (NetworkUtils.isConnectedToNetwork(requireContext())) {
            viewModel.logout()
        } else {
            dismissLoader()
            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
        }
    }

    private fun populateViews() {
        viewModel.dashboardDetailsResult.value?.detail?.let {
            Glide.with(this)
                .load(it.profilePic)
                .into(binding.imgProfile)
            binding.tvPersonName.text = it.fullName
            val walletText = "${it.walletBalanceCurrency} ${it.walletBalanceAmount.toString()}"
            binding.tvWalletAmount.text = walletText
            binding.ivNaira.isGone = true
            if (it.verificationStatus == true) {
                binding.tvVerificationStatus.text = getString(R.string.verified)
                binding.tvVerificationStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_dark))
                binding.ivVerificationStatus.setImageResource(R.drawable.ic_tick_green)
            } else {
                binding.tvVerificationStatus.text = getString(R.string.not_verified)
                binding.tvVerificationStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                binding.ivVerificationStatus.setImageResource(R.drawable.ic_not_verified_red)
            }
            if (true == true) { // TODO: Modify this whenever update the API and the response includes status
                binding.tvBookAppointment.text = getString(R.string.book_appointment)
                binding.tvBookAppointment.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_500))
                binding.ivBookAppointment.setImageResource(R.drawable.ic_schedule)
            } else {
                binding.tvBookAppointment.text = getString(R.string.valid_till_date, "01/01/2025")
                binding.tvBookAppointment.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                binding.ivBookAppointment.setImageResource(R.drawable.ic_not_verified_red)
            }
            it.bankDetail?.let { bankDetail ->
                binding.noBankMsg.visibility = View.GONE
                binding.clDashboardBank.visibility = View.VISIBLE
                if (bankDetail.bankImage != null)
                    Glide.with(this)
                        .load(bankDetail.bankImage)
                        .into(binding.imgBankIcon)
                binding.tvBankname.text = bankDetail.bankName
                binding.tvBanktype.text = bankDetail.accountType
            }
        }
    }

    private fun onLogoutSuccess(response: ResponseLogout) {
        dismissLoader()
        Toast.makeText(context, response.logout_detail?.message, Toast.LENGTH_LONG).show()
        prefs.logout()
        val intent = Intent(context, SignUpActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun startJitsiMeet(response: VideoCallResponse) {
        dismissLoader()
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(requireContext())
                .setEnableInternalTracer(true)
                .createInitializationOptions())
        val options = JitsiMeetConferenceOptions.Builder()
            .setRoom(response.detail?.roomName)
            .setFeatureFlag("welcomepage.enabled", false)
            .setAudioMuted(true)
            .setVideoMuted(true)
            .build()
        JitsiMeetActivity.launch(requireContext(), options)
    }

    private fun startJitsiMeetCall(callLink: String) {
        try {
            val options: JitsiMeetConferenceOptions = JitsiMeetConferenceOptions.Builder()
                .setServerURL(URL(callLink))
                .setRoom(callLink)
                .setAudioOnly(false)
                .build()

            JitsiMeetActivity.launch(requireContext(), options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}