package com.localizedLauncherIcon

import java.io.File

object AppNameGenerator {
    fun generateLocalizedAppName(locales: String, appName: String, projectPath: String){
        val localeList = locales
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        localeList.forEach { locale ->
            val valuesDirName = if (locale.isNotEmpty()) {
                "values-$locale"
            } else {
                "values"
            }

            val valuesDir = File("$projectPath/$valuesDirName")
            if (!valuesDir.exists()) {
                valuesDir.mkdirs()
            }

            val stringsXml = File(valuesDir, "strings.xml")

            if (!stringsXml.exists()) {
                // Создаем новый файл
                stringsXml.writeText(
                    """
                    <resources>
                        <string name="app_name">$appName</string>
                    </resources>
                    """.trimIndent()
                )
                return@forEach
            }

            val currentContent = stringsXml.readText()

            if (currentContent.contains("name=\"app_name\"")) {
                val updated = currentContent.replace(
                    Regex("<string\\s+name=\"app_name\">.*?</string>"),
                    "<string name=\"app_name\">$appName</string>"
                )
                stringsXml.writeText(updated)
            } else {
                val updated = currentContent.replace(
                    "</resources>",
                    "    <string name=\"app_name\">$appName</string>\n</resources>"
                )
                stringsXml.writeText(updated)
            }
        }
    }
}