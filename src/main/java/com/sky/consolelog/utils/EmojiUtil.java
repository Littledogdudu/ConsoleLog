package com.sky.consolelog.utils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class EmojiUtil {
    private static final Map<String, String> KEYWORDS = Map.ofEntries(
            Map.entry("row", "🚣"),
            Map.entry("user", "👤"),
            Map.entry("people", "👥"),
            Map.entry("key", "🔑"),
            Map.entry("lock", "🔒"),
            Map.entry("file", "📄"),
            Map.entry("folder", "📁"),
            Map.entry("search", "🔍"),
            Map.entry("error", "❌"),
            Map.entry("warning", "⚠️"),
            Map.entry("success", "✅"),
            Map.entry("count", "🔢"),
            Map.entry("time", "⏰"),
            Map.entry("date", "📅"),
            Map.entry("email", "📧"),
            Map.entry("message", "💬"),
            Map.entry("image", "🖼️"),
            Map.entry("cart", "🛒"),
            Map.entry("price", "💰"),
            Map.entry("location", "📍")
    );
    private static final List<String> EMOJIS = KEYWORDS.values().stream().distinct().sorted().toList();
    private static final Pattern WORD_BOUNDARY = Pattern.compile("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|[^a-zA-Z]+");
    static final String REGEX = EMOJIS.stream().map(Pattern::quote).collect(Collectors.joining("|", "(?:", ")"));

    private EmojiUtil() {}

    public static String randomEmoji() {
        return EMOJIS.get(ThreadLocalRandom.current().nextInt(EMOJIS.size()));
    }

    public static String smartEmoji(String variableName) {
        String[] words = WORD_BOUNDARY.split(variableName);
        // 优先匹配靠后的词，例如 user.email 选择邮件而不是用户。
        for (int i = words.length - 1; i >= 0; i--) {
            String emoji = KEYWORDS.get(words[i].toLowerCase(Locale.ROOT));
            if (emoji != null) {
                return emoji;
            }
        }
        return EMOJIS.get(Math.floorMod(variableName.hashCode(), EMOJIS.size()));
    }
}
