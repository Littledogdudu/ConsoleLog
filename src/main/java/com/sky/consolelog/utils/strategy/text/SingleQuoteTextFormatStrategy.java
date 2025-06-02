package com.sky.consolelog.utils.strategy.text;

import com.sky.consolelog.constant.SettingConstant;

/**
 * 单引号文本格式化策略
 *
 * @author SkySource
 * @Date: 2025/4/4 20:00
 */
public class SingleQuoteTextFormatStrategy extends BaseTextFormatStrategy {

    private SingleQuoteTextFormatStrategy() {}

    /**
     * 内部静态类的懒汉式单例模式
     */
    private static class SingleQuoteTextFormatStrategyHolder {
        private static final SingleQuoteTextFormatStrategy INSTANCE = new SingleQuoteTextFormatStrategy();
    }

    public static SingleQuoteTextFormatStrategy getInstance() {
        return SingleQuoteTextFormatStrategy.SingleQuoteTextFormatStrategyHolder.INSTANCE;
    }

    @Override
    public String getFormSignal() {
        return SettingConstant.SINGLE_QUOTE;
    }
}
