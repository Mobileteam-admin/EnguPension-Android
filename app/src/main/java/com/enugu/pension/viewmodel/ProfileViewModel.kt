package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.model.request.UpdateProfileForm
import com.enugu.pension.model.response.ProfileResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    var profilePicture: File? = null
    private val _profileFetchApiResult = MutableLiveData<ProfileResponse>(null)
    val profileFetchApiResult: LiveData<ProfileResponse>
        get() = _profileFetchApiResult

    private val _profileUpdateApiResult = MutableLiveData<Pair<UpdateProfileForm,ProfileResponse>?>(null)
    val profileUpdateApiResult: LiveData<Pair<UpdateProfileForm,ProfileResponse>?>
        get() = _profileUpdateApiResult


    fun resetProfileUpdateApiResult() {
        _profileUpdateApiResult.postValue(null)
    }
    fun fetchProfileDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _profileFetchApiResult.postValue(networkRepo.fetchProfileDetails())
            } catch (e: Exception) {
                _profileFetchApiResult.postValue(
                    ProfileResponse(ProfileResponse.Detail("Something went wrong with fetching profile details"))
                )
            }
        }
    }

    fun updateProfileDetails(form: UpdateProfileForm) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _profileUpdateApiResult.postValue(Pair(form,networkRepo.updateProfileDetails(form)))
            } catch (e: Exception) {
                e.printStackTrace()
                _profileUpdateApiResult.postValue(Pair(form,ProfileResponse(ProfileResponse.Detail("Something went wrong with fetching profile details"))))
            }
        }
    }

}