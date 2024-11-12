package com.example.engu_pension_verification_application.commons

import android.view.View
import android.widget.TextView

fun setDocumentView(url: String?, buttonView: View, cardView: View, filenameView: TextView, closeButtonView: View, uploadProgressView: View, percentageView: View, progressBar: View) {
    //if (!url.isNullOrEmpty()) {
        buttonView.visibility = View.VISIBLE
        cardView.visibility = View.VISIBLE
        filenameView.text = url?.substringAfterLast('/')
        closeButtonView.visibility = View.VISIBLE
        uploadProgressView.visibility = View.GONE
        percentageView.visibility = View.GONE
        progressBar.visibility = View.GONE
    //}
}

fun setDocumentViewIfPresent(url: String?, buttonView: View, cardView: View, filenameView: TextView, closeButtonView: View, uploadProgressView: View, percentageView: View, progressBar: View) {
    if (!url.isNullOrEmpty()) {
        setDocumentView(url, buttonView, cardView, filenameView, closeButtonView, uploadProgressView, percentageView, progressBar)
    }
}