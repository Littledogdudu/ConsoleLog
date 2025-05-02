package com.sky.consolelog.entities;

/**
 * 侧边工具栏表达式信息实体类
 *
 * @author SkySource
 * @Date: 2025/4/14 18:12
 */
public class ConsoleLogSearchInfo {
    /** 打印表达式文本 */
    private String text;
    /** 打印表达式所在行 */
    private Integer line;
    /** 打印表达式末尾偏移量 */
    private Integer endOffset;

    public ConsoleLogSearchInfo(String text, Integer line, Integer endOffset) {
        this.text = text;
        this.line = line;
        this.endOffset = endOffset;
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

    public Integer getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(Integer endOffset) {
        this.endOffset = endOffset;
    }
}
