package com.example.engu_pension_verification_application.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.engu_pension_verification_application.R
import com.example.engu_pension_verification_application.databinding.ViewCalendarLabelBinding


class CalendarLabelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding = ViewCalendarLabelBinding.inflate(LayoutInflater.from(context),this, true)

    private var _isRoundBgVisible = false
    val isRoundBgVisible:Boolean
        get() = _isRoundBgVisible

    fun setText(text: String) {
        binding.tvCalendarLabel.text = text
    }
    fun getText() = binding.tvCalendarLabel.text.toString()

    fun setRoundBgVisibility(isRoundBgVisible: Boolean) {
        this._isRoundBgVisible = isRoundBgVisible
        binding.tvCalendarLabel.setBackgroundResource(if (isRoundBgVisible) R.drawable.calendar_round_bg else 0)
//        setTextColor(if(isRoundBgVisible) R.color.white else R.color.black)
    }

    fun setTextColor(colorResId: Int) {
        binding.tvCalendarLabel.setTextColor(ContextCompat.getColor(context, colorResId))
    }
    fun setClickListener(clickListener: OnClickListener) {
        binding.clParent.setOnClickListener(clickListener)
    }
    fun fadeIn() {
        binding.clParent.alpha = 0f
        binding.clParent.animate().alpha(1f).setDuration(600).start()
    }
}