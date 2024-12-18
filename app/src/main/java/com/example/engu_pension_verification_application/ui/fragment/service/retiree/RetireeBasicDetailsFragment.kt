package com.example.engu_pension_verification_application.ui.fragment.service.retiree

import android.app.AlertDialog
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
import androidx.core.view.isGone
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.databinding.FragmentRetireeBasicDetailsBinding
import com.example.engu_pension_verification_application.model.dto.EnguCalendarRange
import com.example.engu_pension_verification_application.model.input.InputRetireeBasicDetails
import com.example.engu_pension_verification_application.model.response.*
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.fragment.service.active.isValidOptionalEmail
import com.example.engu_pension_verification_application.ui.adapter.GradeLevelAdapter
import com.example.engu_pension_verification_application.ui.adapter.LastPositionAdapter
import com.example.engu_pension_verification_application.ui.adapter.LGASpinnerAdapter
import com.example.engu_pension_verification_application.ui.adapter.LocalGovPensionAdapter
import com.example.engu_pension_verification_application.ui.adapter.SubTreasuryAdapter
import com.example.engu_pension_verification_application.ui.dialog.EnguCalendarDialog
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.AlphabeticTextWatcher
import com.example.engu_pension_verification_application.util.CalendarUtils
import com.example.engu_pension_verification_application.util.NetworkUtils
import com.example.engu_pension_verification_application.util.OnboardingStage
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.EnguCalendarHandlerViewModel
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.RetireeBasicDetailsViewModel
import com.example.engu_pension_verification_application.viewmodel.RetireeServiceViewModel
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2
import com.rilixtech.widget.countrycodepicker.Country
import com.rilixtech.widget.countrycodepicker.CountryCodePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList
import java.util.Calendar
import java.util.regex.Pattern

class RetireeBasicDetailsFragment : BaseFragment() {
    companion object {
        const val TAB_POSITION = 0
        private const val CALENDAR_ACTION_DOB = 0
        private const val CALENDAR_ACTION_JOINING = 1
        private const val CALENDAR_ACTION_RETIREMENT = 2
        private const val MINIMUM_AGE = 18
    }
    private lateinit var binding:FragmentRetireeBasicDetailsBinding
    private lateinit var retireeBasicDetailsViewModel: RetireeBasicDetailsViewModel
    private val retireeServiceViewModel by activityViewModels<RetireeServiceViewModel>()
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    private val enguCalendarHandlerViewModel by activityViewModels<EnguCalendarHandlerViewModel>()

    // previuos name NAME_PATTERN = Pattern.compile("^[a-zA-Z]+$")

    val NAME_PATTERN = Pattern.compile("^[a-zA-Z]+(?:\\s[a-zA-Z]+)*$")
    val NAME_PATTERN_OR_NULL = Pattern.compile("^$|^[a-zA-Z]+(?:\\s[a-zA-Z]+)?$")


    var lgaS = ""
    var subS = ""
    var gradelvlS = ""
    var localPenBoardS = ""
    var lastPostionHeldS = ""


    var Ph_no = ""
    var sex = ""
    var selected_country = ""



    //var selected_country: String? = "Nigeria"

    val prefs = SharedPref

    //retiree retrive user profile data store
    lateinit var RetireeUserRetrive: RetireeRetriveUserProfileDetails


    val LGAList = ArrayList<LgasItem?>()
    val subtreasuryList = ArrayList<SubTreasuryItem?>()
    val GradeLevelsList = ArrayList<GradeLevelsItem?>()
    val lastPositionList = ArrayList<CombineLastPositions?>()
    val localGovPensionList = ArrayList<CombineLocalGovenmentPensionBoardsItem?>()


    var gradelevel: Int? = 0
    var subtreasury: Int? = 0
    var lgalist: Int? = 0
    var lastPositionHeld: Int? = 0
    var lastPosition = ""
    var localGovernmentPensionBoardId: Int? = 0


