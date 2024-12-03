package com.example.engu_pension_verification_application.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.databinding.DialogCalendarBinding
import com.example.engu_pension_verification_application.ui.custom.CalendarLabelView
import com.example.engu_pension_verification_application.util.AppUtils.Companion.isValidNumber
import com.example.engu_pension_verification_application.util.CalendarUtils
import com.example.engu_pension_verification_application.viewmodel.CalendarResultViewModel
import com.example.engu_pension_verification_application.viewmodel.CalendarViewModel
import com.example.engu_pension_verification_application.viewmodel.CalendarViewModel.DayType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class CalendarDialog : BaseDialog() {
    private lateinit var binding:DialogCalendarBinding
    private val viewModel by viewModels<CalendarViewModel>()
    private val resultViewModel by activityViewModels<CalendarResultViewModel>()
    private val labels = mutableListOf<MutableList<CalendarLabelView>>()
    private val dayTypes = mutableListOf<MutableList<DayType>>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DialogCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initValues()
        initViews()
    }

    private fun initValues() {
    }

    private fun initViews() {
        initCalendarItems()
        refreshCalendar()
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
            if (viewModel.getSelectedDate() == null)
                showToast(R.string.date_not_selected_msg)
            else {
//                dismiss()
                resultViewModel.onDateSelect.value = viewModel.getSelectedDate()
            }
        }
    }

    private fun initCalendarItems() {
        labels.clear()
        dayTypes.clear()
        val monthFormat = SimpleDateFormat(CalendarUtils.DATE_FORMAT_2, Locale.getDefault())
        binding.txtMonthYear.text = monthFormat.format(viewModel.calendar.time)
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
        val monthFormat = SimpleDateFormat(CalendarUtils.DATE_FORMAT_2, Locale.getDefault())
        binding.txtMonthYear.text = monthFormat.format(viewModel.calendar.time)
        val dayMax = viewModel.calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        var startDay = viewModel.calendar.get(Calendar.DAY_OF_WEEK) - 2
        startDay = if (startDay == -1) 6 else startDay
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

                    if (viewModel.selectedDay == date &&
                        viewModel.selectedMonth == viewModel.calendar.get(Calendar.MONTH) &&
                        viewModel.selectedYear == viewModel.calendar.get(Calendar.YEAR)
                    ) {
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
        resultViewModel.dateRange.forEach {
            if (it.year?.toInt() == selectedDate.get(Calendar.YEAR)
                && CalendarUtils.getMonthNum(it.month.toString()) == selectedDate.get(Calendar.MONTH)
            ) {
                val calendarStart =
                    CalendarUtils.getCalendar(CalendarUtils.DATE_FORMAT_1, it.startDay!!)
                val calendarEnd =
                    CalendarUtils.getCalendar(CalendarUtils.DATE_FORMAT_1, it.endDay!!)
                if (CalendarUtils.isDateInRange(selectedDate, calendarStart!!, calendarEnd!!)) {
                    it.holidays.forEach { holiday ->
                        if (holiday.date != null) {
                            val holidayCal = CalendarUtils.getCalendar(
                                CalendarUtils.DATE_FORMAT_1,
                                holiday.date!!
                            )
                            if (CalendarUtils.isSameDay(
                                    holidayCal,
                                    selectedDate
                                )
                            ) return DayType.HOLIDAY
                        }
                    }
                    return DayType.SELECTABLE
                }
            }
        }
        return DayType.INVALID
    }
}