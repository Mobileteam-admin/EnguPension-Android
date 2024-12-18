package com.example.engu_pension_verification_application.ui.dialog

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.databinding.DialogEnguCalendarBinding
import com.example.engu_pension_verification_application.ui.custom.CalendarLabelView
import com.example.engu_pension_verification_application.util.AppUtils.Companion.isValidNumber
import com.example.engu_pension_verification_application.util.CalendarUtils
import com.example.engu_pension_verification_application.viewmodel.EnguCalendarHandlerViewModel
import com.example.engu_pension_verification_application.viewmodel.EnguCalendarViewModel
import com.example.engu_pension_verification_application.viewmodel.EnguCalendarViewModel.DayType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class EnguCalendarDialog : BaseDialog() {
    private lateinit var binding: DialogEnguCalendarBinding
    private val viewModel by viewModels<EnguCalendarViewModel>()
    private val handlerViewModel by activityViewModels<EnguCalendarHandlerViewModel>()
    private val labels = mutableListOf<MutableList<CalendarLabelView>>()
    private val dayTypes = mutableListOf<MutableList<DayType>>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DialogEnguCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initValues()
        initViews()
        observeLiveData()
    }

    private fun observeLiveData() {
        handlerViewModel.onDismiss.observe(this) {
            if (it != null && isAdded) {
                handlerViewModel.onDismiss.value = null
                dismiss()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun initValues() {
        if (handlerViewModel.initSelectedDay == null) {
            if (handlerViewModel.enguCalendarRange == null) {
                viewModel.calendar.set(
                    Calendar.YEAR,
                    minOf(handlerViewModel.maxYear, Calendar.getInstance().get(Calendar.YEAR))
                )
            } else {
                viewModel.calendar.time = handlerViewModel.enguCalendarRange!!.ranges.last().second.time
            }
        } else {
            viewModel.calendar.time = handlerViewModel.initSelectedDay!!.time
            viewModel.selectedDay = viewModel.calendar.get(Calendar.DAY_OF_MONTH)
            viewModel.selectedMonth = viewModel.calendar.get(Calendar.MONTH)
            viewModel.selectedYear = viewModel.calendar.get(Calendar.YEAR)
        }
    }

    private fun initViews() {
        initCalendarItems()
        refreshCalendar()
        binding.tvYear.setOnClickListener {
            showYearPickDialog()
        }
        binding.tvMonth.setOnClickListener {
            showMonthPickDialog()
        }

        binding.btnPrev.setOnClickListener {
            viewModel.calendar.add(Calendar.MONTH, -1)
            refreshCalendar()
        }

        binding.btnNext.setOnClickListener {
            viewModel.calendar.add(Calendar.MONTH, 1)
            refreshCalendar()
        }

        binding.llClose.setOnClickListener { dismiss() }
        binding.llSubmit.setOnClickListener {
            val selectedDate = viewModel.getSelectedDate()
            if (selectedDate == null)
                showToast(R.string.date_not_selected_msg)
            else {
//                dismiss()
                handlerViewModel.onDateSelect.value = selectedDate
            }
        }
    }

    private fun initCalendarItems() {
        labels.clear()
        dayTypes.clear()
        refreshMonthYearTexts()
        val dayLabels = arrayOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
        for (dayLabel in dayLabels) {
            val label = CalendarLabelView(requireContext())
            label.setText(dayLabel)
            label.setTextColor(R.color.black_text_2)
            label.setRoundBgVisibility(false)
            binding.gridCalendar.addView(label)
        }
        repeat(6) { row ->
            val labelRow = mutableListOf<CalendarLabelView>()
            val dayTypeRow = mutableListOf<DayType>()
            repeat(7) { column ->
                val calendarLabelView = CalendarLabelView(requireContext())
                calendarLabelView.setTextColor(R.color.black_text_3)
                binding.gridCalendar.addView(calendarLabelView)
                labelRow.add(calendarLabelView)
                dayTypeRow.add(DayType.SELECTABLE)
                calendarLabelView.setClickListener({
                    selectDate(row, column)
                })
            }
            labels.add(labelRow)
            dayTypes.add(dayTypeRow)
        }
    }

    private fun selectDate(row: Int, column: Int) {
        if (dayTypes[row][column] == DayType.SELECTABLE && labels[row][column].getText().isValidNumber()) {
            if (viewModel.selectedDateRow >= 0 && viewModel.selectedDateColumn >= 0) {
                val lastSelectedLabel =
                    labels[viewModel.selectedDateRow][viewModel.selectedDateColumn]
                lastSelectedLabel.setRoundBgVisibility(false)
                lastSelectedLabel.setTextColor(getDateColor(DayType.SELECTABLE))
            }
            viewModel.selectedDateRow = row
            viewModel.selectedDateColumn = column
            viewModel.selectedYear = viewModel.calendar.get(Calendar.YEAR)
            viewModel.selectedMonth = viewModel.calendar.get(Calendar.MONTH)
            viewModel.selectedDay = labels[row][column].getText().toInt()
            labels[row][column].setRoundBgVisibility(true)
            labels[row][column].setTextColor(R.color.white)
        }
    }

    private fun refreshCalendar() {
        viewModel.selectedDateRow = -1
        viewModel.selectedDateColumn = -1
        refreshMonthYearTexts()
        val dayMax = viewModel.calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val startDay = getStartDay()
        repeat(6) { row ->
            repeat(7) { column ->
                val pos = row * 7 + column
                val date = pos - startDay + 1
                if (pos < startDay || date > dayMax) {
                    labels[row][column].setText("")
                    labels[row][column].setRoundBgVisibility(false)
                } else {
                    labels[row][column].setText("$date")
                    dayTypes[row][column] = getDayType(date)
                    val isInitSelectedDay =
                        viewModel.selectedDay == -1 && handlerViewModel.initSelectedDay?.let {
                            it.get(Calendar.DAY_OF_MONTH) == date &&
                                    it.get(Calendar.MONTH) == viewModel.calendar.get(Calendar.MONTH) &&
                                    it.get(Calendar.YEAR) == viewModel.calendar.get(Calendar.YEAR)
                        } == true
                    val isCurrentSelectedDay = viewModel.selectedDay == date &&
                            viewModel.selectedMonth == viewModel.calendar.get(Calendar.MONTH) &&
                            viewModel.selectedYear == viewModel.calendar.get(Calendar.YEAR)
                    if (isInitSelectedDay || isCurrentSelectedDay) {
                        viewModel.selectedDateRow = row
                        viewModel.selectedDateColumn = column
                        labels[row][column].setRoundBgVisibility(true)
                        labels[row][column].setTextColor(R.color.white)
                    } else {
                        labels[row][column].setTextColor(getDateColor(dayTypes[row][column]))
                        labels[row][column].setRoundBgVisibility(false)
                    }
                }
                labels[row][column].fadeIn()
            }
        }
    }
    private fun refreshMonthYearTexts() {
        val monthFormat = SimpleDateFormat(CalendarUtils.MONTH_FORMAT_1, Locale.getDefault())
        binding.tvMonth.text = monthFormat.format(viewModel.calendar.time)
        viewModel.calendar.get(Calendar.YEAR).toString().also { binding.tvYear.text = it }
    }

    private fun getDateColor(dayType: DayType) =
        when (dayType) {
            DayType.SELECTABLE -> R.color.black
            DayType.INVALID -> R.color.grey_text_1
            DayType.HOLIDAY -> R.color.red
        }

    private fun getDayType(date: Int): DayType {
        val selectedDate = Calendar.getInstance().apply {
            set(viewModel.calendar.get(Calendar.YEAR), viewModel.calendar.get(Calendar.MONTH), date)
        }
        handlerViewModel.enguCalendarRange?.let {
            it.holidays.forEach { holiday ->
                if (CalendarUtils.isSameDay(holiday, selectedDate))
                    return DayType.HOLIDAY
            }
            it.ranges.forEach { range ->
                if (CalendarUtils.isDateInRange(selectedDate, range.first, range.second))
                    return DayType.SELECTABLE
            }
        }
        return DayType.INVALID
    }
    private fun getStartDay(): Int {
        val calendar = Calendar.getInstance().apply { time = viewModel.calendar.time }
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        var startDay = calendar.get(Calendar.DAY_OF_WEEK) - 2
        startDay = if (startDay == -1) 6 else startDay
        return startDay
    }
    private fun showYearPickDialog() {
        val years = mutableListOf<String>()
        for (i in handlerViewModel.maxYear downTo handlerViewModel.minYear)
            years.add(i.toString())
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.choose_year)
            .setItems(years.toTypedArray()) { _, i ->
                viewModel.calendar.set(Calendar.YEAR, years[i].toInt())
                refreshCalendar()
            }.show()
    }
    private fun showMonthPickDialog() {
        val months = CalendarUtils.getMonthsList()
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.choose_month)
            .setItems(months.toTypedArray()) { _, i ->
                viewModel.calendar.set(Calendar.MONTH, i)
                refreshCalendar()
            }.show()
    }
}