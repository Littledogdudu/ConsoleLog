package com.sky.consolelog.setting.ui;

import com.intellij.openapi.Disposable;
import com.sky.consolelog.constant.SettingConstant;

import javax.swing.*;
import java.awt.event.ActionListener;

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
    private JCheckBox enableDefaultConsoleLogMsg;
    private JTextField defaultConsoleLogMsg;
    private JButton resetButton2;
    private JCheckBox defaultAutoFollowEndCheckBox;

    /** 清空按钮监听器 */
    private final ActionListener resetButtonActionListener = event -> {
        setConsoleLogMsg(SettingConstant.DEFAULT_CONSOLE_LOG_MSG);
    };

    /** 重置按钮监听器 */
    private final ActionListener resetButtonActionListener2 = event -> {
        setConsoleLogMsg2(SettingConstant.DEFAULT_CONSOLE_LOG_MSG_WITHOUT_VARIABLE);
    };

    /** 是否禁用光标处无可打印变量时的标记打印功能 */
    private final ActionListener enableDefaultConsoleLogMsgEventListener = event -> {
        enableDefaultConsoleLogMsgEvent();
    };

    public ConsoleLogComponent() {
        resetButton.addActionListener(resetButtonActionListener);
        resetButton2.addActionListener(resetButtonActionListener2);
        ButtonGroup signalRadioGroup = new ButtonGroup();
        signalRadioGroup.add(singleQuoteRadioButton);
        signalRadioGroup.add(doubleQuoteRadioButton);
        signalRadioGroup.add(backTickRadioButton);
        enableDefaultConsoleLogMsg.addActionListener(enableDefaultConsoleLogMsgEventListener);
    }

    public void enableDefaultConsoleLogMsgEvent() {
        if (getEnableDefaultConsoleLogMsg()) {
            defaultConsoleLogMsg.setEnabled(true);
            defaultAutoFollowEndCheckBox.setEnabled(true);
        } else {
            defaultConsoleLogMsg.setEnabled(false);
            defaultAutoFollowEndCheckBox.setEnabled(false);
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

    public void setConsoleLogMsg2(String msg) {
        defaultConsoleLogMsg.setText(msg);
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

    public Boolean getEnableDefaultConsoleLogMsg() {
        return enableDefaultConsoleLogMsg.isSelected();
    }

    public void setEnableDefaultConsoleLogMsg(Boolean checked) {
        this.enableDefaultConsoleLogMsg.setSelected(checked);
    }

    public String getDefaultConsoleLogMsg() {
        return defaultConsoleLogMsg.getText();
    }

    public void setDefaultConsoleLogMsg(String msg) {
        this.defaultConsoleLogMsg.setText(msg);
    }

    public Boolean getDefaultAutoFollowEndCheckBox() {
        return defaultAutoFollowEndCheckBox.isSelected();
    }

    public void setDefaultAutoFollowEndCheckBox(Boolean checked) {
        this.defaultAutoFollowEndCheckBox.setSelected(checked);
    }

    @Override
    public void dispose() {
        resetButton.removeActionListener(resetButtonActionListener);
        resetButton2.removeActionListener(resetButtonActionListener2);
        enableDefaultConsoleLogMsg.removeActionListener(enableDefaultConsoleLogMsgEventListener);
    }
}
