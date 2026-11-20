package com.localizedlaunchericon.ui

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text
import com.localizedlaunchericon.generation.AppNameGenerator
import com.localizedlaunchericon.generation.LauncherIconGenerator
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

class CreateLocalizationDialog(action: AnActionEvent) : DialogWrapper(action.project) {

    private val project: Project? = action.project
    private val resFolder: VirtualFile? = action.getData(CommonDataKeys.VIRTUAL_FILE)

    private var fgIconPath: String? = null
    private var bgIconPath: String? = null

    private lateinit var localeCodeTextField: Cell<JBTextField>
    private lateinit var iconNameTextField: Cell<JBTextField>
    private lateinit var localizedNameTextField: Cell<JBTextField>
    private lateinit var addLocalizedIconsCheckBox: Cell<JBCheckBox>
    private lateinit var addLocalizedNamesCheckBox: Cell<JBCheckBox>
    private lateinit var localizedIconRow: Row
    private lateinit var localizedNameRow: Row

    private val fgIconLabel = JLabel("No foreground icon selected", SwingConstants.CENTER)
    private val bgIconLabel = JLabel("No background icon selected", SwingConstants.CENTER)

    init {
        title = "Create Localization"
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        if (resFolder?.name != "res") {
            row { label("Selected folder isn't a resource folder") }
        }
        row("Locales:") {
            localeCodeTextField = textField()
                .focused()
                .align(Align.FILL)
                .comment("e.g. ru, kk-rKZ or es-rCL, es-rAR, qu-rPE")
                .validationOnApply(localeTextValidator)
        }
        row {
            addLocalizedIconsCheckBox = checkBox("Add localized launcher icons")
                .applyToComponent { addActionListener { localizedIconRow.visible(isSelected) } }
        }
        localizedIconRow = row {
            panel {
                row("Icon name:") {
                    iconNameTextField = textField()
                        .text("ic_launcher")
                        .validationOnApply { if (addLocalizedIconsCheckBox.component.isSelected) iconTextValidator(this, it) else null }
                }
                row("Foreground (PNG/WebP):") {
                    cell(fgIconLabel).align(Align.FILL)
                    button("Choose…") {
                        chooseImageFile("Select Foreground Icon")?.let {
                            fgIconPath = it.path
                            showPreview(fgIconLabel, it.path)
                        }
                    }
                }
                row("Background (PNG/WebP):") {
                    cell(bgIconLabel).align(Align.FILL)
                    button("Choose…") {
                        chooseImageFile("Select Background Icon")?.let {
                            bgIconPath = it.path
                            showPreview(bgIconLabel, it.path)
                        }
                    }
                }
            }
        }
        row {
            addLocalizedNamesCheckBox = checkBox("Add localized app name")
                .applyToComponent { addActionListener { localizedNameRow.visible(isSelected) } }
        }
        localizedNameRow = row("App name:") {
            localizedNameTextField = textField().align(Align.FILL)
        }
        localizedIconRow.visible(false)
        localizedNameRow.visible(false)
    }

    override fun doValidate(): ValidationInfo? {
        val addIcons = addLocalizedIconsCheckBox.component.isSelected
        val addName = addLocalizedNamesCheckBox.component.isSelected
        return when {
            resFolder == null -> ValidationInfo("Select a res folder in the Project view")
            !addIcons && !addName -> ValidationInfo("Choose what to generate: icons, app name or both")
            addIcons && (fgIconPath == null || bgIconPath == null) ->
                ValidationInfo("Select both foreground and background images")
            addName && localizedNameTextField.component.text.isBlank() ->
                ValidationInfo("App name cannot be empty", localizedNameTextField.component)
            else -> null
        }
    }

    override fun doOKAction() {
        val request = GenerationRequest(
            resDir = File(requireNotNull(resFolder).path),
            locales = localeCodeTextField.component.text,
            iconName = iconNameTextField.component.text,
            fgIconPath = fgIconPath.takeIf { addLocalizedIconsCheckBox.component.isSelected },
            bgIconPath = bgIconPath.takeIf { addLocalizedIconsCheckBox.component.isSelected },
            appName = localizedNameTextField.component.text.takeIf { addLocalizedNamesCheckBox.component.isSelected }
        )
        super.doOKAction()
        GenerationTask(project, resFolder, request).queue()
    }

    private fun showPreview(label: JLabel, path: String) {
        label.preferredSize = Dimension(48, 48)
        label.icon = ImageIcon(ImageIcon(path).image.getScaledInstance(48, 48, Image.SCALE_SMOOTH))
        label.text = ""
    }

    private fun chooseImageFile(title: String): VirtualFile? {
        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
            .withFileFilter { it.extension?.lowercase() in SUPPORTED_EXTENSIONS }
            .withTitle(title)
        return FileChooser.chooseFile(descriptor, project, null)
    }

    private data class GenerationRequest(
        val resDir: File,
        val locales: String,
        val iconName: String,
        val fgIconPath: String?,
        val bgIconPath: String?,
        val appName: String?
    )

    /** Runs generation off the EDT with a progress indicator, then refreshes the VFS and reports the result. */
    private class GenerationTask(
        project: Project?,
        private val resFolder: VirtualFile,
        private val request: GenerationRequest
    ) : Task.Backgroundable(project, "Generating localized resources", false) {

        private var writtenFiles = 0

        override fun run(indicator: ProgressIndicator) {
            indicator.isIndeterminate = true
            runBlockingCancellable {
                if (request.fgIconPath != null && request.bgIconPath != null) {
                    indicator.text = "Generating launcher icons…"
                    writtenFiles += LauncherIconGenerator.generateLauncherIcons(
                        projectBasePath = request.resDir.path,
                        iconName = request.iconName,
                        localeCode = request.locales,
                        fgIconPath = request.fgIconPath,
                        bgIconPath = request.bgIconPath
                    ).size
                }
            }
            if (request.appName != null) {
                indicator.text = "Writing app names…"
                writtenFiles += AppNameGenerator.generateLocalizedAppName(
                    locales = request.locales,
                    appName = request.appName,
                    projectPath = request.resDir.path
                ).size
            }
        }

        override fun onSuccess() {
            VfsUtil.markDirtyAndRefresh(true, true, true, resFolder)
            notify(
                "Generated $writtenFiles files for ${parseLocales(request.locales).joinToString()}",
                NotificationType.INFORMATION
            )
        }

        override fun onThrowable(error: Throwable) {
            VfsUtil.markDirtyAndRefresh(true, true, true, resFolder)
            notify("Generation failed: ${error.message}", NotificationType.ERROR)
        }

        private fun notify(content: String, type: NotificationType) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("LocalizedLauncherIcon")
                .createNotification(content, type)
                .notify(project)
        }
    }

    private companion object {
        val SUPPORTED_EXTENSIONS = setOf("png", "webp", "jpg", "jpeg")
    }
}
