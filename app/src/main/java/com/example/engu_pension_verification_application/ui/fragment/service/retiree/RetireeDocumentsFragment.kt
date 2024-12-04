package com.example.engu_pension_verification_application.ui.fragment.service.retiree

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.commons.setDocumentView
import com.example.engu_pension_verification_application.commons.setDocumentViewIfPresent
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.databinding.FragmentRetireeBinding
import com.example.engu_pension_verification_application.databinding.FragmentRetireeDocumentsBinding
import com.example.engu_pension_verification_application.model.response.ResponseRetireeDocRetrive
import com.example.engu_pension_verification_application.model.response.ResponseRetireeDocUpload
import com.example.engu_pension_verification_application.model.response.RetireeFileUrlResponse
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.activity.WebView.ActiveDocWebViewActivity
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.NetworkUtils
import com.example.engu_pension_verification_application.util.OnboardingStage
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.RetireeDocumentsViewModel
import com.example.engu_pension_verification_application.viewmodel.RetireeServiceViewModel
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class RetireeDocumentsFragment : BaseFragment(), View.OnClickListener {
    private lateinit var binding:FragmentRetireeDocumentsBinding
    val prefs = SharedPref

    val mimeTypes = arrayOf("image/jpeg", "image/png", "application/pdf")

    private lateinit var retireeDocumentsViewModel: RetireeDocumentsViewModel
    private val retireeServiceViewModel by activityViewModels<RetireeServiceViewModel>()
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2


    companion object {
        const val TAB_POSITION = 1

        const val RETIREE_APPLICATION_FORM_FILE = 201
        const val RETIREE_APPOINTMENT_PAYMENT_FILE = 202
        const val RETIREE_AUTHORISATION_PAYMENT_FILE = 203
        const val RETIREE_CLEARANCE_FORM_FILE = 204
        const val RETIREE_NOTIFICATION_PROMOTION_FILE = 205
        const val RETIREE_PENSION_CERTIFICATE_FILE = 206
        const val RETIREE_PASSPORT_PHOTO_FILE = 207
        const val RETIREE_RETIREMENT_NOTICE_FILE = 208
        const val RETIREE_ID_CARD_FILE = 209

    }
    //RetireeRetrive Int
    var RetireeUserDocRetrive: RetireeFileUrlResponse? = null
    var responseRetireeDocRetrive: ResponseRetireeDocRetrive? = null


    private var passportPhotoUri : Uri? = null
    private var idCardUri : Uri? = null
    private var applicationFormUri: Uri? = null
    private var appointmentPayUri : Uri? = null
    private var authorisationPayUri : Uri? = null
    private var clearanceFormUri : Uri? = null
    private var notificationUri : Uri? = null
    private var pensionCertificateUri : Uri? = null
    private var retirementNoticeUri : Uri? = null

    var application_form_file: File? = null
    var application_form_filename = ""
    var application_form_fileMIME = ""

    var monthly_arrears_payment_appointment_file: File? = null
    var monthly_arrears_payment_appointment_filename = ""
    var monthly_arrears_payment_appointment_fileMIME = ""

    var payment_authorization_retirement_or_death_file: File? = null
    var payment_authorization_retirement_or_death_filename = ""
    var payment_authorization_retirement_or_death_fileMIME = ""

    var clearance_form_file: File? = null
    var clearance_form_filename = ""
    var clearance_form_fileMIME = ""

    var notification_promotion_file: File? = null
    var notification_promotion_filename = ""
    var notification_promotion_fileMIME = ""

    var pension_life_certificate_file: File? = null
    var pension_life_certificate_filename = ""
    var pension_life_certificate_fileMIME = ""

    var passport_photo_file: File? = null
    var passport_photo_filename = ""
    var passport_photo_fileMIME = ""


    var retirement_notice_file: File? = null
    var retirement_notice_filename = ""
    var retirement_notice_fileMIME = ""

    var id_card_file: File? = null
    var id_card_filename = ""
    var id_card_fileMIME = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRetireeDocumentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        initViews()
        observeLiveData()
    }
    private fun initViews() {
        binding.llRetireeAppFormUpload.setOnClickListener(this)
        binding.imgRetireeAppFormClose.setOnClickListener(this)

        binding.llRetireeAppointmentPaymentDocUpload.setOnClickListener(this)
        binding.imgRetireeAppointmentPaymentDocClose.setOnClickListener(this)

        binding.llRetireeAuthorisationPaymentDocUpload.setOnClickListener(this)
        binding.imgRetireeAuthorisationPaymentDocClose.setOnClickListener(this)

        binding.llRetireeClearanceFormUpload.setOnClickListener(this)
        binding.llRetireeClearanceFormUpload.setOnClickListener(this)

        binding.llRetireeNotificationPromotionDocUpload.setOnClickListener(this)
        binding.llRetireeNotificationPromotionDocUpload.setOnClickListener(this)

        binding.llRetireePensionCertificateUpload.setOnClickListener(this)
        binding.imgRetireePensionCertificateDocClose.setOnClickListener(this)

        binding.llRetireePassportPhotoUpload.setOnClickListener(this)
        binding.imgRetireePassportPhotoDocClose.setOnClickListener(this)

        binding.llRetireeRetirementNoticeUpload.setOnClickListener(this)
        binding.imgRetireeRetirementNoticeDocClose.setOnClickListener(this)

        binding.llRetireeIdCardUpload.setOnClickListener(this)
        binding.imgRetireeIdCardDocClose.setOnClickListener(this)

        binding.llRetireeDocNext.setOnClickListener(this)

        binding.retireeAppBtnGreenView.setOnClickListener(this)
        binding.retireeAppointmentBtnGreenView.setOnClickListener(this)
        binding.retireeAuthorisationBtnGreenView.setOnClickListener(this)
        binding.retireeClearanceBtnGreenView.setOnClickListener(this)
        binding.retireeNotificationBtnGreenView.setOnClickListener(this)
        binding.retireePensionCertificateBtnGreenView.setOnClickListener(this)
        binding.retireePassportPhotoBtnGreenView.setOnClickListener(this)
        binding.retireeRetirementBtnGreenView.setOnClickListener(this)
        binding.retireeIdCardBtnGreenView.setOnClickListener(this)

    }

    private fun initViewModels() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        retireeDocumentsViewModel = ViewModelProviders.of(
            this,
            EnguViewModelFactory(networkRepo)
        ).get(RetireeDocumentsViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), 
            EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }
    private fun observeLiveData() {
        retireeServiceViewModel.currentTabPos.observe(viewLifecycleOwner){
            if (it == TAB_POSITION) RetireeDocRetrivecall()
        }
        retireeDocumentsViewModel.documentsFetchResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
//                dismissLoader()
                RetireeUserDocRetrive = response.detail.fileUrlResponse
                responseRetireeDocRetrive = response
                populateViews()
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            retireeDocumentsViewModel.fetchDocuments()
                        }
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        retireeDocumentsViewModel.documentsUploadResult.observe(viewLifecycleOwner) { pair ->
            dismissLoader()
            val request = pair.first
            val response = pair.second
            if (response.detail?.status == AppConstants.SUCCESS) {
                onDocUploadSuccess(response)
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            retireeDocumentsViewModel.uploadDocuments(request)
                        }
                    }
                } else {
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun RetireeDocRetrivecall() {
        //showLoader()
        if (NetworkUtils.isConnectedToNetwork(requireContext())) {


            retireeDocumentsViewModel.fetchDocuments()

            //dismissLoader()


        } else {
            dismissLoader()
            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
        }
    }

    override fun onClick(it: View?) {
        when (it) {

            //App Form
            binding.llRetireeAppFormUpload -> {
                selectPdfJpegPng(RETIREE_APPLICATION_FORM_FILE)
                binding.retireeAppBtnGreenView.visibility = View.GONE
                binding.cvRetireeApplicationFormDoc.visibility = View.GONE
            }
            binding.imgRetireeAppFormClose -> {
                binding.retireeAppBtnGreenView.visibility = View.GONE
                binding.cvRetireeApplicationFormDoc.visibility = View.GONE
            }
            //APPOINTMENT_PAYMENT
            binding.llRetireeAppointmentPaymentDocUpload -> {
                selectPdfJpegPng(RETIREE_APPOINTMENT_PAYMENT_FILE)
                binding.retireeAppointmentBtnGreenView.visibility = View.GONE
                binding.cvRetireeAppointmentPaymentDoc.visibility = View.GONE
            }
            binding.imgRetireeAppointmentPaymentDocClose -> {
                binding.retireeAppointmentBtnGreenView.visibility = View.GONE
                binding.cvRetireeAppointmentPaymentDoc.visibility = View.GONE
            }
            //RETIREE_AUTHORISATION_PAYMENT
            binding.llRetireeAuthorisationPaymentDocUpload -> {
                binding.retireeAuthorisationBtnGreenView.visibility=View.GONE
                binding.cvRetireeAuthorisationPaymentDoc.visibility = View.GONE
                selectPdfJpegPng(RETIREE_AUTHORISATION_PAYMENT_FILE)
            }
            binding.imgRetireeAuthorisationPaymentDocClose -> {
                binding.retireeAuthorisationBtnGreenView.visibility=View.GONE
                binding.cvRetireeAuthorisationPaymentDoc.visibility = View.GONE
            }
            //RETIREE_CLEARANCE_FORM
            binding.llRetireeClearanceFormUpload -> {
                binding.retireeClearanceBtnGreenView.visibility = View.GONE
                binding.cvRetireeClearanceFormDoc.visibility = View.GONE
                selectPdfJpegPng(RETIREE_CLEARANCE_FORM_FILE)
            }
            binding.llRetireeClearanceFormUpload -> {
                binding.retireeClearanceBtnGreenView.visibility = View.GONE
                binding.cvRetireeClearanceFormDoc.visibility = View.GONE
            }
            //RETIREE_NOTIFICATION_PROMOTION
            binding.llRetireeNotificationPromotionDocUpload -> {
                binding.retireeNotificationBtnGreenView.visibility = View.GONE
                binding.cvRetireeNotificationPromotionDoc.visibility = View.GONE
                selectPdfJpegPng(RETIREE_NOTIFICATION_PROMOTION_FILE)
            }
            binding.llRetireeNotificationPromotionDocUpload -> {
                binding.retireeNotificationBtnGreenView.visibility = View.GONE
                binding.cvRetireeNotificationPromotionDoc.visibility = View.GONE
            }
            //RETIREE_PENSION_CERTIFICATE
            binding.llRetireePensionCertificateUpload -> {
                binding.retireePensionCertificateBtnGreenView.visibility =View.GONE
                binding.cvRetireePensionCertificateDoc.visibility = View.GONE
                selectPdfJpegPng(RETIREE_PENSION_CERTIFICATE_FILE)
            }
            binding.imgRetireePensionCertificateDocClose ->{
                binding.retireePensionCertificateBtnGreenView.visibility =View.GONE
                binding.cvRetireePensionCertificateDoc.visibility = View.GONE
            }
            //RETIREE_PASSPORT_PHOTO
            binding.llRetireePassportPhotoUpload -> {
                binding.retireePassportPhotoBtnGreenView.visibility= View.GONE
                binding.cvRetireePassportPhotoDoc.visibility = View.GONE
                selectPdfJpegPng(RETIREE_PASSPORT_PHOTO_FILE)
            }
            binding.imgRetireePassportPhotoDocClose ->{
                binding.retireePassportPhotoBtnGreenView.visibility= View.GONE
                binding.cvRetireePassportPhotoDoc.visibility = View.GONE
            }
            //RETIREE_RETIREMENT_NOTICE
            binding.llRetireeRetirementNoticeUpload -> {
                binding.retireeRetirementBtnGreenView.visibility = View.GONE
                binding.cvRetireeRetirementNoticeDoc.visibility = View.GONE
                selectPdfJpegPng(RETIREE_RETIREMENT_NOTICE_FILE)
            }
            binding.imgRetireeRetirementNoticeDocClose ->{
                binding.retireeRetirementBtnGreenView.visibility = View.GONE
                binding.cvRetireeRetirementNoticeDoc.visibility = View.GONE
            }
            //RETIREE_ID_CARD
            binding.llRetireeIdCardUpload -> {
                binding.retireeIdCardBtnGreenView.visibility = View.GONE
                binding.cvRetireeIdCardDoc.visibility = View.GONE
                selectPdfJpegPng(RETIREE_ID_CARD_FILE)
            }
            binding.imgRetireeIdCardDocClose -> {
                binding.retireeIdCardBtnGreenView.visibility = View.GONE
                binding.cvRetireeIdCardDoc.visibility = View.GONE
            }

            binding.llRetireeDocNext -> {
                if (!RetireeUserDocRetrive?.idCardFileUrl.isNullOrEmpty()) {
                    if (prefs.onboardingStage == OnboardingStage.RETIREE_DOCUMENTS)
                        prefs.onboardingStage = OnboardingStage.RETIREE_BANK_INFO
                    retireeServiceViewModel.moveToNextTab()
                    retireeServiceViewModel.refreshTabsState()
                }else if (isValidDocs()) {
                    Log.d("Debug", "isValidDocs is true")
                    nextButtonCall()
                } else {
                    Log.d("Debug", "isValidDocs is false")
                }
            }

            /*  viewPageCallBack.onViewMoveNext()*/
//RETIREE RETRIVE CALL


            /*
            //binding.retireeIdCardBtnGreenView -> viewButtonCall(RetireeUserDocRetrive?.idCardFileUrl)
            binding.retireeIdCardBtnGreenView -> if(!RetireeUserDocRetrive?.idCardFileUrl.isNullOrEmpty()){

                Log.d("viewbutton", " R id cardurl ${RetireeUserDocRetrive?.idCardFileUrl}")

                startActivityWebViewFromUrl(RetireeUserDocRetrive?.idCardFileUrl)
            }else{
                Log.d("viewbutton", " R id card uri  ${idCardUri}")
                Log.d("viewbutton", " R id card url ${RetireeUserDocRetrive?.idCardFileUrl}")
                idCardUri?.let { it1 -> startActivityFromUri(it1) }
            }
            //binding.retireePassportPhotoBtnGreenView -> viewButtonCall(RetireeUserDocRetrive?.passportPhotoFileUrl)
            binding.retireePassportPhotoBtnGreenView -> if(!RetireeUserDocRetrive?.passportPhotoFileUrl.isNullOrEmpty()){
                startActivityWebViewFromUrl(RetireeUserDocRetrive?.passportPhotoFileUrl)
            }else{
                passportPhotoUri?.let { it1 -> startActivityFromUri(it1) }
            }
            //binding.retireeAppBtnGreenView -> viewButtonCall(RetireeUserDocRetrive?.applicationFormFileUrl)
            binding.retireeAppBtnGreenView ->
                if(!RetireeUserDocRetrive?.applicationFormFileUrl.isNullOrEmpty()){
                startActivityWebViewFromUrl(RetireeUserDocRetrive?.applicationFormFileUrl)
            }else{
                applicationFormUri?.let { it1 -> startActivityFromUri(it1) }
            }
            //binding.retireeAppointmentBtnGreenView -> viewButtonCall(RetireeUserDocRetrive?.monthlyArrearsPaymentAppointmentFileUrl)
            binding.retireeAppointmentBtnGreenView -> if(!RetireeUserDocRetrive?.monthlyArrearsPaymentAppointmentFileUrl.isNullOrEmpty()){
                startActivityWebViewFromUrl(RetireeUserDocRetrive?.monthlyArrearsPaymentAppointmentFileUrl)
            }else{
                appointmentPayUri?.let { it1 -> startActivityFromUri(it1) }
            }

            //binding.retireeAuthorisationBtnGreenView -> viewButtonCall(RetireeUserDocRetrive?.paymentAuthorizationRetirementOrDeathFileUrl)
            binding.retireeAuthorisationBtnGreenView -> if(!RetireeUserDocRetrive?.paymentAuthorizationRetirementOrDeathFileUrl.isNullOrEmpty()){
                startActivityWebViewFromUrl(RetireeUserDocRetrive?.paymentAuthorizationRetirementOrDeathFileUrl)
            }else{
                authorisationPayUri?.let { it1 -> startActivityFromUri(it1) }
            }
            //binding.retireeClearanceBtnGreenView -> viewButtonCall(RetireeUserDocRetrive?.clearanceFormFileUrl)
            binding.retireeClearanceBtnGreenView -> if(!RetireeUserDocRetrive?.clearanceFormFileUrl.isNullOrEmpty()){
                startActivityWebViewFromUrl(RetireeUserDocRetrive?.clearanceFormFileUrl)
            }else{
                clearanceFormUri?.let { it1 -> startActivityFromUri(it1) }
            }
            //binding.retireeNotificationBtnGreenView -> viewButtonCall(RetireeUserDocRetrive?.promotionNotificationFileUrl)
            binding.retireeNotificationBtnGreenView -> if(!RetireeUserDocRetrive?.promotionNotificationFileUrl.isNullOrEmpty()){
                startActivityWebViewFromUrl(RetireeUserDocRetrive?.promotionNotificationFileUrl)
            }else{
                notificationUri?.let { it1 -> startActivityFromUri(it1) }
            }

            //binding.retireePensionCertificateBtnGreenView -> viewButtonCall(RetireeUserDocRetrive?.pensionLifeCertificateFileUrl)
            binding.retireePensionCertificateBtnGreenView -> if(!RetireeUserDocRetrive?.pensionLifeCertificateFileUrl.isNullOrEmpty()){
                startActivityWebViewFromUrl(RetireeUserDocRetrive?.pensionLifeCertificateFileUrl)
            }else{
                pensionCertificateUri?.let { it1 -> startActivityFromUri(it1) }
            }


            //binding.retireeRetirementBtnGreenView -> viewButtonCall(RetireeUserDocRetrive?.retirementNoticeFileUrl)
            binding.retireeRetirementBtnGreenView -> if(!RetireeUserDocRetrive?.retirementNoticeFileUrl.isNullOrEmpty()){
                startActivityWebViewFromUrl(RetireeUserDocRetrive?.retirementNoticeFileUrl)
            }else{
                retirementNoticeUri?.let { it1 -> startActivityFromUri(it1) }
            }
            */

            binding.retireeIdCardBtnGreenView -> {
                if (idCardUri != null) {
                    Log.d("viewbutton", "R id card uri ${idCardUri}")
                    startActivityFromUri(idCardUri!!)
                } else if (!RetireeUserDocRetrive?.idCardFileUrl.isNullOrEmpty()) {
                    Log.d("viewbutton", "R id card url ${RetireeUserDocRetrive?.idCardFileUrl}")
                    startActivityWebViewFromUrl(RetireeUserDocRetrive?.idCardFileUrl)
                }
            }
            binding.retireePassportPhotoBtnGreenView -> {
                if (passportPhotoUri != null) {
                    startActivityFromUri(passportPhotoUri!!)
                } else if (!RetireeUserDocRetrive?.passportPhotoFileUrl.isNullOrEmpty()) {
                    startActivityWebViewFromUrl(RetireeUserDocRetrive?.passportPhotoFileUrl)
                }
            }
            binding.retireeAppBtnGreenView -> {
                Log.i("sssssssssssss", "onClick: 1) $applicationFormUri 2) ${RetireeUserDocRetrive?.applicationFormFileUrl}")
                if (applicationFormUri != null) {
                    startActivityFromUri(applicationFormUri!!)
                } else if (!RetireeUserDocRetrive?.applicationFormFileUrl.isNullOrEmpty()) {
                    startActivityWebViewFromUrl(RetireeUserDocRetrive?.applicationFormFileUrl)
                }
            }
            binding.retireeAppointmentBtnGreenView -> {
                if (appointmentPayUri != null) {
                    startActivityFromUri(appointmentPayUri!!)
                } else if (!RetireeUserDocRetrive?.monthlyArrearsPaymentAppointmentFileUrl.isNullOrEmpty()) {
                    startActivityWebViewFromUrl(RetireeUserDocRetrive?.monthlyArrearsPaymentAppointmentFileUrl)
                }
            }
            binding.retireeAuthorisationBtnGreenView -> {
                if (authorisationPayUri != null) {
                    startActivityFromUri(authorisationPayUri!!)
                } else if (!RetireeUserDocRetrive?.paymentAuthorizationRetirementOrDeathFileUrl.isNullOrEmpty()) {
                    startActivityWebViewFromUrl(RetireeUserDocRetrive?.paymentAuthorizationRetirementOrDeathFileUrl)
                }
            }
            binding.retireeClearanceBtnGreenView -> {
                if (clearanceFormUri != null) {
                    startActivityFromUri(clearanceFormUri!!)
                } else if (!RetireeUserDocRetrive?.clearanceFormFileUrl.isNullOrEmpty()) {
                    startActivityWebViewFromUrl(RetireeUserDocRetrive?.clearanceFormFileUrl)
                }
            }
            binding.retireeNotificationBtnGreenView -> {
                if (notificationUri != null) {
                    startActivityFromUri(notificationUri!!)
                } else if (!RetireeUserDocRetrive?.promotionNotificationFileUrl.isNullOrEmpty()) {
                    startActivityWebViewFromUrl(RetireeUserDocRetrive?.promotionNotificationFileUrl)
                }
            }
            binding.retireePensionCertificateBtnGreenView -> {
                if (pensionCertificateUri != null) {
                    startActivityFromUri(pensionCertificateUri!!)
                } else if (!RetireeUserDocRetrive?.pensionLifeCertificateFileUrl.isNullOrEmpty()) {
                    startActivityWebViewFromUrl(RetireeUserDocRetrive?.pensionLifeCertificateFileUrl)
                }
            }
            binding.retireeRetirementBtnGreenView -> {
                if (retirementNoticeUri != null) {
                    startActivityFromUri(retirementNoticeUri!!)
                } else if (!RetireeUserDocRetrive?.retirementNoticeFileUrl.isNullOrEmpty()) {
                    startActivityWebViewFromUrl(RetireeUserDocRetrive?.retirementNoticeFileUrl)
                }
            }
        }
    }

    private fun startActivityWebViewFromUrl(url: String?) {
        val intent = Intent(context, ActiveDocWebViewActivity::class.java)

        intent.putExtra("file url", url)
        startActivity(intent)
    }

    fun startActivityFromUri(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        val mimeType = context?.contentResolver?.getType(uri)
        intent.setDataAndType(uri, mimeType)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        try {
            startActivity(Intent.createChooser(intent, "Open file with"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No application found to open this file type.", Toast.LENGTH_LONG).show()
        }

    }

    private fun isAllViewBtnEnabled(): Boolean {

        if (binding.retireeAppBtnGreenView.visibility == View.VISIBLE && binding.retireeAppointmentBtnGreenView.visibility == View.VISIBLE && binding.retireeAuthorisationBtnGreenView.visibility == View.VISIBLE &&
            binding.retireeClearanceBtnGreenView.visibility == View.VISIBLE && binding.retireeNotificationBtnGreenView.visibility == View.VISIBLE &&
            binding.retireePensionCertificateBtnGreenView.visibility == View.VISIBLE && binding.retireePassportPhotoBtnGreenView.visibility == View.VISIBLE &&
            binding.retireeRetirementBtnGreenView.visibility == View.VISIBLE && binding.retireeIdCardBtnGreenView.visibility == View.VISIBLE  )
        {
            return true
        }
        return false

    }

    private fun isValidDocs(): Boolean {
        /*
        if (TextUtils.isEmpty(binding.tvRetireeAppFormFilename.text) || binding.cvRetireeApplicationFormDoc.visibility == View.GONE
            || binding.retireeAppBtnGreenView.visibility == View.VISIBLE ) {
            Toast.makeText(context, "Please select Application Form File", Toast.LENGTH_LONG).show()
            return false
        } else if (TextUtils.isEmpty(binding.tvRetireeAppointmentPaymentDocFilename.text) || binding.cvRetireeAppointmentPaymentDoc.visibility == View.GONE
            || binding.retireeAppointmentBtnGreenView.visibility == View.VISIBLE) {
            Toast.makeText(context, "Please select Appointment payment File", Toast.LENGTH_LONG)
                .show()
            return false
        } else if (TextUtils.isEmpty(binding.tvRetireeIdCardDocFilename.text) || binding.cvRetireeAuthorisationPaymentDoc.visibility == View.GONE
            || binding.retireeAuthorisationBtnGreenView.visibility == View.VISIBLE) {
            Toast.makeText(context, " Please select Authorization payment File", Toast.LENGTH_LONG)
                .show()
            return false
        } else if (TextUtils.isEmpty(binding.tvRetireePassportPhotoDocFilename.text) || binding.cvRetireeClearanceFormDoc.visibility == View.GONE
            || binding.retireeClearanceBtnGreenView.visibility == View.VISIBLE) {
            Toast.makeText(context, "Please select Clearance Form File ", Toast.LENGTH_LONG).show()
            return false
        } else if (TextUtils.isEmpty(binding.tvRetireeNotificationPromotionDocFilename.text) || binding.cvRetireeNotificationPromotionDoc.visibility == View.GONE
            || binding.retireeNotificationBtnGreenView.visibility == View.VISIBLE) {
            Toast.makeText(context, "Please select Notification Promotion File ", Toast.LENGTH_LONG)
                .show()
            return false
        } else if (TextUtils.isEmpty(binding.tvRetireePensionCertificateDocFilename.text) || binding.cvRetireePensionCertificateDoc.visibility == View.GONE
            || binding.retireePensionCertificateBtnGreenView.visibility == View.VISIBLE) {
            Toast.makeText(context, "Please select Pension Certification File ", Toast.LENGTH_LONG)
                .show()
            return false
        } else if (TextUtils.isEmpty(binding.tvRetireePassportPhotoDocFilename.text) || binding.cvRetireePassportPhotoDoc.visibility == View.GONE
            || binding.retireePassportPhotoBtnGreenView.visibility == View.VISIBLE) {
            Toast.makeText(context, "Please select Photo", Toast.LENGTH_LONG).show()
            return false
        } else if (TextUtils.isEmpty(binding.tvRetireeRetirementNoticeDocFilename.text) || binding.cvRetireeRetirementNoticeDoc.visibility == View.GONE
            || binding.retireeRetirementBtnGreenView.visibility == View.VISIBLE) {
            Toast.makeText(context, "Please select Retirement Notice File", Toast.LENGTH_LONG)
                .show()
            return false
        } else if (TextUtils.isEmpty(binding.tvRetireeIdCardDocFilename.text) || binding.cvRetireeIdCardDoc.visibility == View.GONE
            || binding.retireeIdCardBtnGreenView.visibility == View.VISIBLE) {
            Toast.makeText(context, "Please select Id Card ", Toast.LENGTH_LONG).show()
            return false
        }*/


        if (TextUtils.isEmpty(binding.tvRetireeIdCardDocFilename.text) || binding.cvRetireeIdCardDoc.visibility == View.GONE) {
            Toast.makeText(context, "Please select Id Card", Toast.LENGTH_LONG).show()
            return false
        }
        /*else if (TextUtils.isEmpty(binding.tvRetireePassportPhotoDocFilename.text) || binding.cvRetireePassportPhotoDoc.visibility == View.GONE) {
            Toast.makeText(context, "Please select PassPort Photo", Toast.LENGTH_LONG).show()
            return false
        }*/


        return true

    }

    private fun nextButtonCall() {
        if (NetworkUtils.isConnectedToNetwork(requireContext())) {
            showLoader()
            retireedocUploadCall2()
        } else {
            dismissLoader()
            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
        }
    }

    private fun retireedocUploadCall2() {


        val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)

        // Mandatory parts
        multipartBuilder.addFormDataPart(
            "id_card_file",
            id_card_filename!!,
            id_card_file!!.asRequestBody(
                id_card_fileMIME.toMediaTypeOrNull())
        )


        //optional

        if (passport_photo_file != null){
            multipartBuilder.addFormDataPart(
                "passport_photo_file",
                passport_photo_filename!!,
                passport_photo_file!!.asRequestBody(
                    passport_photo_fileMIME.toMediaTypeOrNull())
            )
        }
        if(application_form_file != null){
        multipartBuilder.addFormDataPart(
                "application_form_file",
                application_form_filename!!,
                application_form_file!!.asRequestBody(
                    application_form_fileMIME.toMediaTypeOrNull())
            )
        }

        if (monthly_arrears_payment_appointment_file != null) {
            multipartBuilder.addFormDataPart(
                "monthly_arrears_payment_appointment_file",
                monthly_arrears_payment_appointment_filename!!,
                monthly_arrears_payment_appointment_file!!.asRequestBody(
                    monthly_arrears_payment_appointment_fileMIME.toMediaTypeOrNull()
                )
            )
        }

        if (payment_authorization_retirement_or_death_file != null) {
            multipartBuilder.addFormDataPart(
                "payment_authorization_retirement_or_death_file",
                payment_authorization_retirement_or_death_filename!!,
                payment_authorization_retirement_or_death_file!!.asRequestBody(
                    payment_authorization_retirement_or_death_fileMIME.toMediaTypeOrNull()
                )
            )
        }

        if (clearance_form_file != null) {
            multipartBuilder.addFormDataPart(
                "clearance_form_file",
                clearance_form_filename!!,
                clearance_form_file!!.asRequestBody(clearance_form_fileMIME.toMediaTypeOrNull())
            )
        }

        if (notification_promotion_file != null) {
            multipartBuilder.addFormDataPart(
                "promotion_notification_file",
                notification_promotion_filename!!,
                notification_promotion_file!!.asRequestBody(notification_promotion_fileMIME.toMediaTypeOrNull())
            )
        }
        if (pension_life_certificate_file != null) {
            multipartBuilder.addFormDataPart(
                "pension_life_certificate_file",
                pension_life_certificate_filename!!,
                pension_life_certificate_file!!.asRequestBody(pension_life_certificate_fileMIME.toMediaTypeOrNull())
            )
        }
        if (retirement_notice_file != null) {
            multipartBuilder.addFormDataPart(
                "retirement_notice_file",
                retirement_notice_filename!!,
                retirement_notice_file!!.asRequestBody(retirement_notice_fileMIME.toMediaTypeOrNull())
            )
        }


        // Build the RequestBody from the Builder
        val requestBody: RequestBody = multipartBuilder.build()

        retireeDocumentsViewModel.uploadDocuments(requestBody)
    }


    //only pdf
    /*private fun selectPdf(REQUEST_CODE: Int) {
        val pdfIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        pdfIntent.type = "application/pdf"
        pdfIntent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(pdfIntent, REQUEST_CODE)
    }*/

    private fun selectPdfJpegPng(REQUEST_CODE: Int) {
        val docIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        docIntent.type = "*/*"
        docIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(docIntent, REQUEST_CODE)
    }

    /*    fun getMimeType(filePath: String): String {
            val extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                ?: "application/octet-stream"
            //return "\"$mimeType\""
            return mimeType
        }*/

    fun getMimeType(filePath: String): String {


        val extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
        // Extract the extension from the filename
        val extension2 = filePath.substringAfterLast('.', "")
        var mimeTypeSecondary = ""

        when (extension2) {
            "jpg", "jpeg" -> mimeTypeSecondary = "image/jpeg"
            "png" -> mimeTypeSecondary = "image/png"
            "pdf" -> mimeTypeSecondary = "application/pdf"
        }
        val mimeType =
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: mimeTypeSecondary
        //return "\"$mimeType\""
        Log.d(
            "getMimeTag",
            " getfilepath : $filePath" + " getextension : $extension" + " getextension2 : $extension2" + " getmimeTypeSecondary : $mimeTypeSecondary" + " getmimeType : $mimeType"
        )
        return mimeType
    }


    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            RETIREE_APPLICATION_FORM_FILE -> if (resultCode == Activity.RESULT_OK) {
                binding.cvRetireeApplicationFormDoc.visibility = View.VISIBLE
                //pdfUri = data?.data!!
                val form_uri: Uri = data?.data!!

                applicationFormUri = form_uri

                //---------------------------------------------
                val parcelFileDescriptor = context?.contentResolver?.openFileDescriptor(
                    form_uri!!, "r", null
                ) ?: return
                val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                val file =
                    File(context?.cacheDir, context?.contentResolver?.getFileName(form_uri!!))
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)

                // Retrieve and format file size
                val fileSize = getFileSize(form_uri)
                val formattedFileSize = formatFileSize(fileSize)
                //progress_bar.progress = 0
                // val body = UploadRequestBody(file,"image",this)
                Log.d("file.name", "onActivityResult: " + file.name)

                // val body = UploadRequestBody(file,"image",this)

                // 5000 kb to5 mb
                //10000 kb = 10 mb
                // val f: File = File(path2)
                Log.e("File", file.toString())

                val length = file.length()
                Log.e("Length", length.toString())
                val fileSizeInMB = length / 1024
                Log.e("fileSizeInMB", fileSizeInMB.toString())

                if (fileSizeInMB > 2000) {  //2000 kb = 2 mb
                    Toast.makeText(
                        requireContext(), "Can't Upload file larger than 2mb", Toast.LENGTH_LONG
                    ).show()
                } else {

                    binding.tvRetireeAppFormFilename.text = file.name

                    application_form_file = file

                    application_form_fileMIME = getMimeType(application_form_file.toString())

                    Log.d("doc upload", "$application_form_file")
                    application_form_filename = file.name
                    updateProgressBar(
                        fileSizeInMB, formattedFileSize, RETIREE_APPLICATION_FORM_FILE
                    )
                    // Only accept size with in 5 mb
                    Log.d("form_pdffile", "onActivityResult: " + file.name)


                }

            }

            RETIREE_APPOINTMENT_PAYMENT_FILE -> if (resultCode == Activity.RESULT_OK) {
                binding.cvRetireeAppointmentPaymentDoc.visibility = View.VISIBLE
                //pdfUri = data?.data!!
                val form_uri: Uri = data?.data!!

                appointmentPayUri = form_uri

                //---------------------------------------------
                val parcelFileDescriptor = context?.contentResolver?.openFileDescriptor(
                    form_uri!!, "r", null
                ) ?: return
                val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                val file =
                    File(context?.cacheDir, context?.contentResolver?.getFileName(form_uri!!))
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)

                // Retrieve and format file size
                val fileSize = getFileSize(form_uri)
                val formattedFileSize = formatFileSize(fileSize)

                //progress_bar.progress = 0
                // val body = UploadRequestBody(file,"image",this)
                Log.d("file.name", "onActivityResult: " + file.name)

                // val body = UploadRequestBody(file,"image",this)

                // 5000 kb to5 mb
                //10000 kb = 10 mb
                // val f: File = File(path2)
                Log.e("File", file.toString())

                val length = file.length()
                Log.e("Length", length.toString())
                val fileSizeInMB = length / 1024
                Log.e("fileSizeInMB", fileSizeInMB.toString())

                if (fileSizeInMB > 2000) {  //2000 kb = 2 mb
                    Toast.makeText(
                        requireContext(), "Can't Upload file larger than 2mb", Toast.LENGTH_LONG
                    ).show()
                } else {

                    binding.tvRetireeAppointmentPaymentDocFilename.text = file.name

                    monthly_arrears_payment_appointment_file = file

                    monthly_arrears_payment_appointment_fileMIME =
                        getMimeType(monthly_arrears_payment_appointment_file.toString())

                    Log.d("doc upload", "$monthly_arrears_payment_appointment_file")
                    monthly_arrears_payment_appointment_filename = file.name
                    updateProgressBar(
                        fileSizeInMB, formattedFileSize, RETIREE_APPOINTMENT_PAYMENT_FILE
                    )
                    // Only accept size with in 5 mb
                    Log.d("form_pdffile", "onActivityResult: " + file.name)


                }
            }

            RETIREE_AUTHORISATION_PAYMENT_FILE -> if (resultCode == Activity.RESULT_OK) {
                binding.cvRetireeAuthorisationPaymentDoc.visibility = View.VISIBLE
                //pdfUri = data?.data!!
                val form_uri: Uri = data?.data!!

                authorisationPayUri = form_uri
                //---------------------------------------------
                val parcelFileDescriptor = context?.contentResolver?.openFileDescriptor(
                    form_uri!!, "r", null
                ) ?: return
                val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                val file =
                    File(context?.cacheDir, context?.contentResolver?.getFileName(form_uri!!))
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)

                // Retrieve and format file size
                val fileSize = getFileSize(form_uri)
                val formattedFileSize = formatFileSize(fileSize)

                //progress_bar.progress = 0
                // val body = UploadRequestBody(file,"image",this)
                Log.d("file.name", "onActivityResult: " + file.name)

                // val body = UploadRequestBody(file,"image",this)

                // 5000 kb to5 mb
                //10000 kb = 10 mb
                // val f: File = File(path2)
                Log.e("File", file.toString())

                val length = file.length()
                Log.e("Length", length.toString())
                val fileSizeInMB = length / 1024
                Log.e("fileSizeInMB", fileSizeInMB.toString())

                if (fileSizeInMB > 2000) {  //2000 kb = 2 mb
                    Toast.makeText(
                        requireContext(), "Can't Upload file larger than 2mb", Toast.LENGTH_LONG
                    ).show()
                } else {

                    binding.tvRetireeAuthorisationPaymentDocFilename.text = file.name

                    payment_authorization_retirement_or_death_file = file

                    payment_authorization_retirement_or_death_fileMIME =
                        getMimeType(payment_authorization_retirement_or_death_file.toString())

                    Log.d("doc upload", "$payment_authorization_retirement_or_death_file")
                    payment_authorization_retirement_or_death_filename = file.name
                    updateProgressBar(
                        fileSizeInMB, formattedFileSize, RETIREE_AUTHORISATION_PAYMENT_FILE
                    )
                    // Only accept size with in 5 mb
                    Log.d("form_pdffile", "onActivityResult: " + file.name)


                }
            }

            RETIREE_CLEARANCE_FORM_FILE -> if (resultCode == Activity.RESULT_OK) {
                binding.cvRetireeClearanceFormDoc.visibility = View.VISIBLE
                //pdfUri = data?.data!!
                val form_uri: Uri = data?.data!!

                clearanceFormUri = form_uri
                //---------------------------------------------
                val parcelFileDescriptor = context?.contentResolver?.openFileDescriptor(
                    form_uri!!, "r", null
                ) ?: return
                val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                val file =
                    File(context?.cacheDir, context?.contentResolver?.getFileName(form_uri!!))
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)

                // Retrieve and format file size
                val fileSize = getFileSize(form_uri)
                val formattedFileSize = formatFileSize(fileSize)


                //progress_bar.progress = 0
                // val body = UploadRequestBody(file,"image",this)
                Log.d("file.name", "onActivityResult: " + file.name)

                // val body = UploadRequestBody(file,"image",this)

                // 5000 kb to5 mb
                //10000 kb = 10 mb
                // val f: File = File(path2)
                Log.e("File", file.toString())

                val length = file.length()
                Log.e("Length", length.toString())
                val fileSizeInMB = length / 1024
                Log.e("fileSizeInMB", fileSizeInMB.toString())

                if (fileSizeInMB > 2000) {  //2000 kb = 2 mb
                    Toast.makeText(
                        requireContext(), "Can't Upload file larger than 2mb", Toast.LENGTH_LONG
                    ).show()
                } else {

                    binding.tvRetireeClearanceFormDocFilename.text = file.name

                    clearance_form_file = file

                    clearance_form_fileMIME = getMimeType(clearance_form_file.toString())

                    Log.d("doc upload", "$clearance_form_file")
                    clearance_form_filename = file.name
                    updateProgressBar(
                        fileSizeInMB, formattedFileSize, RETIREE_CLEARANCE_FORM_FILE
                    )
                    // Only accept size with in 5 mb
                    Log.d("form_pdffile", "onActivityResult: " + file.name)


                }
            }

            RETIREE_NOTIFICATION_PROMOTION_FILE -> if (resultCode == Activity.RESULT_OK) {
                binding.cvRetireeNotificationPromotionDoc.visibility = View.VISIBLE
                //pdfUri = data?.data!!
                val form_uri: Uri = data?.data!!

                notificationUri = form_uri
                //---------------------------------------------
                val parcelFileDescriptor = context?.contentResolver?.openFileDescriptor(
                    form_uri!!, "r", null
                ) ?: return
                val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                val file =
                    File(context?.cacheDir, context?.contentResolver?.getFileName(form_uri!!))
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)

                // Retrieve and format file size
                val fileSize = getFileSize(form_uri)
                val formattedFileSize = formatFileSize(fileSize)

                //progress_bar.progress = 0
                // val body = UploadRequestBody(file,"image",this)
                Log.d("file.name", "onActivityResult: " + file.name)

                // val body = UploadRequestBody(file,"image",this)

                // 5000 kb to5 mb
                //10000 kb = 10 mb
                // val f: File = File(path2)
                Log.e("File", file.toString())

                val length = file.length()
                Log.e("Length", length.toString())
                val fileSizeInMB = length / 1024
                Log.e("fileSizeInMB", fileSizeInMB.toString())

                if (fileSizeInMB > 2000) {  //2000 kb = 2 mb
                    Toast.makeText(
                        requireContext(), "Can't Upload file larger than 2mb", Toast.LENGTH_LONG
                    ).show()
                } else {

                    binding.tvRetireeNotificationPromotionDocFilename.text = file.name

                    notification_promotion_file = file

                    notification_promotion_fileMIME =
                        getMimeType(notification_promotion_file.toString())

                    Log.d("doc upload", "$notification_promotion_file")
                    notification_promotion_filename = file.name
                    updateProgressBar(
                        fileSizeInMB, formattedFileSize, RETIREE_NOTIFICATION_PROMOTION_FILE
                    )
                    // Only accept size with in 5 mb
                    Log.d("form_pdffile", "onActivityResult: " + file.name)


                }
            }

            RETIREE_PENSION_CERTIFICATE_FILE -> if (resultCode == Activity.RESULT_OK) {
                binding.cvRetireePensionCertificateDoc.visibility = View.VISIBLE
                //pdfUri = data?.data!!
                val form_uri: Uri = data?.data!!

                pensionCertificateUri = form_uri
                //---------------------------------------------
                val parcelFileDescriptor = context?.contentResolver?.openFileDescriptor(
                    form_uri!!, "r", null
                ) ?: return
                val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                val file =
                    File(context?.cacheDir, context?.contentResolver?.getFileName(form_uri!!))
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)


                // Retrieve and format file size
                val fileSize = getFileSize(form_uri)
                val formattedFileSize = formatFileSize(fileSize)
                //progress_bar.progress = 0
                // val body = UploadRequestBody(file,"image",this)
                Log.d("file.name", "onActivityResult: " + file.name)

                // val body = UploadRequestBody(file,"image",this)

                // 5000 kb to5 mb
                //10000 kb = 10 mb
                // val f: File = File(path2)
                Log.e("File", file.toString())

                val length = file.length()
                Log.e("Length", length.toString())
                val fileSizeInMB = length / 1024
                Log.e("fileSizeInMB", fileSizeInMB.toString())

                if (fileSizeInMB > 2000) {  //2000 kb = 2 mb
                    Toast.makeText(
                        requireContext(), "Can't Upload file larger than 2mb", Toast.LENGTH_LONG
                    ).show()
                } else {

                    binding.tvRetireePensionCertificateDocFilename.text = file.name

                    pension_life_certificate_file = file

                    pension_life_certificate_fileMIME =
                        getMimeType(pension_life_certificate_file.toString())

                    Log.d("doc upload", "$pension_life_certificate_file")
                    pension_life_certificate_filename = file.name
                    updateProgressBar(
                        fileSizeInMB, formattedFileSize, RETIREE_PENSION_CERTIFICATE_FILE
                    )
                    // Only accept size with in 5 mb
                    Log.d("form_pdffile", "onActivityResult: " + file.name)


                }
            }

            RETIREE_PASSPORT_PHOTO_FILE -> if (resultCode == Activity.RESULT_OK) {
                binding.cvRetireePassportPhotoDoc.visibility = View.VISIBLE
                //pdfUri = data?.data!!
                val form_uri: Uri = data?.data!!

                passportPhotoUri = form_uri
                //---------------------------------------------
                val parcelFileDescriptor = context?.contentResolver?.openFileDescriptor(
                    form_uri!!, "r", null
                ) ?: return
                val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                val file =
                    File(context?.cacheDir, context?.contentResolver?.getFileName(form_uri!!))
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)

                // Retrieve and format file size
                val fileSize = getFileSize(form_uri)
                val formattedFileSize = formatFileSize(fileSize)

                //progress_bar.progress = 0
                // val body = UploadRequestBody(file,"image",this)
                Log.d("file.name", "onActivityResult: " + file.name)

                // val body = UploadRequestBody(file,"image",this)

                // 5000 kb to5 mb
                //10000 kb = 10 mb
                // val f: File = File(path2)
                Log.e("File", file.toString())

                val length = file.length()
                Log.e("Length", length.toString())
                val fileSizeInMB = length / 1024
                Log.e("fileSizeInMB", fileSizeInMB.toString())

                if (fileSizeInMB > 2000) {  //2000 kb = 2 mb
                    Toast.makeText(
                        requireContext(), "Can't Upload file larger than 2mb", Toast.LENGTH_LONG
                    ).show()
                } else {

                    binding.tvRetireePassportPhotoDocFilename.text = file.name

                    passport_photo_file = file

                    passport_photo_fileMIME = getMimeType(passport_photo_file.toString())

                    Log.d("doc upload", "$passport_photo_file")
                    passport_photo_filename = file.name
                    updateProgressBar(
                        fileSizeInMB, formattedFileSize, RETIREE_PASSPORT_PHOTO_FILE
                    )
                    // Only accept size with in 5 mb
                    Log.d("form_pdffile", "onActivityResult: " + file.name)


                }
            }

            RETIREE_RETIREMENT_NOTICE_FILE -> if (resultCode == Activity.RESULT_OK) {
                binding.cvRetireeRetirementNoticeDoc.visibility = View.VISIBLE
                //pdfUri = data?.data!!
                val form_uri: Uri = data?.data!!

                retirementNoticeUri = form_uri
                //---------------------------------------------
                val parcelFileDescriptor = context?.contentResolver?.openFileDescriptor(
                    form_uri!!, "r", null
                ) ?: return
                val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                val file =
                    File(context?.cacheDir, context?.contentResolver?.getFileName(form_uri!!))
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)

                // Retrieve and format file size
                val fileSize = getFileSize(form_uri)
                val formattedFileSize = formatFileSize(fileSize)

                //progress_bar.progress = 0
                // val body = UploadRequestBody(file,"image",this)
                Log.d("file.name", "onActivityResult: " + file.name)

                // val body = UploadRequestBody(file,"image",this)

                // 5000 kb to5 mb
                //10000 kb = 10 mb
                // val f: File = File(path2)
                Log.e("File", file.toString())

                val length = file.length()
                Log.e("Length", length.toString())
                val fileSizeInMB = length / 1024
                Log.e("fileSizeInMB", fileSizeInMB.toString())

                if (fileSizeInMB > 2000) {  //2000 kb = 2 mb
                    Toast.makeText(
                        requireContext(), "Can't Upload file larger than 2mb", Toast.LENGTH_LONG
                    ).show()
                } else {

                    binding.tvRetireeRetirementNoticeDocFilename.text = file.name

                    retirement_notice_file = file

                    retirement_notice_fileMIME = getMimeType(retirement_notice_file.toString())

                    Log.d("doc upload", "$retirement_notice_file")
                    retirement_notice_filename = file.name
                    updateProgressBar(
                        fileSizeInMB, formattedFileSize, RETIREE_RETIREMENT_NOTICE_FILE
                    )
                    // Only accept size with in 5 mb
                    Log.d("form_pdffile", "onActivityResult: " + file.name)


                }
            }

            RETIREE_ID_CARD_FILE -> if (resultCode == Activity.RESULT_OK) {
                binding.cvRetireeIdCardDoc.visibility = View.VISIBLE
                //pdfUri = data?.data!!
                val form_uri: Uri = data?.data!!

                idCardUri = form_uri
                //---------------------------------------------
                val parcelFileDescriptor = context?.contentResolver?.openFileDescriptor(
                    form_uri!!, "r", null
                ) ?: return
                val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                val file =
                    File(context?.cacheDir, context?.contentResolver?.getFileName(form_uri!!))
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)


                // Retrieve and format file size
                val fileSize = getFileSize(form_uri)
                val formattedFileSize = formatFileSize(fileSize)

                //progress_bar.progress = 0
                // val body = UploadRequestBody(file,"image",this)
                Log.d("file.name", "onActivityResult: " + file.name)

                // val body = UploadRequestBody(file,"image",this)

                // 5000 kb to5 mb
                //10000 kb = 10 mb
                // val f: File = File(path2)
                Log.e("File", file.toString())

                val length = file.length()
                Log.e("Length", length.toString())
                val fileSizeInMB = length / 1024
                Log.e("fileSizeInMB", fileSizeInMB.toString())

                if (fileSizeInMB > 2000) {  //2000 kb = 2 mb
                    Toast.makeText(
                        requireContext(), "Can't Upload file larger than 2mb", Toast.LENGTH_LONG
                    ).show()
                } else {

                    binding.tvRetireeIdCardDocFilename.text = file.name

                    id_card_file = file

                    id_card_fileMIME = getMimeType(id_card_file.toString())

                    Log.d("doc upload", "$id_card_file")
                    id_card_filename = file.name
                    updateProgressBar(fileSizeInMB, formattedFileSize, RETIREE_ID_CARD_FILE)
                    // Only accept size with in 5 mb
                    Log.d("form_pdffile", "onActivityResult: " + file.name)


                }
            }

        }
    }

    private fun formatFileSize(size: Long): String {
        return android.text.format.Formatter.formatFileSize(context, size)
    }

    private fun getFileSize(uri: Uri): Long {
        val cursor = context?.contentResolver?.query(
            uri, arrayOf(OpenableColumns.SIZE), null, null, null
        )
        var size: Long = -1
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex >= 0) {
                    size = it.getLong(sizeIndex)
                }
            }
        }
        return size
    }

    private fun ContentResolver.getFileName(selectedImageUri: Uri): String {
        var name = ""
        val returnCursor = this.query(selectedImageUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }

        return name
    }


    private fun updateProgressBar(size: Long, pdfSize: String, file_type: Int) {

        when (file_type) {

            RETIREE_APPLICATION_FORM_FILE -> {
                var form_status = 0
                val form_handler: Handler = Handler()

                binding.cvRetireeApplicationForm.visibility= View.VISIBLE
                binding.imgRetireeAppFormClose.visibility= View.VISIBLE

                binding.llRetireeAppFormUploadprogress.visibility = View.VISIBLE
                binding.llRetireeAppFormPercentage.visibility =View.VISIBLE
                binding.pbRetireeAppForm.visibility =View.VISIBLE
                binding.llRetireeFormSize.visibility = View.GONE

                Thread(Runnable {
                    while (form_status < 100) {
                        form_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        form_handler.post(Runnable {
                            binding.pbRetireeAppForm?.setProgress(form_status)

                            binding.tvRetireeDocPercent.text = form_status.toString() + " %"
                            if (form_status == 100) {

                                binding.retireeAppBtnGreenView.visibility = View.VISIBLE
                                binding.llRetireeAppFormUploadprogress.visibility = View.GONE
                                binding.llRetireeFormSize.visibility = View.VISIBLE
                                binding.txtRetireeFilesize.text = pdfSize
                            }
                        })
                    }
                }).start()


            }

            RETIREE_APPOINTMENT_PAYMENT_FILE -> {
                var form_status = 0
                val form_handler: Handler = Handler()

                binding.cvRetireeAppointmentPayment.visibility= View.VISIBLE
                binding.imgRetireeAppointmentPaymentDocClose.visibility= View.VISIBLE

                binding.llRetireeAppointmentPaymentDocUploadprogress.visibility = View.VISIBLE
                binding.llRetireeAppointmentPaymentDocPercentage.visibility = View.VISIBLE
                binding.pbRetireeAppointmentPaymentDoc.visibility = View.VISIBLE
                binding.llRetireeAppointmentPaymentDocSize.visibility = View.GONE

                Thread(Runnable {
                    while (form_status < 100) {
                        form_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        form_handler.post(Runnable {
                            binding.pbRetireeAppointmentPaymentDoc?.setProgress(form_status)

                            binding.tvRetireeAppointmentPaymentDocPercent.text =
                                form_status.toString() + " %"
                            if (form_status == 100) {

                                binding.retireeAppointmentBtnGreenView.visibility = View.VISIBLE

                                binding.llRetireeAppointmentPaymentDocUploadprogress.visibility =
                                    View.GONE
                                binding.llRetireeAppointmentPaymentDocSize.visibility = View.VISIBLE
                                binding.txtAppointmentPaymentDocFilesize.text = pdfSize
                            }
                        })
                    }
                }).start()


            }

            RETIREE_AUTHORISATION_PAYMENT_FILE -> {
                var form_status = 0
                val form_handler: Handler = Handler()

                binding.cvRetireeAuthorisationPaymentDoc.visibility= View.VISIBLE
                binding.imgRetireeAuthorisationPaymentDocClose.visibility= View.VISIBLE

                binding.llRetireeAuthorisationPaymentDocUploadprogress.visibility = View.VISIBLE
                binding.llRetireeAuthorisationPaymentDocPercentage.visibility =View.VISIBLE
                binding.pbRetireeAuthorisationPaymentDoc.visibility = View.VISIBLE
                binding.llRetireeAuthorisationPaymentDocSize.visibility = View.GONE

                Thread(Runnable {
                    while (form_status < 100) {
                        form_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        form_handler.post(Runnable {
                            binding.pbRetireeAuthorisationPaymentDoc?.setProgress(form_status)
                            binding.tvRetireeAuthorisationPaymentDocPercent.text =
                                form_status.toString() + " %"
                            if (form_status == 100) {
                                binding.retireeAuthorisationBtnGreenView.visibility = View.VISIBLE

                                binding.llRetireeAuthorisationPaymentDocUploadprogress.visibility =
                                    View.GONE
                                binding.llRetireeAuthorisationPaymentDocSize.visibility = View.VISIBLE
                                binding.txtAuthorisationPaymentDocFilesize.text = pdfSize
                            }
                        })
                    }
                }).start()


            }

            RETIREE_CLEARANCE_FORM_FILE -> {
                var form_status = 0
                val form_handler: Handler = Handler()

                binding.cvRetireeClearanceFormDoc.visibility= View.VISIBLE
                binding.llRetireeClearanceFormUpload.visibility= View.VISIBLE

                binding.llRetireeClearanceFormDocUploadprogress.visibility = View.VISIBLE
                binding.llRetireeClearanceFormDocPercentage.visibility =View.VISIBLE
                binding.pbRetireeClearanceFormDoc.visibility =View.VISIBLE
                binding.llRetireeClearanceFormDocSize.visibility = View.GONE

                Thread(Runnable {
                    while (form_status < 100) {
                        form_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        form_handler.post(Runnable {
                            binding.pbRetireeClearanceFormDoc?.setProgress(form_status)

                            binding.tvRetireeClearanceFormDocPercent.text =
                                form_status.toString() + " %"
                            if (form_status == 100) {
                                binding.retireeClearanceBtnGreenView.visibility = View.VISIBLE

                                binding.llRetireeClearanceFormDocUploadprogress.visibility = View.GONE
                                binding.llRetireeClearanceFormDocSize.visibility = View.VISIBLE
                                binding.txtClearanceFormDocFilesize.text = pdfSize
                            }
                        })
                    }
                }).start()


            }

            RETIREE_NOTIFICATION_PROMOTION_FILE -> {
                var form_status = 0
                val form_handler: Handler = Handler()

                binding.cvRetireeNotificationPromotionDoc.visibility= View.VISIBLE
                binding.llRetireeNotificationPromotionDocUpload.visibility= View.VISIBLE

                binding.llRetireeNotificationPromotionDocUploadprogress.visibility = View.VISIBLE
                binding.llRetireeNotificationPromotionDocPercentage.visibility = View.VISIBLE
                binding.pbRetireeNotificationPromotionDoc.visibility = View.VISIBLE
                binding.llRetireeNotificationPromotionDocSize.visibility = View.GONE

                Thread(Runnable {
                    while (form_status < 100) {
                        form_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        form_handler.post(Runnable {
                            binding.pbRetireeNotificationPromotionDoc?.setProgress(form_status)

                            binding.tvRetireeNotificationPromotionDocPercent.text =
                                form_status.toString() + " %"
                            if (form_status == 100) {
                                binding.retireeNotificationBtnGreenView.visibility = View.VISIBLE

                                binding.llRetireeNotificationPromotionDocUploadprogress.visibility =
                                    View.GONE
                                binding.llRetireeNotificationPromotionDocSize.visibility = View.VISIBLE
                                binding.txtNotificationPromotionDocFilesize.text = pdfSize

                            }
                        })
                    }
                }).start()


            }

            RETIREE_PENSION_CERTIFICATE_FILE -> {
                var form_status = 0
                val form_handler: Handler = Handler()

                binding.cvRetireePensionCertificateDoc.visibility= View.VISIBLE
                binding.imgRetireePensionCertificateDocClose.visibility= View.VISIBLE

                binding.llRetireePensionCertificateDocUploadprogress.visibility = View.VISIBLE
                binding.llRetireePensionCertificateDocPercentage.visibility =View.VISIBLE
                binding.pbRetireePensionCertificateDoc.visibility =View.VISIBLE
                binding.llRetireePensionCertificateDocSize.visibility = View.GONE

                Thread(Runnable {
                    while (form_status < 100) {
                        form_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        form_handler.post(Runnable {
                            binding.pbRetireePensionCertificateDoc?.setProgress(form_status)

                            binding.tvRetireePensionCertificateDocPercent.text =
                                form_status.toString() + " %"
                            if (form_status == 100) {
                                binding.retireePensionCertificateBtnGreenView.visibility = View.VISIBLE

                                binding.llRetireePensionCertificateDocUploadprogress.visibility =
                                    View.GONE
                                binding.llRetireePensionCertificateDocSize.visibility = View.VISIBLE
                                binding.txtPensionCertificateDocFilesize.text = pdfSize
                            }
                        })
                    }
                }).start()


            }

            RETIREE_PASSPORT_PHOTO_FILE -> {
                var form_status = 0
                val form_handler: Handler = Handler()

                binding.cvRetireePassportPhotoDoc.visibility= View.VISIBLE
                binding.imgRetireePassportPhotoDocClose.visibility= View.VISIBLE

                binding.llRetireePassportPhotoDocUploadprogress.visibility = View.VISIBLE
                binding.llRetireeAppPassportPhotoDocPercentage.visibility =View.VISIBLE
                binding.pbRetireePassportPhotoDoc.visibility =View.VISIBLE
                binding.llRetireePassportPhotoDocSize.visibility = View.GONE

                Thread(Runnable {
                    while (form_status < 100) {
                        form_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        form_handler.post(Runnable {
                            binding.pbRetireePassportPhotoDoc?.setProgress(form_status)

                            binding.tvRetireePassportPhotoDocPercent.text =
                                form_status.toString() + " %"
                            if (form_status == 100) {
                                binding.retireePassportPhotoBtnGreenView.visibility = View.VISIBLE

                                binding.llRetireePassportPhotoDocUploadprogress.visibility = View.GONE
                                binding.llRetireePassportPhotoDocSize.visibility = View.VISIBLE
                                binding.txtPassportPhotoDocFilesize.text = pdfSize
                            }
                        })
                    }
                }).start()


            }

            RETIREE_RETIREMENT_NOTICE_FILE -> {
                var form_status = 0
                val form_handler: Handler = Handler()

                binding.cvRetireeRetirementNoticeDoc.visibility= View.VISIBLE
                binding.imgRetireeRetirementNoticeDocClose.visibility= View.VISIBLE

                binding.llRetireeRetirementNoticeDocUploadprogress.visibility = View.VISIBLE
                binding.llRetireeRetirementNoticeDocPercentage.visibility =View.VISIBLE
                binding.pbRetireeRetirementNoticeDoc.visibility = View.VISIBLE
                binding.llRetireeRetirementNoticeDocSize.visibility = View.GONE

                Thread(Runnable {
                    while (form_status < 100) {
                        form_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        form_handler.post(Runnable {
                            binding.pbRetireeRetirementNoticeDoc?.setProgress(form_status)

                            binding.tvRetireeRetirementNoticeDocPercent.text =
                                form_status.toString() + " %"
                            if (form_status == 100) {
                                binding.retireeRetirementBtnGreenView.visibility = View.VISIBLE

                                binding.llRetireeRetirementNoticeDocUploadprogress.visibility =
                                    View.GONE
                                binding.llRetireeRetirementNoticeDocSize.visibility = View.VISIBLE
                                binding.txtRetirementNoticeDocFilesize.text = pdfSize
                            }
                        })
                    }
                }).start()

            }

            RETIREE_ID_CARD_FILE -> {
                var form_status = 0
                val form_handler: Handler = Handler()

                binding.cvRetireeIdCardDoc.visibility= View.VISIBLE
                binding.imgRetireeIdCardDocClose.visibility= View.VISIBLE

                binding.llRetireeIdCardDocUploadprogress.visibility = View.VISIBLE
                binding.llRetireeIdCardDocPercentage.visibility = View.VISIBLE
                binding.pbRetireeIdCardDoc.visibility = View.VISIBLE
                binding.llRetireeIdCardDocSize.visibility = View.GONE

                Thread(Runnable {
                    while (form_status < 100) {
                        form_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        form_handler.post(Runnable {
                            binding.pbRetireeIdCardDoc?.setProgress(form_status)

                            binding.tvRetireeIdCardDocPercent.text = form_status.toString() + " %"
                            if (form_status == 100) {

                                binding.retireeIdCardBtnGreenView.visibility = View.VISIBLE

                                binding.llRetireeIdCardDocUploadprogress.visibility = View.GONE
                                binding.llRetireeIdCardDocSize.visibility = View.VISIBLE
                                binding.txtIdCardDocFilesize.text = pdfSize
                            }
                        })
                    }
                }).start()


            }

        }


    }


    private fun onDocUploadSuccess(response: ResponseRetireeDocUpload) {
        Toast.makeText(context, response.detail?.message, Toast.LENGTH_SHORT).show()
        if (prefs.onboardingStage == OnboardingStage.RETIREE_DOCUMENTS)
            prefs.onboardingStage = OnboardingStage.RETIREE_BANK_INFO
        retireeServiceViewModel.moveToNextTab()
        retireeServiceViewModel.refreshTabsState()
    }

    private fun populateViews() {
        RetireeUserDocRetrive?.let { retireeUserDoc ->
            if (!retireeUserDoc.idCardFileUrl.isNullOrEmpty()) {
                if (prefs.onboardingStage == OnboardingStage.RETIREE_DOCUMENTS) {
                    prefs.onboardingStage = OnboardingStage.RETIREE_BANK_INFO
                    retireeServiceViewModel.refreshTabsState()
                }

                // Mandatory documents
                setDocumentView(retireeUserDoc.idCardFileUrl, binding.retireeIdCardBtnGreenView, binding.cvRetireeIdCardDoc, binding.tvRetireeIdCardDocFilename, binding.imgRetireeIdCardDocClose, binding.llRetireeIdCardDocUploadprogress, binding.llRetireeIdCardDocPercentage, binding.pbRetireeIdCardDoc)
                // Optional documents
                setDocumentViewIfPresent(retireeUserDoc.passportPhotoFileUrl, binding.retireePassportPhotoBtnGreenView, binding.cvRetireePassportPhotoDoc, binding.tvRetireePassportPhotoDocFilename, binding.imgRetireePassportPhotoDocClose, binding.llRetireePassportPhotoDocUploadprogress, binding.llRetireeAppPassportPhotoDocPercentage, binding.pbRetireePassportPhotoDoc)
                setDocumentViewIfPresent(retireeUserDoc.applicationFormFileUrl, binding.retireeAppBtnGreenView, binding.cvRetireeApplicationFormDoc, binding.tvRetireeAppFormFilename, binding.imgRetireeAppFormClose, binding.llRetireeAppFormUploadprogress, binding.llRetireeAppFormPercentage, binding.pbRetireeAppForm)
                setDocumentViewIfPresent(retireeUserDoc.monthlyArrearsPaymentAppointmentFileUrl, binding.retireeAppointmentBtnGreenView, binding.cvRetireeAppointmentPaymentDoc, binding.tvRetireeAppointmentPaymentDocFilename, binding.imgRetireeAppointmentPaymentDocClose, binding.llRetireeAppointmentPaymentDocUploadprogress, binding.llRetireeAppointmentPaymentDocPercentage, binding.pbRetireeAppointmentPaymentDoc)
                setDocumentViewIfPresent(retireeUserDoc.paymentAuthorizationRetirementOrDeathFileUrl, binding.retireeAuthorisationBtnGreenView, binding.cvRetireeAuthorisationPaymentDoc, binding.tvRetireeAuthorisationPaymentDocFilename, binding.imgRetireeAuthorisationPaymentDocClose, binding.llRetireeAuthorisationPaymentDocUploadprogress, binding.llRetireeAuthorisationPaymentDocPercentage, binding.pbRetireeAuthorisationPaymentDoc)
                setDocumentViewIfPresent(retireeUserDoc.clearanceFormFileUrl, binding.retireeClearanceBtnGreenView, binding.cvRetireeClearanceFormDoc, binding.tvRetireeClearanceFormDocFilename, binding.llRetireeClearanceFormUpload, binding.llRetireeClearanceFormDocUploadprogress, binding.llRetireeClearanceFormDocPercentage, binding.pbRetireeClearanceFormDoc)
                setDocumentViewIfPresent(retireeUserDoc.promotionNotificationFileUrl, binding.retireeNotificationBtnGreenView, binding.cvRetireeNotificationPromotionDoc, binding.tvRetireeNotificationPromotionDocFilename, binding.llRetireeNotificationPromotionDocUpload, binding.llRetireeNotificationPromotionDocUploadprogress, binding.llRetireeNotificationPromotionDocPercentage, binding.pbRetireeNotificationPromotionDoc)
                setDocumentViewIfPresent(retireeUserDoc.pensionLifeCertificateFileUrl, binding.retireePensionCertificateBtnGreenView, binding.cvRetireePensionCertificateDoc, binding.tvRetireePensionCertificateDocFilename, binding.imgRetireePensionCertificateDocClose, binding.llRetireePensionCertificateDocUploadprogress, binding.llRetireePensionCertificateDocPercentage, binding.pbRetireePensionCertificateDoc)
                setDocumentViewIfPresent(retireeUserDoc.retirementNoticeFileUrl, binding.retireeRetirementBtnGreenView, binding.cvRetireeRetirementNoticeDoc, binding.tvRetireeRetirementNoticeDocFilename, binding.imgRetireeRetirementNoticeDocClose, binding.llRetireeRetirementNoticeDocUploadprogress, binding.llRetireeRetirementNoticeDocPercentage, binding.pbRetireeRetirementNoticeDoc)

                // Enable tabs if needed
                // enableDisableTabs(tab_tablayout_retiree, true, true, true)
            }
        }
    }

    /*private fun setDocumentView(url: String?, buttonView: View, cardView: View, filenameView: TextView, closeButtonView: View, uploadProgressView: View, percentageView: View, progressBar: View) {
        if (!url.isNullOrEmpty()) {
            buttonView.visibility = View.VISIBLE
            cardView.visibility = View.VISIBLE
            filenameView.text = url.substringAfterLast('/')
            closeButtonView.visibility = View.VISIBLE
            uploadProgressView.visibility = View.GONE
            percentageView.visibility = View.GONE
            progressBar.visibility = View.GONE
        }
    }

    private fun setDocumentViewIfPresent(url: String?, buttonView: View, cardView: View, filenameView: TextView, closeButtonView: View, uploadProgressView: View, percentageView: View, progressBar: View) {
        if (!url.isNullOrEmpty()) {
            setDocumentView(url, buttonView, cardView, filenameView, closeButtonView, uploadProgressView, percentageView, progressBar)
        }
    }*/

    private fun onRetireeRetrivedDocSetFields() {
        //responseActiveDocRetrive?.detail?.filePresentCheck.equals("true")
        //if (!ActiveUserDocRetrive?.applicationFormFileUrl.isNullOrEmpty()){
        if (!RetireeUserDocRetrive?.idCardFileUrl.isNullOrEmpty()) {


            //mandatory
            var idcard_url = RetireeUserDocRetrive!!.idCardFileUrl
            var passPhoto_url = RetireeUserDocRetrive!!.passportPhotoFileUrl
            //optional
            var appform_url = RetireeUserDocRetrive!!.applicationFormFileUrl
            var appoinment_payment_url = RetireeUserDocRetrive!!.monthlyArrearsPaymentAppointmentFileUrl
            var autherization_payment_url = RetireeUserDocRetrive!!.paymentAuthorizationRetirementOrDeathFileUrl
            var clearrance_url = RetireeUserDocRetrive!!.clearanceFormFileUrl
            var notification_promotion_url= RetireeUserDocRetrive!!.promotionNotificationFileUrl
            var pension_certificate_url =  RetireeUserDocRetrive!!.pensionLifeCertificateFileUrl
            var retirement_url = RetireeUserDocRetrive!!.retirementNoticeFileUrl


            /*
            binding.retireeAppBtnGreenView.setOnClickListener(this)
            binding.retireeAppointmentBtnGreenView.setOnClickListener(this)
            binding.retireeAuthorisationBtnGreenView.setOnClickListener(this)
            binding.retireeClearanceBtnGreenView.setOnClickListener(this)
            binding.retireeNotificationBtnGreenView.setOnClickListener(this)
            binding.retireePensionCertificateBtnGreenView.setOnClickListener(this)
            binding.retireePassportPhotoBtnGreenView.setOnClickListener(this)
            binding.retireeRetirementBtnGreenView.setOnClickListener(this)
            binding.retireeIdCardBtnGreenView.setOnClickListener(this)
             */

            //visible view btn mandatory

            binding.retireeIdCardBtnGreenView.visibility = View.VISIBLE
            binding.retireePassportPhotoBtnGreenView.visibility = View.VISIBLE

            //retiree id Card
            binding.cvRetireeIdCardDoc.visibility = View.VISIBLE
            binding.tvRetireeIdCardDocFilename.text = idcard_url!!.substringAfterLast('/')
            binding.imgRetireeIdCardDocClose.visibility = View.VISIBLE
            binding.llRetireeIdCardDocUploadprogress.visibility = View.GONE
            binding.llRetireeIdCardDocPercentage.visibility = View.GONE
            binding.pbRetireeIdCardDoc.visibility = View.GONE

            //PassPort photo
            binding.cvRetireePassportPhotoDoc.visibility = View.VISIBLE
            binding.tvRetireePassportPhotoDocFilename.text = passPhoto_url!!.substringAfterLast('/')
            binding.imgRetireePassportPhotoDocClose.visibility = View.VISIBLE
            binding.llRetireePassportPhotoDocUploadprogress.visibility = View.GONE
            binding.llRetireeAppPassportPhotoDocPercentage.visibility = View.GONE
            binding.pbRetireePassportPhotoDoc.visibility = View.GONE



            //optional button visible
            //app form
            if (!appform_url.isNullOrEmpty()){
                binding.retireeAppBtnGreenView.visibility = View.VISIBLE
                binding.cvRetireeApplicationFormDoc.visibility = View.VISIBLE
            binding.tvRetireeAppFormFilename.text = appform_url!!.substringAfterLast('/')

            binding.imgRetireeAppFormClose.visibility = View.VISIBLE
            binding.llRetireeAppFormUploadprogress.visibility = View.GONE
            binding.llRetireeAppFormPercentage.visibility = View.GONE
            binding.pbRetireeAppForm.visibility = View.GONE
            }

            //tv_active_app_form_filename.text = ActiveUserDocRetrive?.applicationFormFileUrl
            //ll_active_app_form_uploadprogress.visibility = View.GONE

            //Appoinment payment
            if (!appoinment_payment_url.isNullOrEmpty()){
            binding.retireeAppointmentBtnGreenView.visibility = View.VISIBLE

            binding.cvRetireeAppointmentPaymentDoc.visibility = View.VISIBLE
            binding.tvRetireeAppointmentPaymentDocFilename.text = appoinment_payment_url!!.substringAfterLast('/')

            binding.imgRetireeAppointmentPaymentDocClose.visibility = View.VISIBLE
            binding.llRetireeAppointmentPaymentDocUploadprogress.visibility = View.GONE
            binding.llRetireeAppointmentPaymentDocPercentage.visibility = View.GONE
            binding.pbRetireeAppointmentPaymentDoc.visibility = View.GONE
            }

            //Authorization Payment
            if (!autherization_payment_url.isNullOrEmpty()) {

                binding.retireeAuthorisationBtnGreenView.visibility = View.VISIBLE
                binding.cvRetireeAuthorisationPaymentDoc.visibility = View.VISIBLE
                binding.tvRetireeAuthorisationPaymentDocFilename.text =
                    autherization_payment_url!!.substringAfterLast('/')

                binding.imgRetireeAuthorisationPaymentDocClose.visibility = View.VISIBLE
                binding.llRetireeAuthorisationPaymentDocUploadprogress.visibility = View.GONE
                binding.llRetireeAuthorisationPaymentDocPercentage.visibility = View.GONE
                binding.pbRetireeAuthorisationPaymentDoc.visibility = View.GONE
            }

            //clearance
            if (!clearrance_url.isNullOrEmpty()){
                binding.retireeClearanceBtnGreenView.visibility = View.VISIBLE
                binding.cvRetireeClearanceFormDoc.visibility = View.VISIBLE
            binding.tvRetireeClearanceFormDocFilename.text = clearrance_url!!.substringAfterLast('/')

            binding.llRetireeClearanceFormUpload.visibility = View.VISIBLE
            binding.llRetireeClearanceFormDocUploadprogress.visibility = View.GONE
            binding.llRetireeClearanceFormDocPercentage.visibility = View.GONE
            binding.pbRetireeClearanceFormDoc.visibility = View.GONE
            }

            //notification promotion
            if (!notification_promotion_url.isNullOrEmpty()) {
                binding.retireeNotificationBtnGreenView.visibility = View.VISIBLE

                binding.cvRetireeNotificationPromotionDoc.visibility = View.VISIBLE
                binding.tvRetireeNotificationPromotionDocFilename.text =
                    notification_promotion_url!!.substringAfterLast('/')

                binding.llRetireeNotificationPromotionDocUpload.visibility = View.VISIBLE
                binding.llRetireeNotificationPromotionDocUploadprogress.visibility = View.GONE
                binding.llRetireeNotificationPromotionDocPercentage.visibility = View.GONE
                binding.pbRetireeNotificationPromotionDoc.visibility = View.GONE
            }


            //Pension certification
            if (!pension_certificate_url.isNullOrEmpty()) {
                binding.retireePensionCertificateBtnGreenView.visibility = View.VISIBLE

                binding.cvRetireePensionCertificateDoc.visibility = View.VISIBLE
                binding.tvRetireePensionCertificateDocFilename.text =
                    pension_certificate_url!!.substringAfterLast('/')

                binding.imgRetireePensionCertificateDocClose.visibility = View.VISIBLE
                binding.llRetireePensionCertificateDocUploadprogress.visibility = View.GONE
                binding.llRetireePensionCertificateDocPercentage.visibility = View.GONE
                binding.pbRetireePensionCertificateDoc.visibility = View.GONE
            }



            //retiree retirement
            if (!retirement_url.isNullOrEmpty()) {
                binding.retireeRetirementBtnGreenView.visibility = View.VISIBLE

                binding.cvRetireeRetirementNoticeDoc.visibility = View.VISIBLE
                binding.tvRetireeRetirementNoticeDocFilename.text =
                    retirement_url!!.substringAfterLast('/')

                binding.imgRetireeRetirementNoticeDocClose.visibility = View.VISIBLE
                binding.llRetireeRetirementNoticeDocUploadprogress.visibility = View.GONE
                binding.llRetireeRetirementNoticeDocPercentage.visibility = View.GONE
                binding.pbRetireeRetirementNoticeDoc.visibility = View.GONE
            }
            //enableDisableTabs(tab_tablayout_retiree, true, true, true)
        }
    }
}