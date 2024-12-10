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
import com.example.engu_pension_verification_application.databinding.FragmentRetireeBasicDetailsBinding
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList
import java.util.Calendar
import java.util.regex.Pattern

class RetireeBasicDetailsFragment : BaseFragment() {
    companion object {
        const val TAB_POSITION = 0
    }
    private lateinit var binding:FragmentRetireeBasicDetailsBinding
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

            binding.etRetireeLastPromotion.setText(RetireeUserRetrive.lastPromotionYear) //12

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
            showDatePickerPresentToPast(binding.etRetireeDOB, dateBirth)
        }

        binding.etRetireeDateAppointment.setOnClickListener {
            showDatePickerPresentToPast(binding.etRetireeDateAppointment, dateAppointment)
        }

        binding.etRetireeDateRetirement.setOnClickListener {
            showDatePickerPresentToPast(binding.etRetireeDateRetirement, dateRetirement)
        }


        binding.llRetireeBasicdetailsNext.setOnClickListener {

            //nextButtonCall()
            if (isValidRetireeBasicDetails()) {

                nextButtonCall()
            }
        }


    }


    private fun isValidRetireeBasicDetails(): Boolean {

        //firstname
        if (TextUtils.isEmpty(binding.etRetireeFirstName.text.trim())) {
            Toast.makeText(context, "Empty FirstName", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!NAME_PATTERN.matcher(binding.etRetireeFirstName.text.trim()).matches()) {

            Toast.makeText(context, "first name not valid", Toast.LENGTH_SHORT).show()
            return false
        }
        //middlename
        /*if (TextUtils.isEmpty(binding.etRetireeMiddleName.text)) {
            Toast.makeText(context, "Empty Middle name", Toast.LENGTH_SHORT).show()
            return false
        }*/


        if (!NAME_PATTERN_OR_NULL.matcher(binding.etRetireeMiddleName.text.trim()).matches()) {

            Toast.makeText(context, "middle name not valid", Toast.LENGTH_SHORT).show()
            return false
        }


        //lastname
        if (TextUtils.isEmpty(binding.etRetireeLastName.text.trim())) {
            Toast.makeText(context, "Empty Last name", Toast.LENGTH_SHORT).show()
            return false
        }


        if (!NAME_PATTERN.matcher(binding.etRetireeLastName.text.trim()).matches()) {

            Toast.makeText(context, "last name not valid", Toast.LENGTH_SHORT).show()
            return false
        }


        //dob
        if (TextUtils.isEmpty(binding.etRetireeDOB.text)) {
            Toast.makeText(context, "select dob", Toast.LENGTH_SHORT).show()
            return false
        }

        //sex
        if (binding.radioGroupRetiree.checkedRadioButtonId <= 0) {
            Toast.makeText(context, "Select Gender", Toast.LENGTH_SHORT).show()
            return false
        }

        //address
        if (TextUtils.isEmpty(binding.etRetireeAddress.text)) {
            Toast.makeText(context, "Empty Address", Toast.LENGTH_SHORT).show()
            return false
        }
       //pincode
        if (TextUtils.isEmpty(binding.etRetireePincode.text)) {
            Toast.makeText(context, "Empty Pincode", Toast.LENGTH_SHORT).show()
            return false
        }


        //country spinner default nigeria selected, no condition check

        //LGA
        if (binding.spRetireeLga.selectedItemPosition == 0 || (binding.spRetireeLga.isEmpty())) {
            Toast.makeText(context, "Select valid lga item", Toast.LENGTH_SHORT).show()
            return false
        }

        //kin name
        if (TextUtils.isEmpty(binding.etRetireeNextKin.text)) {
            Toast.makeText(context, "Empty kin name", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!NAME_PATTERN.matcher(binding.etRetireeNextKin.text.trim()).matches()) {

            Toast.makeText(context, "kin name not valid", Toast.LENGTH_SHORT).show()
            return false
        }

        //kin email
        if (!binding.etRetireeNextKinEmail.text.toString().isValidOptionalEmail()) {
            Toast.makeText(context, "kin email not valid", Toast.LENGTH_SHORT).show()
            return false
        }


    /*    if (!EMAIL_ADDRESS_PATTERN.matcher(binding.etRetireeNextKinEmail.text.toString()).matches()) {

            Toast.makeText(context, "not valid kin email", Toast.LENGTH_SHORT).show()
            return false
        }*/


        //kin phone
        if (TextUtils.isEmpty(binding.etRetireeNextKinPhone.text)) {
            Toast.makeText(context, "Empty phone number", Toast.LENGTH_LONG).show()
            return false
        } else if ((!binding.retireeNextKinPhoneCcp.isValid)) {
            Toast.makeText(context, "Phone Number not valid", Toast.LENGTH_LONG).show()
            return false
        } else {
            //80655707
            Ph_no = "+" + binding.retireeNextKinPhoneCcp.fullNumber
            Log.d("retire_phn", "$Ph_no")
        }

        //kin address
        if (TextUtils.isEmpty(binding.etRetireeNextKinAddress.text)) {
            Toast.makeText(context, "Empty kin Address", Toast.LENGTH_SHORT).show()
            return false
        }
        //kin Pincode
        if (TextUtils.isEmpty(binding.etRetireeKinPincode.text)) {
            Toast.makeText(context, "Empty kin Pincode", Toast.LENGTH_SHORT).show()
            return false
        }

        //local pension board
        if (binding.spRetireePensionBoard.selectedItemPosition == 0 || (binding.spRetireePensionBoard.isEmpty())) {
            Toast.makeText(context, "Select valid local pension board", Toast.LENGTH_SHORT).show()
            return false
        }


        //sub tressury
        if (binding.spRetireeSubTreasury.selectedItemPosition == 0 || (binding.spRetireeSubTreasury.isEmpty())) {
            Toast.makeText(context, "Select valid sub treasury item", Toast.LENGTH_SHORT).show()
            return false
        }

        //date of appointment
        if (TextUtils.isEmpty(binding.etRetireeDateAppointment.text)) {
            Toast.makeText(context, "select date appointment", Toast.LENGTH_SHORT).show()
            return false
        }

        //last promotion year
        if ((TextUtils.isEmpty(binding.etRetireeLastPromotion.text)) || (binding.etRetireeLastPromotion.text.length != 4)) {
            Log.d("yearlength", "${binding.etRetireeLastPromotion.text.length} ")
            Toast.makeText(context, "last promotion year not valid", Toast.LENGTH_SHORT).show()
            return false
        }

        //grade level
        if (binding.spRetireeGradeLevel.selectedItemPosition == 0 || (binding.spRetireeGradeLevel.isEmpty())) {
            Toast.makeText(context, "select valid grade level item", Toast.LENGTH_SHORT).show()
            return false
        }


        //date of retirement
        if (TextUtils.isEmpty(binding.etRetireeDateRetirement.text)) {
            Toast.makeText(context, "select date retirement", Toast.LENGTH_SHORT).show()
            return false
        }

        //postion last
        if (binding.spRetireePositionLast.selectedItemPosition == 0 || (binding.spRetireePositionLast.isEmpty())) {
            Toast.makeText(context, "select valid position last held", Toast.LENGTH_SHORT).show()
            return false
        }

        //postion last other
        if (binding.etRetireePositionOther.visibility == View.VISIBLE && ((!NAME_PATTERN.matcher(
                binding.etRetireePositionOther.text.toString()
            ).matches() || (TextUtils.isEmpty(binding.etRetireePositionOther.text))))
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

        val doa: String
        val dob: String
        val dor: String

        if (dateAppointment.toString() == "") {
            doa = binding.etRetireeDateAppointment.text.toString()
        } else {
            doa = dateAppointment.toString()
        }

        if (dateBirth.toString() == "") {
            dob = binding.etRetireeDOB.text.toString()
        } else {
            dob = dateBirth.toString()
        }

        if (dateRetirement.toString() == "") {
            dor = binding.etRetireeDateRetirement.text.toString()
        } else {
            dor = dateRetirement.toString()
        }




//        retireeBasicDetailsViewModel.getAccountDetails(
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
                lastPromotionYear = binding.etRetireeLastPromotion.text.toString().toInt(),
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