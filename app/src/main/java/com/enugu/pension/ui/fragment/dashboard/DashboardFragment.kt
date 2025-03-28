package com.enugu.pension.ui.fragment.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.enugu.pension.constant.AppConstants
import com.enugu.pension.R
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.databinding.FragmentDashboardBinding
import com.enugu.pension.model.response.BankAccountListResponse
import com.enugu.pension.model.response.ResponseLogout
import com.enugu.pension.model.response.VideoCallResponse
import com.enugu.pension.network.ApiClient
import com.enugu.pension.network.BankAccountItem
import com.enugu.pension.ui.activity.SignUpActivity
import com.enugu.pension.ui.adapter.BankAccountAdapter
import com.enugu.pension.ui.dialog.AddBankDialog
import com.enugu.pension.ui.dialog.AppointmentDialog
import com.enugu.pension.ui.dialog.LogoutConfirmDialog
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.util.NetworkUtils
import com.enugu.pension.util.SharedPref
import com.enugu.pension.viewmodel.DashboardViewModel
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.LogoutConfirmViewModel
import com.enugu.pension.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DashboardFragment : BaseFragment() {
    companion object {
        const val MIN_BOOKING_AMOUNT = 1400f
    }
    private lateinit var binding: FragmentDashboardBinding
    private lateinit var logoutConfirmDialog: LogoutConfirmDialog
    private lateinit var addBankDialog: AddBankDialog
    private lateinit var appointmentDialog: AppointmentDialog
    private lateinit var viewModel: DashboardViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    private lateinit var bankAccountAdapter: BankAccountAdapter
    private val logoutConfirmViewModel by activityViewModels<LogoutConfirmViewModel>()
    val prefs = SharedPref
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initViews()
        fetchInitDetails()
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
        viewModel.profilePictureUrl.observe(viewLifecycleOwner) {
            it?.let {url -> setProfilePicture(url) }
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
//                startJitsiMeet(response)
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
            if (response != null) {
                if (response.detail?.status == AppConstants.SUCCESS) {
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
                            ::fetchInitDetails,
                            response.detail?.message ?: getString(R.string.common_error_msg_2)
                        )
                    }
                }
            }
        }
        viewModel.bankAccountListApiResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                viewModel.bankAccounts = response?.detail?.bankAccounts
                setBankAccountList(response)
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.fetchBankAccountList()
                        }
                    }
                } else {
                    dismissLoader()
                    showToast(response.detail?.message!!)
                }
            }
        }
    }

    private fun initViews() {
        logoutConfirmDialog = LogoutConfirmDialog()
        addBankDialog = AddBankDialog()
        appointmentDialog = AppointmentDialog()
        bankAccountAdapter = BankAccountAdapter()
        binding.rvBankAccount.apply {
            isNestedScrollingEnabled = false
            adapter = bankAccountAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        setClickListeners()
    }

    private fun fetchInitDetails() {
        if (viewModel.dashboardDetailsResult.value?.detail?.status != AppConstants.SUCCESS) {
            if (NetworkUtils.isConnectedToNetwork(requireContext())) {
                showLoader()
                lifecycleScope.launch {
                    viewModel.fetchDashboardDetails().join()
                    viewModel.fetchBankAccountList().join()
                    viewModel.fetchProfilePicture() // TODO: remove after dashboard-details API update
                }
            } else {
                showFetchErrorDialog(::fetchInitDetails, R.string.no_internet_error)
            }
        }
    }

    private fun setClickListeners() {
        binding.imgBell.setOnClickListener { // TODO: remove after video call api completion
//            val callLink =
////                "https://project-one.org/v_call_ser/meeting_6c91334d-5a44-4e79-8a23-599458093cd6?jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJqaXRzaSIsImlzcyI6InByb2plY3Qtb25lIiwic3ViIjoicHJvamVjdC1vbmUub3JnIiwicm9vbSI6Im1lZXRpbmdfNmM5MTMzNGQtNWE0NC00ZTc5LThhMjMtNTk5NDU4MDkzY2Q2IiwiZXhwIjoxNzMzNzI5ODEwLCJjb250ZXh0Ijp7InVzZXIiOnsiZW1haWwiOiJtdWhhbW1hZC5mYWlzYWxAdGVjaHZlcnNhbnRpbmZvdGVjaC5jb20iLCJuYW1lIjoibXVoYW1tYWQuZmFpc2FsIiwibW9kZXJhdG9yIjp0cnVlfX19.WAyx2DjNui38M3Sh2plLI2HphzqLUIElMnV3QrfF-r8"
//                "https://pension-distributor.demoserver.work/video_call_server/gdGWbp0gHY?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIwYjVkZGZmYy03YzczLTQwZjUtOTBiYy03MjI4YmExODk1NmIiLCJpc3MiOiIwYjVkZGZmYy03YzczLTQwZjUtOTBiYy03MjI4YmExODk1NmIiLCJzdWIiOiJqaXRzaS1sb2NhbGhvc3QiLCJyb29tIjoiZ2RHV2JwMGdIWSIsImV4cCI6MTczNzU0MzkzMCwiY29udGV4dCI6eyJ1c2VyIjp7ImVtYWlsIjoiYXZpbi5tYXRoZXcuY29ubmVjdEBnbWFpbC5jb20iLCJuYW1lIjoib2ZmaWNpYWxfMSIsIm1vZGVyYXRvciI6dHJ1ZX19fQ.iemBP12ioyIPNPMi7g8978aO_k07CEViGNH_361snI0  user-link -  https://pension-distributor.demoserver.work/video_call_server/gdGWbp0gHY?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiIwYjVkZGZmYy03YzczLTQwZjUtOTBiYy03MjI4YmExODk1NmIiLCJpc3MiOiIwYjVkZGZmYy03YzczLTQwZjUtOTBiYy03MjI4YmExODk1NmIiLCJzdWIiOiJqaXRzaS1sb2NhbGhvc3QiLCJyb29tIjoiZ2RHV2JwMGdIWSIsImV4cCI6MTczNzU0MzkzMCwiY29udGV4dCI6eyJ1c2VyIjp7ImVtYWlsIjoiYXZpbkB0ZWNodmVyc2FudGluZm8uY29tIiwibmFtZSI6InVzZXJfMDA3IiwibW9kZXJhdG9yIjpmYWxzZX19fQ.EZ1qv-0UhXuRMXldwrbds4EMoxZxzk_PQ7xKbvVEc6U"
//            startJitsiMeetCall(callLink)
//


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
        binding.ivTopup.setOnClickListener {
            navigate(R.id.action_dashboard_to_wallet)
        }
        binding.ivHistory.setOnClickListener {
            navigate(R.id.action_dashboard_to_wallet_history)
        }
        binding.llAccount.setOnClickListener {
            navigate(R.id.action_dashboard_to_account)
        }
        binding.llAddBank.setOnClickListener {
            if (NetworkUtils.isConnectedToNetwork(requireContext())) {
                showDialog(addBankDialog)
            } else {
                showToast(R.string.no_internet_error)
            }
        }
        binding.llAppoinment.setOnClickListener {
            viewModel.dashboardDetailsResult.value?.detail?.walletBalanceAmount?.let {
                if (it >= MIN_BOOKING_AMOUNT) showDialog(appointmentDialog)
                else {
                    showAlertDialog(
                        message = getString(R.string.booking_amount_error),
                        positiveTextId = R.string.ok,
                        onPositiveClick = {},
                    )
                }
            }
        }
        binding.llLogout.setOnClickListener {
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
            //it.profilePic?.let { url -> setProfilePicture(url) } // TODO: uncomment after dashboard-details API update
            binding.tvPersonName.text = it.fullName
            val walletText = "${it.walletBalanceCurrency} ${it.walletBalanceAmount.toString()}"
            binding.tvWalletAmount.text = walletText
            binding.ivNaira.isGone = true
            if (it.verificationStatus == true) {
                binding.tvVerificationStatus.text = getString(R.string.verified)
                binding.tvVerificationStatus.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.green_dark
                    )
                )
                binding.ivVerificationStatus.setImageResource(R.drawable.ic_tick_green)
            } else {
                binding.tvVerificationStatus.text = getString(R.string.not_verified)
                binding.tvVerificationStatus.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.red
                    )
                )
                binding.ivVerificationStatus.setImageResource(R.drawable.ic_not_verified_red)
            }
            if (true == true) { // TODO: Modify this whenever update the API and the response includes status
                binding.tvBookAppointment.text = getString(R.string.book_appointment)
                binding.tvBookAppointment.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.grey_500
                    )
                )
                binding.ivBookAppointment.setImageResource(R.drawable.ic_schedule)
            } else {
                binding.tvBookAppointment.text = getString(R.string.valid_till_date, "01/01/2025")
                binding.tvBookAppointment.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.red
                    )
                )
                binding.ivBookAppointment.setImageResource(R.drawable.ic_not_verified_red)
            }
        }
    }

    private fun setProfilePicture(url: String) {
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.baseline_account_circle_white)
            .into(binding.ivProfile)
    }

    private fun onLogoutSuccess(response: ResponseLogout) {
        dismissLoader()
        Toast.makeText(context, response.logout_detail?.message, Toast.LENGTH_LONG).show()
        prefs.logout()
        val intent = Intent(context, SignUpActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun setBankAccountList(response: BankAccountListResponse) {
        val bankAccounts = mutableListOf<BankAccountItem>()
        response.detail?.bankAccounts?.let { account->
            account.forEach {
                if (it.bankName != null &&
                    it.isPrimary != null &&
                    it.accountNumber != null &&
                    it.accountType != null
                ) {
                    bankAccounts.add(
                        BankAccountItem(
                            it.bankName!!,
                            it.isPrimary!!,
                            it.accountNumber!!,
                            it.accountType!!,
                            it.logoUrl,
                        )
                    )
                }
            }
        }
        binding.noBankMsg.isGone = bankAccounts.isNotEmpty()
        bankAccountAdapter.setItems(bankAccounts)
    }
    private fun startJitsiMeet(response: VideoCallResponse) {
//        dismissLoader()
//        PeerConnectionFactory.initialize(
//            PeerConnectionFactory.InitializationOptions.builder(requireContext())
//                .setEnableInternalTracer(true)
//                .createInitializationOptions())
//        val options = JitsiMeetConferenceOptions.Builder()
//            .setRoom(response.detail?.roomName)
//            .setFeatureFlag("welcomepage.enabled", false)
//            .setAudioMuted(true)
//            .setVideoMuted(true)
//            .build()
//        JitsiMeetActivity.launch(requireContext(), options)
    }

    private fun startJitsiMeetCall(callLink: String) {
//        try {
//            val options: JitsiMeetConferenceOptions = JitsiMeetConferenceOptions.Builder()
//                .setServerURL(URL(callLink))
//                .setRoom(callLink)
//                .setAudioOnly(false)
//                .build()
//
//            JitsiMeetActivity.launch(requireContext(), options)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }
}