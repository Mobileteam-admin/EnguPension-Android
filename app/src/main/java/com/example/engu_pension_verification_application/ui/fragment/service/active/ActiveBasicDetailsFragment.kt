package com.example.engu_pension_verification_application.ui.fragment.service.active

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.view.isEmpty
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.util.AlphabeticTextWatcher
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.databinding.FragmentActiveBasicDetailsBinding
import com.example.engu_pension_verification_application.model.dto.EnguCalendarRange
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
import com.example.engu_pension_verification_application.ui.dialog.EnguCalendarDialog
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.CalendarUtils
import com.example.engu_pension_verification_application.util.NetworkUtils
import com.example.engu_pension_verification_application.util.OnboardingStage
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.ActiveBasicDetailViewModel
import com.example.engu_pension_verification_application.viewmodel.ActiveServiceViewModel
import com.example.engu_pension_verification_application.viewmodel.EnguCalendarHandlerViewModel
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
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
    private val enguCalendarHandlerViewModel by activityViewModels<EnguCalendarHandlerViewModel>()
    companion object {
        const val TAB_POSITION = 0
        private const val CALENDAR_ACTION_DOB = 0
        private const val CALENDAR_ACTION_JOINING = 1
        private const val MINIMUM_AGE = 18
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
        enguCalendarHandlerViewModel.onDateSelect.observe(viewLifecycleOwner) { calendar ->
            if (calendar != null) {
                enguCalendarHandlerViewModel.dismiss()
                val selectedDay = CalendarUtils.getFormattedString(
                    CalendarUtils.DATE_FORMAT_3,
                    calendar
                )
                if (enguCalendarHandlerViewModel.actionId == CALENDAR_ACTION_DOB)
                    binding.etActiveDOB.text = selectedDay
                else if (enguCalendarHandlerViewModel.actionId == CALENDAR_ACTION_JOINING)
                    binding.etActiveDateAppointment.text = selectedDay

                enguCalendarHandlerViewModel.onDateSelect.value = null
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
            val startCalendar = CalendarUtils.getMinCalendar()
            var endCalendar = Calendar.getInstance()
            val doj = binding.etActiveDateAppointment.text.toString()
            if (doj.isNotEmpty()) {
                endCalendar = CalendarUtils.getCalendar(CalendarUtils.DATE_FORMAT_3, doj)!!
            }
            if (CalendarUtils.getYearDifference(endCalendar, Calendar.getInstance()) < MINIMUM_AGE) {
                endCalendar = Calendar.getInstance()
                endCalendar.add(Calendar.YEAR, -MINIMUM_AGE)
            }
            enguCalendarHandlerViewModel.minYear = startCalendar.get(Calendar.YEAR)
            enguCalendarHandlerViewModel.maxYear = endCalendar.get(Calendar.YEAR)
            enguCalendarHandlerViewModel.enguCalendarRange = EnguCalendarRange(
                listOf(Pair(startCalendar, endCalendar))
            )
            enguCalendarHandlerViewModel.setInitSelectedDay(binding.etActiveDOB.text.toString(), CalendarUtils.DATE_FORMAT_3)
            enguCalendarHandlerViewModel.actionId = CALENDAR_ACTION_DOB
            showDialog(EnguCalendarDialog())
        }

        binding.etActiveDateAppointment.setOnClickListener {
            val dob = binding.etActiveDOB.text.toString()
            var startCalendar = CalendarUtils.getMinCalendar()
            val endCalendar = Calendar.getInstance()
            if (dob.isNotEmpty()) {
                startCalendar = CalendarUtils.getCalendar(CalendarUtils.DATE_FORMAT_3, dob)!!
            }
            enguCalendarHandlerViewModel.minYear = startCalendar.get(Calendar.YEAR)
            enguCalendarHandlerViewModel.maxYear = endCalendar.get(Calendar.YEAR)
            enguCalendarHandlerViewModel.enguCalendarRange = EnguCalendarRange(
                listOf(Pair(startCalendar, endCalendar))
            )
            enguCalendarHandlerViewModel.setInitSelectedDay(binding.etActiveDateAppointment.text.toString(), CalendarUtils.DATE_FORMAT_3)
            enguCalendarHandlerViewModel.actionId = CALENDAR_ACTION_JOINING
            showDialog(EnguCalendarDialog())
        }

        binding.llActivebasicdetailsNext.setOnClickListener {
            //nextButtonCall()
            if (isValidActiveBasicDetails()) {
                Ph_no = "+" + binding.activeNextKinPhoneCcp.fullNumber
                nextButtonCall()
            }
        }
    }

    private fun GradeLevelspinnerfun() {
        gradeLevelAdapter.changeList(GradeLevelsList)
    }


    private fun LGAspinnerfun() {
        lgaSpinnerAdapter.changeList(LGAList)
    }

    private fun SubTreasuryspinnerfun() {
        subTreasuryAdapter.changeList(subtreasuryList)
    }

    private fun Occupationspinnerfun() {
        occupationsAdapter.changeList(occupationsList)
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
        val doa = binding.etActiveDateAppointment.text.toString()
        val dob = binding.etActiveDOB.text.toString()
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
        var errorMessage:String? = null
        if (TextUtils.isEmpty(binding.etActiveFirstName.text.trim()) || !NAME_PATTERN.matcher(binding.etActiveFirstName.text.trim()).matches()) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.first_name).lowercase())
        } else if (!NAME_PATTERN_OR_NULL.matcher(binding.etActiveMiddleName.text.trim()).matches()) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.middle_name).lowercase())
        } else if (TextUtils.isEmpty(binding.etActiveLastName.text.trim()) || !NAME_PATTERN.matcher(binding.etActiveLastName.text.trim()).matches()) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.last_name).lowercase())
        } else if (TextUtils.isEmpty(binding.etActiveDOB.text)) {
            errorMessage = getString(R.string.select_input_msg, getString(R.string.date_of_birth).lowercase())
        } else if (binding.radioGroupActive.checkedRadioButtonId <= 0) {
            errorMessage = getString(R.string.select_input_msg, getString(R.string.gender).lowercase())
        } else if (TextUtils.isEmpty(binding.etActiveAddress.text)) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.address).lowercase())
        } else if (TextUtils.isEmpty(binding.etActivePincode.text)) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.pincode).lowercase())
        } else if (binding.spActiveLga.selectedItemPosition == 0 || (binding.spActiveLga.isEmpty())) {
            errorMessage = getString(R.string.select_input_msg, getString(R.string.lga))
        } else if (TextUtils.isEmpty(binding.etActiveNextKin.text.trim()) || !NAME_PATTERN.matcher(binding.etActiveNextKin.text.trim()).matches()) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.name_of_next_kin))
        } else if (!binding.etActiveNextKinEmail.text.toString().isValidOptionalEmail()) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.email_id_of_next_kin))
        } else if (TextUtils.isEmpty(binding.etActiveNextKinPhone.text) || !binding.activeNextKinPhoneCcp.isValid) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.phone_num_of_next_kin))
        } else if (TextUtils.isEmpty(binding.etActiveNextKinAddress.text)) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.address_of_next_kin))
        } else if (TextUtils.isEmpty(binding.etActiveKinPincode.text)) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.pincode_of_next_kin))
        } else if ((binding.spActiveSubTreasury.selectedItemPosition == 0) || (binding.spActiveSubTreasury.isEmpty())) {
            errorMessage = getString(R.string.select_input_msg, getString(R.string.sub_treasury).lowercase())
        } else if (TextUtils.isEmpty(binding.etActiveDateAppointment.text)) {
            errorMessage = getString(R.string.select_input_msg, getString(R.string.date_of_appointment).lowercase())
        } else if ((binding.spActiveLastGrade.selectedItemPosition == 0) || (binding.spActiveLastGrade.isEmpty())) {
            errorMessage = getString(R.string.select_input_msg, getString(R.string.grade_level).lowercase())
        } else if ((binding.spActiveOccupationType.selectedItemPosition == 0) || (binding.spActiveOccupationType.isEmpty())) {
            errorMessage = getString(R.string.select_input_msg, getString(R.string.occupation_type).lowercase())
        } else if (binding.etActiveOccupationOther.visibility == View.VISIBLE && ((!NAME_PATTERN.matcher(
                binding.etActiveOccupationOther.text.toString()
            ).matches() || (TextUtils.isEmpty(binding.etActiveOccupationOther.text))))
        ) {
            errorMessage = getString(R.string.select_input_msg, getString(R.string.other_occupation))
        }
        errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
        return errorMessage == null
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