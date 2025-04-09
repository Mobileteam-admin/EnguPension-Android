package com.enugu.pension.ui.fragment.service.retiree

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.enugu.pension.constant.AppConstants
import com.enugu.pension.R
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.databinding.FragmentRetireeDocumentsBinding
import com.enugu.pension.model.dto.DocumentUploadItem
import com.enugu.pension.model.response.ResponseRetireeDocUpload
import com.enugu.pension.model.response.RetireeFileUrlResponse
import com.enugu.pension.network.ApiClient
import com.enugu.pension.ui.activity.ActiveDocWebViewActivity
import com.enugu.pension.ui.adapter.DocumentUploadAdapter
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.util.FileUtils
import com.enugu.pension.util.NetworkUtils
import com.enugu.pension.util.OnboardingStage
import com.enugu.pension.util.SharedPref
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.RetireeDocumentsViewModel
import com.enugu.pension.viewmodel.RetireeDocumentsViewModel.DocItemIndex.ID_CARD
import com.enugu.pension.viewmodel.RetireeDocumentsViewModel.DocItemIndex.PASSPORT_PHOTO
import com.enugu.pension.viewmodel.RetireeDocumentsViewModel.DocItemIndex.APPLICATION_FORM
import com.enugu.pension.viewmodel.RetireeDocumentsViewModel.DocItemIndex.APPOINTMENT_FOR_PAYMENT
import com.enugu.pension.viewmodel.RetireeDocumentsViewModel.DocItemIndex.AUTHORISATION_FOR_PAYMENT
import com.enugu.pension.viewmodel.RetireeDocumentsViewModel.DocItemIndex.CLEARANCE_FORM
import com.enugu.pension.viewmodel.RetireeDocumentsViewModel.DocItemIndex.NOTIFICATION_OF_PROMOTION
import com.enugu.pension.viewmodel.RetireeDocumentsViewModel.DocItemIndex.PENSION_CERTIFICATE
import com.enugu.pension.viewmodel.RetireeDocumentsViewModel.DocItemIndex.RETIREMENT_NOTICE
import com.enugu.pension.viewmodel.RetireeServiceViewModel
import com.enugu.pension.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class RetireeDocumentsFragment : BaseFragment(), View.OnClickListener {
    companion object {
        const val TAB_POSITION = 1
        private const val MAX_FILE_SIZE_IN_MB = 2
        private const val NUM_DOCS = 9
        private val MIME_TYPES = arrayOf("image/jpeg", "image/png", "application/pdf")
    }
    private object MultipartFileName {
        const val ID_CARD_FILE = "id_card_file"
        const val PASSPORT_PHOTO_FILE = "passport_photo_file"
        const val APPLICATION_FORM_FILE = "application_form_file"
        const val APPOINTMENT_FOR_PAYMENT_FILE = "monthly_arrears_payment_appointment_file"
        const val AUTHORISATION_FOR_PAYMENT_FILE = "payment_authorization_retirement_or_death_file"
        const val CLEARANCE_FORM_FILE = "clearance_form_file"
        const val NOTIFICATION_OF_PROMOTION_FILE = "notification_promotion_file"
        const val PENSION_CERTIFICATE_FILE = "pension_certificate_file"
        const val RETIREMENT_NOTICE_FILE = "retirement_notice_file"
    }

    private object CacheFileName {
        const val ID_CARD_FILE = "id_card_file"
        const val PASSPORT_PHOTO_FILE = "passport_photo_file"
        const val APPLICATION_FORM_FILE = "application_form_file"
        const val APPOINTMENT_FOR_PAYMENT_FILE = "appointment_for_payment_file"
        const val AUTHORISATION_FOR_PAYMENT_FILE = "authorisation_for_payment_file"
        const val CLEARANCE_FORM_FILE = "clearance_form_file"
        const val NOTIFICATION_OF_PROMOTION_FILE = "notification_promotion_file"
        const val PENSION_CERTIFICATE_FILE = "pension_life_certificate_file"
        const val RETIREMENT_NOTICE_FILE = "retirement_notice_file"
    }
    
    private lateinit var binding:FragmentRetireeDocumentsBinding
    private lateinit var viewModel: RetireeDocumentsViewModel
    private val retireeServiceViewModel by activityViewModels<RetireeServiceViewModel>()
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    private lateinit var rvAdapter: DocumentUploadAdapter
    
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
        initDocItemViewList()
        binding.llNext.setOnClickListener(this)
    }

    private fun initDocItemViewList() {
        val docList = MutableList<DocumentUploadItem?>(NUM_DOCS) { null }
        docList[ID_CARD] = DocumentUploadItem(
            multipartFileName = MultipartFileName.ID_CARD_FILE,
            cacheFileName = CacheFileName.ID_CARD_FILE,
            index = ID_CARD,
            isMandatory = true,
            title = getString(R.string.id_card),
        )
        docList[PASSPORT_PHOTO] = DocumentUploadItem(
            multipartFileName = MultipartFileName.PASSPORT_PHOTO_FILE,
            cacheFileName = CacheFileName.PASSPORT_PHOTO_FILE,
            index = PASSPORT_PHOTO,
            isMandatory = false,
            title = getString(R.string.passport_photo),
        )
        docList[APPLICATION_FORM] = DocumentUploadItem(
            multipartFileName = MultipartFileName.APPLICATION_FORM_FILE,
            cacheFileName = CacheFileName.APPLICATION_FORM_FILE,
            index = APPLICATION_FORM,
            isMandatory = false,
            title = getString(R.string.application_form),
        )
        docList[APPOINTMENT_FOR_PAYMENT] = DocumentUploadItem(
            multipartFileName = MultipartFileName.APPOINTMENT_FOR_PAYMENT_FILE,
            cacheFileName = CacheFileName.APPOINTMENT_FOR_PAYMENT_FILE,
            index = APPOINTMENT_FOR_PAYMENT,
            isMandatory = false,
            title = getString(R.string.appointment_for_payment_of_monthly_arrears),
        )
        docList[AUTHORISATION_FOR_PAYMENT] = DocumentUploadItem(
            multipartFileName = MultipartFileName.AUTHORISATION_FOR_PAYMENT_FILE,
            cacheFileName = CacheFileName.AUTHORISATION_FOR_PAYMENT_FILE,
            index = AUTHORISATION_FOR_PAYMENT,
            isMandatory = false,
            title = getString(R.string.authorisation_for_payment),
        )
        docList[CLEARANCE_FORM] = DocumentUploadItem(
            multipartFileName = MultipartFileName.CLEARANCE_FORM_FILE,
            cacheFileName = CacheFileName.CLEARANCE_FORM_FILE,
            index = CLEARANCE_FORM,
            isMandatory = false,
            title = getString(R.string.clearance_form),
        )
        docList[NOTIFICATION_OF_PROMOTION] = DocumentUploadItem(
            multipartFileName = MultipartFileName.NOTIFICATION_OF_PROMOTION_FILE,
            cacheFileName = CacheFileName.NOTIFICATION_OF_PROMOTION_FILE,
            index = NOTIFICATION_OF_PROMOTION,
            isMandatory = false,
            title = getString(R.string.notification_of_promotion),
        )
        docList[PENSION_CERTIFICATE] = DocumentUploadItem(
            multipartFileName = MultipartFileName.PENSION_CERTIFICATE_FILE,
            cacheFileName = CacheFileName.PENSION_CERTIFICATE_FILE,
            index = PENSION_CERTIFICATE,
            isMandatory = false,
            title = getString(R.string.pension_life_certificate),
        )
        docList[RETIREMENT_NOTICE] = DocumentUploadItem(
            multipartFileName = MultipartFileName.RETIREMENT_NOTICE_FILE,
            cacheFileName = CacheFileName.RETIREMENT_NOTICE_FILE,
            index = RETIREMENT_NOTICE,
            isMandatory = false,
            title = getString(R.string.retirement_notice),
        )

        rvAdapter = DocumentUploadAdapter(
            docList, onViewDoc = ::onViewDoc,
            onRemoveDoc = ::onRemoveDoc,
            onUploadClick = ::onUploadClick,
        )
        binding.rvDocs.adapter = rvAdapter
        binding.rvDocs.itemAnimator = null
    }

    private fun initViewModels() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        viewModel = ViewModelProviders.of(
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
            if (it == TAB_POSITION) {
                showLoader()
                fetchRetireeDocuments()
            }
        }
        viewModel.documentsFetchResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                populateViews(response.detail.fileUrlResponse)
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.fetchDocuments()
                        }
                    }
                } else {
                    dismissLoader()
                    showFetchErrorDialog(
                        ::fetchRetireeDocuments,
                        response.detail?.message ?: getString(R.string.common_error_msg_2)
                    )
                }
            }
        }
        viewModel.documentsUploadResult.observe(viewLifecycleOwner) { pair ->
            if (pair != null) {
                dismissLoader()
                val request = pair.first
                val response = pair.second
                val navigateToNext = pair.third
                if (response.detail?.status == AppConstants.SUCCESS) {
                    onDocUploadSuccess(response, navigateToNext)
                } else {
                    if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (tokenRefreshViewModel2.fetchRefreshToken()) {
                                viewModel.uploadDocuments(request, navigateToNext)
                            }
                        }
                    } else {
                        Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG).show()
                    }
                }
                viewModel.resetDocumentsUploadResult()
            }
        }
    }
    
    private fun uploadDocs(navigateToNext: Boolean) {
        val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
        rvAdapter.items.forEach {
            val body =
                if (it!!.hasDocumentAdded())
                    it.file!!.asRequestBody(it.mime!!.toMediaTypeOrNull())
                else
                    ByteArray(0).toRequestBody(null, 0, 0)

            multipartBuilder.addFormDataPart(it.multipartFileName, it.fileName, body)
        }
        val requestBody: RequestBody = multipartBuilder.build()
        viewModel.uploadDocuments(requestBody, navigateToNext)
    }

    private fun onRemoveDoc(item: DocumentUploadItem) {
        item.uploadedUrl = null
        item.pickedUri = null
        item.fileName = ""
        item.mime = null
        rvAdapter.notifyItemChanged(item.index)
    }
    
    private fun onViewDoc(item: DocumentUploadItem) {
        if (item.pickedUri != null) {
            startActivityFromUri(item.pickedUri!!)
        } else {
            item.uploadedUrl?.let { startActivityWebViewFromUrl(it) }
        }
    }

    private fun onUploadClick(item: DocumentUploadItem) {
        val docIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        docIntent.type = "*/*"
        docIntent.putExtra(Intent.EXTRA_MIME_TYPES, MIME_TYPES)
        startActivityForResult(docIntent, item.index)
    }

    override fun onClick(view: View?) {
        if (view?.id == binding.llNext.id) {
            next()
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
            showToast(R.string.no_app_found_to_open)
        }
    }
    
    private fun fetchRetireeDocuments() {
        if (NetworkUtils.isConnectedToNetwork(requireContext())) {
            viewModel.fetchDocuments()
        } else {
            dismissLoader()
            showToast(R.string.no_internet_error)
        }
    }
    private fun next() {
        if (hasMandatoryDocumentsAdded(true)) {
            if (NetworkUtils.isConnectedToNetwork(requireContext())) {
                showLoader()
                uploadDocs(true)
            } else {
                dismissLoader()
                showToast(R.string.no_internet_error)
            }
        }
    }
    
    private fun hasMandatoryDocumentsAdded(showErrorToast: Boolean): Boolean {
        for (item in rvAdapter.items) {
            if (item!!.isMandatory && !item.hasDocumentAdded()) {
                if (showErrorToast) showToast("Please upload your ${item.title.lowercase()}")
                return false
            }
        }
        return true
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data?.data != null) {
            if (requestCode in RetireeDocumentsViewModel.DocItemIndex.getIndexList()) {
                val uri: Uri = data.data!!
                if (NetworkUtils.isConnectedToNetwork(requireContext())) {
                    onFileSelected(requestCode, uri)
                } else {
                    dismissLoader()
                    showToast(R.string.no_internet_error)
                }
            }
        }
    }
    
    private fun onFileSelected(index: Int, uri: Uri): Boolean {
        val parcelFileDescriptor =
            context?.contentResolver?.openFileDescriptor(uri, "r", null) ?: return false
        val fileName = FileUtils.getFileName(context?.contentResolver, uri)
        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(context?.cacheDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        val fileSizeInMB = file.length().toDouble() / (1024 * 1024)
        if (fileSizeInMB > MAX_FILE_SIZE_IN_MB) {
            showToast(getString(R.string.fail_size_exceed_error,
                MAX_FILE_SIZE_IN_MB
            ))
            return false
        } else {
            rvAdapter.items[index]!!.file = file
            rvAdapter.items[index]!!.fileName = file.name
            rvAdapter.items[index]!!.mime = FileUtils.getDocumentMimeType(file.path)
            rvAdapter.items[index]!!.pickedUri = uri
            lifecycleScope.launch {
                var progress = 0
                rvAdapter.items[index]!!.isUploading = true
                while (progress < 100) {
                    rvAdapter.items[index]!!.progress = progress
                    progress += 10
                    delay(100)
                    rvAdapter.notifyItemChanged(index)
                }
                rvAdapter.items[index]!!.isUploading = false
                rvAdapter.notifyItemChanged(index)
                showToast(R.string.doc_uploaded)
            }
            showLoader()
            uploadDocs(false)
            return true
        }
    }

    private fun onDocUploadSuccess(response: ResponseRetireeDocUpload, navigateToNext: Boolean) {
        dismissLoader()
//        Toast.makeText(context, response.detail?.message, Toast.LENGTH_SHORT).show()
        if (SharedPref.onboardingStage == OnboardingStage.RETIREE_DOCUMENTS)
            SharedPref.onboardingStage = OnboardingStage.RETIREE_BANK_INFO
        retireeServiceViewModel.refreshTabsState()
        if (navigateToNext) retireeServiceViewModel.moveToNextTab()
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun populateViews(fileUrlResponse: RetireeFileUrlResponse?) {
        if (!fileUrlResponse?.idCardFileUrl.isNullOrEmpty() && SharedPref.onboardingStage == OnboardingStage.RETIREE_DOCUMENTS) {
            SharedPref.onboardingStage = OnboardingStage.RETIREE_BANK_INFO
            retireeServiceViewModel.refreshTabsState()
        }
        rvAdapter.items[ID_CARD]!!.uploadedUrl = fileUrlResponse?.idCardFileUrl
        rvAdapter.items[PASSPORT_PHOTO]!!.uploadedUrl = fileUrlResponse?.passportPhotoFileUrl
        rvAdapter.items[APPLICATION_FORM]!!.uploadedUrl = fileUrlResponse?.applicationFormFileUrl
        rvAdapter.items[APPOINTMENT_FOR_PAYMENT]!!.uploadedUrl = fileUrlResponse?.applicationFormFileUrl
        rvAdapter.items[AUTHORISATION_FOR_PAYMENT]!!.uploadedUrl = fileUrlResponse?.paymentAuthorizationRetirementOrDeathFileUrl
        rvAdapter.items[CLEARANCE_FORM]!!.uploadedUrl = fileUrlResponse?.clearanceFormFileUrl
        rvAdapter.items[NOTIFICATION_OF_PROMOTION]!!.uploadedUrl = fileUrlResponse?.promotionNotificationFileUrl
        rvAdapter.items[PENSION_CERTIFICATE]!!.uploadedUrl = fileUrlResponse?.pensionLifeCertificateFileUrl
        rvAdapter.items[RETIREMENT_NOTICE]!!.uploadedUrl = fileUrlResponse?.retirementNoticeFileUrl
        rvAdapter.items.forEachIndexed { index, item ->
            item!!.fileName = item.uploadedUrl?.substringAfterLast('/') ?: ""
            if (!item.uploadedUrl.isNullOrEmpty()) {
                item.mime = FileUtils.getDocumentMimeType(item.uploadedUrl!!)
                val cacheFile = File(requireContext().cacheDir, item.cacheFileName)
                item.file = cacheFile
                viewModel.downloadDocCacheFile(item.uploadedUrl!!, cacheFile)
            }
        }
        rvAdapter.notifyDataSetChanged()
    }
}