package com.example.engu_pension_verification_application.ui.fragment.service.bank

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.model.response.BanksItem
import com.example.engu_pension_verification_application.model.response.ListBanksItem
import java.util.ArrayList

class BankAdapter(val context: Context?,val BankList: ArrayList<ListBanksItem?>): BaseAdapter()
{

    val mInflater: LayoutInflater = LayoutInflater.from(context)
    override fun getCount(): Int {
       return BankList.size
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
            view = mInflater.inflate(R.layout.sp_banklist, p2, false)
            vh = ItemRowHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }

        val params = view.layoutParams
        params.height = 80
        view.layoutParams = params

        vh.label.text = BankList.get(p0)?.name
       // vh.label_icon.text = BankList.get(p0)?.name
        return view
    }

    private class ItemRowHolder(row: View?) {

        val label: TextView
       // val label_icon: ImageView

        init {
            this.label = row?.findViewById(R.id.txtDropDownLabel) as TextView
           // this.label_icon = row?.findViewById(R.id.img_banklogo) as ImageView
        }
    }
}