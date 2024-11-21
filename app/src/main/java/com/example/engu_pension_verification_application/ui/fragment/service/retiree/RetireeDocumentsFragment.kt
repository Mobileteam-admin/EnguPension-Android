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
import kotlinx.android.synthetic.main.fragment_retiree_documents.*
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_retiree_documents, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        initViews()
        observeLiveData()
    }
    private fun initViews() {
        ll_retiree_app_form_upload.setOnClickListener(this)
        img_retiree_app_form_close.setOnClickListener(this)

        ll_retiree_appointment_payment_doc_upload.setOnClickListener(this)
        img_retiree_appointment_payment_doc_close.setOnClickListener(this)

        ll_retiree_authorisation_payment_doc_upload.setOnClickListener(this)
        img_retiree_authorisation_payment_doc_close.setOnClickListener(this)

        ll_retiree_clearance_form_upload.setOnClickListener(this)
        img_retiree_clearance_form_doc_close.setOnClickListener(this)

        ll_retiree_notification_promotion_doc_upload.setOnClickListener(this)
        img_retiree_notification_promotion_doc_close.setOnClickListener(this)

        ll_retiree_pension_certificate_upload.setOnClickListener(this)
        img_retiree_pension_certificate_doc_close.setOnClickListener(this)

        ll_retiree_passport_photo_upload.setOnClickListener(this)
        img_retiree_passport_photo_doc_close.setOnClickListener(this)

        ll_retiree_retirement_notice_upload.setOnClickListener(this)
        img_retiree_retirement_notice_doc_close.setOnClickListener(this)

        ll_retiree_id_card_upload.setOnClickListener(this)
        img_retiree_id_card_doc_close.setOnClickListener(this)

        ll_retiree_doc_next.setOnClickListener(this)

        retiree_app_btn_green_view.setOnClickListener(this)
        retiree_appointment_btn_green_view.setOnClickListener(this)
        retiree_authorisation_btn_green_view.setOnClickListener(this)
        retiree_clearance_btn_green_view.setOnClickListener(this)
        retiree_notification_btn_green_view.setOnClickListener(this)
        retiree_pension_certificate_btn_green_view.setOnClickListener(this)
        retiree_passport_photo_btn_green_view.setOnClickListener(this)
        retiree_retirement_btn_green_view.setOnClickListener(this)
        retiree_id_card_btn_green_view.setOnClickListener(this)

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
            ll_retiree_app_form_upload -> {
                selectPdfJpegPng(RETIREE_APPLICATION_FORM_FILE)
                retiree_app_btn_green_view.visibility = View.GONE
                cv_retiree_applicationForm_doc.visibility = View.GONE
            }
            img_retiree_app_form_close -> {
                retiree_app_btn_green_view.visibility = View.GONE
                cv_retiree_applicationForm_doc.visibility = View.GONE
            }
            //APPOINTMENT_PAYMENT
            ll_retiree_appointment_payment_doc_upload -> {
                selectPdfJpegPng(RETIREE_APPOINTMENT_PAYMENT_FILE)
                retiree_appointment_btn_green_view.visibility = View.GONE
                cv_retiree_appointment_payment_doc.visibility = View.GONE
            }
            img_retiree_appointment_payment_doc_close -> {
                retiree_appointment_btn_green_view.visibility = View.GONE
                cv_retiree_appointment_payment_doc.visibility = View.GONE
            }
            //RETIREE_AUTHORISATION_PAYMENT
            ll_retiree_authorisation_payment_doc_upload -> {
                retiree_authorisation_btn_green_view.visibility=View.GONE
                cv_retiree_authorisation_payment_doc.visibility = View.GONE
                selectPdfJpegPng(RETIREE_AUTHORISATION_PAYMENT_FILE)
            }
            img_retiree_authorisation_payment_doc_close -> {
                retiree_authorisation_btn_green_view.visibility=View.GONE
                cv_retiree_authorisation_payment_doc.visibility = View.GONE
            }
            //RETIREE_CLEARANCE_FORM
            ll_retiree_clearance_form_upload -> {
                retiree_clearance_btn_green_view.visibility = View.GONE
                cv_retiree_clearance_form_doc.visibility = View.GONE
                selectPdfJpegPng(RETIREE_CLEARANCE_FORM_FILE)
            }
            img_retiree_clearance_form_doc_close -> {
                retiree_clearance_btn_green_view.visibility = View.GONE
                cv_retiree_clearance_form_doc.visibility = View.GONE
            }
            //RETIREE_NOTIFICATION_PROMOTION
            ll_retiree_notification_promotion_doc_upload -> {
                retiree_notification_btn_green_view.visibility = View.GONE
                cv_retiree_notification_promotion_doc.visibility = View.GONE
                selectPdfJpegPng(RETIREE_NOTIFICATION_PROMOTION_FILE)
            }
            img_retiree_notification_promotion_doc_close -> {
                retiree_notification_btn_green_view.visibility = View.GONE
                cv_retiree_notification_promotion_doc.visibility = View.GONE
            }
            //RETIREE_PENSION_CERTIFICATE
            ll_retiree_pension_certificate_upload -> {
                retiree_pension_certificate_btn_green_view.visibility =View.GONE
                cv_retiree_pension_certificate_doc.visibility = View.GONE
                selectPdfJpegPng(RETIREE_PENSION_CERTIFICATE_FILE)
            }
            img_retiree_pension_certificate_doc_close ->{
                retiree_pension_certificate_btn_green_view.visibility =View.GONE
                cv_retiree_pension_certificate_doc.visibility = View.GONE
            }
            //RETIREE_PASSPORT_PHOTO
            ll_retiree_passport_photo_upload -> {
                retiree_passport_photo_btn_green_view.visibility= View.GONE
                cv_retiree_passport_photo_doc.visibility = View.GONE
                selectPdfJpegPng(RETIREE_PASSPORT_PHOTO_FILE)
            }
            img_retiree_passport_photo_doc_close ->{
                retiree_passport_photo_btn_green_view.visibility= View.GONE
                cv_retiree_passport_photo_doc.visibility = View.GONE
            }
            //RETIREE_RETIREMENT_NOTICE
            ll_retiree_retirement_notice_upload -> {
                retiree_retirement_btn_green_view.visibility = View.GONE
                cv_retiree_retirement_notice_doc.visibility = View.GONE
                selectPdfJpegPng(RETIREE_RETIREMENT_NOTICE_FILE)
            }
            img_retiree_retirement_notice_doc_close ->{
                retiree_retirement_btn_green_view.visibility = View.GONE
                cv_retiree_retirement_notice_doc.visibility = View.GONE
            }
            //RETIREE_ID_CARD
            ll_retiree_id_card_upload -> {
                retiree_id_card_btn_green_view.visibility = View.GONE
                cv_retiree_id_card_doc.visibility = View.GONE
                selectPdfJpegPng(RETIREE_ID_CARD_FILE)
            }
            img_retiree_id_card_doc_close -> {
                retiree_id_card_btn_green_view.visibility = View.GONE
                cv_retiree_id_card_doc.visibility = View.GONE
            }

            ll_retiree_doc_next -> {
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
            //retiree_id_card_btn_green_view -> viewButtonCall(RetireeUserDocRetrive?.idCardFileUrl)
            retiree_id_card_btn_green_view -> if(!RetireeUserDocRetrive?.idCardFileUrl.isNullOrEmpty()){

                Log.d("viewbutton", " R id cardurl ${RetireeUserDocRetrive?.idCardFileUrl}")

                startActivityWebViewFromUrl(RetireeUserDocRetrive?.idCardFileUrl)
            }else{
                Log.d("viewbutton", " R id card uri  ${idCardUri}")
                Log.d("viewbutton", " R id card url ${RetireeUserDocRetrive?.idCardFileUrl}")
                idCardUri?.let { it1 -> startActivityFromUri(it1) }
            }
            //retiree_passport_photo_btn_green_view -> viewButtonCall(RetireeUserDocRetrive?.passportPhotoFileUrl)
            retiree_passport_photo_btn_green_view -> if(!RetireeUserDocRetrive?.passportPhotoFileUrl.isNullOrEmpty()){
                startActivityWebViewFromUrl(RetireeUserDocRetrive?.passportPhotoFileUrl)
            }else{
                passportPhotoUri?.let { it1 -> startActivityFromUri(it1) }
            }
            //retiree_app_btn_green_view -> viewButtonCall(RetireeUserDocRetrive?.applicationFormFileUrl)
            retiree_app_btn_green_view ->
                if(!RetireeUserDocRetrive?.applicationFormFileUrl.isNullOrEmpty()){
                startActivityWebViewFromUrl(RetireeUserDocRetrive?.applicationFormFileUrl)
            }else{
                applicationFormUri?.let { it1 -> startActivityFromUri(it1) }
            }
            //retiree_appointment_btn_green_view -> viewButtonCall(RetireeUserDocRetrive?.monthlyArrearsPaymentAppointmentFileUrl)
            retiree_appointment_btn_green_view -> if(!RetireeUserDocRetrive?.monthlyArrearsPaymentAppointmentFileUrl.isNullOrEmpty()){
                startActivityWebViewFromUrl(RetireeUserDocRetrive?.monthlyArrearsPaymentAppointmentFileUrl)
            }else{
                appointmentPayUri?.let { it1 -> startActivityFromUri(it1) }
            }

            //retiree_authorisation_btn_green_view -> viewButtonCall(RetireeUserDocRetrive?.paymentAuthorizationRetirementOrDeathFileUrl)
            retiree_authorisation_btn_green_view -> if(!RetireeUserDocRetrive?.paymentAuthorizationRetirementOrDeathFileUrl.isNullOrEmpty()){
                startActivityWebViewFromUrl(RetireeUserDocRetrive?.paymentAuthorizationRetirementOrDeathFileUrl)
            }else{
                authorisationPayUri?.let { it1 -> startActivityFromUri(it1) }
            }
            //retiree_clearance_btn_green_view -> viewButtonCall(RetireeUserDocRetrive?.clearanceFormFileUrl)
            retiree_clearance_btn_green_view -> if(!RetireeUserDocRetrive?.clearanceFormFileUrl.isNullOrEmpty()){
                startActivityWebViewFromUrl(RetireeUserDocRetrive?.clearanceFormFileUrl)
            }else{
                clearanceFormUri?.let { it1 -> startActivityFromUri(it1) }
            }
            //retiree_notification_btn_green_view -> viewButtonCall(RetireeUserDocRetrive?.promotionNotificationFileUrl)
            retiree_notification_btn_green_view -> if(!RetireeUserDocRetrive?.promotionNotificationFileUrl.isNullOrEmpty()){
                startActivityWebViewFromUrl(RetireeUserDocRetrive?.promotionNotificationFileUrl)
            }else{
                notificationUri?.let { it1 -> startActivityFromUri(it1) }
            }

            //retiree_pension_certificate_btn_green_view -> viewButtonCall(RetireeUserDocRetrive?.pensionLifeCertificateFileUrl)
            retiree_pension_certificate_btn_green_view -> if(!RetireeUserDocRetrive?.pensionLifeCertificateFileUrl.isNullOrEmpty()){
                startActivityWebViewFromUrl(RetireeUserDocRetrive?.pensionLifeCertificateFileUrl)
            }else{
                pensionCertificateUri?.let { it1 -> startActivityFromUri(it1) }
            }


            //retiree_retirement_btn_green_view -> viewButtonCall(RetireeUserDocRetrive?.retirementNoticeFileUrl)
            retiree_retirement_btn_green_view -> if(!RetireeUserDocRetrive?.retirementNoticeFileUrl.isNullOrEmpty()){
                startActivityWebViewFromUrl(RetireeUserDocRetrive?.retirementNoticeFileUrl)
            }else{
                retirementNoticeUri?.let { it1 -> startActivityFromUri(it1) }
            }
            */

            retiree_id_card_btn_green_view -> {
                if (idCardUri != null) {
                    Log.d("viewbutton", "R id card uri ${idCardUri}")
                    startActivityFromUri(idCardUri!!)
                } else if (!RetireeUserDocRetrive?.idCardFileUrl.isNullOrEmpty()) {
                    Log.d("viewbutton", "R id card url ${RetireeUserDocRetrive?.idCardFileUrl}")
                    startActivityWebViewFromUrl(RetireeUserDocRetrive?.idCardFileUrl)
                }
            }
            retiree_passport_photo_btn_green_view -> {
                if (passportPhotoUri != null) {
                    startActivityFromUri(passportPhotoUri!!)
                } else if (!RetireeUserDocRetrive?.passportPhotoFileUrl.isNullOrEmpty()) {
                    startActivityWebViewFromUrl(RetireeUserDocRetrive?.passportPhotoFileUrl)
                }
            }
            retiree_app_btn_green_view -> {
                Log.i("sssssssssssss", "onClick: 1) $applicationFormUri 2) ${RetireeUserDocRetrive?.applicationFormFileUrl}")
                if (applicationFormUri != null) {
                    startActivityFromUri(applicationFormUri!!)
                } else if (!RetireeUserDocRetrive?.applicationFormFileUrl.isNullOrEmpty()) {
                    startActivityWebViewFromUrl(RetireeUserDocRetrive?.applicationFormFileUrl)
                }
            }
            retiree_appointment_btn_green_view -> {
                if (appointmentPayUri != null) {
                    startActivityFromUri(appointmentPayUri!!)
                } else if (!RetireeUserDocRetrive?.monthlyArrearsPaymentAppointmentFileUrl.isNullOrEmpty()) {
                    startActivityWebViewFromUrl(RetireeUserDocRetrive?.monthlyArrearsPaymentAppointmentFileUrl)
                }
            }
            retiree_authorisation_btn_green_view -> {
                if (authorisationPayUri != null) {
                    startActivityFromUri(authorisationPayUri!!)
                } else if (!RetireeUserDocRetrive?.paymentAuthorizationRetirementOrDeathFileUrl.isNullOrEmpty()) {
                    startActivityWebViewFromUrl(RetireeUserDocRetrive?.paymentAuthorizationRetirementOrDeathFileUrl)
                }
            }
            retiree_clearance_btn_green_view -> {
                if (clearanceFormUri != null) {
                    startActivityFromUri(clearanceFormUri!!)
                } else if (!RetireeUserDocRetrive?.clearanceFormFileUrl.isNullOrEmpty()) {
                    startActivityWebViewFromUrl(RetireeUserDocRetrive?.clearanceFormFileUrl)
                }
            }
            retiree_notification_btn_green_view -> {
                if (notificationUri != null) {
                    startActivityFromUri(notificationUri!!)
                } else if (!RetireeUserDocRetrive?.promotionNotificationFileUrl.isNullOrEmpty()) {
                    startActivityWebViewFromUrl(RetireeUserDocRetrive?.promotionNotificationFileUrl)
                }
            }
            retiree_pension_certificate_btn_green_view -> {
                if (pensionCertificateUri != null) {
                    startActivityFromUri(pensionCertificateUri!!)
                } else if (!RetireeUserDocRetrive?.pensionLifeCertificateFileUrl.isNullOrEmpty()) {
                    startActivityWebViewFromUrl(RetireeUserDocRetrive?.pensionLifeCertificateFileUrl)
                }
            }
            retiree_retirement_btn_green_view -> {
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

        if (retiree_app_btn_green_view.visibility == View.VISIBLE && retiree_appointment_btn_green_view.visibility == View.VISIBLE && retiree_authorisation_btn_green_view.visibility == View.VISIBLE &&
            retiree_clearance_btn_green_view.visibility == View.VISIBLE && retiree_notification_btn_green_view.visibility == View.VISIBLE &&
            retiree_pension_certificate_btn_green_view.visibility == View.VISIBLE && retiree_passport_photo_btn_green_view.visibility == View.VISIBLE &&
            retiree_retirement_btn_green_view.visibility == View.VISIBLE && retiree_id_card_btn_green_view.visibility == View.VISIBLE  )
        {
            return true
        }
        return false

    }

    private fun isValidDocs(): Boolean {
        /*
        if (TextUtils.isEmpty(tv_retiree_app_form_filename.text) || cv_retiree_applicationForm_doc.visibility == View.GONE
            || retiree_app_btn_green_view.visibility == View.VISIBLE ) {
            Toast.makeText(context, "Please select Application Form File", Toast.LENGTH_LONG).show()
            return false
        } else if (TextUtils.isEmpty(tv_retiree_appointment_payment_doc_filename.text) || cv_retiree_appointment_payment_doc.visibility == View.GONE
            || retiree_appointment_btn_green_view.visibility == View.VISIBLE) {
            Toast.makeText(context, "Please select Appointment payment File", Toast.LENGTH_LONG)
                .show()
            return false
        } else if (TextUtils.isEmpty(tv_retiree_id_card_doc_filename.text) || cv_retiree_authorisation_payment_doc.visibility == View.GONE
            || retiree_authorisation_btn_green_view.visibility == View.VISIBLE) {
            Toast.makeText(context, " Please select Authorization payment File", Toast.LENGTH_LONG)
                .show()
            return false
        } else if (TextUtils.isEmpty(tv_retiree_passport_photo_doc_filename.text) || cv_retiree_clearance_form_doc.visibility == View.GONE
            || retiree_clearance_btn_green_view.visibility == View.VISIBLE) {
            Toast.makeText(context, "Please select Clearance Form File ", Toast.LENGTH_LONG).show()
            return false
        } else if (TextUtils.isEmpty(tv_retiree_notification_promotion_doc_filename.text) || cv_retiree_notification_promotion_doc.visibility == View.GONE
            || retiree_notification_btn_green_view.visibility == View.VISIBLE) {
            Toast.makeText(context, "Please select Notification Promotion File ", Toast.LENGTH_LONG)
                .show()
            return false
        } else if (TextUtils.isEmpty(tv_retiree_pension_certificate_doc_filename.text) || cv_retiree_pension_certificate_doc.visibility == View.GONE
            || retiree_pension_certificate_btn_green_view.visibility == View.VISIBLE) {
            Toast.makeText(context, "Please select Pension Certification File ", Toast.LENGTH_LONG)
                .show()
            return false
        } else if (TextUtils.isEmpty(tv_retiree_passport_photo_doc_filename.text) || cv_retiree_passport_photo_doc.visibility == View.GONE
            || retiree_passport_photo_btn_green_view.visibility == View.VISIBLE) {
            Toast.makeText(context, "Please select Photo", Toast.LENGTH_LONG).show()
            return false
        } else if (TextUtils.isEmpty(tv_retiree_retirement_notice_doc_filename.text) || cv_retiree_retirement_notice_doc.visibility == View.GONE
            || retiree_retirement_btn_green_view.visibility == View.VISIBLE) {
            Toast.makeText(context, "Please select Retirement Notice File", Toast.LENGTH_LONG)
                .show()
            return false
        } else if (TextUtils.isEmpty(tv_retiree_id_card_doc_filename.text) || cv_retiree_id_card_doc.visibility == View.GONE
            || retiree_id_card_btn_green_view.visibility == View.VISIBLE) {
            Toast.makeText(context, "Please select Id Card ", Toast.LENGTH_LONG).show()
            return false
        }*/


        if (TextUtils.isEmpty(tv_retiree_id_card_doc_filename.text) || cv_retiree_id_card_doc.visibility == View.GONE) {
            Toast.makeText(context, "Please select Id Card", Toast.LENGTH_LONG).show()
            return false
        }
        /*else if (TextUtils.isEmpty(tv_retiree_passport_photo_doc_filename.text) || cv_retiree_passport_photo_doc.visibility == View.GONE) {
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
                cv_retiree_applicationForm_doc.visibility = View.VISIBLE
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

                    tv_retiree_app_form_filename.text = file.name

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
                cv_retiree_appointment_payment_doc.visibility = View.VISIBLE
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

                    tv_retiree_appointment_payment_doc_filename.text = file.name

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
                cv_retiree_authorisation_payment_doc.visibility = View.VISIBLE
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

                    tv_retiree_authorisation_payment_doc_filename.text = file.name

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
                cv_retiree_clearance_form_doc.visibility = View.VISIBLE
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

                    tv_retiree_clearance_form_doc_filename.text = file.name

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
                cv_retiree_notification_promotion_doc.visibility = View.VISIBLE
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

                    tv_retiree_notification_promotion_doc_filename.text = file.name

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
                cv_retiree_pension_certificate_doc.visibility = View.VISIBLE
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

                    tv_retiree_pension_certificate_doc_filename.text = file.name

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
                cv_retiree_passport_photo_doc.visibility = View.VISIBLE
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

                    tv_retiree_passport_photo_doc_filename.text = file.name

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
                cv_retiree_retirement_notice_doc.visibility = View.VISIBLE
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

                    tv_retiree_retirement_notice_doc_filename.text = file.name

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
                cv_retiree_id_card_doc.visibility = View.VISIBLE
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

                    tv_retiree_id_card_doc_filename.text = file.name

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

                cv_retiree_applicationForm.visibility= View.VISIBLE
                img_retiree_app_form_close.visibility= View.VISIBLE

                ll_retiree_app_form_uploadprogress.visibility = View.VISIBLE
                ll_retiree_app_form_percentage.visibility =View.VISIBLE
                pb_retiree_app_form.visibility =View.VISIBLE
                ll_retiree_form_size.visibility = View.GONE

                Thread(Runnable {
                    while (form_status < 100) {
                        form_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        form_handler.post(Runnable {
                            pb_retiree_app_form?.setProgress(form_status)

                            tv_retiree_doc_percent.text = form_status.toString() + " %"
                            if (form_status == 100) {

                                retiree_app_btn_green_view.visibility = View.VISIBLE
                                ll_retiree_app_form_uploadprogress.visibility = View.GONE
                                ll_retiree_form_size.visibility = View.VISIBLE
                                txt_retiree_filesize.text = pdfSize
                            }
                        })
                    }
                }).start()


            }

            RETIREE_APPOINTMENT_PAYMENT_FILE -> {
                var form_status = 0
                val form_handler: Handler = Handler()

                cv_retiree_appointment_payment.visibility= View.VISIBLE
                img_retiree_appointment_payment_doc_close.visibility= View.VISIBLE

                ll_retiree_appointment_payment_doc_uploadprogress.visibility = View.VISIBLE
                ll_retiree_appointment_payment_doc_percentage.visibility = View.VISIBLE
                pb_retiree_appointment_payment_doc.visibility = View.VISIBLE
                ll_retiree_appointment_payment_doc_size.visibility = View.GONE

                Thread(Runnable {
                    while (form_status < 100) {
                        form_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        form_handler.post(Runnable {
                            pb_retiree_appointment_payment_doc?.setProgress(form_status)

                            tv_retiree_appointment_payment_doc_percent.text =
                                form_status.toString() + " %"
                            if (form_status == 100) {

                                retiree_appointment_btn_green_view.visibility = View.VISIBLE

                                ll_retiree_appointment_payment_doc_uploadprogress.visibility =
                                    View.GONE
                                ll_retiree_appointment_payment_doc_size.visibility = View.VISIBLE
                                txt_appointment_payment_doc_filesize.text = pdfSize
                            }
                        })
                    }
                }).start()


            }

            RETIREE_AUTHORISATION_PAYMENT_FILE -> {
                var form_status = 0
                val form_handler: Handler = Handler()

                cv_retiree_authorisation_payment_doc.visibility= View.VISIBLE
                img_retiree_authorisation_payment_doc_close.visibility= View.VISIBLE

                ll_retiree_authorisation_payment_doc_uploadprogress.visibility = View.VISIBLE
                ll_retiree_authorisation_payment_doc_percentage.visibility =View.VISIBLE
                pb_retiree_authorisation_payment_doc.visibility = View.VISIBLE
                ll_retiree_authorisation_payment_doc_size.visibility = View.GONE

                Thread(Runnable {
                    while (form_status < 100) {
                        form_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        form_handler.post(Runnable {
                            pb_retiree_authorisation_payment_doc?.setProgress(form_status)
                            tv_retiree_authorisation_payment_doc_percent.text =
                                form_status.toString() + " %"
                            if (form_status == 100) {
                                retiree_authorisation_btn_green_view.visibility = View.VISIBLE

                                ll_retiree_authorisation_payment_doc_uploadprogress.visibility =
                                    View.GONE
                                ll_retiree_authorisation_payment_doc_size.visibility = View.VISIBLE
                                txt_authorisation_payment_doc_filesize.text = pdfSize
                            }
                        })
                    }
                }).start()


            }

            RETIREE_CLEARANCE_FORM_FILE -> {
                var form_status = 0
                val form_handler: Handler = Handler()

                cv_retiree_clearance_form_doc.visibility= View.VISIBLE
                img_retiree_clearance_form_doc_close.visibility= View.VISIBLE

                ll_retiree_clearance_form_doc_uploadprogress.visibility = View.VISIBLE
                ll_retiree_clearance_form_doc_percentage.visibility =View.VISIBLE
                pb_retiree_clearance_form_doc.visibility =View.VISIBLE
                ll_retiree_clearance_form_doc_size.visibility = View.GONE

                Thread(Runnable {
                    while (form_status < 100) {
                        form_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        form_handler.post(Runnable {
                            pb_retiree_clearance_form_doc?.setProgress(form_status)

                            tv_retiree_clearance_form_doc_percent.text =
                                form_status.toString() + " %"
                            if (form_status == 100) {
                                retiree_clearance_btn_green_view.visibility = View.VISIBLE

                                ll_retiree_clearance_form_doc_uploadprogress.visibility = View.GONE
                                ll_retiree_clearance_form_doc_size.visibility = View.VISIBLE
                                txt_clearance_form_doc_filesize.text = pdfSize
                            }
                        })
                    }
                }).start()


            }

            RETIREE_NOTIFICATION_PROMOTION_FILE -> {
                var form_status = 0
                val form_handler: Handler = Handler()

                cv_retiree_notification_promotion_doc.visibility= View.VISIBLE
                img_retiree_notification_promotion_doc_close.visibility= View.VISIBLE

                ll_retiree_notification_promotion_doc_uploadprogress.visibility = View.VISIBLE
                ll_retiree_notification_promotion_doc_percentage.visibility = View.VISIBLE
                pb_retiree_notification_promotion_doc.visibility = View.VISIBLE
                ll_retiree_notification_promotion_doc_size.visibility = View.GONE

                Thread(Runnable {
                    while (form_status < 100) {
                        form_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        form_handler.post(Runnable {
                            pb_retiree_notification_promotion_doc?.setProgress(form_status)

                            tv_retiree_notification_promotion_doc_percent.text =
                                form_status.toString() + " %"
                            if (form_status == 100) {
                                retiree_notification_btn_green_view.visibility = View.VISIBLE

                                ll_retiree_notification_promotion_doc_uploadprogress.visibility =
                                    View.GONE
                                ll_retiree_notification_promotion_doc_size.visibility = View.VISIBLE
                                txt_notification_promotion_doc_filesize.text = pdfSize

                            }
                        })
                    }
                }).start()


            }

            RETIREE_PENSION_CERTIFICATE_FILE -> {
                var form_status = 0
                val form_handler: Handler = Handler()

                cv_retiree_pension_certificate_doc.visibility= View.VISIBLE
                img_retiree_pension_certificate_doc_close.visibility= View.VISIBLE

                ll_retiree_pension_certificate_doc_uploadprogress.visibility = View.VISIBLE
                ll_retiree_pension_certificate_doc_percentage.visibility =View.VISIBLE
                pb_retiree_pension_certificate_doc.visibility =View.VISIBLE
                ll_retiree_pension_certificate_doc_size.visibility = View.GONE

                Thread(Runnable {
                    while (form_status < 100) {
                        form_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        form_handler.post(Runnable {
                            pb_retiree_pension_certificate_doc?.setProgress(form_status)

                            tv_retiree_pension_certificate_doc_percent.text =
                                form_status.toString() + " %"
                            if (form_status == 100) {
                                retiree_pension_certificate_btn_green_view.visibility = View.VISIBLE

                                ll_retiree_pension_certificate_doc_uploadprogress.visibility =
                                    View.GONE
                                ll_retiree_pension_certificate_doc_size.visibility = View.VISIBLE
                                txt_pension_certificate_doc_filesize.text = pdfSize
                            }
                        })
                    }
                }).start()


            }

            RETIREE_PASSPORT_PHOTO_FILE -> {
                var form_status = 0
                val form_handler: Handler = Handler()

                cv_retiree_passport_photo_doc.visibility= View.VISIBLE
                img_retiree_passport_photo_doc_close.visibility= View.VISIBLE

                ll_retiree_passport_photo_doc_uploadprogress.visibility = View.VISIBLE
                ll_retiree_app_passport_photo_doc_percentage.visibility =View.VISIBLE
                pb_retiree_passport_photo_doc.visibility =View.VISIBLE
                ll_retiree_passport_photo_doc_size.visibility = View.GONE

                Thread(Runnable {
                    while (form_status < 100) {
                        form_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        form_handler.post(Runnable {
                            pb_retiree_passport_photo_doc?.setProgress(form_status)

                            tv_retiree_passport_photo_doc_percent.text =
                                form_status.toString() + " %"
                            if (form_status == 100) {
                                retiree_passport_photo_btn_green_view.visibility = View.VISIBLE

                                ll_retiree_passport_photo_doc_uploadprogress.visibility = View.GONE
                                ll_retiree_passport_photo_doc_size.visibility = View.VISIBLE
                                txt_passport_photo_doc_filesize.text = pdfSize
                            }
                        })
                    }
                }).start()


            }

            RETIREE_RETIREMENT_NOTICE_FILE -> {
                var form_status = 0
                val form_handler: Handler = Handler()

                cv_retiree_retirement_notice_doc.visibility= View.VISIBLE
                img_retiree_retirement_notice_doc_close.visibility= View.VISIBLE

                ll_retiree_retirement_notice_doc_uploadprogress.visibility = View.VISIBLE
                ll_retiree_retirement_notice_doc_percentage.visibility =View.VISIBLE
                pb_retiree_retirement_notice_doc.visibility = View.VISIBLE
                ll_retiree_retirement_notice_doc_size.visibility = View.GONE

                Thread(Runnable {
                    while (form_status < 100) {
                        form_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        form_handler.post(Runnable {
                            pb_retiree_retirement_notice_doc?.setProgress(form_status)

                            tv_retiree_retirement_notice_doc_percent.text =
                                form_status.toString() + " %"
                            if (form_status == 100) {
                                retiree_retirement_btn_green_view.visibility = View.VISIBLE

                                ll_retiree_retirement_notice_doc_uploadprogress.visibility =
                                    View.GONE
                                ll_retiree_retirement_notice_doc_size.visibility = View.VISIBLE
                                txt_retirement_notice_doc_filesize.text = pdfSize
                            }
                        })
                    }
                }).start()

            }

            RETIREE_ID_CARD_FILE -> {
                var form_status = 0
                val form_handler: Handler = Handler()

                cv_retiree_id_card_doc.visibility= View.VISIBLE
                img_retiree_id_card_doc_close.visibility= View.VISIBLE

                ll_retiree_id_card_doc_uploadprogress.visibility = View.VISIBLE
                ll_retiree_id_card_doc_percentage.visibility = View.VISIBLE
                pb_retiree_id_card_doc.visibility = View.VISIBLE
                ll_retiree_id_card_doc_size.visibility = View.GONE

                Thread(Runnable {
                    while (form_status < 100) {
                        form_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        form_handler.post(Runnable {
                            pb_retiree_id_card_doc?.setProgress(form_status)

                            tv_retiree_id_card_doc_percent.text = form_status.toString() + " %"
                            if (form_status == 100) {

                                retiree_id_card_btn_green_view.visibility = View.VISIBLE

                                ll_retiree_id_card_doc_uploadprogress.visibility = View.GONE
                                ll_retiree_id_card_doc_size.visibility = View.VISIBLE
                                txt_id_card_doc_filesize.text = pdfSize
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
                setDocumentView(retireeUserDoc.idCardFileUrl, retiree_id_card_btn_green_view, cv_retiree_id_card_doc, tv_retiree_id_card_doc_filename, img_retiree_id_card_doc_close, ll_retiree_id_card_doc_uploadprogress, ll_retiree_id_card_doc_percentage, pb_retiree_id_card_doc)
                // Optional documents
                setDocumentViewIfPresent(retireeUserDoc.passportPhotoFileUrl, retiree_passport_photo_btn_green_view, cv_retiree_passport_photo_doc, tv_retiree_passport_photo_doc_filename, img_retiree_passport_photo_doc_close, ll_retiree_passport_photo_doc_uploadprogress, ll_retiree_app_passport_photo_doc_percentage, pb_retiree_passport_photo_doc)
                setDocumentViewIfPresent(retireeUserDoc.applicationFormFileUrl, retiree_app_btn_green_view, cv_retiree_applicationForm_doc, tv_retiree_app_form_filename, img_retiree_app_form_close, ll_retiree_app_form_uploadprogress, ll_retiree_app_form_percentage, pb_retiree_app_form)
                setDocumentViewIfPresent(retireeUserDoc.monthlyArrearsPaymentAppointmentFileUrl, retiree_appointment_btn_green_view, cv_retiree_appointment_payment_doc, tv_retiree_appointment_payment_doc_filename, img_retiree_appointment_payment_doc_close, ll_retiree_appointment_payment_doc_uploadprogress, ll_retiree_appointment_payment_doc_percentage, pb_retiree_appointment_payment_doc)
                setDocumentViewIfPresent(retireeUserDoc.paymentAuthorizationRetirementOrDeathFileUrl, retiree_authorisation_btn_green_view, cv_retiree_authorisation_payment_doc, tv_retiree_authorisation_payment_doc_filename, img_retiree_authorisation_payment_doc_close, ll_retiree_authorisation_payment_doc_uploadprogress, ll_retiree_authorisation_payment_doc_percentage, pb_retiree_authorisation_payment_doc)
                setDocumentViewIfPresent(retireeUserDoc.clearanceFormFileUrl, retiree_clearance_btn_green_view, cv_retiree_clearance_form_doc, tv_retiree_clearance_form_doc_filename, img_retiree_clearance_form_doc_close, ll_retiree_clearance_form_doc_uploadprogress, ll_retiree_clearance_form_doc_percentage, pb_retiree_clearance_form_doc)
                setDocumentViewIfPresent(retireeUserDoc.promotionNotificationFileUrl, retiree_notification_btn_green_view, cv_retiree_notification_promotion_doc, tv_retiree_notification_promotion_doc_filename, img_retiree_notification_promotion_doc_close, ll_retiree_notification_promotion_doc_uploadprogress, ll_retiree_notification_promotion_doc_percentage, pb_retiree_notification_promotion_doc)
                setDocumentViewIfPresent(retireeUserDoc.pensionLifeCertificateFileUrl, retiree_pension_certificate_btn_green_view, cv_retiree_pension_certificate_doc, tv_retiree_pension_certificate_doc_filename, img_retiree_pension_certificate_doc_close, ll_retiree_pension_certificate_doc_uploadprogress, ll_retiree_pension_certificate_doc_percentage, pb_retiree_pension_certificate_doc)
                setDocumentViewIfPresent(retireeUserDoc.retirementNoticeFileUrl, retiree_retirement_btn_green_view, cv_retiree_retirement_notice_doc, tv_retiree_retirement_notice_doc_filename, img_retiree_retirement_notice_doc_close, ll_retiree_retirement_notice_doc_uploadprogress, ll_retiree_retirement_notice_doc_percentage, pb_retiree_retirement_notice_doc)

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
            retiree_app_btn_green_view.setOnClickListener(this)
            retiree_appointment_btn_green_view.setOnClickListener(this)
            retiree_authorisation_btn_green_view.setOnClickListener(this)
            retiree_clearance_btn_green_view.setOnClickListener(this)
            retiree_notification_btn_green_view.setOnClickListener(this)
            retiree_pension_certificate_btn_green_view.setOnClickListener(this)
            retiree_passport_photo_btn_green_view.setOnClickListener(this)
            retiree_retirement_btn_green_view.setOnClickListener(this)
            retiree_id_card_btn_green_view.setOnClickListener(this)
             */

            //visible view btn mandatory

            retiree_id_card_btn_green_view.visibility = View.VISIBLE
            retiree_passport_photo_btn_green_view.visibility = View.VISIBLE

            //retiree id Card
            cv_retiree_id_card_doc.visibility = View.VISIBLE
            tv_retiree_id_card_doc_filename.text = idcard_url!!.substringAfterLast('/')
            img_retiree_id_card_doc_close.visibility = View.VISIBLE
            ll_retiree_id_card_doc_uploadprogress.visibility = View.GONE
            ll_retiree_id_card_doc_percentage.visibility = View.GONE
            pb_retiree_id_card_doc.visibility = View.GONE

            //PassPort photo
            cv_retiree_passport_photo_doc.visibility = View.VISIBLE
            tv_retiree_passport_photo_doc_filename.text = passPhoto_url!!.substringAfterLast('/')
            img_retiree_passport_photo_doc_close.visibility = View.VISIBLE
            ll_retiree_passport_photo_doc_uploadprogress.visibility = View.GONE
            ll_retiree_app_passport_photo_doc_percentage.visibility = View.GONE
            pb_retiree_passport_photo_doc.visibility = View.GONE



            //optional button visible
            //app form
            if (!appform_url.isNullOrEmpty()){
                retiree_app_btn_green_view.visibility = View.VISIBLE
                cv_retiree_applicationForm_doc.visibility = View.VISIBLE
            tv_retiree_app_form_filename.text = appform_url!!.substringAfterLast('/')

            img_retiree_app_form_close.visibility = View.VISIBLE
            ll_retiree_app_form_uploadprogress.visibility = View.GONE
            ll_retiree_app_form_percentage.visibility = View.GONE
            pb_retiree_app_form.visibility = View.GONE
            }

            //tv_active_app_form_filename.text = ActiveUserDocRetrive?.applicationFormFileUrl
            //ll_active_app_form_uploadprogress.visibility = View.GONE

            //Appoinment payment
            if (!appoinment_payment_url.isNullOrEmpty()){
            retiree_appointment_btn_green_view.visibility = View.VISIBLE

            cv_retiree_appointment_payment_doc.visibility = View.VISIBLE
            tv_retiree_appointment_payment_doc_filename.text = appoinment_payment_url!!.substringAfterLast('/')

            img_retiree_appointment_payment_doc_close.visibility = View.VISIBLE
            ll_retiree_appointment_payment_doc_uploadprogress.visibility = View.GONE
            ll_retiree_appointment_payment_doc_percentage.visibility = View.GONE
            pb_retiree_appointment_payment_doc.visibility = View.GONE
            }

            //Authorization Payment
            if (!autherization_payment_url.isNullOrEmpty()) {

                retiree_authorisation_btn_green_view.visibility = View.VISIBLE
                cv_retiree_authorisation_payment_doc.visibility = View.VISIBLE
                tv_retiree_authorisation_payment_doc_filename.text =
                    autherization_payment_url!!.substringAfterLast('/')

                img_retiree_authorisation_payment_doc_close.visibility = View.VISIBLE
                ll_retiree_authorisation_payment_doc_uploadprogress.visibility = View.GONE
                ll_retiree_authorisation_payment_doc_percentage.visibility = View.GONE
                pb_retiree_authorisation_payment_doc.visibility = View.GONE
            }

            //clearance
            if (!clearrance_url.isNullOrEmpty()){
                retiree_clearance_btn_green_view.visibility = View.VISIBLE
                cv_retiree_clearance_form_doc.visibility = View.VISIBLE
            tv_retiree_clearance_form_doc_filename.text = clearrance_url!!.substringAfterLast('/')

            img_retiree_clearance_form_doc_close.visibility = View.VISIBLE
            ll_retiree_clearance_form_doc_uploadprogress.visibility = View.GONE
            ll_retiree_clearance_form_doc_percentage.visibility = View.GONE
            pb_retiree_clearance_form_doc.visibility = View.GONE
            }

            //notification promotion
            if (!notification_promotion_url.isNullOrEmpty()) {
                retiree_notification_btn_green_view.visibility = View.VISIBLE

                cv_retiree_notification_promotion_doc.visibility = View.VISIBLE
                tv_retiree_notification_promotion_doc_filename.text =
                    notification_promotion_url!!.substringAfterLast('/')

                img_retiree_notification_promotion_doc_close.visibility = View.VISIBLE
                ll_retiree_notification_promotion_doc_uploadprogress.visibility = View.GONE
                ll_retiree_notification_promotion_doc_percentage.visibility = View.GONE
                pb_retiree_notification_promotion_doc.visibility = View.GONE
            }


            //Pension certification
            if (!pension_certificate_url.isNullOrEmpty()) {
                retiree_pension_certificate_btn_green_view.visibility = View.VISIBLE

                cv_retiree_pension_certificate_doc.visibility = View.VISIBLE
                tv_retiree_pension_certificate_doc_filename.text =
                    pension_certificate_url!!.substringAfterLast('/')

                img_retiree_pension_certificate_doc_close.visibility = View.VISIBLE
                ll_retiree_pension_certificate_doc_uploadprogress.visibility = View.GONE
                ll_retiree_pension_certificate_doc_percentage.visibility = View.GONE
                pb_retiree_pension_certificate_doc.visibility = View.GONE
            }



            //retiree retirement
            if (!retirement_url.isNullOrEmpty()) {
                retiree_retirement_btn_green_view.visibility = View.VISIBLE

                cv_retiree_retirement_notice_doc.visibility = View.VISIBLE
                tv_retiree_retirement_notice_doc_filename.text =
                    retirement_url!!.substringAfterLast('/')

                img_retiree_retirement_notice_doc_close.visibility = View.VISIBLE
                ll_retiree_retirement_notice_doc_uploadprogress.visibility = View.GONE
                ll_retiree_retirement_notice_doc_percentage.visibility = View.GONE
                pb_retiree_retirement_notice_doc.visibility = View.GONE
            }
            //enableDisableTabs(tab_tablayout_retiree, true, true, true)
        }
    }
}