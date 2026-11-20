package com.localizedlaunchericon.generation

import java.io.File

/**
 * What to generate for a module's `res` folder, captured from the dialog.
 *
 * At least one of [icons] or [appName] is set.
 */
data class LocalizationRequest(
    val resDir: File,
    val locales: List<String>,
    val icons: Icons? = null,
    val appName: String? = null,
) {
    init {
        require(locales.isNotEmpty()) { "At least one locale is required" }
        require(icons != null || appName != null) { "Nothing to generate" }
    }

    /** Source images for the launcher icon set named [name] (e.g. `ic_launcher`). */
    data class Icons(val name: String, val foreground: File, val background: File)
}
