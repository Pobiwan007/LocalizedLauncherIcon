package com.localizedlaunchericon.generation

import java.io.File

/** Writes `app_name` into `values-<locale>/strings.xml` for each locale. */
object AppNameGenerator {

    private val APP_NAME_ENTRY = Regex("""<string\s+name="app_name"[^>]*>.*?</string>""", RegexOption.DOT_MATCHES_ALL)

    private const val RESOURCES_END_TAG = "</resources>"

    /**
     * Creates `values-<locale>/strings.xml` or upserts its `app_name` entry, keeping all other strings.
     *
     * @return the `strings.xml` files that were created or updated.
     * @throws IllegalStateException if an existing `strings.xml` has no closing `</resources>` tag.
     */
    fun generate(resDir: File, locales: List<String>, appName: String): List<File> {
        val entry = """<string name="app_name">${escapeAndroidString(appName)}</string>"""
        return locales.map { locale ->
            val stringsXml = File(resDir, "values-$locale/strings.xml")
            stringsXml.parentFile.mkdirs()
            stringsXml.writeText(upsertAppName(stringsXml.takeIf { it.exists() }?.readText(), entry, stringsXml))
            stringsXml
        }
    }

    private fun upsertAppName(existing: String?, entry: String, file: File): String = when {
        existing == null -> "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n    $entry\n$RESOURCES_END_TAG\n"
        // Lambda replacement: '$' and '\' in the app name must not be treated as group references.
        APP_NAME_ENTRY.containsMatchIn(existing) -> APP_NAME_ENTRY.replace(existing) { entry }
        else -> {
            val end = existing.lastIndexOf(RESOURCES_END_TAG)
            check(end >= 0) { "${file.path} has no $RESOURCES_END_TAG tag; app_name was not added" }
            existing.replaceRange(end, end, "    $entry\n")
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
