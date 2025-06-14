package com.sky.consolelog.setting.storage;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.sky.consolelog.config.ConsoleLogConfigurable;
import com.sky.consolelog.constant.SettingConstant;
import com.sky.consolelog.utils.Base64Util;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    /** 侧边栏字体大小 */
    public Integer sideFontSize = UIManager.getFont("Label.font").getSize();
    /** 首次启动侧边栏默认启用标签查询 */
    public Boolean defaultTagSearch = false;
    /** 侧边栏标签查找项 */
    public List<String> tags = Arrays.asList(
            "<template>", "</template>",
            "<script.*?>", "</script>",
            "<style.*?>", "</style>"
    );

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
        state.enableSideWindow = this.enableSideWindow;
        state.fileTypeAllIn = this.fileTypeAllIn;
        state.vueSide = this.vueSide;
        state.javaScriptSide = this.javaScriptSide;
        state.typeScriptSide = this.typeScriptSide;
        state.textSide = this.textSide;
        state.sideFontSize = this.sideFontSize;
        state.tags = this.tags;
        state.defaultTagSearch = this.defaultTagSearch;
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
        this.enableSideWindow = state.enableSideWindow;
        this.fileTypeAllIn = state.fileTypeAllIn;
        this.vueSide = state.vueSide;
        this.javaScriptSide = state.javaScriptSide;
        this.typeScriptSide = state.typeScriptSide;
        this.textSide = state.textSide;
        this.sideFontSize = state.sideFontSize;
        this.tags = state.tags;
        this.defaultTagSearch = state.defaultTagSearch;

        ConsoleLogConfigurable.finalSetting(this, null);
    }
}
