package com.example.engu_pension_verification_application.model.response

data class PaymentResponse(
    val requires_action: Boolean,
    val payment_intent_client_secret: String,
    val return_url: String

)
