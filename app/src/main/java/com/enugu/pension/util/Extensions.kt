package com.enugu.pension.util

fun String.isValidNumber() = this.toFloatOrNull() != null
fun String.capitalizeFirstLetter(): String {
    return if (this.isEmpty()) {
        this
    } else {
        this.substring(0, 1).uppercase() + this.substring(1).lowercase()
    }
}