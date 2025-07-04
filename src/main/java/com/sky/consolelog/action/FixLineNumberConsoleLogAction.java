package com.sky.consolelog.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
import com.sky.consolelog.utils.WriterCoroutineUtils;
import org.jetbrains.annotations.NotNull;

/**
 * 无默认快捷键 修正每个${lineNumber}对应的值
 *
 * @author SkySource
 * @Date: 2025/7/3 22:58
 */
public class FixLineNumberConsoleLogAction extends AnAction {

    private final ConsoleLogSettingState settings = ApplicationManager.getApplication().getService(ConsoleLogSettingState.class);
    private final WriterCoroutineUtils writerCoroutineUtils = ApplicationManager.getApplication().getService(WriterCoroutineUtils.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        // 获取当前文件信息的对象
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        Project project = e.getProject();
        if (project == null || editor == null || psiFile == null) {
            return;
        }

        writerCoroutineUtils.updateLineNumber(settings, project, editor, psiFile);
    }
}
