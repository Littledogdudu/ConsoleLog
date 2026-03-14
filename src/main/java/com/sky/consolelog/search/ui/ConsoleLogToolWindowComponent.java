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
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.UIUtil;
import com.sky.consolelog.constant.SettingConstant;
import com.sky.consolelog.entities.ConsoleLogSearchInfo;
import com.sky.consolelog.icon.ConsoleLogIcons;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
import com.sky.consolelog.setting.storage.SidebarSettingState;
import com.sky.consolelog.utils.ConsoleLogMsgUtil;
import com.sky.consolelog.utils.ConsoleLogPsiUtil;
import com.sky.consolelog.utils.MessageUtils;
import com.sky.consolelog.utils.WriterCoroutineUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
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
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JLabel tip = new JLabel("");
    /** 显示注释项 */
    private final JButton commentButton = new JButton();
    /** 启用针对性查找 */
    private final JButton specButton = new JButton();
    /** 启用无变量针对性查找 */
    private final JButton nonVarSpecButton = new JButton();
    /** 开启标签查找 */
    private final JButton levelButton = new JButton();
    /** 单击查找项 跳转/删除 */
    private final JButton jumpOrDeleteButton = new JButton();

    private static JBList<ConsoleLogSearchInfo> logList;
    private final DefaultListModel<ConsoleLogSearchInfo> model;

    private final Project project;

    SidebarSettingState settings = null;
    private Document currentDocument = null;
    private DocumentListener currentDocumentListener = null;

    private final ActionListener commentActionListener = event -> {
        updateCommentButtonIcon();
        updateCommentTipToolText();
        updateLogListEntries();
        this.settings.defaultCommentSearch = this.commentButton.isSelected();
    };

    private final ActionListener specActionListener = event -> {
        updateSpecButtonIcon();
        updateSpecTipToolText();
        updateLogListEntries();
        this.settings.defaultSpecSearch = this.specButton.isSelected();
    };

    private final ActionListener nonVarSpecActionListener = event -> {
        updateNonVarSpecButtonIcon();
        updateNonVarSpecTipToolText();
        updateLogListEntries();
        this.settings.defaultNonVarSpecSearch = this.nonVarSpecButton.isSelected();
    };

    private final ActionListener levelActionListener = event -> {
        updateLevelButtonIcon();
        updateLevelTipToolText();
        updateLogListEntries();
        this.settings.defaultTagSearch = this.levelButton.isSelected();
    };

    private final ActionListener jumpOrDeleteActionListener = event -> {
        updateJumpOrDeleteButtonIcon();
        updateJumpOrDeleteTipToolText();
        this.settings.defaultJumpOrDelete = this.jumpOrDeleteButton.isSelected();
    };

    private final MouseAdapter buttonHoverAdapter = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            JButton button = (JButton) e.getSource();
            // 设置悬停时的背景色
            button.setBackground(UIUtil.getPanelBackground().brighter());
        }

        @Override
        public void mouseExited(MouseEvent e) {
            JButton button = (JButton) e.getSource();
            // 恢复默认背景色
            button.setBackground(UIUtil.getPanelBackground());
        }
    };

    private final MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            ConsoleLogSearchInfo selectInfo = logList.getSelectedValue();
            if (selectInfo != null) {
                if (jumpOrDeleteButton.isSelected()) {
                    // 跳转
                    navigateToConsoleLog(selectInfo);
                } else {
                    // 删除
                    deleteFromConsoleLog(selectInfo);
                    updateLogListEntries();
                }
            }
        }
    };

    public ConsoleLogToolWindowComponent(Project project) {
        this.project = project;
        this.settings = project.getService(SidebarSettingState.class);

        model = new DefaultListModel<>();
        logList = new JBList<>(model);

        tip.setOpaque(false);
        tip.setFont(UIUtil.getLabelFont(UIUtil.FontSize.SMALL));
        tip.setForeground(JBColor.GRAY);

        commentButton.setSelected(settings.defaultCommentSearch);
        specButton.setSelected(settings.defaultSpecSearch);
        nonVarSpecButton.setSelected(settings.defaultNonVarSpecSearch);
        levelButton.setSelected(settings.defaultTagSearch);
        jumpOrDeleteButton.setSelected(settings.defaultJumpOrDelete);

        setIconButtonStyle(commentButton, ConsoleLogIcons.ToolWindowIcons.UnComment, ConsoleLogIcons.ToolWindowIcons.Comment);
        setIconButtonStyle(specButton, ConsoleLogIcons.ToolWindowIcons.UnSpec, ConsoleLogIcons.ToolWindowIcons.Spec);
        setIconButtonStyle(nonVarSpecButton, ConsoleLogIcons.ToolWindowIcons.UnSpec, ConsoleLogIcons.ToolWindowIcons.nonVarSpec);
        setIconButtonStyle(levelButton, ConsoleLogIcons.ToolWindowIcons.UnLevel, ConsoleLogIcons.ToolWindowIcons.Level);
        setIconButtonStyle(jumpOrDeleteButton, ConsoleLogIcons.ToolWindowIcons.Delete, ConsoleLogIcons.ToolWindowIcons.Jump);
        updateCommentTipToolText();
        updateSpecTipToolText();
        updateNonVarSpecTipToolText();
        updateLevelTipToolText();
        updateJumpOrDeleteTipToolText();

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 0));
        topPanel.add(tip);
        topPanel.add(commentButton);
        JSeparator checkBoxSeparator1 = new JSeparator(SwingConstants.VERTICAL);
        checkBoxSeparator1.setPreferredSize(new Dimension(2, 10));
        // 添加分割线
        topPanel.add(checkBoxSeparator1);
        topPanel.add(specButton);
        topPanel.add(nonVarSpecButton);
        // 添加分割线
        JSeparator checkBoxSeparator2 = new JSeparator(SwingConstants.VERTICAL);
        checkBoxSeparator2.setPreferredSize(new Dimension(2, 10));
        topPanel.add(checkBoxSeparator2);
        topPanel.add(levelButton);
        // 添加分割线
        JSeparator checkBoxSeparator3 = new JSeparator(SwingConstants.VERTICAL);
        checkBoxSeparator3.setPreferredSize(new Dimension(2, 10));
        topPanel.add(checkBoxSeparator3);
        topPanel.add(jumpOrDeleteButton);

        JSeparator separator = new JSeparator();

        // 触发快速查找
        setLogListCellRender(new BeautifulListCellRender());

        panel.add(topPanel, BorderLayout.NORTH);
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
        commentButton.addActionListener(commentActionListener);
        commentButton.addMouseListener(buttonHoverAdapter);
        specButton.addActionListener(specActionListener);
        specButton.addMouseListener(buttonHoverAdapter);
        nonVarSpecButton.addActionListener(nonVarSpecActionListener);
        nonVarSpecButton.addMouseListener(buttonHoverAdapter);
        levelButton.addActionListener(levelActionListener);
        levelButton.addMouseListener(buttonHoverAdapter);
        jumpOrDeleteButton.addActionListener(jumpOrDeleteActionListener);
        jumpOrDeleteButton.addMouseListener(buttonHoverAdapter);

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

    /**
     * 删除选中表达式
     */
    private void deleteFromConsoleLog(ConsoleLogSearchInfo selectInfo) {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        ConsoleLogSettingState settings = ApplicationManager.getApplication().getService(ConsoleLogSettingState.class);
        // editor 编辑器对象为空 或 是否删除标签设置为不删除且标签为顶级时 不做删除处理
        if (editor == null || (!settings.deleteTag && levelButton.isSelected() && 0 == selectInfo.getLevel())) {
            return;
        }

        int start = selectInfo.getStartOffset();
        int end = selectInfo.getEndOffset();
        if (0 != start) {
            String text = editor.getDocument().getText();
            start = ConsoleLogPsiUtil.getStartOffsetContainSpace(start, text);
        }
        WriterCoroutineUtils writerCoroutineUtils = ApplicationManager.getApplication().getService(WriterCoroutineUtils.class);
        writerCoroutineUtils.deleteWriterByTextRange(project, editor, new TextRange(start, end));
    }

    private void updateLogListEntries() {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) return;
        ConsoleLogSettingState settings = ApplicationManager.getApplication().getService(ConsoleLogSettingState.class);
        if (!checkFileType(editor, settings)) {
            model.clear();
            commentButton.setEnabled(false);
            specButton.setEnabled(false);
            nonVarSpecButton.setEnabled(false);
            tip.setText(MessageUtils.message("sidebar.emptyTip"));
            return;
        }
        commentButton.setEnabled(true);
        specButton.setEnabled(true);
        nonVarSpecButton.setEnabled(true);
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

        boolean enableSpec = specButton.isSelected();
        boolean enableNonVarSpec = nonVarSpecButton.isSelected();
        boolean enableComment = commentButton.isSelected();
        boolean enableLevel = levelButton.isSelected();

        String context = document.getText();
        Matcher matcher = null;
        StringBuilder regexBuilder = new StringBuilder("(");
        List<String> regexList = new ArrayList<>();
        if (enableSpec) {
            // 启用只查找插件指定格式的表达式
            String consoleLogMsgRegex = ConsoleLogMsgUtil.buildWithoutStartSpaceRegexConsoleLogMsg(settings);
            if (consoleLogMsgRegex == null) {
                return;
            }
            regexList.add(consoleLogMsgRegex);
            // console.table特殊情况
            if (settings.deleteTable) {
                regexList.add(SettingConstant.CONSOLE_TABLE_REGEX);
            }
        }
        if (enableNonVarSpec) {
            // 启用只查找插件无变量选中时生成指定格式的表达式
            String consoleLogMsgRegex = ConsoleLogMsgUtil.buildWithoutStartSpaceRegexDefaultConsoleLogMsg(settings);
            if (consoleLogMsgRegex == null) {
                return;
            }
            regexList.add(consoleLogMsgRegex);
        }

        if (CollectionUtils.isNotEmpty(regexList)) {
            regexBuilder.append(String.join("|", regexList));
        } else {
            regexBuilder.append(SettingConstant.CONSOLE_LOG_BEGIN_REGEX_WITHOUT_START_SPACE);
        }

        regexBuilder.append(")");
        if (enableLevel) {
            // 启用级别
            settings.tags.stream().filter(StringUtils::isNotEmpty).forEach(tag -> regexBuilder.append("|(").append(tag).append(")"));
        }
        Pattern pattern = Pattern.compile(regexBuilder.toString());
        matcher = pattern.matcher(context);

        // 对<template>标签进行单独处理
        boolean hasTemplateTag = false;
        Integer templateTagIndex = null;
        ConsoleLogSearchInfo endTemplateTagInfo = null;

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

            //#region 对<template标签进行单独处理
            if (searchText.contains("<template")) {
                hasTemplateTag = true;
            }
            if (hasTemplateTag && searchText.contains("</template>")) {
                templateTagIndex = model.size();
                endTemplateTagInfo = new ConsoleLogSearchInfo(searchText, lineNumber, start, end, level);
                continue;
            }
            //#endregion

            ConsoleLogSearchInfo searchInfo = new ConsoleLogSearchInfo(searchText, lineNumber, start, end, level);
            model.addElement(searchInfo);
        }
        if (endTemplateTagInfo != null) {
            model.add(templateTagIndex, endTemplateTagInfo);
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

    /**
     * 统一设置图标样式
     * @param button 按钮
     * @param icon 未选中时展示的图标
     * @param selectedIcon 选中时展示的图标
     */
    private void setIconButtonStyle(JButton button, Icon icon, Icon selectedIcon) {
        button.putClientProperty("JButton.buttonType", "toolBar");
        button.setBorderPainted(false);
        button.setIcon(icon);
        button.setSelectedIcon(selectedIcon);
        button.setPreferredSize(new Dimension(20, 20));
        button.setMinimumSize(new Dimension(20, 20));
        button.setMaximumSize(new Dimension(20, 20));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
    }

    /**
     * 更新是否显示注释项按钮图标
     */
    private void updateCommentButtonIcon() {
        boolean selected = !commentButton.isSelected();
        commentButton.setSelected(selected);
    }

    /**
     * 更新是否启用针对性查找按钮图标
     */
    private void updateSpecButtonIcon() {
        boolean selected = !specButton.isSelected();
        specButton.setSelected(selected);
    }

    /**
     * 更新是否启用无变量针对性查找按钮图标
     */
    private void updateNonVarSpecButtonIcon() {
        boolean selected = !nonVarSpecButton.isSelected();
        nonVarSpecButton.setSelected(selected);
    }

    /**
     * 更新是否启用标签查找按钮图标
     */
    private void updateLevelButtonIcon() {
        boolean selected = !levelButton.isSelected();
        levelButton.setSelected(selected);
    }

    /**
     * 更新是否启用标签查找按钮图标
     */
    private void updateJumpOrDeleteButtonIcon() {
        boolean selected = !jumpOrDeleteButton.isSelected();
        jumpOrDeleteButton.setSelected(selected);
    }

    /**
     * 更新是否显示注释项按钮提示文本
     */
    private void updateCommentTipToolText() {
        commentButton.setToolTipText(MessageUtils.message(commentButton.isSelected() ? "sidebar.commentButton" : "sidebar.disableCommentButton"));
    }

    /**
     * 更新是否启用针对性查找按钮提示文本
     */
    private void updateSpecTipToolText() {
        specButton.setToolTipText(MessageUtils.message(specButton.isSelected() ? "sidebar.specButton" : "sidebar.disableSpecButton"));
    }

    /**
     * 更新是否启用无变量针对性查找按钮提示文本
     */
    private void updateNonVarSpecTipToolText() {
        nonVarSpecButton.setToolTipText(MessageUtils.message(nonVarSpecButton.isSelected() ? "sidebar.nonVarSpecButton" : "sidebar.disableNonVarSpecButton"));
    }

    /**
     * 更新是否启用标签查找按钮提示文本
     */
    private void updateLevelTipToolText() {
        levelButton.setToolTipText(MessageUtils.message(levelButton.isSelected() ? "sidebar.levelButton" : "sidebar.disableLevelButton"));
    }

    /**
     * 更新是否点击文本项是跳转还是删除
     */
    private void updateJumpOrDeleteTipToolText() {
        jumpOrDeleteButton.setToolTipText(MessageUtils.message(jumpOrDeleteButton.isSelected() ? "sidebar.jumpOrDeleteButton" : "sidebar.disableJumpOrDeleteButton"));
    }

    @Override
    public void dispose() {
        removeCurrentDocumentListener();
        specButton.removeActionListener(specActionListener);
        specButton.removeMouseListener(buttonHoverAdapter);
        nonVarSpecButton.removeActionListener(nonVarSpecActionListener);
        nonVarSpecButton.removeMouseListener(buttonHoverAdapter);
        commentButton.removeActionListener(commentActionListener);
        commentButton.removeMouseListener(buttonHoverAdapter);
        levelButton.removeActionListener(levelActionListener);
        levelButton.removeMouseListener(buttonHoverAdapter);
        jumpOrDeleteButton.removeActionListener(jumpOrDeleteActionListener);
        jumpOrDeleteButton.removeMouseListener(buttonHoverAdapter);
        logList.removeMouseListener(mouseAdapter);

        project.dispose();
    }
}
