package com.sky.consolelog.utils;

import com.intellij.lang.javascript.psi.JSFunctionExpression;
import com.intellij.openapi.editor.Caret;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.sky.consolelog.constant.PsiPosition;
import com.sky.consolelog.entities.ScopeOffset;
import org.jetbrains.annotations.NotNull;

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
            case PsiPosition.Variable.JS_VAR_STATEMENT -> getJSVarStatement(element);
            case PsiPosition.Variable.JS_ASSIGNMENT_EXPRESSION -> getJSAssignmentExpression(element);
            case PsiPosition.Condition.JS_IF_STATEMENT,
                 PsiPosition.Condition.JS_SWITCH_STATEMENT,
                 PsiPosition.Condition.JS_TRY_STATEMENT,
                 PsiPosition.Condition.JS_ELSE_CLAUSE,
                 PsiPosition.Loop.JS_WHILE_STATEMENT,
                 PsiPosition.Loop.JS_FOR_STATEMENT,
                 PsiPosition.Loop.JS_FOR_IN_STATEMENT,
                 PsiPosition.Expression.JS_FUNCTION_PROPERTY,
                 PsiPosition.Expression.TYPE_SCRIPT_FUNCTION_PROPERTY,
                 PsiPosition.Expression.JS_FUNCTION_EXPRESSION,
                 PsiPosition.Expression.TYPE_SCRIPT_FUNCTION_EXPRESSION,
                 PsiPosition.Expression.JS_FUNCTION,
                 PsiPosition.Expression.TYPE_SCRIPT_FUNCTION,
                 PsiPosition.Expression.JS_CLASS,
                 PsiPosition.Exception.JS_CATCH_BLOCK -> getMiddleBlockStatement(element);
            case PsiPosition.Condition.JS_CASE_CLAUSE -> getAfterColon(element);
            case PsiPosition.Loop.JS_DO_WHILE_STATEMENT -> getMiddleBlockStatementBeforeEnd(element);
            case PsiPosition.Expression.JS_CALL_EXPRESSION -> getJSCallExpression(element);
            case PsiPosition.Expression.JS_EXPRESSION_STATEMENT -> getJSExpressionStatement(element);
            case PsiPosition.Expression.JS_ARROW_FUNCTION -> getArrowFunction(element);
            case PsiPosition.Expression.JSX_ELEMENT -> getJSXElement(element);
            case PsiPosition.Expression.JS_RETURN_STATEMENT -> getReturnStatement(element);
            case PsiPosition.Expression.JS_OBJECT_LITERAL_EXPRESSION -> getObjectLiteralExpression(element);
            case PsiPosition.Template.XML_TAG,
                 PsiPosition.Template.HTML_TAG -> getXmlTag(element);
            default -> null;
        };
    }

    private static ScopeOffset getJSVarStatement(PsiElement element) {
        PsiElement parent = element.getParent();
        if (parent != null && PsiPosition.Loop.JS_FOR_STATEMENT_LIST.contains(parent.toString())) {
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
            case PsiPosition.Expression.JS_REFERENCE_EXPRESSION_THEN:
                // 获取调用表达式元素
                PsiElement callElement = parent.getParent();
                // 获取到该调用表达式的参数列表元素
                PsiElement argumentListElement = callElement.getLastChild();
                // 获取函数部分
                @NotNull PsiElement[] children = argumentListElement.getChildren();
                for (@NotNull PsiElement child : children) {
                    if (child instanceof JSFunctionExpression
                        || PsiPosition.Expression.JS_ARROW_FUNCTION.equals(child.toString())) {
                        return getMiddleBlockStatement(child);
                    }
                }
                break;
            case PsiPosition.Expression.JS_EXPRESSION_STATEMENT,
                 PsiPosition.Condition.JS_IF_STATEMENT:
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
     * 箭头函数处理：有 {} 块体 → 块内插入；表达式体 → 元素末尾
     */
    private static ScopeOffset getArrowFunction(PsiElement element) {
        for (@NotNull PsiElement child : element.getChildren()) {
            if (PsiPosition.JS_BLOCK_STATEMENT.equals(child.toString())) {
                return getMiddleBlockStatement(element);
            }
        }
        // 表达式体 () => expr，无法插入块级 console.log，fallback 到元素末尾
        return getDefault(element);
    }

    /**
     * JSX 元素：向上查找最近的函数/类作用域，在其块体内插入
     */
    private static ScopeOffset getJSXElement(PsiElement element) {
        PsiElement current = element.getParent();
        while (current != null) {
            String name = current.toString();
            int idx = name.indexOf(":");
            if (idx != -1) name = name.substring(0, idx);

            if (PsiPosition.Expression.JS_FUNCTION.equals(name)
                || PsiPosition.Expression.JS_FUNCTION_EXPRESSION.equals(name)
                || PsiPosition.Expression.TYPE_SCRIPT_FUNCTION.equals(name)
                || PsiPosition.Expression.TYPE_SCRIPT_FUNCTION_EXPRESSION.equals(name)
                || PsiPosition.Expression.JS_ARROW_FUNCTION.equals(name)
                || PsiPosition.Expression.JS_CLASS.equals(name)
                || PsiPosition.Expression.JS_FUNCTION_PROPERTY.equals(name)
                || PsiPosition.Expression.TYPE_SCRIPT_FUNCTION_PROPERTY.equals(name)) {
                return getMiddleBlockStatement(current);
            }
            current = current.getParent();
        }
        return getDefault(element);
    }

    /**
     * return 语句：在 return 之前插入（return 之后是不可达代码）
     */
    private static ScopeOffset getReturnStatement(PsiElement element) {
        ScopeOffset offset = new ScopeOffset();
        offset.setInsertEndOffset(element.getTextRange().getStartOffset());
        offset.setNeedTab(true);
        offset.setNeedEndLine(true);
        return offset;
    }

    /**
     * 对象字面量：在花括号后、第一个属性前插入
     */
    private static ScopeOffset getObjectLiteralExpression(PsiElement element) {
        ScopeOffset offset = new ScopeOffset();
        offset.setInsertEndOffset(element.getFirstChild().getTextRange().getEndOffset());
        offset.setNeedTab(true);
        offset.setNeedEndLine(true);
        return offset;
    }

    /**
     * Vue template 标签：向上查找 &lt;script&gt; 块，在其中插入
     */
    private static ScopeOffset getXmlTag(PsiElement element) {
        PsiElement current = element;
        while (current != null && !(current instanceof PsiFile)) {
            current = current.getParent();
        }
        if (current != null) {
            for (PsiElement child : current.getChildren()) {
                if (child.toString().contains("script")) {
                    return getMiddleBlockStatement(child);
                }
            }
        }
        return getDefault(element);
    }

    /**
     * 插入到作用域块的中间
     */
    private static ScopeOffset getMiddleBlockStatement(PsiElement element) {
        return getBlockOffset(element, false);
    }

    /**
     * 插入到作用域块的中间，且插入位置置于作用域最后
     */
    private static ScopeOffset getMiddleBlockStatementBeforeEnd(PsiElement element) {
        return getBlockOffset(element, true);
    }

    /**
     * 找到子节点中的 JSBlockStatement 并返回偏移量
     */
    private static ScopeOffset getBlockOffset(PsiElement element, boolean beforeEnd) {
        ScopeOffset offset = new ScopeOffset();
        for (@NotNull PsiElement child : element.getChildren()) {
            if (PsiPosition.JS_BLOCK_STATEMENT.equals(child.toString())) {
                if (beforeEnd) {
                    offset.setInsertEndOffset(child.getLastChild().getTextRange().getStartOffset());
                    offset.setNeedBegLine(false);
                } else {
                    offset.setInsertEndOffset(child.getFirstChild().getTextRange().getEndOffset());
                }
                offset.setNeedTab(true);
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
            if (PsiPosition.COLON_SIGNAL.equals(child.toString())) {
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

    /**
     * 默认行为：换行且不需要额外的制表符对齐
     */
    public static ScopeOffset getUndefinedDefault(Caret caret) {
        ScopeOffset offset = new ScopeOffset();
        offset.setInsertEndOffset(caret.getOffset());
        offset.setNeedTab(false);
        offset.setDefault(false);
        return offset;
    }
}
