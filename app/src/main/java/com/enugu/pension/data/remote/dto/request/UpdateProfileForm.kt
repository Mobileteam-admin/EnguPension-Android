package com.enugu.pension.data.remote.dto.request

import com.enugu.pension.ui.model.KeyValue
import java.io.File

class UpdateProfileForm(
    ein: String,
    employmentStatus: String,
    designation: String,
    department: String,
    duration: String,
    state: String,
    region: String,
    status: String,
    val profilePicFile: File?,
) {
    companion object {
        const val EIN = "ein"
        const val EMPLOYMENT_STATUS = "employment_status"
        const val DESIGNATION = "designation"
        const val DEPARTMENT = "department"
        const val DURATION = "duration"
        const val STATE = "state"
        const val REGION = "region"
        const val STATUS = "status"
    }

    private val _items = mutableListOf<KeyValue>()
    val items: List<KeyValue>
        get() = _items.toList()

    init {
        _items.add(KeyValue(EIN, ein))
        _items.add(KeyValue(EMPLOYMENT_STATUS, employmentStatus))
        _items.add(KeyValue(DESIGNATION, designation))
        _items.add(KeyValue(DEPARTMENT, department))
        _items.add(KeyValue(DURATION, duration))
        _items.add(KeyValue(STATE, state))
        _items.add(KeyValue(REGION, region))
        _items.add(KeyValue(STATUS, status))
    }

}