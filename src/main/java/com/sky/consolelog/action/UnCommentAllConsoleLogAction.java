package com.sky.consolelog.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.sky.consolelog.constant.SettingConstant;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
import com.sky.consolelog.utils.ConsoleLogMsgUtil;
import com.sky.consolelog.utils.ConsoleLogPsiUtil;
import com.sky.consolelog.utils.TextRangeHandle;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 按下Alt+Shift+2快捷键注释掉全部console.log调用表达式
 *
 * @author SkySource
 * @Date: 2025/2/2 16:12
 */
public class UnCommentAllConsoleLogAction extends AnAction {

    private final ConsoleLogSettingState settings = ApplicationManager.getApplication().getService(ConsoleLogSettingState.class);

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

        Document document = editor.getDocument();
        List<TextRange> consoleLogRangeList = ConsoleLogPsiUtil.detectAllOnlyComment(psiFile);

        // 处理选中区域和console.log表达式
        List<TextRange> consoleLogNewRangeList = TextRangeHandle.handleSelectedAndConsoleLogTextRange(editor, consoleLogRangeList, settings.unCommentSelection);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            int deleteStringSize = 0;
            for (TextRange range : consoleLogNewRangeList) {
                TextRange newRange = new TextRange(range.getStartOffset() - deleteStringSize, range.getEndOffset() - deleteStringSize);
                String text = document.getText(newRange);
                Matcher matcher = pattern.matcher(text);

                while (matcher.find()) {
                    // 删除符合插件规范的console.log表达式语句
                    int startOffset = newRange.getStartOffset() - SettingConstant.COMMENT_SIGNAL_LENGTH;
                    int endOffset = newRange.getStartOffset();
                    deleteStringSize += SettingConstant.COMMENT_SIGNAL_LENGTH;

                    // 删除匹配的内容
                    document.deleteString(startOffset, endOffset);
                }
            }
//            FileDocumentManager.getInstance().saveAllDocuments();
        });
    }
}
