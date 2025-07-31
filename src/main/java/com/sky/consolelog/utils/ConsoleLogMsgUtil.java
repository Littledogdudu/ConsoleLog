package com.sky.consolelog.utils;

import com.sky.consolelog.constant.SettingConstant;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
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

    private static String currentWithoutStartSpaceFormSignal = "";
    private static String currentWithoutStartSpaceConsoleLogMsg = "";
    private static String withoutStartSpacConsoleLogMsgRegex = "";

    private static String currentDefaultFormSignal = "";
    private static String currentDefaultConsoleLogMsg = "";
    private static String defaultConsoleLogMsgRegex = "";

    private static String currentLinNumberFormSignal = "";
    private static String currentLineNumberConsoleLogMsg = "";
    private static String currentLineNumberDefaultConsoleLogMsg = "";
    private static String lineNumberConsoleLogMsgRegex = "";

    /**
     * 构建consoleLog正则表达式
     *
     * @return 返回设置的ConsoleLog语句正则表达式
     */
    public static String buildRegexConsoleLogMsg(ConsoleLogSettingState settings) {
        String consoleLogMsg = settings.consoleLogMsg;
        if (StringUtils.isEmpty(consoleLogMsg)) {
            return null;
        }

        if (consoleLogMsg.equals(currentConsoleLogMsg) && TextFormatContext.FORM_SIGNAL.equals(currentFormSignal)) {
            return consoleLogMsgRegex;
        }
        StringBuilder regexConsoleLogMsg = new StringBuilder();
        regexConsoleLogMsg.append(TextFormatContext.CONSOLE_LOG_BEGIN_REGEX);
        // 插件设置console.log内的文字提示指针
        replacePlaceHolderToRegex(consoleLogMsg, regexConsoleLogMsg);
        regexConsoleLogMsg.append(TextFormatContext.CONSOLE_LOG_END_REGEX);

        currentFormSignal = TextFormatContext.FORM_SIGNAL;
        currentConsoleLogMsg = consoleLogMsg;
        consoleLogMsgRegex = regexConsoleLogMsg.toString();
        return consoleLogMsgRegex;
    }

    /**
     * 构建consoleLog正则表达式
     *
     * @return 返回设置的ConsoleLog语句正则表达式
     */
    public static String buildWithoutStartSpaceRegexConsoleLogMsg(ConsoleLogSettingState settings) {
        String consoleLogMsg = settings.consoleLogMsg;
        if (StringUtils.isEmpty(consoleLogMsg)) {
            return null;
        }

        if (consoleLogMsg.equals(currentWithoutStartSpaceConsoleLogMsg) && TextFormatContext.FORM_SIGNAL.equals(currentWithoutStartSpaceFormSignal)) {
            return withoutStartSpacConsoleLogMsgRegex;
        }
        StringBuilder regexConsoleLogMsg = new StringBuilder();
        regexConsoleLogMsg.append(TextFormatContext.CONSOLE_LOG_BEGIN_REGEX_WITHOUT_START_SPACE);
        // 插件设置console.log内的文字提示指针
        replacePlaceHolderToRegex(consoleLogMsg, regexConsoleLogMsg);
        regexConsoleLogMsg.append(TextFormatContext.CONSOLE_LOG_END_REGEX);

        currentWithoutStartSpaceFormSignal = TextFormatContext.FORM_SIGNAL;
        currentWithoutStartSpaceConsoleLogMsg = consoleLogMsg;
        withoutStartSpacConsoleLogMsgRegex = regexConsoleLogMsg.toString();
        return withoutStartSpacConsoleLogMsgRegex;
    }

    /**
     * 构建无选中文本时默认生成的consoleLog正则表达式
     *
     * @return 返回设置的ConsoleLog语句正则表达式
     */
    public static String buildRegexDefaultConsoleLogMsg(ConsoleLogSettingState settings) {
        String consoleLogMsg = settings.defaultConsoleLogMsg;
        if (StringUtils.isEmpty(consoleLogMsg)) {
            return null;
        }

        if (consoleLogMsg.equals(currentDefaultConsoleLogMsg) && TextFormatContext.FORM_SIGNAL.equals(currentDefaultFormSignal)) {
            return defaultConsoleLogMsgRegex;
        }
        StringBuilder regexConsoleLogMsg = new StringBuilder();
        regexConsoleLogMsg.append(TextFormatContext.CONSOLE_LOG_BEGIN_REGEX);
        // 插件设置console.log内的文字提示指针
        replacePlaceHolderToRegex(consoleLogMsg, regexConsoleLogMsg);
        regexConsoleLogMsg.append(TextFormatContext.CONSOLE_LOG_END_NO_VARIABLE_REGEX);

        currentDefaultFormSignal = TextFormatContext.FORM_SIGNAL;
        currentDefaultConsoleLogMsg = consoleLogMsg;
        defaultConsoleLogMsgRegex = regexConsoleLogMsg.toString();
        return defaultConsoleLogMsgRegex;
    }

    /**
     * 构建用于修改行号的consoleLog正则表达式
     *
     * @return 返回设置的ConsoleLog语句正则表达式
     */
    public static String buildFindLineNumberConsoleLogMsgRegex(ConsoleLogSettingState settings) {
        String consoleLogMsg = settings.consoleLogMsg;
        String defaultConsoleLogMsg = settings.defaultConsoleLogMsg;
        if (StringUtils.isEmpty(consoleLogMsg) && StringUtils.isEmpty(defaultConsoleLogMsg)) {
            return null;
        }

        if (currentLineNumberConsoleLogMsg.equals(consoleLogMsg) && currentLineNumberDefaultConsoleLogMsg.equals(defaultConsoleLogMsg) && TextFormatContext.FORM_SIGNAL.equals(currentLinNumberFormSignal)) {
            return lineNumberConsoleLogMsgRegex;
        }

        StringBuilder regexConsoleLogMsg = new StringBuilder();
        regexConsoleLogMsg.append("(?:");
        List<String> regex = new ArrayList<>();
        if (StringUtils.isNotEmpty(consoleLogMsg)) {
            StringBuilder tmpConsoleLogMsg = new StringBuilder();
            tmpConsoleLogMsg.append(TextFormatContext.CONSOLE_LOG_BEGIN_REGEX);
            replacePlaceHolderToRegexExceptLineNumber(consoleLogMsg, tmpConsoleLogMsg);
            String newConsoleLogMsg = tmpConsoleLogMsg.toString().replaceAll(SettingConstant.AliasRegex.LINE_NUMBER_REGEX.getKey(), "(" + SettingConstant.ALL_REGEX + ")");
            regex.add(newConsoleLogMsg + TextFormatContext.CONSOLE_LOG_END_REGEX);
        }
        if (StringUtils.isNotEmpty(defaultConsoleLogMsg)) {
            StringBuilder tmpConsoleLogMsg = new StringBuilder();
            tmpConsoleLogMsg.append(TextFormatContext.CONSOLE_LOG_BEGIN_REGEX);
            replacePlaceHolderToRegexExceptLineNumber(defaultConsoleLogMsg, tmpConsoleLogMsg);
            String newDefaultConsoleLogMsg = tmpConsoleLogMsg.toString().replaceAll(SettingConstant.AliasRegex.LINE_NUMBER_REGEX.getKey(), "(" + SettingConstant.ALL_REGEX + ")") ;
            regex.add(newDefaultConsoleLogMsg + TextFormatContext.CONSOLE_LOG_END_NO_VARIABLE_REGEX);
        }
        regexConsoleLogMsg.append(String.join("|", regex))
                .append(")");

        currentLinNumberFormSignal = TextFormatContext.FORM_SIGNAL;
        currentLineNumberConsoleLogMsg = consoleLogMsg;
        currentLineNumberDefaultConsoleLogMsg = defaultConsoleLogMsg;
        lineNumberConsoleLogMsgRegex = regexConsoleLogMsg.toString();
        return lineNumberConsoleLogMsgRegex;
    }

    private static void replacePlaceHolderToRegex(String consoleLogMsg, StringBuilder regexConsoleLogMsg) {
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
    }

    private static void replacePlaceHolderToRegexExceptLineNumber(String consoleLogMsg, StringBuilder regexConsoleLogMsg) {
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
            SettingConstant.VariableAlias variableAlias = null;
            for (int settingIndex = 0; settingIndex < SettingConstant.VariableAlias.values().length; settingIndex++) {
                int index = subMsg.indexOf(variableAliasList[settingIndex].getKey());
                if (index != -1) {
                    if (commandBegIndex > index) {
                        // 选取匹配子字符串的最小索引
                        commandBegIndex = commandPointerIndex + index;
                        commandEndIndex = commandPointerIndex + index + variableAliasList[settingIndex].getKey().length();
                        variableAlias = variableAliasList[settingIndex];
                        ++hasKeys;
                    }
                }
            }

            if (hasKeys == 0) {
                // 如果指定的子字符串已经没有一个可以匹配上的则退出循环
                break;
            }

            // 字面化普通字符串，指定的子字符串使用.*替代
            regexConsoleLogMsg.append(Pattern.quote(consoleLogMsg.substring(commandPointerIndex, commandBegIndex)));
            if (SettingConstant.VariableAlias.LINE_NUMBER_COMMAND.equals(variableAlias)) {
                regexConsoleLogMsg.append(SettingConstant.VariableAlias.LINE_NUMBER_COMMAND.getKey());
            } else {
                regexConsoleLogMsg.append(SettingConstant.ALL_REGEX);
            }
            commandPointerIndex = commandEndIndex;
        }
        if (commandPointerIndex < consoleLogMsg.length()) {
            regexConsoleLogMsg.append(Pattern.quote(consoleLogMsg.substring(commandPointerIndex)));
        }
    }
}
