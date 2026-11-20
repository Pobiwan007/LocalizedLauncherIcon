package com.localizedlaunchericon.validator

import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.ValidationInfoBuilder

val iconTextValidator: ValidationInfoBuilder.(JBTextField) -> ValidationInfo? = { field ->
    validateIconName(field.text.orEmpty())?.let { ValidationInfo(it, field) }
}

val localeTextValidator: ValidationInfoBuilder.(JBTextField) -> ValidationInfo? = { field ->
    validateLocales(field.text.orEmpty())?.let { ValidationInfo(it, field) }
}
