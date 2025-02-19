package com.sky.consolelog.action;

import com.intellij.application.options.CodeStyle;
import com.intellij.lang.javascript.psi.JSFunction;
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
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.util.PsiTreeUtil;
import com.sky.consolelog.constant.SettingConstant;
import com.sky.consolelog.entities.ScopeOffset;
import com.sky.consolelog.setting.ConsoleLogSettingVo;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
import com.sky.consolelog.utils.PsiPositionUtil;
import com.sky.consolelog.utils.PsiVariableUtil;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;

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
        boolean hasSelectedText = getVariableName(editor, caret, elementAtCaretIndex, psiFile, consoleLogSettingVo);
        if (!hasSelectedText) return;
        getMethodName(caret, psiFile, consoleLogSettingVo);

        // 构建 console.log
        String consoleLogMsg = getCustomHandleConsoleLogMsg(consoleLogSettingVo);

        // 找到最近的作用域块
        ScopeOffset scopeOffset = findScopeOffset(elementAtCaret);
        insertConsoleLogMsg(project, editor, psiFile, caret, scopeOffset, consoleLogMsg);
    }

    private static boolean getVariableName(Editor editor, Caret caret, Integer elementAtCaretIndex, PsiFile psiFile, ConsoleLogSettingVo consoleLogSettingVo) {
        String selectedText = editor.getSelectionModel().getSelectedText();
        if (selectedText != null && !selectedText.isEmpty()) {
            consoleLogSettingVo.setVariableName(selectedText);
        } else {
            // 如果没有选中的文本，则获取光标所在位置的单词
            // Notice：当光标位置处于变量结尾时，PSI元素会选取变量的父级，故同时判断光标前移1一个字符的PSI元素类型
            String variableName = PsiVariableUtil.getVariableNameByOffsetIndex(editor, psiFile, caret, elementAtCaretIndex);
            if (variableName == null) {
                return false;
            }
            consoleLogSettingVo.setVariableName(variableName);
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
        String replaceConsoleLogStr = settings.consoleLogMsg;
        replaceConsoleLogStr = replaceConsoleLog(replaceConsoleLogStr, SettingConstant.AliasRegex.VARIABLE_REGEX, consoleLogSettingVo.getVariableName());
        replaceConsoleLogStr = replaceConsoleLog(replaceConsoleLogStr, SettingConstant.AliasRegex.METHOD_REGEX, consoleLogSettingVo.getMethodName());
        return SettingConstant.CONSOLE_LOG_COMMAND +
                replaceConsoleLogStr + "\", " + consoleLogSettingVo.getVariableName() + ");";
    }

    private String replaceConsoleLog(String replaceConsoleLogStr, SettingConstant.AliasRegex aliasRegex, String value) {
        if (value.contains("$")) {
            if (value.contains("\"")) {
                // 新tips：因为replaceAll对替换项（replacement）的$有特殊处理，故此处使用Matcher.quoteReplacement对替换项做处理
                // 哎光看源码了，今天看了注释才发现可以这么简单，焯！🤡
                return replaceConsoleLogStr.replaceAll(aliasRegex.getKey(), Matcher.quoteReplacement(value.replaceAll("\"", "\\\\\"")));
            }
            return replaceConsoleLogStr.replaceAll(aliasRegex.getKey(), Matcher.quoteReplacement(value));
        }
        if (value.contains("\"")) {
            return replaceConsoleLogStr.replaceAll(aliasRegex.getKey(), value.replaceAll("\"", "\\\\\""));
        }
        return replaceConsoleLogStr.replaceAll(aliasRegex.getKey(), value);
    }

    /**
     * 找到对应语句块并得到对应末尾偏移量
     * @param element 当前光标所在PSI元素
     * @return 对应语句块末尾偏移量
     */
    private static ScopeOffset findScopeOffset(PsiElement element) {
        int count = 0;
        ScopeOffset offset = PsiPositionUtil.getScopeOffsetByType(element);
        PsiElement parent = element.getParent();
        while (parent != null && offset == null && count++ < 5) {
            offset = PsiPositionUtil.getScopeOffsetByType(parent);
            if (offset != null) {
                return offset;
            }
            parent = parent.getParent();
        }
        return PsiPositionUtil.getDefault(element);
    }

    private void insertConsoleLogMsg(Project project, Editor editor, PsiFile psiFile, Caret caret, ScopeOffset scopeOffset, String consoleLogMsg) {
        Document document = editor.getDocument();
        // 找到光标所在行的结束位置
        int lineStartOffset = document.getLineStartOffset(document.getLineNumber(scopeOffset.getInsertEndOffset()));
        int lineEndOffset = document.getLineEndOffset(document.getLineNumber(scopeOffset.getInsertEndOffset()));
        // 获取光标所在行的内容，并计算缩进
        String currentLine = document.getText().substring(lineStartOffset, lineEndOffset);
        String indentation = currentLine.replace(currentLine.trim(), "");
        // 插入代码前添加适当的缩进
        String indentedCode;
        if (scopeOffset.getNeedTab()) {
            CodeStyleSettings currentSettings = CodeStyle.getSettings(project);
            CommonCodeStyleSettings languageSettings = currentSettings.getCommonSettings(psiFile.getLanguage());
            CommonCodeStyleSettings.IndentOptions indentOptions = languageSettings.getIndentOptions();
            int tabSize = 2;
            if (indentOptions != null) {
                tabSize = indentOptions.TAB_SIZE;
            }
            indentation += " ".repeat(tabSize);
        }
        indentedCode = indentation + consoleLogMsg;
        // 在光标所在行的结束位置插入 console.log 语句
        if (scopeOffset.getDefault()) {
            WriteCommandAction.runWriteCommandAction(project, () ->
                    document.insertString(lineEndOffset + 1, indentedCode + "\n"));
        } else {
            WriteCommandAction.runWriteCommandAction(project, () ->
                    document.insertString(scopeOffset.getInsertEndOffset(), "\n" + indentedCode));
        }
        // 更新 PSI 树以反映文档变化
        PsiDocumentManager.getInstance(project).commitDocument(document);
        // 将光标移动到新插入的 console.log 语句后
        if (settings.autoFollowEnd) {
            caret.moveToOffset(lineEndOffset + 1 + indentation.length() + consoleLogMsg.length());
        }
    }
}
