package com.enugu.pension.model.misc

import java.util.Calendar

data class EnguCalendarRange(
    val ranges: List<Pair<Calendar, Calendar>>,
    val holidays: List<Calendar> = emptyList(),
)