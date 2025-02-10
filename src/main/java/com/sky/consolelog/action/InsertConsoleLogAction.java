package com.sky.consolelog.action;

import com.intellij.lang.javascript.psi.JSExpressionStatement;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSVarStatement;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptParameter;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptVariable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.sky.consolelog.constant.SettingConstant;
import com.sky.consolelog.setting.ConsoleLogSettingVo;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
import org.jetbrains.annotations.NotNull;

/**
 * @author by: SkySource
 * @Description: 按下Alt+1快捷键生成console.log调用表达式
 * @Date: 2025/1/24 22:43
 */
public class InsertConsoleLogAction extends AnAction {

    private final ConsoleLogSettingState settings = ApplicationManager.getApplication().getService(ConsoleLogSettingState.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (settings.consoleLogMsg == null || settings.consoleLogMsg.isEmpty()) {
            return;
        }

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        // 获取当前文件信息的对象（用于判断当前文件语言类型）
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        Project project = e.getProject();
        if (project == null || editor == null || psiFile == null) {
            return;
        }
        // 获取当前光标对象
        Caret caret = editor.getCaretModel().getCurrentCaret();
        int elementAtCaretIndex = caret.getOffset();
        PsiElement elementAtCaret = psiFile.findElementAt(elementAtCaretIndex);
        if (elementAtCaret == null) {
            return;
        }

        ConsoleLogSettingVo consoleLogSettingVo = new ConsoleLogSettingVo();
        // 检查是否有选中的文本
        boolean hasSelectedText = getVariableName(editor, caret, elementAtCaret, consoleLogSettingVo);
        if (!hasSelectedText) return;
        getMethodName(caret, psiFile, consoleLogSettingVo);

        // 构建 console.log
        String consoleLogMsg = getCustomHandleConsoleLogMsg(consoleLogSettingVo);

        // 找到最近的作用域块
        int insertOffset = findScopeOffset(elementAtCaret);
        insertConsoleLogMsg(project, editor, caret, insertOffset, consoleLogMsg);
    }

