package com.example.engu_pension_verification_application.model.dto

import java.util.Calendar

data class EnguCalendarRange(
    val ranges: List<Pair<Calendar, Calendar>>,
    val holidays: List<Calendar> = emptyList(),
)