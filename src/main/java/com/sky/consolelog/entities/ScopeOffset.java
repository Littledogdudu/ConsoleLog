package com.sky.consolelog.entities;

/**
 * 记录当前元素PSI信息
 *
 * @author SkySource
 * @Date: 2025/2/13 17:40
 */
public class ScopeOffset {
    /** 需要插入的位置 */
    private Integer insertEndOffset;
    /** 是否需要额外的制表符（tab）对齐 */
    private Boolean needTab = false;
    /** 是否为默认插入方法：换行插入 */
    private Boolean isDefault = false;
    /** 句首是否换行 */
    private Boolean needBegLine = true;
    /** 末尾是否换行 */
    private Boolean needEndLine = false;

    public ScopeOffset() {}

    public Integer getInsertEndOffset() {
        return insertEndOffset;
    }

    public void setInsertEndOffset(Integer insertEndOffset) {
        this.insertEndOffset = insertEndOffset;
    }

    public Boolean getNeedTab() {
        return needTab;
    }

    public void setNeedTab(Boolean needTab) {
        this.needTab = needTab;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public void setDefault(Boolean aDefault) {
        isDefault = aDefault;
    }

    public Boolean getNeedBegLine() {
        return needBegLine;
    }

    public void setNeedBegLine(Boolean needBegLine) {
        this.needBegLine = needBegLine;
    }

    public Boolean getNeedEndLine() {
        return needEndLine;
    }

    public void setNeedEndLine(Boolean needEndLine) {
        this.needEndLine = needEndLine;
    }
}
