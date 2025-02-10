package com.sky.consolelog.constant;

import java.util.regex.Pattern;

/**
 * @author by: SkySource
 * @Description:
 * @Date: 2025/1/25 12:41
 */
public interface SettingConstant {
    String CONSOLE_LOG_COMMAND = "console.log(\"";
    String CONSOLE_LOG_BEGIN_REGEX = "\\s*console\\s*" + Pattern.quote(".") + "\\s*log\\s*" + Pattern.quote("(") + "\\s*" + Pattern.quote("\"");
    String CONSOLE_LOG_END_REGEX = Pattern.quote("\"") + "\\s*" + Pattern.quote(",") + ".*" + Pattern.quote(")") + "\\s*" + ";?";
    String ALL_REGEX = ".*";
    Integer COMMENT_SIGNAL_LENGTH = 3;

    String DEFAULT_CONSOLE_LOG_MSG = "🚀 ~ " + VariableAlias.METHOD_COMMAND.getKey()
            + " ~ " + VariableAlias.VARIABLE_COMMAND.getKey() + ": ";

    enum VariableAlias {
        VARIABLE_COMMAND("${variableName}"),
        METHOD_COMMAND("${methodName}")
        ;

        final String key;

        VariableAlias(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    enum AliasRegex {
        VARIABLE_REGEX(Pattern.quote("${variableName}")),
        METHOD_REGEX(Pattern.quote("${methodName}"))
        ;

        final String key;

        AliasRegex(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
}
