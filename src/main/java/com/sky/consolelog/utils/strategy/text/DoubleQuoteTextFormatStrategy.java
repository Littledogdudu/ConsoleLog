package com.sky.consolelog.utils.strategy.text;

import com.sky.consolelog.constant.SettingConstant;

/**
 * 单引号文本格式化策略
 *
 * @author SkySource
 * @Date: 2025/4/4 20:00
 */
public class DoubleQuoteTextFormatStrategy extends BaseTextFormatStrategy {
    @Override
    public String getFormSignal() {
        return SettingConstant.DOUBLE_QUOTE;
    }
}
