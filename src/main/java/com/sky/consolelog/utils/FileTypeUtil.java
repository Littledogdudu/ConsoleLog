package com.sky.consolelog.utils;

import com.sky.consolelog.constant.SupportFileType;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;

/**
 * 文件类型工具
 *
 * @author SkySource
 * @Date: 2025/4/21 11:45
 */
public class FileTypeUtil {

    /**
     * 根据设置中侧边栏已配置的文件类型配置文件类型列表
     */
    public static void setSettingFileTypeList(ConsoleLogSettingState settings) {
        boolean flag = false;
        ConsoleLogSettingState.fileTypeList.clear();
        if (settings.fileTypeAllIn) {
            return;
        }
        if (settings.vueSide) {
            ConsoleLogSettingState.fileTypeList.add(SupportFileType.VUE);
        }
        if (settings.javaScriptSide) {
            ConsoleLogSettingState.fileTypeList.addAll(SupportFileType.JAVASCRIPT);
            flag = true;
        }
        if (settings.typeScriptSide) {
            ConsoleLogSettingState.fileTypeList.addAll(SupportFileType.TYPESCRIPT);
            flag = true;
        }
        if (settings.textSide) {
            ConsoleLogSettingState.fileTypeList.add(SupportFileType.TEXT);
        }
        if (flag) {
            ConsoleLogSettingState.fileTypeList.add(SupportFileType.ECMASCRIPT6);
        }
    }
}
