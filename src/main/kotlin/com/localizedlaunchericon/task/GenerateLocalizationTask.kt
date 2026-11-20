package com.localizedlaunchericon.task

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.localizedlaunchericon.generation.LocalizationGenerator
import com.localizedlaunchericon.generation.LocalizationRequest

/**
 * Runs [LocalizationGenerator] off the EDT with a progress indicator, then refreshes the VFS
 * so the new resources show up in the Project view, and reports the result as a notification.
 *
 * Cancelling the progress indicator cancels the coroutine started by [runBlockingCancellable].
 */
class GenerateLocalizationTask(
    project: Project,
    private val resFolder: VirtualFile,
    private val request: LocalizationRequest,
) : Task.Backgroundable(project, "Generating localized resources", /* canBeCancelled = */ true) {

    @Volatile
    private var writtenFiles = 0

    override fun run(indicator: ProgressIndicator) {
        indicator.isIndeterminate = true
        writtenFiles = runBlockingCancellable {
            LocalizationGenerator.generate(request) { step -> indicator.text = step }.size
        }
    }

    override fun onSuccess() {
        refreshResFolder()
        notify("Generated $writtenFiles files for ${request.locales.joinToString()}", NotificationType.INFORMATION)
    }

    override fun onCancel() {
        refreshResFolder() // files written before cancellation should still appear in the Project view
        notify("Generation cancelled", NotificationType.WARNING)
    }

    override fun onThrowable(error: Throwable) {
        thisLogger().warn("Localized resource generation failed", error)
        refreshResFolder() // some files may already have been written
        notify("Generation failed: ${error.message}", NotificationType.ERROR)
    }

    private fun refreshResFolder() {
        VfsUtil.markDirtyAndRefresh(true, true, true, resFolder)
    }

    private fun notify(content: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP_ID)
            .createNotification(content, type)
            .notify(project)
    }

    companion object {
        /** Must match the `notificationGroup` id registered in plugin.xml. */
        const val NOTIFICATION_GROUP_ID = "LocalizedLauncherIcon"
    }
}
