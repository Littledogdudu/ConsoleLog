package com.sky.consolelog.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.NlsContexts;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
import com.sky.consolelog.setting.ui.ConsoleLogComponent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 插件配置
 *
 * @author SkySource
 * @Date: 2025/1/24 21:36
 */
public class ConsoleLogConfigurable implements Configurable {
    private ConsoleLogComponent component;
    private final ConsoleLogSettingState settings = ApplicationManager.getApplication().getService(ConsoleLogSettingState.class);

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Console Log Plugin";
    }

    @Override
    public @Nullable JComponent createComponent() {
        component = new ConsoleLogComponent();
        return component.getPanel();
    }

    @Override
    public boolean isModified() {
        return !component.getConsoleLogMsg().equals(this.settings.consoleLogMsg)
               || !component.getAutoFollowEndCheckBox().equals(this.settings.autoFollowEnd);
    }

    /**
     * 应用：把设置输入框数据写入到配置文件
     */
    @Override
    public void apply() {
        this.settings.consoleLogMsg = component.getConsoleLogMsg();
        this.settings.autoFollowEnd = component.getAutoFollowEndCheckBox();
    }

    /**
     * 重置：把配置文件的旧设置重置到设置输入框
     */
    @Override
    public void reset() {
        component.setConsoleLogMsg(this.settings.consoleLogMsg);
        component.setAutoFollowEndCheckBox(this.settings.autoFollowEnd);
    }
}
