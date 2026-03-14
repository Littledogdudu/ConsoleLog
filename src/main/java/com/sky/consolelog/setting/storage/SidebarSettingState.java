package com.sky.consolelog.setting.storage;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * consoleLog侧边栏持久化设置实体类
 *
 * @author SkySource
 * @Date: 2026/3/14 14:27
 */
@State(
        name = "com.sky.consolelog.idea.plugin.setting.storage.SidebarSettingState",
        storages = @Storage(value = "console-log-sidebar-setting.xml")
)
@Service(value = Service.Level.PROJECT)
public final class SidebarSettingState implements PersistentStateComponent<SidebarSettingState>, Serializable {

    private final ConsoleLogSettingState settings = ApplicationManager.getApplication().getService(ConsoleLogSettingState.class);

    /** 显示注释项 */
    public Boolean defaultCommentSearch = settings.defaultCommentSearch;
    /** 启用针对性查找 */
    public Boolean defaultSpecSearch = settings.defaultSpecSearch;
    /** 启用无变量针对性查找 */
    public Boolean defaultNonVarSpecSearch = settings.defaultNonVarSpecSearch;
    /** 开启标签查找 */
    public Boolean defaultTagSearch = settings.defaultTagSearch;
    /** 单击查找项 跳转/删除 */
    public Boolean defaultJumpOrDelete = settings.defaultJumpOrDelete;

    @Override
    public @NotNull SidebarSettingState getState() {
        SidebarSettingState state = new SidebarSettingState();
        state.defaultCommentSearch = this.defaultCommentSearch;
        state.defaultSpecSearch = this.defaultSpecSearch;
        state.defaultNonVarSpecSearch = this.defaultNonVarSpecSearch;
        state.defaultTagSearch = this.defaultTagSearch;
        state.defaultJumpOrDelete = this.defaultJumpOrDelete;

        return state;
    }

    @Override
    public void noStateLoaded() {}

    @Override
    public void loadState(@NotNull SidebarSettingState state) {
        this.defaultCommentSearch = state.defaultCommentSearch;
        this.defaultSpecSearch = state.defaultSpecSearch;
        this.defaultNonVarSpecSearch = state.defaultNonVarSpecSearch;
        this.defaultTagSearch = state.defaultTagSearch;
        this.defaultJumpOrDelete = state.defaultJumpOrDelete;
    }
}
