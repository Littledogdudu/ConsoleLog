package com.sky.consolelog.setting.ui;

import com.intellij.openapi.Disposable;
import com.sky.consolelog.constant.SettingConstant;

import javax.swing.*;
import java.awt.event.ActionEvent;
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
    private JCheckBox isDoubleQuote;

    /** 清空按钮监听器 */
    private final ActionListener resetButtonActionListener = event -> {
        setConsoleLogMsg(SettingConstant.DEFAULT_CONSOLE_LOG_MSG);
    };

    public ConsoleLogComponent() {
        resetButton.addActionListener(resetButtonActionListener);
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

    @Override
    public void dispose() {
        resetButton.removeActionListener(resetButtonActionListener);
    }
}
