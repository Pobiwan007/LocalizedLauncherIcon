package com.localizedlaunchericon.validation

/** Cross-field problems of the Create Localization form. Field formats are checked separately. */
enum class SelectionError(val message: String) {
    NOTHING_SELECTED("Choose what to generate: icons, app name or both"),
    IMAGES_MISSING("Select both foreground and background images"),
    APP_NAME_EMPTY("App name cannot be empty"),
}

/** @return the first problem with the user's choices, or `null` when the form can be submitted. */
fun validateSelection(
    addIcons: Boolean,
    hasForeground: Boolean,
    hasBackground: Boolean,
    addAppName: Boolean,
    appName: String,
): SelectionError? = when {
    !addIcons && !addAppName -> SelectionError.NOTHING_SELECTED
    addIcons && !(hasForeground && hasBackground) -> SelectionError.IMAGES_MISSING
    addAppName && appName.isBlank() -> SelectionError.APP_NAME_EMPTY
    else -> null
}
