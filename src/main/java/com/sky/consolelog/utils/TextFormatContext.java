package com.sky.consolelog.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.sky.consolelog.setting.ConsoleLogSettingVo;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
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
    private final ConsoleLogSettingState settings = ApplicationManager.getApplication().getService(ConsoleLogSettingState.class);
    /**
     * 策略对象缓存（即单例模式）
     */
    private final Map<Class<? extends TextFormatStrategy>, TextFormatStrategy> cache = new HashMap<>(2);

    public static String CONSOLE_LOG_COMMAND = "console.log(\"";
    public static String CONSOLE_LOG_BEGIN_REGEX = "\\s*console\\s*" + Pattern.quote(".") + "\\s*log\\s*" + Pattern.quote("(\"") + "\\s*";
    public static String CONSOLE_LOG_END_REGEX = "\"\\s*" + Pattern.quote(",") + ".*" + Pattern.quote(")") + "\\s*" + ";?";

    public void setTextFormatStrategyByProjectSetting() {
        Class<? extends TextFormatStrategy> strategy = DoubleQuoteTextFormatStrategy.class;
        if (settings != null && !settings.isDoubleQuote) {
            strategy = SingleQuoteTextFormatStrategy.class;
        }
        this.setStrategy(strategy);
        CONSOLE_LOG_COMMAND = textFormatStrategy.getBeginText();
        CONSOLE_LOG_BEGIN_REGEX = textFormatStrategy.getBeginRegexText();
        CONSOLE_LOG_END_REGEX = textFormatStrategy.getEndRegexText();
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
