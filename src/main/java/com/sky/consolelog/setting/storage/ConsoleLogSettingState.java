package com.sky.consolelog.setting.storage;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.sky.consolelog.config.ConsoleLogConfigurable;
import com.sky.consolelog.constant.SettingConstant;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * consoleLog持久化设置实体类
 *
 * @author SkySource
 * @Date: 2025/1/24 21:24
 */
@State(
        name = "com.sky.consolelog.idea.plugin.setting.storage.ConsoleLogSettingState",
        storages = @Storage("console-log-setting.xml")
)
@Service(value = Service.Level.APP)
public final class ConsoleLogSettingState implements PersistentStateComponent<ConsoleLogSettingState> {
    /**
     * 默认打印语句
     */
    public String consoleLogMsg = SettingConstant.DEFAULT_CONSOLE_LOG_MSG;
    /**
     * 插入后光标是否自动跟随到log表达式末尾
     */
    public Boolean autoFollowEnd = true;
    /**
     * 是否使用双引号
     */
    public Boolean isDoubleQuote = true;

    /**
     * 是否启用侧边栏（重启生效）
     */
    public Boolean enableSideWindow = true;

    /**
     * 侧边栏查找不限定文件类型
     */
    public Boolean fileTypeAllIn = true;
    /**
     * 启用侧边栏文件类型：Vue
     */
    public Boolean vueSide = false;
    /**
     * 启用侧边栏文件类型：JavaScript
     */
    public Boolean javaScriptSide = false;
    /**
     * 启用侧边栏文件类型：TypeScript
     */
    public Boolean typeScriptSide = false;
    /**
     * 启用侧边栏文件类型：text
     */
    public Boolean textSide = false;
    public static List<String> fileTypeList = new ArrayList<>(6);

    @Override
    public @NotNull ConsoleLogSettingState getState() {
        return this;
    }

    @Override
    public void noStateLoaded() {
        ConsoleLogConfigurable.finalSetting(this, null);
    }

    @Override
    public void loadState(@NotNull ConsoleLogSettingState state) {
        this.consoleLogMsg = state.consoleLogMsg;
        this.autoFollowEnd = state.autoFollowEnd;
        this.isDoubleQuote = state.isDoubleQuote;
        this.enableSideWindow = state.enableSideWindow;
        this.fileTypeAllIn = state.fileTypeAllIn;
        this.vueSide = state.vueSide;
        this.javaScriptSide = state.javaScriptSide;
        this.typeScriptSide = state.typeScriptSide;
        this.textSide = state.textSide;
        ConsoleLogConfigurable.finalSetting(this, null);
    }
}