    private static boolean getVariableName(Editor editor, Caret caret, PsiElement elementAtCaret, ConsoleLogSettingVo consoleLogSettingVo) {
        String selectedText = editor.getSelectionModel().getSelectedText();
        if (selectedText != null && !selectedText.isEmpty()) {
            consoleLogSettingVo.setVariableName(selectedText);
        } else {
            // 如果没有选中的文本，则获取光标所在位置的单词
            PsiElement parent = elementAtCaret.getParent();
            if (parent instanceof JSReferenceExpression) {
                String word = parent.getText();
                if (word == null) {
                    return false;
                }
                consoleLogSettingVo.setVariableName(word);
            } else {
                // 首先获取光标的偏移量
                int offset = caret.getOffset();
                Document document = editor.getDocument();
                // 获取光标之前的所有文本
                String line = document.getText().substring(0, offset);

                // 获取单词前后内容，直到遇到空格
                int wordStart = findWordStart(line, offset);
                int wordEnd = findWordEnd(document, offset);

                if (wordStart >= 0 && wordEnd > wordStart) {
                    String word = document.getText().substring(wordStart, wordEnd);
                    consoleLogSettingVo.setVariableName(word);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private static void getMethodName(Caret caret, PsiFile psiFile, ConsoleLogSettingVo consoleLogSettingVo) {
        // 找到光标所在位置的 PSI 元素
        int caretOffset = caret.getOffset();
        PsiElement elementAtCaret = psiFile.findElementAt(caretOffset);
        JSFunction containingFunction = PsiTreeUtil.getParentOfType(elementAtCaret, JSFunction.class, false);
        if (containingFunction != null) {
            String functionName = containingFunction.getName();
            if (functionName != null) {
                consoleLogSettingVo.setMethodName(functionName);
            }
        }
    }

    private @NotNull String getCustomHandleConsoleLogMsg(ConsoleLogSettingVo consoleLogSettingVo) {
        return SettingConstant.CONSOLE_LOG_COMMAND +
                settings.consoleLogMsg
                        .replaceAll(SettingConstant.AliasRegex.VARIABLE_REGEX.getKey(), replaceDoubleQuoteToEscapeDoubleQuote(consoleLogSettingVo.getVariableName()))
                        .replaceAll(SettingConstant.AliasRegex.METHOD_REGEX.getKey(), replaceDoubleQuoteToEscapeDoubleQuote(consoleLogSettingVo.getMethodName())) +
                "\", " + consoleLogSettingVo.getVariableName() + ");";
    }

    private static int findWordStart(String text, int offset) {
        for (int i = offset - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '-') {
                return i + 1;
            }
        }
        return 0;
    }

    private static int findWordEnd(Document document, int offset) {
        int length = document.getTextLength();
        for (int i = offset; i < length; i++) {
            char c = document.getCharsSequence().charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '-') {
                return i;
            }
        }
        return length;
    }

    /**
     * 找到对应语句块并得到对应末尾偏移量
     * @param element 当前光标所在PSI元素
     * @return 对应语句块末尾偏移量
     */
    private static int findScopeOffset(PsiElement element) {
        PsiElement parent = element.getParent();
        while (parent != null) {
            // 变量范围
            if (parent instanceof JSVarStatement || parent instanceof TypeScriptVariable) {
                return parent.getTextRange().getEndOffset();
            }
            // 参数
            if (parent instanceof TypeScriptParameter) {
                return element.getTextRange().getEndOffset();
            }
            // 表达式范围
            if (parent instanceof JSExpressionStatement) {
                return parent.getTextRange().getEndOffset();
            }
            parent = parent.getParent();
        }
        // 单纯在光标下一行插入
        return element.getTextRange().getEndOffset();
    }

    private static void insertConsoleLogMsg(Project project, Editor editor, Caret caret, Integer insertOffset, String consoleLogMsg) {
        Document document = editor.getDocument();
        // 找到光标所在行的结束位置
        int lineStartOffset = document.getLineStartOffset(document.getLineNumber(insertOffset));
        int lineEndOffset = document.getLineEndOffset(document.getLineNumber(insertOffset));
        // 获取光标所在行的内容，并计算缩进
        String currentLine = document.getText().substring(lineStartOffset, lineEndOffset);
        String indentation = currentLine.replace(currentLine.trim(), "");
        // 插入代码前添加适当的缩进
        String indentedCode = indentation + consoleLogMsg;
        // 在光标所在行的结束位置插入 console.log 语句
        WriteCommandAction.runWriteCommandAction(project, () ->
                document.insertString(lineEndOffset + 1, indentedCode + "\n"));
        // 更新 PSI 树以反映文档变化
        PsiDocumentManager.getInstance(project).commitDocument(document);
        // 将光标移动到新插入的 console.log 语句后
        caret.moveToOffset(lineEndOffset + 1 + indentation.length() + consoleLogMsg.length());
    }

    /**
     * 转义双引号避免遇到类似data["tableName"]的用法出现 用于结尾的双引号提前
     * @param name 变量名/方法名
     * @return 双引号转移过的变量名/方法名
     */
    private static String replaceDoubleQuoteToEscapeDoubleQuote(String name) {
        // 因为replaceAll对$有特殊处理，故此处也做特殊处理
        StringBuilder replaceStr = new StringBuilder();
        int length = name.length();
        int begIndex = 0;
        int dstIndex = name.indexOf("$");
        while (dstIndex != -1) {
            replaceStr.append(name.substring(begIndex, dstIndex).replaceAll("\"", "\\\\\\\\\""));
            replaceStr.append("$");
            begIndex = dstIndex + 1;
            dstIndex = replaceStr.substring(begIndex, length).indexOf("$");
        }
        if (begIndex < length) {
            replaceStr.append(name, begIndex, length);
        }
        return replaceStr.toString();
    }
}
