package com.sky.consolelog.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64编解码工具
 *
 * @author SkySource
 * @Date: 2025/5/15 22:57
 */
public class Base64Util {
    public static String encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String encoded) {
        byte[] decodedBytes;
        try {
            decodedBytes = Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException e) {
            return encoded;
        }
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
