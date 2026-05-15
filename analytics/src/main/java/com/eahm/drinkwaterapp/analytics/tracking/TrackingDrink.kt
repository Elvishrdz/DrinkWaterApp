package com.eahm.drinkwaterapp.analytics.tracking

interface TrackingDrink {

    /**
     * Recorded each time the user drinks.
     *
     * @param source     Where the action originated. Use the SOURCE_* constants below.
     * @param amountMl   The volume consumed in milliliters.
     */
    fun trackDrinkRecorded(source: String, amountMl: Float)

    /**
     * Recorded when the user creates a new preset.
     * Use [totalPresets] to understand how large a user's preset library grows over time.
     *
     * @param amountMl    The preset amount in milliliters.
     * @param totalPresets Number of presets after this creation.
     */
    fun trackPresetCreated(amountMl: Float, totalPresets: Int)

    /**
     * Recorded each time the user opens the preset bottom sheet.
     * Frequency indicates how useful the sheet is; [presetCount] shows whether
     * users with more presets open it more often.
     *
     * @param presetCount Number of presets the user has when the sheet opens.
     */
    fun trackPresetBottomSheetOpened(presetCount: Int)

    companion object {
        const val EVENT_DRINK_RECORDED = "drink_recorded"
        const val EVENT_PRESET_CREATED = "preset_created"
        const val EVENT_PRESET_BOTTOM_SHEET_OPENED = "preset_bottom_sheet_opened"

        /** Quick-preset chip on the main drink screen. */
        const val SOURCE_QUICK_SUGGESTION = "quick_suggestion"

        /** Preset chip inside the bottom sheet grid. */
        const val SOURCE_BOTTOM_SHEET_SUGGESTION = "bottom_sheet_suggestion"

        /** Custom-amount "Drink" button inside the bottom sheet. */
        const val SOURCE_BOTTOM_SHEET_CUSTOM = "bottom_sheet_custom"
    }
}
