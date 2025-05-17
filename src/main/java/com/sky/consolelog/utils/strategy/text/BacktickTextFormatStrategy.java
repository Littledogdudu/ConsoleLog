package com.sky.consolelog.utils.strategy.text;

import com.sky.consolelog.constant.SettingConstant;

/**
 * 反引号策略
 *
 * @author SkySource
 * @Date: 2025/5/16 20:38
 */
public class BacktickTextFormatStrategy extends BaseTextFormatStrategy {
    @Override
    public String getFormSignal() {
        return SettingConstant.BACKTICK;
    }
}
