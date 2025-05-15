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
     * 是否使用双引号
     */
    public Boolean isDoubleQuote = true;
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

    @Override
    public @NotNull ConsoleLogSettingState getState() {
        ConsoleLogSettingState state = new ConsoleLogSettingState();
        // 对 consoleLogMsg 进行 Base64 编码后再赋值
        state.consoleLogMsg = Base64Util.encode(this.consoleLogMsg);
        state.autoFollowEnd = this.autoFollowEnd;
        state.isDoubleQuote = this.isDoubleQuote;
        state.deleteInSelection = this.deleteInSelection;
        state.commentInSelection = this.commentInSelection;
        state.unCommentSelection = this.unCommentSelection;
        state.variableLineNumber = this.variableLineNumber;
        state.fileSuffix = this.fileSuffix;
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
        this.isDoubleQuote = state.isDoubleQuote;
        this.deleteInSelection = state.deleteInSelection;
        this.commentInSelection = state.commentInSelection;
        this.unCommentSelection = state.unCommentSelection;
        this.variableLineNumber = state.variableLineNumber;
        this.fileSuffix = state.fileSuffix;

        ConsoleLogConfigurable.finalSetting(this, null);
    }
}
