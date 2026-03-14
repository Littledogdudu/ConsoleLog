package com.sky.consolelog.start

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.sky.consolelog.utils.MessageUtils

class UpdateNotificationActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        // 1. 获取当前插件版本
        val pluginId = PluginId.getId("com.sky.consolelog");
        val currentVersion = PluginManagerCore.getPlugin(pluginId)?.version ?: return;

        // 2. 获取上次记录的版本号 (存储在 IDE 的持久化配置中)
        val properties = PropertiesComponent.getInstance();
        val lastVersion = properties.getValue("com.sky.consolelog.last_version");

        // 3. 如果版本不一致，说明是新安装或更新了
        if (currentVersion != lastVersion) {
            showUpdateNotification(project, currentVersion);
            
            // 4. 更新持久化存储的版本号，确保下次启动不再弹出
            properties.setValue("com.sky.consolelog.last_version", currentVersion);
        }
    }

    private fun showUpdateNotification(project: Project, version: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("ConsoleLogPluginNotificationGroup")
            .createNotification(
                "${MessageUtils.message("start.updateTitle")}(v${version})",
                MessageUtils.message("start.updateContent"),
                NotificationType.INFORMATION
            )
            .notify(project);
    }
}