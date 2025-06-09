package com.sky.consolelog.constant;

import java.util.Arrays;
import java.util.List;

/**
 * PSI树结构名常量
 * @author SkySource
 * @Date: 2025/2/17 21:38
 */
public interface PsiPosition {
    /**
     * 变量
     */
    interface Variable {
        /**
         * 创建变量语句
         * const variable = "var";
         */
        String JS_ASSIGNMENT_EXPRESSION = "JSAssignmentExpression";
        /**
         * 赋值表达式
         * variable = "var";
         */
        String JS_VAR_STATEMENT = "JSVarStatement";
    }

    /**
     * 表达式
     */
    interface Expression {
        /**
         * 调用表达式
         * update(s: string)
         */
        String JS_CALL_EXPRESSION = "JSCallExpression";
        /**
         * update(s: string).then(() => {})
         * 包含then的调用表达式
         */
        String JS_REFERENCE_EXPRESSION_THEN = "JSReferenceExpression:then";
        /** 参数列表 */
        String TYPE_SCRIPT_PARAMETER_LIST = "TypeScriptParameterList";
        /** 函数表达式 */
        String JS_FUNCTION_EXPRESSION = "JSFunctionExpression";
        String TYPE_SCRIPT_FUNCTION_EXPRESSION = "TypeScriptFunctionExpression";
        String JS_FUNCTION_PROPERTY = "JSFunctionProperty";
        /**
         * 表达式语句<br/>
         * ElMessage({<br/>
         * &nbsp;&nbsp;&nbsp;&nbsp;message: "修改成功",<br/>
         * &nbsp;&nbsp;&nbsp;&nbsp;type: "success"<br/>
         * });
         */
        String JS_EXPRESSION_STATEMENT = "JSExpressionStatement";
        String JS_FUNCTION = "JSFunction";
    }

    /**
     * 分支（if）语句
     */
    interface Condition {
        /**
         * if语句<br/>
         * if (this.czShgg == "0") {<br/>
         * &nbsp;&nbsp;&nbsp;&nbsp;this.nreditor.disable();<br/>
         * } else if (this.czShgg == "1") {}
         */
        String JS_IF_STATEMENT = "JSIfStatement";
        String JS_SWITCH_STATEMENT = "JSSwitchStatement";
        String JS_CASE_CLAUSE = "JSCaseClause";
    }

    /**
     * 循环语句
     */
    interface Loop {
        /**
         * while语句
         */
        String JS_WHILE_STATEMENT = "JSWhileStatement";
        /** do while语句 */
        String JS_DO_WHILE_STATEMENT = "JSDoWhileStatement";
        /**
         * for(let x of a)语句
         */
        String JS_FOR_IN_STATEMENT = "JSForInStatement";
        String JS_FOR_STATEMENT = "JSForStatement";
        List<String> JS_FOR_STATEMENT_LIST = Arrays.asList(JS_FOR_IN_STATEMENT, JS_FOR_STATEMENT);
    }

    /**
     * 异常捕获
     */
    interface Exception {
        String JS_CATCH_BLOCK = "JSCatchBlock";
    }


    String JS_BLOCK_STATEMENT = "JSBlockStatement";
    String COLON_SIGNAL = "PsiElement(JS:COLON)";
}
