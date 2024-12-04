package com.example.engu_pension_verification_application.ui.fragment.service.active

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.commons.setDocumentView
import com.example.engu_pension_verification_application.commons.setDocumentViewIfPresent
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.databinding.FragmentActiveDocumentsBinding
import com.example.engu_pension_verification_application.model.response.FileUrlResponse
import com.example.engu_pension_verification_application.model.response.ResponseActiveDocRetrive
import com.example.engu_pension_verification_application.model.response.ResponseActiveDocUpload
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.ui.activity.WebView.ActiveDocWebViewActivity
import com.example.engu_pension_verification_application.ui.fragment.base.BaseFragment
import com.example.engu_pension_verification_application.util.OnboardingStage
import com.example.engu_pension_verification_application.util.SharedPref
import com.example.engu_pension_verification_application.viewmodel.ActiveDocumentsViewModel
import com.example.engu_pension_verification_application.viewmodel.ActiveServiceViewModel
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
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


class ActiveDocumentsFragment(
) : BaseFragment(),
    View.OnClickListener
{
    private lateinit var binding:FragmentActiveDocumentsBinding
    private val activeServiceViewModel by activityViewModels<ActiveServiceViewModel>()
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    private lateinit var activeDocumentsViewModel: ActiveDocumentsViewModel

    val prefs = SharedPref

    val mimeTypes = arrayOf("image/jpeg", "image/png", "application/pdf")

    val uris = mutableListOf<Uri>()

    private var applicationFormUri: Uri? = null
    private var letterFileUri: Uri? = null
    private var idCardUri: Uri? = null
    private var PhotoUri: Uri? = null
    private var ClearanceUri: Uri? = null


    companion object {
        const val TAB_POSITION = 1
        const val APPLICATION_FORM_FILE = 101
        const val LETTER_FILE = 102
        const val IDCARD_FILE = 103
        const val PHOTO_FILE = 104
        const val CLEARANCE_FORM_FILE = 105

        /*fun newInstance(viewPageCallBack: ViewPageCallBack, tabAccessControl: TabAccessControl): ActiveDocumentsFragment {
            return ActiveDocumentsFragment(viewPageCallBack, tabAccessControl).apply {
                this.viewPageCallBack = viewPageCallBack
            }
        }*/
    }

    var ActiveUserDocRetrive: FileUrlResponse? = null
    var responseActiveDocRetrive: ResponseActiveDocRetrive? = null
    var applicationform_file: File? = null
    var applicationform_filename = ""
    var applicationform_fileMIME = ""


    var promotion_letter_file: File? = null
    var promotion_letter_filename = ""
    var promotion_letter_fileMIME = ""


    var id_card_file: File? = null
    var id_card_filename = ""
    var id_card_fileMIME = ""


    var passport_photo_file: File? = null
    var passport_photo_filename = ""
    var passport_photo_fileMIME = ""

    var clearance_form_file: File? = null
    var clearance_form_filename = ""
    var clearance_form_fileMIME = ""



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActiveDocumentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        initViews()
        observeLiveData()
    }

    private fun initViews() {
        binding.llActiveAppFormUpload.setOnClickListener(this)
        binding.imgActiveAppFormClose.setOnClickListener(this)
        binding.llActivePromotionLettermUpload.setOnClickListener(this)
        binding.imgActivePromotionLetterClose.setOnClickListener(this)
        binding.llActiveIdCardUpload.setOnClickListener(this)
        binding.imgActiveIdCardClose.setOnClickListener(this)
        binding.llActivePassportPhotoUpload.setOnClickListener(this)
        binding.imgActivePassportPhotoClose.setOnClickListener(this)
        binding.llActiveClearenceFormUpload.setOnClickListener(this)
        binding.imgActiveClearenceFormClose.setOnClickListener(this)
        binding.llActivedocNext.setOnClickListener(this)
        binding.tvActiveAppFormFilename.setOnClickListener(this)

        //observeActiveDoc()

        binding.appFormBtnGreenView.setOnClickListener(this)
        binding.promotionLetterBtnGreenView.setOnClickListener(this)
        binding.aIdCardBtnGreenView.setOnClickListener(this)
        binding.aPassportPhotoBtnGreenView.setOnClickListener(this)
        binding.aClearenceFormBtnGreenView.setOnClickListener(this)
    }
    private fun initViewModels() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        activeDocumentsViewModel = ViewModelProviders.of(
            this,
            EnguViewModelFactory(networkRepo)
        ).get(ActiveDocumentsViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), 
            EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }
    private fun observeLiveData() {
        activeServiceViewModel.currentTabPos.observe(viewLifecycleOwner){
            if (it == TAB_POSITION) DocRetrivecall()
        }
        activeDocumentsViewModel.documentsApiResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                ActiveUserDocRetrive = response.detail.fileUrlResponse
                responseActiveDocRetrive = response
                populateViews()
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            activeDocumentsViewModel.fetchActiveDocuments()
                        }
                    }
                } else {
                    dismissLoader()
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        activeDocumentsViewModel.documentsUploadResult.observe(viewLifecycleOwner) { pair ->
            dismissLoader()
            val request = pair.first
            val response = pair.second
            if (response.detail?.status == AppConstants.SUCCESS) {
                onDocUploadSuccess(response) //
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            activeDocumentsViewModel.uploadDocuments(request)
                        }
                    }
                } else {
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onClick(it: View?) {
        when (it) {

            binding.llActiveAppFormUpload -> {
                selectPdfJpegPng(APPLICATION_FORM_FILE)
                binding.appFormBtnGreenView.visibility = View.GONE
                binding.cvActiveAppForm.visibility = View.GONE

                /*binding.llActiveAppFormUploadprogress.visibility = View.VISIBLE
                binding.llActiveAppFormPercentage.visibility = View.VISIBLE
                binding.pbActiveAppForm.visibility = View.VISIBLE*/


            }

            binding.imgActiveAppFormClose -> {
                binding.appFormBtnGreenView.visibility = View.GONE
                binding.cvActiveAppForm.visibility = View.GONE
            }

            binding.llActivePromotionLettermUpload -> {
                selectPdfJpegPng(LETTER_FILE)
                binding.promotionLetterBtnGreenView.visibility = View.GONE
                binding.cvActivePromotionLetter.visibility = View.GONE/*binding.llActivePromotionLetterUploadprogress.visibility = View.VISIBLE
                binding.llActivePromotionLetterPercentage.visibility = View.VISIBLE
                binding.pbActivePromotionLetter.visibility = View.VISIBLE*/


            }

            binding.imgActivePromotionLetterClose -> {
                binding.promotionLetterBtnGreenView.visibility = View.GONE
                binding.cvActivePromotionLetter.visibility = View.GONE
            }

            binding.llActiveIdCardUpload -> {
                selectPdfJpegPng(IDCARD_FILE)
                binding.aIdCardBtnGreenView.visibility = View.GONE
                binding.cvActiveIdCard.visibility = View.GONE/*binding.llActiveIdCardUploadprogress.visibility = View.VISIBLE
                binding.llActiveIdCardPercentage.visibility = View.VISIBLE
                binding.pbActiveIdCard.visibility = View.VISIBLE*/


            }

            binding.imgActiveIdCardClose -> {
                binding.aIdCardBtnGreenView.visibility = View.GONE
                binding.cvActiveIdCard.visibility = View.GONE
            }

            binding.llActivePassportPhotoUpload -> {
                selectPdfJpegPng(PHOTO_FILE)
                binding.aPassportPhotoBtnGreenView.visibility = View.GONE
                binding.cvActivePassportPhoto.visibility = View.GONE/*binding.llActivePassportPhotoUploadprogress.visibility = View.VISIBLE
                binding.llActivePassportPhotoPercentage.visibility = View.VISIBLE
                binding.pbActivePassportPhoto.visibility = View.VISIBLE*/

            }

            binding.imgActivePassportPhotoClose -> {
                binding.aPassportPhotoBtnGreenView.visibility = View.GONE
                binding.cvActivePassportPhoto.visibility = View.GONE
            }

            binding.llActiveClearenceFormUpload -> {
                selectPdfJpegPng(CLEARANCE_FORM_FILE)
                binding.aClearenceFormBtnGreenView.visibility = View.GONE
                binding.cvActiveClearenceForm.visibility = View.GONE/*binding.llActiveClearenceFormUploadprogress.visibility = View.VISIBLE
                binding.llActiveClearenceFormPercentage.visibility = View.VISIBLE
                binding.pbActiveClearenceForm.visibility = View.VISIBLE*/


            }

            binding.imgActiveClearenceFormClose -> {
                binding.aClearenceFormBtnGreenView.visibility = View.GONE
                binding.cvActiveClearenceForm.visibility = View.GONE
            }

            binding.llActivedocNext -> {
                if (!ActiveUserDocRetrive?.idCardFileUrl.isNullOrEmpty()) {
                    if (prefs.onboardingStage == OnboardingStage.ACTIVE_DOCUMENTS)
                        prefs.onboardingStage = OnboardingStage.ACTIVE_BANK_INFO
                    activeServiceViewModel.moveToNextTab()
                    activeServiceViewModel.refreshTabsState()
                }else if (isValidDocs()) {
                    Log.d("Debug", "isValidDocs is true")
                    nextButtonCall()
                } else {
                    Log.d("Debug", "isValidDocs is false")
                }
            }


            /*
            //from device or web view when view button clicked
            binding.aIdCardBtnGreenView ->
                if(!ActiveUserDocRetrive?.idCardFileUrl.isNullOrEmpty()){

                    Log.d("viewbutton", " id cardurl ${ActiveUserDocRetrive?.idCardFileUrl}")
                    startActivityWebViewFromUrl(ActiveUserDocRetrive?.idCardFileUrl)
                }else{
                    Log.d("viewbutton", " id card uri  ${idCardUri}")
                    Log.d("viewbutton", " id card url ${ActiveUserDocRetrive?.idCardFileUrl}")
                    idCardUri?.let { it1 -> startActivityFromUri(it1) }
                }
            binding.aPassportPhotoBtnGreenView ->
                if(!ActiveUserDocRetrive?.passportPhotoFileUrl.isNullOrEmpty()){
                    Log.d("viewbutton", " passport url ${ActiveUserDocRetrive?.passportPhotoFileUrl}")
                    startActivityWebViewFromUrl(ActiveUserDocRetrive?.passportPhotoFileUrl)
                }else{
                    Log.d("viewbutton", " passport uri ${PhotoUri}")
                    Log.d("viewbutton", " passport url ${ActiveUserDocRetrive?.passportPhotoFileUrl}")
                    PhotoUri?.let { it1 -> startActivityFromUri(it1) }
                }

            binding.appFormBtnGreenView ->
                if(!ActiveUserDocRetrive?.applicationFormFileUrl.isNullOrEmpty()){
                startActivityWebViewFromUrl(ActiveUserDocRetrive?.applicationFormFileUrl)
                }else{
                applicationFormUri?.let { it1 -> startActivityFromUri(it1) }
                }
            binding.promotionLetterBtnGreenView ->
                if(!ActiveUserDocRetrive?.promotionLetterTransferLetterFileUrl.isNullOrEmpty()){
                    startActivityWebViewFromUrl(ActiveUserDocRetrive?.promotionLetterTransferLetterFileUrl)
                }else{
                    letterFileUri?.let { it1 -> startActivityFromUri(it1) }
                }

            binding.aClearenceFormBtnGreenView ->
                if(!ActiveUserDocRetrive?.clearanceFormFileUrl.isNullOrEmpty()){
                startActivityWebViewFromUrl(ActiveUserDocRetrive?.clearanceFormFileUrl)
            }else{
                ClearanceUri?.let { it1 -> startActivityFromUri(it1) }
            }*/
            binding.aIdCardBtnGreenView -> {
                if (idCardUri != null) {
                    Log.d("viewbutton", "id card uri ${idCardUri}")
                    startActivityFromUri(idCardUri!!)
                } else if (!ActiveUserDocRetrive?.idCardFileUrl.isNullOrEmpty()) {
                    Log.d("viewbutton", "id card url ${ActiveUserDocRetrive?.idCardFileUrl}")
                    startActivityWebViewFromUrl(ActiveUserDocRetrive?.idCardFileUrl)
                }
            }

            binding.aPassportPhotoBtnGreenView -> {
                if (PhotoUri != null) {
                    Log.d("viewbutton", "passport uri ${PhotoUri}")
                    startActivityFromUri(PhotoUri!!)
                } else if (!ActiveUserDocRetrive?.passportPhotoFileUrl.isNullOrEmpty()) {
                    Log.d("viewbutton", "passport url ${ActiveUserDocRetrive?.passportPhotoFileUrl}")
                    startActivityWebViewFromUrl(ActiveUserDocRetrive?.passportPhotoFileUrl)
                }
            }

            binding.appFormBtnGreenView -> {
                if (applicationFormUri != null) {
                    startActivityFromUri(applicationFormUri!!)
                } else if (!ActiveUserDocRetrive?.applicationFormFileUrl.isNullOrEmpty()) {
                    startActivityWebViewFromUrl(ActiveUserDocRetrive?.applicationFormFileUrl)
                }
            }

            binding.promotionLetterBtnGreenView -> {
                if (letterFileUri != null) {
                    startActivityFromUri(letterFileUri!!)
                } else if (!ActiveUserDocRetrive?.promotionLetterTransferLetterFileUrl.isNullOrEmpty()) {
                    startActivityWebViewFromUrl(ActiveUserDocRetrive?.promotionLetterTransferLetterFileUrl)
                }
            }

            binding.aClearenceFormBtnGreenView -> {
                if (ClearanceUri != null) {
                    startActivityFromUri(ClearanceUri!!)
                } else if (!ActiveUserDocRetrive?.clearanceFormFileUrl.isNullOrEmpty()) {
                    startActivityWebViewFromUrl(ActiveUserDocRetrive?.clearanceFormFileUrl)
                }
            }


//viewPageCallBack.onViewMoveNext()


            //binding.llActivedocNext -> navigate(R.id.action_login_to_signup)

        }
    }

    private fun startActivityWebViewFromUrl(url: String?) {
        val intent = Intent(context, ActiveDocWebViewActivity::class.java)

        intent.putExtra("file url", url)
        startActivity(intent)
    }

    fun openFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, mimeType)
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No application found to open this file type.", Toast.LENGTH_LONG).show()
        }
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

    private fun DocRetrivecall() {
        //showLoader()
        if (context?.isConnectedToNetwork()!!) {

            activeDocumentsViewModel.fetchActiveDocuments()
            //dismissLoader()


        } else {
            dismissLoader()
            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
        }
    }

    private fun RetriveStoredCheckCall(): Boolean {

        //DocRetrivecall()

        if (ActiveUserDocRetrive == null) {
            return false
        }

        return true
    }

    private fun isAllViewBtnEnabled(): Boolean {

        /*if (binding.appFormBtnGreenView.visibility == View.VISIBLE && binding.promotionLetterBtnGreenView.visibility == View.VISIBLE && binding.aIdCardBtnGreenView.visibility == View.VISIBLE && binding.aPassportPhotoBtnGreenView.visibility == View.VISIBLE ) {
            return true
        }*/

        if (binding.aIdCardBtnGreenView.visibility == View.VISIBLE && binding.aPassportPhotoBtnGreenView.visibility == View.VISIBLE) {
            return true
        }
        return false
    }

    private fun isValidDocs(): Boolean {

        /* if (applicationform_file?.equals("")!!) {
             Toast.makeText(
                 requireContext(),
                 "Please select Application form",
                 Toast.LENGTH_SHORT
             )
                 .show()
             return false
         }*/

        // Do similar validation for all document


        /*if (TextUtils.isEmpty(binding.tvActiveAppFormFilename.text) || binding.cvActiveAppForm.visibility == View.GONE ||binding.appFormBtnGreenView.visibility == View.VISIBLE) {
            Toast.makeText(context, "Please select Application Form File", Toast.LENGTH_LONG).show()
            return false
        } else if (TextUtils.isEmpty(binding.tvActivePromotionLetterFilename.text) || binding.cvActivePromotionLetter.visibility == View.GONE || binding.promotionLetterBtnGreenView.visibility == View.VISIBLE) {
            Toast.makeText(context, "Please select Promotion Letter File", Toast.LENGTH_LONG).show()
            return false
        } else if (TextUtils.isEmpty(binding.tvActiveIdCardFilename.text) || binding.cvActiveIdCard.visibility == View.GONE || binding.aIdCardBtnGreenView.visibility == View.VISIBLE) {
            Toast.makeText(context, "Please select Id Card", Toast.LENGTH_LONG).show()
            return false
        } else if (TextUtils.isEmpty(binding.tvActivePassportPhotoFilename.text) || binding.cvActivePassportPhoto.visibility == View.GONE || binding.aPassportPhotoBtnGreenView.visibility == View.VISIBLE) {
            Toast.makeText(context, "Please select PassPort Photo", Toast.LENGTH_LONG).show()
            return false
        } else if (TextUtils.isEmpty(binding.tvActiveClearenceFormFilename.text) || binding.cvActiveClearenceForm.visibility == View.GONE || binding.aClearenceFormBtnGreenView.visibility == View.VISIBLE) {
            Toast.makeText(context, "Please select Clearence Form File", Toast.LENGTH_LONG).show()
            return false
        }*/


        /*
        if (idCardUri == null
            && PhotoUri == null
            && ClearanceUri == null
            && applicationFormUri == null
            && letterFileUri ==null){

            if (!ActiveUserDocRetrive?.idCardFileUrl.isNullOrEmpty()
                && !ActiveUserDocRetrive?.passportPhotoFileUrl.isNullOrEmpty()
                && !ActiveUserDocRetrive?.applicationFormFileUrl.isNullOrEmpty()
                && !ActiveUserDocRetrive?.clearanceFormFileUrl.isNullOrEmpty()
                && !ActiveUserDocRetrive?.promotionLetterTransferLetterFileUrl.isNullOrEmpty()
                && !ActiveUserDocRetrive?.clearanceFormFileUrl.isNullOrEmpty()){
                return true
            }

            if (ActiveUserDocRetrive?.idCardFileUrl.isNullOrEmpty() || ActiveUserDocRetrive?.passportPhotoFileUrl.isNullOrEmpty()  ){
                Toast.makeText(context, "Please select mandatory fields", Toast.LENGTH_LONG).show()
                return false
            }else if (idCardUri == null){
                Toast.makeText(context, "Please select idcard", Toast.LENGTH_LONG).show()
                return false

            }else if (PhotoUri == null){
                Toast.makeText(context, "Please select passport photo", Toast.LENGTH_LONG).show()
                return false
            }
            //return true
        }else if (ClearanceUri != null){
            Toast.makeText(context, "Please select clearance", Toast.LENGTH_LONG).show()

            if (idCardUri == null){
                Toast.makeText(context, "Please select idcard", Toast.LENGTH_LONG).show()
                return false
            }
            if (PhotoUri == null){
                Toast.makeText(context, "Please select passport photo", Toast.LENGTH_LONG).show()
                return false
            }

        }else if (applicationFormUri != null){

            Toast.makeText(context, "Please select application form", Toast.LENGTH_LONG).show()


            if (idCardUri == null){
                Toast.makeText(context, "Please select idcard", Toast.LENGTH_LONG).show()
                return false
            }
            if (PhotoUri == null){
                Toast.makeText(context, "Please select passport photo", Toast.LENGTH_LONG).show()
                return false
            }

        }else if (letterFileUri != null){

            Toast.makeText(context, "Please select promotion letter", Toast.LENGTH_LONG).show()


            if (idCardUri == null){
                Toast.makeText(context, "Please select idcard", Toast.LENGTH_LONG).show()
                return false
            }
            if (PhotoUri == null){
                Toast.makeText(context, "Please select passport photo", Toast.LENGTH_LONG).show()
                return false
            }

        }
        */

        if (TextUtils.isEmpty(binding.tvActiveIdCardFilename.text) || binding.cvActiveIdCard.visibility == View.GONE) {
            Toast.makeText(context, "Please select Id Card", Toast.LENGTH_LONG).show()
            return false
        } /*else if (TextUtils.isEmpty(binding.tvActivePassportPhotoFilename.text) || binding.cvActivePassportPhoto.visibility == View.GONE) {
            Toast.makeText(context, "Please select PassPort Photo", Toast.LENGTH_LONG).show()
            return false
        }*/


        return true

    }

    private fun nextButtonCall() {

//reference
        //  https://www.simplifiedcoding.net/android-upload-file-to-server/

        /* val requestFile1 = RequestBody.create(MultipartBody.FORM, applicationform_file)
    val body1 = MultipartBody.Part.createFormData(
        "application_form_file",
        applicationform_filename,
        requestFile1
    )
    val requestFile2 = RequestBody.create(MultipartBody.FORM, applicationform_file)
    val body2 = MultipartBody.Part.createFormData(
        "promotion_letter_transfer_letter_file",
        applicationform_filename,
        requestFile2
    )
    val requestFile3 = RequestBody.create(MultipartBody.FORM, applicationform_file)
    val body3 = MultipartBody.Part.createFormData(
        "id_card_file",
        applicationform_filename,
        requestFile3
    )
    val requestFile4 = RequestBody.create(MultipartBody.FORM, applicationform_file)
    val body4 = MultipartBody.Part.createFormData(
        "passport_photo_file",
        applicationform_filename,
        requestFile3
    )
    val requestFile5 = RequestBody.create(MultipartBody.FORM, applicationform_file)
    val body5 = MultipartBody.Part.createFormData(
        "clearance_form_file",
        applicationform_filename,
        requestFile3
    )*/

        if (context?.isConnectedToNetwork()!!) {
            showLoader()

            docUploadCall2()


        } else {
            dismissLoader()
            Toast.makeText(context, "Please connect to internet", Toast.LENGTH_LONG).show()
        }

    }

    private fun docUploadCall() {
        Log.d(
            "typeofdoc",
            "nextButtonCall:" + applicationform_fileMIME + promotion_letter_fileMIME + id_card_fileMIME + passport_photo_fileMIME + clearance_form_fileMIME
        )

        val requestBody: RequestBody =
            MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart(
                "application_form_file",
                applicationform_filename,
                applicationform_file!!.asRequestBody(applicationform_fileMIME.toMediaTypeOrNull())
            ).addFormDataPart(
                "promotion_letter_transfer_letter_file",
                promotion_letter_filename,
                promotion_letter_file!!.asRequestBody(promotion_letter_fileMIME.toMediaTypeOrNull())
            ).addFormDataPart(
                "id_card_file",
                id_card_filename,
                id_card_file!!.asRequestBody(id_card_fileMIME.toMediaTypeOrNull())
            )

                .addFormDataPart(
                    "passport_photo_file",
                    passport_photo_filename,
                    passport_photo_file!!.asRequestBody(passport_photo_fileMIME.toMediaTypeOrNull())
                ).addFormDataPart(
                    "clearance_form_file",
                    clearance_form_filename,
                    clearance_form_file!!.asRequestBody(clearance_form_fileMIME.toMediaTypeOrNull())
                ).build()

        activeDocumentsViewModel.uploadDocuments(requestBody)

    }

    private fun docUploadCall2() {

        Log.d(
            "typeofdoc",
            "nextButtonCall:" + applicationform_fileMIME + promotion_letter_fileMIME + id_card_fileMIME + passport_photo_fileMIME + clearance_form_fileMIME
        )

        val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)

        // Mandatory parts
        multipartBuilder.addFormDataPart(
            "id_card_file",
            id_card_filename!!,
            id_card_file!!.asRequestBody(id_card_fileMIME.toMediaTypeOrNull())
        )


        // Optional parts
        if (passport_photo_file != null){
            multipartBuilder.addFormDataPart(
                "passport_photo_file",
                passport_photo_filename!!,
                passport_photo_file!!.asRequestBody(passport_photo_fileMIME.toMediaTypeOrNull())
            )
        }

        //appication form
        if (applicationform_file != null){
            multipartBuilder.addFormDataPart(
                "application_form_file",
                applicationform_filename!!,
                applicationform_file!!.asRequestBody(applicationform_fileMIME.toMediaTypeOrNull())
            )
        }/*else{
            multipartBuilder.addFormDataPart("application_form_file", "")
        }*/
        //promotion letter
        if(promotion_letter_file != null){
            multipartBuilder.addFormDataPart(
                "promotion_letter_transfer_letter_file",
                promotion_letter_filename!!,
                promotion_letter_file!!.asRequestBody(promotion_letter_fileMIME.toMediaTypeOrNull())
            )
        }/*else{
            multipartBuilder.addFormDataPart("promotion_letter_transfer_letter_file", "")
        }*/

        //clearance form
        if (clearance_form_file != null) {
            multipartBuilder.addFormDataPart(
                "clearance_form_file",
                clearance_form_filename!!,
                clearance_form_file!!.asRequestBody(clearance_form_fileMIME.toMediaTypeOrNull())
            )
        }
        /*else{
            multipartBuilder.addFormDataPart("clearance_form_file", "")

        }*/

    /*promotion_letter_file?.let {
                multipartBuilder.addFormDataPart(
                    "promotion_letter_transfer_letter_file",
                    promotion_letter_filename,
                    it.asRequestBody(promotion_letter_fileMIME.toMediaTypeOrNull())
                )
            }
            clearance_form_file?.let {
                multipartBuilder.addFormDataPart(
                    "clearance_form_file",
                    clearance_form_filename,
                    it.asRequestBody(clearance_form_fileMIME.toMediaTypeOrNull())
                )
            }*/

    // Build the RequestBody from the Builder
    val requestBody: RequestBody = multipartBuilder.build()

    // Now pass the RequestBody to the docUpload function
        activeDocumentsViewModel.uploadDocuments(requestBody)
    }


    private fun selectPdfJpegPng(REQUEST_CODE: Int) {
        val docIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        docIntent.type = "*/*"
        docIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(docIntent, REQUEST_CODE)
    }

    fun getMimeType(filePath: String): String {

        // Extract the extension from the getFileExtensionFromUrl
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


    fun getPathFromUri(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
        var path: String? = null
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(projection[0])
                path = it.getString(columnIndex)
            }
        }
        return path
    }


    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            APPLICATION_FORM_FILE -> if (resultCode == Activity.RESULT_OK) {
                binding.cvActiveAppForm.visibility = View.VISIBLE
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

                if (fileSizeInMB > 2000) { //2000 kb = 2 mb

                    binding.cvActiveAppForm.visibility = View.GONE
                    Toast.makeText(
                        requireContext(), "Can't Upload file larger than 2mb", Toast.LENGTH_LONG
                    ).show()
                } else {

                    binding.tvActiveAppFormFilename.text = file.name

                    applicationform_file = file

                    applicationform_fileMIME = getMimeType(applicationform_file.toString())

                    Log.d("doc upload", "$applicationform_file")
                    applicationform_filename = file.name
                    updateProgressBar(fileSizeInMB, formattedFileSize, APPLICATION_FORM_FILE)
                    // Only accept size with in 5 mb
                    Log.d("form_pdffile", "onActivityResult: " + file.name)


                }


                /*                val form_file = File(getPathFromUri(requireActivity(),form_uri)) // Convert URI to File object

                            // Create RequestBody instance from file
                                val form_fileRequestBody = form_file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

                                multipartBuilder.addFormDataPart("application_form_file",form_file.name,form_fileRequestBody)*/


                /*  uris.clear()

                  uris.add(form_uri)
                  val form_uriString: String = form_uri.toString()
                  var form_pdfName: String? = null
                  var form_pdfSize: String? = null
                  var form_sizeIndex: Int = 0
                  var form_size: Long = 0

                  if (form_uriString.startsWith("content://")) {
                      var form_myCursor: Cursor? = null
                      try {
                          form_myCursor = activity?.applicationContext!!.contentResolver.query(
                              form_uri, null, null, null, null
                          )
                          if (form_myCursor != null && form_myCursor.moveToFirst()) {

                              form_pdfName =
                                  form_myCursor.getString(form_myCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                              //get file size
                              form_sizeIndex = form_myCursor.getColumnIndex(OpenableColumns.SIZE)
                              form_size = form_myCursor.getLong(form_sizeIndex)
                              form_pdfSize = formatFileSize(form_size)

                              applicationFormUri = form_uri
                              *//*    applicationform_files = File(form_myCursor.getString(form_sizeIndex))*//*

                            binding.tvActiveAppFormFilename.text = form_pdfName
                            applicationform_filename = form_pdfName

                            updateProgressBar(form_size, form_pdfSize, APPLICATION_FORM_FILE)

                        }
                    } finally {
                        form_myCursor?.close()
                    }
                }*/
            }

            LETTER_FILE -> if (resultCode == Activity.RESULT_OK) {
                binding.cvActivePromotionLetter.visibility = View.VISIBLE
                val letter_uri: Uri = data?.data!!

                letterFileUri = letter_uri



                val parcelFileDescriptor = context?.contentResolver?.openFileDescriptor(
                    letter_uri!!, "r", null
                ) ?: return

                val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                val file =
                    File(context?.cacheDir, context?.contentResolver?.getFileName(letter_uri!!))
                val filetype =
                    File(context?.cacheDir, context?.contentResolver?.getType(letter_uri!!))
                Log.d("filetype", "onActivityResult: " + filetype)
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)


                // Retrieve and format file size
                val fileSize = getFileSize(letter_uri)
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

                    binding.cvActivePromotionLetter.visibility = View.GONE
                    Toast.makeText(
                        requireContext(), "Can't Upload file larger than 2mb", Toast.LENGTH_LONG
                    ).show()
                } else {

                    binding.tvActivePromotionLetterFilename.text = file.name
                    promotion_letter_file = file
                    promotion_letter_fileMIME = getMimeType(promotion_letter_file.toString())
                    promotion_letter_filename = file.name
                    updateProgressBar(fileSizeInMB, formattedFileSize, LETTER_FILE)
                    // Only accept size with in 5 mb
                    Log.d("form_pdffile", "onActivityResult: " + file.name)
                    Log.d("form_pdffile", "onActivityResult: " + file)

                }


            }

            IDCARD_FILE -> if (resultCode == Activity.RESULT_OK) {


                binding.cvActiveIdCard.visibility = View.VISIBLE
                val idcard_uri: Uri = data?.data!!

                idCardUri = idcard_uri

                val parcelFileDescriptor = context?.contentResolver?.openFileDescriptor(
                    idcard_uri!!, "r", null
                ) ?: return

                val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                val file =
                    File(context?.cacheDir, context?.contentResolver?.getFileName(idcard_uri!!))
                val filetype =
                    File(context?.cacheDir, context?.contentResolver?.getType(idcard_uri!!))
                Log.d("filetype", "onActivityResult: " + filetype)
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)

                // Retrieve and format file size
                val fileSize = getFileSize(idcard_uri)
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

                    binding.cvActiveIdCard.visibility = View.GONE
                    Toast.makeText(
                        requireContext(), "Can't Upload file larger than 2mb", Toast.LENGTH_LONG
                    ).show()
                } else {

                    binding.tvActiveIdCardFilename.text = file.name
                    id_card_file = file
                    id_card_fileMIME = getMimeType(id_card_file.toString())
                    id_card_filename = file.name
                    updateProgressBar(fileSizeInMB, formattedFileSize, IDCARD_FILE)

                    // Only accept size with in 5 mb
                    Log.d("form_pdffile", "onActivityResult: " + file.name)
                    Log.d("form_pdffile", "onActivityResult: " + file)

                }

            }

            PHOTO_FILE -> if (resultCode == Activity.RESULT_OK) {
                binding.cvActivePassportPhoto.visibility = View.VISIBLE

                val photo_uri: Uri = data?.data!!

                PhotoUri = photo_uri

                val parcelFileDescriptor = context?.contentResolver?.openFileDescriptor(
                    photo_uri!!, "r", null
                ) ?: return

                val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                val file =
                    File(context?.cacheDir, context?.contentResolver?.getFileName(photo_uri!!))
                val filetype =
                    File(context?.cacheDir, context?.contentResolver?.getType(photo_uri!!))
                Log.d("filetype", "onActivityResult: " + filetype)
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)


                // Retrieve and format file size
                val fileSize = getFileSize(photo_uri)
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

                    binding.cvActivePassportPhoto.visibility = View.GONE
                    Toast.makeText(
                        requireContext(), "Can't Upload file larger than 2mb", Toast.LENGTH_LONG
                    ).show()
                } else {

                    binding.tvActivePassportPhotoFilename.text = file.name
                    passport_photo_file = file
                    passport_photo_fileMIME = getMimeType(passport_photo_file.toString())
                    passport_photo_filename = file.name
                    updateProgressBar(fileSizeInMB, formattedFileSize, PHOTO_FILE)
                    // Only accept size with in 5 mb
                    Log.d("form_pdffile", "onActivityResult: " + file.name)
                    Log.d("form_pdffile", "onActivityResult: " + file)

                }
            }

            CLEARANCE_FORM_FILE -> if (resultCode == Activity.RESULT_OK) {

                binding.cvActiveClearenceForm.visibility = View.VISIBLE
                //pdfUri = data?.data!!
                val clear_uri: Uri = data?.data!!

                ClearanceUri = clear_uri


                val parcelFileDescriptor = context?.contentResolver?.openFileDescriptor(
                    clear_uri!!, "r", null
                ) ?: return

                val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                val file =
                    File(context?.cacheDir, context?.contentResolver?.getFileName(clear_uri!!))
                val filetype =
                    File(context?.cacheDir, context?.contentResolver?.getType(clear_uri!!))
                Log.d("filetype", "onActivityResult: " + filetype)
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)

                // Retrieve and format file size
                val fileSize = getFileSize(clear_uri)
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


                    binding.cvActiveClearenceForm.visibility = View.GONE

                    Toast.makeText(
                        requireContext(), "Can't Upload file larger than 2mb", Toast.LENGTH_LONG
                    ).show()
                } else {

                    binding.tvActiveClearenceFormFilename.text = file.name
                    clearance_form_file = file

                    clearance_form_fileMIME = getMimeType(clearance_form_file.toString())
                    clearance_form_filename = file.name
                    updateProgressBar(fileSizeInMB, formattedFileSize, CLEARANCE_FORM_FILE)
                    // Only accept size with in 5 mb
                    Log.d("form_pdffile", "onActivityResult: " + file.name)
                    Log.d("form_pdffile", "onActivityResult: " + file)

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
            APPLICATION_FORM_FILE -> {
                var form_status = 0
                val form_handler: Handler = Handler()

                binding.cvActiveAppForm.visibility = View.VISIBLE
                binding.imgActiveAppFormClose.visibility = View.VISIBLE

                binding.llActiveAppFormUploadprogress.visibility = View.VISIBLE
                binding.llActiveAppFormPercentage.visibility = View.VISIBLE
                binding.pbActiveAppForm.visibility = View.VISIBLE
                binding.llActiveFormSize.visibility = View.GONE

                Thread(Runnable {
                    while (form_status < 100) {
                        form_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        form_handler.post(Runnable {


                            binding.pbActiveAppForm?.setProgress(form_status)

                            binding.tvPercentActiveDoc.text = form_status.toString() + " %"
                            if (form_status == 100) {

                                binding.appFormBtnGreenView.visibility = View.VISIBLE
                                binding.llActiveAppFormUploadprogress.visibility = View.GONE
                                binding.llActiveFormSize.visibility = View.VISIBLE
                                binding.txtFilesize.text = pdfSize
                            }
                        })
                    }
                }).start()


            }

            LETTER_FILE -> {
                var letter_status = 0
                val letter_handler: Handler = Handler()

                binding.cvActivePromotionLetter.visibility = View.VISIBLE
                binding.imgActivePromotionLetterClose.visibility = View.VISIBLE

                binding.llActivePromotionLetterUploadprogress.visibility = View.VISIBLE
                binding.llActivePromotionLetterPercentage.visibility = View.VISIBLE
                binding.pbActivePromotionLetter.visibility = View.VISIBLE
                binding.llActivePromotionLetterSize.visibility = View.GONE

                Thread(Runnable {
                    while (letter_status < 100) {
                        letter_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        letter_handler.post(Runnable {
                            binding.pbActivePromotionLetter?.setProgress(letter_status)

                            binding.tvActivePromotionLetterDoc.text = letter_status.toString() + " %"
                            if (letter_status == 100) {
                                binding.promotionLetterBtnGreenView.visibility = View.VISIBLE
                                binding.llActivePromotionLetterUploadprogress.visibility = View.GONE
                                binding.llActivePromotionLetterSize.visibility = View.VISIBLE
                                binding.txtLetterFilesize.text = pdfSize
                            }
                        })
                    }
                }).start()

            }

            IDCARD_FILE -> {
                var idcard_status = 0
                val idcard_handler: Handler = Handler()

                binding.cvActiveIdCard.visibility = View.VISIBLE
                binding.imgActiveIdCardClose.visibility = View.VISIBLE

                binding.llActiveIdCardUploadprogress.visibility = View.VISIBLE
                binding.llActiveIdCardPercentage.visibility = View.VISIBLE
                binding.pbActiveIdCard.visibility = View.VISIBLE
                binding.llActiveIdCardSize.visibility = View.GONE

                Thread(Runnable {
                    while (idcard_status < 100) {
                        idcard_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        idcard_handler.post(Runnable {
                            binding.pbActiveIdCard?.setProgress(idcard_status)

                            binding.tvIdCardActiveDoc.text = idcard_status.toString() + " %"
                            if (idcard_status == 100) {
                                binding.aIdCardBtnGreenView.visibility = View.VISIBLE
                                binding.llActiveIdCardUploadprogress.visibility = View.GONE
                                binding.llActiveIdCardSize.visibility = View.VISIBLE
                                binding.txtActiveIdCardFilesize.text = pdfSize
                            }
                        })
                    }
                }).start()

            }

            PHOTO_FILE -> {
                var photo_status = 0
                val photo_handler: Handler = Handler()

                binding.cvActivePassportPhoto.visibility = View.VISIBLE
                binding.imgActivePassportPhotoClose.visibility = View.VISIBLE

                binding.llActivePassportPhotoUploadprogress.visibility = View.VISIBLE
                binding.llActivePassportPhotoPercentage.visibility = View.VISIBLE
                binding.pbActivePassportPhoto.visibility = View.VISIBLE
                binding.llActivePassportPhotoSize.visibility = View.GONE
                Thread(Runnable {
                    while (photo_status < 100) {
                        photo_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        photo_handler.post(Runnable {
                            binding.pbActivePassportPhoto?.setProgress(photo_status)

                            binding.tvPassportPhotoActiveDoc.text = photo_status.toString() + " %"
                            if (photo_status == 100) {
                                binding.aPassportPhotoBtnGreenView.visibility = View.VISIBLE
                                binding.llActivePassportPhotoUploadprogress.visibility = View.GONE
                                binding.llActivePassportPhotoSize.visibility = View.VISIBLE
                                binding.txtActivePassportPhotoFilesize.text = pdfSize
                            }
                        })
                    }
                }).start()
            }

            CLEARANCE_FORM_FILE -> {

                var clear_status = 0
                val clear_handler: Handler = Handler()

                binding.cvActiveClearenceForm.visibility = View.VISIBLE
                binding.imgActiveClearenceFormClose.visibility = View.VISIBLE

                binding.llActiveClearenceFormUploadprogress.visibility = View.VISIBLE
                binding.llActiveClearenceFormPercentage.visibility = View.VISIBLE
                binding.pbActiveClearenceForm.visibility = View.VISIBLE
                binding.llActiveClearenceFormSize.visibility = View.GONE

                Thread(Runnable {
                    while (clear_status < 100) {
                        clear_status += 10
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        clear_handler.post(Runnable {
                            binding.pbActiveClearenceForm?.setProgress(clear_status)

                            binding.tvClearenceFormActiveDoc.text = clear_status.toString() + " %"
                            if (clear_status == 100) {
                                binding.aClearenceFormBtnGreenView.visibility = View.VISIBLE
                                binding.llActiveClearenceFormUploadprogress.visibility = View.GONE
                                binding.llActiveClearenceFormSize.visibility = View.VISIBLE
                                binding.txtActiveClearenceFormFilesize.text = pdfSize
                            }
                        })
                    }
                }).start()
            }

        }


    }

    fun Context.isConnectedToNetwork(): Boolean {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting() ?: false
    }

    fun onDocUploadSuccess(response: ResponseActiveDocUpload) {
        dismissLoader()
        Toast.makeText(context, response.detail?.message, Toast.LENGTH_SHORT).show()
        if (prefs.onboardingStage == OnboardingStage.ACTIVE_DOCUMENTS)
            prefs.onboardingStage = OnboardingStage.ACTIVE_BANK_INFO
        activeServiceViewModel.moveToNextTab()
        activeServiceViewModel.refreshTabsState()
    }



    private fun populateViews() {
        ActiveUserDocRetrive?.let { activeUserDoc ->
            if (!activeUserDoc.idCardFileUrl.isNullOrEmpty()) {
                if (prefs.onboardingStage == OnboardingStage.ACTIVE_DOCUMENTS) {
                    prefs.onboardingStage = OnboardingStage.ACTIVE_BANK_INFO
                    activeServiceViewModel.refreshTabsState()
                }

                // Mandatory documents
                setDocumentView(activeUserDoc.idCardFileUrl, binding.aIdCardBtnGreenView, binding.cvActiveIdCard, binding.tvActiveIdCardFilename, binding.imgActiveIdCardClose, binding.llActiveIdCardUploadprogress, binding.llActiveIdCardPercentage, binding.pbActiveIdCard)
                // Optional documents
                setDocumentViewIfPresent(activeUserDoc.passportPhotoFileUrl, binding.aPassportPhotoBtnGreenView, binding.cvActivePassportPhoto, binding.tvActivePassportPhotoFilename, binding.imgActivePassportPhotoClose, binding.llActivePassportPhotoUploadprogress, binding.llActivePassportPhotoPercentage, binding.pbActivePassportPhoto)
                setDocumentViewIfPresent(activeUserDoc.applicationFormFileUrl, binding.appFormBtnGreenView, binding.cvActiveAppForm, binding.tvActiveAppFormFilename, binding.imgActiveAppFormClose, binding.llActiveAppFormUploadprogress, binding.llActiveAppFormPercentage, binding.pbActiveAppForm)
                setDocumentViewIfPresent(activeUserDoc.promotionLetterTransferLetterFileUrl, binding.promotionLetterBtnGreenView, binding.cvActivePromotionLetter, binding.tvActivePromotionLetterFilename, binding.imgActivePromotionLetterClose, binding.llActivePromotionLetterUploadprogress, binding.llActivePromotionLetterPercentage, binding.pbActivePromotionLetter)
                setDocumentViewIfPresent(activeUserDoc.clearanceFormFileUrl, binding.aClearenceFormBtnGreenView, binding.cvActiveClearenceForm, binding.tvActiveClearenceFormFilename, binding.imgActiveClearenceFormClose, binding.llActiveClearenceFormUploadprogress, binding.llActiveClearenceFormPercentage, binding.pbActiveClearenceForm)
            }else{
                //tabAccessControl.enableDisableTabs(true, true, false)
            }
        }
    }

    private fun onRetrivedDocSetFields() {
        /*if(responseActiveDocRetrive?.detail?.filePresentCheck.equals("true")){
            prefs.isActiveDocSubmit = true
        }*/
        //if (!ActiveUserDocRetrive?.applicationFormFileUrl.isNullOrEmpty()){
        if (!ActiveUserDocRetrive?.idCardFileUrl.isNullOrEmpty()) {

            Log.d("activeretrive", "${ActiveUserDocRetrive?.idCardFileUrl}")
            //prefs.isActiveDocSubmit = true



            //mandatory
            var idcard_url = ActiveUserDocRetrive!!.idCardFileUrl
            var passPhoto_url = ActiveUserDocRetrive!!.passportPhotoFileUrl
            //optional
            var appform_url = ActiveUserDocRetrive!!.applicationFormFileUrl
            var promo_url = ActiveUserDocRetrive!!.promotionLetterTransferLetterFileUrl
            var clearrance_url = ActiveUserDocRetrive!!.clearanceFormFileUrl

            //visible mandatory view buttons
            binding.aIdCardBtnGreenView.visibility = View.VISIBLE
            binding.aPassportPhotoBtnGreenView.visibility = View.VISIBLE


            //id card
            binding.cvActiveIdCard.visibility = View.VISIBLE
            binding.tvActiveIdCardFilename.text = idcard_url!!.substringAfterLast('/')
            binding.imgActiveIdCardClose.visibility = View.VISIBLE
            binding.llActiveIdCardUploadprogress.visibility = View.GONE
            binding.llActiveIdCardPercentage.visibility = View.GONE
            binding.pbActiveIdCard.visibility = View.GONE

            //passport
            binding.cvActivePassportPhoto.visibility = View.VISIBLE
            binding.tvActivePassportPhotoFilename.text = passPhoto_url!!.substringAfterLast('/')
            binding.imgActivePassportPhotoClose.visibility = View.VISIBLE
            binding.llActivePassportPhotoUploadprogress.visibility = View.GONE
            binding.llActivePassportPhotoPercentage.visibility = View.GONE
            binding.pbActivePassportPhoto.visibility = View.GONE


            //optional view btn visble
            //appform
            if (!appform_url.isNullOrEmpty()){
                binding.appFormBtnGreenView.visibility = View.VISIBLE

                binding.cvActiveAppForm.visibility = View.VISIBLE
                binding.tvActiveAppFormFilename.text = appform_url!!.substringAfterLast('/')
                Log.d("texturl", "${binding.tvActiveClearenceFormFilename.text}")
                binding.imgActiveAppFormClose.visibility = View.VISIBLE
                binding.llActiveAppFormUploadprogress.visibility = View.GONE
                binding.llActiveAppFormPercentage.visibility = View.GONE
                binding.pbActiveAppForm.visibility = View.GONE
            }
            //promotion letter
            if (!promo_url.isNullOrEmpty()){
                binding.promotionLetterBtnGreenView.visibility = View.VISIBLE

                binding.cvActivePromotionLetter.visibility = View.VISIBLE
                binding.tvActivePromotionLetterFilename.text = promo_url!!.substringAfterLast('/')
                binding.imgActivePromotionLetterClose.visibility = View.VISIBLE
                binding.llActivePromotionLetterUploadprogress.visibility = View.GONE
                binding.llActivePromotionLetterPercentage.visibility = View.GONE
                binding.pbActivePromotionLetter.visibility = View.GONE
            }
            //clearance
            if (!clearrance_url.isNullOrEmpty()){
                binding.aClearenceFormBtnGreenView.visibility = View.VISIBLE

                binding.cvActiveClearenceForm.visibility = View.VISIBLE
                binding.tvActiveClearenceFormFilename.text = clearrance_url!!.substringAfterLast('/')
                binding.imgActiveClearenceFormClose.visibility = View.VISIBLE
                binding.llActiveClearenceFormUploadprogress.visibility = View.GONE
                binding.llActiveClearenceFormPercentage.visibility = View.GONE
                binding.pbActiveClearenceForm.visibility = View.GONE
            }
            //enableDisableTabs(tab_tablayout_activeservice, true, true, true)

        }


    }
}


