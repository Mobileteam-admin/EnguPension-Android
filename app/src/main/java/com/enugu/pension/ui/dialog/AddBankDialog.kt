package com.enugu.pension.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.enugu.pension.common.constant.AppConstants
import com.enugu.pension.R
import com.enugu.pension.data.repository.NetworkRepo
import com.enugu.pension.databinding.CardBankVerifyBinding
import com.enugu.pension.databinding.DialogAddBankBinding
import com.enugu.pension.data.remote.dto.request.ExtraBankAccountRequest
import com.enugu.pension.data.remote.dto.request.InputBankVerification
import com.enugu.pension.data.remote.dto.response.AccountTypeItem
import com.enugu.pension.data.remote.dto.response.ListBanksItem
import com.enugu.pension.data.remote.api.ApiClient
import com.enugu.pension.ui.adapter.AccountTypeAdapter
import com.enugu.pension.ui.adapter.BankAdapter
import com.enugu.pension.common.util.AppUtils
import com.enugu.pension.common.util.NetworkUtils
import com.enugu.pension.viewmodel.AddBankViewModel
import com.enugu.pension.viewmodel.DashboardViewModel
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AddBankDialog : BaseDialog() {
    companion object {
        private const val ENABLE_BANK_CODE_VERIFICATION = false
    }
    private lateinit var binding: DialogAddBankBinding
    private lateinit var viewModel: AddBankViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    private lateinit var dashboardViewModel: DashboardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        dismissOnDoubleBackPress = true
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogAddBankBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initViews()
        observeLiveData()
        initBanks()
    }

    private fun initBanks() {
        if (dashboardViewModel.banks == null || dashboardViewModel.bankAccountTypes == null) {
            setBankListLoaderState(true)
            viewModel.fetchBankList()
        } else {
            setBankListLoaderState(false)
            setBankAdapters()
        }
    }

    private fun observeLiveData() {
        if (ENABLE_BANK_CODE_VERIFICATION) {
            viewModel.verificationState.observe(viewLifecycleOwner) {
                if (it != null)
                    when (it) {
                        AddBankViewModel.VerificationState.NOT_VERIFIED -> {
                            binding.tvBankCodeVerification.text = getString(R.string.verify)
                            binding.tvBankCodeVerification.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.red
                                )
                            )
                            binding.tvBankCodeVerification.isVisible = true
                            binding.pbVerification.isInvisible = true
                            setBankInputEnabled(true)
                        }

                        AddBankViewModel.VerificationState.VERIFYING -> {
                            binding.tvBankCodeVerification.isInvisible = true
                            binding.pbVerification.isVisible = true
                            setBankInputEnabled(false)
                        }

                        AddBankViewModel.VerificationState.FAILED -> {
                            binding.tvBankCodeVerification.text = getString(R.string.reverify)
                            binding.tvBankCodeVerification.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.red
                                )
                            )
                            binding.tvBankCodeVerification.isVisible = true
                            binding.pbVerification.isInvisible = true
                            setBankInputEnabled(true)
                        }

                        AddBankViewModel.VerificationState.VERIFIED -> {
                            binding.tvBankCodeVerification.text = getString(R.string.verified)
                            binding.tvBankCodeVerification.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.green_middle
                                )
                            )
                            binding.tvBankCodeVerification.isVisible = true
                            binding.pbVerification.isInvisible = true
                            setBankInputEnabled(true)
                        }
                    }
            }
        }
        viewModel.bankListApiResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                setBankListLoaderState(false)
                dashboardViewModel.banks = response.detail.banks
                dashboardViewModel.bankAccountTypes = response.detail.accountType
                setBankAdapters()
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.fetchBankList()
                        }
                    }
                } else {
                    setBankListLoaderState(false)
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                    resetAndDismiss()
                }
            }
        }
        viewModel.bankVerificationResult.observe(viewLifecycleOwner) { pair ->
            val request = pair.first
            val response = pair.second
            if (response.detail?.status == AppConstants.SUCCESS) {
                viewModel.verificationState.value = AddBankViewModel.VerificationState.VERIFIED
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.verifyBankAccount(request)
                        }
                    }
                } else {
                    viewModel.verificationState.value = AddBankViewModel.VerificationState.FAILED
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        viewModel.extraBankAccountResult.observe(viewLifecycleOwner) { pair ->
            val inputActiveBankInfo = pair.first
            val response = pair.second
            if (response.detail?.status == AppConstants.SUCCESS) {
                showToast(response.detail.message ?: "Bank account added successfully.")
                resetAndDismiss()
                dashboardViewModel.fetchBankAccountList()
            } else if (response.detail?.status == AppConstants.FAIL) {
                dismissLoader()
                showToast(response.detail.message ?: getString(R.string.common_error_msg_2))
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.createExtraBankAccount(inputActiveBankInfo)
                        }
                    }
                } else {
                    dismissLoader()
                    showToast(response.detail?.message ?: getString(R.string.common_error_msg_2))
                }
            }
        }
    }

    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        viewModel = ViewModelProviders.of(
            this,
            EnguViewModelFactory(networkRepo)
        ).get(AddBankViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(),
            EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
        dashboardViewModel = ViewModelProviders.of(
            requireActivity(),
            EnguViewModelFactory(networkRepo)
        ).get(DashboardViewModel::class.java)
    }

    private fun initViews() {
        if (ENABLE_BANK_CODE_VERIFICATION) {
            binding.tvBankCodeVerification.isVisible = true
            binding.tvBankCodeVerification.setOnClickListener {
                if (viewModel.verificationState.value != AddBankViewModel.VerificationState.VERIFIED
                    && isValidInput(false)
                ) {
                    showBankVerifyDialog()
                }
            }
        } else{
            binding.tvBankCodeVerification.isGone = true
        }
        val holderName = dashboardViewModel.dashboardDetailsResult.value?.detail?.fullName?.trim()
            ?.replace("  ", " ") ?: ""
        binding.etHolderName.setText(holderName)
        binding.etSwiftCode.filters = arrayOf(InputFilter.AllCaps(), AppConstants.SwiftCodeFilter )
        binding.llClose.setOnClickListener { resetAndDismiss() }
        binding.llSubmit.setOnClickListener {
            if (isValidInput(true)) {
                if (!ENABLE_BANK_CODE_VERIFICATION ||
                    viewModel.verificationState.value == AddBankViewModel.VerificationState.VERIFIED) {
                    if (NetworkUtils.isConnectedToNetwork(requireContext())) {
                        showLoader()
                        viewModel.createExtraBankAccount(
                            ExtraBankAccountRequest(
                                bankId = viewModel.bankItems[viewModel.selectedBankIndex]!!.id.toString(),
                                accountNumber = binding.etAccountNumber.text.toString(),
                                bankCode = binding.etBankCode.text.toString(),
                                accountType = viewModel.accountTypeItems[viewModel.selectedAccountTypeIndex]!!.type,
                                accountHolderName = binding.etHolderName.text.toString(),
                                swiftCode = binding.etSwiftCode.text.toString(),
                                reEnterAccountNumber = binding.etAccountNumberReenter.text.toString(),
                            )
                        )
                    } else {
                        showToast(R.string.no_internet_error)
                    }
                } else {
                    showToast(R.string.verify_bank_code_msg)
                }
            }
        }
        binding.spBank.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.bankItems[position]?.id?.let {
                    viewModel.selectedBankIndex = position
                    refreshBankCode(position)
                    refreshBankImage(position)
                }
                changeVerifiedState()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        binding.spAccountType.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.selectedAccountTypeIndex = position
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }
        binding.etAccountNumber.addTextChangedListener {
            changeVerifiedState()
        }
        binding.etAccountNumberReenter.addTextChangedListener {
            changeVerifiedState()
        }
        binding.etHolderName.addTextChangedListener {
            changeVerifiedState()
        }
        binding.etSwiftCode.addTextChangedListener {
            changeVerifiedState()
        }
        binding.etBankCode.addTextChangedListener {
            changeVerifiedState()
        }
    }

    private fun setBankAdapters() {
        viewModel.bankItems.clear()
        viewModel.accountTypeItems.clear()
        if (!dashboardViewModel.banks.isNullOrEmpty()) {
            viewModel.bankItems.add(
                ListBanksItem(
                    "",
                    " - Select Bank - ",
                    "0",
                    "",
                    -1,
                    ""
                )
            )
            dashboardViewModel.banks?.let { viewModel.bankItems.addAll(it) }
        }
        if (!dashboardViewModel.bankAccountTypes.isNullOrEmpty()) {
            viewModel.accountTypeItems.add(
                AccountTypeItem(
                    -1,
                    " - Select Account Type - "
                )
            )
            dashboardViewModel.bankAccountTypes?.forEach {
                viewModel.accountTypeItems.add(AccountTypeItem(it?.id, it?.type))
            }
        }
        binding.spBank.adapter = BankAdapter(context, viewModel.bankItems)
        binding.spBank.post {
            binding.spBank.setSelection(viewModel.selectedBankIndex)
        }
        binding.spAccountType.adapter = AccountTypeAdapter(context, viewModel.accountTypeItems)
        binding.spAccountType.setSelection(viewModel.selectedAccountTypeIndex)
    }

    private fun refreshBankImage(position: Int) {
        binding.ivBank.setImageResource(R.drawable.ic_bank_green)
        viewModel.bankItems[position]?.let {
            if (position != AddBankViewModel.BANK_DEFAULT_ITEM_INDEX) {
                Glide.with(requireContext())
                    .load(it.logo)
                    .placeholder(R.drawable.ic_bank_green)
                    .into(binding.ivBank)
            }
        }
    }

    private fun changeVerifiedState() {
        if (viewModel.verificationState.value == AddBankViewModel.VerificationState.VERIFIED) {
            viewModel.verificationState.value = AddBankViewModel.VerificationState.NOT_VERIFIED
        }
    }

    private fun isValidInput(includeAccountType: Boolean): Boolean {
        var errorMessage: String? = null
        if (viewModel.selectedBankIndex == AddBankViewModel.BANK_DEFAULT_ITEM_INDEX) {
            errorMessage = getString(R.string.select_bank_msg)
        } else if (!AppUtils.isValidBankAccountNumber(binding.etAccountNumber.text.toString())) {
            val minLength = resources.getInteger(R.integer.account_number_min_length)
            val maxLength = resources.getInteger(R.integer.account_number_max_length)
            errorMessage = getString(R.string.bank_account_number_error_msg,minLength, maxLength)
        } else if (binding.etAccountNumberReenter.text.isNullOrEmpty()) {
            errorMessage = getString(R.string.re_enter_account_number_msg)
        } else if (binding.etAccountNumber.text.toString() != binding.etAccountNumberReenter.text.toString()) {
            errorMessage = getString(R.string.re_entered_account_number_error_msg)
        } else if (!AppUtils.isValidFullName(binding.etHolderName.text.toString())) {
            errorMessage = getString(R.string.account_holder_error_msg)
        } else if (binding.etSwiftCode.text.length !in AppUtils.getSwiftCodeRange()) {
            val length1 = resources.getInteger(R.integer.swift_code_length_1)
            val length2 = resources.getInteger(R.integer.swift_code_length_2)
            errorMessage = getString(R.string.swift_code_error_msg, length1, length2)
        } else if (binding.etBankCode.text.isNullOrEmpty()) {
            errorMessage = getString(R.string.please_enter_bank_code)
        } else if (hasAccountAdded()) {
            errorMessage = getString(R.string.bank_account_already_added)
        } else if (includeAccountType &&
            viewModel.selectedAccountTypeIndex == AddBankViewModel.ACC_TYPE_DEFAULT_ITEM_INDEX) {
            errorMessage = getString(R.string.please_select_account_type)
        }
        errorMessage?.let { showToast(it) }
        return errorMessage == null
    }

    private fun hasAccountAdded(): Boolean {
        dashboardViewModel.bankAccounts?.forEach {
            if (it.bankName == viewModel.bankItems[viewModel.selectedBankIndex]?.name &&
                it.accountNumber == binding.etAccountNumber.text.toString() &&
                it.swiftCode == binding.etSwiftCode.text.toString() &&
                it.bankCode == binding.etBankCode.text.toString()
            )
                return true
        }
        return false
    }

    private fun showBankVerifyDialog() {
        val bankVerifyBinding = CardBankVerifyBinding.inflate(LayoutInflater.from(requireContext()))
        val bankVerifyDialog = AlertDialog.Builder(requireContext())
            .setView(bankVerifyBinding.root)
            .create()
        bankVerifyDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val accountNumber = binding.etAccountNumber.text.toString()
        val bankCode = binding.etBankCode.text.toString()
        bankVerifyBinding.etBankVerifyAccNum.setText(accountNumber)
        bankVerifyBinding.etBankVerifyBankCode.setText(bankCode)
        bankVerifyBinding.llBankverifysubmit.setOnClickListener {
            if (NetworkUtils.isConnectedToNetwork(requireContext())) {
                bankVerifyDialog.dismiss()
                viewModel.verificationState.value = AddBankViewModel.VerificationState.VERIFYING
                viewModel.verifyBankAccount(InputBankVerification(accountNumber, bankCode))
            } else {
                showToast(R.string.no_internet_error)
            }
        }
        bankVerifyDialog.show()
    }

    private fun setBankListLoaderState(isLoading: Boolean) {
        binding.ivBankSelection.isInvisible = isLoading
        binding.pbBank.isVisible = isLoading
    }

    private fun setBankInputEnabled(isEnabled: Boolean) {
        binding.spBank.isEnabled = isEnabled
        binding.etAccountNumber.isEnabled = isEnabled
        binding.etAccountNumberReenter.isEnabled = isEnabled
        binding.etHolderName.isEnabled = isEnabled
        binding.etSwiftCode.isEnabled = isEnabled
        binding.etBankCode.isEnabled = isEnabled
    }

    private fun refreshBankCode(position: Int) {
        val bankCode =
            if (position != AddBankViewModel.BANK_DEFAULT_ITEM_INDEX) viewModel.bankItems[position]?.code else ""
        binding.etBankCode.setText(bankCode)
    }

    private fun resetAndDismiss() {
        resetEntries()
        dismiss()
    }

    private fun resetEntries() {
        binding.spBank.setSelection(AddBankViewModel.BANK_DEFAULT_ITEM_INDEX)
        binding.spAccountType.setSelection(AddBankViewModel.ACC_TYPE_DEFAULT_ITEM_INDEX)
        binding.etAccountNumber.setText("")
        binding.etAccountNumberReenter.setText("")
        binding.etHolderName.setText("")
        binding.etSwiftCode.setText("")
        binding.etBankCode.setText("")
        binding.cbPrimary.isChecked = false
    }

}