package com.localizedLauncherIcon

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooser

class LocalizedLauncherIconAction : AnAction("Add Localized Launcher Icon") {

    override fun actionPerformed(e: AnActionEvent) {
        val dialog = LLIADialogWrapper(e)
        if(dialog.showAndGet()){

        }
    }

}
