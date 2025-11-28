package com.localizedLauncherIcon.validator

import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.ValidationInfoBuilder

val iconTextValidator:ValidationInfoBuilder.(JBTextField) -> ValidationInfo? = {
    when {
        it.text.isNullOrEmpty() -> ValidationInfo("Value cannot be empty")
        !it.text.matches(Regex("^[a-z][a-z0-9_]*\$")) -> ValidationInfo("Incorrect icon name format")
        else -> null
    }
}
val localeTextValidator: ValidationInfoBuilder.(JBTextField) -> ValidationInfo? = {
    val singleLocaleRegex = "^[a-z]{2}(-r[A-Z]{2})?$"
    val listRegex = "^([a-z]{2}(-r[A-Z]{2})?)(\\s*,\\s*[a-z]{2}(-r[A-Z]{2})?)*$"

    when {
        it.text.isNullOrEmpty() ->
            ValidationInfo("Value cannot be empty")

        !it.text.matches(Regex(singleLocaleRegex)) &&
                !it.text.matches(Regex(listRegex)) ->
            ValidationInfo("Invalid locale format. Use 'ru', 'ru-rKZ' or comma-separated list like 'en,ru,uz-rUZ'")

        else -> null
    }
}
