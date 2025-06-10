package com.sky.consolelog.search.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.UIUtil;
import com.sky.consolelog.constant.SettingConstant;
import com.sky.consolelog.entities.ConsoleLogSearchInfo;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
import com.sky.consolelog.utils.ConsoleLogMsgUtil;
import com.sky.consolelog.utils.TextFormatContext;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 侧边栏UI组件
 *
 * @author SkySource
 * @Date: 2025/3/21 20:51
 */
public class ConsoleLogToolWindowComponent implements Disposable {
    private final JPanel panel;
    private final JLabel tip;
    /** 显示注释项 */
    private final JBCheckBox commentCheckBox;
    /** 启用针对性查找 */
    private final JBCheckBox specCheckBox;
    /** 开启标签查找 */
    private final JBCheckBox levelCheckBox;
    private static JBList<ConsoleLogSearchInfo> logList;
    private final DefaultListModel<ConsoleLogSearchInfo> model;
    private final Project project;

    private Document currentDocument = null;
    private DocumentListener currentDocumentListener = null;
    private final ActionListener actionListener = event -> {
        updateLogListEntries();
    };
    private final MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            ConsoleLogSearchInfo selectInfo = logList.getSelectedValue();
            if (selectInfo != null) {
                navigateToConsoleLog(selectInfo);
            }
        }
    };

    public ConsoleLogToolWindowComponent(Project project) {
        this.project = project;
        panel = new JPanel(new BorderLayout());
        tip = new JLabel("");
        commentCheckBox = new JBCheckBox();
        specCheckBox = new JBCheckBox();
        levelCheckBox = new JBCheckBox();
        model = new DefaultListModel<>();
        logList = new JBList<>(model);

        tip.setOpaque(false);
        tip.setFont(UIUtil.getLabelFont(UIUtil.FontSize.SMALL));
        tip.setForeground(JBColor.GRAY);
        commentCheckBox.setText("展示注释项");
        specCheckBox.setText("启用针对性查找");
        levelCheckBox.setText("启用标签查找");
        Box topBox = Box.createHorizontalBox();
        topBox.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        topBox.add(tip);
        topBox.add(commentCheckBox);
        topBox.add(specCheckBox);
        topBox.add(levelCheckBox);

        JSeparator separator = new JSeparator();

        // 触发快速查找
        setLogListCellRender(new BeautifulListCellRender());

        panel.add(topBox, BorderLayout.NORTH);
        panel.add(separator, BorderLayout.CENTER);
        panel.add(new JScrollPane(logList), BorderLayout.CENTER);

        // 初始化console.log表达式列表
        updateLogListEntries();
        // 初始化文件监听器
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor != null) {
            addDocumentListener(editor.getDocument());
        }

        // 监听【是否启用针对性查找】按钮
        specCheckBox.addActionListener(actionListener);
        commentCheckBox.addActionListener(actionListener);
        levelCheckBox.addActionListener(actionListener);
        // 监听鼠标点击--到达表达式位置
        logList.addMouseListener(mouseAdapter);
        // 添加文件切换监听器
        project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                Editor editor = source.getSelectedTextEditor();
                if (editor != null) {
                    addDocumentListener(editor.getDocument());
                }
            }

            @Override
            public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                removeCurrentDocumentListener();
            }

            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                Editor editor = event.getManager().getSelectedTextEditor();
                if (editor != null) {
                    Document document = editor.getDocument();
                    if (currentDocument != null && !currentDocument.equals(document)) {
                        removeCurrentDocumentListener();
                    }
                    addDocumentListener(document);
                } else {
                    removeCurrentDocumentListener();
                }
            }
        });
    }

    private void addDocumentListener(Document document) {
        updateLogListEntries();
        // 避免重复添加
        if (currentDocumentListener != null) return;

        currentDocumentListener = new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                updateLogListEntries();
            }
        };
        document.addDocumentListener(currentDocumentListener);
        currentDocument = document;
    }

    private void removeCurrentDocumentListener() {
        if (currentDocumentListener != null && currentDocument != null) {
            currentDocument.removeDocumentListener(currentDocumentListener);
            currentDocumentListener = null;
            currentDocument = null;
        }
    }

    /**
     * 导航到选中表达式
     *
     * @param selectedInfo 选择的值
     */
    private void navigateToConsoleLog(ConsoleLogSearchInfo selectedInfo) {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            return;
        }
        editor.getCaretModel().moveToOffset(selectedInfo.getEndOffset());
        editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
    }

    private void updateLogListEntries() {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) return;
        ConsoleLogSettingState settings = ApplicationManager.getApplication().getService(ConsoleLogSettingState.class);
        if (!checkFileType(editor, settings)) {
            model.clear();
            commentCheckBox.setEnabled(false);
            specCheckBox.setEnabled(false);
            tip.setText("当前类型文件不在查询范围内");
            return;
        }
        commentCheckBox.setEnabled(true);
        specCheckBox.setEnabled(true);
        tip.setText("");

        setConsoleLogs(editor.getDocument(), settings);
    }

    private boolean checkFileType(Editor editor, ConsoleLogSettingState settings) {
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        String fileTypeName = "";
        if (virtualFile != null) {
            fileTypeName = virtualFile.getFileType().getName();
        }
        if (settings.fileTypeAllIn) {
            return true;
        } else {
            return ConsoleLogSettingState.fileTypeList.contains(fileTypeName);
        }
    }

    private void setConsoleLogs(Document document, ConsoleLogSettingState settings) {
        model.clear();

        boolean enableSpec = specCheckBox.isSelected();
        boolean enableComment = commentCheckBox.isSelected();
        boolean enableLevel = levelCheckBox.isSelected();

        String context = document.getText();
        Matcher matcher = null;
        StringBuilder regexBuilder = new StringBuilder("(");
        if (enableSpec) {
            // 启用只查找插件指定格式的表达式
            String consoleLogMsgRegex = ConsoleLogMsgUtil.buildRegexConsoleLogMsg(settings, TextFormatContext.CONSOLE_LOG_BEGIN_REGEX_WITHOUT_START_SPACE, TextFormatContext.CONSOLE_LOG_END_REGEX);
            if (consoleLogMsgRegex == null) {
                return;
            }
            regexBuilder.append(consoleLogMsgRegex);
        } else {
            // 查找所有控制台打印表达式
            regexBuilder.append(SettingConstant.CONSOLE_LOG_BEGIN_REGEX_WITHOUT_START_SPACE);
        }

        regexBuilder.append(")");
        if (enableLevel) {
            // 启用级别
            settings.tags.stream().filter(StringUtils::isNotEmpty).forEach(tag -> regexBuilder.append("|(").append(tag).append(")"));
        }
        Pattern pattern = Pattern.compile(regexBuilder.toString());
        matcher = pattern.matcher(context);

        while (matcher.find()) {
            // 打印表达式起始位置
            int start = matcher.start();
            // 打印表达式所在行号
            int lineNumber = document.getLineNumber(start);
            // 检查是否排除注释项
            if (!enableComment) {
                // 启用排除注释项
                if (document.getText(new TextRange(document.getLineStartOffset(lineNumber), start)).contains(SettingConstant.COMMENT_SIGNAL)) {
                    continue;
                }
            } else {
                // 不排除注释项
                int lineStart = document.getLineStartOffset(lineNumber);
                if (document.getText(new TextRange(lineStart, start)).contains(SettingConstant.COMMENT_SIGNAL)) {
                    start = lineStart;
                }
            }
            // 打印表达式所在行末尾位置
            int end = document.getLineEndOffset(lineNumber);
            String searchText = context.substring(start, end).replaceAll("\n", "").trim();
            int level = 0;
            if (enableLevel && StringUtils.isNotEmpty(matcher.group(1))) {
                // 启用级别
                level = 1;
            }
            ConsoleLogSearchInfo searchInfo = new ConsoleLogSearchInfo(searchText, lineNumber, end, level);
            model.addElement(searchInfo);
        }
    }

    public static void setLogListCellRender(ListCellRenderer<ConsoleLogSearchInfo> render) {
        if (Objects.isNull(logList)) {
            return;
        }
        logList.setCellRenderer(render);
        logList.repaint();
    }

    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void dispose() {
        removeCurrentDocumentListener();
        specCheckBox.removeActionListener(actionListener);
        commentCheckBox.removeActionListener(actionListener);
        logList.removeMouseListener(mouseAdapter);

        project.dispose();
    }
}
