package com.example.engu_pension_verification_application.model.input

data class InputPayment(
    val cvc: String? = null,
    val amount: Any? = null,
    val card_number: String? = null,
    val user_id: Int? = null,
    val bank_id: Int? = null,
    val name: String? = null,
    val exp_month: Int? = null,
    val description: String? = null,
    val currency: String? = null,
    val billing_address: BillingAddress? = null,
    val exp_year: Int? = null,
    val shipping_address: ShippingAddress? = null
)
data class BillingAddress(
    val country: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postal_code: String? = null,
    val line1: String? = null
)

data class ShippingAddress(
    val country: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postal_code: String? = null,
    val line1: String? = null
)
