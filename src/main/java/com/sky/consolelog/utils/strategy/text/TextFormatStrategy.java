package com.sky.consolelog.utils.strategy.text;

import com.sky.consolelog.setting.ConsoleLogSettingVo;
import org.jetbrains.annotations.NotNull;

/**
 * 文本格式策略
 *
 * @author SkySource
 * @Date: 2025/4/4 19:57
 */
public interface TextFormatStrategy {

    /**
     * 获取console.log表达式的使用的标识符
     *
     * @return console.log表达式的标识符
     */
    String getFormSignal();

    /**
     * 获取console.log表达式的起始文本
     *
     * @return console.log表达式的文本
     */
    String getBeginText();

    /**
     * 获取console.log表达式的正则表达式起始文本
     *
     * @return console.log表达式的正则表达式文本
     */
    String getBeginRegexText();

    /**
     * 获取console.log表达式的正则表达式结束文本
     *
     * @return console.log表达式的正则表达式文本
     */
    String getEndRegexText();

    /**
     * 格式化console.log表达式的文本
     *
     * @return 需要插入到编辑器中的文本
     */
    @NotNull String getCustomHandleConsoleLogMsg(String consoleLogMsg, ConsoleLogSettingVo consoleLogSettingVo);

    /**
     * 格式化console.log表达式的文本（光标处位处于任何变量时的默认行为）
     * @param defaultConsoleLogMsg 默认的console.log表达式
     * @param consoleLogSettingVo 打印参数
     * @return 需要插入到编辑器中的文本
     */
    @NotNull String getDefaultHandleConsoleLogMsg(String defaultConsoleLogMsg, ConsoleLogSettingVo consoleLogSettingVo);
}
