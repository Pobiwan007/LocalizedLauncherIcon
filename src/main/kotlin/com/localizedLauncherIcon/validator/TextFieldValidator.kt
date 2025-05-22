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
    when{
        it.text.isNullOrEmpty() -> ValidationInfo("Value cannot be empty")
        !it.text.matches(Regex("^[a-z]{2}(-r[A-Z]{2})?$")) ->
            ValidationInfo("Invalid locale format. Use format like 'ru' or 'ru-rKZ'")
        else -> null
    }
}
