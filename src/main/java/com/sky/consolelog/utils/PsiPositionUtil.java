package com.sky.consolelog.utils;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionExpression;
import com.intellij.psi.PsiElement;
import com.sky.consolelog.entities.ScopeOffset;
import org.jetbrains.annotations.NotNull;

import static com.sky.consolelog.constant.PsiPosition.*;

/**
 * 获取插入位置的工具类
 *
 * @author SkySource
 * @Date: 2025/2/17 21:36
 */
public class PsiPositionUtil {
    public static ScopeOffset getScopeOffsetByType(PsiElement element) {
        int index = element.toString().indexOf(":");
        if (index != -1) {
            return getScopeOffsetByType(element, element.toString().substring(0, index));
        } else {
            return getScopeOffsetByType(element, element.toString());
        }
    }

    private static ScopeOffset getScopeOffsetByType(PsiElement element, String name) {
        return switch (name) {
            case Variable.JS_VAR_STATEMENT -> getJSVarStatement(element);
            case Variable.JS_ASSIGNMENT_EXPRESSION -> getJSAssignmentExpression(element);
            case Condition.JS_IF_STATEMENT,
                 Condition.JS_SWITCH_STATEMENT,
                 Loop.JS_WHILE_STATEMENT,
                 Loop.JS_FOR_STATEMENT,
                 Loop.JS_FOR_IN_STATEMENT,
                 Expression.JS_FUNCTION_PROPERTY,
                 Expression.JS_FUNCTION_EXPRESSION,
                 Expression.TYPE_SCRIPT_FUNCTION_EXPRESSION -> getMiddleBlockStatement(element);
            case Condition.JS_CASE_CLAUSE -> getAfterColon(element);
            case Loop.JS_DO_WHILE_STATEMENT -> getMiddleBlockStatementBeforeEnd(element);
            case Expression.JS_CALL_EXPRESSION -> getJSCallExpression(element);
            case Expression.JS_EXPRESSION_STATEMENT -> getJSExpressionStatement(element);
            default -> null;
        };
    }

    private static ScopeOffset getJSVarStatement(PsiElement element) {
        if (Loop.JS_FOR_STATEMENT_LIST.contains(element.getParent().toString())) {
            // for循环中的变量
            ScopeOffset offset = new ScopeOffset();
            offset.setInsertEndOffset(element.getTextRange().getEndOffset());
            offset.setNeedTab(true);
            offset.setDefault(true);
            return offset;
        }
        ScopeOffset offset = new ScopeOffset();
        // 创建变量表达式获取到变量创建结束（或有;）处结束
        offset.setInsertEndOffset(element.getLastChild().getTextRange().getEndOffset());
        offset.setNeedTab(false);
        return offset;
    }

    private static ScopeOffset getJSAssignmentExpression(PsiElement element) {
        ScopeOffset offset = new ScopeOffset();
        // 赋值表达式获取到赋值完毕后结束
        offset.setInsertEndOffset(element.getParent().getLastChild().getTextRange().getEndOffset());
        offset.setNeedTab(false);
        return offset;
    }

    private static ScopeOffset getJSCallExpression(PsiElement element) {
        PsiElement parent = element.getParent();
        switch (parent.toString()) {
            case Expression.JS_REFERENCE_EXPRESSION_THEN:
                // 获取调用表达式元素
                PsiElement callElement = parent.getParent();
                // 获取到该调用表达式的参数列表元素
                PsiElement argumentListElement = callElement.getLastChild();
                // 获取函数部分
                @NotNull PsiElement[] children = argumentListElement.getChildren();
                for (@NotNull PsiElement child : children) {
                    if (child instanceof TypeScriptFunctionExpression) {
                        return getMiddleBlockStatement(child);
                    }
                }
                break;
            case Expression.JS_EXPRESSION_STATEMENT:
                return getMiddleBlockStatement(parent);
            default:
                break;
        }
        return getDefault(element);
    }

    private static ScopeOffset getJSExpressionStatement(PsiElement element) {
        ScopeOffset offset = new ScopeOffset();
        PsiElement endElement = element.getLastChild();
        offset.setInsertEndOffset(endElement.getTextRange().getEndOffset());
        offset.setNeedTab(false);
        return offset;
    }

    /**
     * 插入到作用域块的中间
     */
    private static ScopeOffset getMiddleBlockStatement(PsiElement element) {
        ScopeOffset offset = new ScopeOffset();
        for (@NotNull PsiElement child : element.getChildren()) {
            if (JS_BLOCK_STATEMENT.equals(child.toString())) {
                offset.setInsertEndOffset(child.getFirstChild().getTextRange().getEndOffset());
                offset.setNeedTab(true);
                offset.setNeedEndLine(true);
                return offset;
            }
        }
        return getDefault(element);
    }

    /**
     * 插入到作用域块的中间，且插入位置置于作用域最后
     */
    private static ScopeOffset getMiddleBlockStatementBeforeEnd(PsiElement element) {
        ScopeOffset offset = new ScopeOffset();
        for (@NotNull PsiElement child : element.getChildren()) {
            if (JS_BLOCK_STATEMENT.equals(child.toString())) {
                offset.setInsertEndOffset(child.getLastChild().getTextRange().getStartOffset());
                offset.setNeedTab(true);
                offset.setNeedBegLine(false);
                offset.setNeedEndLine(true);
                return offset;
            }
        }
        return getDefault(element);
    }

    /**
     * 插入到分号之后
     */
    private static ScopeOffset getAfterColon(PsiElement element) {
        ScopeOffset offset = new ScopeOffset();
        for (@NotNull PsiElement child : element.getChildren()) {
            if (COLON_SIGNAL.equals(child.toString())) {
                offset.setInsertEndOffset(child.getTextRange().getEndOffset());
                offset.setNeedTab(true);
                return offset;
            }
        }
        return getDefault(element);
    }

    /**
     * 带额外制表符对齐的默认行为：换行且额外的制表符对齐
     */
    public static ScopeOffset getAlignDefault(PsiElement element) {
        ScopeOffset offset = new ScopeOffset();
        offset.setInsertEndOffset(element.getTextRange().getEndOffset());
        offset.setNeedTab(true);
        offset.setDefault(true);
        return offset;
    }

    /**
     * 默认行为：换行且不需要额外的制表符对齐
     */
    public static ScopeOffset getDefault(PsiElement element) {
        ScopeOffset offset = new ScopeOffset();
        offset.setInsertEndOffset(element.getTextRange().getEndOffset());
        offset.setNeedTab(false);
        offset.setDefault(true);
        return offset;
    }
}
