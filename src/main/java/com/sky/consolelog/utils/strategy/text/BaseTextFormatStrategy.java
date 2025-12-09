package com.sky.consolelog.utils.strategy.text;

import com.sky.consolelog.constant.SettingConstant;
import com.sky.consolelog.setting.ConsoleLogSettingVo;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本格式化特征抽取基类
 *
 * @author SkySource
 * @Date: 2025/4/4 23:45
 */
public abstract class BaseTextFormatStrategy implements TextFormatStrategy {
    private static final String SPECIAL_CHARACTER = "$";

    @Override
    public String getBeginText() {
        return SettingConstant.CONSOLE_LOG_COMMAND;
    }

    @Override
    public String getBeginRegexText() {
        return SettingConstant.CONSOLE_LOG_BEGIN_REGEX + Pattern.quote(this.getFormSignal());
    }

    @Override
    public String getEndRegexText() {
        return Pattern.quote(this.getFormSignal()) + SettingConstant.CONSOLE_LOG_END_REGEX;
    }

    @Override
    public @NotNull String getEndNoVariableRegexText() {
        return Pattern.quote(this.getFormSignal()) + SettingConstant.CONSOLE_LOG_END_NO_VARIABLE_REGEX;
    }

    @Override
    public @NotNull String getEndCompositeNoVariableRegexText() {
        return Pattern.quote(this.getFormSignal()) + SettingConstant.CONSOLE_LOG_END_COMPOSITE_NO_VARIABLE_REGEX;
    }

    /**
     * 获取需要插入的console.log表达式语句
     *
     * @param consoleLogSettingVo consoleLog设置
     * @return console.log表达式语句
     */
    @Override
    public @NotNull String getCustomHandleConsoleLogMsg(String consoleLogMsg, ConsoleLogSettingVo consoleLogSettingVo) {
        consoleLogMsg = replaceConsoleLog(consoleLogMsg, SettingConstant.AliasRegex.VARIABLE_REGEX, consoleLogSettingVo.getVariableName());
        consoleLogMsg = replaceConsoleLog(consoleLogMsg, SettingConstant.AliasRegex.METHOD_REGEX, consoleLogSettingVo.getMethodName());
        consoleLogMsg = replaceConsoleLog(consoleLogMsg, SettingConstant.AliasRegex.LINE_NUMBER_REGEX, consoleLogSettingVo.getLineNumber().toString());
        consoleLogMsg = replaceConsoleLog(consoleLogMsg, SettingConstant.AliasRegex.FILE_NAME_REGEX, consoleLogSettingVo.getFileName());
        consoleLogMsg = replaceConsoleLog(consoleLogMsg, SettingConstant.AliasRegex.FILE_PATH_REGEX, consoleLogSettingVo.getFilePath());
        return SettingConstant.CONSOLE_LOG_COMMAND + this.getFormSignal() +
                consoleLogMsg + this.getFormSignal() + ", " + consoleLogSettingVo.getVariableName() + ");";
    }

    /**
     * 格式化console.log表达式的文本（光标处位处于任何变量时的默认行为）
     * @param defaultConsoleLogMsg 默认的console.log表达式
     * @param consoleLogSettingVo 打印参数
     * @return 需要插入到编辑器中的文本
     */
    @Override
    public @NotNull String getDefaultHandleConsoleLogMsg(String defaultConsoleLogMsg, ConsoleLogSettingVo consoleLogSettingVo) {
        defaultConsoleLogMsg = replaceConsoleLog(defaultConsoleLogMsg, SettingConstant.AliasRegex.METHOD_REGEX, consoleLogSettingVo.getMethodName());
        defaultConsoleLogMsg = replaceConsoleLog(defaultConsoleLogMsg, SettingConstant.AliasRegex.LINE_NUMBER_REGEX, consoleLogSettingVo.getLineNumber().toString());
        defaultConsoleLogMsg = replaceConsoleLog(defaultConsoleLogMsg, SettingConstant.AliasRegex.FILE_NAME_REGEX, consoleLogSettingVo.getFileName());
        defaultConsoleLogMsg = replaceConsoleLog(defaultConsoleLogMsg, SettingConstant.AliasRegex.FILE_PATH_REGEX, consoleLogSettingVo.getFilePath());
        return SettingConstant.CONSOLE_LOG_COMMAND + this.getFormSignal() +
                defaultConsoleLogMsg + this.getFormSignal() + ");";
    }

    /**
     * 用于转义可能存在的双引号以防止出现<br/>
     * console.log("arr["1"]: ", arr["1"])<br/>
     * 的报错问题
     *
     * @param replaceConsoleLogStr 需要被转义的字符串
     * @param aliasRegex           需要匹配的正则表达式
     * @param value                替换值
     * @return 转移过后的replaceConsoleLogStr
     */
    public String replaceConsoleLog(String replaceConsoleLogStr, SettingConstant.AliasRegex aliasRegex, String value) {
        if (value.contains(SPECIAL_CHARACTER)) {
            if (value.contains(this.getFormSignal())) {
                // 新tips：因为replaceAll对替换项（replacement）的$有特殊处理，故此处使用Matcher.quoteReplacement对替换项做处理
                // 哎光看源码了，今天看了注释才发现可以这么简单，焯！🤡
                return replaceConsoleLogStr.replaceAll(aliasRegex.getKey(), Matcher.quoteReplacement(value.replaceAll("\"", "\\\\\\\\" + this.getFormSignal())));
            }
            return replaceConsoleLogStr.replaceAll(aliasRegex.getKey(), Matcher.quoteReplacement(value));
        }
        if (value.contains(this.getFormSignal())) {
            return replaceConsoleLogStr.replaceAll(aliasRegex.getKey(), value.replaceAll(this.getFormSignal(), "\\\\\\\\" + this.getFormSignal()));
        }
        return replaceConsoleLogStr.replaceAll(aliasRegex.getKey(), value);
    }
}
