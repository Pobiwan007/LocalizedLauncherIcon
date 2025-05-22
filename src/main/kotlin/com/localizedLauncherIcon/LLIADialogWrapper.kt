package com.localizedLauncherIcon

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text
import com.localizedLauncherIcon.validator.iconTextValidator
import com.localizedLauncherIcon.validator.localeTextValidator
import java.awt.Dimension
import javax.swing.JComponent

class LLIADialogWrapper(private val action: AnActionEvent) : DialogWrapper(action.project) {
    private lateinit var fgIcon: String
    private lateinit var bgIcon: String
    private lateinit var iconNameTextField: Cell<JBTextField>
    private lateinit var localeCodeTextField: Cell<JBTextField>

    init {
        title = "Create Localized Launcher Icon"
        super.init()
        window.setSize(900, 500)
    }

    fun getIconName(): String = iconNameTextField.component.text
    fun getLocaleCode(): String = localeCodeTextField.component.text

    override fun createCenterPanel(): JComponent {
        val selectedFolder = PlatformDataKeys.VIRTUAL_FILE.getData(action.dataContext)
        println("PathToSelectedFolder: ".plus(selectedFolder?.path))
        val panel = panel {
            if (!selectedFolder?.name.equals("res")) row {  label ("Selected folder isn`t resource") }
            row { label("Icon name") }
            row {
                iconNameTextField = textField()
                    .focused()
                    .text("ic_launcher")
                    .validationOnInput(iconTextValidator)
            }
            row { label("Add locale code to icon") }
            row {
                localeCodeTextField = textField()
                    .focused()
                    .validationOnInput(localeTextValidator)
            }

        }
        return panel
    }
}