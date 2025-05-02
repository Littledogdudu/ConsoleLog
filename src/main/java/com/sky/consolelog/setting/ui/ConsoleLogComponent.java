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
    private JCheckBox enableSideWindow;
    private JCheckBox fileTypeAllInCheckBox;
    private JCheckBox vueSideCheckBox;
    private JCheckBox javaScriptSideCheckBox;
    private JCheckBox typeScriptSideCheckBox;
    private JCheckBox textSideCheckBox;

    /** 清空按钮监听器 */
    private final ActionListener resetButtonActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            setConsoleLogMsg(SettingConstant.DEFAULT_CONSOLE_LOG_MSG);
        }
    };
    private final ActionListener enableSideWindowActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            setEnableSideWindowStatus();
        }
    };
    private final ActionListener fileTypeAllInCheckBoxActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            setLanguageCheckBoxStatus();
        }
    };

    public ConsoleLogComponent() {
        resetButton.addActionListener(resetButtonActionListener);
        enableSideWindow.addActionListener(enableSideWindowActionListener);
        fileTypeAllInCheckBox.addActionListener(fileTypeAllInCheckBoxActionListener);
    }

    public void setEnableSideWindowStatus() {
        if (enableSideWindow.isSelected()) {
            fileTypeAllInCheckBox.setEnabled(true);
            setLanguageCheckBoxStatus();
        } else {
            fileTypeAllInCheckBox.setEnabled(false);
            vueSideCheckBox.setEnabled(false);
            javaScriptSideCheckBox.setEnabled(false);
            typeScriptSideCheckBox.setEnabled(false);
            textSideCheckBox.setEnabled(false);
        }
    }

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

    @Override
    public void dispose() {
        resetButton.removeActionListener(resetButtonActionListener);
        enableSideWindow.removeActionListener(enableSideWindowActionListener);
        fileTypeAllInCheckBox.removeActionListener(fileTypeAllInCheckBoxActionListener);
    }
}
