package com.sky.consolelog.entities;

/**
 * 用于更新编辑器中指定位置文本的专用实体类
 * <p>
 * 当前用于更新行数
 *
 * @author SkySource
 * @Date: 2025/7/3 19:33
 */
public class UpdateEntity {
    Integer startOffset;
    Integer endOffset;
    String text;

    public UpdateEntity(Integer startOffset, Integer endOffset, String text) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.text = text;
    }

    public Integer getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(Integer startOffset) {
        this.startOffset = startOffset;
    }

    public Integer getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(Integer endOffset) {
        this.endOffset = endOffset;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}