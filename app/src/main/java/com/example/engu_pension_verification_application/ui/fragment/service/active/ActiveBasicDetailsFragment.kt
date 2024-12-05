package com.example.engu_pension_verification_application.ui.fragment.service.active

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isEmpty
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.util.AlphabeticTextWatcher
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.databinding.FragmentActiveBasicDetailsBinding
import com.example.engu_pension_verification_application.model.input.InputActiveBasicDetails
import com.example.engu_pension_verification_application.model.response.ActiveRetriveUserProfileDetails
import com.example.engu_pension_verification_application.model.response.GradeLevelsItem
import com.example.engu_pension_verification_application.model.response.LgasItem
import com.example.engu_pension_verification_application.model.response.OccupationsItem
import com.example.engu_pension_verification_application.model.response.ResponseActiveBasicDetails
import com.example.engu_pension_verification_application.model.response.ResponseCombinationDetails
import com.example.engu_pension_verification_application.model.response.SubTreasuryItem
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.adapter.GradeLevelAdapter
import com.example.engu_pension_verification_application.ui.adapter.LGASpinnerAdapter
import com.example.engu_pension_verification_application.ui.adapter.OccupationsAdapter
import com.example.engu_pension_verification_application.ui.adapter.SubTreasuryAdapter
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.NetworkUtils
import com.example.engu_pension_verification_application.util.OnboardingStage
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.ActiveBasicDetailViewModel
import com.example.engu_pension_verification_application.viewmodel.ActiveServiceViewModel
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.collections.ArrayList

    fun String?.isValidOptionalEmail(): Boolean {
    // Check if the string is null or empty
    if (this.isNullOrEmpty()) {
        return true // Accepts empty string or null
    }
    // Check if the string is in valid email format
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

class ActiveBasicDetailsFragment : BaseFragment()
{
    private lateinit var binding:FragmentActiveBasicDetailsBinding
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2

    companion object {
        const val TAB_POSITION = 0
    }

    // previous name pattern ^[a-zA-Z\s]+$
    val NAME_PATTERN = Pattern.compile("^[a-zA-Z]+(?:\\s[a-zA-Z]+)*$")
    val NAME_PATTERN_OR_NULL = Pattern.compile("^$|^[a-zA-Z]+(?:\\s[a-zA-Z]+)?$")

    val EMAIL_ADDRESS_PATTERN = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+"
    )

    //for spinner selection from retrive
    var lgaS = ""
    var subS = ""
    var gradelvlS = ""
    var occupationS = ""


    var Ph_no = ""
    var sex = ""
    var selected_country = ""

   // var doa = ""
    //var dob = ""
    private var dateBirth = StringBuilder()
    private var dateAppointment = StringBuilder()

    val prefs = SharedPref

    private lateinit var activeBasicDetailViewModel: ActiveBasicDetailViewModel
    private val activeServiceViewModel by activityViewModels<ActiveServiceViewModel>()

    //var selected_country: String? = null


    var occupation = ""
    var occuOther = ""
    var gradelevel: Int? = 0
    var subtreasury: Int? = 0
    var lgalist: Int? = 0

    //active retrive user profile data store
    lateinit var ActiveUserRetrive: ActiveRetriveUserProfileDetails

    val LGAList = ArrayList<LgasItem?>()
    val subtreasuryList = ArrayList<SubTreasuryItem?>()
    val GradeLevelsList = ArrayList<GradeLevelsItem?>()
    val occupationsList = ArrayList<OccupationsItem?>()

    lateinit var lgaSpinnerAdapter: LGASpinnerAdapter
    lateinit var subTreasuryAdapter: SubTreasuryAdapter
    lateinit var gradeLevelAdapter: GradeLevelAdapter
    lateinit var occupationsAdapter: OccupationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActiveBasicDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        initViews()
        observeLiveData()
    }

    private fun initViewModels() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        activeBasicDetailViewModel = ViewModelProviders.of(
            this,
            EnguViewModelFactory(networkRepo)
        ).get(ActiveBasicDetailViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), 
            EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }
    private fun observeLiveData() {
        activeServiceViewModel.currentTabPos.observe(viewLifecycleOwner){
            if (it == TAB_POSITION) initcall()
        }
        activeBasicDetailViewModel.combinedDetailsApiResult.observe(viewLifecycleOwner) { response ->
            dismissLoader()
            if (response.combinedetails?.status == AppConstants.SUCCESS) {
                onAcombinedDetailSuccess(response)
            } else {
                showFetchErrorDialog(
                    ::initcall,
                    response.combinedetails?.message ?: getString(R.string.common_error_msg_2)
                )
            }
        }
        activeBasicDetailViewModel.basicDetailsApiResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                response.detail.userProfileDetails?.let {
                    ActiveUserRetrive = it
                    populateViews()
                }
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            activeBasicDetailViewModel.fetchActiveBasicDetails()
                        }
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        activeBasicDetailViewModel.basicDetailsSubmissionResult.observe(viewLifecycleOwner) { pair ->
            if (pair != null) {
                dismissLoader()
                val request = pair.first
                val response = pair.second
                if (response.detail?.status == AppConstants.SUCCESS) {
                    onActiveBasicDetailSuccess(response)
                } else {
                    if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (tokenRefreshViewModel2.fetchRefreshToken()) {
                                activeBasicDetailViewModel.submitActiveBasicDetails(request)
                            }
                        }
                    } else {
                        Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                    }
                }
                activeBasicDetailViewModel.resetBasicDetailsSubmissionResult()
            }
        }
    }
    private fun initViews() {
        lgaSpinnerAdapter = LGASpinnerAdapter(context, LGAList)
        binding.spActiveLga.adapter = lgaSpinnerAdapter

        occupationsAdapter = OccupationsAdapter(context, occupationsList)
        binding.spActiveOccupationType.adapter = occupationsAdapter

        gradeLevelAdapter = GradeLevelAdapter(context, GradeLevelsList)
        binding.spActiveLastGrade.adapter = gradeLevelAdapter

        subTreasuryAdapter = SubTreasuryAdapter(context, subtreasuryList)
        binding.spActiveSubTreasury.adapter = subTreasuryAdapter


        /*activeBasicDetailViewModel =
            ViewModelProvider(this).get(ActiveBasicDetailViewModel::class.java)*/

        /*tokenRefreshViewModel = ViewModelProvider(this).get(TokenRefreshViewModel::class.java)*/

        //kinphone
        binding.activeNextKinPhoneCcp.registerPhoneNumberTextView(binding.etActiveNextKinPhone)

        selected_country = binding.ccpActivedetails.selectedCountryName

        // Example usage to enable only the first tab
        //tabAccessControl.enableDisableTabs(true, false, false)

        onSpinnerTextWatcher()

        //ActiveRetriveApiCall()

        onClicked()

        binding.ccpActivedetails.setOnCountryChangeListener {
            selected_country = binding.ccpActivedetails.selectedCountryName
            Log.d("changed_country", "onViewCreated: " + binding.ccpActivedetails.selectedCountryName)
            showLoader()
            if (NetworkUtils.isConnectedToNetwork(requireContext())) {
                lifecycleScope.launch(Dispatchers.IO) {
                    activeBasicDetailViewModel.fetchCombinedDetails(selected_country)
                }
            } else {
                dismissLoader()
                Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun populateViews() {
        if (!ActiveUserRetrive.firstName.isNullOrEmpty()) {
            if (prefs.onboardingStage == OnboardingStage.ACTIVE_BASIC_DETAILS) {
                prefs.onboardingStage = OnboardingStage.ACTIVE_DOCUMENTS
                activeServiceViewModel.refreshTabsState()
            }
            binding.etActiveFirstName.setText(ActiveUserRetrive.firstName)
            binding.etActiveMiddleName.setText(ActiveUserRetrive.middleName)
            binding.etActiveLastName.setText(ActiveUserRetrive.lastName)

            binding.etActiveDOB.text = ActiveUserRetrive.dob.toString()

            var RetriveSex = ActiveUserRetrive.sex

            when (RetriveSex) {

                "male" -> {
                    binding.radioGroupActive.check(R.id.rb_active_male)
                    sex = "male"
                }

                "female" -> {
                    binding.radioGroupActive.check(R.id.rb_active_female)
                    sex = "female"
                }
            }


            binding.etActiveAddress.setText(ActiveUserRetrive.address)

            binding.etActivePincode.setText(ActiveUserRetrive.pincode)

            binding.etActiveNextKin.setText(ActiveUserRetrive.nextOfKinName)

            binding.etActiveNextKinEmail.setText(ActiveUserRetrive.nextOfKinEmail)

            //backend format indian number +917917854563 thats why takelast 10 digits
            val RetrivePhn = ActiveUserRetrive.nextOfKinPhoneNumber

            var WithoutCC = RetrivePhn!!.takeLast(10)

            var RetriveCC = RetrivePhn.substring(1 until RetrivePhn.length - WithoutCC.length)

            var RetriveCCInt = RetriveCC.toInt()

            binding.etActiveNextKinPhone.setText(WithoutCC)

            binding.activeNextKinPhoneCcp.setCountryForPhoneCode(RetriveCCInt)


            binding.etActiveNextKinAddress.setText(ActiveUserRetrive.nextOfKinAddress)

            binding.etActiveKinPincode.setText(ActiveUserRetrive.kinPincode)

            binding.etActiveDateAppointment.text = ActiveUserRetrive.dateOfAppointment.toString()



            //spinnerSet from Retrive ActiveUserRetrive


            //Log.d("spinner", "spinnerListLGAList $LGAList ")


            //lga
            /*lgaSpinnerAdapter = LGASpinnerAdapter(context, LGAList)
            binding.spActiveLga.adapter = lgaSpinnerAdapter*/

            lgaS = ActiveUserRetrive.lga.toString()

            Log.d("spinner", "spinnerLgas $lgaS ")
            if (lgaS.isNotEmpty()) {
                var pos = lgaSpinnerAdapter.getPositionByName(lgaS)

                binding.spActiveLga.setSelection(pos)

                Log.d("spinner", "spinnerPos $pos ")
            }

            //sub tresury
            subS = ActiveUserRetrive.subTreasury.toString()
            if (!subS.isNullOrEmpty()) {

                var pos = subTreasuryAdapter.getPositionByName(subS)

                binding.spActiveSubTreasury.setSelection(pos)
            }

            //gradelvl
            gradelvlS = ActiveUserRetrive.gradeLevel.toString()
            if (!gradelvlS.isNullOrEmpty()) {

                var pos = gradeLevelAdapter.getPositionByName(gradelvlS)

                binding.spActiveLastGrade.setSelection(pos)
            }

            //ocupations
            occupationS = ActiveUserRetrive.occupation.toString()
            if (!occupationS.isNullOrEmpty()) {

                var pos = occupationsAdapter.getPositionByName(occupationS)
                if (pos != -1) {

                    Log.d("LogOccu", "spinnerOccuPos: $pos")
                    binding.spActiveOccupationType.setSelection(pos)
                } else {
                    //occupation other case
                    binding.spActiveOccupationType.setSelection(occupationsAdapter.count -1)
                    binding.etActiveOccupationOther.visibility = View.VISIBLE
                    binding.etActiveOccupationOther.setText(occupationS)
                    occupation = occupationS

                    // below code will prevent the EditText from gaining focus
                    /*binding.etActiveOccupationOther.inputType = InputType.TYPE_NULL
                    binding.etActiveOccupationOther.isFocusable = false
                    binding.etActiveOccupationOther.isFocusableInTouchMode = false*/

                }


                /*occupationsList.forEachIndexed { index, element ->
                    if (occupationS.equals(element)) {
                        val updateIndex = index
                        Log.d("LogOccu", "selectedIndex: " + updateIndex)
                        binding.spActiveOccupationType.setSelection(updateIndex)

                    } else {

                        binding.etActiveOccupationOther.visibility = View.VISIBLE
                        binding.etActiveOccupationOther.setText(occupationS)
                    }
                }*/

            }

            onSpinnerTextWatcher()

            //enableDisableTabs(tab_tablayout_activeservice, true, true, false)



        }else{
            //tabAccessControl.enableDisableTabs(true, false, false)
        }

        /*binding.etActiveLastName.text = Editable.Factory.getInstance().newEditable(prefs.last_name)
        binding.etActiveFirstName.text = Editable.Factory.getInstance().newEditable(prefs.first_name)
        binding.etActiveMiddleName.text = Editable.Factory.getInstance().newEditable(prefs.middle_name)
        binding.spActiveLga.setSelection(prefs.lga!!.toInt())

        binding.etActiveLastName.text = Editable.Factory.getInstance().newEditable(prefs.last_name)
        binding.etActiveDOB.text = Editable.Factory.getInstance().newEditable(prefs.dob)
        binding.etActiveAddress.text = Editable.Factory.getInstance().newEditable(prefs.address)
        binding.etActiveDateAppointment.text = Editable.Factory.getInstance().newEditable(prefs.doa)

        binding.etActiveDOB.text = Editable.Factory.getInstance().newEditable(prefs.dob)
        binding.etActiveAddress.text = Editable.Factory.getInstance().newEditable(prefs.address)
        binding.etActiveNextKin.text = Editable.Factory.getInstance().newEditable(prefs.kin_name)
        binding.etActiveNextKinAddress.text =
            Editable.Factory.getInstance().newEditable(prefs.kin_address)
        binding.etActiveNextKinEmail.text = Editable.Factory.getInstance().newEditable(prefs.kin_email)
        binding.etActiveNextKinPhone.text = Editable.Factory.getInstance().newEditable(prefs.kin_phone)
        binding.etActiveDateAppointment.text = Editable.Factory.getInstance().newEditable(prefs.doa)

        binding.etActiveOccupationOther.text =
            Editable.Factory.getInstance().newEditable(prefs.a_Occupation)
        if (prefs.sex != "") {
            if (prefs.sex!!.toInt() != -1) {
                binding.radioGroupActive.check(prefs.sex!!.toInt())
            }
        }*/
    }

    private fun onSpinnerTextWatcher() {

        Log.d("LogLGA", "LGAList: onSpinnerTextWatcher() $LGAList")
        onTextLgaWatcher()
        onTextSubTresuryWatcher()
        onTextGradeLevelWatcher()
        onTextOccupationWatcher()
    }

    private fun onTextOccupationWatcher() {
        binding.spActiveOccupationType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {

                    if (occupationsList.get(position)?.id?.equals(0) == true) {

                        // Toast.makeText(context, "select occupation item", Toast.LENGTH_SHORT).show()
                    } else if (occupationsList.get(position)?.id?.equals(-1) == true) {

                        // prefs.Occupation = binding.spActiveOccupationType.selectedItemPosition.toString()
                        //Toast.makeText(context, "others", Toast.LENGTH_SHORT).show()
                        //visiblty show
                        binding.etActiveOccupationOther.visibility = View.VISIBLE

                        //for check
                        //occupation = binding.etActiveOccupationOther.text.toString()

                       /* binding.etActiveOccupationOther.setOnFocusChangeListener { view, hasFocus ->
                            if (!hasFocus) {
                                // User has moved the focus away from the EditText
                                // Implement your logic here
                                //Toast.makeText(view.context, "Focus lost", Toast.LENGTH_SHORT).show()
                            }
                        }*/


                        binding.etActiveOccupationOther.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                                // Code to handle text before changes are made
                            }

                            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                                // Code to handle text changes
                            }

                            override fun afterTextChanged(s: Editable) {
                                // Code to handle text after changes are made

                                occupation = binding.etActiveOccupationOther.text.toString()
                            }
                        })

                        /*
                                                binding.etActiveOccupationOther.text.clear()
                                                //binding.etActiveOccupationOther.inputType = InputType.TYPE_CLASS_TEXT
                                              binding.etActiveOccupationOther.isFocusable = true
                                                binding.etActiveOccupationOther.isFocusableInTouchMode = true*/
                    } else {
                        binding.etActiveOccupationOther.visibility = View.GONE
                        occupation = occupationsList[position]?.id.toString()

                        //occuOther = occupationsList[position]?.name.toString()


                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

            }
    }

    private fun onTextGradeLevelWatcher() {
        binding.spActiveLastGrade.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (GradeLevelsList.get(position)?.id?.equals(0) == true) {

                } else {
                    gradelevel = GradeLevelsList[position]?.id

                    //prefs.grade = binding.spActiveLastGrade.selectedItemPosition.toString()


                }
            }


            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
    }

    private fun onTextSubTresuryWatcher() {
        binding.spActiveSubTreasury.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    if (subtreasuryList.get(position)?.id?.equals(0) == true) {

                    } else {
                        subtreasury = subtreasuryList[position]?.id
                        // prefs.sub = binding.spActiveSubTreasury.selectedItemPosition.toString()
                    }
                }


                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

            }
    }

    private fun onTextLgaWatcher() {
        binding.spActiveLga.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (LGAList.get(position)?.id?.equals(0) == true) {


                } else {
                    lgalist = LGAList[position]?.id

                    //lga pref store
                    //prefs.lga = binding.spActiveLga.selectedItemPosition.toString()
                }
            }


            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
    }

    private fun onClicked() {

        binding.etActiveFirstName.addTextChangedListener(AlphabeticTextWatcher(binding.etActiveFirstName))
        binding.etActiveMiddleName.addTextChangedListener(AlphabeticTextWatcher(binding.etActiveMiddleName))
        binding.etActiveLastName.addTextChangedListener(AlphabeticTextWatcher(binding.etActiveLastName))
        binding.etActiveNextKin.addTextChangedListener(AlphabeticTextWatcher(binding.etActiveNextKin))


        binding.radioGroupActive.setOnCheckedChangeListener { group, checkedId ->
            //sex = "You selected: " + if (R.id.rb_active_male == checkedId) "male" else "female"
            sex = if (R.id.rb_active_male == checkedId) "male" else "female"
        }




        binding.etActiveDOB.setOnClickListener {
            showDatePickerPresentToPast(binding.etActiveDOB, dateBirth)

            //dob = dateBirth.toString()

            /*try {

                val today = MaterialDatePicker.todayInUtcMilliseconds()
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = today
                calendar[Calendar.YEAR] = 1950
                val startDate = calendar.timeInMillis

                calendar.timeInMillis = today
                calendar[Calendar.YEAR] = 2003
                val endDate = calendar.timeInMillis

                val constraints: CalendarConstraints = CalendarConstraints.Builder()
                    .setOpenAt(endDate)
                    .setStart(startDate)
                    .setEnd(endDate)
                    .build()

                val datePickerBuilder: MaterialDatePicker.Builder<Long> = MaterialDatePicker
                    .Builder
                    .datePicker()
                    .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                //.setTitleText("Select Business Start Date")
                // .setTheme(R.style.MaterialCalendarTheme)
                //.setCalendarConstraints(constraints)
                val datePicker = datePickerBuilder.build()
                datePicker.show(requireActivity().supportFragmentManager, "DATE_PICKER")


                datePicker.addOnPositiveButtonClickListener {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = sdf.format(it)
                    binding.etActiveDOB.text = date
                }
            }catch (e:java.lang.Exception){
                Log.d("Exception", "onClicks: "+e.localizedMessage)
            }*/

        }

        binding.etActiveDateAppointment.setOnClickListener {
            showDatePickerPresentToPast(binding.etActiveDateAppointment, dateAppointment)
            //doa = dateAppointment.toString()
        }



        binding.llActivebasicdetailsNext.setOnClickListener {
            //nextButtonCall()
            if (isValidActiveBasicDetails()) {
                nextButtonCall()
            }
        }
    }

    /*private fun showDatePickerr(textView: TextView) {
        calendar = Calendar.getInstance()
        dob_year = calendar.get(Calendar.YEAR)
        dob_month = calendar.get(Calendar.MONTH)
        dob_dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        datePickerDialog = DatePickerDialog(
            requireContext(),
            { view, selectedYear, selectedMonth, selectedDayOfMonth ->
                var doDate =
                    String.format("%02d/%02d/%d", selectedDayOfMonth, selectedMonth, selectedYear)
                //binding.etActiveDOB.text = doDate
                textView.text = doDate
            }, dob_year, dob_month, dob_dayOfMonth
        )
        datePickerDialog.show()


    }*/

    private fun showDatePicker(textView: TextView, dateBuilder: StringBuilder) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                dateBuilder.apply {
                    setLength(0)
                    append(
                        String.format(
                            "%02d/%02d/%d", selectedDayOfMonth, selectedMonth + 1, selectedYear
                        )
                    )
                }
                textView.text = dateBuilder.toString()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )



        datePickerDialog.show()
    }


    private fun showDatePickerDOB(textView: TextView, dateBuilder: StringBuilder) {
        val calendar = Calendar.getInstance()

        // Subtract 18 years from the current date to set the minimum date
        calendar.add(Calendar.YEAR, -18)
        val eighteenYearsAgo = calendar.timeInMillis

        // Set the DatePickerDialog to show 18 years back dates when opened
        val datePickerDialog = DatePickerDialog(
            requireContext(), // context
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                // Handle the date selected by the user
                dateBuilder.apply {
                    setLength(0)
                    append(
                        String.format(
                            "%02d/%02d/%d", selectedDayOfMonth, selectedMonth + 1, selectedYear
                        )
                    )
                }
                textView.text = dateBuilder.toString()
            },
            calendar.get(Calendar.YEAR), // Set to 18 years ago
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set the maximum date to 18 years back to disable future dates
        datePickerDialog.datePicker.maxDate = eighteenYearsAgo

        // Set the minimum date to 18 years ago from today
        //datePickerDialog.datePicker.minDate = eighteenYearsAgo

        datePickerDialog.show()
    }

    private fun showDatePickerPresentToPast(textView: TextView, dateBuilder: StringBuilder) {
        val calendar = Calendar.getInstance()

// Get the current date
        val currentDate = calendar.timeInMillis

// Set the DatePickerDialog to show current and past dates when opened
        val datePickerDialog = DatePickerDialog(
            requireContext(), // context
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
// Handle the date selected by the user
                dateBuilder.apply {
                    setLength(0)
                    append(
                        String.format(
                            "%02d/%02d/%d", selectedDayOfMonth, selectedMonth + 1, selectedYear
                        )
                    )
                }


                textView.text = dateBuilder.toString()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

// Set the maximum date to the current date to disable future dates
        datePickerDialog.datePicker.maxDate = currentDate

// The minimum date is not set, allowing all past dates

        datePickerDialog.show()
    }
    private fun showDatePickerPresentToPastDOB(textView: TextView, dateBuilder: StringBuilder) {
        val calendar = Calendar.getInstance()

        // Get the current date
        val currentDate = calendar.timeInMillis

        // Set the DatePickerDialog to show current and past dates when opened
        val datePickerDialog = DatePickerDialog(
            requireContext(), // context
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                // Calculate the age
                val selectedDateInMillis = calendar.timeInMillis
                val ageInMillis = currentDate - selectedDateInMillis
                val ageInYears = TimeUnit.MILLISECONDS.toDays(ageInMillis) / 365
                if (ageInYears < 18) {
                // Display a toast message if age is less than 18
                    Toast.makeText(
                        requireContext(),
                        "You must be at least 18 years old.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                // Handle the date selected by the user
                    dateBuilder.apply {
                        setLength(0)
                        append(
                            String.format(
                                "%02d/%02d/%d", selectedDayOfMonth, selectedMonth + 1, selectedYear
                            )
                        )
                    }
                    textView.text = dateBuilder.toString()
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

// Set the maximum date to the current date to disable future dates
        datePickerDialog.datePicker.maxDate = currentDate

// The minimum date is not set, allowing all past dates
        datePickerDialog.show()
    }





    private fun GradeLevelspinnerfun() {
        gradeLevelAdapter.changeList(GradeLevelsList)
        //binding.spActiveLastGrade.setSelection(prefs.grade!!.toInt())
/*
        gradelvlS = ActiveUserRetrive.gradeLevel.toString()

        if (!gradelvlS.isNullOrEmpty()) {

            val pos = gradeLevelAdapter.getPositionByName(gradelvlS)

            binding.spActiveLastGrade.setSelection(pos)
        }*/


    }


    private fun LGAspinnerfun() {
        lgaSpinnerAdapter.changeList(LGAList)
        // binding.spActiveLga.setSelection(prefs.lga!!.toInt())

        Log.d("LogLGA", "LGAList: LGAspinnerfun() $LGAList")


        /*LGAList.forEachIndexed { index, element ->
            *//*Log.d("business_type", "business_type: " + bd_fundingModel.f_business_type)
            Log.d("element", "element: " + element)
            Log.d("index", "index: " + index)*//*

            var lgaSelect =  ActiveUserRetrive.lga.toString() //response.detail.userProfileDetails?.lga.toString()
            Log.d("retrive", "$lgaSelect")
            if (lgaSelect.equals(element)) {
                val updateIndex = index
                Log.d("Test", "selectedIndex: " + updateIndex)

                binding.spActiveLga.setSelection(updateIndex)
            }
        }*/


        /*lgaS = ActiveUserRetrive.lga.toString()

        Log.d("spinner", "LGAspinner: $lgaS")

        if (!lgaS.isNullOrEmpty()) {

            val pos = lgaSpinnerAdapter.getPositionByName(lgaS)

            Log.d("spinner", "LGAspinner: $pos")

            binding.spActiveLga.setSelection(pos)
        }*/

    }

    private fun SubTreasuryspinnerfun() {
        subTreasuryAdapter.changeList(subtreasuryList)
        //binding.spActiveSubTreasury.setSelection(prefs.sub!!.toInt())

        /*subS = ActiveUserRetrive.subTreasury.toString()


        if (!subS.isNullOrEmpty()) {

            val pos = subTreasuryAdapter.getPositionByName(subS)

            binding.spActiveSubTreasury.setSelection(pos)
        }*/


    }

    private fun Occupationspinnerfun() {
        occupationsAdapter.changeList(occupationsList)
        // binding.spActiveOccupationType.setSelection(prefs.Occupation!!.toInt())
        /*occupationS = ActiveUserRetrive.occupation.toString()

        if (!occupationS.isNullOrEmpty()) {

            val pos = occupationsAdapter.getPositionByName(occupationS)
            if (pos != -1) {
                binding.spActiveOccupationType.setSelection(pos)
            }
            else{
                binding.etActiveOccupationOther.visibility = View.VISIBLE
                binding.etActiveOccupationOther.setText(occupationS)


                *//*binding.etActiveOccupationOther.inputType = InputType.TYPE_NULL
                binding.etActiveOccupationOther.isFocusable = false
                binding.etActiveOccupationOther.isFocusableInTouchMode = false // This will prevent the EditText from gaining focus
                binding.etActiveOccupationOther.setText(occupationS)*//*

            }


           *//* occupationsList.forEachIndexed { index, element ->
                if (occupationS.equals(element)) {
                    val updateIndex = index
                    Log.d("Test", "selectedIndex: " + updateIndex)
                    binding.spActiveOccupationType.setSelection(updateIndex)

                } else {
                    binding.etActiveOccupationOther.visibility = View.VISIBLE
                    binding.etActiveOccupationOther.setText(occupationS)
                }
            }*//*

        }*/

    }


    private fun initcall() {
        showLoader()
        if (NetworkUtils.isConnectedToNetwork(requireContext())) {
            lifecycleScope.launch(Dispatchers.IO) {
                if (activeBasicDetailViewModel.fetchCombinedDetails(selected_country))
                    activeBasicDetailViewModel.fetchActiveBasicDetails()
            }

        } else {
            dismissLoader()
            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
        }

    }




    private fun accountdetailCalll() {

        val doa : String
        val dob : String

        if(dateAppointment.toString() == ""){
            doa = binding.etActiveDateAppointment.text.toString()
        }
        else{
            doa = dateAppointment.toString()
        }

        if (dateBirth.toString() == ""){
            dob = binding.etActiveDOB.text.toString()
        }
        else{
            dob = dateBirth.toString()
        }



        activeBasicDetailViewModel.submitActiveBasicDetails(
            InputActiveBasicDetails(
                pincode = binding.etActivePincode.text.toString(),
                kinPincode = binding.etActiveKinPincode.text.toString(),
                userType = "active",
                country = selected_country,
                address = binding.etActiveAddress.text.toString(),
                subTreasuryId = subtreasury,
                sex = sex,
                lastName = binding.etActiveLastName.text.trim().toString(),
                middleName = binding.etActiveMiddleName.text.trim().toString(),
                firstName = binding.etActiveFirstName.text.trim().toString(),
                dateOfAppointment = doa,
                lgaId = lgalist,
                nextOfKinAddress = binding.etActiveNextKinAddress.text.toString(),
                nextOfKinEmail = binding.etActiveNextKinEmail.text.toString(),
                nextOfKinName = binding.etActiveNextKin.text.trim().toString(),
                nextOfKinPhoneNumber = Ph_no,
                gradeLevel = gradelevel,
                dob = dob,
                occupationIdString = occupation
            )
        )


    }


    private fun nextButtonCall() {
        showLoader()
        if (NetworkUtils.isConnectedToNetwork(requireContext())) {
            prefs.first_name = binding.etActiveFirstName.text.trim().toString()
            prefs.middle_name = binding.etActiveMiddleName.text.trim().toString()
            prefs.last_name = binding.etActiveLastName.text.trim().toString()

            accountdetailCalll()


        } else {
            dismissLoader()
            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
        }

    }

    private fun isValidActiveBasicDetails(): Boolean {
        //firstname
        /*if (TextUtils.isEmpty(binding.etActiveFirstName.text)) {
            Toast.makeText(context, "Empty FirstName", Toast.LENGTH_SHORT).show()
            return false
        } else {

        }
        if (binding.etActiveFirstName.text.toString().contains(" ")) {
            Toast.makeText(context, "Enter  Firstname, No whitespace", Toast.LENGTH_SHORT).show()
            return false
        }*/

        if (TextUtils.isEmpty(binding.etActiveFirstName.text.trim())) {
            Toast.makeText(context, "Empty FirstName", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!NAME_PATTERN.matcher(binding.etActiveFirstName.text.trim()).matches()) {

            Toast.makeText(context, "first name not valid", Toast.LENGTH_SHORT).show()
            return false
        }


        //middlename
        /*if (TextUtils.isEmpty(binding.etActiveMiddleName.text)) {
            Toast.makeText(context, "Empty Middle name", Toast.LENGTH_SHORT).show()
            return false
        }*/

        if (!NAME_PATTERN_OR_NULL.matcher(binding.etActiveMiddleName.text.trim()).matches()) {

            Toast.makeText(context, "middle name not valid", Toast.LENGTH_SHORT).show()
            return false
        }


        //lastname
        if (TextUtils.isEmpty(binding.etActiveLastName.text.trim())) {
            Toast.makeText(context, "Empty Last name", Toast.LENGTH_SHORT).show()
            return false
        }


        if (!NAME_PATTERN.matcher(binding.etActiveLastName.text.trim()).matches()) {

            Toast.makeText(context, "last name not valid", Toast.LENGTH_SHORT).show()
            return false
        }


        //dob
        if (TextUtils.isEmpty(binding.etActiveDOB.text)) {
            Toast.makeText(context, "select dob", Toast.LENGTH_SHORT).show()
            return false
        }

        //sex
        /*if (!(rb_active_male.isChecked || rb_active_female.isChecked)) {

            Toast.makeText(context, "Select gender", Toast.LENGTH_SHORT).show()

        }*/
        if (binding.radioGroupActive.checkedRadioButtonId <= 0) {
            Toast.makeText(context, "Select Gender", Toast.LENGTH_SHORT).show()
            return false
        }/*else {
            binding.radioGroupActive.setOnCheckedChangeListener { group, checkedId ->
                //sex = "You selected: " + if (R.id.rb_active_male == checkedId) "male" else "female"
                sex = if (R.id.rb_active_male == checkedId) "male" else "female"
                Toast.makeText(context, sex, Toast.LENGTH_SHORT).show()
            }
        }*/

        //address
        if (TextUtils.isEmpty(binding.etActiveAddress.text)) {
            Toast.makeText(context, "Empty Address", Toast.LENGTH_SHORT).show()
            return false
        }
        //pincode

        if (TextUtils.isEmpty(binding.etActivePincode.text)) {
            Toast.makeText(context, "Empty Pincode", Toast.LENGTH_SHORT).show()
            return false
        }

        //country spinner default nigeria selected, no condition check

        //LGA
        if (binding.spActiveLga.selectedItemPosition == 0 || (binding.spActiveLga.isEmpty())) {
            Toast.makeText(context, "Select valid lga item", Toast.LENGTH_SHORT).show()
            return false
        }

        //kin name
        if (TextUtils.isEmpty(binding.etActiveNextKin.text.trim())) {
            Toast.makeText(context, "Empty kin name", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!NAME_PATTERN.matcher(binding.etActiveNextKin.text.trim()).matches()) {

            Toast.makeText(context, "kin name not valid", Toast.LENGTH_SHORT).show()
            return false
        }

        //kin email
        if (!binding.etActiveNextKinEmail.text.toString().isValidOptionalEmail()) {
            Toast.makeText(context, "kin email not valid", Toast.LENGTH_SHORT).show()
            return false
        }
        /*if (TextUtils.isEmpty(binding.etActiveNextKinEmail.text)){
                Toast.makeText(context, " Empty kin email address", Toast.LENGTH_SHORT).show()
            }*/
 /*       if (!EMAIL_ADDRESS_PATTERN.matcher(binding.etActiveNextKinEmail.text.toString()).matches()) {

            Toast.makeText(context, " kin email not valid", Toast.LENGTH_SHORT).show()
            return false
        }*/


        //kin phone
        if (TextUtils.isEmpty(binding.etActiveNextKinPhone.text)) {
            Toast.makeText(context, "Empty phone number", Toast.LENGTH_LONG).show()
            return false
        } else if ((!binding.activeNextKinPhoneCcp.isValid)) {
            Toast.makeText(context, "Phone Number not valid", Toast.LENGTH_LONG).show()
            return false
        } else {
            //80655707
            Ph_no = "+" + binding.activeNextKinPhoneCcp.fullNumber
            Log.d("active_phn", "$Ph_no")
        }

        //kin address
        if (TextUtils.isEmpty(binding.etActiveNextKinAddress.text)) {
            Toast.makeText(context, "Empty kin Address", Toast.LENGTH_SHORT).show()
            return false
        }
        //kin pincode
        if (TextUtils.isEmpty(binding.etActiveKinPincode.text)) {
            Toast.makeText(context, "Empty kin Pincode", Toast.LENGTH_SHORT).show()
            return false
        }

        //sub tressury
        if ((binding.spActiveSubTreasury.selectedItemPosition == 0) || (binding.spActiveSubTreasury.isEmpty())) {
            Toast.makeText(context, "Select valid sub treasury item", Toast.LENGTH_SHORT).show()
            return false
        }

        //date of appointment
        if (TextUtils.isEmpty(binding.etActiveDateAppointment.text)) {
            Toast.makeText(context, "select date appointment", Toast.LENGTH_SHORT).show()
            return false
        }

        //last grade
        if ((binding.spActiveLastGrade.selectedItemPosition == 0) || (binding.spActiveLastGrade.isEmpty())) {
            Toast.makeText(context, "select valid grade level item", Toast.LENGTH_SHORT).show()
            return false
        }


        //occupation type
        /*if (binding.spActiveOccupationType.selectedItemPosition == 0|| (occupation.isEmpty())) {
            Toast.makeText(context, "select valid occupation item", Toast.LENGTH_SHORT).show()
            return false
        }*/

        //occupation
        if ((binding.spActiveOccupationType.selectedItemPosition == 0) || (binding.spActiveOccupationType.isEmpty())) {
            Toast.makeText(context, "select valid occupation item", Toast.LENGTH_SHORT).show()
            return false
        }


        //occupation other
        if (binding.etActiveOccupationOther.visibility == View.VISIBLE && ((!NAME_PATTERN.matcher(
                binding.etActiveOccupationOther.text.toString()
            ).matches() || (TextUtils.isEmpty(binding.etActiveOccupationOther.text))))
        ) {
            Toast.makeText(context, "Enter other occupation", Toast.LENGTH_SHORT).show()
            return false
        } /*else if (binding.etActiveOccupationOther.visibility == View.GONE) {
            onTextOccupationWatcher()
        } else {
            occupation = binding.etActiveOccupationOther.text.toString()
        }*/

        return true

    }

    private fun onActiveBasicDetailSuccess(response: ResponseActiveBasicDetails) {

        dismissLoader()

        Toast.makeText(context, response.detail!!.message, Toast.LENGTH_SHORT).show()

        if (prefs.onboardingStage == OnboardingStage.ACTIVE_BASIC_DETAILS)
            prefs.onboardingStage = OnboardingStage.ACTIVE_DOCUMENTS
        activeServiceViewModel.moveToNextTab()
        activeServiceViewModel.refreshTabsState()
        //prefs.isActiveBasicSubmit = true
        //tabAccessControl.enableDisableTabs(true, true, false)

        //enableDisableTabs(tab_tablayout_activeservice, true, true, false)


    }



    /*override fun onActiveBasicCombinedDetailSuccess(response: ResponseCombinationDetails) {
        dismissLoader()
        Log.d(
            "Combine",
            "observeActiveDetails: " + response.combinedetails
        )

        LGAList.clear()
        subtreasuryList.clear()
        GradeLevelsList.clear()
        occupationsList.clear()

        if (response.combinedetails?.combinelgas?.size!! > 0) {
            LGAList.add(
                LgasItem(
                    "",
                    " - Select LGA - ",
                    0,
                    ""
                )
            )

            response.combinedetails.combinelgas.forEach {
                LGAList.add(
                    LgasItem(
                        it?.country,
                        it?.name,
                        it?.id,
                        it?.state
                    )
                )
            }
        }
        LGAspinnerfun()
        //binding.spActiveLga.setSelection(prefs.lga!!!!.toInt())

        if (response.combinedetails?.combinesubTreasuries?.size!! > 0) {
            subtreasuryList.add(
                SubTreasuryItem(
                    "",
                    " - Select SubTreasury - ",
                    0,
                    ""
                )
            )
            response.combinedetails.combinesubTreasuries.forEach {
                subtreasuryList.add(
                    SubTreasuryItem(
                        it?.country,
                        it?.name,
                        it?.id,
                        it?.state
                    )
                )
            }
        }
        SubTreasuryspinnerfun()

        if (response.combinedetails?.combinegradeLevels?.size!! > 0) {
            GradeLevelsList.add(
                GradeLevelsItem(
                    " - Select GradeLevel - ",
                    0
                )
            )
            response.combinedetails.combinegradeLevels.forEach {
                GradeLevelsList.add(
                    GradeLevelsItem(
                        it?.level,
                        it?.id
                    )
                )
            }
        }
        GradeLevelspinnerfun()

        if (response.combinedetails?.combineoccupations?.size!! > 0) {
            occupationsList.add(
                OccupationsItem(
                    " - Select Occupation - ",
                    0,
                    ""
                )


            )

            occupationsList.add(
                OccupationsItem(
                    " Others ",
                    -1,
                    ""
                )


            )
            response.combinedetails.combineoccupations.forEach {
                occupationsList.add(
                    OccupationsItem(
                        it?.name,
                        it?.id,
                        it?.category
                    )
                )
            }
        }
        Occupationspinnerfun()
    }

    override fun onActiveBasicCombinedDetailFailure(response: ResponseCombinationDetails) {

        dismissLoader()
        Toast.makeText(
            context,
            response.combinedetails?.message,
            Toast.LENGTH_LONG
        ).show()

    }*/

    fun onAcombinedDetailSuccess(response: ResponseCombinationDetails) {
        Log.d("Active combine", "onAcombinedDetailSuccess: " + response)




        Log.d(
            "Combine", "observeActiveDetails: " + response.combinedetails?.combinelgas
        )

        LGAList.clear()
        subtreasuryList.clear()
        GradeLevelsList.clear()
        occupationsList.clear()

        if (response.combinedetails?.combinelgas?.size!! > 0) {
            LGAList.add(
                LgasItem(
                    "", " - Select LGA - ", 0, ""
                )
            )

            response.combinedetails.combinelgas.forEach {
                LGAList.add(
                    LgasItem(
                        it?.country, it?.name, it?.id, it?.state
                    )
                )
            }
        }
        Log.d("LogLGA", "LGAList:CombinedSuccess $LGAList")
        //Log.d("TAG", "spinnerLGAList $response.combinedetails?.combinelgas?.size")
        LGAspinnerfun()

        if (response.combinedetails?.combinesubTreasuries?.size!! > 0) {
            subtreasuryList.add(
                SubTreasuryItem(
                    "", " - Select SubTreasury - ", 0, ""
                )
            )
            response.combinedetails.combinesubTreasuries.forEach {
                subtreasuryList.add(
                    SubTreasuryItem(
                        it?.country, it?.name, it?.id, it?.state
                    )
                )
            }
        }
        SubTreasuryspinnerfun()

        if (response.combinedetails?.combinegradeLevels?.size!! > 0) {
            GradeLevelsList.add(
                GradeLevelsItem(
                    " - Select GradeLevel - ", 0
                )
            )
            response.combinedetails.combinegradeLevels.forEach {
                GradeLevelsList.add(
                    GradeLevelsItem(
                        it?.level, it?.id
                    )
                )
            }
        }
        GradeLevelspinnerfun()

        if (response.combinedetails?.combineoccupations?.size!! > 0) {
            occupationsList.add(
                OccupationsItem(
                    " - Select Occupation - ", 0, ""
                )
            )


            response.combinedetails.combineoccupations.forEach {
                occupationsList.add(
                    OccupationsItem(
                        it?.name, it?.id, it?.category
                    )
                )
            }
            occupationsList.add(
                OccupationsItem(
                    " Others ", -1, ""
                )
            )


        }
        Occupationspinnerfun()


        //ActiveRetriveApiCall()



    }

}