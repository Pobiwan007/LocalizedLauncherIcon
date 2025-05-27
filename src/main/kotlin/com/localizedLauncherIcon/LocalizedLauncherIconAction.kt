package com.localizedLauncherIcon

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class LocalizedLauncherIconAction : AnAction("Add Localized Launcher Icon") {

    override fun actionPerformed(e: AnActionEvent) {
        LLIADialogWrapper(e).showAndGet()
    }

}
