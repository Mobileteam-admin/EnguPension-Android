package com.enugu.pension.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.enugu.pension.common.constant.AppConstants
import com.enugu.pension.R
import com.enugu.pension.data.repository.NetworkRepo
import com.enugu.pension.databinding.DialogAppointmentBinding
import com.enugu.pension.data.remote.api.ApiClient
import com.enugu.pension.common.util.CalendarUtils
import com.enugu.pension.viewmodel.AppointmentViewModel
import com.enugu.pension.viewmodel.EnguCalendarHandlerViewModel
import com.enugu.pension.viewmodel.EnguViewModelFactory
import com.enugu.pension.viewmodel.TokenRefreshViewModel2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AppointmentDialog : BaseDialog() {
    private lateinit var binding:DialogAppointmentBinding
    private lateinit var viewModel: AppointmentViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    private val enguCalendarHandlerViewModel by activityViewModels<EnguCalendarHandlerViewModel>()
    private lateinit var enguCalendarDialog: EnguCalendarDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        dismissOnDoubleBackPress = true
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DialogAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissLoader()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initValues()
        initViews()
        observeLiveData()
        viewModel.fetchBookingDateRange()
    }

    private fun initValues() {
        viewModel.selectedDate = null
        viewModel.selectedTimeSlotId = null
    }

    private fun initViewModel() {
        val networkRepo = NetworkRepo(ApiClient.getApiInterface())
        viewModel = ViewModelProviders.of(
            this,
            EnguViewModelFactory(networkRepo)
        ).get(AppointmentViewModel::class.java)
        tokenRefreshViewModel2 = ViewModelProviders.of(
            requireActivity(),
            EnguViewModelFactory(networkRepo)
        ).get(TokenRefreshViewModel2::class.java)
    }

    private fun observeLiveData() {
        enguCalendarHandlerViewModel.onDateSelect.observe(viewLifecycleOwner) { calendar ->
            if (calendar != null) {
                val selectedDay = CalendarUtils.getFormattedString(
                    CalendarUtils.DATE_FORMAT_3,
                    calendar
                )
                binding.tvDate.text = selectedDay
                binding.tvTime.text = ""
                viewModel.selectedDate = selectedDay
                viewModel.selectedTimeSlotId = null
                showLoader()
                viewModel.fetchBookingSlots(selectedDay)
                enguCalendarHandlerViewModel.onDateSelect.value = null
            }
        }
        viewModel.slotApiResult.observe(viewLifecycleOwner) { pair ->
            val selectedDay = pair.first
            val response = pair.second
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                if (response.detail!!.slots.isEmpty()){
                    response.detail?.message?.let { showToast(it) }
                } else {
                    if (enguCalendarDialog.isAdded)
                        enguCalendarDialog.dismiss()
                }
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.fetchBookingSlots(selectedDay)
                        }
                    }
                } else {
                    dismissLoader()
                    response.detail?.message?.let { showToast(it) }
                    dismiss()
                }
            }
        }
        viewModel.dateRangeApiResult.observe(viewLifecycleOwner) { response ->
            if (response.detail?.status == AppConstants.SUCCESS) {
                binding.pbDate.isGone = true
                response.detail?.bookingDateRange?.let {
                    enguCalendarHandlerViewModel.enguCalendarRange = CalendarUtils.getEnguCalendarRange(it, false)
                }
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.fetchBookingDateRange()
                        }
                    }
                } else {
                    dismissLoader()
                    response.detail?.message?.let { showToast(it) }
                    dismiss()
                }
            }
        }

        viewModel.bookAppointmentApiResult.observe(viewLifecycleOwner) { pair ->
            val request = pair.first
            val response = pair.second
            if (response.detail?.status == AppConstants.SUCCESS) {
                dismissLoader()
                response.detail?.message?.let { showToast(it) }
                dismiss()
                showDialog(BookingDetailsDialog.newInstance(binding.tvDate.text.toString(),binding.tvTime.text.toString()))
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.bookAppointment(request)
                        }
                    }
                } else {
                    dismissLoader()
                    response.detail?.message?.let { showToast(it) }
                }
            }
        }
    }

    private fun initViews() {
        binding.pbDate.isVisible = true
        enguCalendarDialog = EnguCalendarDialog()
        binding.tvDate.setOnClickListener {
            if (binding.tvDate.text.isNullOrEmpty()) {
                enguCalendarHandlerViewModel.openRangeLastMonth = false
            } else {
                val date = binding.tvDate.text.toString()
                enguCalendarHandlerViewModel.initSelectedDay = CalendarUtils.getCalendar(
                    CalendarUtils.DATE_FORMAT_3, date)
            }
            showDialog(enguCalendarDialog)
        }
        binding.tvTime.setOnClickListener { showTimeSlotPopUp() }
        binding.llBack.setOnClickListener { dismiss() }
        binding.llBookNow.setOnClickListener { bookAppointment() }

    }

    private fun bookAppointment() {
        if (viewModel.selectedDate == null) {
            showToast(R.string.date_not_selected_msg)
        } else if (viewModel.selectedTimeSlotId == null) {
            showToast(R.string.slot_not_selected_msg)
        } else {
            showLoader()
            viewModel.bookAppointment(
                com.enugu.pension.data.remote.dto.request.BookAppointmentRequest(
                    viewModel.selectedDate!!,
                    viewModel.selectedTimeSlotId!!
                )
            )
        }
    }

    private fun showTimeSlotPopUp() {
        val items = mutableListOf<String>()
        viewModel.slotApiResult.value?.second?.detail?.slots?.forEach {
            items.add("${it.startTime} - ${it.endTime}")
        }
        if (items.isNotEmpty()) {
            showListPopUp(binding.tvTime, items) { position, text ->
                val slot = viewModel.slotApiResult.value?.second?.detail?.slots?.get(position)
                viewModel.selectedTimeSlotId = slot?.id
                binding.tvTime.text = text
            }
        } else if (binding.tvDate.text.isNotEmpty()){
            showToast(R.string.no_time_slots_msg)
        }
    }
}