package com.example.engu_pension_verification_application.util

enum class OnboardingStage(val id: Int) {
    SERVICES(0),
    ACTIVE_BASIC_DETAILS(1),
    ACTIVE_DOCUMENTS(2),
    ACTIVE_BANK_INFO(3),
    RETIREE_BASIC_DETAILS(4),
    RETIREE_DOCUMENTS(5),
    RETIREE_BANK_INFO(6),
    GOV_VERIFY(7),
    DASHBOARD(8),
    ;

    companion object {
        fun fromId(id: Int): OnboardingStage {
            return values().find { it.id == id }!!
        }
    }
}