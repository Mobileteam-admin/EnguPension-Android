package com.example.engu_pension_verification_application.ui.fragment.service.active

import android.annotation.SuppressLint
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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.commons.Loader
import com.example.engu_pension_verification_application.model.input.InputActiveBankInfo
import com.example.engu_pension_verification_application.model.input.InputBankVerification
import com.example.engu_pension_verification_application.model.input.InputEinNumber
import com.example.engu_pension_verification_application.model.input.InputSwiftBankCode
import com.example.engu_pension_verification_application.model.response.*
import com.example.engu_pension_verification_application.ui.activity.ProcessDashboardActivity
import com.example.engu_pension_verification_application.ui.activity.SignUpActivity
import com.example.engu_pension_verification_application.ui.fragment.service.EIN_Number.EIN_Number_CallBack
import com.example.engu_pension_verification_application.ui.fragment.service.EIN_Number.EIN_Number_Presenter
import com.example.engu_pension_verification_application.ui.fragment.service.accounttype.AccountTypeAdapter
import com.example.engu_pension_verification_application.ui.fragment.service.bank.BankAdapter
import com.example.engu_pension_verification_application.ui.fragment.service.bank_verification.Bank_Verify_Callback
import com.example.engu_pension_verification_application.ui.fragment.service.bank_verification.Bank_Verify_Presenter
import com.example.engu_pension_verification_application.ui.fragment.tokenrefresh.TokenRefreshCallBack
import com.example.engu_pension_verification_application.ui.fragment.tokenrefresh.TokenRefreshViewModel
import com.example.engu_pension_verification_application.utils.SharedPref
import kotlinx.android.synthetic.main.fragment_active_bank.*
import kotlinx.android.synthetic.main.fragment_retiree_bank.et_retireebank_swiftcode
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
class ActiveBankFragment(
    var bankdetailsList: ArrayList<ListBanksItem?>,
    @JvmField var accountTypeList: ArrayList<AccountTypeItem?>,
) : Fragment(), TokenRefreshCallBack, ActiveBankCallBack, EIN_Number_CallBack,Bank_Verify_Callback {

    val FULL_NAME_PATTERN =
        Pattern.compile("^[a-zA-Z]+(?:\\s[a-zA-Z]+)*$") /*Pattern.compile("^[a-zA-Z\\s]+$")*/

    val ACC_NO_PATTERN = Pattern.compile("\\d+")
    val ACC_NO_PATTERN_TWO = Pattern.compile("\\d{10,12}")

    private lateinit var activeBankViewModel: ActiveBankViewModel
    private lateinit var tokenRefreshViewModel: TokenRefreshViewModel

    private lateinit var einnumberpresenter: EIN_Number_Presenter

    private lateinit var bankVerifyPresenter: Bank_Verify_Presenter


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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_active_bank, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //activeBankViewModel = ViewModelProvider(this).get(ActiveBankViewModel::class.java)
        activeBankViewModel = ActiveBankViewModel(this)

        /*tokenRefreshViewModel = ViewModelProvider(this).get(TokenRefreshViewModel::class.java)*/
        tokenRefreshViewModel = TokenRefreshViewModel(this)

        einnumberpresenter = EIN_Number_Presenter(this)

        bankVerifyPresenter = Bank_Verify_Presenter(this)

        //local use
//        et_activebank_swiftcode.text = Editable.Factory.getInstance().newEditable("MOOGNGL1")
        //local use
        /*et_activebank_swiftcode.text = Editable.Factory.getInstance().newEditable("MOOGNGL1")
        et_activebank_bankcode.text = Editable.Factory.getInstance().newEditable("MOOG")*/

        et_activebank_swiftcode.filters = arrayOf(InputFilter.AllCaps(), filterUpperCaseAndDigits)

        et_activebank_accname.text = Editable.Factory.getInstance().newEditable(
            prefs.first_name + if (prefs.middle_name != "") {
                " " + prefs.middle_name + " "
            } else {
                " "

            } + prefs.last_name
        )






        setAdapter()
        // initcall()  - hold
        OnTextWatcher()
        onClicked()

        //observeActiveBankDetails()


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
         sp_active_bank.adapter = bankAdapter



         if (accountTypeList.size > 0) {
             AccountTypeList.add(AccountTypeItem(0, " - Select AccountType - "))
             accountTypeList.forEach {
                 AccountTypeList.add(AccountTypeItem(it?.id, it?.type))
             }
         }
         accounttypeAdapter = AccountTypeAdapter(context, AccountTypeList)
         sp_activebank_acctype.adapter = accounttypeAdapter*/

        if (bankdetailsList.size > 0) {

            BankList.clear()

            BankList.add(ListBanksItem("", "- select Bank", "0", "", 1, ""))
            bankdetailsList.forEach {
                BankList.add(
                    ListBanksItem(
                        it?.code, it?.name, it?.ussd, it?.logo, it?.id, it?.slug
                    )
                )
            }
        }
        bankAdapter = BankAdapter(context, BankList)
        sp_active_bank.adapter = bankAdapter



        if (accountTypeList.size > 0) {

            AccountTypeList.clear()

            AccountTypeList.add(AccountTypeItem(0, " - Select AccountType - "))
            accountTypeList.forEach {
                AccountTypeList.add(AccountTypeItem(it?.id, it?.type))
            }
        }
        accounttypeAdapter = AccountTypeAdapter(context, AccountTypeList)
        sp_activebank_acctype.adapter = accounttypeAdapter


    }

    private fun OnTextWatcher() {

        et_activebank_swiftcode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {

                et_activebank_swiftcode.setOnFocusChangeListener { view, hasFocus ->
                    if (!hasFocus) {

                        activeBankViewModel.getswiftBankCode(InputSwiftBankCode(swiftCode = et_activebank_swiftcode.text.toString()))

                    }
                }


            }
        })

        sp_active_bank.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                if (BankList.get(position)?.id?.equals(0) == true) {

                } else {
                    a_bankid = BankList[position]?.id.toString()
                    //prefs.A_BANK = sp_active_bank.selectedItemPosition.toString()
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        sp_activebank_acctype.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                if (AccountTypeList.get(position)?.id?.equals(0) == true) {


                } else {
                    a_accounttype = AccountTypeList[position]?.type.toString()
                    //prefs.A_ACCTYPE = sp_activebank_acctype.selectedItemPosition.toString()
                }
            }


            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

    }

    private fun onClicked() {

        // Assuming you have a CheckBox with the ID 'myCheckBox' in your layout

        cb_activebank_autorenewal.setOnCheckedChangeListener { buttonView, isChecked ->
            autoRenewal = isChecked
        }

        tv_activebank_bankcode_verify.setOnClickListener{

            if (isValidBankAccountNumber()){
                bankVerifyDialog()
            }
        }

        tv_activebank_bankcode_reverify.setOnClickListener{
            if (isValidBankAccountNumber()){
                bankVerifyDialog()
            }
        }


        ll_activebank_next.setOnClickListener {
            if (isValidBank()) {
                //finish the Form
                if (context?.isConnectedToNetwork()!!) {
                    //bank
                    Loader.showLoader(requireContext())
                    BankinformationCall()


                } else {
                    Loader.hideLoader()
                    Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
                }
            }

        }


    }


    //Validation Bank info
    private fun isValidBank(): Boolean {

        //select bank
        if (sp_active_bank.selectedItemPosition == 0) {
            Toast.makeText(context, "Please Select Bank", Toast.LENGTH_SHORT).show()
            return false
        }

        //acc number
        if (TextUtils.isEmpty(et_activebank_accnum.text)) {
            Toast.makeText(context, "Empty account number", Toast.LENGTH_LONG).show()
            return false
        } else if (!ACC_NO_PATTERN.matcher(et_activebank_accnum.text.toString()).matches()) {
            Toast.makeText(context, "Account number not valid", Toast.LENGTH_LONG).show()
            return false
        }

        //re enter acc number
        if (TextUtils.isEmpty(et_activebank_re_accnum.text)) {
            Toast.makeText(context, "Empty reAccount number", Toast.LENGTH_LONG).show()
            return false
        } else if (!ACC_NO_PATTERN.matcher(et_activebank_re_accnum.text.toString()).matches()) {
            Toast.makeText(context, "reAccount number not valid", Toast.LENGTH_LONG).show()
            return false
        } else if (et_activebank_accnum.text.toString() != et_activebank_re_accnum.text.toString()) {
            Toast.makeText(context, "Account numbers doesn't match", Toast.LENGTH_LONG).show()
            return false
        }

        //acc name
        if (TextUtils.isEmpty(et_activebank_accname.text)) {
            Toast.makeText(context, "Empty account Name", Toast.LENGTH_LONG).show()
            return false
        } else if (!FULL_NAME_PATTERN.matcher(et_activebank_accname.text.toString()).matches()) {

            Toast.makeText(context, "account name not valid", Toast.LENGTH_SHORT).show()
            return false
        }

        //swift
        if (TextUtils.isEmpty(et_activebank_swiftcode.text)) {
            Toast.makeText(context, "Please enter SwiftCode", Toast.LENGTH_LONG).show()
            return false
        }
        //bankcode
        if (TextUtils.isEmpty(et_activebank_bankcode.text)) {
            Toast.makeText(context, "Please enter BankCode", Toast.LENGTH_LONG).show()
            return false
        }
        //accounttype
        if (sp_activebank_acctype.selectedItemPosition == 0) {
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
        activeBankViewModel.ActiveBankinformation(
            InputActiveBankInfo(

                bankId = a_bankid/*"7b8dc580-ba28-8f3b-354410354410351ab4"*//*sp_active_bank.selectedItemPosition.toString()*/,
                accountNumber = et_activebank_accnum.text.toString(),
                bankCode = et_activebank_bankcode.text.toString(),
                accountType = a_accounttype/*sp_activebank_acctype.selectedItemPosition.toString()*/,
                accountHolderName = et_activebank_accname.text.toString(),
                swiftCode = et_activebank_swiftcode.text.toString(),
                reEnterAccountNumber = et_activebank_re_accnum.text.toString(),
                autoRenewal = autoRenewal,
                userId = prefs.user_id

            )
        )
    }


    /*   private fun initcall() {

           if (context?.isConnectedToNetwork()!!) {
               Loader.showLoader(requireContext())
               // call bank details retrive api  - hold now

           } else {
               Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
           }
       }*/


    private fun clearLogin() {
        prefs.isLogin = false
        prefs.user_id = ""
        prefs.user_name = ""
        prefs.email = ""
        prefs.access_token = ""
        prefs.refresh_token = ""
        //prefs.isBankVerify = false
    }

    fun Context.isConnectedToNetwork(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting() ?: false
    }

    override fun onTokenRefreshSuccess(response: ResponseRefreshToken) {
        Loader.hideLoader()
        Toast.makeText(context, "Please wait.....", Toast.LENGTH_SHORT).show()

        //again listing api call
//                    initcall()
        BankinformationCall()
    }

    override fun onTokenRefreshFailure(response: ResponseRefreshToken) {
        Loader.hideLoader()
        Toast.makeText(
            context, response.token_detail?.message, Toast.LENGTH_LONG
        ).show()

        clearLogin()
        val intent = Intent(context, SignUpActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    override fun onSwiftBankCodeSuccess(response: ResponseSwiftBankCode) {
        Loader.hideLoader()
        et_activebank_bankcode.text = Editable.Factory.getInstance()
            .newEditable(response.swiftbankdetail?.swiftCodeResponse?.bankCode)

    }

    override fun onSwiftBankCodeFailure(response: ResponseSwiftBankCode) {
        Loader.hideLoader()
        if (response.swiftbankdetail?.tokenStatus.equals("expired")) {
            Toast.makeText(context, "Please wait.....", Toast.LENGTH_SHORT).show()
            //refresh api call
            tokenRefreshViewModel.getTokenRefresh()
            activeBankViewModel.getswiftBankCode(InputSwiftBankCode(swiftCode = et_activebank_swiftcode.text.toString()))

        } else {
            Toast.makeText(
                context, response.swiftbankdetail?.message, Toast.LENGTH_LONG
            ).show()
        }

    }

    override fun onActiveBankInfoSubmitSuccess(response: ResponseBankInfo) {


        Loader.hideLoader()
//new tablock
        prefs.isActiveBankSubmit = true
        prefs.isActiveDocSubmit = true

        prefs.isActiveBasicSubmit = false

        // Toast.makeText(context, response.detail., Toast.LENGTH_LONG).show()
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

        et_bank_verify_acc_number.text = et_activebank_accnum.text

        et_bank_verify_bank_code.text = et_activebank_bankcode.text

        val bank_verify_submit = bankVerifyView.findViewById<LinearLayout>(R.id.ll_bankverifysubmit)
        bankVerifyalertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bankVerifyalertDialog.setCancelable(true)

        bank_verify_submit.setOnClickListener {
             Loader.showLoader(requireContext())
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
                Loader.showLoader(requireContext())
                EINApicall(et_ein_number_popup) // Pass the EditText to the function
            }
            add_einalertDialog.show()
        }
        add_einalertDialog.show()
    }

    private fun EINApicall(et_ein_number_popupapicall: EditText?) {
        //Loader.showLoader(requireContext())
        if (context?.isConnectedToNetwork()!!) {
            //einnumber
            /* Loader.showLoader(requireContext())*/
            EinSubmitCall(et_ein_number_popupapicall)

        } else {/*       Loader.hideLoader()*/
            Loader.hideLoader()
            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
        }
    }

    private fun BankVerifyCall(et_acc_num: EditText?, et_bank_code: EditText?) {
        //Loader.showLoader(requireContext())
        if (context?.isConnectedToNetwork()!!) {

            BankVerifyApiCall(et_acc_num,et_bank_code)

        } else {
            Loader.hideLoader()
            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
        }
    }

    private fun BankVerifyApiCall(etAccNum: EditText?, etBankCode: EditText?) {

        bankVerifyPresenter.BankVerify_Submit(
            InputBankVerification(
                accountNumber = etAccNum?.text.toString(),
                bankCode = etBankCode?.text.toString()

            )
        )
        Log.d("bankVerifyApiPresenter", "bankVerify account ${etAccNum?.text}")
    }

    private fun EinSubmitCall(et_ein_number_popupsub: EditText?) {

        einnumberpresenter.Ein_Number_Submit(
            InputEinNumber(/* ein = "1234567890"*/
                ein = et_ein_number_popupsub!!.text.toString()


            )
        )
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
        if (sp_active_bank.selectedItemPosition == 0) {
            Toast.makeText(context, "Please Select Bank", Toast.LENGTH_SHORT).show()
            return false
        }

        //acc number
        if (TextUtils.isEmpty(et_activebank_accnum.text)) {
            Toast.makeText(context, "Empty account number", Toast.LENGTH_LONG).show()
            return false
        } else if (!ACC_NO_PATTERN_TWO.matcher(et_activebank_accnum.text.toString()).matches()) {
            Toast.makeText(context, "Account number Not valid, must 10-12 digits", Toast.LENGTH_LONG).show()
            return false
        }

        //re enter acc number
        if (TextUtils.isEmpty(et_activebank_re_accnum.text)) {
            Toast.makeText(context, "Empty reAccount number", Toast.LENGTH_LONG).show()
            return false
        } else if (!ACC_NO_PATTERN_TWO.matcher(et_activebank_re_accnum.text.toString()).matches()) {
            Toast.makeText(context, "reAccount number Not valid, , must 10-12 digits", Toast.LENGTH_LONG).show()
            return false
        } else if (et_activebank_accnum.text.toString() != et_activebank_re_accnum.text.toString()) {
            Toast.makeText(context, "Account numbers doesn't match", Toast.LENGTH_LONG).show()
            return false
        }

        //acc name
        if (TextUtils.isEmpty(et_activebank_accname.text)) {
            Toast.makeText(context, "Empty account Name", Toast.LENGTH_LONG).show()
            return false
        } else if (!FULL_NAME_PATTERN.matcher(et_activebank_accname.text.toString()).matches()) {

            Toast.makeText(context, "account name not valid", Toast.LENGTH_SHORT).show()
            return false
        }

        //swift
        if (TextUtils.isEmpty(et_activebank_swiftcode.text)) {
            Toast.makeText(context, "Please enter SwiftCode", Toast.LENGTH_LONG).show()
            return false
        }
        //bankcode
        if (TextUtils.isEmpty(et_activebank_bankcode.text)) {
            Toast.makeText(context, "Please enter BankCode", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }



    override fun onActiveBankInfoSubmitFailure(response: ResponseBankInfo) {
        Loader.hideLoader()

        if (response.detail?.tokenStatus.equals("expired")) {
            Toast.makeText(context, "Please wait.....", Toast.LENGTH_SHORT).show()
            //refresh api call
            tokenRefreshViewModel.getTokenRefresh()
            BankinformationCall()

        } else {
            Toast.makeText(
                context, response.detail?.message, Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onEinNumberSubmitSuccess(response: ResponseEinNumber) {

        Loader.hideLoader()
        Toast.makeText(context, response.detail?.message, Toast.LENGTH_SHORT).show()


        val intent = Intent(context, ProcessDashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    override fun onEinNumberSubmitFailure(response: ResponseEinNumber) {
        Loader.hideLoader()
        if (response.detail?.tokenStatus.equals("expired")) {
            Toast.makeText(context, "Please wait.....", Toast.LENGTH_SHORT).show()
            //refresh api call
            tokenRefreshViewModel.getTokenRefresh()/*  EinSubmitCall()*/

        } else {
            Toast.makeText(
                context, response.detail?.message, Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onBankVerifySubmitSuccess(response: ResponseBankVerify) {
        Loader.hideLoader()
        Toast.makeText(context, response.detail?.message, Toast.LENGTH_SHORT).show()

        //prefs.isBankVerify = true

        isBankVerifyBtn = true
        tv_activebank_bankcode_verify.visibility = View.INVISIBLE
        tv_activebank_bankcode_reverify.visibility = View.INVISIBLE
        tv_activebank_bankcode_verified.visibility = View.VISIBLE
    }

    override fun onBankVerifySubmitFailure(response: ResponseBankVerify) {
        Loader.hideLoader()
        if (response.detail?.tokenStatus.equals("expired")) {
            Toast.makeText(context, "Please wait.....", Toast.LENGTH_SHORT).show()
            //refresh api call
            tokenRefreshViewModel.getTokenRefresh()/*  EinSubmitCall()*/

        } else {
            Toast.makeText(
                context, response.detail?.message, Toast.LENGTH_LONG
            ).show()
        }
        isBankVerifyBtn = false

        tv_activebank_bankcode_verify.visibility = View.INVISIBLE
        tv_activebank_bankcode_reverify.visibility = View.VISIBLE
        tv_activebank_bankcode_verified.visibility = View.INVISIBLE
    }
}

