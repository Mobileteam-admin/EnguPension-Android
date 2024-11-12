package com.example.engu_pension_verification_application.ui.fragment.service.lastposition

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.model.response.CombineLastPositions
import java.util.ArrayList

class LastPositionAdapter(val context: Context?,val lastPositionList: ArrayList<CombineLastPositions?>): BaseAdapter() {
    val mInflater: LayoutInflater = LayoutInflater.from(context)

    fun    getPositionByName(name: String): Int {
        return lastPositionList.indexOfFirst { it?.positionname == name }
    }

    override fun getCount(): Int {
        return lastPositionList.size
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

        vh.label.text = lastPositionList.get(p0)?.positionname
        return view
    }

    private class ItemRowHolder(row: View?) {

        val label: TextView

        init {
            this.label = row?.findViewById(R.id.txtDropDownLabel) as TextView
        }
    }
}