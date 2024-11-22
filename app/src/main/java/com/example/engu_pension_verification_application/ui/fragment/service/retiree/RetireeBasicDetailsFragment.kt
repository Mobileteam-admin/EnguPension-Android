package com.example.engu_pension_verification_application.ui.fragment.service.retiree

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
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
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.InputRetireeBasicDetails
import com.example.engu_pension_verification_application.model.response.*
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.fragment.service.active.isValidOptionalEmail
import com.example.engu_pension_verification_application.ui.adapter.GradeLevelAdapter
import com.example.engu_pension_verification_application.ui.adapter.LastPositionAdapter
import com.example.engu_pension_verification_application.ui.adapter.LGASpinnerAdapter
import com.example.engu_pension_verification_application.ui.adapter.LocalGovPensionAdapter
import com.example.engu_pension_verification_application.ui.adapter.SubTreasuryAdapter
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.AlphabeticTextWatcher
import com.example.engu_pension_verification_application.util.NetworkUtils
import com.example.engu_pension_verification_application.util.OnboardingStage
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.RetireeBasicDetailsViewModel
import com.example.engu_pension_verification_application.viewmodel.RetireeServiceViewModel
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2
import com.rilixtech.widget.countrycodepicker.Country
import com.rilixtech.widget.countrycodepicker.CountryCodePicker
import kotlinx.android.synthetic.main.fragment_retiree_basic_details.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList
import java.util.Calendar
import java.util.regex.Pattern

