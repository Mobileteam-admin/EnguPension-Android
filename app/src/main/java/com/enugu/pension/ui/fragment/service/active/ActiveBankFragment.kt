package com.enugu.pension.ui.fragment.service.active

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.enugu.pension.R
import com.enugu.pension.constant.AppConstants
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.databinding.FragmentActiveBankBinding
import com.enugu.pension.model.request.InputActiveBankInfo
import com.enugu.pension.model.request.InputBankVerification
import com.enugu.pension.model.response.*
import com.enugu.pension.network.ApiClient
import com.enugu.pension.ui.activity.ProcessDashboardActivity
import com.enugu.pension.ui.adapter.AccountTypeAdapter
import com.enugu.pension.ui.adapter.BankAdapter
import com.enugu.pension.ui.dialog.SwiftVerificationDialog
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.util.AppUtils
import com.enugu.pension.util.OnboardingStage
import com.enugu.pension.util.SharedPref
import com.enugu.pension.util.VerificationState
import com.enugu.pension.viewmodel.ActiveBankViewModel
import com.enugu.pension.viewmodel.ActiveServiceViewModel
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.SwiftVerificationViewModel
import com.enugu.pension.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActiveBankFragment: BaseFragment() {
    private lateinit var binding:FragmentActiveBankBinding
    companion object {
        const val TAB_POSITION = 2
        private const val BANK_ITEM_SELECT_ID = -1
        private const val ACCOUNT_TYPE_ITEM_SELECT_ID = -1
    }
    var bankdetailsList = mutableListOf<ListBanksItem?>()
    var accountTypeList = mutableListOf<AccountTypeItem?>()

    private val activeServiceViewModel by activityViewModels<ActiveServiceViewModel>()
    private val swiftVerificationViewModel by activityViewModels<SwiftVerificationViewModel>()

    private lateinit var viewModel: ActiveBankViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2


    var a_bankid = ""
    var a_accounttype = ""
    var autoRenewal: Boolean = false

    var BankList = ArrayList<ListBanksItem?>()
    lateinit var bankAdapter: BankAdapter

    var AccountTypeList = ArrayList<AccountTypeItem?>()
    lateinit var accounttypeAdapter: AccountTypeAdapter

    val prefs = SharedPref
    val et_ein_number_popup: EditText? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentActiveBankBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        observeLiveData()

        //activeBankViewModel = ViewModelProvider(this).get(ActiveBankViewModel::class.java)

        /*tokenRefreshViewModel = ViewModelProvider(this).get(TokenRefreshViewModel::class.java)*/



        //local use
//        binding.etActivebankSwiftcode.text = Editable.Factory.getInstance().newEditable("MOOGNGL1")
        //local use
        /*binding.etActivebankSwiftcode.text = Editable.Factory.getInstance().newEditable("MOOGNGL1")
        binding.etActivebankBankcode.text = Editable.Factory.getInstance().newEditable("MOOG")*/

        binding.etSwiftCode.filters = arrayOf(InputFilter.AllCaps(), AppConstants.SwiftCodeFilter)
        binding.etHolderName.setText(AppUtils.getFullName(prefs.first_name,prefs.middle_name,prefs.last_name))

//        setAdapter()
        // initcall()  - hold
        setListeners()
        //observeActiveBankDetails()
    }

    private fun initViewModels() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        viewModel = ViewModelProviders.of(
            this,
            EnguViewModelFactory(networkRepo)
        ).get(ActiveBankViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), 
            EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }
    private fun observeLiveData() {
        activeServiceViewModel.currentTabPos.observe(viewLifecycleOwner){
            if (it == TAB_POSITION) {
                showLoader()
                viewModel.fetchBankList()
            }
        }
        swiftVerificationViewModel.swiftCodeState.observe(viewLifecycleOwner) {
            binding.tvSwiftCodeVerification.text = getVerificationStateText(it)
            binding.tvSwiftCodeVerification.isClickable = it != VerificationState.VERIFIED
            binding.tvSwiftCodeVerification.setTextColor(getVerificationStateColor(it))
            binding.ivSwiftCodeVerified.isVisible = it == VerificationState.VERIFIED
        }
        viewModel.bankCodeState.observe(viewLifecycleOwner) {
            binding.tvBankCodeVerification.text = getVerificationStateText(it)
            binding.tvBankCodeVerification.isClickable = it != VerificationState.VERIFIED
            binding.tvBankCodeVerification.setTextColor(getVerificationStateColor(it))
            binding.ivBankCodeVerified.isVisible = it == VerificationState.VERIFIED
        }
        viewModel.bankListApiResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                bankdetailsList.clear()
                accountTypeList.clear()
                response.detail.banks?.let {  bankdetailsList.addAll(it)}
                response.detail.accountType?.let {  accountTypeList.addAll(it)}
                setAdapter()
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.fetchBankList()
                        }
                    }
                } else {
                    dismissLoader()
                    showFetchErrorDialog(
                        viewModel::fetchBankList,
                        response.detail?.message ?: getString(R.string.common_error_msg_2)
                    )
                }
            }
        }
        viewModel.bankDetailsApiResult.observe(viewLifecycleOwner) { pair ->
            if (pair != null) {
                val swiftCode = pair.first
                val response = pair.second
                if (response.detail?.status == AppConstants.SUCCESS) {
                    dismissLoader()
                    response.detail.swiftCodeResponse?.let {
                        val bundle = Bundle().apply {
                            putString(SwiftVerificationDialog.ARG_BANK_NAME, it.bankName)
                            putString(SwiftVerificationDialog.ARG_BRANCH, it.branchName)
                            putString(SwiftVerificationDialog.ARG_CITY, it.cityName)
                        }
                        findNavController().navigate(R.id.swiftVerificationDialog, bundle)
                    }
                } else {
                    if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (tokenRefreshViewModel2.fetchRefreshToken()) {
                                viewModel.fetchBankDetails(swiftCode)
                            }
                        }
                    } else {
                        dismissLoader()
                        Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                    }
                }
                viewModel.resetBankDetailsApiResult()
            }
        }
        viewModel.bankInfoSubmissionResult.observe(viewLifecycleOwner) { pair ->
            val inputActiveBankInfo = pair.first
            val response = pair.second
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                onActiveBankInfoSubmitSuccess(response)
            } else if (response.detail?.status == AppConstants.FAIL && response.detail.tokenStatus != AppConstants.EXPIRED) {
                dismissLoader()
                Toast.makeText(context, response.detail.message, Toast.LENGTH_LONG).show()
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.submitBankInfo(inputActiveBankInfo)
                        }
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        viewModel.bankVerificationResult.observe(viewLifecycleOwner) { pair ->
            if (pair != null) {
                val inputBankVerification = pair.first
                val response = pair.second
                if (response.detail?.status == AppConstants.SUCCESS) {
                    dismissLoader()
                    response.detail?.message?.let { showToast(it) }
                    viewModel.bankCodeState.value = VerificationState.VERIFIED
                } else {
                    if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (tokenRefreshViewModel2.fetchRefreshToken()) {
                                viewModel.verifyBankAccount(inputBankVerification)
                            }
                        }
                    } else {
                        dismissLoader()
                        Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                        viewModel.bankCodeState.value = VerificationState.RE_VERIFY
                    }
                }
                viewModel.resetBankVerificationResult()
            }
        }
        viewModel.einSubmissionResult.observe(viewLifecycleOwner) { pair ->
            val ein = pair.first
            val response = pair.second
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                onEinNumberSubmitSuccess(response)
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.submitEin(ein)
                        }
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getVerificationStateText(state: VerificationState): String {
        return getString(
            when (state) {
                VerificationState.VERIFY -> R.string.verify
                VerificationState.RE_VERIFY -> R.string.reverify
                VerificationState.VERIFIED -> R.string.verified
            }
        )
    }
    private fun getVerificationStateColor(state: VerificationState): Int {
        return  ContextCompat.getColor(
            requireContext(),
            when (state) {
                VerificationState.VERIFY -> R.color.red
                VerificationState.RE_VERIFY -> R.color.red
                VerificationState.VERIFIED -> R.color.green_middle
            }
        )
    }

    private fun setAdapter() {

        /* BankList = bankdetailsList
         AccountTypeList = accountTypeList*//* if (bankdetailsList.size > 0){

             BankList.add(ListBanksItem("","- select Bank","0","",1,""))
             bankdetailsList.forEach {
                 BankList.add(ListBanksItem(it?.code, it?.name, it?.ussd, it?.logo,it?.id,it?.slug))
             }
         }
         bankAdapter = BankAdapter(context, BankList)
         binding.spActiveBank.adapter = bankAdapter



         if (accountTypeList.size > 0) {
             AccountTypeList.add(AccountTypeItem(0, " - Select AccountType - "))
             accountTypeList.forEach {
                 AccountTypeList.add(AccountTypeItem(it?.id, it?.type))
             }
         }
         accounttypeAdapter = AccountTypeAdapter(context, AccountTypeList)
         binding.spActivebankAcctype.adapter = accounttypeAdapter*/

        if (bankdetailsList.size > 0) {

            BankList.clear()

            BankList.add(ListBanksItem("", "- Select Bank - ", "0", "", BANK_ITEM_SELECT_ID, ""))
            bankdetailsList.forEach {
                BankList.add(
                    ListBanksItem(
                        it?.code, it?.name, it?.ussd, it?.logo, it?.id, it?.slug
                    )
                )
            }
        }
        bankAdapter = BankAdapter(context, BankList)
        binding.spActiveBank.adapter = bankAdapter



        if (accountTypeList.size > 0) {

            AccountTypeList.clear()

            AccountTypeList.add(AccountTypeItem(ACCOUNT_TYPE_ITEM_SELECT_ID, " - Select Account Type - "))
            accountTypeList.forEach {
                AccountTypeList.add(AccountTypeItem(it?.id, it?.type))
            }
        }
        accounttypeAdapter = AccountTypeAdapter(context, AccountTypeList)
        binding.spActivebankAcctype.adapter = accounttypeAdapter


    }

    private fun setListeners() {
        binding.etSwiftCode.addTextChangedListener {
            swiftVerificationViewModel.swiftCodeState.value = VerificationState.VERIFY
        }
        binding.etBankcode.addTextChangedListener {
            viewModel.bankCodeState.value = VerificationState.VERIFY
        }
        binding.etAccountNumber.addTextChangedListener {
            viewModel.bankCodeState.value = VerificationState.VERIFY
        }
        binding.etReAccountNumber.addTextChangedListener {
            viewModel.bankCodeState.value = VerificationState.VERIFY
        }

        binding.spActiveBank.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                swiftVerificationViewModel.swiftCodeState.value = VerificationState.VERIFY
                refreshBankCode(position)
                refreshBankImage(position)
                if (BankList.get(position)?.id?.equals(0) == true) {

                } else {
                    a_bankid = BankList[position]?.id.toString()
                    //prefs.A_BANK = binding.spActiveBank.selectedItemPosition.toString()
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        binding.spActivebankAcctype.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                if (AccountTypeList.get(position)?.id?.equals(0) == true) {


                } else {
                    a_accounttype = AccountTypeList[position]?.type.toString()
                    //prefs.A_ACCTYPE = binding.spActivebankAcctype.selectedItemPosition.toString()
                }
            }


            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }


        binding.spActiveBank.setOnTouchListener { _, _ ->
            clearAllEditTextFocus()
            false
        }
        binding.spActivebankAcctype.setOnTouchListener { _, _ ->
            clearAllEditTextFocus()
            false
        }

        // Assuming you have a CheckBox with the ID 'myCheckBox' in your layout

        binding.cbActivebankAutorenewal.setOnCheckedChangeListener { buttonView, isChecked ->
            clearAllEditTextFocus()
            autoRenewal = isChecked
        }

        binding.tvBankCodeVerification.setOnClickListener {
            if (viewModel.bankCodeState.value != VerificationState.VERIFIED) {
                clearAllEditTextFocus()
                if (isValidAccountNumber() && isValidBankCode()) {
                    bankVerifyDialog()
                }
            }
        }

        binding.tvSwiftCodeVerification.setOnClickListener {
            if (swiftVerificationViewModel.swiftCodeState.value != VerificationState.VERIFIED) {
                if (isValidBank() && isValidSwiftCode()) {
                    clearAllEditTextFocus()
                    if (confirmInternet()) {
                        showLoader()
                        viewModel.fetchBankDetails(binding.etSwiftCode.text.toString())
                    }
                }
            }
        }


        binding.llActivebankNext.setOnClickListener {
            clearAllEditTextFocus()
            if (isValidInput()) {
                //finish the Form
                if (context?.isConnectedToNetwork()!!) {
                    //bank
                    showLoader()
                    BankinformationCall()


                } else {
                    dismissLoader()
                    Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun BankinformationCall() {
        viewModel.submitBankInfo(
            InputActiveBankInfo(

                bankId = a_bankid,/*"7b8dc580-ba28-8f3b-354410354410351ab4"*//*binding.spActiveBank.selectedItemPosition.toString()*/
                accountNumber = binding.etAccountNumber.text.toString(),
                bankCode = binding.etBankcode.text.toString(),
                accountType = a_accounttype,/*binding.spActivebankAcctype.selectedItemPosition.toString()*/
                accountHolderName = binding.etHolderName.text.toString(),
                swiftCode = binding.etSwiftCode.text.toString(),
                reEnterAccountNumber = binding.etReAccountNumber.text.toString(),
                autoRenewal = autoRenewal,
//                userId = prefs.user_id

            )
        )
    }


    /*   private fun initcall() {

           if (context?.isConnectedToNetwork()!!) {
               showLoader()
               // call bank details retrive api  - hold now

           } else {
               Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
           }
       }*/

    fun Context.isConnectedToNetwork(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting() ?: false
    }

    private fun onActiveBankInfoSubmitSuccess(response: ResponseBankInfo) {
        dismissLoader()
        Toast.makeText(context, response.detail?.message, Toast.LENGTH_SHORT).show()

        /*  val intent = Intent(context, DashboardActivity::class.java)
          intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
          startActivity(intent)*/
        Handler(Looper.getMainLooper()).postDelayed({
// Call your function here
            addEINDialog()
        }, Toast.LENGTH_SHORT.toLong() * 1000L)




    }

    private fun bankVerifyDialog() {
        val bankVerifyBuilder = AlertDialog.Builder(requireContext())
        val bankVerifyView: View =
            LayoutInflater.from(requireContext()).inflate(R.layout.card_bank_verify, null)
        bankVerifyBuilder.setView(bankVerifyView)
        val bankVerifyalertDialog: AlertDialog = bankVerifyBuilder.create()

        // Initialize et_acc_num here with the correct ID
        val et_bank_verify_acc_number = bankVerifyView.findViewById<EditText>(R.id.et_bank_verify_acc_num)
        val et_bank_verify_bank_code = bankVerifyView.findViewById<EditText>(R.id.et_bank_verify_bank_code)

        et_bank_verify_acc_number.text = binding.etAccountNumber.text

        et_bank_verify_bank_code.text = binding.etBankcode.text

        val bank_verify_submit = bankVerifyView.findViewById<LinearLayout>(R.id.ll_bankverifysubmit)
        bankVerifyalertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bankVerifyalertDialog.setCancelable(true)

        bank_verify_submit.setOnClickListener {
             showLoader()
            // Pass the EditText to the function api call
            BankVerifyCall(et_bank_verify_acc_number,et_bank_verify_bank_code)

            /*
            Toast.makeText(requireContext(), "tv clicked", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                bankVerifyalertDialog.dismiss()
            }, 3500) // 3500 milliseconds for LENGTH_LONG
            */
            Handler(Looper.getMainLooper()).postDelayed({
                bankVerifyalertDialog.dismiss()
            }, 2000) // 3500 milliseconds for LENGTH_LONG

            //bankVerifyalertDialog.show()

        }
        bankVerifyalertDialog.show()
    }

    private fun addEINDialog() {
        val add_einbuilder = AlertDialog.Builder(requireContext())
        val add_einview: View =
            LayoutInflater.from(requireContext()).inflate(R.layout.card_ein_number, null)
        add_einbuilder.setView(add_einview)
        val add_einalertDialog: AlertDialog = add_einbuilder.create()

        // Initialize et_ein_number_popup here with the correct ID
        // Initialize et_ein_number_popup here with the correct ID
        val et_ein_number_popup = add_einview.findViewById<EditText>(R.id.et_ein_number_popup)

        val addein_close = add_einview.findViewById<LinearLayout>(R.id.ll_addein_close)
        val addein_submit = add_einview.findViewById<LinearLayout>(R.id.ll_addein_submit)
        add_einalertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        add_einalertDialog.setCancelable(false)
        addein_close.setOnClickListener {
            add_einalertDialog.dismiss()
        }
        addein_submit.setOnClickListener {
            //EINApicall
            if (isValidEinNumber(et_ein_number_popup, add_einalertDialog)) {
                showLoader()
                EINApicall(et_ein_number_popup) // Pass the EditText to the function
            }
            add_einalertDialog.show()
        }
        add_einalertDialog.show()
    }

    private fun EINApicall(et_ein_number_popupapicall: EditText?) {
        //showLoader()
        if (context?.isConnectedToNetwork()!!) {
            //einnumber
            /* showLoader()*/
            EinSubmitCall(et_ein_number_popupapicall)

        } else {/*       dismissLoader()*/
            dismissLoader()
            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
        }
    }

    private fun BankVerifyCall(et_acc_num: EditText?, et_bank_code: EditText?) {
        //showLoader()
        if (context?.isConnectedToNetwork()!!) {

            BankVerifyApiCall(et_acc_num,et_bank_code)

        } else {
            dismissLoader()
            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
        }
    }

    private fun BankVerifyApiCall(etAccNum: EditText?, etBankCode: EditText?) {

        viewModel.verifyBankAccount(
            InputBankVerification(
                accountNumber = etAccNum?.text.toString(),
                bankCode = etBankCode?.text.toString()

            )
        )
        Log.d("bankVerifyApiPresenter", "bankVerify account ${etAccNum?.text}")
    }

    private fun EinSubmitCall(et_ein_number_popupsub: EditText?) {
        viewModel.submitEin(et_ein_number_popupsub!!.text.toString())
        Log.d("Ein", "EIN_Number${et_ein_number_popupsub.text}")
    }


    /*// Update isValidEinNumber to accept the EditText as a parameter
    private fun isValidEinNumber(et_ein_number_popup: EditText?): Boolean {
// Your validation logic here

        return true
    }*/


    private fun isValidEinNumber(et_ein_number_popup: EditText?, dia: AlertDialog?): Boolean {
// Get the text from EditText and convert it to a String
        val einNumber = et_ein_number_popup?.text.toString()
//einNumber!="" || TextUtils.isEmpty(et_ein_number_popup?.text) ||
        /*if (!einNumber.matches(Regex("^\\d{10}$"))) {
            Toast.makeText(context, "Please enter EIN Number", Toast.LENGTH_LONG).show()
            return false
        }*/

        //Ein Number Validation
        if (TextUtils.isEmpty(einNumber)) {
            Toast.makeText(context, "Please enter EIN Number", Toast.LENGTH_LONG).show()
            return false
        }
        //dia!!.dismiss()
// Check if the String contains exactly 10 digits
        return true
    }

    private fun getSwiftCodeErrorMessage(): String {
        val length1 = resources.getInteger(R.integer.swift_code_length_1)
        val length2 = resources.getInteger(R.integer.swift_code_length_2)
        return getString(R.string.swift_code_error_msg, length1, length2)
    }

    private fun isValidBank(): Boolean {
        var errorMessage: String? = null
        if (BankList[binding.spActiveBank.selectedItemPosition]?.id == BANK_ITEM_SELECT_ID) {
            errorMessage = getString(R.string.select_bank_msg)
        }
        errorMessage?.let { showToast(it) }
        return errorMessage == null
    }
    private fun isValidAccountNumber(): Boolean {
        var errorMessage: String? = null
        if (!AppUtils.isValidBankAccountNumber(binding.etAccountNumber.text.toString())) {
            val minLength = resources.getInteger(R.integer.account_number_min_length)
            val maxLength = resources.getInteger(R.integer.account_number_max_length)
            errorMessage = getString(R.string.bank_account_number_error_msg,minLength, maxLength)
        } else if (binding.etReAccountNumber.text.isNullOrEmpty()) {
            errorMessage = getString(R.string.re_enter_account_number_msg)
        } else if (binding.etAccountNumber.text.toString() != binding.etReAccountNumber.text.toString()) {
            errorMessage = getString(R.string.re_entered_account_number_error_msg)
        }
        errorMessage?.let { showToast(it) }
        return errorMessage == null
    }
    private fun isValidSwiftCode(): Boolean {
        var errorMessage: String? = null
        if (binding.etSwiftCode.text.length !in AppUtils.getSwiftCodeRange()) {
            errorMessage = getSwiftCodeErrorMessage()
        }
        errorMessage?.let { showToast(it) }
        return errorMessage == null
    }
    private fun isValidBankCode(): Boolean {
        var errorMessage: String? = null
        if (binding.etBankcode.text.isNullOrEmpty()) {
            errorMessage = getString(R.string.please_enter_bank_code)
        }
        errorMessage?.let { showToast(it) }
        return errorMessage == null
    }
    private fun isValidInput(): Boolean {
        if (!isValidBank() || !isValidAccountNumber()) return false
        var errorMessage: String? = null
        if (!AppUtils.isValidFullName(binding.etHolderName.text.toString())) {
            errorMessage = getString(R.string.account_holder_error_msg)
        } else if (swiftVerificationViewModel.swiftCodeState.value != VerificationState.VERIFIED) {
            errorMessage = getString(R.string.verify_swift_code_msg)
        } else if (viewModel.bankCodeState.value != VerificationState.VERIFIED) {
            errorMessage = getString(R.string.verify_bank_code_msg)
        } else if (AccountTypeList[binding.spActivebankAcctype.selectedItemPosition]?.id == ACCOUNT_TYPE_ITEM_SELECT_ID) {
            errorMessage = getString(R.string.please_select_account_type)
        }
        errorMessage?.let { showToast(it) }
        return errorMessage == null
    }


    private fun onEinNumberSubmitSuccess(response: ResponseEinNumber) {
        prefs.onboardingStage = OnboardingStage.PROCESSING
        dismissLoader()
        Toast.makeText(context, response.detail?.message, Toast.LENGTH_SHORT).show()
        val intent = Intent(context, ProcessDashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    private fun refreshBankImage(position:Int) {
        binding.imgActivebank.setImageResource(R.drawable.ic_bank_green)
        BankList[position]?.let {
            if (it.id != BANK_ITEM_SELECT_ID) {
                Glide.with(requireContext())
                    .load(it.logo)
                    .placeholder(R.drawable.ic_bank_green)
                    .into(binding.imgActivebank)
            }
        }
    }

    private fun refreshBankCode(position:Int) {
        val bankCode = if (BankList[position]?.id != BANK_ITEM_SELECT_ID) BankList[position]?.code else ""
        binding.etBankcode.setText(bankCode)
    }
}

