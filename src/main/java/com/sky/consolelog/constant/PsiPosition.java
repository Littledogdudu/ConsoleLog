package com.sky.consolelog.constant;

import java.util.Arrays;
import java.util.List;

/**
 * @author by: SkySource
 * @Description:
 * @Date: 2025/2/17 21:38
 */
public interface PsiPosition {
    // 创建变量语句
    String JSVarStatement = "JSVarStatement";
    // 赋值表达式
    String JSAssignmentExpression = "JSAssignmentExpression";
    // 调用表达式
    // update(s: string)
    String JSCallExpression = "JSCallExpression";
    // 包含then的调用表达式
    // update(s: string).then(() => {})
    String JSReferenceExpression_then = "JSReferenceExpression:then";
    // 参数列表
    String TypeScriptParameterList = "TypeScriptParameterList";
    // 函数表达式
    //res => {}
    String JSFunctionExpression = "JSFunctionExpression";
    String TypeScriptFunctionExpression = "TypeScriptFunctionExpression";
    // 表达式语句
    //ElMessage({
    //    message: "修改成功",
    //            type: "success"
    //});
    String JSExpressionStatement = "JSExpressionStatement";
    // if语句
    //if (this.czShgg == "0") {
    //    this.nreditor.disable();
    //} else if (this.czShgg == "1") {}
    String JSIfStatement = "JSIfStatement";
    // while语句
    String JSWhileStatement = "JSWhileStatement";
    // for(let x of a)语句
    String JSForInStatement = "JSForInStatement";
    String JSForStatement = "JSForStatement";
    List<String> JSForStatementList = Arrays.asList(JSForInStatement, JSForStatement);

    String JSBlockStatement = "JSBlockStatement";
}
