package com.example.engu_pension_verification_application.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.example.engu_pension_verification_application.Constants.AppConstants
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.data.NetworkRepo
import com.example.engu_pension_verification_application.model.input.BookAppointmentRequest
import com.example.engu_pension_verification_application.network.ApiClient
import com.example.engu_pension_verification_application.util.CalendarUtils
import com.example.engu_pension_verification_application.viewmodel.AppointmentViewModel
import com.example.engu_pension_verification_application.viewmodel.CalendarResultViewModel
import com.example.engu_pension_verification_application.viewmodel.CalendarViewModel
import com.example.engu_pension_verification_application.viewmodel.EnguViewModelFactory
import com.example.engu_pension_verification_application.viewmodel.TokenRefreshViewModel2
import kotlinx.android.synthetic.main.dialog_appointment.ll_back
import kotlinx.android.synthetic.main.dialog_appointment.ll_pay_now
import kotlinx.android.synthetic.main.dialog_appointment.tv_date
import kotlinx.android.synthetic.main.dialog_appointment.tv_time
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AppointmentDialog : BaseDialog() {
    private lateinit var viewModel: AppointmentViewModel
    private lateinit var tokenRefreshViewModel2: TokenRefreshViewModel2
    private val calendarResultViewModel by activityViewModels<CalendarResultViewModel>()
    private lateinit var calendarDialog: CalendarDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_appointment, container, false)
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
        showLoader()
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
        calendarResultViewModel.onDateSelect.observe(viewLifecycleOwner) { calendar ->
            if (calendar != null) {
                val selectedDay = CalendarUtils.getFormattedString(
                    CalendarUtils.DATE_FORMAT_3,
                    calendar
                )
                tv_date.text = selectedDay
                tv_time.text = ""
                viewModel.selectedDate = selectedDay
                viewModel.selectedTimeSlotId = null
                showLoader()
                viewModel.fetchBookingSlots(selectedDay)
                calendarResultViewModel.onDateSelect.value = null
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
                    if (calendarDialog.isAdded)
                        calendarDialog.dismiss()
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
                dismissLoader()
                response.detail?.bookingDateRange?.let {
                    calendarResultViewModel.dateRange = it
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
                showDialog(BookingDetailsDialog.newInstance(tv_date.text.toString(),tv_time.text.toString()))
            } else {
                if (response.detail?.tokenStatus.equals(AppConstants.EXPIRED)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (tokenRefreshViewModel2.fetchRefreshToken()) {
                            viewModel.bookAppointmentCall(request)
                        }
                    }
                } else {
                    dismissLoader()
                    response.detail?.message?.let { showToast(it) }
                    dismiss()
                }
            }
        }
    }

    private fun initViews() {
        calendarDialog = CalendarDialog()
        tv_date.setOnClickListener {
            showDialog(calendarDialog)
        }
        tv_time.setOnClickListener { showTimeSlotPopUp() }
        ll_back.setOnClickListener { dismiss() }
        ll_pay_now.setOnClickListener { bookAppointment() }

    }

    private fun bookAppointment() {
        if (viewModel.selectedDate == null) {
            showToast(R.string.date_not_selected_msg)
        } else if (viewModel.selectedTimeSlotId == null) {
            showToast(R.string.slot_not_selected_msg)
        } else {
            showLoader()
            viewModel.bookAppointmentCall(
                BookAppointmentRequest(
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
            showListPopUp(tv_time, items) { position, text ->
                val slot = viewModel.slotApiResult.value?.second?.detail?.slots?.get(position)
                viewModel.selectedTimeSlotId = slot?.id
                tv_time.text = text
            }
        } else if (tv_date.text.isNotEmpty()){
            showToast(R.string.no_time_slots_msg)
        }
    }
}