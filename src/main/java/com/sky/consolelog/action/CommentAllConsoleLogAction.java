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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 按下Alt+Shift+1快捷键注释所有符合插件规范的console.log调用表达式
 *
 * @author SkySource
 * @Date: 2025/2/2 9:26
 */
public class CommentAllConsoleLogAction extends AnAction {

    private final ConsoleLogSettingState settings = ApplicationManager.getApplication().getService(ConsoleLogSettingState.class);

    @Override
    public void actionPerformed(AnActionEvent e) {
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
        TreeMap<TextRange, List<Integer>> consoleLogLineNumberMap = ConsoleLogPsiUtil.detectAllButSkipComment(psiFile, document);

        // 处理选中区域和console.log表达式
        Map<TextRange, List<Integer>> consoleLogNewLineNumberMap = TextRangeHandle.handleSelectedAndConsoleLogTextRange(editor, consoleLogLineNumberMap, settings.commentInSelection);

        if (!consoleLogNewLineNumberMap.isEmpty()) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                int insertCommentSignalSize = 0;
                for (Map.Entry<TextRange, List<Integer>> map : consoleLogNewLineNumberMap.entrySet()) {
                    TextRange range = map.getKey();
                    List<Integer> lineNumberList = map.getValue();
                    TextRange newRange = new TextRange(range.getStartOffset() + insertCommentSignalSize, range.getEndOffset() + insertCommentSignalSize);
                    String text = document.getText(newRange);
                    Matcher matcher = pattern.matcher(text);
                    while (matcher.find()) {
                        int firstLineStartOffset = document.getLineStartOffset(lineNumberList.get(0));
                        // 取最大的偏移，保证不会注释到console.log前面的代码（怪异代码的感觉+1）
                        document.insertString(Math.max(newRange.getStartOffset(), firstLineStartOffset), "// ");
                        insertCommentSignalSize += SettingConstant.COMMENT_SIGNAL_LENGTH;
                        for (int i = 1; i < lineNumberList.size(); ++i) {
                            int lineStartOffset = document.getLineStartOffset(lineNumberList.get(i));
                            document.insertString(lineStartOffset, "// ");
                            insertCommentSignalSize += SettingConstant.COMMENT_SIGNAL_LENGTH;
                            // 万一结尾还有非console.log的代码呢？应该没有人写这么怪异的代码，，，吧？
                        }
                    }
                }

//            FileDocumentManager.getInstance().saveAllDocuments();
            });
        }
    }
}
