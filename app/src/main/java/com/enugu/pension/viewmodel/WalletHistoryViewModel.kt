package com.enugu.pension.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.data.TransactionHistoryPagingSource
import com.enugu.pension.model.response.TransactionHistoryResponse
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