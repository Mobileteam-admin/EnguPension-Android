package com.enugu.pension.ui.fragment.profile

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.view.isGone
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.enugu.pension.R
import com.enugu.pension.constant.AppConstants
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.databinding.FragmentProfileBinding
import com.enugu.pension.databinding.ItemProfile2Binding
import com.enugu.pension.model.request.UpdateProfileForm
import com.enugu.pension.network.ApiClient
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.util.AppUtils
import com.enugu.pension.util.FileUtils
import com.enugu.pension.viewmodel.DashboardViewModel
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.ProfileViewModel
import com.enugu.pension.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ProfileFragment : BaseFragment() {
    companion object {
        private const val MAX_FILE_SIZE_IN_MB = 2

        private const val INFO_ITEM_COUNT = 8

        private const val EIN_INDEX = 0
        private const val EMP_STATUS_INDEX = 1
        private const val DESIGNATION_INDEX = 2
        private const val DEPARTMENT_INDEX = 3
        private const val DURATION_INDEX = 4
        private const val STATE_INDEX = 5
        private const val REGION_INDEX = 6
        private const val STATUS_INDEX = 7
    }

    private lateinit var binding: FragmentProfileBinding
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var viewModel: ProfileViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2

    //    private lateinit var adapter: ProfileAdapter
//    private lateinit var profileInfo: ProfileInfo
    private val infoBindingList = mutableListOf<ItemProfile2Binding>()

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { onFileSelected(it) }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initViews()
        observeLiveData()
        showLoader()
        viewModel.fetchProfileDetails()
    }

    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        dashboardViewModel = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(DashboardViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
        viewModel = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(ProfileViewModel::class.java)
    }

    private fun initViews() {
        initInfoList()
//        initRvProfile()
        dashboardViewModel.dashboardDetailsResult.value?.detail?.let {
            binding.tvName.text = it.fullName ?: ""
            setProfileImageView(Uri.parse(it.profilePic))
        }

        binding.imgBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.llBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.ivEditProfilePic.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.llSubmit.setOnClickListener {
            if (isValidInput()) {
                showLoader()
                viewModel.updateProfileDetails(
                    UpdateProfileForm(
                        infoBindingList[EIN_INDEX].etInfo.text?.toString() ?: "",
                        infoBindingList[EMP_STATUS_INDEX].etInfo.text?.toString() ?: "",
                        infoBindingList[DESIGNATION_INDEX].etInfo.text?.toString() ?: "",
                        infoBindingList[DEPARTMENT_INDEX].etInfo.text?.toString() ?: "",
                        infoBindingList[DURATION_INDEX].etInfo.text?.toString() ?: "",
                        infoBindingList[STATE_INDEX].etInfo.text?.toString() ?: "",
                        infoBindingList[REGION_INDEX].etInfo.text?.toString() ?: "",
                        infoBindingList[STATUS_INDEX].etInfo.text?.toString() ?: "",
                        viewModel.profilePicture
                    )
                )
            }
        }
    }

    private fun initInfoList() {
        binding.llProfile.removeAllViews()
        val hintList = listOf(
            R.string.ein,
            R.string.employment_status,
            R.string.designation,
            R.string.department,
            R.string.duration,
            R.string.state,
            R.string.region,
            R.string.status,
        )
        val hiddenItemIndices = listOf(DURATION_INDEX, REGION_INDEX)
        for(i in 0 until INFO_ITEM_COUNT) {
            val item = addInfoItem(hintList[i])
            infoBindingList.add(item)
            if (i in hiddenItemIndices) {
                item.root.isGone = true
            }
        }
        repeat(hiddenItemIndices.size) {
            infoBindingList.add(addInfoItem(hintList[it]))
        }
        binding.llProfile.requestLayout()
    }

    private fun addInfoItem(@StringRes hint: Int): ItemProfile2Binding {
        val itemBinding = ItemProfile2Binding.inflate(
            LayoutInflater.from(requireContext()),
            binding.llProfile,
            false
        )
        itemBinding.etInfo.setText("")
        itemBinding.tilInfo.hint = getString(hint)
        binding.llProfile.addView(itemBinding.root)
        return itemBinding
    }

    private fun initRvProfile() {
//        profileInfo = ProfileInfo(resources)
//        adapter = ProfileAdapter(profileInfo)
//        binding.rvProfile.adapter = adapter
    }


    private fun setProfileImageView(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.baseline_account_circle_green)
