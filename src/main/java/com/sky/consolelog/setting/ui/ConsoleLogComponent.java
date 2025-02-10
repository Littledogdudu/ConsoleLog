package com.sky.consolelog.setting.ui;

import com.sky.consolelog.constant.SettingConstant;

import javax.swing.*;

/**
 * @author by: SkySource
 * @Description:
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
}
