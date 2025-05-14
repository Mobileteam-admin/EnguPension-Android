package com.enugu.pension.ui.fragment.account

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.enugu.pension.constant.AppConstants
import com.enugu.pension.R
import com.enugu.pension.data.NetworkRepo
import com.enugu.pension.databinding.FragmentAccountStatementBinding
import com.enugu.pension.model.misc.EnguCalendarRange
import com.enugu.pension.model.ui.StatementItem
import com.enugu.pension.network.ApiClient
import com.enugu.pension.ui.adapter.StatementAdapter
import com.enugu.pension.ui.dialog.EnguCalendarDialog
import com.enugu.pension.ui.fragment.base.BaseFragment
import com.enugu.pension.util.CalendarUtils
import com.enugu.pension.util.NetworkUtils
import com.enugu.pension.viewmodel.AccountStatementViewModel
import com.enugu.pension.viewmodel.DashboardViewModel
import com.enugu.pension.viewmodel.EnguCalendarHandlerViewModel
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AccountStatementFragment : BaseFragment() {
    private lateinit var binding: FragmentAccountStatementBinding
    private lateinit var viewModel: AccountStatementViewModel
    private lateinit var dashboardViewModel: DashboardViewModel
    private val enguCalendarHandlerViewModel by activityViewModels<EnguCalendarHandlerViewModel>()
    private lateinit var enguCalendarDialog: EnguCalendarDialog
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    private lateinit var statementAdapter: StatementAdapter

    companion object {
        const val CALENDAR_ACTION_FROM = 1
        const val CALENDAR_ACTION_TO = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountStatementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModels()
        initViews()
        observeLiveData()
    }

    private fun initViewModels() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        dashboardViewModel = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(DashboardViewModel::class.java)
        viewModel = ViewModelProviders.of(
            this, EnguViewModelFactory(networkRepo)
        ).get(AccountStatementViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(), EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }

    private fun initViews() {
        initRvStatement()
        binding.tvEmptyMessage.isGone = true
        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }
        enguCalendarDialog = EnguCalendarDialog()
        binding.tvFrom.setOnClickListener { showCalendar(CALENDAR_ACTION_FROM) }
        binding.tvTo.setOnClickListener { showCalendar(CALENDAR_ACTION_TO) }
    }


    private fun initRvStatement() {
        statementAdapter = StatementAdapter()
        binding.rvStatement.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = statementAdapter
        }
    }

    private fun observeLiveData() {
        enguCalendarHandlerViewModel.onDateSelect.observe(viewLifecycleOwner) { calendar ->
            if (calendar != null) {
                val selectedDay = CalendarUtils.getFormattedString(
                    CalendarUtils.DATE_FORMAT_3,
                    calendar
                )
                if (enguCalendarHandlerViewModel.actionId == CALENDAR_ACTION_FROM)
                    binding.tvFrom.text = selectedDay
                else if (enguCalendarHandlerViewModel.actionId == CALENDAR_ACTION_TO)
                    binding.tvTo.text = selectedDay

                enguCalendarDialog.dismiss()
                enguCalendarHandlerViewModel.onDateSelect.value = null
                fetchStatement()
            }
        }
        dashboardViewModel.dashboardDetailsResult.observe(viewLifecycleOwner) { response ->
            if (response?.detail?.status == AppConstants.SUCCESS) {
                populateViews()
            }
        }
        viewModel.statementApiResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                refreshStatementList()
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            fetchStatement()
                        }
                    }
                } else {
                    refreshStatementList()
                    Toast.makeText(context, response.detail?.message, Toast.LENGTH_LONG)
                        .show() // TODO:
                }
            }
        }
    }

    private fun refreshStatementList() {
        val items = mutableListOf<StatementItem>()
        viewModel.statementApiResult.value?.detail?.pensionData.let { pensionData ->
            pensionData?.salaryReceived?.forEach {
                items.add(
                    StatementItem(
                        id = it.id,
                        amount = it.amount,
                        date = it.paymentDate,
                    )
                )
            }
            pensionData?.pensionWithdrawn?.forEach {
                items.add(
                    StatementItem(
                        id = it.id,
                        amount = it.amount,
                        date = it.transactionDate,
                        description = it.description,
                    )
                )
            }
        }
        statementAdapter.refreshList(items)
        binding.tvEmptyMessage.isGone = items.isNotEmpty()
    }

    private fun populateViews() {
        dashboardViewModel.dashboardDetailsResult.value?.detail?.let {
            val walletText = "${it.walletBalanceCurrency} ${it.walletBalanceAmount.toString()}"
            binding.tvWalletAmount.text = walletText
            binding.ivNaira.isGone = true
        }
    }

    private fun showCalendar(actionId: Int) {
        val startCalendar =
            if (actionId == CALENDAR_ACTION_TO && !binding.tvFrom.text.isNullOrEmpty()) {
                CalendarUtils.getCalendar(
                    CalendarUtils.DATE_FORMAT_3,
                    binding.tvFrom.text.toString()
                )!!
            } else {
                CalendarUtils.getMinCalendar()
            }
        val endCalendar = Calendar.getInstance()
        enguCalendarHandlerViewModel.minYear = startCalendar.get(Calendar.YEAR)
        enguCalendarHandlerViewModel.maxYear = endCalendar.get(Calendar.YEAR)
        enguCalendarHandlerViewModel.enguCalendarRange = EnguCalendarRange(
            listOf(Pair(startCalendar, endCalendar))
        )
        enguCalendarHandlerViewModel.actionId = actionId
        showDialog(enguCalendarDialog)
    }

    private fun fetchStatement() {
        if (!binding.tvFrom.text.isNullOrEmpty() && !binding.tvTo.text.isNullOrEmpty()) {
            val from = CalendarUtils.getFormattedString(CalendarUtils.DATE_FORMAT_3, CalendarUtils.DATE_TIME_FORMAT_4,binding.tvFrom.text.toString())
            val toCalendar = CalendarUtils.getCalendar(CalendarUtils.DATE_FORMAT_3, binding.tvTo.text.toString())!!
            CalendarUtils.setDayEnd(toCalendar)
            val to = CalendarUtils.getFormattedString(CalendarUtils.DATE_TIME_FORMAT_4, toCalendar)
            viewModel.fetchStatement(from, to)
        }
    }
}