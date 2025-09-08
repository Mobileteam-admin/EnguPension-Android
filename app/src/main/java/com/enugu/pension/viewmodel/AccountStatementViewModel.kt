package com.enugu.pension.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enugu.pension.data.repository.NetworkRepo
import com.enugu.pension.data.remote.dto.response.StatementResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountStatementViewModel(private val networkRepo: NetworkRepo) : ViewModel() {

    private val _statementApiResult = MutableLiveData<StatementResponse>()
    val statementApiResult: LiveData<StatementResponse>
        get() = _statementApiResult

    fun fetchStatement(startDate: String,endDate: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _statementApiResult.postValue(networkRepo.fetchStatement(startDate, endDate))
            } catch (e: Exception) {
                _statementApiResult.postValue(
                    StatementResponse(StatementResponse.Detail(message = "Something went wrong."))
                )
            }
        }
    }
}