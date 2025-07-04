package com.sky.consolelog.utils

import com.intellij.application.options.CodeStyle
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.sky.consolelog.constant.SettingConstant
import com.sky.consolelog.entities.ScopeOffset
import com.sky.consolelog.entities.UpdateEntity
import com.sky.consolelog.setting.storage.ConsoleLogSettingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * 文本写入协程工具类
 *
 * @author SkySource
 * @Date: 2025/6/30 18:53
 */
@Service
class WriterCoroutineUtils(
    private val cs: CoroutineScope
) {

    private var insertJob: Job? = null;
    private var defaultInsertJob: Job? = null;
    private var deleteJob: Job? = null;
    private var commentJob: Job? = null;
    private var uncommentJob: Job? = null;

    /**
     * 插入console.log表达式信息
     */
    fun insertWriter(
        project: Project,
        editor: Editor,
        psiFile: PsiFile,
        caret: Caret,
        scopeOffset: ScopeOffset,
        consoleLogMsg: String,
        autoFollowEnd: Boolean
    ) {
        insertJob = cs.launch {
            val document: Document = editor.document;

            // 找到光标所在行的结束位置
            val lineNumber: Int = document.getLineNumber(scopeOffset.insertEndOffset);
            val lineStartOffset: Int = document.getLineStartOffset(lineNumber);
            val lineEndOffset: Int = document.getLineEndOffset(lineNumber);

            // 获取光标所在行的内容，并计算缩进
            val currentLine: String = document.getText(TextRange(lineStartOffset, lineEndOffset));
            var indentation: String = currentLine.replace(currentLine.trim(), "");

            val currentSettings: CodeStyleSettings = CodeStyle.getSettings(project);
            val languageSettings: CommonCodeStyleSettings = currentSettings.getCommonSettings(psiFile.language);
            val tabSize: Int = languageSettings.indentOptions?.TAB_SIZE ?: 2;

            // 插入代码前添加适当的缩进
            if (scopeOffset.needTab) {
                indentation += " ".repeat(tabSize);
            }
            val indentedCode = "$indentation$consoleLogMsg";

            var offset: Int;
            // 在光标所在行的结束位置插入 console.log 语句
            if (scopeOffset.default) {
                offset = lineEndOffset + 1 + indentedCode.length;
                WriteCommandAction.runWriteCommandAction(project) {
                    document.insertString(lineEndOffset + 1, indentedCode + "\n");
                    // 将光标移动到新插入的 console.log 语句后
                    if (autoFollowEnd) {
                        caret.moveToOffset(offset);
                    }
                };
            } else {
                val sentence = StringBuilder();
                if (scopeOffset.needBegLine) {
                    sentence.append("\n").append(indentedCode);
                    offset = scopeOffset.insertEndOffset + 1 + indentedCode.length;
                } else {
                    sentence.append(" ".repeat(tabSize)).append(consoleLogMsg);
                    offset = scopeOffset.insertEndOffset + tabSize + consoleLogMsg.length;
                }

                if (scopeOffset.needEndLine) {
                    val ch: String =
                        document.getText(TextRange(scopeOffset.insertEndOffset, scopeOffset.insertEndOffset + 1));
                    if ("\n" != ch) {
                        // 插入的语句后面不是换行符，包含了代码，那么就在插入语句后面换行
                        sentence.append("\n").append(indentation);
                    }
                }
                WriteCommandAction.runWriteCommandAction(project) {
                    document.insertString(scopeOffset.insertEndOffset, sentence.toString());
                    // 更新 PSI 树以反应文档变化
                    PsiDocumentManager.getInstance(project).commitDocument(document);
                    // 将光标移动到新插入的 console.log 语句后
                    if (autoFollowEnd) {
                        caret.moveToOffset(offset);
                    }
                }
            }
        }
    }

    /**
     * 未选中文本时默认插入console.log表达式信息
     */
    fun insertDefaultWriter(
        project: Project,
        editor: Editor,
        psiFile: PsiFile,
        caret: Caret,
        scopeOffset: ScopeOffset,
        consoleLogMsg: String,
        autoFollowEnd: Boolean
    ) {
        defaultInsertJob = cs.launch {
            val document: Document = editor.document;

            // 找到光标所在行的结束位置
            var lineNumber: Int = document.getLineNumber(scopeOffset.insertEndOffset);
            val lineStartOffset: Int = document.getLineStartOffset(lineNumber);
            val lineEndOffset: Int = document.getLineEndOffset(lineNumber);

            val nextText: String = document.getText(TextRange(scopeOffset.insertEndOffset, lineEndOffset));
            scopeOffset.needEndLine = !StringUtils.isAllBlank(nextText);

            // 获取光标所在行的内容，并计算缩进
            val currentLine: String = document.text.substring(lineStartOffset, lineEndOffset);
            val indentation: String = currentLine.replace(currentLine.trim(), "");

            // 在光标所在行的结束位置插入 console.log 语句
            val sentence = StringBuilder();
            var offset: Int;
            if (scopeOffset.needBegLine) {
                sentence.append("\n").append(consoleLogMsg);
                offset = 1 + scopeOffset.insertEndOffset + consoleLogMsg.length;
                ++lineNumber;
            } else {
                sentence.append(consoleLogMsg);
                offset = scopeOffset.insertEndOffset + consoleLogMsg.length;
            }

            if (scopeOffset.needEndLine) {
                val ch: String = document.text.substring(scopeOffset.insertEndOffset, scopeOffset.insertEndOffset + 1);
                if ("\n" != ch) {
                    sentence.append("\n").append(indentation);
                }
            }
            WriteCommandAction.runWriteCommandAction(project) {
                document.insertString(scopeOffset.insertEndOffset, sentence.toString());
                // 格式化
                val codeStyleManager: CodeStyleManager = CodeStyleManager.getInstance(project);
                codeStyleManager.reformatText(psiFile, scopeOffset.insertEndOffset, offset);
                // 更新 PSI 树以反应文档变化
                PsiDocumentManager.getInstance(project).commitDocument(document);
                // 将光标移动到新插入的 console.log 语句后
                if (autoFollowEnd) {
                    caret.moveToOffset(document.getLineEndOffset(lineNumber));
                }
            }
        }
    }

    /**
     * 删除 console.log 语句
     */
    fun deleteWriter(
        project: Project,
        editor: Editor,
        consoleLogNewRangeList: List<TextRange>,
        pattern: Pattern,
        patternDefaultRegex: Pattern?
    ) {
        deleteJob = cs.launch {
            WriteCommandAction.runWriteCommandAction(project) {
                var deleteStringSize = 0;

                val document = editor.document;
                for (range in consoleLogNewRangeList) {
                    val newRange = TextRange(
                        range.startOffset - deleteStringSize,
                        range.endOffset - deleteStringSize
                    );
                    val text = document.getText(newRange);
                    val matcher = pattern.matcher(text);

                    if (matcher.find()) {
                        deleteStringSize += deleteConsoleLogMsg(newRange, document);
                        continue;
                    }

                    val matchDefault = patternDefaultRegex?.matcher(text);
                    if (matchDefault?.find() ?: false) {
                        deleteStringSize += deleteConsoleLogMsg(newRange, document);
                    }
                }
            }
        }
    }

    /**
     * 删除符合插件规范的console.log表达式语句
     */
    private fun deleteConsoleLogMsg(newRange: TextRange, document: Document): Int {
        // 删除符合插件规范的console.log表达式语句
        val startOffset: Int = newRange.startOffset;
        val endOffset: Int = newRange.endOffset;

        // 删除匹配的内容
        document.deleteString(startOffset, endOffset);
        return endOffset - startOffset;
    }

    /**
     * 注释console.log表达式
     */
    fun commentWriter(
        project: Project,
        editor: Editor,
        consoleLogNewLineNumberMap: Map<TextRange, List<Int>>,
        pattern: Pattern,
        patternDefaultRegex: Pattern?
    ) {
        commentJob = cs.launch {
            WriteCommandAction.runWriteCommandAction(project) {
                var insertCommentSignalSize = 0;

                val document = editor.document;
                for ((range, lineNumberList) in consoleLogNewLineNumberMap) {
                    val newRange = TextRange(
                        range.startOffset + insertCommentSignalSize,
                        range.endOffset + insertCommentSignalSize
                    );
                    val text = document.getText(newRange);

                    val matcher = pattern.matcher(text);
                    if (matcher.find()) {
                        insertCommentSignalSize += insertCommentSignalBeforeConsoleLogMsg(
                            document,
                            lineNumberList,
                            newRange
                        );
                        continue;
                    }

                    val matchDefault = patternDefaultRegex?.matcher(text);
                    if (matchDefault?.find() ?: false) {
                        insertCommentSignalSize += insertCommentSignalBeforeConsoleLogMsg(
                            document,
                            lineNumberList,
                            newRange
                        );
                    }
                }
            }
        }
    }

    /**
     * 注释console.log表达式
     */
    private fun insertCommentSignalBeforeConsoleLogMsg(
        document: Document,
        lineNumberList: List<Int>,
        newRange: TextRange
    ): Int {
        var insertCommentSignalSize = 0;

        val firstLineStartOffset = document.getLineStartOffset(lineNumberList[0]);
        // 取最大的偏移，保证不会注释到console.log前面的代码（怪异代码的感觉+1）
        document.insertString(newRange.startOffset.coerceAtLeast(firstLineStartOffset), SettingConstant.COMMENT_SIGNAL);
        insertCommentSignalSize += SettingConstant.COMMENT_SIGNAL_LENGTH;

        for (i in 1 until lineNumberList.size) {
            val lineStartOffset = document.getLineStartOffset(lineNumberList[i]);
            document.insertString(lineStartOffset, SettingConstant.COMMENT_SIGNAL);
            insertCommentSignalSize += SettingConstant.COMMENT_SIGNAL_LENGTH;
            // 万一结尾还有非console.log的代码呢？应该没有人写这么怪异的代码，，，吧？
        }

        return insertCommentSignalSize;
    }

    /**
     * 解注释console.log表达式
     */
    fun unCommentWriter(
        project: Project,
        editor: Editor,
        consoleLogNewRangeList: List<TextRange>,
        pattern: Pattern,
        patternDefaultRegex: Pattern?
    ) {
        uncommentJob = cs.launch {
            WriteCommandAction.runWriteCommandAction(project, Runnable {
                var deleteStringSize = 0;

                val document = editor.document;
                for (range in consoleLogNewRangeList) {
                    val newRange = TextRange(range.startOffset - deleteStringSize, range.endOffset - deleteStringSize);
                    val text = document.getText(newRange);
                    val matcher: Matcher = pattern.matcher(text);

                    if (matcher.find()) {
                        deleteStringSize += deleteCommentSignalBeforeConsoleLogMsg(document, newRange);
                        continue;
                    }

                    val matcherDefault = patternDefaultRegex?.matcher(text);
                    if (matcherDefault?.find() ?: false) {
                        deleteStringSize += deleteCommentSignalBeforeConsoleLogMsg(document, newRange);
                    }
                }
            })
        }
    }

    /**
     * 解注释符合插件规范的console.log表达式语句
     */
    private fun deleteCommentSignalBeforeConsoleLogMsg(document: Document, newRange: TextRange): Int {

        val startOffset: Int = newRange.startOffset - SettingConstant.COMMENT_SIGNAL_LENGTH;
        val endOffset: Int = newRange.startOffset;

        // 解注释匹配的内容
        document.deleteString(startOffset, endOffset);
        return SettingConstant.COMMENT_SIGNAL_LENGTH;
    }

    /**
     * 更新行号
     */
    fun updateLineNumber(settings: ConsoleLogSettingState, project: Project, editor: Editor, psiFile: PsiFile) {
        cs.launch {
            val compositeConsoleLogMsgRegex: String = ConsoleLogMsgUtil.buildFindLineNumberConsoleLogMsgRegex(settings);
            val pattern:  Pattern = Pattern.compile(compositeConsoleLogMsgRegex);

            insertJob?.join();
            defaultInsertJob?.join();
            deleteJob?.join();
            commentJob?.join();
            uncommentJob?.join();

            val document = editor.document;

            val consoleLogRangeList: List<TextRange> = runReadAction<List<TextRange>> {
                ConsoleLogPsiUtil.detectAll(psiFile, document);
            }

            var updateStringSize = 0;
            val updateEntityList: MutableList<UpdateEntity> = mutableListOf();
            for (range in consoleLogRangeList) {
                val newRange = TextRange(range.startOffset - updateStringSize, range.endOffset - updateStringSize);
                val text = document.getText(newRange);
                val matcher: Matcher = pattern.matcher(text);

                if (matcher.find()) {
                    updateStringSize += updateLineNumberBeforeConsoleLogMsg(document, newRange, matcher, updateEntityList);
                }
            }

            WriteCommandAction.runWriteCommandAction(project) {
                for (updateEntity in updateEntityList) {
                    document.replaceString(
                        updateEntity.startOffset,
                        updateEntity.endOffset,
                        updateEntity.text
                    );
                }
            }
        }
    }

    private fun updateLineNumberBeforeConsoleLogMsg(
        document: Document,
        newRange: TextRange,
        matcher: Matcher,
        updateEntityList: MutableList<UpdateEntity>
    ): Int {
        val startOffset: Int = newRange.startOffset;
        val endOffset: Int = newRange.endOffset;
        // 正则总组数
        val groupCount: Int = matcher.groupCount();
        var updateStringSize = 0;

        // 新行号信息
        val newLineNumber: String = (document.getLineNumber((startOffset + endOffset) / 2) + 1).toString();
        val newLineNumberSize: Int = newLineNumber.length;

        for (i in 1..groupCount) {
            // 旧行号信息
            val oldLineNumber: String = matcher.group(i) ?: continue;
            val oldLineNumberSize: Int = oldLineNumber.length;
            // 相对于newRange范围的偏移量
            val oldLineNumberStartOffset: Int = matcher.start(i);
            val oldLineNumberEndOffset: Int = matcher.end(i);

            updateEntityList.add(
                UpdateEntity(
                    startOffset + oldLineNumberStartOffset + updateStringSize,
                    startOffset + oldLineNumberEndOffset + updateStringSize,
                    newLineNumber
                    )
            );
            updateStringSize += oldLineNumberSize - newLineNumberSize;
        }

        return updateStringSize;
    }

    inline fun <T> runReadAction(crossinline block: () -> T): T {
        return ApplicationManager.getApplication().runReadAction(Computable {
            block()
        });
    }
}