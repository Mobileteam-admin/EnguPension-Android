package com.example.engu_pension_verification_application.ui.fragment.Dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.engu_pension_verification_application.R


import android.widget.Button
import android.widget.Toast
import com.example.engu_pension_verification_application.R
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.view.CardInputWidget

class paymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
    }
}