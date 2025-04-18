package com.sky.consolelog.search.ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBList;
import com.sky.consolelog.constant.SettingConstant;
import com.sky.consolelog.entities.ConsoleLogSearchInfo;
import com.sky.consolelog.utils.TextFormatContext;
import com.sky.consolelog.utils.TextFormatContextSingleton;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 侧边栏UI组件
 *
 * @author SkySource
 * @Date: 2025/3/21 20:51
 */
public class ConsoleLogToolWindowComponent {
    private final JPanel panel;
    private final JBCheckBox commentCheckBox;
    private final JBCheckBox specCheckBox;
    private final JBList<ConsoleLogSearchInfo> logList;
    private final DefaultListModel<ConsoleLogSearchInfo> model;
    private final Project project;

    public ConsoleLogToolWindowComponent(Project project) {
        TextFormatContextSingleton.getInstance();
        this.project = project;
        panel = new JPanel(new BorderLayout());
        commentCheckBox = new JBCheckBox();
        specCheckBox = new JBCheckBox();
        model = new DefaultListModel<>();
        logList = new JBList<>(model);

        commentCheckBox.setText("展示注释项");
        specCheckBox.setText("启用针对性查找");
        Box topBox = Box.createHorizontalBox();
        topBox.add(Box.createHorizontalGlue());
        topBox.add(commentCheckBox);
        topBox.add(specCheckBox);

        JSeparator separator = new JSeparator();

        // 触发快速查找
        logList.setCellRenderer(new BeautifulListCellRender());

        panel.add(topBox, BorderLayout.NORTH);
        panel.add(separator, BorderLayout.CENTER);
        panel.add(new JScrollPane(logList), BorderLayout.CENTER);

        // 初始化console.log表达式列表
        updateLogListEntries();

        // 监听【是否启用针对性查找】按钮
        specCheckBox.addActionListener(e -> {
            updateLogListEntries();
        });
        commentCheckBox.addActionListener(e -> {
            updateLogListEntries();
        });
        // 监听鼠标点击--到达表达式位置
        logList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ConsoleLogSearchInfo selectInfo = logList.getSelectedValue();
                if (selectInfo != null) {
                    navigateToConsoleLog(selectInfo);
                }
            }
        });
        // 添加文件切换监听器
        project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,
                new FileEditorManagerListener() {
                    @Override
                    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                        Editor editor = source.getSelectedTextEditor();
                        if (editor != null) {
                            addDocumentListenerToEditor(editor);
                        }
                    }

                    private void addDocumentListenerToEditor(Editor editor) {
                        Document document = editor.getDocument();
                        document.addDocumentListener(new DocumentListener() {
                            @Override
                            public void documentChanged(@NotNull DocumentEvent event) {
                                updateLogListEntries();
                            }
                        });
                    }

                    @Override
                    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                        updateLogListEntries();
                    }
                });
    }

    /**
     * 导航到选中表达式
     * @param selectedInfo 选择的值
     */
    private void navigateToConsoleLog(ConsoleLogSearchInfo selectedInfo) {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            return;
        }
        Document document = editor.getDocument();
        jumpAndHighlight(editor, new TextRange(
                        document.getLineStartOffset(selectedInfo.getLine()),
                        document.getLineEndOffset(selectedInfo.getLine())
                    )
        );
    }

    /**
     * 光标跳转并高亮
     * @param editor 编辑器对象
     * @param textRange 文本范围
     */
    private void jumpAndHighlight(Editor editor, TextRange textRange) {
        // 移动光标到文本末尾
        editor.getCaretModel().moveToOffset(textRange.getEndOffset());
        editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
    }

    private void updateLogListEntries() {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) return;

        setConsoleLogs(editor.getDocument());
    }

    private void setConsoleLogs(Document document) {
        model.clear();

        String context = document.getText();
        Matcher matcher;
        if (specCheckBox.isSelected()) {
            // 只查找插件指定格式的表达式
            Pattern pattern = Pattern.compile(
                    TextFormatContext.CONSOLE_LOG_BEGIN_REGEX_WITHOUT_START_SPACE +
                            SettingConstant.ALL_REGEX +
                            TextFormatContext.CONSOLE_LOG_END_REGEX
            );
            matcher = pattern.matcher(context);
        } else {
            // 查找所有控制台打印表达式
            Pattern beginRegex = Pattern.compile(SettingConstant.CONSOLE_LOG_BEGIN_REGEX_WITHOUT_START_SPACE);
            matcher = beginRegex.matcher(context);
        }

        while (matcher.find()) {
            // 打印表达式起始位置
            int start = matcher.start();
            // 打印表达式所在行号
            int lineNumber = document.getLineNumber(start);
            // 检查是否排除注释项
            if (!commentCheckBox.isSelected()) {
                if (document.getText(new TextRange(document.getLineStartOffset(lineNumber), start)).contains(SettingConstant.COMMENT_SIGNAL)) {
                    continue;
                }
            }
            // 打印表达式所在行末尾位置
            int end = document.getLineEndOffset(lineNumber);
            String searchText = context.substring(start, end).replaceAll("\n", "").trim();
            ConsoleLogSearchInfo searchInfo = new ConsoleLogSearchInfo(searchText, lineNumber);
            model.addElement(searchInfo);
        }
    }

    public JComponent getComponent() {
        return panel;
    }


}
