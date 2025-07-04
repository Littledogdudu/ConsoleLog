package com.sky.consolelog.setting.ui;

import com.intellij.openapi.Disposable;
import com.sky.consolelog.constant.SettingConstant;
import com.sky.consolelog.search.ui.BeautifulListCellRender;
import com.sky.consolelog.search.ui.ConsoleLogToolWindowComponent;
import org.apache.commons.collections.CollectionUtils;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * ConsoleLog设置的UI组件
 *
 * @author SkySource
 * @Date: 2025/1/24 21:29
 */
public class ConsoleLogComponent implements Disposable {
    private JPanel jPanel;
    private JTextField consoleLogMsgInput;
    private JButton resetButton;
    private JLabel signal;
    private JLabel description;
    private JLabel method;
    private JLabel variableLabel;
    private JLabel methodLabel;
    private JLabel variable;
    private JCheckBox autoFollowEndCheckBox;
    private JCheckBox deleteInSelectionCheckBox;
    private JCheckBox commentInSelectionCheckBox;
    private JCheckBox unCommentSelectionCheckBox;
    private JCheckBox variableLineNumberCheckBox;
    private JCheckBox fileSuffixCheckBox;
    private JRadioButton singleQuoteRadioButton;
    private JRadioButton doubleQuoteRadioButton;
    private JRadioButton backTickRadioButton;
    private JPanel basic;
    private JPanel format;
    private JPanel component;
    private JCheckBox enableSideWindow;
    private JCheckBox fileTypeAllInCheckBox;
    private JCheckBox vueSideCheckBox;
    private JCheckBox javaScriptSideCheckBox;
    private JCheckBox typeScriptSideCheckBox;
    private JCheckBox textSideCheckBox;
    private JComboBox<Integer> sideFontSize;
    private JTextField tagTextField;
    private JCheckBox defaultTagSearchCheckBox;

    /** 清空按钮监听器 */
    private final ActionListener resetButtonActionListener = e -> setConsoleLogMsg(SettingConstant.DEFAULT_CONSOLE_LOG_MSG);
    /** 是否启用侧边栏按钮监听器 */
    private final ActionListener enableSideWindowActionListener = e -> setEnableSideWindowStatus();
    /** 侧边栏查找不限定语言类型按钮监听器 */
    private final ActionListener fileTypeAllInCheckBoxActionListener = e -> setLanguageCheckBoxStatus();

    public ConsoleLogComponent() {
        resetButton.addActionListener(resetButtonActionListener);

        // 字符串引号单选选项组
        ButtonGroup signalRadioGroup = new ButtonGroup();
        signalRadioGroup.add(singleQuoteRadioButton);
        signalRadioGroup.add(doubleQuoteRadioButton);
        signalRadioGroup.add(backTickRadioButton);

        enableSideWindow.addActionListener(enableSideWindowActionListener);
        fileTypeAllInCheckBox.addActionListener(fileTypeAllInCheckBoxActionListener);
        
        setSideFontSizeOptions();
        setDefaultSideFontSize();
    }

    /**
     * 【是否启用侧边栏】按钮改变事件方法
     */
    public void setEnableSideWindowStatus() {
        if (getEnableSideWindow()) {
            fileTypeAllInCheckBox.setEnabled(true);
            setLanguageCheckBoxStatus();
            sideFontSize.setEnabled(true);
        } else {
            fileTypeAllInCheckBox.setEnabled(false);
            vueSideCheckBox.setEnabled(false);
            javaScriptSideCheckBox.setEnabled(false);
            typeScriptSideCheckBox.setEnabled(false);
            textSideCheckBox.setEnabled(false);
            sideFontSize.setEnabled(false);
        }
    }

    /**
     * 【文件类型限定】按钮改变事件方法
     */
    public void setLanguageCheckBoxStatus() {
        if (fileTypeAllInCheckBox.isSelected()) {
            // 开启全文件
            vueSideCheckBox.setEnabled(false);
            javaScriptSideCheckBox.setEnabled(false);
            typeScriptSideCheckBox.setEnabled(false);
            textSideCheckBox.setEnabled(false);
        } else {
            vueSideCheckBox.setEnabled(true);
            javaScriptSideCheckBox.setEnabled(true);
            typeScriptSideCheckBox.setEnabled(true);
            textSideCheckBox.setEnabled(true);
        }
    }

    /**
     * 侧边栏字体大小下拉框选项
     */
    private void setSideFontSizeOptions() {
        for (int size = 8; size <= 72; size += 2) {
            sideFontSize.addItem(size);
        }
    }

    /**
     * 尝试重新渲染侧边栏字体大小
     */
    public void setDefaultSideFontSize() {
        if (getEnableSideWindow()) {
            // 重新渲染侧边栏样式
            ConsoleLogToolWindowComponent.setLogListCellRender(new BeautifulListCellRender());
        }
    }

