package com.example.engu_pension_verification_application.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.model.response.TransactionHistoryResponse

class TransactionHistoryPagingSource(private val networkRepo: NetworkRepo) :
    PagingSource<Int, TransactionHistoryResponse.Detail.Data.Transaction>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TransactionHistoryResponse.Detail.Data.Transaction> {
        val currentPage = params.key ?: 1
        return try {
            val response =
                networkRepo.fetchTransactionHistory(page = currentPage, limit = params.loadSize)
            if (response.detail.tokenStatus == AppConstants.EXPIRED) {
                LoadResult.Error(Exception(AppConstants.TOKEN_EXPIRED))
            } else {
                val transactions = response.detail.data!!.transactions
                LoadResult.Page(
                    data = transactions,
                    prevKey = if (currentPage == 1) null else currentPage - 1,
                    nextKey = if (transactions.isEmpty()) null else currentPage + 1
                )
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, TransactionHistoryResponse.Detail.Data.Transaction>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}