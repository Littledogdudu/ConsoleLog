package com.sky.consolelog.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
import com.sky.consolelog.utils.ConsoleLogMsgUtil;
import com.sky.consolelog.utils.ConsoleLogPsiUtil;
import com.sky.consolelog.utils.TextRangeHandle;
import com.sky.consolelog.utils.WriterCoroutineUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 按下Alt+2快捷键删除所有符合插件规范的console.log调用表达式
 *
 * @author SkySource
 * @Date: 2025/1/29 18:02
 */
public class DeleteAllConsoleLogAction extends AnAction {

    private final ConsoleLogSettingState settings = ApplicationManager.getApplication().getService(ConsoleLogSettingState.class);
    private final WriterCoroutineUtils writerCoroutineUtils = ApplicationManager.getApplication().getService(WriterCoroutineUtils.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        Project project = e.getProject();
        if (project == null || editor == null) {
            return;
        }
        // 获取当前文件的 PSI 文件对象
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null) {
            return;
        }

        // 插件生成的命令的长度
        String regexConsoleLogMsg = ConsoleLogMsgUtil.buildRegexConsoleLogMsg(settings);
        if (regexConsoleLogMsg == null || regexConsoleLogMsg.isEmpty()) {
            return;
        }

        Pattern pattern = Pattern.compile(regexConsoleLogMsg);

        // 没有可打印变量时默认插入语句正则对象
        Pattern patternDefaultRegex = null;
        if (settings.enableDefaultConsoleLogMsg) {
            String defaultRegexConsoleLogMsg = ConsoleLogMsgUtil.buildRegexDefaultConsoleLogMsg(settings);
            if (StringUtils.isNotEmpty(defaultRegexConsoleLogMsg)) {
                patternDefaultRegex = Pattern.compile(defaultRegexConsoleLogMsg);
            }
        }

        // 以后考虑一下当前所在文件代码行数过多导致的性能问题吗？
        Document document = editor.getDocument();
        List<TextRange> consoleLogRangeList = ConsoleLogPsiUtil.detectAll(psiFile, document);

        // 处理选中区域和console.log表达式
        List<TextRange> consoleLogNewRangeList = TextRangeHandle.handleSelectedAndConsoleLogTextRange(editor, consoleLogRangeList, settings.deleteInSelection);

        writerCoroutineUtils.deleteWriter(project, editor, consoleLogNewRangeList, pattern, patternDefaultRegex);
    }
}
