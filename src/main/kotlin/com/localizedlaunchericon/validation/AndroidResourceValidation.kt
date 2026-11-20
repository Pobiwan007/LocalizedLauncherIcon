package com.localizedlaunchericon.validation

/** Android resource qualifier for a language with an optional region: `ru`, `fil`, `en-rUS`. */
private val LOCALE_QUALIFIER = Regex("^[a-z]{2,3}(-r[A-Z]{2})?$")

/** Android resource names: lowercase letter first, then lowercase letters, digits or `_`. */
private val RESOURCE_NAME = Regex("^[a-z][a-z0-9_]*$")

/** Splits a comma-separated list of locale qualifiers, trimming blanks and duplicates. */
fun parseLocales(input: String): List<String> =
    input.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct()

/** @return an error message, or `null` when [input] is a valid list of locale qualifiers. */
fun validateLocales(input: String): String? {
    val locales = parseLocales(input)
    return when {
        locales.isEmpty() -> "Value cannot be empty"
        locales.any { !it.matches(LOCALE_QUALIFIER) } ->
            "Invalid locale: ${locales.first { !it.matches(LOCALE_QUALIFIER) }}. " +
                "Use 'ru', 'ru-rKZ' or a comma-separated list like 'en, ru, uz-rUZ'"
        else -> null
    }
}

/** @return an error message, or `null` when [input] is a valid Android resource name. */
fun validateIconName(input: String): String? = when {
    input.isEmpty() -> "Value cannot be empty"
    !input.matches(RESOURCE_NAME) -> "Use lowercase letters, digits and '_', starting with a letter"
    else -> null
}