    lateinit var lgaSpinnerAdapter: LGASpinnerAdapter
    lateinit var subTreasuryAdapter: SubTreasuryAdapter
    lateinit var gradeLevelAdapter: GradeLevelAdapter
    lateinit var lastPositionAdapter: LastPositionAdapter
    lateinit var localGovPensionAdapter: LocalGovPensionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRetireeBasicDetailsBinding.inflate(inflater, container, false)
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
        retireeBasicDetailsViewModel = ViewModelProviders.of(
            this,
            EnguViewModelFactory(networkRepo)
        ).get(RetireeBasicDetailsViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), 
            EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }
    private fun observeLiveData() {
        retireeServiceViewModel.currentTabPos.observe(viewLifecycleOwner){
            if (it == TAB_POSITION) initcall()
        }
        retireeBasicDetailsViewModel.combinedDetailsApiResult.observe(viewLifecycleOwner) { response ->
            dismissLoader()
            if (response.combinedetails?.status == AppConstants.SUCCESS) {
                onRcombinedDetailSuccess(response)
            } else {
                showFetchErrorDialog(
                    ::initcall,
                    response.combinedetails?.message ?: getString(R.string.common_error_msg_2)
                )
            }
        }
        retireeBasicDetailsViewModel.basicDetailsApiResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                response.detail.userProfileDetails?.let {
                    RetireeUserRetrive = it
                    populateViews()
                }
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            retireeBasicDetailsViewModel.fetchRetireeBasicDetails()
                        }
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        retireeBasicDetailsViewModel.basicDetailsSubmissionResult.observe(viewLifecycleOwner) { pair ->
            if (pair != null) {
                dismissLoader()
                val request = pair.first
                val response = pair.second
                if (response.detail?.status == AppConstants.SUCCESS) {
                    onRetireeBasicDetailSuccess(response)
                } else {
                    if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (tokenRefreshViewModel2.fetchRefreshToken()) {
                                retireeBasicDetailsViewModel.submitBasicDetails(request)
                            }
                        }
                    } else {
                        Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                    }
                }
                retireeBasicDetailsViewModel.resetBasicDetailsSubmissionResult()
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
                    binding.etRetireeDOB.text = selectedDay
                else if (enguCalendarHandlerViewModel.actionId == CALENDAR_ACTION_JOINING) {
                    binding.etRetireeDateAppointment.text = selectedDay
                    validateLastPromotionYear()
                } else if (enguCalendarHandlerViewModel.actionId == CALENDAR_ACTION_RETIREMENT) {
                    binding.etRetireeDateRetirement.text = selectedDay
                    validateLastPromotionYear()
                }

                enguCalendarHandlerViewModel.onDateSelect.value = null
            }
        }
    }
    private fun initViews() {
        lastPositionAdapter = LastPositionAdapter(context, lastPositionList)
        binding.spRetireePositionLast.adapter = lastPositionAdapter

        gradeLevelAdapter = GradeLevelAdapter(context, GradeLevelsList)
        binding.spRetireeGradeLevel.adapter = gradeLevelAdapter

        lgaSpinnerAdapter = LGASpinnerAdapter(context, LGAList)
        binding.spRetireeLga.adapter = lgaSpinnerAdapter

        subTreasuryAdapter = SubTreasuryAdapter(context, subtreasuryList)
        binding.spRetireeSubTreasury.adapter = subTreasuryAdapter

        localGovPensionAdapter = LocalGovPensionAdapter(context, localGovPensionList)
        binding.spRetireePensionBoard.adapter = localGovPensionAdapter

        //kinphone
        binding.retireeNextKinPhoneCcp.registerPhoneNumberTextView(binding.etRetireeNextKinPhone)
        selected_country = binding.ccpRetireedetails.selectedCountryName
        Log.d("selected_country", "onViewCreated: " + selected_country)

        onSpinnerTextWatcher()

        onClicked()
        // observeRetireeDetails()

        binding.ccpRetireedetails.setOnCountryChangeListener(object :
            CountryCodePicker.OnCountryChangeListener {
            override fun onCountrySelected(selectedCountry: Country?) {
                selected_country = binding.ccpRetireedetails.selectedCountryName
                Log.d("changed_country", "onViewCreated: " + binding.ccpRetireedetails.selectedCountryName)
                showLoader()
                if (NetworkUtils.isConnectedToNetwork(requireContext())) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        retireeBasicDetailsViewModel.fetchCombinedDetails(selected_country)
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
                }
            }
        })
    }
    private fun populateViews() {
        Log.d("LogLGA", "LGAList:onRetrivedDataSetFields() $LGAList")

        if (!RetireeUserRetrive.firstName.isNullOrEmpty()) {
            if (prefs.onboardingStage == OnboardingStage.RETIREE_BASIC_DETAILS) {
                prefs.onboardingStage = OnboardingStage.RETIREE_DOCUMENTS
                retireeServiceViewModel.refreshTabsState()
            }
            binding.etRetireeFirstName.setText(RetireeUserRetrive.firstName) //1
            binding.etRetireeMiddleName.setText(RetireeUserRetrive.middleName) //2
            binding.etRetireeLastName.setText(RetireeUserRetrive.lastName) //3
            binding.etRetireeDOB.setText(RetireeUserRetrive.dob) //4


            var RetriveSex = RetireeUserRetrive.sex //5

            when (RetriveSex) {

                "male" -> {
                    binding.radioGroupRetiree.check(R.id.rb_retiree_male)
                    sex = "male"
                }

                "female" -> {
                    binding.radioGroupRetiree.check(R.id.rb_retiree_female)
                    sex = "female"
                }
            }

            binding.etRetireeAddress.setText(RetireeUserRetrive.address) //6

            binding.etRetireePincode.setText(RetireeUserRetrive.pincode)

            binding.etRetireeNextKin.setText(RetireeUserRetrive.nextOfKinName) //7

            binding.etRetireeNextKinEmail.setText(RetireeUserRetrive.nextOfKinEmail) //8


            //backend format indian number +917917854563 thats why takelast 10 digits
            val RetrivePhn = RetireeUserRetrive.nextOfKinPhoneNumber

            var WithoutCC = RetrivePhn!!.takeLast(10)

            var RetriveCC = RetrivePhn.substring(1 until RetrivePhn.length - WithoutCC.length)

            var RetriveCCInt = RetriveCC.toInt()

            binding.etRetireeNextKinPhone.setText(WithoutCC) //9

            binding.retireeNextKinPhoneCcp.setCountryForPhoneCode(RetriveCCInt)

            binding.etRetireeNextKinAddress.setText(RetireeUserRetrive.nextOfKinAddress) //10

            binding.etRetireeKinPincode.setText(RetireeUserRetrive.kinPincode)

            binding.etRetireeDateAppointment.setText(RetireeUserRetrive.dateOfAppointment) //11

            binding.tvLastPromotionYear.setText(RetireeUserRetrive.lastPromotionYear) //12

            binding.etRetireeDateRetirement.setText(RetireeUserRetrive.dateOfRetirement)  //13

            // 5 spinners, lga,loal pension, subtresury,gradelevel,position held lst

            //spinnerSet from Retrive RetireeUserRetrive

            //lga
            lgaS = RetireeUserRetrive.lga.toString()

            Log.d("spinner", "spinnerLgas $lgaS ")
            if (lgaS.isNotEmpty()) {
                var pos = lgaSpinnerAdapter.getPositionByName(lgaS)
                binding.spRetireeLga.setSelection(pos)

                Log.d("spinner", "spinnerPos $pos ")
            } //14

            //local pension
            localPenBoardS = RetireeUserRetrive.localGovernmentPensionBoard.toString()
            if (localPenBoardS.isNotEmpty()) {
                var pos = localGovPensionAdapter.getPositionByName(localPenBoardS)
                binding.spRetireePensionBoard.setSelection(pos)
            } //15


            //sub tresury
            subS = RetireeUserRetrive.subTreasury.toString()
            if (subS.isNotEmpty()) {
                var pos = subTreasuryAdapter.getPositionByName(subS)
                binding.spRetireeSubTreasury.setSelection(pos)

            } //16

            //gradelvl
            gradelvlS = RetireeUserRetrive.gradeLevel.toString()
            Log.d("LogGradelevel", "gradelvlS:$gradelvlS ")
            Log.d("logGradelevel", "gradelist : $GradeLevelsList")
            if (gradelvlS.isNotEmpty()) {
                Log.d("LogGradelevel", "gradelvlS.isNotEmpty():$gradelvlS")
                var Gradelvlpos = gradeLevelAdapter.getPositionByName(gradelvlS)
                Log.d("LogGradelevel", "Gradelvlpos:$Gradelvlpos ")
                binding.spRetireeGradeLevel.setSelection(Gradelvlpos)
            } //17

            //positon last held
            lastPostionHeldS = RetireeUserRetrive.positionHeldLast.toString()
            if (lastPostionHeldS.isNotEmpty()) {

                var pos = lastPositionAdapter.getPositionByName(lastPostionHeldS)

                if (pos != -1) {
                    Log.d("positionlast", "spinnerlast: $pos $lastPostionHeldS")
                    binding.spRetireePositionLast.setSelection(pos)
                }else{
                    /*sp_active_occupation_type.setSelection(1)
                    et_active_occupation_other.visibility = View.VISIBLE
                    et_active_occupation_other.setText(occupationS)
                    occupation = occupationS*/

                    binding.spRetireePositionLast.setSelection(lastPositionAdapter.count -1)
                    binding.etRetireePositionOther.visibility = View.VISIBLE
                    binding.etRetireePositionOther.setText(lastPostionHeldS)

                    lastPosition = lastPostionHeldS

                }
                //binding.spRetireePositionLast.setSelection(lastPostionHeldS.toInt())

            }
        //18
            //enableDisableTabs(tab_tablayout_retiree, true, true, false)


        }

    }

    private fun onSpinnerTextWatcher() {
        Log.d("LogLGA", "LGAList: onSpinnerTextWatcher() $LGAList")
        onTextLgaWatcher()
        onlocalGovPensionTextwatcher()
        onTextSubTresuryWatcher()
        onTextGradeLevelWatcher()
        onLastPositionTextWatcher()

    }


    private fun onTextGradeLevelWatcher() {
        binding.spRetireeGradeLevel.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    if (GradeLevelsList.get(position)?.id?.equals(0) == true) {

                    } else {
                        gradelevel = GradeLevelsList[position]?.id
                    }
                }


                override fun onNothingSelected(parent: AdapterView<*>?) {
                    
                }

            }
    }

    private fun onTextSubTresuryWatcher() {
        binding.spRetireeSubTreasury.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    if (subtreasuryList.get(position)?.id?.equals(0) == true) {

                    } else {
                        subtreasury = subtreasuryList[position]?.id
                    }
                }


                override fun onNothingSelected(parent: AdapterView<*>?) {
                    
                }

            }
    }

    private fun onTextLgaWatcher() {
        binding.spRetireeLga.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (LGAList.get(position)?.id?.equals(0) == true) {

                } else {
                    lgalist = LGAList[position]?.id
                }
            }


            override fun onNothingSelected(parent: AdapterView<*>?) {
                
            }

        }
    }

    private fun onLastPositionTextWatcher() {
        binding.spRetireePositionLast.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    if (lastPositionList.get(position)?.id?.equals(0) == true) {

                    } else if (lastPositionList.get(position)?.id?.equals(-1) == true){


                        //visiblty show
                        binding.etRetireePositionOther.visibility = View.VISIBLE
                        binding.etRetireePositionOther.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                                // Code to handle text before changes are made
                            }

                            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                                // Code to handle text changes
                            }

                            override fun afterTextChanged(s: Editable) {
                                // Code to handle text after changes are made

                                lastPosition = binding.etRetireePositionOther.text.toString()
                            }
                        })


                    }else{
                        binding.etRetireePositionOther.visibility = View.GONE
                        lastPosition = lastPositionList[position]?.id.toString()
                    }
                }


                override fun onNothingSelected(parent: AdapterView<*>?) {
                    
                }

            }
    }

    private fun onlocalGovPensionTextwatcher() {
        binding.spRetireePensionBoard.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    if (localGovPensionList.get(position)?.id?.equals(0) == true) {

                    } else {
                        localGovernmentPensionBoardId = localGovPensionList[position]?.id
                    }
                }


                override fun onNothingSelected(parent: AdapterView<*>?) {
                    
                }

            }
    }

    private fun onClicked() {

        binding.etRetireeFirstName.addTextChangedListener(AlphabeticTextWatcher(binding.etRetireeFirstName))
        binding.etRetireeMiddleName.addTextChangedListener(AlphabeticTextWatcher(binding.etRetireeMiddleName))
        binding.etRetireeLastName.addTextChangedListener(AlphabeticTextWatcher(binding.etRetireeLastName))
        binding.etRetireeNextKin.addTextChangedListener(AlphabeticTextWatcher(binding.etRetireeNextKin))


        onTextGradeLevelWatcher()
        onTextSubTresuryWatcher()
        onTextLgaWatcher()
        onLastPositionTextWatcher()
        onlocalGovPensionTextwatcher()


        binding.radioGroupRetiree.setOnCheckedChangeListener { group, checkedId ->
            sex = if (R.id.rb_retiree_male == checkedId) "male" else "female"
        }

        binding.etRetireeDOB.setOnClickListener {
            val startCalendar = CalendarUtils.getMinCalendar()
            var endCalendar = Calendar.getInstance()
            val doj = binding.etRetireeDateAppointment.text.toString()
            if (doj.isEmpty()) {
                val dor = binding.etRetireeDateRetirement.text.toString()
                if (dor.isNotEmpty()) {
                    endCalendar = CalendarUtils.getCalendar(CalendarUtils.DATE_FORMAT_3, dor)!!
                }
            } else {
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
            enguCalendarHandlerViewModel.setInitSelectedDay(binding.etRetireeDOB.text.toString(), CalendarUtils.DATE_FORMAT_3)
            enguCalendarHandlerViewModel.actionId = CALENDAR_ACTION_DOB
            showDialog(EnguCalendarDialog())
        }

        binding.etRetireeDateAppointment.setOnClickListener {
            val dob = binding.etRetireeDOB.text.toString()
            val dor = binding.etRetireeDateRetirement.text.toString()
            var startCalendar = CalendarUtils.getMinCalendar()
            var endCalendar = Calendar.getInstance()
            if (dob.isNotEmpty()) {
                startCalendar = CalendarUtils.getCalendar(CalendarUtils.DATE_FORMAT_3, dob)!!
            }
            if (dor.isNotEmpty()) {
                endCalendar = CalendarUtils.getCalendar(CalendarUtils.DATE_FORMAT_3, dor)!!
            }
            enguCalendarHandlerViewModel.minYear = startCalendar.get(Calendar.YEAR)
            enguCalendarHandlerViewModel.maxYear = endCalendar.get(Calendar.YEAR)
            enguCalendarHandlerViewModel.enguCalendarRange = EnguCalendarRange(
                listOf(Pair(startCalendar, endCalendar))
            )
            enguCalendarHandlerViewModel.setInitSelectedDay(binding.etRetireeDateAppointment.text.toString(), CalendarUtils.DATE_FORMAT_3)
            enguCalendarHandlerViewModel.actionId = CALENDAR_ACTION_JOINING
            showDialog(EnguCalendarDialog())
        }

        binding.etRetireeDateRetirement.setOnClickListener {
            var startCalendar = CalendarUtils.getMinCalendar()
            val endCalendar = Calendar.getInstance()
            val doj = binding.etRetireeDateAppointment.text.toString()
            if (doj.isEmpty()) {
                val dob = binding.etRetireeDOB.text.toString()
                if (dob.isNotEmpty())
                    startCalendar = CalendarUtils.getCalendar(CalendarUtils.DATE_FORMAT_3, dob)!!
            } else {
                startCalendar = CalendarUtils.getCalendar(CalendarUtils.DATE_FORMAT_3, doj)!!
            }
            enguCalendarHandlerViewModel.minYear = startCalendar.get(Calendar.YEAR)
            enguCalendarHandlerViewModel.maxYear = endCalendar.get(Calendar.YEAR)
            enguCalendarHandlerViewModel.enguCalendarRange = EnguCalendarRange(
                listOf(Pair(startCalendar, endCalendar))
            )
            enguCalendarHandlerViewModel.setInitSelectedDay(binding.etRetireeDateRetirement.text.toString(), CalendarUtils.DATE_FORMAT_3)
            enguCalendarHandlerViewModel.actionId = CALENDAR_ACTION_RETIREMENT
            showDialog(EnguCalendarDialog())
        }

        binding.tvLastPromotionYear.setOnClickListener {
            val doj = binding.etRetireeDateAppointment.text.toString()
            val dor = binding.etRetireeDateRetirement.text.toString()
            val minYear = if (doj.isNotEmpty())
                 CalendarUtils.getCalendar(CalendarUtils.DATE_FORMAT_3, doj)!!.get(Calendar.YEAR)
                else 1900
            val maxYear = if (dor.isNotEmpty())
                 CalendarUtils.getCalendar(CalendarUtils.DATE_FORMAT_3, dor)!!.get(Calendar.YEAR)
                else Calendar.getInstance().get(Calendar.YEAR)
            val years = mutableListOf<String>()
            for (i in maxYear downTo minYear)
                years.add(i.toString())
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.choose_year)
                .setItems(years.toTypedArray()) { _, i ->
                    binding.tvLastPromotionYear.text = years[i]
                    validateLastPromotionYear()
                }.show()
        }
        binding.llRetireeBasicdetailsNext.setOnClickListener {

            //nextButtonCall()
            if (isValidRetireeBasicDetails()) {
                Ph_no = "+" + binding.retireeNextKinPhoneCcp.fullNumber
                nextButtonCall()
            }
        }


    }

    private fun validateLastPromotionYear() {
        if (!binding.tvLastPromotionYear.text.isNullOrEmpty())
            binding.tvLastPromotionError.isGone = isValidLastPromotionYear()
    }

    private fun isValidLastPromotionYear(): Boolean {
        if (binding.tvLastPromotionYear.text.isNullOrEmpty())
            return false
        else {
            val lastPromotionYear = binding.tvLastPromotionYear.text.toString().toInt()
            val doj = binding.etRetireeDateAppointment.text.toString()
            if (doj.isNotEmpty()) {
                val joiningYear = CalendarUtils.getCalendar(CalendarUtils.DATE_FORMAT_3, doj)!!.get(Calendar.YEAR)
                if (lastPromotionYear < joiningYear) return false
            }

            val dor = binding.etRetireeDateRetirement.text.toString()
            if (dor.isNotEmpty()) {
                val retirementYear = CalendarUtils.getCalendar(CalendarUtils.DATE_FORMAT_3, dor)!!.get(Calendar.YEAR)
                if (lastPromotionYear > retirementYear) return false
            }
        }
        return true
    }

    private fun isValidRetireeBasicDetails(): Boolean {
        var errorMessage:String? = null
        if (TextUtils.isEmpty(binding.etRetireeFirstName.text.trim()) || !NAME_PATTERN.matcher(binding.etRetireeFirstName.text.trim()).matches()) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.first_name).lowercase())
        } else if (!NAME_PATTERN_OR_NULL.matcher(binding.etRetireeMiddleName.text.trim()).matches()) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.middle_name).lowercase())
        } else if (TextUtils.isEmpty(binding.etRetireeLastName.text.trim()) || !NAME_PATTERN.matcher(binding.etRetireeLastName.text.trim()).matches()) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.last_name).lowercase())
        } else if (TextUtils.isEmpty(binding.etRetireeDOB.text)) {
            errorMessage = getString(R.string.select_input_msg, getString(R.string.date_of_birth))
        } else if (binding.radioGroupRetiree.checkedRadioButtonId <= 0) {
            errorMessage = getString(R.string.select_input_msg, getString(R.string.gender))
        } else if (TextUtils.isEmpty(binding.etRetireeAddress.text)) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.address).lowercase())
        } else if (TextUtils.isEmpty(binding.etRetireePincode.text)) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.pincode).lowercase())
        } else if (binding.spRetireeLga.selectedItemPosition == 0 || (binding.spRetireeLga.isEmpty())) {
            errorMessage = getString(R.string.select_input_msg, getString(R.string.lga))
        } else if (TextUtils.isEmpty(binding.etRetireeNextKin.text) || !NAME_PATTERN.matcher(binding.etRetireeNextKin.text.trim()).matches()) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.name_of_next_kin))
        } else if (!binding.etRetireeNextKinEmail.text.toString().isValidOptionalEmail()) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.email_id_of_next_kin))
        } else if (TextUtils.isEmpty(binding.etRetireeNextKinPhone.text) || !binding.retireeNextKinPhoneCcp.isValid) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.phone_num_of_next_kin))
        } else if (TextUtils.isEmpty(binding.etRetireeNextKinAddress.text)) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.address_of_next_kin))
        } else if (TextUtils.isEmpty(binding.etRetireeKinPincode.text)) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.pincode_of_next_kin))
        } else if (binding.spRetireePensionBoard.selectedItemPosition == 0 || (binding.spRetireePensionBoard.isEmpty())) {
            errorMessage = getString(R.string.select_input_msg, getString(R.string.local_pension_board))
        } else if (binding.spRetireeSubTreasury.selectedItemPosition == 0 || (binding.spRetireeSubTreasury.isEmpty())) {
            errorMessage = getString(R.string.select_input_msg, getString(R.string.sub_treasury).lowercase())
        } else if (TextUtils.isEmpty(binding.etRetireeDateAppointment.text)) {
            errorMessage = getString(R.string.select_input_msg, getString(R.string.date_of_appointment).lowercase())
        } else if ((TextUtils.isEmpty(binding.tvLastPromotionYear.text)) || (binding.tvLastPromotionYear.text.length != 4) || !isValidLastPromotionYear()) {
            errorMessage = getString(R.string.select_input_msg, getString(R.string.last_promotion_year).lowercase())
        } else if (binding.spRetireeGradeLevel.selectedItemPosition == 0 || (binding.spRetireeGradeLevel.isEmpty())) {
            errorMessage = getString(R.string.select_input_msg, getString(R.string.grade_level).lowercase())
        } else if (TextUtils.isEmpty(binding.etRetireeDateRetirement.text)) {
            errorMessage = getString(R.string.select_input_msg, getString(R.string.date_of_retirement).lowercase())
        } else if (binding.spRetireePositionLast.selectedItemPosition == 0 || (binding.spRetireePositionLast.isEmpty())) {
            errorMessage = getString(R.string.select_input_msg, getString(R.string.position_last_held).lowercase())
        } else if (binding.etRetireePositionOther.visibility == View.VISIBLE && ((!NAME_PATTERN.matcher(
                binding.etRetireePositionOther.text.toString()
            ).matches() || (TextUtils.isEmpty(binding.etRetireePositionOther.text))))
        ) {
            errorMessage = getString(R.string.enter_input_msg, getString(R.string.other_position))
        }
        errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
        return errorMessage == null
    }


    private fun initcall() {
        showLoader()
        if (NetworkUtils.isConnectedToNetwork(requireContext())) {
            lifecycleScope.launch (Dispatchers.IO) {
                if (retireeBasicDetailsViewModel.fetchCombinedDetails(selected_country))
                    retireeBasicDetailsViewModel.fetchRetireeBasicDetails()
            }
        } else {
            dismissLoader()
            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
        }
    }

    private fun LastPositionspinnerfun() {
        lastPositionAdapter.changeList(lastPositionList)
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

    private fun LocalGovspinnerfun() {
        localGovPensionAdapter.changeList(localGovPensionList)
    }


    private fun nextButtonCall() {
        showLoader()
        if (NetworkUtils.isConnectedToNetwork(requireContext())) {

            prefs.Rfirst_name = binding.etRetireeFirstName.text.trim().toString()
            prefs.Rmiddle_name = binding.etRetireeMiddleName.text.trim().toString()
            prefs.Rlast_name = binding.etRetireeLastName.text.trim().toString()
            accountDetailCall()


        } else {
            dismissLoader()
            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
        }

    }

    private fun accountDetailCall() {

        val doa = binding.etRetireeDateAppointment.text.toString()
        val dob = binding.etRetireeDOB.text.toString()
        val dor = binding.etRetireeDateRetirement.text.toString()

        retireeBasicDetailsViewModel.submitBasicDetails(
            InputRetireeBasicDetails(
                positionHeldLastId = lastPosition, //lastPositionHeld.toString(),
                country = selected_country,
                address = binding.etRetireeAddress.text.toString(),
                subTreasuryId = subtreasury,
                sex = sex,
                lastName = binding.etRetireeLastName.text.trim().toString(),
                middleName = binding.etRetireeMiddleName.text.trim().toString(),
                dateOfRetirement = dor,
                dateOfAppointment = doa,
                lgaId = lgalist,
                nextOfKinAddress = binding.etRetireeNextKinAddress.text.toString(),
                nextOfKinPhoneNumber = Ph_no,
                gradeLevel = gradelevel,
                userType = "retired",
                dob = dob,
                nextOfKinName = binding.etRetireeNextKin.text.trim().toString(),
                localGovernmentPensionBoardId = localGovernmentPensionBoardId ,
                lastPromotionYear = binding.tvLastPromotionYear.text.toString().toInt(),
                nextOfKinEmail = binding.etRetireeNextKinEmail.text.toString(),
                firstName = binding.etRetireeFirstName.text.trim().toString(),
                pincode = binding.etRetireePincode.text.toString(),
                kinPincode = binding.etRetireeKinPincode.text.toString()
            )
        )
    }



    private fun onRcombinedDetailSuccess(combinationdetailsdata: ResponseCombinationDetails) {
        Log.d("Retiree combine", "onRcombinedDetailSuccess: " + combinationdetailsdata)

        Log.d(
            "Combine",
            "observeActiveDetails: " + combinationdetailsdata.combinedetails?.combinelocalGovenmentPensionBoards
        )


        LGAList.clear()
        subtreasuryList.clear()
        GradeLevelsList.clear()
        localGovPensionList.clear()

        if (combinationdetailsdata.combinedetails?.combinelgas?.size!! > 0) {
            LGAList.add(
                LgasItem(
                    "", " - Select LGA - ", 0, ""
                )
            )
            combinationdetailsdata.combinedetails.combinelgas.forEach {
                LGAList.add(
                    LgasItem(
                        it?.country, it?.name, it?.id, it?.state
                    )
                )
            }
        }
        LGAspinnerfun()

        if (combinationdetailsdata.combinedetails?.combinesubTreasuries?.size!! > 0) {
            subtreasuryList.add(
                com.example.engu_pension_verification_application.model.response.SubTreasuryItem(
                    "", " - Select SubTreasury - ", 0, ""
                )
            )
            combinationdetailsdata.combinedetails.combinesubTreasuries.forEach {
                subtreasuryList.add(
                    com.example.engu_pension_verification_application.model.response.SubTreasuryItem(
                        it?.country, it?.name, it?.id, it?.state
                    )
                )
            }
        }
        SubTreasuryspinnerfun()

        if (combinationdetailsdata.combinedetails?.combinegradeLevels?.size!! > 0) {
            GradeLevelsList.add(
                com.example.engu_pension_verification_application.model.response.GradeLevelsItem(
                    " - Select GradeLevel - ", 0
                )
            )
            combinationdetailsdata.combinedetails.combinegradeLevels.forEach {
                GradeLevelsList.add(
                    com.example.engu_pension_verification_application.model.response.GradeLevelsItem(
                        it?.level, it?.id
                    )
                )
            }
        }
        GradeLevelspinnerfun()

        if (combinationdetailsdata.combinedetails?.combinelastPositions?.size!! > 0) {
            lastPositionList.add(
                CombineLastPositions(
                    " - Select LastPosition - ", 0
                )
            )
            combinationdetailsdata.combinedetails.combinelastPositions.forEach {
                lastPositionList.add(
                    CombineLastPositions(
                        it?.positionname, it?.id
                    )
                )
            }
            lastPositionList.add(CombineLastPositions(" Others ", -1))
            /*lastPositionList.add(
                CombineLastPositions(
                    " Others ", -1
                )
            )*/
        }
        LastPositionspinnerfun()

        if (combinationdetailsdata.combinedetails?.combinelocalGovenmentPensionBoards?.size!! > 0) {
            localGovPensionList.add(
                CombineLocalGovenmentPensionBoardsItem(
                    " - Select LastPosition - ", 0
                )
            )
            combinationdetailsdata.combinedetails?.combinelocalGovenmentPensionBoards?.forEach {
                localGovPensionList.add(
                    CombineLocalGovenmentPensionBoardsItem(
                        it?.positionName, it?.id
                    )
                )
            }
        }
        LocalGovspinnerfun()
    }

    private fun onRetireeBasicDetailSuccess(response: ResponseRetireeBasicDetails) {
        Toast.makeText(context, response.detail!!.message, Toast.LENGTH_SHORT).show()
        if (prefs.onboardingStage == OnboardingStage.RETIREE_BASIC_DETAILS)
            prefs.onboardingStage = OnboardingStage.RETIREE_DOCUMENTS
        retireeServiceViewModel.moveToNextTab()
        retireeServiceViewModel.refreshTabsState()
    }
}