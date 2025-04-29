package com.enugu.pension.model.ui

import android.content.res.Resources
import com.enugu.pension.R
import com.enugu.pension.util.ProfileItemType

class ProfileInfo(resources: Resources) {
    private val _items = mutableListOf <ProfileItem>()
    val items : List<ProfileItem>
        get() = _items.toList()
    init {
        _items.add(ProfileItem(ProfileItemType.EIN,resources.getString(R.string.ein),"", isEditable = true))
        _items.add(ProfileItem(ProfileItemType.EMP_STATUS,resources.getString(R.string.employment_status),"", isEditable = true))
        _items.add(ProfileItem(ProfileItemType.DESIGNATION,resources.getString(R.string.designation),"", isEditable = true))
        _items.add(ProfileItem(ProfileItemType.DEPARTMENT,resources.getString(R.string.department),""))
        _items.add(ProfileItem(ProfileItemType.DURATION,resources.getString(R.string.duration),""))
        _items.add(ProfileItem(ProfileItemType.STATE,resources.getString(R.string.state),""))
        _items.add(ProfileItem(ProfileItemType.REGION,resources.getString(R.string.region),""))
        _items.add(ProfileItem(ProfileItemType.STATUS,resources.getString(R.string.status),""))
    }

    fun setValue(profileItemType: ProfileItemType, value:String?) {
        _items[ProfileItemType.entries.indexOf(profileItemType)].value = value?: ""
    }
    fun getValue(profileItemType: ProfileItemType) =
        _items[ProfileItemType.entries.indexOf(profileItemType)].value

}