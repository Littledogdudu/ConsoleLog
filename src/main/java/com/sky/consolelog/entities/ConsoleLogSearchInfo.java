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
    /** 节点级别（类树状结构） */
    private Integer level = 0;

    public ConsoleLogSearchInfo(String text, Integer line, Integer endOffset, Integer level) {
        this.text = text;
        this.line = line;
        this.endOffset = endOffset;
        this.level = level;
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

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }
}
