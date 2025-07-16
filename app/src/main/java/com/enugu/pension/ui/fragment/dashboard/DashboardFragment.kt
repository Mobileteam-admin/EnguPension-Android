package com.enugu.pension.ui.fragment.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
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
    ): View {
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
            setProfilePicture(it)
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
                    showToast(response.detail?.message?: getString(R.string.common_error_msg))
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
        binding.tvProfile.setOnClickListener {
            if (confirmInternet()) navigate(R.id.action_dashboard_to_profile)
        }
        binding.ivTopup.setOnClickListener {
            navigate(R.id.action_dashboard_to_wallet)
        }
        binding.ivReservation.setOnClickListener {
            if (confirmInternet()) {
                navigate(R.id.action_dashboard_to_navigation_reservation)
            }
        }
        binding.llYourBooking.setOnClickListener {
            if (confirmInternet()) {
                navigate(R.id.action_dashboard_to_navigation_reservation)
            }
        }
        binding.llAccount.setOnClickListener {
            navigate(R.id.action_dashboard_to_account)
        }
        binding.llAddBank.setOnClickListener {
            if (confirmInternet()) showDialog(addBankDialog)
        }
        binding.ivVerificationHistory.setOnClickListener {
            if (confirmInternet()) navigate(R.id.action_dashboard_to_verification_history)
        }
        binding.llAppointment.setOnClickListener {
            viewModel.dashboardDetailsResult.value?.detail?.walletBalanceAmount?.let {
                if (it >= MIN_BOOKING_AMOUNT) {
                    if (confirmInternet()) showDialog(appointmentDialog)
                }
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
            binding.tvWalletAmount.text = it.getWalletBalanceAmount()
            binding.ivNaira.isGone = true

            binding.llAppointment.isGone = true
            binding.llYourBooking.isGone = true
            binding.llValidity.isGone = true
            if (it.isExpired) {
                setTvVerificationStatus(false, getString(R.string.not_verified))
                binding.llAppointment.isVisible = true
            } else {
                if (it.isVerified()) {
                    setTvVerificationStatus(true, getString(R.string.verified))
                    binding.llValidity.isVisible = true
                    binding.tvValidTill.text = getString(R.string.valid_till_date, it.expiryDate)
                } else {
                    setTvVerificationStatus(false, it.verificationStatus ?: getString(R.string.verification_pending))
                    binding.llYourBooking.isVisible = true
                }
            }
        }
    }

    private fun setTvVerificationStatus(isVerified: Boolean, text: String) {
        binding.tvVerificationStatus.text = text
        binding.tvVerificationStatus.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (isVerified) R.color.green_dark else R.color.red
            )
        )
        binding.ivVerificationStatus.setImageResource(if (isVerified) R.drawable.ic_tick_green else R.drawable.ic_not_verified_red)
    }
    private fun setProfilePicture(url: String?) {
        if (url == null) {
            binding.ivProfile.setImageResource(R.drawable.baseline_account_circle_white)
        } else {
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.baseline_account_circle_white)
                .into(binding.ivProfile)
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
}