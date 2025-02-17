package com.sky.consolelog.entities;

/**
 * @author by: SkySource
 * @Description: 记录当前元素PSI信息
 * @Date: 2025/2/13 17:40
 */
public class ScopeOffset {
    private Integer insertEndOffset;
    private Boolean needTab = false;
    private Boolean isDefault = false;

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
}
