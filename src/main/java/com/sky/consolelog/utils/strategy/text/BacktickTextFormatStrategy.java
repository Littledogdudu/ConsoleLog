package com.sky.consolelog.utils.strategy.text;

import com.sky.consolelog.constant.SettingConstant;

/**
 * 反引号策略
 *
 * @author SkySource
 * @Date: 2025/5/16 20:38
 */
public class BacktickTextFormatStrategy extends BaseTextFormatStrategy {

    private BacktickTextFormatStrategy() {
        // 针对反射攻击做操作...
    }

    /**
     * 内部静态类的懒汉式单例模式
     */
    private static class BacktickTextFormatStrategyHolder {
        private static final BacktickTextFormatStrategy INSTANCE = new BacktickTextFormatStrategy();
    }

    public static BacktickTextFormatStrategy getInstance() {
        return BacktickTextFormatStrategyHolder.INSTANCE;
    }

//    /**
//     * ObjectInputStream类readObject方法中会通过反射调用该方法
//     * 通过编写readResolve方法来帮助readObject方法返回正确的单例对象（针对反序列化攻击）
//     * 仅在同时实现了Serializable时才需要
//     * @return 单例对象
//     */
//    public BacktickTextFormatStrategy readResolve() {
//        return getInstance();
//    }

    @Override
    public String getFormSignal() {
        return SettingConstant.BACKTICK;
    }
}
