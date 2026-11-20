package com.localizedlaunchericon.ui

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text
import com.localizedlaunchericon.generation.LocalizationRequest
import com.localizedlaunchericon.task.GenerateLocalizationTask
import com.localizedlaunchericon.validation.iconTextValidator
import com.localizedlaunchericon.validation.localeTextValidator
import com.localizedlaunchericon.validation.parseLocales
import java.awt.Dimension
import java.awt.Image
import java.io.File
import javax.swing.ImageIcon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.SwingConstants

/** Collects locales, icon images and the app name, then queues [GenerateLocalizationTask]. */
class CreateLocalizationDialog(
    private val project: Project,
    private val resFolder: VirtualFile,
) : DialogWrapper(project) {

    private var foregroundImage: File? = null
    private var backgroundImage: File? = null

    private lateinit var localesField: Cell<JBTextField>
    private lateinit var iconNameField: Cell<JBTextField>
    private lateinit var appNameField: Cell<JBTextField>
    private lateinit var addIconsCheckBox: Cell<JBCheckBox>
    private lateinit var addAppNameCheckBox: Cell<JBCheckBox>
    private lateinit var iconsRow: Row
    private lateinit var appNameRow: Row

    private val foregroundPreview = JLabel("No foreground image selected", SwingConstants.CENTER)
    private val backgroundPreview = JLabel("No background image selected", SwingConstants.CENTER)

    init {
        title = "Create Localization"
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Locales:") {
            localesField = textField()
                .focused()
                .align(Align.FILL)
                .comment("e.g. ru, kk-rKZ or es-rCL, es-rAR, qu-rPE")
                .validationOnApply(localeTextValidator)
        }
        row {
            addIconsCheckBox = checkBox("Add localized launcher icons")
                .applyToComponent { addActionListener { iconsRow.visible(isSelected) } }
        }
        iconsRow = row {
            panel {
                row("Icon name:") {
                    iconNameField = textField()
                        .text(DEFAULT_ICON_NAME)
                        .validationOnApply { field -> if (addIcons) iconTextValidator(this, field) else null }
                }
                row("Foreground (PNG/WebP):") {
                    cell(foregroundPreview).align(Align.FILL)
                    button("Choose…") {
                        chooseImage("Select Foreground Image")?.let {
                            foregroundImage = it
                            showPreview(foregroundPreview, it)
                        }
                    }
                }
                row("Background (PNG/WebP):") {
                    cell(backgroundPreview).align(Align.FILL)
                    button("Choose…") {
                        chooseImage("Select Background Image")?.let {
                            backgroundImage = it
                            showPreview(backgroundPreview, it)
                        }
                    }
                }
            }
        }
        row {
            addAppNameCheckBox = checkBox("Add localized app name")
                .applyToComponent { addActionListener { appNameRow.visible(isSelected) } }
        }
        appNameRow = row("App name:") {
            appNameField = textField().align(Align.FILL)
        }
        iconsRow.visible(false)
        appNameRow.visible(false)
    }

    private val addIcons: Boolean get() = addIconsCheckBox.component.isSelected
    private val addAppName: Boolean get() = addAppNameCheckBox.component.isSelected

    override fun doValidate(): ValidationInfo? = when {
        !addIcons && !addAppName -> ValidationInfo("Choose what to generate: icons, app name or both")
        addIcons && (foregroundImage == null || backgroundImage == null) ->
            ValidationInfo("Select both foreground and background images")
        addAppName && appNameField.component.text.isBlank() ->
            ValidationInfo("App name cannot be empty", appNameField.component)
        else -> null
    }

    override fun doOKAction() {
        val request = buildRequest()
        super.doOKAction()
        GenerateLocalizationTask(project, resFolder, request).queue()
    }

    private fun buildRequest() = LocalizationRequest(
        resDir = File(resFolder.path),
        locales = parseLocales(localesField.component.text),
        icons = if (addIcons) {
            LocalizationRequest.Icons(
                name = iconNameField.component.text,
                foreground = checkNotNull(foregroundImage),
                background = checkNotNull(backgroundImage),
            )
        } else {
            null
        },
        appName = appNameField.component.text.takeIf { addAppName },
    )

    private fun showPreview(label: JLabel, image: File) {
        label.preferredSize = Dimension(PREVIEW_SIZE_PX, PREVIEW_SIZE_PX)
        label.icon = ImageIcon(ImageIcon(image.path).image.getScaledInstance(PREVIEW_SIZE_PX, PREVIEW_SIZE_PX, Image.SCALE_SMOOTH))
        label.text = ""
    }

    private fun chooseImage(title: String): File? {
        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
            .withFileFilter { it.extension?.lowercase() in SUPPORTED_EXTENSIONS }
            .withTitle(title)
        return FileChooser.chooseFile(descriptor, project, null)?.let { File(it.path) }
    }

    private companion object {
        const val DEFAULT_ICON_NAME = "ic_launcher"
        const val PREVIEW_SIZE_PX = 48
        val SUPPORTED_EXTENSIONS = setOf("png", "webp", "jpg", "jpeg")
    }
}
