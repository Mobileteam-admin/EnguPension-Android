package com.example.engu_pension_verification_application.ui.fragment.service.gradelevel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.model.response.GradeLevelsItem
import com.example.engu_pension_verification_application.ui.fragment.service.subtresury.SubTreasuryAdapter
import java.util.ArrayList

class GradeLevelAdapter(var context: Context?,list: ArrayList<GradeLevelsItem?>) :
    BaseAdapter() {
    private val GradeLevelsList = ArrayList<GradeLevelsItem?>()
    init {
        this.GradeLevelsList.addAll(list)
    }
    val mInflater: LayoutInflater = LayoutInflater.from(context)

    fun changeList(list: ArrayList<GradeLevelsItem?>) {
        this.GradeLevelsList.clear()
        this.GradeLevelsList.addAll(list)
        notifyDataSetChanged()
    }

    fun getPositionByName(name: String): Int {
        return GradeLevelsList.indexOfFirst { it?.level == name }
    }

    //there is gradelvl spinner not working on retiree basic  retrive using getPositionByName(),
    // so created this fn ,getPositionById(),for hanlding retiree basic retrive
    fun getPositionById(id: Int): Int {
        return GradeLevelsList.indexOfFirst { it?.id == id }
    }

    override fun getCount(): Int {
      return  GradeLevelsList.size
    }

    override fun getItem(p0: Int): Any {
       return 0
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(p0: Int, convertView: View?, p2: ViewGroup?): View {
        val view: View
        val vh: ItemRowHolder
        if (convertView == null) {
            view = mInflater.inflate(R.layout.sp_dropdown_menu, p2, false)
            vh = ItemRowHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }

        val params = view.layoutParams
        params.height = 80
        view.layoutParams = params

        vh.label.text = GradeLevelsList.get(p0)?.level
        return view
    }

    private class ItemRowHolder(row: View?) {

        val label: TextView

        init {
            this.label = row?.findViewById(R.id.txtDropDownLabel) as TextView
        }
    }
}