package com.sky.consolelog.entities;

/**
 * 侧边工具栏表达式信息实体类
 *
 * @author SkySource
 * @Date: 2025/4/14 18:12
 */
public class ConsoleLogSearchInfo {
    private String text;
    private Integer line;

    public ConsoleLogSearchInfo(String text, Integer line) {
        this.text = text;
        this.line = line;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }
}
