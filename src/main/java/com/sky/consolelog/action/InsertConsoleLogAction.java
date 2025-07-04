package com.sky.consolelog.action;

import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.sky.consolelog.entities.ScopeOffset;
import com.sky.consolelog.setting.ConsoleLogSettingVo;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
import com.sky.consolelog.utils.PsiPositionUtil;
import com.sky.consolelog.utils.PsiVariableUtil;
import com.sky.consolelog.utils.TextFormatContext;
import com.sky.consolelog.utils.WriterCoroutineUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 按下Alt+1快捷键生成console.log调用表达式
 *
 * @author SkySource
 * @Date: 2025/1/24 22:43
 */
public class InsertConsoleLogAction extends AnAction {

    private final ConsoleLogSettingState settings = ApplicationManager.getApplication().getService(ConsoleLogSettingState.class);
    private final WriterCoroutineUtils writerCoroutineUtils = ApplicationManager.getApplication().getService(WriterCoroutineUtils.class);

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
        boolean hasSelectedText = getVariableName(editor, psiFile, consoleLogSettingVo);
        if (!hasSelectedText) {
            if (settings.enableDefaultConsoleLogMsg) {
                // 没有选中的文本，则打印不带变量值的默认插入语句
                ScopeOffset scopeOffset = PsiPositionUtil.getUndefinedDefault(caret);

                //提前获取句首是否需要换行以便确定行号
                Document document = editor.getDocument();
                int lineNumber = document.getLineNumber(scopeOffset.getInsertEndOffset());
                int lineStartOffset = document.getLineStartOffset(lineNumber);
                String prevText = document.getText(new TextRange(lineStartOffset, scopeOffset.getInsertEndOffset()));
                scopeOffset.setNeedBegLine(!StringUtils.isAllBlank(prevText));

                buildDefaultCommonPlaceHolder(caret, psiFile, consoleLogSettingVo, editor, scopeOffset);
                String consoleLogMsg = TextFormatContext.INSTANCE.getDefaultHandleConsoleLogMsg(settings.defaultConsoleLogMsg, consoleLogSettingVo);
                writerCoroutineUtils.insertDefaultWriter(project, editor, psiFile, caret, scopeOffset, consoleLogMsg, settings.defaultAutoFollowEnd);
                if (settings.enableAutoFixLineNumber) {
                    writerCoroutineUtils.updateLineNumber(settings, project, editor, psiFile);
                }
            }
            return;
        }

        // 找到最近的作用域块
        ScopeOffset scopeOffset = findScopeOffset(elementAtCaret);

        buildCommonPlaceHolder(caret, psiFile, consoleLogSettingVo, editor, scopeOffset);
        // 构建 console.log
        String consoleLogMsg = TextFormatContext.INSTANCE.getCustomHandleConsoleLogMsg(settings.consoleLogMsg, consoleLogSettingVo);

        writerCoroutineUtils.insertWriter(project, editor, psiFile, caret, scopeOffset, consoleLogMsg, settings.autoFollowEnd);

        if (settings.enableAutoFixLineNumber) {
            writerCoroutineUtils.updateLineNumber(settings, project, editor, psiFile);
        }
    }

    /**
     * 获取占位符的值
     * @param caret 光标对象
     * @param psiFile PSI文件树
     * @param consoleLogSettingVo 参数
     * @param editor 编辑器对象
     * @param scopeOffset 偏移量信息
     */
    private void buildDefaultCommonPlaceHolder(Caret caret, PsiFile psiFile, ConsoleLogSettingVo consoleLogSettingVo, Editor editor, ScopeOffset scopeOffset) {
        getMethodName(caret, psiFile, consoleLogSettingVo);
        getLineNumber(scopeOffset, editor, consoleLogSettingVo);
        getFileName(psiFile, settings, consoleLogSettingVo);
    }

    /**
     * 获取占位符的值
     * @param caret 光标对象
     * @param psiFile PSI文件树
     * @param consoleLogSettingVo 参数
     * @param editor 编辑器对象
     * @param scopeOffset 偏移量信息
     */
    private void buildCommonPlaceHolder(Caret caret, PsiFile psiFile, ConsoleLogSettingVo consoleLogSettingVo, Editor editor, ScopeOffset scopeOffset) {
        getMethodName(caret, psiFile, consoleLogSettingVo);
        if (settings.variableLineNumber) {
            getLineNumber(caret, editor, consoleLogSettingVo);
        } else {
            getLineNumber(scopeOffset, editor, consoleLogSettingVo);
        }
        getFileName(psiFile, settings, consoleLogSettingVo);
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
     * @param consoleLogSettingVo 占位符值
     */
    private static void getLineNumber(Caret caret, Editor editor, ConsoleLogSettingVo consoleLogSettingVo) {
        int offset = caret.getOffset();
        Document document = editor.getDocument();
        consoleLogSettingVo.setLineNumber(document.getLineNumber(offset) + 1);
    }

    /**
     * 获取打印表达式将要插入位置的行号
     * @param scopeOffset 插入位置对象
     * @param consoleLogSettingVo 占位符值
     */
    private static void getLineNumber(ScopeOffset scopeOffset, Editor editor, ConsoleLogSettingVo consoleLogSettingVo) {
        int offset = scopeOffset.getInsertEndOffset();
        Document document = editor.getDocument();
        int lineNumber = document.getLineNumber(offset) + 1;
        if (scopeOffset.getNeedBegLine()) {
            ++lineNumber;
        }
        consoleLogSettingVo.setLineNumber(lineNumber);
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
}