    public JPanel getPanel() {
        return this.jPanel;
    }

    public String getConsoleLogMsg() {
        return consoleLogMsgInput.getText();
    }

    public void setConsoleLogMsg(String msg) {
        consoleLogMsgInput.setText(msg);
    }

    public Boolean getAutoFollowEndCheckBox() {
        return autoFollowEndCheckBox.isSelected();
    }

    public void setAutoFollowEndCheckBox(Boolean checked) {
        autoFollowEndCheckBox.setSelected(checked);
    }

    public Boolean getSingleQuoteRadioButton() {
        return singleQuoteRadioButton.isSelected();
    }

    public void setSingleQuoteRadioButton(Boolean checked) {
        singleQuoteRadioButton.setSelected(checked);
    }

    public Boolean getDoubleQuoteRadioButton() {
        return doubleQuoteRadioButton.isSelected();
    }

    public void setDoubleQuoteRadioButton(Boolean checked) {
        doubleQuoteRadioButton.setSelected(checked);
    }

    public Boolean getBackTickRadioButton() {
        return backTickRadioButton.isSelected();
    }

    public void setBackTickRadioButton(Boolean checked) {
        backTickRadioButton.setSelected(checked);
    }

    public Boolean getDeleteInSelectionCheckBox() {
        return deleteInSelectionCheckBox.isSelected();
    }

    public void setDeleteInSelectionCheckBox(Boolean checked) {
        this.deleteInSelectionCheckBox.setSelected(checked);
    }

    public Boolean getCommentInSelectionCheckBox() {
        return commentInSelectionCheckBox.isSelected();
    }

    public void setCommentInSelectionCheckBox(Boolean checked) {
        this.commentInSelectionCheckBox.setSelected(checked);
    }

    public Boolean getUnCommentSelectionCheckBox() {
        return unCommentSelectionCheckBox.isSelected();
    }

    public void setUnCommentSelectionCheckBox(Boolean checked) {
        this.unCommentSelectionCheckBox.setSelected(checked);
    }

    public Boolean getVariableLineNumberCheckBox() {
        return variableLineNumberCheckBox.isSelected();
    }

    public void setVariableLineNumberCheckBox(Boolean checked) {
        this.variableLineNumberCheckBox.setSelected(checked);
    }

    public Boolean getFileSuffixCheckBox() {
        return fileSuffixCheckBox.isSelected();
    }

    public void setFileSuffixCheckBox(Boolean checked) {
        this.fileSuffixCheckBox.setSelected(checked);
    }

    public Boolean getEnableSideWindow() {
        return enableSideWindow.isSelected();
    }

    public void setEnableSideWindow(Boolean checked) {
        enableSideWindow.setSelected(checked);
    }

    public Boolean getTextSideCheckBox() {
        return textSideCheckBox.isSelected();
    }

    public void setTextSideCheckBox(Boolean checked) {
        textSideCheckBox.setSelected(checked);
    }

    public Boolean getTypeScriptSideCheckBox() {
        return typeScriptSideCheckBox.isSelected();
    }

    public void setTypeScriptSideCheckBox(Boolean checked) {
        typeScriptSideCheckBox.setSelected(checked);
    }

    public Boolean getJavaScriptSideCheckBox() {
        return javaScriptSideCheckBox.isSelected();
    }

    public void setJavaScriptSideCheckBox(Boolean checked) {
        javaScriptSideCheckBox.setSelected(checked);
    }

    public Boolean getVueSideCheckBox() {
        return vueSideCheckBox.isSelected();
    }

    public void setVueSideCheckBox(Boolean checked) {
        vueSideCheckBox.setSelected(checked);
    }

    public Boolean getFileTypeAllInCheckBox() {
        return fileTypeAllInCheckBox.isSelected();
    }

    public void setFileTypeAllInCheckBox(Boolean checked) {
        fileTypeAllInCheckBox.setSelected(checked);
    }

    public Integer getSideFontSize() {
        return (Integer) sideFontSize.getSelectedItem();
    }

    public void setSideFontSize(Integer size) {
        sideFontSize.setSelectedItem(size);
    }

    public String getTextTags() {
        return tagTextField.getText();
    }

    public void setTextTags(List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            tagTextField.setText("");
        }
        tagTextField.setText(String.join(SettingConstant.TAGS_DELIMITER, tags));
    }

    public Boolean getDefaultTagSearchCheckBox() {
        return defaultTagSearchCheckBox.isSelected();
    }

    public void setDefaultTagSearchCheckBox(Boolean checked) {
        defaultTagSearchCheckBox.setSelected(checked);
    }

    @Override
    public void dispose() {
        resetButton.removeActionListener(resetButtonActionListener);
        enableSideWindow.removeActionListener(enableSideWindowActionListener);
        fileTypeAllInCheckBox.removeActionListener(fileTypeAllInCheckBoxActionListener);
    }
}
