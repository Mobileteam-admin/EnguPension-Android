package com.enugu.pension.ui.fragment.service.active

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
import com.enugu.pension.common.constant.AppConstants
import com.enugu.pension.R
import com.enugu.pension.data.repository.NetworkRepo
import com.enugu.pension.databinding.FragmentActiveDocumentsBinding
import com.enugu.pension.ui.model.DocumentUploadItem
import com.enugu.pension.data.remote.dto.response.FileUrlResponse
import com.enugu.pension.data.remote.dto.response.ResponseActiveDocUpload
import com.enugu.pension.data.remote.api.ApiClient
import com.enugu.pension.ui.activity.ActiveDocWebViewActivity
import com.enugu.pension.ui.adapter.DocumentUploadAdapter
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.common.util.NetworkUtils
import com.enugu.pension.common.util.OnboardingStage
import com.enugu.pension.data.local.SharedPref
import com.enugu.pension.common.util.FileUtils
import com.enugu.pension.viewmodel.ActiveDocumentsViewModel
import com.enugu.pension.viewmodel.ActiveDocumentsViewModel.DocItemIndex.ID_CARD
import com.enugu.pension.viewmodel.ActiveDocumentsViewModel.DocItemIndex.PHOTO
import com.enugu.pension.viewmodel.ActiveDocumentsViewModel.DocItemIndex.APPLICATION_FORM
import com.enugu.pension.viewmodel.ActiveDocumentsViewModel.DocItemIndex.LETTER
import com.enugu.pension.viewmodel.ActiveDocumentsViewModel.DocItemIndex.CLEARANCE
import com.enugu.pension.viewmodel.ActiveServiceViewModel
import com.enugu.pension.viewmodel.EnguViewModelFactory
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


class ActiveDocumentsFragment : BaseFragment(), View.OnClickListener {
    companion object {
        const val TAB_POSITION = 1
        private const val MAX_FILE_SIZE_IN_MB = 2
        private const val NUM_DOCS = 5
        private val MIME_TYPES = arrayOf("image/jpeg", "image/png", "application/pdf")
    }

    private object MultipartFileName {
        const val ID_CARD_FILE = "id_card_file"
        const val PASSPORT_PHOTO_FILE = "passport_photo_file"
        const val APPLICATION_FORM_FILE = "application_form_file"
        const val PROMOTION_LETTER_TRANSFER_LETTER_FILE = "promotion_letter_transfer_letter_file"
        const val CLEARANCE_FORM_FILE = "clearance_form_file"
    }

    private object CacheFileName {
        const val ID_CARD_FILE = "active_id_card_file"
        const val PASSPORT_PHOTO_FILE = "active_passport_photo_file"
        const val APPLICATION_FORM_FILE = "active_application_form_file"
        const val PROMOTION_LETTER_TRANSFER_LETTER_FILE =
            "active_promotion_letter_transfer_letter_file"
        const val CLEARANCE_FORM_FILE = "active_clearance_form_file"
    }

    private lateinit var binding: FragmentActiveDocumentsBinding
    private val activeServiceViewModel by activityViewModels<ActiveServiceViewModel>()
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    private lateinit var viewModel: ActiveDocumentsViewModel
    private lateinit var rvAdapter: DocumentUploadAdapter

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
        docList[PHOTO] = DocumentUploadItem(
            multipartFileName = MultipartFileName.PASSPORT_PHOTO_FILE,
            cacheFileName = CacheFileName.PASSPORT_PHOTO_FILE,
            index = PHOTO,
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
        docList[LETTER] = DocumentUploadItem(
            multipartFileName = MultipartFileName.PROMOTION_LETTER_TRANSFER_LETTER_FILE,
            cacheFileName = CacheFileName.PROMOTION_LETTER_TRANSFER_LETTER_FILE,
            index = LETTER,
            isMandatory = false,
            title = getString(R.string.promotion_letter_transfer_letter),
        )
        docList[CLEARANCE] = DocumentUploadItem(
            multipartFileName = MultipartFileName.CLEARANCE_FORM_FILE,
            cacheFileName = CacheFileName.CLEARANCE_FORM_FILE,
            index = CLEARANCE,
            isMandatory = false,
            title = getString(R.string.clearance_form),
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
        ).get(ActiveDocumentsViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(),
            EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }

    private fun observeLiveData() {
        activeServiceViewModel.currentTabPos.observe(viewLifecycleOwner) {
            if (it == TAB_POSITION) {
                showLoader()
                fetchActiveDocuments()
            }
        }
        viewModel.documentsApiResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                populateViews(response.detail.fileUrlResponse)
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.fetchActiveDocuments()
                        }
                    }
                } else {
                    dismissLoader()
                    showFetchErrorDialog(
                        ::fetchActiveDocuments,
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

    private fun startActivityWebViewFromUrl(url: String) {
        val intent = Intent(context, ActiveDocWebViewActivity::class.java)
        intent.putExtra("file url", url)
        startActivity(intent)
    }

    private fun startActivityFromUri(uri: Uri) {
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

    private fun fetchActiveDocuments() {
        if (NetworkUtils.isConnectedToNetwork(requireContext())) {
            viewModel.fetchActiveDocuments()
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
            if (requestCode in ActiveDocumentsViewModel.DocItemIndex.getIndexList()) {
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
            showToast(getString(R.string.fail_size_exceed_error, MAX_FILE_SIZE_IN_MB))
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

    private fun onDocUploadSuccess(response: ResponseActiveDocUpload, navigateToNext: Boolean) {
        dismissLoader()
//        Toast.makeText(context, response.detail?.message, Toast.LENGTH_SHORT).show()
        if (SharedPref.onboardingStage == OnboardingStage.ACTIVE_DOCUMENTS)
            SharedPref.onboardingStage = OnboardingStage.ACTIVE_BANK_INFO
        activeServiceViewModel.refreshTabsState()
        if (navigateToNext) activeServiceViewModel.moveToNextTab()
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun populateViews(fileUrlResponse: FileUrlResponse?) {
        if (!fileUrlResponse?.idCardFileUrl.isNullOrEmpty() && SharedPref.onboardingStage == OnboardingStage.ACTIVE_DOCUMENTS) {
            SharedPref.onboardingStage = OnboardingStage.ACTIVE_BANK_INFO
            activeServiceViewModel.refreshTabsState()
        }
        rvAdapter.items[ID_CARD]!!.uploadedUrl = fileUrlResponse?.idCardFileUrl
        rvAdapter.items[PHOTO]!!.uploadedUrl = fileUrlResponse?.passportPhotoFileUrl
        rvAdapter.items[APPLICATION_FORM]!!.uploadedUrl = fileUrlResponse?.applicationFormFileUrl
        rvAdapter.items[LETTER]!!.uploadedUrl =
            fileUrlResponse?.promotionLetterTransferLetterFileUrl
        rvAdapter.items[CLEARANCE]!!.uploadedUrl = fileUrlResponse?.clearanceFormFileUrl
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


