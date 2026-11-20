package com.localizedlaunchericon.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.localizedlaunchericon.ui.CreateLocalizationDialog

/** "New → Create Localization", available on a module's `res` folder. */
class CreateLocalizationAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null && e.selectedResFolder() != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val resFolder = e.selectedResFolder() ?: return
        CreateLocalizationDialog(project, resFolder).show()
    }

    private fun AnActionEvent.selectedResFolder(): VirtualFile? =
        getData(CommonDataKeys.VIRTUAL_FILE)?.takeIf { it.isDirectory && it.name == RES_FOLDER_NAME }

    private companion object {
        const val RES_FOLDER_NAME = "res"
    }
}