class RetireeBasicDetailsFragment : BaseFragment() {
    companion object {
        const val TAB_POSITION = 0
    }
    private lateinit var retireeBasicDetailsViewModel: RetireeBasicDetailsViewModel
    private val retireeServiceViewModel by activityViewModels<RetireeServiceViewModel>()
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2

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
    private var dateBirth = StringBuilder()
    private var dateAppointment = StringBuilder()
    private var dateRetirement = StringBuilder()



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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_retiree_basic_details, container, false)
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
                Toast.makeText(context, response.combinedetails?.message, Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
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
        }
    }
    private fun initViews() {
        lastPositionAdapter = LastPositionAdapter(context, lastPositionList)
        sp_retiree_position_last.adapter = lastPositionAdapter

        gradeLevelAdapter = GradeLevelAdapter(context, GradeLevelsList)
        sp_retiree_grade_level.adapter = gradeLevelAdapter

        lgaSpinnerAdapter = LGASpinnerAdapter(context, LGAList)
        sp_retiree_lga.adapter = lgaSpinnerAdapter

        subTreasuryAdapter = SubTreasuryAdapter(context, subtreasuryList)
        sp_retiree_sub_treasury.adapter = subTreasuryAdapter

        localGovPensionAdapter = LocalGovPensionAdapter(context, localGovPensionList)
        sp_retiree_pension_board.adapter = localGovPensionAdapter

        //kinphone
        retiree_next_kin_phone_ccp.registerPhoneNumberTextView(et_retiree_next_kin_phone)
        selected_country = ccp_retireedetails.selectedCountryName
        Log.d("selected_country", "onViewCreated: " + selected_country)

        onSpinnerTextWatcher()

        onClicked()
        // observeRetireeDetails()

        ccp_retireedetails.setOnCountryChangeListener(object :
            CountryCodePicker.OnCountryChangeListener {
            override fun onCountrySelected(selectedCountry: Country?) {
                selected_country = ccp_retireedetails.selectedCountryName
                Log.d("changed_country", "onViewCreated: " + ccp_retireedetails.selectedCountryName)
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
            et_retiree_firstName.setText(RetireeUserRetrive.firstName) //1
            et_retiree_middleName.setText(RetireeUserRetrive.middleName) //2
            et_retiree_lastName.setText(RetireeUserRetrive.lastName) //3
            et_retiree_DOB.setText(RetireeUserRetrive.dob) //4


            var RetriveSex = RetireeUserRetrive.sex //5

            when (RetriveSex) {

                "male" -> {
                    radioGroup_retiree.check(R.id.rb_retiree_male)
                    sex = "male"
                }

                "female" -> {
                    radioGroup_retiree.check(R.id.rb_retiree_female)
                    sex = "female"
                }
            }

            et_retiree_address.setText(RetireeUserRetrive.address) //6

            et_retiree_pincode.setText(RetireeUserRetrive.pincode)

            et_retiree_next_kin.setText(RetireeUserRetrive.nextOfKinName) //7

            et_retiree_next_kin_email.setText(RetireeUserRetrive.nextOfKinEmail) //8


            //backend format indian number +917917854563 thats why takelast 10 digits
            val RetrivePhn = RetireeUserRetrive.nextOfKinPhoneNumber

            var WithoutCC = RetrivePhn!!.takeLast(10)

            var RetriveCC = RetrivePhn.substring(1 until RetrivePhn.length - WithoutCC.length)

            var RetriveCCInt = RetriveCC.toInt()

            et_retiree_next_kin_phone.setText(WithoutCC) //9

            retiree_next_kin_phone_ccp.setCountryForPhoneCode(RetriveCCInt)

            et_retiree_next_kin_address.setText(RetireeUserRetrive.nextOfKinAddress) //10

            et_retiree_kin_pincode.setText(RetireeUserRetrive.kinPincode)

            et_retiree_date_appointment.setText(RetireeUserRetrive.dateOfAppointment) //11

            et_retiree_last_promotion.setText(RetireeUserRetrive.lastPromotionYear) //12

            et_retiree_date_retirement.setText(RetireeUserRetrive.dateOfRetirement)  //13

            // 5 spinners, lga,loal pension, subtresury,gradelevel,position held lst

            //spinnerSet from Retrive RetireeUserRetrive

            //lga
            lgaS = RetireeUserRetrive.lga.toString()

            Log.d("spinner", "spinnerLgas $lgaS ")
            if (lgaS.isNotEmpty()) {
                var pos = lgaSpinnerAdapter.getPositionByName(lgaS)
                sp_retiree_lga.setSelection(pos)

                Log.d("spinner", "spinnerPos $pos ")
            } //14

            //local pension
            localPenBoardS = RetireeUserRetrive.localGovernmentPensionBoard.toString()
            if (localPenBoardS.isNotEmpty()) {
                var pos = localGovPensionAdapter.getPositionByName(localPenBoardS)
                sp_retiree_pension_board.setSelection(pos)
            } //15


            //sub tresury
            subS = RetireeUserRetrive.subTreasury.toString()
            if (subS.isNotEmpty()) {
                var pos = subTreasuryAdapter.getPositionByName(subS)
                sp_retiree_sub_treasury.setSelection(pos)

            } //16

            //gradelvl
            gradelvlS = RetireeUserRetrive.gradeLevel.toString()
            Log.d("LogGradelevel", "gradelvlS:$gradelvlS ")
            Log.d("logGradelevel", "gradelist : $GradeLevelsList")
            if (gradelvlS.isNotEmpty()) {
                Log.d("LogGradelevel", "gradelvlS.isNotEmpty():$gradelvlS")
                var Gradelvlpos = gradeLevelAdapter.getPositionByName(gradelvlS)
                Log.d("LogGradelevel", "Gradelvlpos:$Gradelvlpos ")
                sp_retiree_grade_level.setSelection(Gradelvlpos)
            } //17

            //positon last held
            lastPostionHeldS = RetireeUserRetrive.positionHeldLast.toString()
            if (lastPostionHeldS.isNotEmpty()) {

                var pos = lastPositionAdapter.getPositionByName(lastPostionHeldS)

                if (pos != -1) {
                    Log.d("positionlast", "spinnerlast: $pos $lastPostionHeldS")
                    sp_retiree_position_last.setSelection(pos)
                }else{
                    /*sp_active_occupation_type.setSelection(1)
                    et_active_occupation_other.visibility = View.VISIBLE
                    et_active_occupation_other.setText(occupationS)
                    occupation = occupationS*/

                    sp_retiree_position_last.setSelection(lastPositionAdapter.count -1)
                    et_retiree_position_other.visibility = View.VISIBLE
                    et_retiree_position_other.setText(lastPostionHeldS)

                    lastPosition = lastPostionHeldS

                }
                //sp_retiree_position_last.setSelection(lastPostionHeldS.toInt())

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
        sp_retiree_grade_level.onItemSelectedListener =
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
        sp_retiree_sub_treasury.onItemSelectedListener =
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
        sp_retiree_lga.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        sp_retiree_position_last.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    if (lastPositionList.get(position)?.id?.equals(0) == true) {

                    } else if (lastPositionList.get(position)?.id?.equals(-1) == true){


                        //visiblty show
                        et_retiree_position_other.visibility = View.VISIBLE
                        et_retiree_position_other.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                                // Code to handle text before changes are made
                            }

                            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                                // Code to handle text changes
                            }

                            override fun afterTextChanged(s: Editable) {
                                // Code to handle text after changes are made

                                lastPosition = et_retiree_position_other.text.toString()
                            }
                        })


                    }else{
                        et_retiree_position_other.visibility = View.GONE
                        lastPosition = lastPositionList[position]?.id.toString()
                    }
                }


                override fun onNothingSelected(parent: AdapterView<*>?) {
                    
                }

            }
    }

    private fun onlocalGovPensionTextwatcher() {
        sp_retiree_pension_board.onItemSelectedListener =
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

        et_retiree_firstName.addTextChangedListener(AlphabeticTextWatcher(et_retiree_firstName))
        et_retiree_middleName.addTextChangedListener(AlphabeticTextWatcher(et_retiree_middleName))
        et_retiree_lastName.addTextChangedListener(AlphabeticTextWatcher(et_retiree_lastName))
        et_retiree_next_kin.addTextChangedListener(AlphabeticTextWatcher(et_retiree_next_kin))


        onTextGradeLevelWatcher()
        onTextSubTresuryWatcher()
        onTextLgaWatcher()
        onLastPositionTextWatcher()
        onlocalGovPensionTextwatcher()


        radioGroup_retiree.setOnCheckedChangeListener { group, checkedId ->
            sex = if (R.id.rb_retiree_male == checkedId) "male" else "female"
        }

        et_retiree_DOB.setOnClickListener {
            showDatePickerPresentToPast(et_retiree_DOB, dateBirth)
        }

        et_retiree_date_appointment.setOnClickListener {
            showDatePickerPresentToPast(et_retiree_date_appointment, dateAppointment)
        }

        et_retiree_date_retirement.setOnClickListener {
            showDatePickerPresentToPast(et_retiree_date_retirement, dateRetirement)
        }


        ll_retiree_basicdetails_next.setOnClickListener {

            //nextButtonCall()
            if (isValidRetireeBasicDetails()) {

                nextButtonCall()
            }
        }


    }


    private fun isValidRetireeBasicDetails(): Boolean {

        //firstname
        if (TextUtils.isEmpty(et_retiree_firstName.text.trim())) {
            Toast.makeText(context, "Empty FirstName", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!NAME_PATTERN.matcher(et_retiree_firstName.text.trim()).matches()) {

            Toast.makeText(context, "first name not valid", Toast.LENGTH_SHORT).show()
            return false
        }
        //middlename
        /*if (TextUtils.isEmpty(et_retiree_middleName.text)) {
            Toast.makeText(context, "Empty Middle name", Toast.LENGTH_SHORT).show()
            return false
        }*/


        if (!NAME_PATTERN_OR_NULL.matcher(et_retiree_middleName.text.trim()).matches()) {

            Toast.makeText(context, "middle name not valid", Toast.LENGTH_SHORT).show()
            return false
        }


        //lastname
        if (TextUtils.isEmpty(et_retiree_lastName.text.trim())) {
            Toast.makeText(context, "Empty Last name", Toast.LENGTH_SHORT).show()
            return false
        }


        if (!NAME_PATTERN.matcher(et_retiree_lastName.text.trim()).matches()) {

            Toast.makeText(context, "last name not valid", Toast.LENGTH_SHORT).show()
            return false
        }


        //dob
        if (TextUtils.isEmpty(et_retiree_DOB.text)) {
            Toast.makeText(context, "select dob", Toast.LENGTH_SHORT).show()
            return false
        }

        //sex
        if (radioGroup_retiree.checkedRadioButtonId <= 0) {
            Toast.makeText(context, "Select Gender", Toast.LENGTH_SHORT).show()
            return false
        }

        //address
        if (TextUtils.isEmpty(et_retiree_address.text)) {
            Toast.makeText(context, "Empty Address", Toast.LENGTH_SHORT).show()
            return false
        }
       //pincode
        if (TextUtils.isEmpty(et_retiree_pincode.text)) {
            Toast.makeText(context, "Empty Pincode", Toast.LENGTH_SHORT).show()
            return false
        }


        //country spinner default nigeria selected, no condition check

        //LGA
        if (sp_retiree_lga.selectedItemPosition == 0 || (sp_retiree_lga.isEmpty())) {
            Toast.makeText(context, "Select valid lga item", Toast.LENGTH_SHORT).show()
            return false
        }

        //kin name
        if (TextUtils.isEmpty(et_retiree_next_kin.text)) {
            Toast.makeText(context, "Empty kin name", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!NAME_PATTERN.matcher(et_retiree_next_kin.text.trim()).matches()) {

            Toast.makeText(context, "kin name not valid", Toast.LENGTH_SHORT).show()
            return false
        }

        //kin email
        if (!et_retiree_next_kin_email.text.toString().isValidOptionalEmail()) {
            Toast.makeText(context, "kin email not valid", Toast.LENGTH_SHORT).show()
            return false
        }


    /*    if (!EMAIL_ADDRESS_PATTERN.matcher(et_retiree_next_kin_email.text.toString()).matches()) {

            Toast.makeText(context, "not valid kin email", Toast.LENGTH_SHORT).show()
            return false
        }*/


        //kin phone
        if (TextUtils.isEmpty(et_retiree_next_kin_phone.text)) {
            Toast.makeText(context, "Empty phone number", Toast.LENGTH_LONG).show()
            return false
        } else if ((!retiree_next_kin_phone_ccp.isValid)) {
            Toast.makeText(context, "Phone Number not valid", Toast.LENGTH_LONG).show()
            return false
        } else {
            //80655707
            Ph_no = "+" + retiree_next_kin_phone_ccp.fullNumber
            Log.d("retire_phn", "$Ph_no")
        }

        //kin address
        if (TextUtils.isEmpty(et_retiree_next_kin_address.text)) {
            Toast.makeText(context, "Empty kin Address", Toast.LENGTH_SHORT).show()
            return false
        }
        //kin Pincode
        if (TextUtils.isEmpty(et_retiree_kin_pincode.text)) {
            Toast.makeText(context, "Empty kin Pincode", Toast.LENGTH_SHORT).show()
            return false
        }

        //local pension board
        if (sp_retiree_pension_board.selectedItemPosition == 0 || (sp_retiree_pension_board.isEmpty())) {
            Toast.makeText(context, "Select valid local pension board", Toast.LENGTH_SHORT).show()
            return false
        }


        //sub tressury
        if (sp_retiree_sub_treasury.selectedItemPosition == 0 || (sp_retiree_sub_treasury.isEmpty())) {
            Toast.makeText(context, "Select valid sub treasury item", Toast.LENGTH_SHORT).show()
            return false
        }

        //date of appointment
        if (TextUtils.isEmpty(et_retiree_date_appointment.text)) {
            Toast.makeText(context, "select date appointment", Toast.LENGTH_SHORT).show()
            return false
        }

        //last promotion year
        if ((TextUtils.isEmpty(et_retiree_last_promotion.text)) || (et_retiree_last_promotion.text.length != 4)) {
            Log.d("yearlength", "${et_retiree_last_promotion.text.length} ")
            Toast.makeText(context, "last promotion year not valid", Toast.LENGTH_SHORT).show()
            return false
        }

        //grade level
        if (sp_retiree_grade_level.selectedItemPosition == 0 || (sp_retiree_grade_level.isEmpty())) {
            Toast.makeText(context, "select valid grade level item", Toast.LENGTH_SHORT).show()
            return false
        }


        //date of retirement
        if (TextUtils.isEmpty(et_retiree_date_retirement.text)) {
            Toast.makeText(context, "select date retirement", Toast.LENGTH_SHORT).show()
            return false
        }

        //postion last
        if (sp_retiree_position_last.selectedItemPosition == 0 || (sp_retiree_position_last.isEmpty())) {
            Toast.makeText(context, "select valid position last held", Toast.LENGTH_SHORT).show()
            return false
        }

        //postion last other
        if (et_retiree_position_other.visibility == View.VISIBLE && ((!NAME_PATTERN.matcher(
                et_retiree_position_other.text.toString()
            ).matches() || (TextUtils.isEmpty(et_retiree_position_other.text))))
        ) {
            Toast.makeText(context, "Enter other Position", Toast.LENGTH_SHORT).show()
            return false
        }

        return true

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


    private fun clearLogin() {
        prefs.isLogin = false
        prefs.user_id = ""
        prefs.user_name = ""
        prefs.email = ""
        prefs.access_token = ""
        prefs.refresh_token = ""
    }


    private fun nextButtonCall() {
        showLoader()
        if (NetworkUtils.isConnectedToNetwork(requireContext())) {

            prefs.Rfirst_name = et_retiree_firstName.text.trim().toString()
            prefs.Rmiddle_name = et_retiree_middleName.text.trim().toString()
            prefs.Rlast_name = et_retiree_lastName.text.trim().toString()
            accountDetailCall()


        } else {
            dismissLoader()
            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
        }

    }

    private fun accountDetailCall() {

        val doa: String
        val dob: String
        val dor: String

        if (dateAppointment.toString() == "") {
            doa = et_retiree_date_appointment.text.toString()
        } else {
            doa = dateAppointment.toString()
        }

        if (dateBirth.toString() == "") {
            dob = et_retiree_DOB.text.toString()
        } else {
            dob = dateBirth.toString()
        }

        if (dateRetirement.toString() == "") {
            dor = et_retiree_date_retirement.text.toString()
        } else {
            dor = dateRetirement.toString()
        }




//        retireeBasicDetailsViewModel.getAccountDetails(
        retireeBasicDetailsViewModel.submitBasicDetails(
            InputRetireeBasicDetails(
                positionHeldLastId = lastPosition, //lastPositionHeld.toString(),
                country = selected_country,
                address = et_retiree_address.text.toString(),
                subTreasuryId = subtreasury,
                sex = sex,
                lastName = et_retiree_lastName.text.trim().toString(),
                middleName = et_retiree_middleName.text.trim().toString(),
                dateOfRetirement = dor,
                dateOfAppointment = doa,
                lgaId = lgalist,
                nextOfKinAddress = et_retiree_next_kin_address.text.toString(),
                nextOfKinPhoneNumber = Ph_no,
                gradeLevel = gradelevel,
                userType = "retired",
                dob = dob,
                nextOfKinName = et_retiree_next_kin.text.trim().toString(),
                localGovernmentPensionBoardId = localGovernmentPensionBoardId ,
                lastPromotionYear = et_retiree_last_promotion.text.toString().toInt(),
                nextOfKinEmail = et_retiree_next_kin_email.text.toString(),
                firstName = et_retiree_firstName.text.trim().toString(),
                pincode = et_retiree_pincode.text.toString(),
                kinPincode = et_retiree_kin_pincode.text.toString()
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
            combinationdetailsdata.combinedetails?.combinelocalGovenmentPensionBoards.forEach {
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