package com.example.engu_pension_verification_application.ui.fragment.service.active

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
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
import com.bumptech.glide.Glide
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.databinding.FragmentActiveBankBinding
import com.example.engu_pension_verification_application.model.input.InputActiveBankInfo
import com.example.engu_pension_verification_application.model.input.InputBankVerification
import com.example.engu_pension_verification_application.model.response.*
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.activity.ProcessDashboardActivity
import com.example.engu_pension_verification_application.ui.adapter.AccountTypeAdapter
import com.example.engu_pension_verification_application.ui.adapter.BankAdapter
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.AppUtils
import com.example.engu_pension_verification_application.util.OnboardingStage
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.ActiveBankViewModel
import com.example.engu_pension_verification_application.viewmodel.ActiveServiceViewModel
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import kotlin.collections.ArrayList

val filterUpperCaseAndDigits = InputFilter { source, start, end, dest, dstart, dend ->
    for (index in start until end) {
        if (!Character.isDigit(source[index]) && !Character.isUpperCase(source[index])) {
            return@InputFilter ""
        }
    }
    null // Accepts the original characters
}
class ActiveBankFragment: BaseFragment() {
    private lateinit var binding:FragmentActiveBankBinding
    companion object {
        const val TAB_POSITION = 2
        private const val BANK_ITEM_SELECT_ID = -1
    }
    var bankdetailsList = mutableListOf<ListBanksItem?>()
    var accountTypeList = mutableListOf<AccountTypeItem?>()

    private val activeServiceViewModel by activityViewModels<ActiveServiceViewModel>()

    val FULL_NAME_PATTERN =
        Pattern.compile("^[a-zA-Z]+(?:\\s[a-zA-Z]+)*$") /*Pattern.compile("^[a-zA-Z\\s]+$")*/

    val ACC_NO_PATTERN = Pattern.compile("\\d+")
    val ACC_NO_PATTERN_TWO = Pattern.compile("\\d{10,12}")

    private lateinit var activeBankViewModel: ActiveBankViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2


    var a_bankid = ""
    var a_accounttype = ""
    var autoRenewal: Boolean = false
    var isBankVerifyBtn = false

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

