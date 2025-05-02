package com.sky.consolelog.utils;

/**
 * 文本格式上下文单例
 *
 * @author SkySource
 * @Date: 2025/4/5 22:14
 */
public class TextFormatContextSingleton {

    private TextFormatContextSingleton() {
    }

    /**
     * Holder模式实现单例模式，由JVM保证类的初始化方法clinit只执行一次，保证线程安全
     */
    private static final class TextFormatContextHolder {
        private static final TextFormatContext TEXT_FORMAT_CONTEXT = new TextFormatContext();
    }

    public static TextFormatContext getInstance() {
        return TextFormatContextHolder.TEXT_FORMAT_CONTEXT;
    }
}
