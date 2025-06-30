package com.sky.consolelog.utils

import com.intellij.application.options.CodeStyle
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.sky.consolelog.entities.ScopeOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils

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
        cs.launch {
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
                    val ch: String = document.getText(TextRange(scopeOffset.insertEndOffset, scopeOffset.insertEndOffset + 1));
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
        cs.launch {
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
}