//            .override(Target.SIZE_ORIGINAL, binding.ivProfilePic.height)
            .into(binding.ivProfilePic)
    }
    private fun observeLiveData() {
        viewModel.profileFetchApiResult.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.detail?.status == AppConstants.SUCCESS) {
                    dismissLoader()
                    populateViews()
                } else {
                    if (response.detail?.tokenStatus == AppConstants.EXPIRED) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (tokenRefreshViewModel2.fetchRefreshToken()) {
                                viewModel.fetchProfileDetails()
                            }
                        }
                    } else {
                        dismissLoader()
                        response.detail?.message?.let { showToast(it) }
                    }
                }
            }
        }
        viewModel.profileUpdateApiResult.observe(viewLifecycleOwner) {
            if (it != null) {
                val form = it.first
                val response = it.second
                if (response.detail?.status == AppConstants.SUCCESS) {
                    dismissLoader()
                    response.detail?.message?.let { message -> showToast(message) }
                    dashboardViewModel.profilePictureUrl.postValue(response.detail?.userProfileDetails?.imageUrl)
                    findNavController().popBackStack()
//                    populateViews()
                } else {
                    if (response.detail?.tokenStatus == AppConstants.EXPIRED) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            if (tokenRefreshViewModel2.fetchRefreshToken()) {
                                viewModel.updateProfileDetails(form)
                            }
                        }
                    } else {
                        dismissLoader()
                        response.detail?.message?.let { message -> showToast(message) }
                    }
                }
                viewModel.resetProfileUpdateApiResult()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun populateViews() {
        viewModel.profileFetchApiResult.value?.detail?.userProfileDetails?.let {
//            profileInfo.setValue(ProfileItemType.EIN, it.ein)
//            profileInfo.setValue(ProfileItemType.EMP_STATUS, it.employmentStatus)
//            profileInfo.setValue(ProfileItemType.DESIGNATION, it.designation)
//            profileInfo.setValue(ProfileItemType.DEPARTMENT, it.department)
//            profileInfo.setValue(ProfileItemType.DURATION, it.duration)
//            profileInfo.setValue(ProfileItemType.STATE, it.state)
//            profileInfo.setValue(ProfileItemType.REGION, it.region)
//            profileInfo.setValue(ProfileItemType.STATUS, it.status)
//            adapter.notifyDataSetChanged()

            infoBindingList[EIN_INDEX].etInfo.setText(it.ein)
            infoBindingList[EMP_STATUS_INDEX].etInfo.setText(it.employmentStatus)
            infoBindingList[DESIGNATION_INDEX].etInfo.setText(it.designation)
            infoBindingList[DEPARTMENT_INDEX].etInfo.setText(it.department)
            infoBindingList[DURATION_INDEX].etInfo.setText(it.duration)
            infoBindingList[STATE_INDEX].etInfo.setText(it.state)
            infoBindingList[REGION_INDEX].etInfo.setText(it.region)
            infoBindingList[STATUS_INDEX].etInfo.setText(it.status)

            it.imageUrl?.let {imageUrl -> setProfileImageView(Uri.parse(imageUrl))}
        }
    }

    private fun onFileSelected(uri: Uri) {
        val file = FileUtils.getFileFromUri(requireContext(), uri)
        if (file == null) {
            showToast(R.string.file_selection_error)
        } else {
            if (FileUtils.getFileSizeInMB(file) > MAX_FILE_SIZE_IN_MB) {
                showToast(getString(R.string.fail_size_exceed_error, MAX_FILE_SIZE_IN_MB))
            } else {
//                binding.ivProfilePic.setImageURI(uri)
                setProfileImageView(uri)
                viewModel.profilePicture = file
            }
        }
    }


    private fun isValidInput(): Boolean {
        var errorMessage: Int? = null
        if (!AppUtils.isValidEIN(infoBindingList[EIN_INDEX].etInfo.text?.toString())) {
            errorMessage = R.string.invalid_ein_error
        }
        errorMessage?.let { showToast(it) }
        return errorMessage == null
    }

}