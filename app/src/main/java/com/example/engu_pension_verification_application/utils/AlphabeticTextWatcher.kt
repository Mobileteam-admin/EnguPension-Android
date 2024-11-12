package com.example.engu_pension_verification_application.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class AlphabeticTextWatcher(private val editText: EditText) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // Not needed for this implementation
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Not needed for this implementation
    }

    override fun afterTextChanged(s: Editable?) {
        val inputText = s.toString()
        val filteredText = inputText.filter { it.isLetter() || it.isWhitespace() }
        if (inputText != filteredText) {
            editText.setText(filteredText)
            editText.setSelection(filteredText.length)
        }
    }
}