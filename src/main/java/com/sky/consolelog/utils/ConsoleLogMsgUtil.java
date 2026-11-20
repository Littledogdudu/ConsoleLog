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
    private static String withoutStartSpecConsoleLogMsgRegex = "";

    private static String currentDefaultFormSignal = "";
    private static String currentDefaultConsoleLogMsg = "";
    private static String defaultConsoleLogMsgRegex = "";

    private static String currentWithoutStartSpaceDefaultFormSignal = "";
    private static String currentWithoutStartSpaceDefaultConsoleLogMsg = "";
    private static String withoutStartSpaceDefaultConsoleLogMsgRegex = "";

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
        replacePlaceHolderToRegex(consoleLogMsg, regexConsoleLogMsg, false);
        regexConsoleLogMsg.append(TextFormatContext.CONSOLE_LOG_END_REGEX);

        currentFormSignal = TextFormatContext.FORM_SIGNAL;
        currentConsoleLogMsg = consoleLogMsg;
        consoleLogMsgRegex = regexConsoleLogMsg.toString();
        return consoleLogMsgRegex;
    }

    /**
     * 构建consoleLog正则表达式（没有起始空格）
     *
     * @return 返回设置的ConsoleLog语句正则表达式（没有起始空格）
     */
    public static String buildWithoutStartSpaceRegexConsoleLogMsg(ConsoleLogSettingState settings) {
        String consoleLogMsg = settings.consoleLogMsg;
        if (StringUtils.isEmpty(consoleLogMsg)) {
            return null;
        }

        if (consoleLogMsg.equals(currentWithoutStartSpaceConsoleLogMsg) && TextFormatContext.FORM_SIGNAL.equals(currentWithoutStartSpaceFormSignal)) {
            return withoutStartSpecConsoleLogMsgRegex;
        }
        StringBuilder regexConsoleLogMsg = new StringBuilder();
        regexConsoleLogMsg.append(TextFormatContext.CONSOLE_LOG_BEGIN_REGEX_WITHOUT_START_SPACE);
        // 插件设置console.log内的文字提示指针
        replacePlaceHolderToRegex(consoleLogMsg, regexConsoleLogMsg, false);
        regexConsoleLogMsg.append(TextFormatContext.CONSOLE_LOG_END_REGEX);

        currentWithoutStartSpaceFormSignal = TextFormatContext.FORM_SIGNAL;
        currentWithoutStartSpaceConsoleLogMsg = consoleLogMsg;
        withoutStartSpecConsoleLogMsgRegex = regexConsoleLogMsg.toString();
        return withoutStartSpecConsoleLogMsgRegex;
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
        replacePlaceHolderToRegex(consoleLogMsg, regexConsoleLogMsg, false);
        regexConsoleLogMsg.append(TextFormatContext.CONSOLE_LOG_END_NO_VARIABLE_REGEX);

        currentDefaultFormSignal = TextFormatContext.FORM_SIGNAL;
        currentDefaultConsoleLogMsg = consoleLogMsg;
        defaultConsoleLogMsgRegex = regexConsoleLogMsg.toString();
        return defaultConsoleLogMsgRegex;
    }

    /**
     * 构建无选中文本时默认生成的consoleLog正则表达式（没有起始空格）
     *
     * @return 返回设置的ConsoleLog语句正则表达式（没有起始空格）
     */
    public static String buildWithoutStartSpaceRegexDefaultConsoleLogMsg(ConsoleLogSettingState settings) {
        String consoleLogMsg = settings.defaultConsoleLogMsg;
        if (StringUtils.isEmpty(consoleLogMsg)) {
            return null;
        }

        if (consoleLogMsg.equals(currentWithoutStartSpaceDefaultConsoleLogMsg) && TextFormatContext.FORM_SIGNAL.equals(currentWithoutStartSpaceDefaultFormSignal)) {
            return withoutStartSpaceDefaultConsoleLogMsgRegex;
        }
        StringBuilder regexConsoleLogMsg = new StringBuilder();
        regexConsoleLogMsg.append(TextFormatContext.CONSOLE_LOG_BEGIN_REGEX_WITHOUT_START_SPACE);
        // 插件设置console.log内的文字提示指针
        replacePlaceHolderToRegex(consoleLogMsg, regexConsoleLogMsg, false);
        regexConsoleLogMsg.append(TextFormatContext.CONSOLE_LOG_END_NO_VARIABLE_REGEX);

        currentWithoutStartSpaceDefaultFormSignal = TextFormatContext.FORM_SIGNAL;
        currentWithoutStartSpaceDefaultConsoleLogMsg = consoleLogMsg;
        withoutStartSpaceDefaultConsoleLogMsgRegex = regexConsoleLogMsg.toString();
        return withoutStartSpaceDefaultConsoleLogMsgRegex;
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
            replacePlaceHolderToRegex(consoleLogMsg, tmpConsoleLogMsg, true);
            regex.add(tmpConsoleLogMsg + TextFormatContext.CONSOLE_LOG_END_REGEX);
        }
        if (StringUtils.isNotEmpty(defaultConsoleLogMsg)) {
            StringBuilder tmpConsoleLogMsg = new StringBuilder();
            tmpConsoleLogMsg.append(TextFormatContext.CONSOLE_LOG_BEGIN_REGEX);
            replacePlaceHolderToRegex(defaultConsoleLogMsg, tmpConsoleLogMsg, true);
            regex.add(tmpConsoleLogMsg + TextFormatContext.CONSOLE_LOG_END_NO_VARIABLE_REGEX);
        }
        regexConsoleLogMsg.append(String.join("|", regex))
                .append(")");

        currentLinNumberFormSignal = TextFormatContext.FORM_SIGNAL;
        currentLineNumberConsoleLogMsg = consoleLogMsg;
        currentLineNumberDefaultConsoleLogMsg = defaultConsoleLogMsg;
        lineNumberConsoleLogMsgRegex = regexConsoleLogMsg.toString();
        return lineNumberConsoleLogMsgRegex;
    }

    private static void replacePlaceHolderToRegex(String consoleLogMsg, StringBuilder regexConsoleLogMsg, boolean captureLineNumber) {
        int pointer = 0;
        SettingConstant.VariableAlias[] aliases = SettingConstant.VariableAlias.values();
        while (pointer < consoleLogMsg.length()) {
            int nextIndex = consoleLogMsg.length();
            SettingConstant.VariableAlias nextAlias = null;
            for (SettingConstant.VariableAlias alias : aliases) {
                int index = consoleLogMsg.indexOf(alias.getKey(), pointer);
                if (index >= 0 && index < nextIndex) {
                    nextIndex = index;
                    nextAlias = alias;
                }
            }

            regexConsoleLogMsg.append(Pattern.quote(consoleLogMsg.substring(pointer, nextIndex)));
            if (nextAlias == null) {
                break;
            }
            switch (nextAlias) {
                case RANDOM_EMOJI_COMMAND, SMART_EMOJI_COMMAND -> regexConsoleLogMsg.append(EmojiUtil.REGEX);
                case LINE_NUMBER_COMMAND -> regexConsoleLogMsg.append(captureLineNumber ? "(" + SettingConstant.ALL_REGEX + ")" : SettingConstant.ALL_REGEX);
                default -> regexConsoleLogMsg.append(SettingConstant.ALL_REGEX);
            }
            pointer = nextIndex + nextAlias.getKey().length();
        }
    }
}
