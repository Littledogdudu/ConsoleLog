package com.sky.consolelog.setting.storage;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.sky.consolelog.config.ConsoleLogConfigurable;
import com.sky.consolelog.constant.SettingConstant;
import com.sky.consolelog.utils.Base64Util;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * consoleLog持久化设置实体类
 *
 * @author SkySource
 * @Date: 2025/1/24 21:24
 */
@State(
        name = "com.sky.consolelog.idea.plugin.setting.storage.ConsoleLogSettingState",
        storages = @Storage(value = "console-log-setting.xml")
)
@Service(value = Service.Level.APP)
public final class ConsoleLogSettingState implements PersistentStateComponent<ConsoleLogSettingState>, Serializable {
    /**
     * 默认打印语句
     */
    public String consoleLogMsg = SettingConstant.DEFAULT_CONSOLE_LOG_MSG;
    /**
     * 插入后光标是否自动跟随到log表达式末尾
     */
    public Boolean autoFollowEnd = true;
    /**
     * 是否使用单引号
     */
    public Boolean singleQuote = false;
    /**
     * 是否使用双引号
     */
    public Boolean doubleQuote = true;
    /**
     * 是否使用反引号
     */
    public Boolean backTickQuote = false;
    /**
     * 是否仅在选中区域内删除
     */
    public Boolean deleteInSelection = true;
    /**
     * 是否仅在选中区域内注释
     */
    public Boolean commentInSelection = true;
    /**
     * 是否仅在选中区域内取消注释
     */
    public Boolean unCommentSelection = true;
    /** 行号是否使用打印变量所在的行号 */
    public Boolean variableLineNumber = false;
    /** 打印的文件名是否需要后缀名 */
    public Boolean fileSuffix = true;
    /**
     * 当没有可打印变量时生成默认插入语句内容
     */
    public Boolean enableDefaultConsoleLogMsg = false;
    /**
     * 当没有可打印变量时需要生成的默认插入语句内容
     */
    public String defaultConsoleLogMsg = SettingConstant.DEFAULT_CONSOLE_LOG_MSG_WITHOUT_VARIABLE;
    /** 无变量插入打印语句插入后是否跟随到语句末尾 */
    public Boolean defaultAutoFollowEnd = false;
    /**
     * 是否启用在插入时自动修复行号功能
     */
    public Boolean enableAutoFixLineNumber = false;

    @Override
    public @NotNull ConsoleLogSettingState getState() {
        ConsoleLogSettingState state = new ConsoleLogSettingState();
        // 对 consoleLogMsg 进行 Base64 编码后再赋值
        state.consoleLogMsg = Base64Util.encode(this.consoleLogMsg);
        state.autoFollowEnd = this.autoFollowEnd;
        state.singleQuote = this.singleQuote;
        state.doubleQuote = this.doubleQuote;
        state.backTickQuote = this.backTickQuote;
        state.deleteInSelection = this.deleteInSelection;
        state.commentInSelection = this.commentInSelection;
        state.unCommentSelection = this.unCommentSelection;
        state.variableLineNumber = this.variableLineNumber;
        state.fileSuffix = this.fileSuffix;
        state.enableDefaultConsoleLogMsg = this.enableDefaultConsoleLogMsg;
        state.defaultConsoleLogMsg = Base64Util.encode(this.defaultConsoleLogMsg);
        state.defaultAutoFollowEnd = this.defaultAutoFollowEnd;
        state.enableAutoFixLineNumber = this.enableAutoFixLineNumber;
        return state;
    }

    @Override
    public void noStateLoaded() {
        ConsoleLogConfigurable.finalSetting(this, null);
    }

    @Override
    public void loadState(@NotNull ConsoleLogSettingState state) {
        this.consoleLogMsg = Base64Util.decode(state.consoleLogMsg);
        this.autoFollowEnd = state.autoFollowEnd;
        this.singleQuote = state.singleQuote;
        this.doubleQuote = state.doubleQuote;
        this.backTickQuote = state.backTickQuote;
        this.deleteInSelection = state.deleteInSelection;
        this.commentInSelection = state.commentInSelection;
        this.unCommentSelection = state.unCommentSelection;
        this.variableLineNumber = state.variableLineNumber;
        this.fileSuffix = state.fileSuffix;
        this.enableDefaultConsoleLogMsg = state.enableDefaultConsoleLogMsg;
        this.defaultConsoleLogMsg = Base64Util.decode(state.defaultConsoleLogMsg);
        this.defaultAutoFollowEnd = state.defaultAutoFollowEnd;
        this.enableAutoFixLineNumber = state.enableAutoFixLineNumber;

        ConsoleLogConfigurable.finalSetting(this, null);
    }
}
