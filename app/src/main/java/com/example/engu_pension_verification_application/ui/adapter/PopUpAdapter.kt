package com.example.engu_pension_verification_application.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.engu_pension_verification_application.R

class PopUpAdapter(
    val context: Context,
    private val items: List<String>,
    private val onClick:(position:Int, text:String)->Unit
) :
    BaseAdapter() {
    val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, p2: ViewGroup): View {
        val view: View
        val vh: ItemRowHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.popup_item, p2, false)
            vh = ItemRowHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }

        val params = view.layoutParams
        params.height = 80
        params.width = p2.width
        view.layoutParams = params

        vh.label.text = items[position]
        view.setOnClickListener {
            onClick.invoke(position, items[position])
        }
        return view
    }

    private class ItemRowHolder(row: View?) {
        val label: TextView = row?.findViewById(R.id.tv_item)!!
    }
}