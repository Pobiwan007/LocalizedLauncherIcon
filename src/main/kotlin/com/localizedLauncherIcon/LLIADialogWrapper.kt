package com.localizedLauncherIcon

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.localizedLauncherIcon.validator.iconTextValidator
import com.localizedLauncherIcon.validator.localeTextValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Dimension
import java.awt.Image
import javax.swing.*

class LLIADialogWrapper(action: AnActionEvent) : DialogWrapper(action.project) {
    private lateinit var fgIcon: String
    private lateinit var bgIcon: String
    private lateinit var iconNameTextField: Cell<JBTextField>
    private lateinit var localeCodeTextField: Cell<JBTextField>
    private lateinit var localizedNameTextField: Cell<JBTextField>
    private lateinit var localizedNameRow: Row
    private lateinit var localizedIconRow: Row

    private val fgIconLabel = JLabel("No foreground icon selected")
    private val bgIconLabel = JLabel("No background icon selected")
    private lateinit var addLocalizedNamesCheckBox: Cell<JBCheckBox>
    private lateinit var addLocalizedIconsCheckBox: Cell<JBCheckBox>

    init {
        title = "Create Localization"
        super.init()
    }

    private val selectedFolder = PlatformDataKeys.VIRTUAL_FILE.getData(action.dataContext)

    fun getIconName(): String = iconNameTextField.component.text
    fun getLocaleCodes(): String = localeCodeTextField.component.text
    fun getLocalizedName(): String = localizedNameTextField.component.text

    override fun createCenterPanel(): JComponent {
        fgIconLabel.horizontalAlignment = SwingConstants.CENTER
        bgIconLabel.horizontalAlignment = SwingConstants.CENTER

        println("PathToSelectedFolder: ".plus(selectedFolder?.path))
        val panel = panel {
            if (!selectedFolder?.name.equals("res")) row { label("Selected folder isn`t resource") }
            row { label("Add locales") }
            row {
                localeCodeTextField = textField()
                    .focused()
                    .validationOnApply(localeTextValidator)
            }
            row {
                addLocalizedIconsCheckBox = checkBox("Also add localized icons")
                    .applyToComponent {
                        addActionListener {
                            val visible = isSelected
                            localizedIconRow.visible(visible)
                        }
                    }
            }
            localizedIconRow = row {
                panel {
                    row { label("Icon name") }
                    row {
                        iconNameTextField = textField()
                            .text("ic_launcher")
                            .validationOnApply(iconTextValidator)
                    }

                    row { label("Select foreground icon (PNG)") }

                    row {
                        cell(fgIconLabel).align(Align.FILL)
                        button("Choose…") {
                            val file = chooseImageFile("Select Foreground Icon")
                            file?.let {
                                fgIcon = it.path
                                showSelectedImagePreview(fgIconLabel, it.path)
                            }
                        }
                    }
                    row { label("Select background icon (PNG/WebP)") }
                    row {
                        cell(bgIconLabel).align(Align.FILL)
                        button("Choose…") {
                            val file = chooseImageFile("Select Background Icon")
                            file?.let {
                                bgIcon = it.path
                                showSelectedImagePreview(bgIconLabel, it.path)
                            }
                        }
                    }
                }
            }
            row {
                addLocalizedNamesCheckBox = checkBox("Also add localized appname")
                    .applyToComponent {
                        addActionListener {
                            val visible = isSelected
                            localizedNameRow.visible(visible)
                        }
                    }
            }

            // Hidden text field (display names for desktop)
            localizedNameRow = row {
                label("AppName value")
                localizedNameTextField = textField()
            }
            localizedIconRow.visible(false)
            localizedNameRow.visible(false)
        }
        return panel
    }

    private fun showSelectedImagePreview(jLabel: JLabel, path: String) {
        jLabel.preferredSize = Dimension(48, 48)
        jLabel.icon = ImageIcon(ImageIcon(path).image.getScaledInstance(48, 48, Image.SCALE_SMOOTH))
        jLabel.text = ""
    }

    private fun chooseImageFile(title: String): VirtualFile? {
        val fileChooserDescriptor = FileChooserDescriptor(
            true, false, false,
            false, false, false
        )
            .withFileFilter { it.extension in listOf("png", "webp", "svg") }
            .withTitle(title)
        return FileChooser.chooseFile(fileChooserDescriptor, null, null)
    }

    override fun doOKAction() {

        CoroutineScope(Dispatchers.Default).launch {
            val projectPath = selectedFolder?.path ?: ""
            if (addLocalizedIconsCheckBox.component.isSelected) {
                if (fgIcon.isNotEmpty() && bgIcon.isNotEmpty()) {
                    LauncherGenerator.generateLauncherIcons(
                        projectBasePath = projectPath,
                        localeCode = getLocaleCodes(),
                        iconName = getIconName(),
                        fgIconPath = fgIcon,
                        bgIconPath = bgIcon
                    )
                }
            }
            if (addLocalizedNamesCheckBox.component.isSelected) {
                AppNameGenerator.generateLocalizedAppName(
                    appName = getLocalizedName(),
                    locales = getLocaleCodes(),
                    projectPath = projectPath
                )
            }
            withContext(Dispatchers.Main) {
                ApplicationManager.getApplication().invokeLater {
                    LocalFileSystem.getInstance()
                        .refreshAndFindFileByPath(projectPath)?.refresh(true, true)
                }
            }
        }
        super.doOKAction()
    }

}