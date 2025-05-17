package com.sky.consolelog.utils;

import com.sky.consolelog.setting.ConsoleLogSettingVo;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
import com.sky.consolelog.utils.strategy.text.BacktickTextFormatStrategy;
import com.sky.consolelog.utils.strategy.text.DoubleQuoteTextFormatStrategy;
import com.sky.consolelog.utils.strategy.text.SingleQuoteTextFormatStrategy;
import com.sky.consolelog.utils.strategy.text.TextFormatStrategy;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 文本格式策略上下文
 *
 * @author SkySource
 * @Date: 2025/4/4 23:12
 */
public class TextFormatContext {
    private TextFormatStrategy textFormatStrategy;
    /**
     * 策略对象缓存（即单例模式）
     */
    private final Map<Class<? extends TextFormatStrategy>, TextFormatStrategy> cache = new HashMap<>(3);

    public static String CONSOLE_LOG_COMMAND = "console.log(\"";
    public static String CONSOLE_LOG_BEGIN_REGEX = "\\s*console\\s*" + Pattern.quote(".") + "\\s*log\\s*" + Pattern.quote("(\\s*\"");
    public static String CONSOLE_LOG_END_REGEX = "\"\\s*" + Pattern.quote(",") + ".*" + Pattern.quote(")") + "\\s*" + ";?";
    public static String CONSOLE_LOG_BEGIN_REGEX_WITHOUT_START_SPACE = "console\\s*" + Pattern.quote(".") + "\\s*log\\s*" + Pattern.quote("(\\s*\"");

    public void setTextFormatStrategyByProjectSetting(ConsoleLogSettingState settings) {
        Class<? extends TextFormatStrategy> strategy = DoubleQuoteTextFormatStrategy.class;
        if (settings != null) {
            if (settings.singleQuote) {
                strategy = SingleQuoteTextFormatStrategy.class;
            } else if (settings.backTickQuote) {
                strategy = BacktickTextFormatStrategy.class;
            }
        }
        this.setStrategy(strategy);
        CONSOLE_LOG_COMMAND = textFormatStrategy.getBeginText();
        CONSOLE_LOG_BEGIN_REGEX = textFormatStrategy.getBeginRegexText();
        CONSOLE_LOG_END_REGEX = textFormatStrategy.getEndRegexText();
        CONSOLE_LOG_BEGIN_REGEX_WITHOUT_START_SPACE = textFormatStrategy.getBeginRegexText().replaceFirst("\\\\s\\*", "");
    }

    /**
     * 设置文本格式策略
     */
    public void setStrategy(Class<? extends TextFormatStrategy> clazz) {
        if (cache.get(clazz) == null) {
            try {
                cache.put(clazz, clazz.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException("I am so sorry, 没有找到对应的策略");
            }
        }
        this.textFormatStrategy = cache.get(clazz);
    }

    public String getCustomHandleConsoleLogMsg(String consoleLogMsg, ConsoleLogSettingVo consoleLogSettingVo) {
        if (textFormatStrategy == null) {
            throw new RuntimeException("I am so sorry, 策略为空");
        }
        return textFormatStrategy.getCustomHandleConsoleLogMsg(consoleLogMsg, consoleLogSettingVo);
    }
}
