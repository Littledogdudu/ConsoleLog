package com.sky.consolelog.constant;

import java.util.regex.Pattern;

/**
 * 占位符常量
 *
 * @author SkySource
 * @Date: 2025/1/25 12:41
 */
public interface SettingConstant {
    String DOUBLE_QUOTE = "\"";
    String SINGLE_QUOTE = "'";
    String CONSOLE_LOG_COMMAND = "console.log(";
    String CONSOLE_LOG_BEGIN_REGEX = "\\s*console\\s*" + Pattern.quote(".") + "\\s*log\\s*" + Pattern.quote("(") + "\\s*";
    String CONSOLE_LOG_END_REGEX = "\\s*" + Pattern.quote(",") + ".*" + Pattern.quote(")") + "\\s*" + ";?";
    String CONSOLE_LOG_BEGIN_REGEX_WITHOUT_START_SPACE = "console\\s*" + Pattern.quote(".") + "\\s*log\\s*" + Pattern.quote("(") + "\\s*";
    String ALL_REGEX = ".*";
    /**
     * 注释符号偏移长度
     */
    Integer COMMENT_SIGNAL_LENGTH = 3;

    /**
     * 默认的console打印信息
     */
    String DEFAULT_CONSOLE_LOG_MSG = "🚀 ~ " + VariableAlias.METHOD_COMMAND.getKey()
            + " ~ " + VariableAlias.VARIABLE_COMMAND.getKey() + ": ";

    enum VariableAlias {
        /**
         * 设置别名：变量名
         */
        VARIABLE_COMMAND("${variableName}"),
        /**
         * 设置别名：方法名
         */
        METHOD_COMMAND("${methodName}");

        final String key;

        VariableAlias(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    enum AliasRegex {
        /**
         * 设置别名：用于正则匹配的变量名
         */
        VARIABLE_REGEX(Pattern.quote("${variableName}")),
        /**
         * 设置别名：用于正则匹配的方法名
         */
        METHOD_REGEX(Pattern.quote("${methodName}"));

        final String key;

        AliasRegex(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
}
