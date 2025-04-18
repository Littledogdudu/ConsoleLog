package com.sky.consolelog.search;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.sky.consolelog.search.ui.ConsoleLogToolWindowComponent;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
import org.jetbrains.annotations.NotNull;

/**
 * 侧边栏
 *
 * @author SkySource
 * @Date: 2025/3/20 15:22
 */
public class ConsoleLogToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ConsoleLogSettingState settings = ApplicationManager.getApplication().getService(ConsoleLogSettingState.class);
        if (settings.enableSideWindow) {
            ConsoleLogToolWindowComponent consoleLogToolWindow = new ConsoleLogToolWindowComponent(project);
            ContentFactory contentFactory = ApplicationManager.getApplication().getService(ContentFactory.class);
            Content content = contentFactory.createContent(consoleLogToolWindow.getComponent(), null, false);
            toolWindow.getContentManager().addContent(content);
        } else {
            toolWindow.getContentManager().removeAllContents(true);
            toolWindow.remove();
        }
    }
}
