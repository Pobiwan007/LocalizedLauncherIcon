package com.localizedlaunchericon.generation

import java.io.File

/** Writes `app_name` into `values-<locale>/strings.xml` for each locale. */
object AppNameGenerator {

    private val APP_NAME_ENTRY = Regex("""<string\s+name="app_name"[^>]*>.*?</string>""", RegexOption.DOT_MATCHES_ALL)

    /** @return the `strings.xml` files that were created or updated. */
    fun generate(resDir: File, locales: List<String>, appName: String): List<File> {
        val entry = """<string name="app_name">${escapeAndroidString(appName)}</string>"""

        return locales.map { locale ->
            val valuesDir = File(resDir, if (locale.isEmpty()) "values" else "values-$locale").apply { mkdirs() }
            val stringsXml = File(valuesDir, "strings.xml")

            val content = when {
                !stringsXml.exists() ->
                    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n    $entry\n</resources>\n"
                APP_NAME_ENTRY.containsMatchIn(stringsXml.readText()) ->
                    // Lambda replacement: '$' and '\' in the app name must not be treated as group references.
                    APP_NAME_ENTRY.replace(stringsXml.readText()) { entry }
                else ->
                    stringsXml.readText().replace("</resources>", "    $entry\n</resources>")
            }
            stringsXml.writeText(content)
            stringsXml
        }
    }

    /**
     * Escapes a value for an Android `<string>` resource: XML special characters,
     * plus the characters aapt treats specially (`'`, `"`, `\`, leading `@` / `?`).
     */
    fun escapeAndroidString(value: String): String {
        val escaped = buildString {
            for (ch in value) {
                when (ch) {
                    '\\' -> append("\\\\")
                    '\'' -> append("\\'")
                    '"' -> append("\\\"")
                    '&' -> append("&amp;")
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    '\n' -> append("\\n")
                    else -> append(ch)
                }
            }
        }
        return if (escaped.startsWith("@") || escaped.startsWith("?")) "\\$escaped" else escaped
    }
}
