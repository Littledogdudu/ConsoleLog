package com.sky.consolelog.utils;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionExpression;
import com.intellij.psi.PsiElement;
import com.sky.consolelog.entities.ScopeOffset;
import org.jetbrains.annotations.NotNull;

import static com.sky.consolelog.constant.PsiPosition.*;

/**
 * @author by: SkySource
 * @Description:
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
            case JSVarStatement -> getJSVarStatement(element);
            case JSAssignmentExpression -> getJSAssignmentExpression(element);
            case JSIfStatement -> getJSIfStatement(element);
            case JSWhileStatement -> getJSWhileStatement(element);
            // for语句直接放下一行罢
            case TypeScriptParameterList -> getTypeScriptParameterList(element);
            case TypeScriptFunctionExpression -> getTypeScriptFunctionExpression(element);
            case JSCallExpression -> getJSCallExpression(element);
            case JSExpressionStatement -> getJSExpressionStatement(element);
            default -> null;
        };
    }

    private static ScopeOffset getJSVarStatement(PsiElement element) {
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


    private static ScopeOffset getJSIfStatement(PsiElement element) {
        return blockStatementIsChild(element);
    }

    private static ScopeOffset getJSWhileStatement(PsiElement element) {
        return blockStatementIsLastChild(element);
    }

    private static ScopeOffset getTypeScriptParameterList(PsiElement element) {
        return getTypeScriptFunctionExpression(element.getParent());
    }

    /**
     * TypeScriptFunctionExpression和TypeScriptFunctionProperty类型均适用
     * @param element Psi元素
     * @return 末尾偏移量
     */
    private static ScopeOffset getTypeScriptFunctionExpression(PsiElement element) {
        return blockStatementIsLastChild(element);
    }

    private static ScopeOffset getJSCallExpression(PsiElement element) {
        PsiElement parent = element.getParent();
        switch (parent.toString()) {
            case JSReferenceExpression_then:
                // 获取调用表达式元素
                PsiElement callElement = parent.getParent();
                // 获取到该调用表达式的参数列表元素
                PsiElement argumentListElement = callElement.getLastChild();
                // 获取函数部分
                @NotNull PsiElement[] children = argumentListElement.getChildren();
                for (@NotNull PsiElement child : children) {
                    if (child instanceof TypeScriptFunctionExpression) {
                        return getTypeScriptFunctionExpression(child);
                    }
                }
                break;
            case JSExpressionStatement:
                return getJSExpressionStatement(parent);
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

    private static ScopeOffset blockStatementIsLastChild(PsiElement element) {
        ScopeOffset offset = new ScopeOffset();
        PsiElement blockStatement_LBRACE = element.getLastChild().getFirstChild();
        offset.setInsertEndOffset(blockStatement_LBRACE.getTextRange().getEndOffset());
        offset.setNeedTab(true);
        return offset;
    }

    private static ScopeOffset blockStatementIsChild(PsiElement element) {
        ScopeOffset offset = new ScopeOffset();
        for (@NotNull PsiElement child : element.getChildren()) {
            if (JSBlockStatement.equals(child.toString())) {
                offset.setInsertEndOffset(child.getFirstChild().getTextRange().getEndOffset());
                offset.setNeedTab(true);
                return offset;
            }
        }
        return getDefault(element);
    }

    public static ScopeOffset getDefault(PsiElement element) {
        ScopeOffset offset = new ScopeOffset();
        offset.setInsertEndOffset(element.getTextRange().getEndOffset());
        offset.setNeedTab(false);
        offset.setDefault(true);
        return offset;
    }
}
