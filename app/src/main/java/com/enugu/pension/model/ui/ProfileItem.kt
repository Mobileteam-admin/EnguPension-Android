package com.enugu.pension.model.ui

import com.enugu.pension.util.ProfileItemType

data class ProfileItem(
    val profileItemType: ProfileItemType,
    var key: String,
    var value: String,
    val isEditable: Boolean = false,
)