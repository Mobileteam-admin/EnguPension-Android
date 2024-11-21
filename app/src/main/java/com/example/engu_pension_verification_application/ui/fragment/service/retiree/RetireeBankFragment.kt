package com.example.engu_pension_verification_application.ui.fragment.service.retiree

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.InputActiveBankInfo
import com.example.engu_pension_verification_application.model.input.InputBankVerification
import com.example.engu_pension_verification_application.model.response.*
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.activity.ProcessDashboardActivity
import com.example.engu_pension_verification_application.ui.adapter.AccountTypeAdapter
import com.example.engu_pension_verification_application.ui.fragment.service.active.filterUpperCaseAndDigits
import com.example.engu_pension_verification_application.ui.adapter.BankAdapter
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.AppUtils
import com.example.engu_pension_verification_application.util.NetworkUtils
import com.example.engu_pension_verification_application.util.OnboardingStage
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.RetireeBankViewModel
import com.example.engu_pension_verification_application.viewmodel.RetireeServiceViewModel
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2
import kotlinx.android.synthetic.main.fragment_retiree_bank.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList
import java.util.regex.Pattern


class RetireeBankFragment : BaseFragment() {

    val ACC_NO_PATTERN = Pattern.compile("\\d+")

    val FULL_NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]+$")

    private lateinit var bankViewModel: RetireeBankViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    private val retireeServiceViewModel by activityViewModels<RetireeServiceViewModel>()
    private val bankDetailsList = mutableListOf<ListBanksItem?>()
    private val accountTypeList = mutableListOf<AccountTypeItem?>()

    var r_bankid = ""
    var r_accounttype = ""
    var autoRenewal : Boolean = false
    var isBankVerifyBtn = false

    val BankList = ArrayList<ListBanksItem?>()
    lateinit var bankAdapter: BankAdapter

    val AccountTypeList = ArrayList<AccountTypeItem?>()
    lateinit var accounttypeAdapter: AccountTypeAdapter

    val prefs = SharedPref
    val et_ein_number_popup: EditText? = null

    companion object {
        const val TAB_POSITION = 2
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_retiree_bank, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        initViews()
        observeLiveData()
    }

    private fun initViews(){
        et_retireebank_swiftcode.filters = arrayOf(InputFilter.AllCaps(), filterUpperCaseAndDigits )
//        setAdapter()
        // initcall()  - hold
        OnTextWatcher()
        onClicked()

        /*   observeRetireeBankDetails()*/

//        et_activebank_swiftcode.text = Editable.Factory.getInstance().newEditable("MOOGNGL1")

        et_retireebank_accname.setText(AppUtils.getFullName(
            prefs.Rfirst_name,
            prefs.Rmiddle_name,
            prefs.Rlast_name))
    }
    private fun initViewModels() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        bankViewModel = ViewModelProviders.of(
            this,
            EnguViewModelFactory(networkRepo)
        ).get(RetireeBankViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), 
            EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }
    private fun observeLiveData() {
        retireeServiceViewModel.currentTabPos.observe(viewLifecycleOwner){
            if (it == TAB_POSITION) bankViewModel.fetchBankList()
        }
        bankViewModel.bankListApiResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                bankDetailsList.clear()
                accountTypeList.clear()
                response.detail.banks?.let {  bankDetailsList.addAll(it)}
                response.detail.accountType?.let {  accountTypeList.addAll(it)}
                setAdapter()
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            bankViewModel.fetchBankList()
                        }
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        bankViewModel.bankDetailsApiResult.observe(viewLifecycleOwner) { pair ->
            val swiftCode = pair.first
            val response = pair.second
            if (response.swiftbankdetail?.status == AppConstants.SUCCESS) {
                onSwiftBankCodeSuccess(response)
            } else {
                if (response.swiftbankdetail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            bankViewModel.fetchBankDetails(swiftCode)
                        }
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, response.swiftbankdetail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        bankViewModel.bankInfoSubmissionResult.observe(viewLifecycleOwner) { pair ->
            val inputActiveBankInfo = pair.first
            val response = pair.second
            if (response.detail?.status == AppConstants.SUCCESS) {
                onRetireeBankInfoSubmitSuccess(response)
            }else if (response.detail?.status == AppConstants.FAIL) {
                dismissLoader()
                Toast.makeText(context, response.detail.message, Toast.LENGTH_LONG).show()
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            bankViewModel.submitBankInfo(inputActiveBankInfo)
                        }
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        bankViewModel.bankVerificationResult.observe(viewLifecycleOwner) { pair ->
            val inputBankVerification = pair.first
            val response = pair.second
            if (response.detail?.status == AppConstants.SUCCESS) {
                onBankVerifySubmitSuccess(response)
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            bankViewModel.verifyBankAccount(inputBankVerification)
                        }
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                    isBankVerifyBtn = false
                    tv_retireebank_bankcode_verify.visibility = View.INVISIBLE
                    tv_retireebank_bankcode_reverify.visibility = View.VISIBLE
                    tv_retireebank_bankcode_verified.visibility = View.INVISIBLE
                }
            }
        }
        bankViewModel.einSubmissionResult.observe(viewLifecycleOwner) { pair ->
            val ein = pair.first
            val response = pair.second
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                onRetireeEinNumberSubmitSuccess(response)
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            bankViewModel.submitEin(ein)
                        }
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun OnTextWatcher() {

        et_retireebank_swiftcode.addTextChangedListener(object : TextWatcher {


            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {

                et_retireebank_swiftcode.setOnFocusChangeListener { view, hasFocus ->
                    if (!hasFocus) {

                        bankViewModel.fetchBankDetails(et_retireebank_swiftcode.text.toString())

                    }
                }

            }
        })

        sp_retireebank_.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                if (BankList.get(position)?.id?.equals(0) == true) {

                } else {
                    r_bankid = BankList[position]?.id.toString()

                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        sp_retireebank_acctype.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    if (AccountTypeList.get(position)?.id?.equals(0) == true) {


                    } else {
                        r_accounttype = AccountTypeList[position]?.type.toString()

                    }
                }


                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }
    }

    private fun setAdapter() {
        if (bankDetailsList.size > 0) {

            BankList.clear()

            BankList.add(ListBanksItem("", "- select Bank", "0", "", 1, ""))
            bankDetailsList.forEach {
                BankList.add(
                    ListBanksItem(
                        it?.code,
                        it?.name,
                        it?.ussd,
                        it?.logo,
                        it?.id,
                        it?.slug
                    )
                )
            }
        }
        bankAdapter = BankAdapter(context, BankList)
        sp_retireebank_.adapter = bankAdapter



        if (accountTypeList.size > 0) {

            AccountTypeList.clear()

            AccountTypeList.add(AccountTypeItem(0, " - Select AccountType - "))
            accountTypeList.forEach {
                AccountTypeList.add(AccountTypeItem(it?.id, it?.type))
            }
        }
        accounttypeAdapter = AccountTypeAdapter(context, AccountTypeList)
        sp_retireebank_acctype.adapter = accounttypeAdapter

    }

    private fun onClicked() {

        cb_retireebank_autorenewal.setOnCheckedChangeListener { buttonView, isChecked ->
            autoRenewal = isChecked
        }

        tv_retireebank_bankcode_verify.setOnClickListener{
            if (isValidBankAccountNumber()){
                bankVerifyDialog()
            }
        }

        tv_retireebank_bankcode_reverify.setOnClickListener{
            if (isValidBankAccountNumber()){
                bankVerifyDialog()
            }
        }

        ll_retireebank_next.setOnClickListener {

            if (isValidRBank()) {
                if (NetworkUtils.isConnectedToNetwork(requireContext())) {/*     //finish the Form
                         FinishFnCall()*/
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

    private fun bankVerifyDialog() {
        val bankVerifyBuilder = AlertDialog.Builder(requireContext())
        val bankVerifyView: View =
            LayoutInflater.from(requireContext()).inflate(R.layout.card_bank_verify, null)
        bankVerifyBuilder.setView(bankVerifyView)
        val bankVerifyalertDialog: AlertDialog = bankVerifyBuilder.create()

        // Initialize et_acc_num here with the correct ID
        val et_bank_verify_acc_number = bankVerifyView.findViewById<EditText>(R.id.et_bank_verify_acc_num)
        val et_bank_verify_bank_code = bankVerifyView.findViewById<EditText>(R.id.et_bank_verify_bank_code)


        et_bank_verify_acc_number.text = et_retireebank_accnum.text

        et_bank_verify_bank_code.text = et_retireebank_bankcode.text

        val bank_verify_submit = bankVerifyView.findViewById<LinearLayout>(R.id.ll_bankverifysubmit)
        bankVerifyalertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //bankVerifyalertDialog.setCancelable(false)
        bankVerifyalertDialog.setCancelable(true)

        bank_verify_submit.setOnClickListener {
            //bankverifyApicall
            /*
            if (isValidBankAccountNumber(et_bank_verify_number, bankVerifyalertDialog)) {
                showLoader()
                EINApicall(et_ein_number_popup) // Pass the EditText to the function
            }
            */
            BankVerifyCall(et_bank_verify_acc_number,et_bank_verify_bank_code)

            // Call a function after the duration of LENGTH_LONG
            Handler(Looper.getMainLooper()).postDelayed({
                // Call your function here
                bankVerifyalertDialog.dismiss()
            }, 2000) // 3500 milliseconds for LENGTH_LONG



            //bankVerifyalertDialog.show()

        }
        bankVerifyalertDialog.show()
    }


    private fun BankVerifyCall(et_acc_num: EditText?, et_bank_code: EditText?) {
        //showLoader()
        if (NetworkUtils.isConnectedToNetwork(requireContext())) {

            BankVerifyApiCall(et_acc_num,et_bank_code)

        } else {
            dismissLoader()
            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
        }
    }

    private fun BankVerifyApiCall(etAccNum: EditText?, etBankCode: EditText?) {
        bankViewModel.verifyBankAccount(
            InputBankVerification(
                accountNumber = etAccNum?.text.toString(),
                bankCode = etBankCode?.text.toString()

            )
        )
        Log.d("bankVerifyApiPresenter", "bankVerify account ${etAccNum?.text}")
    }


    private fun isValidBankAccountNumber(): Boolean {

        //select bank
        if (sp_retireebank_.selectedItemPosition == 0) {
            Toast.makeText(context, "Please Select Bank", Toast.LENGTH_SHORT).show()
            return false
        }

        //acc number
        if (TextUtils.isEmpty(et_retireebank_accnum.text)) {
            Toast.makeText(context, "Empty account number", Toast.LENGTH_LONG).show()
            return false
        } else if (!ACC_NO_PATTERN.matcher(et_retireebank_accnum.text.toString()).matches()) {
            Toast.makeText(
                context, "Account number Not valid, must 10-12 digits", Toast.LENGTH_LONG
            ).show()
            return false
        }


        //re enter acc number
        if (TextUtils.isEmpty(et_retireebank_re_accnum.text)) {
            Toast.makeText(context, "Empty reAccount number", Toast.LENGTH_LONG).show()
            return false
        } else if (!ACC_NO_PATTERN.matcher(et_retireebank_re_accnum.text.toString()).matches()) {
            Toast.makeText(context, "reAccount number Not valid, , must 10-12 digits", Toast.LENGTH_LONG).show()
            return false
        } else if (et_retireebank_accnum.text.toString() != et_retireebank_re_accnum.text.toString()) {
            Toast.makeText(
                context, "Account numbers doesn't match", Toast.LENGTH_LONG
            ).show()
            return false
        }


        //acc name
        if (TextUtils.isEmpty(et_retireebank_accname.text)) {
            Toast.makeText(context, "Empty account Name", Toast.LENGTH_LONG).show()
            return false
        } else if (!FULL_NAME_PATTERN.matcher(et_retireebank_accname.text.toString()).matches()) {

            Toast.makeText(context, "account name not valid", Toast.LENGTH_SHORT).show()
            return false
        }

        //swift code
        if (TextUtils.isEmpty(et_retireebank_swiftcode.text)) {
            Toast.makeText(context, "Please enter SwiftCode", Toast.LENGTH_LONG).show()
            return false
        }

        //bank code
        if (TextUtils.isEmpty(et_retireebank_bankcode.text)) {
            Toast.makeText(context, "Please enter BankCode", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }


    private fun isValidRBank(): Boolean {

        //select bank
        if (sp_retireebank_.selectedItemPosition == 0) {
            Toast.makeText(context, "Please Select Bank", Toast.LENGTH_SHORT).show()
            return false
        }

        //acc number
        if (TextUtils.isEmpty(et_retireebank_accnum.text)) {
            Toast.makeText(context, "Empty account number", Toast.LENGTH_LONG).show()
            return false
        } else if (!ACC_NO_PATTERN.matcher(et_retireebank_accnum.text.toString()).matches()) {
            Toast.makeText(
                context, "Account number not valid", Toast.LENGTH_LONG
            ).show()
            return false
        }


        //re enter acc number
        if (TextUtils.isEmpty(et_retireebank_re_accnum.text)) {
            Toast.makeText(context, "Empty reAccount number", Toast.LENGTH_LONG).show()
            return false
        } else if (!ACC_NO_PATTERN.matcher(et_retireebank_re_accnum.text.toString()).matches()) {
            Toast.makeText(context, "reAccount number not valid", Toast.LENGTH_LONG).show()
            return false
        } else if (et_retireebank_accnum.text.toString() != et_retireebank_re_accnum.text.toString()) {
            Toast.makeText(
                context, "Account numbers doesn't match", Toast.LENGTH_LONG
            ).show()
            return false
        }


        //acc name
        if (TextUtils.isEmpty(et_retireebank_accname.text)) {
            Toast.makeText(context, "Empty account Name", Toast.LENGTH_LONG).show()
            return false
        } else if (!FULL_NAME_PATTERN.matcher(et_retireebank_accname.text.toString()).matches()) {

            Toast.makeText(context, "account name not valid", Toast.LENGTH_SHORT).show()
            return false
        }

        //swift code
        if (TextUtils.isEmpty(et_retireebank_swiftcode.text)) {
            Toast.makeText(context, "Please enter SwiftCode", Toast.LENGTH_LONG).show()
            return false
        }

        //bank code
        if (TextUtils.isEmpty(et_retireebank_bankcode.text)) {
            Toast.makeText(context, "Please enter BankCode", Toast.LENGTH_LONG).show()
            return false
        }

        //bank type
        if (sp_retireebank_acctype.selectedItemPosition == 0) {

            Toast.makeText(context, "Please Select Bank Type", Toast.LENGTH_LONG).show()
            return false
        }

        if (!isBankVerifyBtn){
            Toast.makeText(context, "Please Verify Bank code", Toast.LENGTH_LONG).show()
            return false
        }

        //Toast.makeText(context, "Retiree Bank info validated", Toast.LENGTH_LONG).show()

        return true
    }


    private fun BankinformationCall() {
        bankViewModel.submitBankInfo(
            InputActiveBankInfo(
//                userId = prefs.user_id?.toInt(),

                bankId = r_bankid/*"7b8dc580-ba28-8f3b-354410354410351ab4"*/,
                accountNumber = et_retireebank_accnum.text.toString(),
                bankCode = et_retireebank_bankcode.text.toString(),
                accountType = r_accounttype /*sp_retireebank_acctype.selectedItemPosition.toString()*/,
                accountHolderName = et_retireebank_accname.text.toString(),
                swiftCode = et_retireebank_swiftcode.text.toString(),
                reEnterAccountNumber = et_retireebank_re_accnum.text.toString(),
                autoRenewal = autoRenewal,
//                userId = prefs.user_id


            )
        )
    }


    fun onSwiftBankCodeSuccess(response: ResponseSwiftBankCode) {
        dismissLoader()
        et_retireebank_bankcode.text = Editable.Factory.getInstance()
            .newEditable(response.swiftbankdetail?.swiftCodeResponse?.bankCode)
    }

    private fun onRetireeBankInfoSubmitSuccess(response: ResponseBankInfo) {
        prefs.onboardingStage = OnboardingStage.PROCESSING
        dismissLoader()
        Toast.makeText(context, "${response.detail?.message}", Toast.LENGTH_LONG).show()

        /*     val intent = Intent(context, DashboardActivity::class.java)
             intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
             startActivity(intent)*/
        /* addEINDialog()*/

        Handler(Looper.getMainLooper()).postDelayed({
// Call your function here
            addEINDialog()
        }, Toast.LENGTH_SHORT.toLong() * 1000L)

    }

    private fun addEINDialog() {
        val add_einbuilder = AlertDialog.Builder(requireContext())
        val add_einview: View =
            LayoutInflater.from(requireContext()).inflate(R.layout.card_ein_number, null)
        add_einbuilder.setView(add_einview)
        val add_einalertDialog: AlertDialog = add_einbuilder.create()

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

        if (NetworkUtils.isConnectedToNetwork(requireContext())) {
            //einnumber
            /* showLoader()*/
            EinSubmitCall(et_ein_number_popupapicall)

        } else {/*       dismissLoader()*/
            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
        }


    }

    private fun EinSubmitCall(et_ein_number_popupsub: EditText?) {
        bankViewModel.submitEin(et_ein_number_popupsub!!.text.toString())
        Log.d("Ein", "EIN_Number${et_ein_number_popupsub.text}")
    }

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

    private fun onRetireeEinNumberSubmitSuccess(response: ResponseEinNumber) {
        Toast.makeText(context, response.detail?.message, Toast.LENGTH_SHORT).show()


        val intent = Intent(context, ProcessDashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    private fun onBankVerifySubmitSuccess(response: ResponseBankVerify) {
        dismissLoader()
        Toast.makeText(context, response.detail?.message, Toast.LENGTH_SHORT).show()

        //prefs.isBankVerify = true

        isBankVerifyBtn = true
        tv_retireebank_bankcode_verify.visibility = View.INVISIBLE
        tv_retireebank_bankcode_reverify.visibility = View.INVISIBLE
        tv_retireebank_bankcode_verified.visibility = View.VISIBLE
    }
}
