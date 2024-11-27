package com.example.engu_pension_verification_application.ui.fragment.Dashboard

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.response.ResponseLogout
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.activity.SignUpActivity
import com.example.engu_pension_verification_application.ui.dialog.AddBankDialog
import com.example.engu_pension_verification_application.ui.dialog.LogoutConfirmDialog
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.NetworkUtils
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.AddBankViewModel
import com.example.engu_pension_verification_application.viewmodel.DashboardViewModel
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.LogoutConfirmViewModel
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class DashboardFragment : BaseFragment() {
    private lateinit var logoutConfirmDialog: LogoutConfirmDialog
    private lateinit var addBankDialog: AddBankDialog
    lateinit var datePickerDialog: DatePickerDialog
    private lateinit var viewModel: DashboardViewModel
//    private lateinit var addBankViewModel: AddBankViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    private val logoutConfirmViewModel by activityViewModels<LogoutConfirmViewModel>()
    val prefs = SharedPref
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
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
//        addBankViewModel = ViewModelProviders.of(
//            requireActivity(),
//            EnguViewModelFactory(networkRepo)
//        ).get(AddBankViewModel::class.java)
    }

    private fun observeLiveData() {
        logoutConfirmViewModel.logout.observe(viewLifecycleOwner) { logout ->
            if (logout != null) callLogout()
        }
        viewModel.logoutResult.observe(viewLifecycleOwner) { response ->
            if (response.logout_detail?.status == AppConstants.SUCCESS) {
                ondashboardLogoutSuccess(response)
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
                        response.detail?.message ?: getString(R.string.common_error_msg_2)
                    )
                }
            }
        }
    }

    private fun showFetchErrorDialog(message: String) {
        showAlertDialog(
            message = message,
            positiveTextId = R.string.retry,
            onPositiveClick = ::initCall,
            negativeTextId = R.string.close,
            onNegativeClick = { requireActivity().finish() }
        )
    }
    private fun initViews() {
        logoutConfirmDialog = LogoutConfirmDialog()
        addBankDialog = AddBankDialog()
    }

    private fun initCall() {
        if (NetworkUtils.isConnectedToNetwork(requireContext())) {
            viewModel.fetchDashboardDetails()
        } else {
            showFetchErrorDialog("Please connect to internet and retry")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onClicked() {
        tv_profile_.setOnClickListener {
            navigate(R.id.action_dashboard_to_profile)
        }
        img_add_amount.setOnClickListener {
            navigate(R.id.action_dashboard_to_wallet)
        }
        ll_account.setOnClickListener {
            navigate(R.id.action_dashboard_to_account)
        }
        ll_add_bank.setOnClickListener {
            showDialog(addBankDialog)
        }
        ll_appoinment.setOnClickListener {
            ll_dashboard_main.visibility = View.GONE
            ll_dashboard_booking.visibility = View.VISIBLE
        }
        ll_bookappoinment_back.setOnClickListener {
            ll_dashboard_main.visibility = View.VISIBLE
            ll_dashboard_booking.visibility = View.GONE
        }
        txt_booking_date.setOnClickListener {
            onDateSelect()
        }
        txt_logout.setOnClickListener {
            showDialog(logoutConfirmDialog)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onDateSelect() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        datePickerDialog = DatePickerDialog(
            requireContext(),
            { datePicker, year, month, day ->

                var f_month: Int = month + 1
                var formatmonth: String = month.toString()
                var formatDayOfMonth: String = "" + day


                if (f_month < 10) {

                    formatmonth = "0" + f_month
                } else {
                    formatmonth = f_month.toString()
                }
                if (day < 10) {

                    formatDayOfMonth = "0" + day;
                }



                txt_booking_date.text = Editable.Factory.getInstance()
                    .newEditable(
                        formatDayOfMonth + "/" + formatmonth + "/" + year.toString()
                    )  //day.toString() + "/" + (month + 1) + "/" + year

            }, year, month, dayOfMonth
        )

        datePickerDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            "Cancel",
            DialogInterface.OnClickListener { dialog, which ->
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    datePickerDialog.dismiss()

                }
            })
        datePickerDialog.show()
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
                .into(img_profile)
            tv_person_name.text = it.fullName
            tv_wallet_amount_digits.text = it.walletBalanceAmount.toString()
            tv_wallet_symbol_sign.text = it.walletBalanceCurrency
            it.bankDetail?.let { bankDetail ->
                no_bank_msg.visibility = View.GONE
                cl_dashboard_bank.visibility = View.VISIBLE
                if (bankDetail.bankImage != null)
                    Glide.with(this)
                        .load(bankDetail.bankImage)
                        .into(img_bank_icon)
                tv_bankname.text = bankDetail.bankName
                tv_banktype.text = bankDetail.accountType
            }
        }
    }

    private fun ondashboardLogoutSuccess(response: ResponseLogout) {
        dismissLoader()
        Toast.makeText(context, response.logout_detail?.message, Toast.LENGTH_LONG).show()
        prefs.logout()
        val intent = Intent(context, SignUpActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }



}