package com.eahm.drinkwaterapp.core.presentation

enum class UnitPreference {
    ML,           // Always show milliliters: "250 mL"
    AUTO_LITERS,  // Auto-convert above 1000: "250 mL" / "1.5 L"
    FL_OZ,        // US fluid ounces: "8.5 fl oz"
}
