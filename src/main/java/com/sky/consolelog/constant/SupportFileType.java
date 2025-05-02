package com.sky.consolelog.constant;

import java.util.List;

/**
 * 支持的文件类型常量
 *
 * @author SkySource
 * @Date: 2025/4/21 10:22
 */
public interface SupportFileType {
    String VUE = "Vue.js";
    String ECMASCRIPT6 = "ECMAScript 6";
    List<String> JAVASCRIPT = List.of(
            "JavaScript",
            "EJS"
    );
    List<String> TYPESCRIPT = List.of(
            "TypeScript",
            "TypeScript JSX"
    );
    String TEXT = "TEXT";

    List<String> SUPPORT_FILE_TYPE = List.of(
            "Vue.js",
            "EJS",
            "TEXT",
            "JavaScript",
            "TypeScript",
            "TypeScript JSX",
            "ECMAScript 6"
    );
}
