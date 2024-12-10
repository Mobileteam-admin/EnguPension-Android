package com.example.engu_pension_verification_application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.data.TransactionHistoryPagingSource
import com.example.engu_pension_verification_application.model.response.TransactionHistoryResponse
import kotlinx.coroutines.flow.Flow

class WalletHistoryViewModel(private val networkRepo: NetworkRepo) : ViewModel() {
    val transactionFlow: Flow<PagingData<TransactionHistoryResponse.Detail.Data.Transaction>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            prefetchDistance = 6,
        ),
        pagingSourceFactory = { TransactionHistoryPagingSource(networkRepo) }
    ).flow.cachedIn(viewModelScope)
}