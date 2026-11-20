package com.sky.consolelog.utils;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterfaceClass;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.psi.PsiElement;
import com.sky.consolelog.constant.SettingConstant;
import com.sky.consolelog.setting.ConsoleLogSettingVo;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;
import com.sky.consolelog.utils.strategy.text.BacktickTextFormatStrategy;
import com.sky.consolelog.utils.strategy.text.DoubleQuoteTextFormatStrategy;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConsoleLogPlaceholderTest {
    private final DoubleQuoteTextFormatStrategy strategy = DoubleQuoteTextFormatStrategy.getInstance();

    @Test
    public void rendersSmartEmojiClassAndMethodTogether() {
        String template = "${smartEmoji} ~ ${className}.${methodName} ~ ${variableName}: ";
        ConsoleLogSettingVo values = values("rowValue");
        values.setClassName("Grid$View");

        assertEquals("console.log(\"🚣 ~ Grid$View.render ~ rowValue: \", rowValue);",
                strategy.getCustomHandleConsoleLogMsg(template, values));
        assertEquals("console.$METHOD$(`🚣 ~ Grid$View.render ~ rowValue: `, rowValue);",
                BacktickTextFormatStrategy.getInstance().getCustomTemplateHandleConsoleLogMsg(template, values));
    }

    @Test
    public void matchesKeywordsAndKeepsUnknownVariableEmojiStable() {
        assertEquals("🚣", EmojiUtil.smartEmoji("model.ROW_VALUE"));
        assertEquals("📧", EmojiUtil.smartEmoji("userEmail"));
        String fallback = EmojiUtil.smartEmoji("customField");
        assertTrue(fallback.matches(EmojiUtil.REGEX));
        assertEquals(fallback, EmojiUtil.smartEmoji("customField"));
    }

    @Test
    public void resolvesNearestClassInJavaScriptAndTypeScript() {
        PsiElement outer = psi(JSClass.class, "Outer", null);
        PsiElement inner = psi(TypeScriptClass.class, "Inner", outer);
        PsiElement anonymous = psi(JSClass.class, null, outer);
        PsiElement tsInterface = psi(TypeScriptInterfaceClass.class, "OnlyInterface", null);

        assertEquals("Outer", PsiVariableUtil.getEnclosingClassName(psi(PsiElement.class, null, outer)));
        assertEquals("Inner", PsiVariableUtil.getEnclosingClassName(psi(PsiElement.class, null, inner)));
        assertEquals("", PsiVariableUtil.getEnclosingClassName(psi(PsiElement.class, null, anonymous)));
        assertEquals("", PsiVariableUtil.getEnclosingClassName(psi(PsiElement.class, null, tsInterface)));
        assertEquals("", PsiVariableUtil.getEnclosingClassName(psi(PsiElement.class, null, null)));
    }

    @Test
    public void recognizesGeneratedLogsWithoutMatchingPlainTextLogs() {
        ConsoleLogSettingState settings = new ConsoleLogSettingState();
        settings.consoleLogMsg = "${randomEmoji} ~ ${className} ~ ${smartEmoji} ~ L${lineNumber} ~ ${variableName}";
        TextFormatContext.INSTANCE.setTextFormatStrategyByProjectSetting(settings);
        ConsoleLogSettingVo values = values("rowValue");
        String log = strategy.getCustomHandleConsoleLogMsg(settings.consoleLogMsg, values);

        String regex = ConsoleLogMsgUtil.buildRegexConsoleLogMsg(settings);
        assertTrue(log.matches(regex));
        assertTrue(log.matches(ConsoleLogMsgUtil.buildWithoutStartSpaceRegexConsoleLogMsg(settings)));
        assertFalse("console.log(\"debug ~  ~ 🚣 ~ L42 ~ rowValue\", rowValue);".matches(regex));

        Matcher matcher = Pattern.compile(ConsoleLogMsgUtil.buildFindLineNumberConsoleLogMsgRegex(settings)).matcher(log);
        assertTrue(matcher.matches());
        assertEquals(2, matcher.groupCount());
        assertEquals("42", matcher.group(1));
    }

    @Test
    public void supportsNoVariableTemplateAndPreservesOldDefaults() {
        ConsoleLogSettingState settings = new ConsoleLogSettingState();
        settings.defaultConsoleLogMsg = "${smartEmoji} ~ ${className} ~ ${fileName}";
        TextFormatContext.INSTANCE.setTextFormatStrategyByProjectSetting(settings);
        String log = strategy.getDefaultHandleConsoleLogMsg(settings.defaultConsoleLogMsg, values(""));

        assertEquals("console.log(\"" + EmojiUtil.smartEmoji("") + " ~  ~ grid.ts\");", log);
        assertTrue(log.matches(ConsoleLogMsgUtil.buildRegexDefaultConsoleLogMsg(settings)));
        assertEquals("console.log(\"🚀 ~ render ~ rowValue: \", rowValue);",
                strategy.getCustomHandleConsoleLogMsg(SettingConstant.DEFAULT_CONSOLE_LOG_MSG, values("rowValue")));
        assertEquals("console.log(\"➤➤➤ ~ grid.ts ~ L42\");",
                strategy.getDefaultHandleConsoleLogMsg(SettingConstant.DEFAULT_CONSOLE_LOG_MSG_WITHOUT_VARIABLE, values("")));
    }

    private static ConsoleLogSettingVo values(String variableName) {
        ConsoleLogSettingVo values = new ConsoleLogSettingVo();
        values.setVariableName(variableName);
        values.setMethodName("render");
        values.setLineNumber(42);
        values.setFileName("grid.ts");
        values.setFilePath("src");
        return values;
    }

    private static PsiElement psi(Class<? extends PsiElement> type, String name, PsiElement parent) {
        return (PsiElement) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getName" -> name;
                    case "getParent" -> parent;
                    case "isInterface" -> type == TypeScriptInterfaceClass.class;
                    default -> throw new AssertionError(method.getName());
                });
    }
}
