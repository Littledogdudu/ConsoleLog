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
import com.sky.consolelog.entities.ScopeOffset;
import com.sky.consolelog.setting.ConsoleLogSettingVo;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
import com.sky.consolelog.utils.PsiPositionUtil;
import com.sky.consolelog.utils.PsiVariableUtil;
import com.sky.consolelog.utils.TextFormatContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 按下Alt+1快捷键生成console.log调用表达式
 *
 * @author SkySource
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

        // 找到最近的作用域块
        ScopeOffset scopeOffset = findScopeOffset(elementAtCaret);

        ConsoleLogSettingVo consoleLogSettingVo = new ConsoleLogSettingVo();
        // 检查是否有选中的文本
        boolean hasSelectedText = getVariableName(editor, psiFile, consoleLogSettingVo);
        if (!hasSelectedText) return;
        getMethodName(caret, psiFile, consoleLogSettingVo);
        if (settings.variableLineNumber) {
            getLineNumber(caret, editor, consoleLogSettingVo);
        } else {
            getLineNumber(scopeOffset, editor, consoleLogSettingVo);
        }
        getFileName(psiFile, settings, consoleLogSettingVo);

        // 构建 console.log
        // 获取文本格式上下文单例的同时更新策略
        String consoleLogMsg = TextFormatContext.INSTANCE.getCustomHandleConsoleLogMsg(settings.consoleLogMsg, consoleLogSettingVo);

        insertConsoleLogMsg(project, editor, psiFile, caret, scopeOffset, consoleLogMsg);
    }

    /**
     * 获取光标处变量名称
     *
     * @return 变量名称
     */
    private static boolean getVariableName(Editor editor, PsiFile psiFile, ConsoleLogSettingVo consoleLogSettingVo) {
        Caret caret = editor.getCaretModel().getCurrentCaret();
        int elementAtCaretIndex = caret.getOffset();

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

    /**
     * 获取光标处方法名称
     */
    private static void getMethodName(Caret caret, PsiFile psiFile, ConsoleLogSettingVo consoleLogSettingVo) {
        // 找到光标所在位置的 PSI 元素
        int caretOffset = caret.getOffset();
        PsiElement elementAtCaret = psiFile.findElementAt(caretOffset);

        int loopCount = 0;
        JSFunction function = PsiTreeUtil.getParentOfType(elementAtCaret, JSFunction.class, false);
        while (!Objects.isNull(function) && "<anonymous>".equals(function.toString().substring(function.toString().indexOf(":") + 1))) {
            function = PsiTreeUtil.getParentOfType(function, JSFunction.class, true);
            if (++loopCount >= 5) {
                break;
            }
        }

        if (!Objects.isNull(function)) {
            String functionName = function.getName();
            if (!Objects.isNull(functionName)) {
                consoleLogSettingVo.setMethodName(functionName);
            }
        }
    }

    /**
     * 获取当前光标所在位置的行号
     * @param caret 光标对象
     * @param psiFile 当前文件对象
     * @param consoleLogSettingVo 占位符值
     */
    private static void getLineNumber(Caret caret, Editor editor, ConsoleLogSettingVo consoleLogSettingVo) {
        int offset = caret.getOffset();
        Document document = editor.getDocument();
        consoleLogSettingVo.setLineNumber(document.getLineNumber(offset) + 1);
    }

    /**
     * 获取打印表达式将要插入位置的行号
     * @param psiFile 当前文件对象
     * @param scopeOffset 插入位置对象
     * @param consoleLogSettingVo 占位符值
     */
    private static void getLineNumber(ScopeOffset scopeOffset, Editor editor, ConsoleLogSettingVo consoleLogSettingVo) {
        int offset = scopeOffset.getInsertEndOffset();
        if (scopeOffset.getNeedBegLine()) {
            ++offset;
        }
        Document document = editor.getDocument();
        consoleLogSettingVo.setLineNumber(document.getLineNumber(offset) + 1);
    }

    private static void getFileName(PsiFile psiFile, ConsoleLogSettingState settings, ConsoleLogSettingVo consoleLogSettingVo) {
        String fileName = psiFile.getName();
        if (!settings.fileSuffix) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        consoleLogSettingVo.setFileName(fileName);
    }

    /**
     * 找到对应语句块并得到对应末尾偏移量
     *
     * @param element 当前光标所在PSI元素
     * @return 对应语句块末尾偏移量
     */
    private static ScopeOffset findScopeOffset(PsiElement element) {
        int count = 0;
        ScopeOffset offset = PsiPositionUtil.getScopeOffsetByType(element);
        PsiElement parent = element.getParent();
        while (parent != null && offset == null && count++ < 10) {
            offset = PsiPositionUtil.getScopeOffsetByType(parent);
            if (offset != null) {
                return offset;
            }
            parent = parent.getParent();
        }
        return PsiPositionUtil.getDefault(element);
    }

    /**
     * 插入console.log表达式信息
     */
    private void insertConsoleLogMsg(Project project, Editor editor, PsiFile psiFile, Caret caret, ScopeOffset scopeOffset, String consoleLogMsg) {
        Document document = editor.getDocument();
        // 找到光标所在行的结束位置
        int lineStartOffset = document.getLineStartOffset(document.getLineNumber(scopeOffset.getInsertEndOffset()));
        int lineEndOffset = document.getLineEndOffset(document.getLineNumber(scopeOffset.getInsertEndOffset()));
        // 获取光标所在行的内容，并计算缩进
        String currentLine = document.getText().substring(lineStartOffset, lineEndOffset);
        String indentation = currentLine.replace(currentLine.trim(), "");
        // 存储偏移量以使光标回归到插入表达式后&记录当前缩进程度
        Map<String, Object> offset = new HashMap<>(2);
        offset.put("indentation", indentation);

        CodeStyleSettings currentSettings = CodeStyle.getSettings(project);
        CommonCodeStyleSettings languageSettings = currentSettings.getCommonSettings(psiFile.getLanguage());
        CommonCodeStyleSettings.IndentOptions indentOptions = languageSettings.getIndentOptions();
        int tabSize = indentOptions == null ? 2 : indentOptions.TAB_SIZE;

        // 插入代码前添加适当的缩进
        if (scopeOffset.getNeedTab()) {
            indentation += " ".repeat(tabSize);
        }
        String indentedCode = indentation + consoleLogMsg;

        // 在光标所在行的结束位置插入 console.log 语句
        if (scopeOffset.getDefault()) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                document.insertString(lineEndOffset + 1, indentedCode + "\n");
                offset.put("offset", lineEndOffset + indentedCode.length());
            });
        } else {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                if (scopeOffset.getNeedBegLine()) {
                    document.insertString(scopeOffset.getInsertEndOffset(), "\n" + indentedCode);
                    offset.put("offset", scopeOffset.getInsertEndOffset() + 1 + indentedCode.length());
                } else {
                    document.insertString(scopeOffset.getInsertEndOffset(), " ".repeat(tabSize) + consoleLogMsg);
                    offset.put("offset", scopeOffset.getInsertEndOffset() + tabSize + consoleLogMsg.length());
                }

                if (scopeOffset.getNeedEndLine()) {
                    String ch = document.getText().substring((int) offset.get("offset"), (int) offset.get("offset") + 1);
                    if (!"\n".equals(ch)) {
                        document.insertString((int) offset.get("offset"), "\n");
                        document.insertString((int) offset.get("offset") + 1, (String) offset.get("indentation"));
                    }
                }
            });
        }

        // 更新 PSI 树以反映文档变化
        PsiDocumentManager.getInstance(project).commitDocument(document);
        // 将光标移动到新插入的 console.log 语句后
        if (settings.autoFollowEnd) {
            caret.moveToOffset((int) offset.get("offset"));
        }
    }
}
