package com.example.engu_pension_verification_application.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.engu_pension_verification_application.R
import kotlinx.android.synthetic.main.view_calendar_label.view.cl_parent
import kotlinx.android.synthetic.main.view_calendar_label.view.tv_calendar_label


class CalendarLabelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var _isRoundBgVisible = false
    val isRoundBgVisible:Boolean
        get() = _isRoundBgVisible
    init {
        LayoutInflater.from(context).inflate(R.layout.view_calendar_label, this, true)
    }

    fun setText(text: String) {
        tv_calendar_label.text = text
    }
    fun getText() = tv_calendar_label.text.toString()

    fun setRoundBgVisibility(isRoundBgVisible: Boolean) {
        this._isRoundBgVisible = isRoundBgVisible
        tv_calendar_label.setBackgroundResource(if (isRoundBgVisible) R.drawable.calendar_round_bg else 0)
//        setTextColor(if(isRoundBgVisible) R.color.white else R.color.black)
    }

    fun setTextColor(colorResId: Int) {
        tv_calendar_label.setTextColor(ContextCompat.getColor(context, colorResId))
    }
    fun setClickListener(clickListener: OnClickListener) {
        cl_parent.setOnClickListener(clickListener)
    }
    fun fadeIn() {
        cl_parent.alpha = 0f
        cl_parent.animate().alpha(1f).setDuration(600).start()
    }
}