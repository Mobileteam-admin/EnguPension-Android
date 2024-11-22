package com.example.engu_pension_verification_application.ui.fragment.Dashboard

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.commons.Loader
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.response.DashboardDetails
import com.example.engu_pension_verification_application.model.response.ResponseDashboardDetails
import com.example.engu_pension_verification_application.model.response.ResponseLogout
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.activity.SignUpActivity
import com.example.engu_pension_verification_application.ui.dialog.LogoutConfirmDialog
import com.example.engu_pension_verification_application.util.NetworkUtils
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.DashboardViewModel
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.LogoutConfirmViewModel
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*







class DashboardFragment : Fragment() {


    private lateinit var UserDashboardDetails: DashboardDetails
    lateinit var datePickerDialog: DatePickerDialog
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    private val logoutConfirmViewModel by activityViewModels<LogoutConfirmViewModel>()


    var year = 0
    var month = 0
    var dayOfMonth = 0
    lateinit var calendar: Calendar

    var imgurl : String? = null
    var imgbank : String? = null


    val sdf = SimpleDateFormat("yyyy-MM-dd")
    val currentDate = sdf.format(Date())
    val prefs = SharedPref
    private lateinit var logoutConfirmDialog: LogoutConfirmDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
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
        initPayment()

    }

    private fun initPayment() {
        img_add_amount.setOnClickListener {

            val intent=Intent(requireContext(),PaymentActivity::class.java )
            startActivity(intent)

            PaymentConfiguration.init(
                requireContext(),
                "pk_live_51Q54MvHCzH2YQbvm4mfvkR3qYeflbejKMgW076PxFMfPAcoqGnTbTm7UFUsb277x0jItnqEPQWGP5Xtw6jzTMUXh000CnOoyxt"  // Replace with your actual publishable key
            )

        }
    }

    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), // use `this` if the ViewModel want to tie with fragment's lifecycle
            EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
        dashboardViewModel = ViewModelProviders.of(
            requireActivity(), // use `this` if the ViewModel want to tie with fragment's lifecycle
            EnguViewModelFactory(networkRepo)
        ).get(DashboardViewModel::class.java)
    }

    private fun observeLiveData() {
        tokenRefreshViewModel2.tokenRefreshError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                onTokenRefreshFailure(error)
            }
        }
        logoutConfirmViewModel.logout.observe(viewLifecycleOwner) { logout ->
            if(logout != null) callLogout()
        }
        dashboardViewModel.logoutResult.observe(viewLifecycleOwner) { response ->
            if (response.logout_detail?.status == AppConstants.SUCCESS) {
                ondashboardLogoutSuccess(response)
            } else {
                if (response.logout_detail?.tokenStatus == AppConstants.EXPIRED) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            dashboardViewModel.logout()
                        }
                    }
                } else {
                    Loader.hideLoader()
                    Toast.makeText(context, response.logout_detail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        dashboardViewModel.dashboardDetailsResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                ondashboardDetailsSuccess(response)
            } else {
                if (response.detail?.tokenStatus == AppConstants.EXPIRED) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            dashboardViewModel.fetchDashboardDetails()
                        }
                    }
                } else {
                    Loader.hideLoader()
                    showRetryDashboardFetchDialog(response.detail?.message?:getString(R.string.common_error_msg_2))
                }
            }
        }
    }
    private fun showRetryDashboardFetchDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton(R.string.retry
            ) { dialogInterface, _ ->
                initCall()
                dialogInterface.dismiss()
            }
            .setNegativeButton(R.string.close
            ) { _, _ ->
                requireActivity().finish()
            }
            .show()
    }
    private fun initViews() {
        logoutConfirmDialog = LogoutConfirmDialog()
    }
    private fun initCall() {
        if (NetworkUtils.isConnectedToNetwork(requireContext())) {
            //Bank Loader commented for Loader issue
            // Loader.showLoader(requireContext())
            dashboardViewModel.fetchDashboardDetails()
        } else {
            showRetryDashboardFetchDialog("Please connect to internet")
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun onClicked() {
        tv_profile_.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_profile)
        }
        img_add_amount.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_wallet)
        }
        ll_account.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_account)
        }

        ll_add_bank.setOnClickListener {
            addBankDialog()
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
            //showDatePickerM3(txt_booking_date)
        }

        txt_logout.setOnClickListener {
            logoutConfirmDialog.show(parentFragmentManager,null)
        }
    }

    private fun clearLogin() {
        prefs.isLogin = false
        prefs.user_id = ""
        prefs.user_name = ""
        prefs.email = ""
        prefs.access_token = ""
        prefs.refresh_token = ""
        prefs.isGovVerify = false
        prefs.isActiveDocSubmit = false
        prefs.isActiveBasicSubmit = false
        prefs.isRBasicSubmit = false
        prefs.isRDocSubmit = false
        //prefs.isBankVerify = false
    }
    @RequiresApi(Build.VERSION_CODES.O)


    private fun onDateSelect() {

        calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH)
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
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
        // datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis())
        datePickerDialog.show()

        /*    // Custome Design
       val calenderbuilder = AlertDialog.Builder(requireContext())
       val add_bankview: View =
           LayoutInflater.from(requireContext()).inflate(R.layout.custome_calender, null)
       calenderbuilder.setView(add_bankview)
       val calenderalertDialog: AlertDialog = calenderbuilder.create()

       val calender_close = add_bankview.findViewById<LinearLayout>(R.id.ll_calender_close)
       val calender_submit = add_bankview.findViewById<LinearLayout>(R.id.ll_calender_submit)
       val cv__appoinment = add_bankview.findViewById<CalendarView>(R.id.cv__appoinment)
       calenderalertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
       calender_close.setOnClickListener {
           calenderalertDialog.dismiss()
       }
       cv__appoinment.setOnDateChangeListener { calView: CalendarView, year: Int, month: Int, dayOfMonth: Int ->

           val calender: Calendar = Calendar.getInstance()
           calender.set(year, month, dayOfMonth)
           calView.setDate(calender.timeInMillis, true, true)
           Log.d("SelectedDate", "$dayOfMonth/${month + 1}/$year")
       }
       calender_submit.setOnClickListener {

           val dateMillis: Long = cv__appoinment.date
           val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
           val instant = Instant.ofEpochMilli(dateMillis)

           val date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
           println(formatter.format(date)) // 10/12/2019
           txt_booking_date.text = formatter.format(date).toString()
           //Toast.makeText(context, "Selected Date ="+formatter.format(date), Toast.LENGTH_SHORT).show()

           calenderalertDialog.dismiss()
       }


       calenderalertDialog.show()*/


    }

    fun showDatePickerM3( textView: TextView) {


        val constraintsBuilder = CalendarConstraints.Builder()

// Validator to enable only dates from the 1st to the 21st
        val dateValidator = object : CalendarConstraints.DateValidator {
            override fun isValid(date: Long): Boolean {
                val calendar = Calendar.getInstance().apply { timeInMillis = date }
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                return dayOfMonth in 1..21
            }

            override fun describeContents(): Int {
                return 0
            }

            override fun writeToParcel(p0: Parcel, p1: Int) {
                TODO("Not yet implemented")
            }
        }

        // Combine validators to disable past dates and enable 1st to 21st
        /*val combinedValidator = CalendarConstraints.DateValidator.allOf(
            listOf(DateValidatorPointForward.now(), dateValidator)
        )*/

        val combinedValidator = CompositeDateValidator.allOf(
            listOf(DateValidatorPointForward.now(), dateValidator)
        )

        constraintsBuilder.setValidator(combinedValidator)

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .setTheme(R.style.Widget_AppTheme_MaterialDatePicker)
            .setCalendarConstraints(constraintsBuilder.build())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            // Format the date and set it to the TextView
            val selectedDate = Calendar.getInstance().apply { timeInMillis = selection }
            val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(selectedDate.time)
            textView.text = formattedDate
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")

    }


    private fun callLogout() {
        Loader.showLoader(requireContext())
        if (NetworkUtils.isConnectedToNetwork(requireContext())) {

            prefs.lastActivityDashboard = true
            dashboardViewModel.logout()

        } else {
            Loader.hideLoader()
            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
        }

    }


    private fun ondashboardDetailsSuccess(response: ResponseDashboardDetails) {
        Loader.hideLoader()

        /*     Log.d("DashboardDetails", "onDashboardDetailSuccess: " + response)*/
        Toast.makeText(context, response.detail?.message, Toast.LENGTH_SHORT).show()

        /*  ll_dashboard_banklist.visibility = View.VISIBLE*/
        UserDashboardDetails = response.detail!!

        RetriveDashboardDetailsSetFields()

    }

    private fun RetriveDashboardDetailsSetFields() {


        /*     if (!UserDashboardDetails.fullName.isNullOrEmpty()) {*/

        Log.d("dataDashRetrive", "${UserDashboardDetails.fullName}")

        /*    fullName
            profilePic
            walletBalanceCurrency
            walletBalanceAmount*/
        imgurl = UserDashboardDetails.profilePic
        Glide.with(this)
            .load(imgurl)
            .into(img_profile)
        /* img_profile.setImageResource(UserDashboardDetails.profilePic.toString())*/
        tv_person_name.setText(UserDashboardDetails.fullName)

        /* UserDashboardDetails.walletBalanceAmount?.let { tv_wallet_amount.setText(it) }*/


        tv_wallet_amount_digits.setText(UserDashboardDetails.walletBalanceAmount.toString())
        tv_wallet_symbol_sign.setText(UserDashboardDetails.walletBalanceCurrency)

        //New Edit Of Bank Details Show

        imgbank = UserDashboardDetails.bankDetail?.bankImage
        if (!imgbank.isNullOrEmpty()){

            no_bank_msg.visibility = View.GONE

            //img_bank_icon
            Glide.with(this)
                .load(imgbank)
                .into(img_bank_icon)

            tv_bankname.setText(UserDashboardDetails.bankDetail?.bankName)
            tv_banktype.setText(UserDashboardDetails.bankDetail?.accountType)
            cl_dashboard_bank.visibility = View.VISIBLE
        }





        /*  }*/
    }

    private fun ondashboardLogoutSuccess(response: ResponseLogout) {
        Loader.hideLoader()
        Toast.makeText(
            context,
            response.logout_detail?.message,
            Toast.LENGTH_LONG
        ).show()
        prefs.lastActivityDashboard = true
        clearLogin()
        val intent = Intent(context, SignUpActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun onTokenRefreshFailure(error:String) {
        Loader.hideLoader()

        Toast.makeText(
            context,
            error,
            Toast.LENGTH_LONG
        ).show()

        clearLogin()
        val intent = Intent(context, SignUpActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun addBankDialog() {
        val add_bankbuilder = AlertDialog.Builder(requireContext())
        val add_bankview: View =
            LayoutInflater.from(requireContext()).inflate(R.layout.card_add_bank, null)
        add_bankbuilder.setView(add_bankview)
        val add_bankalertDialog: AlertDialog = add_bankbuilder.create()

        val addbank_close = add_bankview.findViewById<LinearLayout>(R.id.ll_addbank_close)
        val addbank_submit = add_bankview.findViewById<LinearLayout>(R.id.ll_addbank_submit)
        add_bankalertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addbank_close.setOnClickListener {
            add_bankalertDialog.dismiss()
        }
        addbank_submit.setOnClickListener {
            add_bankalertDialog.dismiss()
        }
        add_bankalertDialog.show()
    }


}


/*
class DashboardFragment : Fragment(), LogoutConfirmDialog.LogoutClick,DashboardCallBack,TokenRefreshCallBack {

    lateinit var datePickerDialog: DatePickerDialog
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var tokenRefreshViewModel: TokenRefreshViewModel

    var year = 0
    var month = 0
    var dayOfMonth = 0
    lateinit var calendar: Calendar

    val sdf = SimpleDateFormat("yyyy-MM-dd")
    val currentDate = sdf.format(Date())
    val prefs = SharedPref
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dashboardViewModel = DashboardViewModel(this)
      /*  dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)*/
        /*tokenRefreshViewModel = ViewModelProvider(this).get(TokenRefreshViewModel::class.java)*/
        tokenRefreshViewModel = TokenRefreshViewModel(this)
        Log.d("Token", "onViewCreated: access- " + prefs.access_token)
        Log.d("Token", "onViewCreated: refresh- " + prefs.refresh_token)
        onClicked()
     /*   onobserveIn()*/

        tv_person_name.text = Editable.Factory.getInstance().newEditable(
            prefs.first_name + if (prefs.middle_name != "") {
                " " + prefs.middle_name + " "
            } else {
                " "

            } + prefs.last_name
        )
       }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun onClicked() {
        tv_profile_.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_profile)
        }
        img_add_amount.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_wallet)
        }
        ll_account.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_account)
        }

        ll_add_bank.setOnClickListener {
            addBankDialog()
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
            LogoutConfirmDialog.showDialog(requireContext(), this)
        }
    }

    private fun clearLogin() {
        prefs.isLogin = false
        prefs.user_id = ""
        prefs.user_name = ""
        prefs.email = ""
        prefs.access_token = ""
        prefs.refresh_token = ""
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun onDateSelect() {

        calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH)
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
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
        // datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis())
        datePickerDialog.show()

        /*    // Custome Design
       val calenderbuilder = AlertDialog.Builder(requireContext())
       val add_bankview: View =
           LayoutInflater.from(requireContext()).inflate(R.layout.custome_calender, null)
       calenderbuilder.setView(add_bankview)
       val calenderalertDialog: AlertDialog = calenderbuilder.create()

       val calender_close = add_bankview.findViewById<LinearLayout>(R.id.ll_calender_close)
       val calender_submit = add_bankview.findViewById<LinearLayout>(R.id.ll_calender_submit)
       val cv__appoinment = add_bankview.findViewById<CalendarView>(R.id.cv__appoinment)
       calenderalertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
       calender_close.setOnClickListener {
           calenderalertDialog.dismiss()
       }
       cv__appoinment.setOnDateChangeListener { calView: CalendarView, year: Int, month: Int, dayOfMonth: Int ->

           val calender: Calendar = Calendar.getInstance()
           calender.set(year, month, dayOfMonth)
           calView.setDate(calender.timeInMillis, true, true)
           Log.d("SelectedDate", "$dayOfMonth/${month + 1}/$year")
       }
       calender_submit.setOnClickListener {

           val dateMillis: Long = cv__appoinment.date
           val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
           val instant = Instant.ofEpochMilli(dateMillis)

           val date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
           println(formatter.format(date)) // 10/12/2019
           txt_booking_date.text = formatter.format(date).toString()
           //Toast.makeText(context, "Selected Date ="+formatter.format(date), Toast.LENGTH_SHORT).show()

           calenderalertDialog.dismiss()
       }


       calenderalertDialog.show()*/


    }


    override fun proceedLogout() {
         /*clearLogin()
         val intent = Intent(context, SignUpActivity::class.java)
         intent.flags =
             Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
         startActivity(intent)*/

        callLogout()

    }

    private fun callLogout() {
        Loader.showLoader(requireContext())
        if (context?.isConnectedToNetwork()!!) {
            dashboardViewModel.getLogout()

        } else {
            Loader.hideLoader()
            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
        }

    }

    fun Context.isConnectedToNetwork(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting() ?: false
    }

    override fun onTokenRefreshSuccess(response: ResponseRefreshToken) {
        Loader.hideLoader()
        Toast.makeText(context, "Please wait.....", Toast.LENGTH_SHORT).show()
        //again logout api call
        callLogout()
    }

    override fun onTokenRefreshFailure(response: ResponseRefreshToken) {
        Loader.hideLoader()

        Toast.makeText(
            context,
            response.token_detail?.message,
            Toast.LENGTH_LONG
        ).show()

        clearLogin()
        val intent = Intent(context, SignUpActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun ondashboardLogoutSuccess(response: ResponseLogout) {
        Loader.hideLoader()
        Toast.makeText(
            context,
            response.logout_detail?.message,
            Toast.LENGTH_LONG
        ).show()

        clearLogin()
        val intent = Intent(context, SignUpActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun ondashboardLogoutFailure(response: ResponseLogout) {
        if (!response.logout_detail?.tokenStatus.isNullOrEmpty()) {
            if (response.logout_detail?.tokenStatus.equals("expired")) {
                Toast.makeText(context, "Please wait.....", Toast.LENGTH_SHORT).show()

                //refresh api call
                tokenRefreshViewModel.getTokenRefresh()
            } else {
                Loader.hideLoader()
                Toast.makeText(
                    context,
                    response.logout_detail?.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Loader.hideLoader()
            Toast.makeText(
                context,
                response.logout_detail?.message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun addBankDialog() {
        val add_bankbuilder = AlertDialog.Builder(requireContext())
        val add_bankview: View =
            LayoutInflater.from(requireContext()).inflate(R.layout.card_add_bank, null)
        add_bankbuilder.setView(add_bankview)
        val add_bankalertDialog: AlertDialog = add_bankbuilder.create()

        val addbank_close = add_bankview.findViewById<LinearLayout>(R.id.ll_addbank_close)
        val addbank_submit = add_bankview.findViewById<LinearLayout>(R.id.ll_addbank_submit)
        add_bankalertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addbank_close.setOnClickListener {
            add_bankalertDialog.dismiss()
        }
        addbank_submit.setOnClickListener {
            add_bankalertDialog.dismiss()
        }
        add_bankalertDialog.show()
    }


}*/

/*
private fun onobserveIn() {

    dashboardViewModel.LogoutStatus.observe(
        viewLifecycleOwner,
        androidx.lifecycle.Observer { logoutresponse ->

            if (logoutresponse.logout_detail?.status.equals("success")) {
                Loader.hideLoader()
                Toast.makeText(
                    context,
                    logoutresponse.logout_detail?.message,
                    Toast.LENGTH_LONG
                ).show()

                clearLogin()
                val intent = Intent(context, SignUpActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

            } else {
                if (!logoutresponse.logout_detail?.tokenStatus.isNullOrEmpty()) {
                    if (logoutresponse.logout_detail?.tokenStatus.equals("expired")) {
                        Toast.makeText(context, "Please wait.....", Toast.LENGTH_SHORT).show()

                        //refresh api call
                        tokenRefreshViewModel.getTokenRefresh()
                    } else {
                        Loader.hideLoader()
                        Toast.makeText(
                            context,
                            logoutresponse.logout_detail?.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Loader.hideLoader()
                    Toast.makeText(
                        context,
                        logoutresponse.logout_detail?.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })


    */
/*tokenRefreshViewModel.TokenrefreshStatus.observe(
        viewLifecycleOwner,
        androidx.lifecycle.Observer { refreshdata ->
            Loader.hideLoader()
            if (refreshdata.token_detail?.status.equals("success")) {
                Toast.makeText(context, "Please wait.....", Toast.LENGTH_SHORT).show()
                //again logout api call
                callLogout()
            } else {
                Toast.makeText(
                    context,
                    refreshdata.token_detail?.message,
                    Toast.LENGTH_LONG
                ).show()

                clearLogin()
                val intent = Intent(context, SignUpActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        })*//*
}*/
