package com.sky.consolelog.utils;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * 国际化工具类
 *
 * @author SkySource
 * @Date: 2025/6/14 15:25
 */
public class MessageUtils extends AbstractBundle {

    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
        return INSTANCE.getMessage(key, params);
    }

    public static final String BUNDLE = "messages.MyBundle";
    private static final MessageUtils INSTANCE = new MessageUtils();

    private MessageUtils() {
        super(BUNDLE);
    }
}
