package com.enugu.pension.model.dto

import java.util.Calendar

data class EnguCalendarRange(
    val ranges: List<Pair<Calendar, Calendar>>,
    val holidays: List<Calendar> = emptyList(),
)