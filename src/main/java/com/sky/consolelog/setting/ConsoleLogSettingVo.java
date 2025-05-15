package com.sky.consolelog.setting;

/**
 * consoleLog变量实体类
 *
 * @author SkySource
 * @Date: 2025/1/25 11:55
 */
public class ConsoleLogSettingVo {
    private String variableName;
    private String methodName;
    private Integer lineNumber;
    private String fileName;

    public ConsoleLogSettingVo() {
        this.variableName = "";
        this.methodName = "";
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
