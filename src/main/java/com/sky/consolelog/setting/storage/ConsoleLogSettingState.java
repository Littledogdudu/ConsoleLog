package com.sky.consolelog.setting.storage;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.sky.consolelog.config.ConsoleLogConfigurable;
import com.sky.consolelog.constant.SettingConstant;
import org.jetbrains.annotations.NotNull;

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
     * 是否仅在选中区域内删除
     */
    public Boolean deleteInSelectionCheckBox = true;
    /**
     * 是否仅在选中区域内注释
     */
    public Boolean commentInSelectionCheckBox = true;
    /**
     * 是否仅在选中区域内取消注释
     */
    public Boolean unCommentSelectionCheckBox = true;

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
        this.deleteInSelectionCheckBox = state.deleteInSelectionCheckBox;
        this.commentInSelectionCheckBox = state.commentInSelectionCheckBox;
        this.unCommentSelectionCheckBox = state.unCommentSelectionCheckBox;

        ConsoleLogConfigurable.finalSetting(this, null);
    }
}
