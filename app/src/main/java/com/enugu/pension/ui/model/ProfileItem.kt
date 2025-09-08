package com.enugu.pension.ui.model

import com.enugu.pension.common.util.ProfileItemType

data class ProfileItem(
    val profileItemType: ProfileItemType,
    var key: String,
    var value: String,
    val isEditable: Boolean = false,
)