package com.sky.consolelog.config;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.NlsContexts;
import com.sky.consolelog.constant.SettingConstant;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
import com.sky.consolelog.setting.ui.ConsoleLogComponent;
import com.sky.consolelog.utils.TextFormatContext;
import com.sky.consolelog.utils.FileTypeUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.Objects;

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
                || !component.getFileSuffixCheckBox().equals(this.settings.fileSuffix)
                || !component.getEnableDefaultConsoleLogMsg().equals(this.settings.enableDefaultConsoleLogMsg)
                || !component.getDefaultConsoleLogMsg().equals(this.settings.defaultConsoleLogMsg)
                || !component.getDefaultAutoFollowEndCheckBox().equals(this.settings.defaultAutoFollowEnd)
                || !component.getEnableAutoFixLineNumber().equals(this.settings.enableAutoFixLineNumber)
                || !component.getFileSuffixCheckBox().equals(this.settings.fileSuffix)
                || !component.getEnableSideWindow().equals(this.settings.enableSideWindow)
                || !component.getFileTypeAllInCheckBox().equals(this.settings.fileTypeAllIn)
                || !component.getVueSideCheckBox().equals(this.settings.vueSide)
                || !component.getJavaScriptSideCheckBox().equals(this.settings.javaScriptSide)
                || !component.getTypeScriptSideCheckBox().equals(this.settings.typeScriptSide)
                || !component.getTextSideCheckBox().equals(this.settings.textSide)
                || !component.getSideFontSize().equals(this.settings.sideFontSize)
                || !Arrays.stream(component.getTextTags().split(SettingConstant.TAGS_DELIMITER)).toList().equals(this.settings.tags)
                || !component.getDefaultTagSearchCheckBox().equals(this.settings.defaultTagSearch)
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
        this.settings.enableDefaultConsoleLogMsg = component.getEnableDefaultConsoleLogMsg();
        this.settings.defaultConsoleLogMsg = component.getDefaultConsoleLogMsg();
        this.settings.defaultAutoFollowEnd = component.getDefaultAutoFollowEndCheckBox();
        this.settings.enableAutoFixLineNumber = component.getEnableAutoFixLineNumber();
        this.settings.enableSideWindow = component.getEnableSideWindow();
        // 是否启用侧边栏（重启生效）
        this.settings.fileTypeAllIn = component.getFileTypeAllInCheckBox();
        // 文件类型
        this.settings.vueSide = component.getVueSideCheckBox();
        this.settings.javaScriptSide = component.getJavaScriptSideCheckBox();
        this.settings.typeScriptSide = component.getTypeScriptSideCheckBox();
        this.settings.textSide = component.getTextSideCheckBox();
        this.settings.sideFontSize = component.getSideFontSize();
        this.settings.tags = Arrays.stream(component.getTextTags().split(SettingConstant.TAGS_DELIMITER)).toList();
        this.settings.defaultTagSearch = component.getDefaultTagSearchCheckBox();

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
        component.setEnableDefaultConsoleLogMsg(this.settings.enableDefaultConsoleLogMsg);
        component.setDefaultConsoleLogMsg(this.settings.defaultConsoleLogMsg);
        component.setDefaultAutoFollowEndCheckBox(this.settings.defaultAutoFollowEnd);
        component.setEnableAutoFixLineNumber(this.settings.enableAutoFixLineNumber);
        component.setEnableSideWindow(this.settings.enableSideWindow);
        component.setFileTypeAllInCheckBox(this.settings.fileTypeAllIn);
        component.setVueSideCheckBox(this.settings.vueSide);
        component.setJavaScriptSideCheckBox(this.settings.javaScriptSide);
        component.setTypeScriptSideCheckBox(this.settings.typeScriptSide);
        component.setTextSideCheckBox(this.settings.textSide);
        component.setSideFontSize(this.settings.sideFontSize);
        component.setTextTags(this.settings.tags);
        component.setDefaultTagSearchCheckBox(this.settings.defaultTagSearch);

        finalSetting(settings, component);
    }

    public static void finalSetting(ConsoleLogSettingState settings, ConsoleLogComponent component) {
        if (Objects.nonNull(settings)) {
            // 更新TextFormatContext的CONSOLE常量
            TextFormatContext.INSTANCE.setTextFormatStrategyByProjectSetting(settings);
            // 更新允许的文件类型
            FileTypeUtil.setSettingFileTypeList(settings);
        }
        if (Objects.nonNull(component)) {
            component.enableDefaultConsoleLogMsgEvent();
            // 设置禁用关系
            component.setLanguageCheckBoxStatus();
            component.setEnableSideWindowStatus();
            // 如果没有配置字体大小，则设置默认
            component.setDefaultSideFontSize();
        }
    }
}
