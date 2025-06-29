package com.sky.consolelog.utils;

import com.sky.consolelog.setting.ConsoleLogSettingVo;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
import com.sky.consolelog.utils.strategy.text.BacktickTextFormatStrategy;
import com.sky.consolelog.utils.strategy.text.DoubleQuoteTextFormatStrategy;
import com.sky.consolelog.utils.strategy.text.SingleQuoteTextFormatStrategy;
import com.sky.consolelog.utils.strategy.text.TextFormatStrategy;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 文本格式策略上下文
 *
 * @author SkySource
 * @Date: 2025/4/4 23:12
 */
public enum TextFormatContext {
    /**
     * 文本格式策略 枚举方式实现单例模式<br/>
     * 枚举方式实现单例模式可有效防止反射攻击和反序列化攻击，本质上也是饿汉单例
     */
    INSTANCE;

    private TextFormatStrategy textFormatStrategy;

    public static String FORM_SIGNAL = "\"";
    public static String CONSOLE_LOG_COMMAND = "console.log(" + FORM_SIGNAL;
    public static String CONSOLE_LOG_BEGIN_REGEX = "\\s*console\\s*" + Pattern.quote(".") + "\\s*log\\s*" + Pattern.quote("(\\s*" + FORM_SIGNAL);
    public static String CONSOLE_LOG_END_REGEX = FORM_SIGNAL + "\\s*" + Pattern.quote(",") + ".*" + Pattern.quote(")") + "\\s*" + ";?";
    public static String CONSOLE_LOG_END_NO_VARIABLE_REGEX = FORM_SIGNAL + "\\s*" + Pattern.quote(")") + "\\s*" + ";?";
    public static String CONSOLE_LOG_END_COMPOSITE_NO_VARIABLE_REGEX = FORM_SIGNAL + "\\s*" + "(" + Pattern.quote(",") + ".*" + ")?" + Pattern.quote(")") + "\\s*" + ";?";
    public static String CONSOLE_LOG_BEGIN_REGEX_WITHOUT_START_SPACE = "console\\s*" + Pattern.quote(".") + "\\s*log\\s*" + Pattern.quote("(\\s*") + FORM_SIGNAL;

    public void setTextFormatStrategyByProjectSetting(ConsoleLogSettingState settings) {
        TextFormatStrategy strategy = DoubleQuoteTextFormatStrategy.getInstance();
        if (settings != null) {
            if (settings.singleQuote) {
                strategy = SingleQuoteTextFormatStrategy.getInstance();
            } else if (settings.backTickQuote) {
                strategy = BacktickTextFormatStrategy.getInstance();
            }
        }
        this.setStrategy(strategy);

        FORM_SIGNAL = textFormatStrategy.getFormSignal();
        CONSOLE_LOG_COMMAND = textFormatStrategy.getBeginText();
        CONSOLE_LOG_BEGIN_REGEX = textFormatStrategy.getBeginRegexText();
        CONSOLE_LOG_END_REGEX = textFormatStrategy.getEndRegexText();
        CONSOLE_LOG_BEGIN_REGEX_WITHOUT_START_SPACE = textFormatStrategy.getBeginRegexText().replaceFirst("\\\\s\\*", "");
    }

    /**
     * 设置文本格式策略
     */
    public void setStrategy(TextFormatStrategy strategy) {
        this.textFormatStrategy = strategy;
    }

    public String getCustomHandleConsoleLogMsg(String consoleLogMsg, ConsoleLogSettingVo consoleLogSettingVo) {
        if (Objects.isNull(textFormatStrategy)) {
            throw new RuntimeException("I am so sorry, 策略为空");
        }
        return textFormatStrategy.getCustomHandleConsoleLogMsg(consoleLogMsg, consoleLogSettingVo);
    }

    public String getDefaultHandleConsoleLogMsg(String defaultConsoleLogMsg, ConsoleLogSettingVo consoleLogSettingVo) {
        if (Objects.isNull(textFormatStrategy)) {
            throw new RuntimeException("I am so sorry, 策略为空");
        }
        return textFormatStrategy.getDefaultHandleConsoleLogMsg(defaultConsoleLogMsg, consoleLogSettingVo);
    }
}