        binding.etActivebankSwiftcode.filters = arrayOf(InputFilter.AllCaps(), filterUpperCaseAndDigits)
        binding.etActivebankAccname.setText(AppUtils.getFullName(prefs.first_name,prefs.middle_name,prefs.last_name))

//        setAdapter()
        // initcall()  - hold
        OnTextWatcher()
        onClicked()
        //observeActiveBankDetails()
    }

    private fun initViewModels() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        activeBankViewModel = ViewModelProviders.of(
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
            if (it == TAB_POSITION) activeBankViewModel.fetchBankList()
        }
        activeBankViewModel.bankListApiResult.observe(viewLifecycleOwner) { response ->
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
                            activeBankViewModel.fetchBankList()
                        }
                    }
                } else {
                    dismissLoader()
                    showFetchErrorDialog(
                        activeBankViewModel::fetchBankList,
                        response.detail?.message ?: getString(R.string.common_error_msg_2)
                    )
                }
            }
        }
        activeBankViewModel.bankDetailsApiResult.observe(viewLifecycleOwner) { pair ->
            val swiftCode = pair.first
            val response = pair.second
            if (response.swiftbankdetail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                onSwiftBankCodeSuccess(response)
            } else {
                if (response.swiftbankdetail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            activeBankViewModel.fetchBankDetails(swiftCode)
                        }
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, response.swiftbankdetail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        activeBankViewModel.bankInfoSubmissionResult.observe(viewLifecycleOwner) { pair ->
            val inputActiveBankInfo = pair.first
            val response = pair.second
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                onActiveBankInfoSubmitSuccess(response)
            }else if (response.detail?.status == AppConstants.FAIL) {
                dismissLoader()
                Toast.makeText(context, response.detail.message, Toast.LENGTH_LONG).show()
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            activeBankViewModel.submitBankInfo(inputActiveBankInfo)
                        }
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        activeBankViewModel.bankVerificationResult.observe(viewLifecycleOwner) { pair ->
            val inputBankVerification = pair.first
            val response = pair.second
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                onBankVerifySubmitSuccess(response)
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            activeBankViewModel.verifyBankAccount(inputBankVerification)
                        }
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                    isBankVerifyBtn = false
                    binding.tvActivebankBankcodeVerify.visibility = View.INVISIBLE
                    binding.tvActivebankBankcodeReverify.visibility = View.VISIBLE
                    binding.tvActivebankBankcodeVerified.visibility = View.INVISIBLE

                }
            }
        }
        activeBankViewModel.einSubmissionResult.observe(viewLifecycleOwner) { pair ->
            val ein = pair.first
            val response = pair.second
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                onEinNumberSubmitSuccess(response)
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            activeBankViewModel.submitEin(ein)
                        }
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
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

            AccountTypeList.add(AccountTypeItem(0, " - Select Account Type - "))
            accountTypeList.forEach {
                AccountTypeList.add(AccountTypeItem(it?.id, it?.type))
            }
        }
        accounttypeAdapter = AccountTypeAdapter(context, AccountTypeList)
        binding.spActivebankAcctype.adapter = accounttypeAdapter


    }

    private fun OnTextWatcher() {

        binding.etActivebankSwiftcode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {

                binding.etActivebankSwiftcode.setOnFocusChangeListener { view, hasFocus ->
                    if (!hasFocus) {
                        // TODO: Uncomment after fixing api -> "/api/v1/get_bank_details"
//                        activeBankViewModel.fetchBankDetails(binding.etActivebankSwiftcode.text.toString())

                    }
                }


            }
        })

        binding.spActiveBank.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
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

    }

    private fun onClicked() {

        // Assuming you have a CheckBox with the ID 'myCheckBox' in your layout

        binding.cbActivebankAutorenewal.setOnCheckedChangeListener { buttonView, isChecked ->
            autoRenewal = isChecked
        }

        binding.tvActivebankBankcodeVerify.setOnClickListener{

            if (isValidBankAccountNumber()){
                bankVerifyDialog()
            }
        }

        binding.tvActivebankBankcodeReverify.setOnClickListener{
            if (isValidBankAccountNumber()){
                bankVerifyDialog()
            }
        }


        binding.llActivebankNext.setOnClickListener {
            if (isValidBank()) {
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


    //Validation Bank info
    private fun isValidBank(): Boolean {

        //select bank
        if (binding.spActiveBank.selectedItemPosition == 0) {
            Toast.makeText(context, "Please Select Bank", Toast.LENGTH_SHORT).show()
            return false
        }

        //acc number
        if (TextUtils.isEmpty(binding.etActivebankAccnum.text)) {
            Toast.makeText(context, "Empty account number", Toast.LENGTH_LONG).show()
            return false
        } else if (!ACC_NO_PATTERN.matcher(binding.etActivebankAccnum.text.toString()).matches()) {
            Toast.makeText(context, "Account number not valid", Toast.LENGTH_LONG).show()
            return false
        }

        //re enter acc number
        if (TextUtils.isEmpty(binding.etActivebankReAccnum.text)) {
            Toast.makeText(context, "Empty reAccount number", Toast.LENGTH_LONG).show()
            return false
        } else if (!ACC_NO_PATTERN.matcher(binding.etActivebankReAccnum.text.toString()).matches()) {
            Toast.makeText(context, "reAccount number not valid", Toast.LENGTH_LONG).show()
            return false
        } else if (binding.etActivebankAccnum.text.toString() != binding.etActivebankReAccnum.text.toString()) {
            Toast.makeText(context, "Account numbers doesn't match", Toast.LENGTH_LONG).show()
            return false
        }

        //acc name
        if (TextUtils.isEmpty(binding.etActivebankAccname.text)) {
            Toast.makeText(context, "Empty account Name", Toast.LENGTH_LONG).show()
            return false
        } else if (!FULL_NAME_PATTERN.matcher(binding.etActivebankAccname.text.toString().trim()).matches()) {

            Toast.makeText(context, "account name not valid", Toast.LENGTH_SHORT).show()
            return false
        }

        //swift
        if (TextUtils.isEmpty(binding.etActivebankSwiftcode.text)) {
            Toast.makeText(context, "Please enter SwiftCode", Toast.LENGTH_LONG).show()
            return false
        }
        //bankcode
        if (TextUtils.isEmpty(binding.etActivebankBankcode.text)) {
            Toast.makeText(context, "Please enter BankCode", Toast.LENGTH_LONG).show()
            return false
        }
        //accounttype
        if (binding.spActivebankAcctype.selectedItemPosition == 0) {
            Toast.makeText(context, "Please Select Account Type", Toast.LENGTH_LONG).show()
            return false
        }

        if (!isBankVerifyBtn){
            Toast.makeText(context, "Please Verify Bank code", Toast.LENGTH_LONG).show()
            return false
        }


        /*Toast.makeText(context, "Active Bank info validated", Toast.LENGTH_LONG)
            .show()*/
        return true
    }
    //END Validation Bank info

    private fun BankinformationCall() {
        activeBankViewModel.submitBankInfo(
            InputActiveBankInfo(

                bankId = a_bankid/*"7b8dc580-ba28-8f3b-354410354410351ab4"*//*binding.spActiveBank.selectedItemPosition.toString()*/,
                accountNumber = binding.etActivebankAccnum.text.toString(),
                bankCode = binding.etActivebankBankcode.text.toString(),
                accountType = a_accounttype/*binding.spActivebankAcctype.selectedItemPosition.toString()*/,
                accountHolderName = binding.etActivebankAccname.text.toString(),
                swiftCode = binding.etActivebankSwiftcode.text.toString(),
                reEnterAccountNumber = binding.etActivebankReAccnum.text.toString(),
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


    fun onSwiftBankCodeSuccess(response: ResponseSwiftBankCode) {
        dismissLoader()
        binding.etActivebankBankcode.text = Editable.Factory.getInstance()
            .newEditable(response.swiftbankdetail?.swiftCodeResponse?.bankCode)

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

        et_bank_verify_acc_number.text = binding.etActivebankAccnum.text

        et_bank_verify_bank_code.text = binding.etActivebankBankcode.text

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

        activeBankViewModel.verifyBankAccount(
            InputBankVerification(
                accountNumber = etAccNum?.text.toString(),
                bankCode = etBankCode?.text.toString()

            )
        )
        Log.d("bankVerifyApiPresenter", "bankVerify account ${etAccNum?.text}")
    }

    private fun EinSubmitCall(et_ein_number_popupsub: EditText?) {
        activeBankViewModel.submitEin(et_ein_number_popupsub!!.text.toString())
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

    private fun isValidBankAccountNumber(): Boolean {

        //select bank
        if (binding.spActiveBank.selectedItemPosition == 0) {
            Toast.makeText(context, "Please Select Bank", Toast.LENGTH_SHORT).show()
            return false
        }

        //acc number
        if (TextUtils.isEmpty(binding.etActivebankAccnum.text)) {
            Toast.makeText(context, "Empty account number", Toast.LENGTH_LONG).show()
            return false
        } else if (!ACC_NO_PATTERN_TWO.matcher(binding.etActivebankAccnum.text.toString()).matches()) {
            Toast.makeText(context, "Account number Not valid, must 10-12 digits", Toast.LENGTH_LONG).show()
            return false
        }

        //re enter acc number
        if (TextUtils.isEmpty(binding.etActivebankReAccnum.text)) {
            Toast.makeText(context, "Empty reAccount number", Toast.LENGTH_LONG).show()
            return false
        } else if (!ACC_NO_PATTERN_TWO.matcher(binding.etActivebankReAccnum.text.toString()).matches()) {
            Toast.makeText(context, "reAccount number Not valid, , must 10-12 digits", Toast.LENGTH_LONG).show()
            return false
        } else if (binding.etActivebankAccnum.text.toString() != binding.etActivebankReAccnum.text.toString()) {
            Toast.makeText(context, "Account numbers doesn't match", Toast.LENGTH_LONG).show()
            return false
        }

        //acc name
        if (TextUtils.isEmpty(binding.etActivebankAccname.text)) {
            Toast.makeText(context, "Empty account Name", Toast.LENGTH_LONG).show()
            return false
        } else if (!FULL_NAME_PATTERN.matcher(binding.etActivebankAccname.text.toString().trim()).matches()) {
            Toast.makeText(context, "account name not valid", Toast.LENGTH_SHORT).show()
            return false
        }

        //swift
        if (TextUtils.isEmpty(binding.etActivebankSwiftcode.text)) {
            Toast.makeText(context, "Please enter SwiftCode", Toast.LENGTH_LONG).show()
            return false
        }
        //bankcode
        if (TextUtils.isEmpty(binding.etActivebankBankcode.text)) {
            Toast.makeText(context, "Please enter BankCode", Toast.LENGTH_LONG).show()
            return false
        }

        return true
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

    fun onBankVerifySubmitSuccess(response: ResponseBankVerify) {
        dismissLoader()
        Toast.makeText(context, response.detail?.message, Toast.LENGTH_SHORT).show()

        //prefs.isBankVerify = true

        isBankVerifyBtn = true
        binding.tvActivebankBankcodeVerify.visibility = View.INVISIBLE
        binding.tvActivebankBankcodeReverify.visibility = View.INVISIBLE
        binding.tvActivebankBankcodeVerified.visibility = View.VISIBLE
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
    // TODO: Remove this function after fixing api -> "/api/v1/get_bank_details"
    private fun refreshBankCode(position:Int) {
        val bankCode = if (BankList[position]?.id != BANK_ITEM_SELECT_ID) BankList[position]?.code else ""
        binding.etActivebankBankcode.setText(bankCode)
    }
}

