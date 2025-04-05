package com.sky.consolelog.setting.ui;

import com.sky.consolelog.constant.SettingConstant;

import javax.swing.*;

/**
 * ConsoleLog设置的UI组件
 *
 * @author SkySource
 * @Date: 2025/1/24 21:29
 */
public class ConsoleLogComponent {
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
    private JCheckBox isDoubleQuote;

    public ConsoleLogComponent() {
        resetButton.addActionListener(e -> {
            setConsoleLogMsg(SettingConstant.DEFAULT_CONSOLE_LOG_MSG);
        });
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

    public Boolean getIsDoubleQuote() {
        return isDoubleQuote.isSelected();
    }

    public void setIsDoubleQuote(Boolean checked) {
        isDoubleQuote.setSelected(checked);
    }
}
