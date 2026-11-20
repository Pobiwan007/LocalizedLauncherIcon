package com.localizedlaunchericon.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.localizedlaunchericon.ui.CreateLocalizationDialog

/** "New → Create Localization", available on a module's `res` folder. */
class CreateLocalizationAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = e.project != null && file != null && file.isDirectory && file.name == "res"
    }

    override fun actionPerformed(e: AnActionEvent) {
        CreateLocalizationDialog(e).show()
    }
}
