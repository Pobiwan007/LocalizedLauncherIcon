package com.localizedLauncherIcon

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class LocalizedLauncherIconAction : AnAction("Create Localization") {

    override fun actionPerformed(e: AnActionEvent) {
        LLIADialogWrapper(e).showAndGet()
    }

}
