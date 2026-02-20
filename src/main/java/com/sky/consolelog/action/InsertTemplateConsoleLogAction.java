package com.sky.consolelog.action;

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
import com.sky.consolelog.entities.ScopeOffset;
import com.sky.consolelog.setting.ConsoleLogSettingVo;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
import com.sky.consolelog.utils.PsiPositionUtil;
import com.sky.consolelog.utils.TextFormatContext;
import com.sky.consolelog.utils.WriterCoroutineUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import static com.sky.consolelog.action.InsertConsoleLogAction.*;

/**
 * TODO
 *
 * @author SkySource
 * @Date: 2026/2/15 20:06
 */
public class InsertTemplateConsoleLogAction extends AnAction {

    private final ConsoleLogSettingState settings = ApplicationManager.getApplication().getService(ConsoleLogSettingState.class);
    private final WriterCoroutineUtils writerCoroutineUtils = ApplicationManager.getApplication().getService(WriterCoroutineUtils.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        // 获取当前文件信息的对象（用于判断当前文件语言类型）
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        Project project = event.getProject();
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
                String consoleLogMsg = TextFormatContext.INSTANCE.getDefaultTemplateHandleConsoleLogMsg(settings.defaultConsoleLogMsg, consoleLogSettingVo);
                writerCoroutineUtils.insertDefaultTemplateWriter(project, editor, psiFile, caret, scopeOffset, consoleLogMsg, settings.defaultAutoFollowEnd);
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
        String consoleLogMsg = TextFormatContext.INSTANCE.getCustomTemplateHandleConsoleLogMsg(settings.consoleLogMsg, consoleLogSettingVo);

        writerCoroutineUtils.insertTemplateWriter(project, editor, psiFile, caret, scopeOffset, consoleLogMsg, settings.autoFollowEnd);

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
    public void buildDefaultCommonPlaceHolder(Caret caret, PsiFile psiFile, ConsoleLogSettingVo consoleLogSettingVo, Editor editor, ScopeOffset scopeOffset) {
        getMethodName(caret, psiFile, consoleLogSettingVo);
        getLineNumber(scopeOffset, editor, consoleLogSettingVo);
        getFileName(psiFile, settings, consoleLogSettingVo);
        getFilePath(psiFile, settings, consoleLogSettingVo);
    }

    /**
     * 获取占位符的值
     * @param caret 光标对象
     * @param psiFile PSI文件树
     * @param consoleLogSettingVo 参数
     * @param editor 编辑器对象
     * @param scopeOffset 偏移量信息
     */
    public void buildCommonPlaceHolder(Caret caret, PsiFile psiFile, ConsoleLogSettingVo consoleLogSettingVo, Editor editor, ScopeOffset scopeOffset) {
        getMethodName(caret, psiFile, consoleLogSettingVo);
        if (settings.variableLineNumber) {
            getLineNumber(caret, editor, consoleLogSettingVo);
        } else {
            getLineNumber(scopeOffset, editor, consoleLogSettingVo);
        }
        getFileName(psiFile, settings, consoleLogSettingVo);
        getFilePath(psiFile, settings, consoleLogSettingVo);
    }
}
