package com.localizedlaunchericon.generation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Runs every generator a [LocalizationRequest] asks for. Free of IntelliJ Platform APIs, so it is unit-testable. */
object LocalizationGenerator {

    /**
     * @param onStep receives a short description of the step that is about to run (for progress UI).
     * @return all files that were created or overwritten.
     */
    suspend fun generate(request: LocalizationRequest, onStep: (String) -> Unit = {}): List<File> {
        val written = mutableListOf<File>()

        request.icons?.let { icons ->
            onStep("Generating launcher icons…")
            val (foreground, background) = withContext(Dispatchers.IO) {
                LauncherIconGenerator.readImage(icons.foreground) to LauncherIconGenerator.readImage(icons.background)
            }
            written += LauncherIconGenerator.generate(
                resDir = request.resDir,
                iconName = icons.name,
                locales = request.locales,
                foreground = foreground,
                background = background,
            )
        }

        request.appName?.let { appName ->
            onStep("Writing app names…")
            written += withContext(Dispatchers.IO) {
                AppNameGenerator.generate(request.resDir, request.locales, appName)
            }
        }

        return written
    }
}
