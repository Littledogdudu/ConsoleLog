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

    private static String currentFormSignal = "";
    private static String currentConsoleLogMsg = "";
    private static String consoleLogMsgRegex = "";

    private static String currentDefaultFormSignal = "";
    private static String currentDefaultConsoleLogMsg = "";
    private static String defaultConsoleLogMsgRegex = "";

    /**
     * 构建consoleLog正则表达式
     *
     * @return 返回设置的ConsoleLog语句正则表达式
     */
    public static String buildRegexConsoleLogMsg(ConsoleLogSettingState settings) {
        return buildRegexConsoleLogMsg(settings, TextFormatContext.CONSOLE_LOG_BEGIN_REGEX, TextFormatContext.CONSOLE_LOG_END_REGEX);
    }

    /**
     * 构建无选中文本时默认生成的consoleLog正则表达式
     *
     * @return 返回设置的ConsoleLog语句正则表达式
     */
    public static String buildRegexDefaultConsoleLogMsg(ConsoleLogSettingState settings) {
        return buildRegexDefaultConsoleLogMsg(settings, TextFormatContext.CONSOLE_LOG_BEGIN_REGEX, TextFormatContext.CONSOLE_LOG_END_NO_VARIABLE_REGEX);
    }

    /**
     * 构建consoleLog正则表达式
     *
     * @return 返回设置的ConsoleLog语句正则表达式
     */
    public static String buildRegexConsoleLogMsg(ConsoleLogSettingState settings, String beginRegex, String endRegex) {
        String consoleLogMsg = settings.consoleLogMsg;
        if (consoleLogMsg == null || consoleLogMsg.isEmpty()) {
            return null;
        }

        if (consoleLogMsg.equals(currentConsoleLogMsg) && TextFormatContext.FORM_SIGNAL.equals(currentFormSignal)) {
            return consoleLogMsgRegex;
        }
        StringBuilder regexConsoleLogMsg = new StringBuilder();
        regexConsoleLogMsg.append(beginRegex);
        // 插件设置console.log内的文字提示指针
        int commandPointerIndex = 0;

        int consoleLogMsgLength = consoleLogMsg.length();
        SettingConstant.VariableAlias[] variableAliasList = SettingConstant.VariableAlias.values();
        while (true) {
            // 是否存在匹配的子字符串标识
            int hasKeys = 0;
            String subMsg = consoleLogMsg.substring(commandPointerIndex, consoleLogMsgLength);
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
            regexConsoleLogMsg.append(Pattern.quote(consoleLogMsg.substring(commandPointerIndex, commandBegIndex)))
                    .append(SettingConstant.ALL_REGEX);
            commandPointerIndex = commandEndIndex;
        }
        if (commandPointerIndex < consoleLogMsg.length()) {
            regexConsoleLogMsg.append(Pattern.quote(consoleLogMsg.substring(commandPointerIndex)));
        }
        regexConsoleLogMsg.append(endRegex);

        currentFormSignal = TextFormatContext.FORM_SIGNAL;
        currentConsoleLogMsg = consoleLogMsg;
        consoleLogMsgRegex = regexConsoleLogMsg.toString();
        return consoleLogMsgRegex;
    }

    /**
     * 构建未选择文本时生成的默认consoleLog正则表达式
     *
     * @return 返回设置的默认ConsoleLog语句正则表达式
     */
    public static String buildRegexDefaultConsoleLogMsg(ConsoleLogSettingState settings, String beginRegex, String endRegex) {
        String consoleLogMsg = settings.defaultConsoleLogMsg;
        if (consoleLogMsg == null || consoleLogMsg.isEmpty()) {
            return null;
        }

        if (consoleLogMsg.equals(currentDefaultConsoleLogMsg) && TextFormatContext.FORM_SIGNAL.equals(currentDefaultFormSignal)) {
            return defaultConsoleLogMsgRegex;
        }
        StringBuilder regexConsoleLogMsg = new StringBuilder();
        regexConsoleLogMsg.append(beginRegex);
        // 插件设置console.log内的文字提示指针
        int commandPointerIndex = 0;

        int consoleLogMsgLength = consoleLogMsg.length();
        SettingConstant.VariableAlias[] variableAliasList = SettingConstant.VariableAlias.values();
        while (true) {
            // 是否存在匹配的子字符串标识
            int hasKeys = 0;
            String subMsg = consoleLogMsg.substring(commandPointerIndex, consoleLogMsgLength);
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
            regexConsoleLogMsg.append(Pattern.quote(consoleLogMsg.substring(commandPointerIndex, commandBegIndex)))
                    .append(SettingConstant.ALL_REGEX);
            commandPointerIndex = commandEndIndex;
        }
        if (commandPointerIndex < consoleLogMsg.length()) {
            regexConsoleLogMsg.append(Pattern.quote(consoleLogMsg.substring(commandPointerIndex)));
        }
        regexConsoleLogMsg.append(endRegex);

        currentDefaultFormSignal = TextFormatContext.FORM_SIGNAL;
        currentDefaultConsoleLogMsg = consoleLogMsg;
        defaultConsoleLogMsgRegex = regexConsoleLogMsg.toString();
        return defaultConsoleLogMsgRegex;
    }
}
