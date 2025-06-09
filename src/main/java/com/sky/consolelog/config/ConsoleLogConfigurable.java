package com.sky.consolelog.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.NlsContexts;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
import com.sky.consolelog.setting.ui.ConsoleLogComponent;
import com.sky.consolelog.utils.TextFormatContext;
import com.sky.consolelog.utils.FileTypeUtil;
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
                || !component.getAutoFollowEndCheckBox().equals(this.settings.autoFollowEnd)
                || !component.getSingleQuoteRadioButton().equals(this.settings.singleQuote)
                || !component.getDoubleQuoteRadioButton().equals(this.settings.doubleQuote)
                || !component.getBackTickRadioButton().equals(this.settings.backTickQuote)
                || !component.getDeleteInSelectionCheckBox().equals(this.settings.deleteInSelection)
                || !component.getCommentInSelectionCheckBox().equals(this.settings.commentInSelection)
                || !component.getUnCommentSelectionCheckBox().equals(this.settings.unCommentSelection)
                || !component.getVariableLineNumberCheckBox().equals(this.settings.variableLineNumber)
                || !component.getFileSuffixCheckBox().equals(this.settings.fileSuffix)|| !component.getEnableSideWindow().equals(this.settings.enableSideWindow)
                || !component.getFileTypeAllInCheckBox().equals(this.settings.fileTypeAllIn)
                || !component.getVueSideCheckBox().equals(this.settings.vueSide)
                || !component.getJavaScriptSideCheckBox().equals(this.settings.javaScriptSide)
                || !component.getTypeScriptSideCheckBox().equals(this.settings.typeScriptSide)
                || !component.getTextSideCheckBox().equals(this.settings.textSide)
                ;
    }

    /**
     * 应用：把设置输入框数据写入到配置文件
     */
    @Override
    public void apply() {
        this.settings.consoleLogMsg = component.getConsoleLogMsg();
        this.settings.autoFollowEnd = component.getAutoFollowEndCheckBox();
        this.settings.singleQuote = component.getSingleQuoteRadioButton();
        this.settings.doubleQuote = component.getDoubleQuoteRadioButton();
        this.settings.backTickQuote = component.getBackTickRadioButton();
        this.settings.deleteInSelection = component.getDeleteInSelectionCheckBox();
        this.settings.commentInSelection = component.getCommentInSelectionCheckBox();
        this.settings.unCommentSelection = component.getUnCommentSelectionCheckBox();
        this.settings.variableLineNumber = component.getVariableLineNumberCheckBox();
        this.settings.fileSuffix = component.getFileSuffixCheckBox();
        this.settings.enableSideWindow = component.getEnableSideWindow();
        // 是否启用侧边栏（重启生效）
        this.settings.fileTypeAllIn = component.getFileTypeAllInCheckBox();
        // 文件类型
        this.settings.vueSide = component.getVueSideCheckBox();
        this.settings.javaScriptSide = component.getJavaScriptSideCheckBox();
        this.settings.typeScriptSide = component.getTypeScriptSideCheckBox();
        this.settings.textSide = component.getTextSideCheckBox();

        finalSetting(settings, component);
    }

    /**
     * 重置：把配置文件的旧设置重置到设置输入框
     */
    @Override
    public void reset() {
        component.setConsoleLogMsg(this.settings.consoleLogMsg);
        component.setAutoFollowEndCheckBox(this.settings.autoFollowEnd);
        component.setSingleQuoteRadioButton(this.settings.singleQuote);
        component.setDoubleQuoteRadioButton(this.settings.doubleQuote);
        component.setBackTickRadioButton(this.settings.backTickQuote);
        component.setDeleteInSelectionCheckBox(this.settings.deleteInSelection);
        component.setCommentInSelectionCheckBox(this.settings.commentInSelection);
        component.setUnCommentSelectionCheckBox(this.settings.unCommentSelection);
        component.setVariableLineNumberCheckBox(this.settings.variableLineNumber);
        component.setFileSuffixCheckBox(this.settings.fileSuffix);
        component.setEnableSideWindow(this.settings.enableSideWindow);
        component.setFileTypeAllInCheckBox(this.settings.fileTypeAllIn);
        component.setVueSideCheckBox(this.settings.vueSide);
        component.setJavaScriptSideCheckBox(this.settings.javaScriptSide);
        component.setTypeScriptSideCheckBox(this.settings.typeScriptSide);
        component.setTextSideCheckBox(this.settings.textSide);

        finalSetting(settings, component);
    }

    public static void finalSetting(ConsoleLogSettingState settings, ConsoleLogComponent component) {
        if (settings != null) {
            // 更新TextFormatContext的CONSOLE常量
            TextFormatContext.INSTANCE.setTextFormatStrategyByProjectSetting(settings);
            // 更新允许的文件类型
            FileTypeUtil.setSettingFileTypeList(settings);
        }
        if (component != null) {
            // 设置禁用关系
            component.setLanguageCheckBoxStatus();
            component.setEnableSideWindowStatus();
        }
    }
}
