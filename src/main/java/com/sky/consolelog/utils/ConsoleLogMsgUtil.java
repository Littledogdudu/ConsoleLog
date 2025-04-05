package com.sky.consolelog.utils;

import com.sky.consolelog.constant.SettingConstant;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;

import java.util.regex.Pattern;

/**
 * consoleLog信息处理工具
 *
 * @author SkySource
 * @Date: 2025/2/1 22:06
 */
public class ConsoleLogMsgUtil {

    /**
     * 构建consoleLog正则表达式
     *
     * @return 返回设置的ConsoleLog语句正则表达式
     */
    public static String buildRegexConsoleLogMsg(ConsoleLogSettingState settings) {
        if (settings.consoleLogMsg == null || settings.consoleLogMsg.isEmpty()) {
            return null;
        }
        // 更新TextFormatContext的CONSOLE常量
        TextFormatContextSingleton.getInstance();
        StringBuilder regexConsoleLogMsg = new StringBuilder();
        regexConsoleLogMsg.append(TextFormatContext.CONSOLE_LOG_BEGIN_REGEX);
        // 插件设置console.log内的文字提示指针
        int commandPointerIndex = 0;

        int consoleLogMsgLength = settings.consoleLogMsg.length();
        SettingConstant.VariableAlias[] variableAliasList = SettingConstant.VariableAlias.values();
        while (true) {
            // 是否存在匹配的子字符串标识
            int hasKeys = 0;
            String subMsg = settings.consoleLogMsg.substring(commandPointerIndex, consoleLogMsgLength);
            // 指定子字符串的开始和结束索引
            int commandBegIndex = Integer.MAX_VALUE;
            int commandEndIndex = commandPointerIndex;
            for (int settingIndex = 0; settingIndex < SettingConstant.VariableAlias.values().length; settingIndex++) {
                int index = subMsg.indexOf(variableAliasList[settingIndex].getKey());
                if (index != -1) {
                    if (commandBegIndex > index) {
                        // 选取匹配子字符串的最小索引
                        commandBegIndex = commandPointerIndex + index;
                        commandEndIndex = commandPointerIndex + index + variableAliasList[settingIndex].getKey().length();
                        ++hasKeys;
                    }
                }
            }

            if (hasKeys == 0) {
                // 如果指定的子字符串已经没有一个可以匹配上的则退出循环
                break;
            }

            // 字面化普通字符串，指定的子字符串使用.*替代
            regexConsoleLogMsg.append(Pattern.quote(settings.consoleLogMsg.substring(commandPointerIndex, commandBegIndex)))
                    .append(".*");
            commandPointerIndex = commandEndIndex;
        }
        if (commandPointerIndex < settings.consoleLogMsg.length()) {
            regexConsoleLogMsg.append(Pattern.quote(settings.consoleLogMsg.substring(commandPointerIndex)));
        }
        regexConsoleLogMsg.append(TextFormatContext.CONSOLE_LOG_END_REGEX);
        return regexConsoleLogMsg.toString();
    }
}
