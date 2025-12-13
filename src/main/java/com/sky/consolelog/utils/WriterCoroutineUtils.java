package com.sky.consolelog.utils;

import com.intellij.application.options.CodeStyle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.sky.consolelog.constant.SettingConstant;
import com.sky.consolelog.entities.ScopeOffset;
import com.sky.consolelog.entities.UpdateEntity;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本写入异步工具类
 *
 * @author SkySource
 * @Date: 2025/6/30 18:53
 */
@Service(Service.Level.APP)
public final class WriterCoroutineUtils {
    /**
     * 插入console.log表达式信息
     */
    public void insertWriter(
            Project project,
            Editor editor,
            PsiFile psiFile,
            Caret caret,
            ScopeOffset scopeOffset,
            String consoleLogMsg,
            boolean autoFollowEnd
    ) {
        Document document = editor.getDocument();

        // 找到光标所在行的结束位置
        int lineNumber = document.getLineNumber(scopeOffset.getInsertEndOffset());
        int lineStartOffset = document.getLineStartOffset(lineNumber);
        int lineEndOffset = document.getLineEndOffset(lineNumber);

        // 获取光标所在行的内容，并计算缩进
        String currentLine = document.getText(TextRange.create(lineStartOffset, lineEndOffset));
        String indentation = currentLine.replace(currentLine.trim(), "");

        CodeStyleSettings currentSettings = CodeStyle.getSettings(project);
        CommonCodeStyleSettings languageSettings = currentSettings.getCommonSettings(psiFile.getLanguage());
        int tabSize = languageSettings.getIndentOptions() != null ?
                languageSettings.getIndentOptions().TAB_SIZE : 2;

        // 插入代码前添加适当的缩进
        if (scopeOffset.getNeedTab()) {
            indentation += " ".repeat(tabSize);
        }
        String indentedCode = indentation + consoleLogMsg;

        int offset;
        // 在光标所在行的结束位置插入 console.log 语句
        if (scopeOffset.getDefault()) {
            offset = lineEndOffset + 1 + indentedCode.length();
            WriteCommandAction.runWriteCommandAction(project, () -> {
                document.insertString(lineEndOffset + 1, indentedCode + "\n");
                // 将光标移动到新插入的 console.log 语句后
                if (autoFollowEnd) {
                    caret.moveToOffset(offset);
                }
            });
        } else {
            StringBuilder sentence = new StringBuilder();
            if (scopeOffset.getNeedBegLine()) {
                sentence.append("\n").append(indentedCode);
                offset = scopeOffset.getInsertEndOffset() + 1 + indentedCode.length();
            } else {
                sentence.append(" ".repeat(tabSize)).append(consoleLogMsg);
                offset = scopeOffset.getInsertEndOffset() + tabSize + consoleLogMsg.length();
            }

            if (scopeOffset.getNeedEndLine()) {
                String ch = document.getText(TextRange.create(scopeOffset.getInsertEndOffset(),
                        scopeOffset.getInsertEndOffset() + 1));
                if (!"\n".equals(ch)) {
                    // 插入的语句后面不是换行符，包含了代码，那么就在插入语句后面换行
                    sentence.append("\n").append(indentation);
                }
            }
            WriteCommandAction.runWriteCommandAction(project, () -> {
                document.insertString(scopeOffset.getInsertEndOffset(), sentence.toString());
                // 更新 PSI 树以反应文档变化
                PsiDocumentManager.getInstance(project).commitDocument(document);
                // 将光标移动到新插入的 console.log 语句后
                if (autoFollowEnd) {
                    caret.moveToOffset(offset);
                }
            });
        }
    }

    /**
     * 未选中文本时默认插入console.log表达式信息
     */
    public void insertDefaultWriter(
            Project project,
            Editor editor,
            PsiFile psiFile,
            Caret caret,
            ScopeOffset scopeOffset,
            String consoleLogMsg,
            boolean autoFollowEnd
    ) {
        Document document = editor.getDocument();

        // 找到光标所在行的结束位置
        int lineNumber = document.getLineNumber(scopeOffset.getInsertEndOffset());
        int lineStartOffset = document.getLineStartOffset(lineNumber);
        int lineEndOffset = document.getLineEndOffset(lineNumber);

        String nextText = document.getText(TextRange.create(scopeOffset.getInsertEndOffset(), lineEndOffset));
        scopeOffset.setNeedEndLine(!StringUtils.isAllBlank(nextText));

        // 获取光标所在行的内容，并计算缩进
        String currentLine = document.getText().substring(lineStartOffset, lineEndOffset);
        String indentation = currentLine.replace(currentLine.trim(), "");

        WriteCommandAction.runWriteCommandAction(project, () -> {
            int lineNumberCopy = document.getLineNumber(scopeOffset.getInsertEndOffset());
            // 在光标所在行的结束位置插入 console.log 语句
            StringBuilder sentence = new StringBuilder();
            int offset;
            if (scopeOffset.getNeedBegLine()) {
                sentence.append("\n").append(consoleLogMsg);
                offset = 1 + scopeOffset.getInsertEndOffset() + consoleLogMsg.length();
                lineNumberCopy++;
            } else {
                sentence.append(consoleLogMsg);
                offset = scopeOffset.getInsertEndOffset() + consoleLogMsg.length();
            }

            if (scopeOffset.getNeedEndLine()) {
                String ch = document.getText().substring(scopeOffset.getInsertEndOffset(),
                        Math.min(scopeOffset.getInsertEndOffset() + 1, document.getTextLength()));
                if (!"\n".equals(ch)) {
                    sentence.append("\n").append(indentation);
                }
            }

            document.insertString(scopeOffset.getInsertEndOffset(), sentence.toString());
            // 格式化
            CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
            codeStyleManager.reformatText(psiFile, scopeOffset.getInsertEndOffset(), offset);
            // 更新 PSI 树以反应文档变化
            PsiDocumentManager.getInstance(project).commitDocument(document);
            // 将光标移动到新插入的 console.log 语句后
            if (autoFollowEnd) {
                caret.moveToOffset(document.getLineEndOffset(lineNumberCopy));
            }
        });
    }

