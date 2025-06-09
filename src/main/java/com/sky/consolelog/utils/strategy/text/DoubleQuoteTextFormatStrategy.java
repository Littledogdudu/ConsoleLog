package com.sky.consolelog.utils.strategy.text;

import com.sky.consolelog.constant.SettingConstant;

/**
 * 单引号文本格式化策略
 *
 * @author SkySource
 * @Date: 2025/4/4 20:00
 */
public class DoubleQuoteTextFormatStrategy extends BaseTextFormatStrategy {

    private DoubleQuoteTextFormatStrategy() {}

    /**
     * 内部静态类的懒汉式单例模式
     */
    private static class DoubleQuoteTextFormatStrategyHolder {
        private static final DoubleQuoteTextFormatStrategy INSTANCE = new DoubleQuoteTextFormatStrategy();
    }

    public static DoubleQuoteTextFormatStrategy getInstance() {
        return DoubleQuoteTextFormatStrategy.DoubleQuoteTextFormatStrategyHolder.INSTANCE;
    }

    @Override
    public String getFormSignal() {
        return SettingConstant.DOUBLE_QUOTE;
    }
}