    /**
     * 删除 console.log 语句
     */
    public void deleteWriter(
            Project project,
            Editor editor,
            List<TextRange> consoleLogNewRangeList,
            Pattern pattern,
            Pattern patternDefaultRegex
    ) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            int deleteStringSize = 0;

            Document document = editor.getDocument();
            for (TextRange range : consoleLogNewRangeList) {
                TextRange newRange = TextRange.create(
                        range.getStartOffset() - deleteStringSize,
                        range.getEndOffset() - deleteStringSize
                );
                String text = document.getText(newRange);
                Matcher matcher = pattern.matcher(text);

                if (matcher.find()) {
                    deleteStringSize += deleteConsoleLogMsg(newRange, document);
                    continue;
                }

                Matcher matchDefault = patternDefaultRegex != null ?
                        patternDefaultRegex.matcher(text) : null;
                if (matchDefault != null && matchDefault.find()) {
                    deleteStringSize += deleteConsoleLogMsg(newRange, document);
                }
            }
        });
    }

    /**
     * 删除符合插件规范的console.log表达式语句
     */
    private int deleteConsoleLogMsg(TextRange newRange, Document document) {
        // 删除符合插件规范的console.log表达式语句
        int startOffset = newRange.getStartOffset();
        int endOffset = newRange.getEndOffset();

        // 删除匹配的内容
        document.deleteString(startOffset, endOffset);
        return endOffset - startOffset;
    }

    /**
     * 注释console.log表达式
     */
    public void commentWriter(
            Project project,
            Editor editor,
            Map<TextRange, List<Integer>> consoleLogNewLineNumberMap,
            Pattern pattern,
            Pattern patternDefaultRegex
    ) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            int insertCommentSignalSize = 0;

            Document document = editor.getDocument();
            for (Map.Entry<TextRange, List<Integer>> entry : consoleLogNewLineNumberMap.entrySet()) {
                TextRange range = entry.getKey();
                List<Integer> lineNumberList = entry.getValue();

                TextRange newRange = TextRange.create(
                        range.getStartOffset() + insertCommentSignalSize,
                        range.getEndOffset() + insertCommentSignalSize
                );
                String text = document.getText(newRange);

                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    insertCommentSignalSize += insertCommentSignalBeforeConsoleLogMsg(
                            document,
                            lineNumberList,
                            newRange
                    );
                    continue;
                }

                Matcher matchDefault = patternDefaultRegex != null ?
                        patternDefaultRegex.matcher(text) : null;
                if (matchDefault != null && matchDefault.find()) {
                    insertCommentSignalSize += insertCommentSignalBeforeConsoleLogMsg(
                            document,
                            lineNumberList,
                            newRange
                    );
                }
            }
        });
    }

    /**
     * 注释console.log表达式
     */
    private int insertCommentSignalBeforeConsoleLogMsg(
            Document document,
            List<Integer> lineNumberList,
            TextRange newRange
    ) {
        int insertCommentSignalSize = 0;

        int firstLineStartOffset = document.getLineStartOffset(lineNumberList.get(0));
        // 取最大的偏移，保证不会注释到console.log前面的代码（怪异代码的感觉+1）
        document.insertString(Math.max(newRange.getStartOffset(), firstLineStartOffset),
                SettingConstant.COMMENT_SIGNAL);
        insertCommentSignalSize += SettingConstant.COMMENT_SIGNAL_LENGTH;

        for (int i = 1; i < lineNumberList.size(); i++) {
            int lineStartOffset = document.getLineStartOffset(lineNumberList.get(i));
            document.insertString(lineStartOffset, SettingConstant.COMMENT_SIGNAL);
            insertCommentSignalSize += SettingConstant.COMMENT_SIGNAL_LENGTH;
            // 万一结尾还有非console.log的代码呢？应该没有人写这么怪异的代码，，，吧？
        }

        return insertCommentSignalSize;
    }

    /**
     * 解注释console.log表达式
     */
    public void unCommentWriter(
            Project project,
            Editor editor,
            List<TextRange> consoleLogNewRangeList,
            Pattern pattern,
            Pattern patternDefaultRegex
    ) {
        WriteCommandAction.runWriteCommandAction(project, (Runnable) () -> {
            int deleteStringSize = 0;

            Document document = editor.getDocument();
            for (TextRange range : consoleLogNewRangeList) {
                TextRange newRange = TextRange.create(range.getStartOffset() - deleteStringSize,
                        range.getEndOffset() - deleteStringSize);
                String text = document.getText(newRange);
                Matcher matcher = pattern.matcher(text);

                if (matcher.find()) {
                    deleteStringSize += deleteCommentSignalBeforeConsoleLogMsg(document, newRange);
                    continue;
                }

                Matcher matcherDefault = patternDefaultRegex != null ?
                        patternDefaultRegex.matcher(text) : null;
                if (matcherDefault != null && matcherDefault.find()) {
                    deleteStringSize += deleteCommentSignalBeforeConsoleLogMsg(document, newRange);
                }
            }
        });
    }

    /**
     * 解注释符合插件规范的console.log表达式语句
     */
    private int deleteCommentSignalBeforeConsoleLogMsg(Document document, TextRange newRange) {

        int startOffset = newRange.getStartOffset() - SettingConstant.COMMENT_SIGNAL_LENGTH;
        int endOffset = newRange.getStartOffset();

        // 解注释匹配的内容
        document.deleteString(startOffset, endOffset);
        return SettingConstant.COMMENT_SIGNAL_LENGTH;
    }

    /**
     * 更新行号
     */
    public void updateLineNumber(ConsoleLogSettingState settings, Project project, Editor editor, PsiFile psiFile) {
        CompletableFuture.runAsync(() -> {
            String compositeConsoleLogMsgRegex = ConsoleLogMsgUtil.buildFindLineNumberConsoleLogMsgRegex(settings);
            Pattern pattern = Pattern.compile(compositeConsoleLogMsgRegex);

            Document document = editor.getDocument();

            List<TextRange> consoleLogRangeList = runReadAction(() -> ConsoleLogPsiUtil.detectAll(psiFile, document));

            int updateStringSize = 0;
            List<UpdateEntity> updateEntityList = new java.util.ArrayList<>();
            for (TextRange range : consoleLogRangeList) {
                TextRange newRange = TextRange.create(range.getStartOffset() - updateStringSize,
                        range.getEndOffset() - updateStringSize);
                String text = document.getText(newRange);
                Matcher matcher = pattern.matcher(text);

                if (matcher.find()) {
                    updateStringSize += updateLineNumberBeforeConsoleLogMsg(document, newRange, matcher, updateEntityList);
                }
            }

            WriteCommandAction.runWriteCommandAction(project, () -> {
                for (UpdateEntity updateEntity : updateEntityList) {
                    document.replaceString(
                            updateEntity.getStartOffset(),
                            updateEntity.getEndOffset(),
                            updateEntity.getText()
                    );
                }
            });
        });
    }

    private int updateLineNumberBeforeConsoleLogMsg(
            Document document,
            TextRange newRange,
            Matcher matcher,
            List<UpdateEntity> updateEntityList
    ) {
        int startOffset = newRange.getStartOffset();
        int endOffset = newRange.getEndOffset();
        // 正则总组数
        int groupCount = matcher.groupCount();
        int updateStringSize = 0;

        // 新行号信息
        String newLineNumber = String.valueOf(document.getLineNumber((startOffset + endOffset) / 2) + 1);
        int newLineNumberSize = newLineNumber.length();

        for (int i = 1; i <= groupCount; i++) {
            // 旧行号信息
            String oldLineNumber = matcher.group(i);
            if (oldLineNumber == null) continue;
            int oldLineNumberSize = oldLineNumber.length();
            // 相对于newRange范围的偏移量
            int oldLineNumberStartOffset = matcher.start(i);
            int oldLineNumberEndOffset = matcher.end(i);

            updateEntityList.add(
                    new UpdateEntity(
                            startOffset + oldLineNumberStartOffset + updateStringSize,
                            startOffset + oldLineNumberEndOffset + updateStringSize,
                            newLineNumber
                    )
            );
            updateStringSize += oldLineNumberSize - newLineNumberSize;
        }

        return updateStringSize;
    }

    /**
     * 执行读操作
     */
    public static <T> T runReadAction(java.util.function.Supplier<T> block) {
        return ApplicationManager.getApplication().runReadAction((Computable<T>) block::get);
    }
}