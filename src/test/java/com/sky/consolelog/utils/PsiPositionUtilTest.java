package com.sky.consolelog.utils;

import com.intellij.lang.javascript.psi.JSFunctionExpression;
import com.intellij.openapi.editor.Caret;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.sky.consolelog.action.InsertConsoleLogAction;
import com.sky.consolelog.constant.PsiPosition;
import com.sky.consolelog.entities.ScopeOffset;
import org.junit.Test;

import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * {@link PsiPositionUtil} 插入位置计算单元测试。
 *
 * <p>测试通过 {@link FakePsiElement} 模拟 WebStorm JavaScript/TypeScript 插件的真实 PSI 树结构，
 * 逐一验证 {@link PsiPositionUtil#getScopeOffsetByType(PsiElement)} 的全部语法分支
 * （变量、条件、循环、表达式、异常捕获、Vue 模板），以及
 * {@link InsertConsoleLogAction#findScopeOffset(PsiElement)} 的向上查找逻辑。
 *
 * <p>每个用例都验证 {@link ScopeOffset} 的五个字段：
 * <ul>
 *   <li>{@code insertEndOffset} 插入偏移量</li>
 *   <li>{@code needTab} 是否需要额外制表符对齐</li>
 *   <li>{@code isDefault} 是否默认换行插入</li>
 *   <li>{@code needBegLine} 句首是否换行</li>
 *   <li>{@code needEndLine} 末尾是否换行</li>
 * </ul>
 */
public class PsiPositionUtilTest {

    // =====================================================================
    // 辅助方法
    // =====================================================================

    private static FakePsiElement fake(String type, String text) {
        return new FakePsiElement(type, text);
    }

    private static FakePsiElement fake(String type, String name, String text) {
        return new FakePsiElement(type, name, text);
    }

    /** 验证一次 getScopeOffsetByType 调用的完整结果。 */
    private static void assertScope(PsiElement element,
                                    int expectedOffset,
                                    boolean needTab, boolean isDefault,
                                    boolean needBegLine, boolean needEndLine) {
        ScopeOffset off = PsiPositionUtil.getScopeOffsetByType(element);
        assertNotNull("getScopeOffsetByType 不应返回 null: " + element, off);
        assertScopeFields(off, element, expectedOffset, needTab, isDefault, needBegLine, needEndLine);
    }

    private static void assertScopeFields(ScopeOffset off, PsiElement element,
                                          int expectedOffset,
                                          boolean needTab, boolean isDefault,
                                          boolean needBegLine, boolean needEndLine) {
        assertEquals("insertEndOffset 错误 [" + element + "]", Integer.valueOf(expectedOffset), off.getInsertEndOffset());
        assertEquals("needTab 错误 [" + element + "]", needTab, off.getNeedTab());
        assertEquals("isDefault 错误 [" + element + "]", isDefault, off.getDefault());
        assertEquals("needBegLine 错误 [" + element + "]", needBegLine, off.getNeedBegLine());
        assertEquals("needEndLine 错误 [" + element + "]", needEndLine, off.getNeedEndLine());
    }

    /** 创建实现 JSFunctionExpression 接口的代理（用于 instanceof 判断）。 */
    private static JSFunctionExpression jsFunctionExpression(FakePsiElement inner) {
        return (JSFunctionExpression) Proxy.newProxyInstance(
                PsiPositionUtilTest.class.getClassLoader(),
                new Class<?>[]{JSFunctionExpression.class, FakePsiElement.FakePsiWrapper.class},
                (proxy, method, args) -> {
                    if ("inner".equals(method.getName())) {
                        return inner;
                    }
                    return delegateToFake(method.getName(), method.getReturnType(), inner, false, null);
                });
    }

    /** 创建实现 PsiFile 接口的代理（用于 getXmlTag 的 PsiFile instanceof 判断）。 */
    private static PsiFile psiFile(FakePsiElement inner) {
        return (PsiFile) Proxy.newProxyInstance(
                PsiPositionUtilTest.class.getClassLoader(),
                new Class<?>[]{PsiFile.class},
                (proxy, method, args) -> delegateToFake(method.getName(), method.getReturnType(), inner, false, null));
    }

    /** 创建实现 Caret 接口的代理（仅 getOffset 有意义）。 */
    private static Caret caret(int offset) {
        return (Caret) Proxy.newProxyInstance(
                PsiPositionUtilTest.class.getClassLoader(),
                new Class<?>[]{Caret.class},
                (proxy, method, args) -> {
                    if ("getOffset".equals(method.getName())) {
                        return offset;
                    }
                    return defaultByReturnType(method.getReturnType());
                });
    }

    private static Object delegateToFake(String methodName, Class<?> returnType, FakePsiElement inner,
                                         boolean physical, Object fallback) {
        switch (methodName) {
            case "toString":
                return inner.toString();
            case "getChildren":
                return inner.getChildren();
            case "getFirstChild":
                return inner.getFirstChild();
            case "getLastChild":
                return inner.getLastChild();
            case "getParent":
                return inner.getParent();
            case "getText":
                return inner.getText();
            case "getTextRange":
                return inner.getTextRange();
            case "getTextOffset":
                return inner.getTextOffset();
            case "getTextLength":
                return inner.getTextLength();
            default:
                return defaultByReturnType(returnType);
        }
    }

    private static Object defaultByReturnType(Class<?> returnType) {
        if (returnType == boolean.class) return false;
        if (returnType == int.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == double.class) return 0d;
        if (returnType == float.class) return 0f;
        if (returnType == short.class) return (short) 0;
        if (returnType == byte.class) return (byte) 0;
        if (returnType == char.class) return (char) 0;
        if (returnType == void.class) return null;
        if (returnType.isArray()) return java.lang.reflect.Array.newInstance(returnType.getComponentType(), 0);
        return null;
    }

    // =====================================================================
    // 一、变量语句 JSVarStatement（PsiPosition.Variable.JS_VAR_STATEMENT）
    // =====================================================================

    @Test
    public void testVarStatementConstWithSemicolon() {
        // const TARGET = 1;
        FakePsiElement stmt = fake("JSVarStatement", "const TARGET = 1;")
                .child(fake("PsiElement(JS:VAR_KEYWORD)", "const"))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:EQ)", "="))
                .child(fake("JSLiteralExpression", "1"))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        // 最后一个子节点是分号：插入到分号之后
        assertScope(stmt, stmt.getTextRange().getEndOffset(), false, false, true, false);
    }

    @Test
    public void testVarStatementLetWithSemicolon() {
        // let TARGET = 1;
        FakePsiElement stmt = fake("JSVarStatement", "let TARGET = 1;")
                .child(fake("PsiElement(JS:VAR_KEYWORD)", "let"))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:EQ)", "="))
                .child(fake("JSLiteralExpression", "1"))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        assertScope(stmt, stmt.getTextRange().getEndOffset(), false, false, true, false);
    }

    @Test
    public void testVarStatementVarWithSemicolon() {
        // var TARGET = 1;
        FakePsiElement stmt = fake("JSVarStatement", "var TARGET = 1;")
                .child(fake("PsiElement(JS:VAR_KEYWORD)", "var"))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:EQ)", "="))
                .child(fake("JSLiteralExpression", "1"))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        assertScope(stmt, stmt.getTextRange().getEndOffset(), false, false, true, false);
    }

    @Test
    public void testVarStatementWithoutSemicolonAsi() {
        // const TARGET = 1   （ASI，无分号）
        FakePsiElement stmt = fake("JSVarStatement", "const TARGET = 1")
                .child(fake("PsiElement(JS:VAR_KEYWORD)", "const"))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:EQ)", "="))
                .child(fake("JSLiteralExpression", "1"));
        // 无分号时最后一个子节点是初始化表达式，插入到其末尾
        assertScope(stmt, stmt.getTextRange().getEndOffset(), false, false, true, false);
    }

    @Test
    public void testVarStatementMultipleDeclarators() {
        // const TARGET = 1, b = 2;
        FakePsiElement stmt = fake("JSVarStatement", "const TARGET = 1, b = 2;")
                .child(fake("PsiElement(JS:VAR_KEYWORD)", "const"))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:EQ)", "="))
                .child(fake("JSLiteralExpression", "1"))
                .child(fake("PsiElement(JS:COMMA)", ","))
                .child(fake("JSReferenceExpression", "b"))
                .child(fake("PsiElement(JS:EQ)", "="))
                .child(fake("JSLiteralExpression", "2"))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        assertScope(stmt, stmt.getTextRange().getEndOffset(), false, false, true, false);
    }

    @Test
    public void testVarStatementObjectDestructuring() {
        // const { a: TARGET } = obj;
        FakePsiElement stmt = fake("JSVarStatement", "const { a: TARGET } = obj;")
                .child(fake("PsiElement(JS:VAR_KEYWORD)", "const"))
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSReferenceExpression", "a"))
                .child(fake("PsiElement(JS:COLON)", ":"))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:RBRACE)", "}"))
                .child(fake("PsiElement(JS:EQ)", "="))
                .child(fake("JSReferenceExpression", "obj"))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        assertScope(stmt, stmt.getTextRange().getEndOffset(), false, false, true, false);
    }

    @Test
    public void testVarStatementArrayDestructuring() {
        // const [TARGET] = arr;
        FakePsiElement stmt = fake("JSVarStatement", "const [TARGET] = arr;")
                .child(fake("PsiElement(JS:VAR_KEYWORD)", "const"))
                .child(fake("PsiElement(JS:LBRACKET)", "["))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:RBRACKET)", "]"))
                .child(fake("PsiElement(JS:EQ)", "="))
                .child(fake("JSReferenceExpression", "arr"))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        assertScope(stmt, stmt.getTextRange().getEndOffset(), false, false, true, false);
    }

    @Test
    public void testVarStatementTypeScriptTyped() {
        // const TARGET: number = 1;   （TS 类型注解）
        FakePsiElement stmt = fake("JSVarStatement", "const TARGET: number = 1;")
                .child(fake("PsiElement(JS:VAR_KEYWORD)", "const"))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:COLON)", ":"))
                .child(fake("JSType", "number"))
                .child(fake("PsiElement(JS:EQ)", "="))
                .child(fake("JSLiteralExpression", "1"))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        assertScope(stmt, stmt.getTextRange().getEndOffset(), false, false, true, false);
    }

    @Test
    public void testVarStatementInForLoop() {
        // for (let TARGET = 0; TARGET < 10; TARGET++) { foo(); }
        // JSVarStatement 的父节点是 JSForStatement -> 特殊处理：tab 对齐 + 默认
        FakePsiElement block = fake("JSBlockStatement", "{ foo(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "foo();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement varStmt = fake("JSVarStatement", "let TARGET = 0")
                .child(fake("PsiElement(JS:VAR_KEYWORD)", "let"))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:EQ)", "="))
                .child(fake("JSLiteralExpression", "0"));
        FakePsiElement forStmt = fake("JSForStatement", "for (let TARGET = 0; TARGET < 10; TARGET++) { foo(); }")
                .child(fake("PsiElement(JS:FOR_KEYWORD)", "for"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(varStmt)
                .child(fake("PsiElement(JS:SEMICOLON)", ";"))
                .child(fake("JSBinaryExpression", "TARGET < 10"))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"))
                .child(fake("JSUpdateExpression", "TARGET++"))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(block);
        // for 循环中的变量声明：插入到变量声明末尾，tab 对齐
        assertScope(varStmt, varStmt.getTextRange().getEndOffset(), true, true, true, false);
    }

    @Test
    public void testVarStatementInForInLoop() {
        // for (const TARGET in obj) { foo(); }
        FakePsiElement block = fake("JSBlockStatement", "{ foo(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "foo();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement varStmt = fake("JSVarStatement", "const TARGET")
                .child(fake("PsiElement(JS:VAR_KEYWORD)", "const"))
                .child(fake("JSReferenceExpression", "TARGET"));
        FakePsiElement forIn = fake("JSForInStatement", "for (const TARGET in obj) { foo(); }")
                .child(fake("PsiElement(JS:FOR_KEYWORD)", "for"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(varStmt)
                .child(fake("PsiElement(JS:IN_KEYWORD)", "in"))
                .child(fake("JSReferenceExpression", "obj"))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(block);
        assertScope(varStmt, varStmt.getTextRange().getEndOffset(), true, true, true, false);
    }

    @Test
    public void testVarStatementInForOfLoop() {
        // for (const TARGET of arr) { foo(); }
        FakePsiElement block = fake("JSBlockStatement", "{ foo(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "foo();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement varStmt = fake("JSVarStatement", "const TARGET")
                .child(fake("PsiElement(JS:VAR_KEYWORD)", "const"))
                .child(fake("JSReferenceExpression", "TARGET"));
        FakePsiElement forOf = fake("JSForInStatement", "for (const TARGET of arr) { foo(); }")
                .child(fake("PsiElement(JS:FOR_KEYWORD)", "for"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(varStmt)
                .child(fake("PsiElement(JS:OF_KEYWORD)", "of"))
                .child(fake("JSReferenceExpression", "arr"))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(block);
        assertScope(varStmt, varStmt.getTextRange().getEndOffset(), true, true, true, false);
    }

    // =====================================================================
    // 二、赋值表达式 JSAssignmentExpression（PsiPosition.Variable.JS_ASSIGNMENT_EXPRESSION）
    // =====================================================================

    private static FakePsiElement assignmentStatement(String code, FakePsiElement... assignChildren) {
        FakePsiElement assign = fake("JSAssignmentExpression", code);
        for (FakePsiElement c : assignChildren) {
            assign.child(c);
        }
        return fake("JSExpressionStatement", code + ";")
                .child(assign)
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
    }

    @Test
    public void testAssignmentExpressionSimple() {
        // TARGET = 1;  -> 父级表达式语句的最后一个子节点是分号
        FakePsiElement stmt = fake("JSExpressionStatement", "TARGET = 1;")
                .child(fake("JSAssignmentExpression", "TARGET = 1")
                        .child(fake("JSReferenceExpression", "TARGET"))
                        .child(fake("PsiElement(JS:EQ)", "="))
                        .child(fake("JSLiteralExpression", "1")))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        FakePsiElement assign = (FakePsiElement) stmt.getFirstChild();
        assertScope(assign, stmt.getTextRange().getEndOffset(), false, false, true, false);
    }

    @Test
    public void testAssignmentExpressionWithoutSemicolonAsi() {
        // TARGET = 1 （ASI 无分号）-> 父级最后一个子节点就是赋值表达式本身
        FakePsiElement stmt = fake("JSExpressionStatement", "TARGET = 1")
                .child(fake("JSAssignmentExpression", "TARGET = 1")
                        .child(fake("JSReferenceExpression", "TARGET"))
                        .child(fake("PsiElement(JS:EQ)", "="))
                        .child(fake("JSLiteralExpression", "1")));
        FakePsiElement assign = (FakePsiElement) stmt.getFirstChild();
        assertScope(assign, stmt.getTextRange().getEndOffset(), false, false, true, false);
    }

    @Test
    public void testAssignmentExpressionWithCall() {
        // TARGET = foo();
        FakePsiElement stmt = fake("JSExpressionStatement", "TARGET = foo();")
                .child(fake("JSAssignmentExpression", "TARGET = foo()")
                        .child(fake("JSReferenceExpression", "TARGET"))
                        .child(fake("PsiElement(JS:EQ)", "="))
                        .child(fake("JSCallExpression", "foo()")
                                .child(fake("JSReferenceExpression", "foo"))
                                .child(fake("PsiElement(JS:LPAR)", "("))
                                .child(fake("PsiElement(JS:RPAR)", ")"))))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        FakePsiElement assign = (FakePsiElement) stmt.getFirstChild();
        assertScope(assign, stmt.getTextRange().getEndOffset(), false, false, true, false);
    }

    @Test
    public void testAssignmentExpressionMemberAccess() {
        // obj.TARGET = 1;
        FakePsiElement stmt = fake("JSExpressionStatement", "obj.TARGET = 1;")
                .child(fake("JSAssignmentExpression", "obj.TARGET = 1")
                        .child(fake("JSReferenceExpression", "obj.TARGET"))
                        .child(fake("PsiElement(JS:EQ)", "="))
                        .child(fake("JSLiteralExpression", "1")))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        FakePsiElement assign = (FakePsiElement) stmt.getFirstChild();
        assertScope(assign, stmt.getTextRange().getEndOffset(), false, false, true, false);
    }

    @Test
    public void testAssignmentExpressionChained() {
        // a = TARGET = 1;  内层赋值表达式的父级是外层赋值表达式，父级最后子节点为 1
        FakePsiElement outerAssign = fake("JSAssignmentExpression", "a = TARGET = 1")
                .child(fake("JSReferenceExpression", "a"))
                .child(fake("PsiElement(JS:EQ)", "="))
                .child(fake("JSAssignmentExpression", "TARGET = 1")
                        .child(fake("JSReferenceExpression", "TARGET"))
                        .child(fake("PsiElement(JS:EQ)", "="))
                        .child(fake("JSLiteralExpression", "1")));
        FakePsiElement stmt = fake("JSExpressionStatement", "a = TARGET = 1;")
                .child(outerAssign)
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        FakePsiElement innerAssign = (FakePsiElement) outerAssign.getLastChild();
        // 内层赋值：父级（外层赋值）最后子节点是 1
        assertScope(innerAssign, innerAssign.getTextRange().getEndOffset(), false, false, true, false);
        // 外层赋值：父级（表达式语句）最后子节点是分号
        assertScope(outerAssign, stmt.getTextRange().getEndOffset(), false, false, true, false);
    }

    // =====================================================================
    // 三、块作用域语句（getMiddleBlockStatement）
    // =====================================================================

    /** 构造带 JSBlockStatement 子节点的语句。 */
    private static FakePsiElement withBlock(String type, String code, FakePsiElement... head) {
        FakePsiElement block = fake("JSBlockStatement", "{ TARGET(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "TARGET();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement stmt = fake(type, code);
        for (FakePsiElement h : head) {
            stmt.child(h);
        }
        stmt.child(block);
        return stmt;
    }

    @Test
    public void testIfStatementWithBlock() {
        // if (TARGET) { ... }  -> 插入到 { 之后
        FakePsiElement stmt = withBlock("JSIfStatement", "if (TARGET) { TARGET(); }",
                fake("PsiElement(JS:IF_KEYWORD)", "if"),
                fake("PsiElement(JS:LPAR)", "("),
                fake("JSReferenceExpression", "TARGET"),
                fake("PsiElement(JS:RPAR)", ")"));
        int expected = stmt.getLastChild().getFirstChild().getTextRange().getEndOffset();
        assertScope(stmt, expected, true, false, true, true);
    }

    @Test
    public void testIfStatementWithoutBlock() {
        // if (TARGET) foo();  -> 无块体，退化为默认（语句末尾换行）
        FakePsiElement stmt = fake("JSIfStatement", "if (TARGET) foo();")
                .child(fake("PsiElement(JS:IF_KEYWORD)", "if"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(fake("JSExpressionStatement", "foo();"));
        assertScope(stmt, stmt.getTextRange().getEndOffset(), false, true, true, false);
    }

    @Test
    public void testIfStatementEmptyBlock() {
        // if (TARGET) {}  -> 空块体也能在 { 后插入
        FakePsiElement block = fake("JSBlockStatement", "{ }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement stmt = fake("JSIfStatement", "if (TARGET) { }")
                .child(fake("PsiElement(JS:IF_KEYWORD)", "if"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(block);
        int expected = block.getFirstChild().getTextRange().getEndOffset();
        assertScope(stmt, expected, true, false, true, true);
    }

    @Test
    public void testElseClauseWithBlock() {
        // if (a) {} else { TARGET(); }  -> JSElseClause 插入到 { 之后
        FakePsiElement elseClause = fake("JSElseClause", "else { TARGET(); }")
                .child(fake("PsiElement(JS:ELSE_KEYWORD)", "else"))
                .child(fake("JSBlockStatement", "{ TARGET(); }")
                        .child(fake("PsiElement(JS:LBRACE)", "{"))
                        .child(fake("JSExpressionStatement", "TARGET();"))
                        .child(fake("PsiElement(JS:RBRACE)", "}")));
        int expected = elseClause.getLastChild().getFirstChild().getTextRange().getEndOffset();
        assertScope(elseClause, expected, true, false, true, true);
    }

    @Test
    public void testElseIfClause() {
        // else if (TARGET) {}  -> JSElseClause 的子节点是 JSIfStatement 而非块，退化为默认
        FakePsiElement elseClause = fake("JSElseClause", "else if (TARGET) { TARGET(); }")
                .child(fake("PsiElement(JS:ELSE_KEYWORD)", "else"))
                .child(fake("JSIfStatement", "if (TARGET) { TARGET(); }")
                        .child(fake("PsiElement(JS:IF_KEYWORD)", "if"))
                        .child(fake("PsiElement(JS:LPAR)", "("))
                        .child(fake("JSReferenceExpression", "TARGET"))
                        .child(fake("PsiElement(JS:RPAR)", ")"))
                        .child(fake("JSBlockStatement", "{ TARGET(); }")
                                .child(fake("PsiElement(JS:LBRACE)", "{"))
                                .child(fake("JSExpressionStatement", "TARGET();"))
                                .child(fake("PsiElement(JS:RBRACE)", "}"))));
        assertScope(elseClause, elseClause.getTextRange().getEndOffset(), false, true, true, false);
    }

    @Test
    public void testSwitchStatement() {
        // switch (TARGET) { ... }  -> 插入到 { 之后
        FakePsiElement stmt = withBlock("JSSwitchStatement", "switch (TARGET) { TARGET(); }",
                fake("PsiElement(JS:SWITCH_KEYWORD)", "switch"),
                fake("PsiElement(JS:LPAR)", "("),
                fake("JSReferenceExpression", "TARGET"),
                fake("PsiElement(JS:RPAR)", ")"));
        int expected = stmt.getLastChild().getFirstChild().getTextRange().getEndOffset();
        assertScope(stmt, expected, true, false, true, true);
    }

    @Test
    public void testTryStatement() {
        // try { TARGET(); } catch (e) {}  -> 插入到 try 块的 { 之后
        FakePsiElement tryBlock = fake("JSBlockStatement", "{ TARGET(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "TARGET();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement catchBlock = fake("JSCatchBlock", "catch (e) { }")
                .child(fake("PsiElement(JS:CATCH_KEYWORD)", "catch"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("JSIdentifier", "e"))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(fake("JSBlockStatement", "{ }")
                        .child(fake("PsiElement(JS:LBRACE)", "{"))
                        .child(fake("PsiElement(JS:RBRACE)", "}")));
        FakePsiElement stmt = fake("JSTryStatement", "try { TARGET(); } catch (e) { }")
                .child(fake("PsiElement(JS:TRY_KEYWORD)", "try"))
                .child(tryBlock)
                .child(catchBlock);
        int expected = tryBlock.getFirstChild().getTextRange().getEndOffset();
        assertScope(stmt, expected, true, false, true, true);
    }

    @Test
    public void testWhileStatement() {
        // while (TARGET) { ... }
        FakePsiElement stmt = withBlock("JSWhileStatement", "while (TARGET) { TARGET(); }",
                fake("PsiElement(JS:WHILE_KEYWORD)", "while"),
                fake("PsiElement(JS:LPAR)", "("),
                fake("JSReferenceExpression", "TARGET"),
                fake("PsiElement(JS:RPAR)", ")"));
        int expected = stmt.getLastChild().getFirstChild().getTextRange().getEndOffset();
        assertScope(stmt, expected, true, false, true, true);
    }

    @Test
    public void testForStatementBlock() {
        // for (let i = 0; i < TARGET; i++) { ... }  -> 插入到 { 之后
        FakePsiElement block = fake("JSBlockStatement", "{ TARGET(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "TARGET();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement stmt = fake("JSForStatement", "for (let i = 0; i < TARGET; i++) { TARGET(); }")
                .child(fake("PsiElement(JS:FOR_KEYWORD)", "for"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("JSVarStatement", "let i = 0"))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"))
                .child(fake("JSBinaryExpression", "i < TARGET"))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"))
                .child(fake("JSUpdateExpression", "i++"))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(block);
        int expected = block.getFirstChild().getTextRange().getEndOffset();
        assertScope(stmt, expected, true, false, true, true);
    }

    @Test
    public void testForInStatementBlock() {
        // for (const v of TARGET) { ... }
        FakePsiElement block = fake("JSBlockStatement", "{ TARGET(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "TARGET();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement stmt = fake("JSForInStatement", "for (const v of TARGET) { TARGET(); }")
                .child(fake("PsiElement(JS:FOR_KEYWORD)", "for"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("JSVarStatement", "const v"))
                .child(fake("PsiElement(JS:OF_KEYWORD)", "of"))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(block);
        int expected = block.getFirstChild().getTextRange().getEndOffset();
        assertScope(stmt, expected, true, false, true, true);
    }

    @Test
    public void testFunctionPropertyObjectMethod() {
        // const o = { foo() { TARGET(); } };  对象方法 -> 插入到方法体 { 之后
        FakePsiElement body = fake("JSBlockStatement", "{ TARGET(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "TARGET();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement method = fake("JSFunctionProperty", "foo() { TARGET(); }")
                .child(fake("JSReferenceExpression", "foo"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(body);
        int expected = body.getFirstChild().getTextRange().getEndOffset();
        assertScope(method, expected, true, false, true, true);
    }

    @Test
    public void testFunctionPropertyClassMethod() {
        // class A { foo() { TARGET(); } }  类方法同样为 JSFunctionProperty
        FakePsiElement body = fake("JSBlockStatement", "{ TARGET(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "TARGET();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement method = fake("JSFunctionProperty", "foo() { TARGET(); }")
                .child(fake("JSReferenceExpression", "foo"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(body);
        FakePsiElement cls = fake("JSClass", "class A { foo() { TARGET(); } }")
                .child(fake("PsiElement(JS:CLASS_KEYWORD)", "class"))
                .child(fake("JSIdentifier", "A"))
                .child(fake("JSBlockStatement", "{ foo() { TARGET(); } }")
                        .child(fake("PsiElement(JS:LBRACE)", "{"))
                        .child(method)
                        .child(fake("PsiElement(JS:RBRACE)", "}")));
        int expected = body.getFirstChild().getTextRange().getEndOffset();
        assertScope(method, expected, true, false, true, true);
    }

    @Test
    public void testFunctionDeclaration() {
        // function foo() { TARGET(); }
        FakePsiElement body = fake("JSBlockStatement", "{ TARGET(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "TARGET();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement fn = fake("JSFunction", "foo", "function foo() { TARGET(); }")
                .child(fake("PsiElement(JS:FUNCTION_KEYWORD)", "function"))
                .child(fake("JSIdentifier", "foo"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(body);
        int expected = body.getFirstChild().getTextRange().getEndOffset();
        // 带名字的 JSFunction 也能正确分发
        assertScope(fn, expected, true, false, true, true);
    }

    @Test
    public void testFunctionExpression() {
        // const f = function() { TARGET(); };
        FakePsiElement body = fake("JSBlockStatement", "{ TARGET(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "TARGET();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement fn = fake("JSFunctionExpression", "function() { TARGET(); }")
                .child(fake("PsiElement(JS:FUNCTION_KEYWORD)", "function"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(body);
        int expected = body.getFirstChild().getTextRange().getEndOffset();
        assertScope(fn, expected, true, false, true, true);
    }

    @Test
    public void testNamedFunctionExpression() {
        // const f = function named() { TARGET(); };
        FakePsiElement body = fake("JSBlockStatement", "{ TARGET(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "TARGET();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement fn = fake("JSFunctionExpression", "function named() { TARGET(); }")
                .child(fake("PsiElement(JS:FUNCTION_KEYWORD)", "function"))
                .child(fake("JSIdentifier", "named"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(body);
        int expected = body.getFirstChild().getTextRange().getEndOffset();
        assertScope(fn, expected, true, false, true, true);
    }

    @Test
    public void testClassDeclaration() {
        // class Foo { bar = TARGET; }  -> 插入到类体 { 之后
        FakePsiElement classBody = fake("JSBlockStatement", "{ bar = TARGET; }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSProperty", "bar = TARGET;")
                        .child(fake("JSReferenceExpression", "bar"))
                        .child(fake("PsiElement(JS:EQ)", "="))
                        .child(fake("JSReferenceExpression", "TARGET"))
                        .child(fake("PsiElement(JS:SEMICOLON)", ";")))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement cls = fake("JSClass", "class Foo { bar = TARGET; }")
                .child(fake("PsiElement(JS:CLASS_KEYWORD)", "class"))
                .child(fake("JSIdentifier", "Foo"))
                .child(classBody);
        int expected = classBody.getFirstChild().getTextRange().getEndOffset();
        assertScope(cls, expected, true, false, true, true);
    }

    @Test
    public void testCatchBlock() {
        // try {} catch (e) { TARGET(); }  -> 插入到 catch 块 { 之后
        FakePsiElement catchBody = fake("JSBlockStatement", "{ TARGET(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "TARGET();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement catchBlock = fake("JSCatchBlock", "catch (e) { TARGET(); }")
                .child(fake("PsiElement(JS:CATCH_KEYWORD)", "catch"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("JSIdentifier", "e"))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(catchBody);
        int expected = catchBody.getFirstChild().getTextRange().getEndOffset();
        assertScope(catchBlock, expected, true, false, true, true);
    }

    // =====================================================================
    // 四、TypeScript 函数（TypeScriptFunction / TypeScriptFunctionExpression /
    //     TypeScriptFunctionProperty）
    // =====================================================================

    @Test
    public void testTypeScriptFunctionDeclaration() {
        // function foo(): void { TARGET(); }  （.ts 文件）
        FakePsiElement body = fake("JSBlockStatement", "{ TARGET(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "TARGET();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement fn = fake("TypeScriptFunction", "function foo(): void { TARGET(); }")
                .child(fake("PsiElement(JS:FUNCTION_KEYWORD)", "function"))
                .child(fake("JSIdentifier", "foo"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(fake("PsiElement(JS:COLON)", ":"))
                .child(fake("JSType", "void"))
                .child(body);
        int expected = body.getFirstChild().getTextRange().getEndOffset();
        assertScope(fn, expected, true, false, true, true);
    }

    @Test
    public void testTypeScriptFunctionExpression() {
        // const f = function(): void { TARGET(); };
        FakePsiElement body = fake("JSBlockStatement", "{ TARGET(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "TARGET();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement fn = fake("TypeScriptFunctionExpression", "function(): void { TARGET(); }")
                .child(fake("PsiElement(JS:FUNCTION_KEYWORD)", "function"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(fake("PsiElement(JS:COLON)", ":"))
                .child(fake("JSType", "void"))
                .child(body);
        int expected = body.getFirstChild().getTextRange().getEndOffset();
        assertScope(fn, expected, true, false, true, true);
    }

    @Test
    public void testTypeScriptFunctionProperty() {
        // const o = { foo(): void { TARGET(); } };
        FakePsiElement body = fake("JSBlockStatement", "{ TARGET(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "TARGET();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement method = fake("TypeScriptFunctionProperty", "foo(): void { TARGET(); }")
                .child(fake("JSReferenceExpression", "foo"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(fake("PsiElement(JS:COLON)", ":"))
                .child(fake("JSType", "void"))
                .child(body);
        int expected = body.getFirstChild().getTextRange().getEndOffset();
        assertScope(method, expected, true, false, true, true);
    }

    // =====================================================================
    // 五、case 子句 JSCaseClause（getAfterColon）
    // =====================================================================

    @Test
    public void testCaseClause() {
        // case 1: TARGET(); break;  -> 插入到冒号之后
        FakePsiElement colon = fake("PsiElement(JS:COLON)", ":");
        FakePsiElement caseClause = fake("JSCaseClause", "case 1: TARGET();")
                .child(fake("PsiElement(JS:CASE_KEYWORD)", "case"))
                .child(fake("JSLiteralExpression", "1"))
                .child(colon)
                .child(fake("JSExpressionStatement", "TARGET();"));
        int expected = colon.getTextRange().getEndOffset();
        assertScope(caseClause, expected, true, false, true, false);
    }

    @Test
    public void testCaseClauseWithExpression() {
        // case TARGET: foo();  -> case 表达式中包含变量
        FakePsiElement colon = fake("PsiElement(JS:COLON)", ":");
        FakePsiElement caseClause = fake("JSCaseClause", "case TARGET: foo();")
                .child(fake("PsiElement(JS:CASE_KEYWORD)", "case"))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(colon)
                .child(fake("JSExpressionStatement", "foo();"));
        int expected = colon.getTextRange().getEndOffset();
        assertScope(caseClause, expected, true, false, true, false);
    }

    @Test
    public void testDefaultCaseClause() {
        // default: TARGET();  -> default 分支同样是 JSCaseClause
        FakePsiElement colon = fake("PsiElement(JS:COLON)", ":");
        FakePsiElement caseClause = fake("JSCaseClause", "default: TARGET();")
                .child(fake("PsiElement(JS:DEFAULT_KEYWORD)", "default"))
                .child(colon)
                .child(fake("JSExpressionStatement", "TARGET();"));
        int expected = colon.getTextRange().getEndOffset();
        assertScope(caseClause, expected, true, false, true, false);
    }

    // =====================================================================
    // 六、do-while 语句 JSDoWhileStatement（getMiddleBlockStatementBeforeEnd）
    // =====================================================================

    @Test
    public void testDoWhileStatement() {
        // do { TARGET(); } while (x);  -> 插入到 } 之前（块体末尾）
        FakePsiElement rBrace = fake("PsiElement(JS:RBRACE)", "}");
        FakePsiElement block = fake("JSBlockStatement", "{ TARGET(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "TARGET();"))
                .child(rBrace);
        FakePsiElement doWhile = fake("JSDoWhileStatement", "do { TARGET(); } while (x);")
                .child(fake("PsiElement(JS:DO_KEYWORD)", "do"))
                .child(block)
                .child(fake("PsiElement(JS:WHILE_KEYWORD)", "while"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("JSReferenceExpression", "x"))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        // 插入到右花括号起始位置之前
        assertScope(doWhile, rBrace.getTextRange().getStartOffset(), true, false, false, true);
    }

    // =====================================================================
    // 七、调用表达式 JSCallExpression（getJSCallExpression）
    // =====================================================================

    @Test
    public void testCallExpressionAsStatement() {
        // TARGET();  父级是 JSExpressionStatement，无块体 -> 默认在语句末尾
        FakePsiElement call = fake("JSCallExpression", "TARGET()")
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"));
        FakePsiElement stmt = fake("JSExpressionStatement", "TARGET();")
                .child(call)
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        assertScope(call, stmt.getTextRange().getEndOffset(), false, true, true, false);
    }

    @Test
    public void testCallExpressionInsideIf() {
        // if (foo()) { TARGET(); }  调用表达式的父级是 JSIfStatement -> 插入到块 { 之后
        FakePsiElement block = fake("JSBlockStatement", "{ TARGET(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "TARGET();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement call = fake("JSCallExpression", "foo()")
                .child(fake("JSReferenceExpression", "foo"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"));
        FakePsiElement ifStmt = fake("JSIfStatement", "if (foo()) { TARGET(); }")
                .child(fake("PsiElement(JS:IF_KEYWORD)", "if"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(call)
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(block);
        int expected = block.getFirstChild().getTextRange().getEndOffset();
        assertScope(call, expected, true, false, true, true);
    }

    @Test
    public void testCallExpressionWithThenArrowCallback() {
        // foo(TARGET).then(() => { body(); })
        // 光标位于 foo(TARGET)（then 的限定调用表达式）-> 插入到箭头函数回调块 { 之后
        FakePsiElement arrowBody = fake("JSBlockStatement", "{ body(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "body();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement arrow = fake("JSArrowFunction", "() => { body(); }")
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(fake("PsiElement(JS:FAT_ARROW)", "=>"))
                .child(arrowBody);
        FakePsiElement argumentList = fake("JSArgumentList", "(() => { body(); })")
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(arrow)
                .child(fake("PsiElement(JS:RPAR)", ")"));
        FakePsiElement innerCall = fake("JSCallExpression", "foo(TARGET)")
                .child(fake("JSReferenceExpression", "foo"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:RPAR)", ")"));
        FakePsiElement reference = fake("JSReferenceExpression", "then", "foo(TARGET).then")
                .child(innerCall)
                .child(fake("JSIdentifier", "then"));
        FakePsiElement outerCall = fake("JSCallExpression", "foo(TARGET).then(() => { body(); })")
                .child(reference)
                .child(argumentList);
        int expected = arrowBody.getFirstChild().getTextRange().getEndOffset();
        assertScope(innerCall, expected, true, false, true, true);
    }

    @Test
    public void testCallExpressionWithThenFunctionCallback() {
        // foo(TARGET).then(function() { body(); })
        // 回调是 JSFunctionExpression（instanceof 匹配）-> 插入到函数体 { 之后
        FakePsiElement fnBody = fake("JSBlockStatement", "{ body(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "body();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement fnExpr = fake("JSFunctionExpression", "function() { body(); }")
                .child(fake("PsiElement(JS:FUNCTION_KEYWORD)", "function"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(fnBody);
        // 用代理包装，使 instanceof JSFunctionExpression 成立
        JSFunctionExpression fnProxy = jsFunctionExpression(fnExpr);
        FakePsiElement argumentList = fake("JSArgumentList", "(function() { body(); })")
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child((PsiElement) fnProxy)
                .child(fake("PsiElement(JS:RPAR)", ")"));
        FakePsiElement innerCall = fake("JSCallExpression", "foo(TARGET)")
                .child(fake("JSReferenceExpression", "foo"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:RPAR)", ")"));
        FakePsiElement reference = fake("JSReferenceExpression", "then", "foo(TARGET).then")
                .child(innerCall)
                .child(fake("JSIdentifier", "then"));
        FakePsiElement outerCall = fake("JSCallExpression", "foo(TARGET).then(function() { body(); })")
                .child(reference)
                .child(argumentList);
        int expected = fnBody.getFirstChild().getTextRange().getEndOffset();
        assertScope(innerCall, expected, true, false, true, true);
    }

    @Test
    public void testCallExpressionWithThenNoCallback() {
        // foo(TARGET).then(callback)  （无函数/箭头回调）-> 退化默认
        FakePsiElement innerCall = fake("JSCallExpression", "foo(TARGET)")
                .child(fake("JSReferenceExpression", "foo"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:RPAR)", ")"));
        FakePsiElement reference = fake("JSReferenceExpression", "then", "foo(TARGET).then")
                .child(innerCall)
                .child(fake("JSIdentifier", "then"));
        FakePsiElement argumentList = fake("JSArgumentList", "(callback)")
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("JSReferenceExpression", "callback"))
                .child(fake("PsiElement(JS:RPAR)", ")"));
        FakePsiElement outerCall = fake("JSCallExpression", "foo(TARGET).then(callback)")
                .child(reference)
                .child(argumentList);
        FakePsiElement stmt = fake("JSExpressionStatement", "foo(TARGET).then(callback);")
                .child(outerCall)
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        assertScope(innerCall, innerCall.getTextRange().getEndOffset(), false, true, true, false);
    }

    // =====================================================================
    // 八、表达式语句 JSExpressionStatement（getJSExpressionStatement）
    // =====================================================================

    @Test
    public void testExpressionStatementWithCall() {
        // ElMessage({ message: "success" });
        FakePsiElement stmt = fake("JSExpressionStatement", "ElMessage({ message: \"success\" });")
                .child(fake("JSCallExpression", "ElMessage({ message: \"success\" })")
                        .child(fake("JSReferenceExpression", "ElMessage"))
                        .child(fake("PsiElement(JS:LPAR)", "("))
                        .child(fake("JSObjectLiteralExpression", "{ message: \"success\" }"))
                        .child(fake("PsiElement(JS:RPAR)", ")")))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        assertScope(stmt, stmt.getTextRange().getEndOffset(), false, false, true, false);
    }

    @Test
    public void testExpressionStatementWithoutSemicolon() {
        // foo() （ASI）
        FakePsiElement stmt = fake("JSExpressionStatement", "foo()")
                .child(fake("JSCallExpression", "foo()"));
        assertScope(stmt, stmt.getTextRange().getEndOffset(), false, false, true, false);
    }

    // =====================================================================
    // 九、箭头函数 JSArrowFunction（getArrowFunction）
    // =====================================================================

    @Test
    public void testArrowFunctionWithBlockBody() {
        // const f = () => { TARGET(); };
        FakePsiElement body = fake("JSBlockStatement", "{ TARGET(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "TARGET();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement arrow = fake("JSArrowFunction", "() => { TARGET(); }")
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(fake("PsiElement(JS:FAT_ARROW)", "=>"))
                .child(body);
        int expected = body.getFirstChild().getTextRange().getEndOffset();
        assertScope(arrow, expected, true, false, true, true);
    }

    @Test
    public void testArrowFunctionWithExpressionBody() {
        // const f = () => TARGET;  表达式体无法插入块级日志 -> 退化默认
        FakePsiElement arrow = fake("JSArrowFunction", "() => TARGET")
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(fake("PsiElement(JS:FAT_ARROW)", "=>"))
                .child(fake("JSReferenceExpression", "TARGET"));
        assertScope(arrow, arrow.getTextRange().getEndOffset(), false, true, true, false);
    }

    @Test
    public void testArrowFunctionAsyncTypeScript() {
        // const f = async (): Promise<void> => { TARGET(); };  （TS 异步箭头）
        FakePsiElement body = fake("JSBlockStatement", "{ TARGET(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "TARGET();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement arrow = fake("JSArrowFunction", "async (): Promise<void> => { TARGET(); }")
                .child(fake("PsiElement(JS:ASYNC_KEYWORD)", "async"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(fake("PsiElement(JS:COLON)", ":"))
                .child(fake("JSType", "Promise<void>"))
                .child(fake("PsiElement(JS:FAT_ARROW)", "=>"))
                .child(body);
        int expected = body.getFirstChild().getTextRange().getEndOffset();
        assertScope(arrow, expected, true, false, true, true);
    }

    // =====================================================================
    // 十、JSX 元素（getJSXElement）
    // =====================================================================

    private static FakePsiElement jsxElement() {
        return fake("JSXElement", "<div>TARGET</div>")
                .child(fake("JSXOpeningElement", "<div>"))
                .child(fake("JSXText", "TARGET"))
                .child(fake("JSXClosingElement", "</div>"));
    }

    @Test
    public void testJsxInsideFunctionReturn() {
        // function App() { return <div>TARGET</div>; }
        FakePsiElement jsx = jsxElement();
        FakePsiElement fnBody = fake("JSBlockStatement", "{ return <div>TARGET</div>; }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSReturnStatement", "return <div>TARGET</div>;")
                        .child(fake("PsiElement(JS:RETURN_KEYWORD)", "return"))
                        .child(jsx)
                        .child(fake("PsiElement(JS:SEMICOLON)", ";")))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement fn = fake("JSFunction", "App", "function App() { return <div>TARGET</div>; }")
                .child(fake("PsiElement(JS:FUNCTION_KEYWORD)", "function"))
                .child(fake("JSIdentifier", "App"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(fnBody);
        int expected = fnBody.getFirstChild().getTextRange().getEndOffset();
        assertScope(jsx, expected, true, false, true, true);
    }

    @Test
    public void testJsxInsideArrowFunction() {
        // const App = () => <div>TARGET</div>;
        FakePsiElement jsx = jsxElement();
        FakePsiElement arrowBody = fake("JSBlockStatement", "{ return <div>TARGET</div>; }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSReturnStatement", "return <div>TARGET</div>;")
                        .child(fake("PsiElement(JS:RETURN_KEYWORD)", "return"))
                        .child(jsx)
                        .child(fake("PsiElement(JS:SEMICOLON)", ";")))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement arrow = fake("JSArrowFunction", "() => { return <div>TARGET</div>; }")
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(fake("PsiElement(JS:FAT_ARROW)", "=>"))
                .child(arrowBody);
        int expected = arrowBody.getFirstChild().getTextRange().getEndOffset();
        assertScope(jsx, expected, true, false, true, true);
    }

    @Test
    public void testJsxInsideClassRenderMethod() {
        // class App { render() { return <div>TARGET</div>; } }
        FakePsiElement jsx = jsxElement();
        FakePsiElement methodBody = fake("JSBlockStatement", "{ return <div>TARGET</div>; }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSReturnStatement", "return <div>TARGET</div>;")
                        .child(fake("PsiElement(JS:RETURN_KEYWORD)", "return"))
                        .child(jsx)
                        .child(fake("PsiElement(JS:SEMICOLON)", ";")))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement method = fake("JSFunctionProperty", "render() { return <div>TARGET</div>; }")
                .child(fake("JSReferenceExpression", "render"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(methodBody);
        int expected = methodBody.getFirstChild().getTextRange().getEndOffset();
        assertScope(jsx, expected, true, false, true, true);
    }

    @Test
    public void testJsxAtModuleScope() {
        // const el = <div>TARGET</div>;  顶层 JSX 找不到函数/类作用域 -> 退化默认
        FakePsiElement jsx = jsxElement();
        FakePsiElement varStmt = fake("JSVarStatement", "const el = <div>TARGET</div>;")
                .child(fake("PsiElement(JS:VAR_KEYWORD)", "const"))
                .child(fake("JSReferenceExpression", "el"))
                .child(fake("PsiElement(JS:EQ)", "="))
                .child(jsx)
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        assertScope(jsx, jsx.getTextRange().getEndOffset(), false, true, true, false);
    }

    // =====================================================================
    // 十一、return 语句 JSReturnStatement（getReturnStatement）
    // =====================================================================

    @Test
    public void testReturnStatementWithValue() {
        // return TARGET;  -> 插入到 return 之前
        FakePsiElement ret = fake("JSReturnStatement", "return TARGET;")
                .child(fake("PsiElement(JS:RETURN_KEYWORD)", "return"))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        assertScope(ret, ret.getTextRange().getStartOffset(), true, false, true, true);
    }

    @Test
    public void testReturnStatementEmpty() {
        // return;
        FakePsiElement ret = fake("JSReturnStatement", "return;")
                .child(fake("PsiElement(JS:RETURN_KEYWORD)", "return"))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        assertScope(ret, ret.getTextRange().getStartOffset(), true, false, true, true);
    }

    @Test
    public void testReturnStatementComplexExpression() {
        // return TARGET + 1;
        FakePsiElement ret = fake("JSReturnStatement", "return TARGET + 1;")
                .child(fake("PsiElement(JS:RETURN_KEYWORD)", "return"))
                .child(fake("JSBinaryExpression", "TARGET + 1"))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        assertScope(ret, ret.getTextRange().getStartOffset(), true, false, true, true);
    }

    // =====================================================================
    // 十二、对象字面量 JSObjectLiteralExpression（getObjectLiteralExpression）
    // =====================================================================

    @Test
    public void testObjectLiteralExpression() {
        // const o = { a: TARGET };  -> 插入到 { 之后
        FakePsiElement lBrace = fake("PsiElement(JS:LBRACE)", "{");
        FakePsiElement obj = fake("JSObjectLiteralExpression", "{ a: TARGET }")
                .child(lBrace)
                .child(fake("JSProperty", "a: TARGET")
                        .child(fake("JSReferenceExpression", "a"))
                        .child(fake("PsiElement(JS:COLON)", ":"))
                        .child(fake("JSReferenceExpression", "TARGET")))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        assertScope(obj, lBrace.getTextRange().getEndOffset(), true, false, true, true);
    }

    @Test
    public void testObjectLiteralExpressionEmpty() {
        // const o = {};
        FakePsiElement lBrace = fake("PsiElement(JS:LBRACE)", "{");
        FakePsiElement obj = fake("JSObjectLiteralExpression", "{ }")
                .child(lBrace)
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        assertScope(obj, lBrace.getTextRange().getEndOffset(), true, false, true, true);
    }

    @Test
    public void testObjectLiteralExpressionNested() {
        // const o = { nested: { key: TARGET } };
        FakePsiElement innerLBrace = fake("PsiElement(JS:LBRACE)", "{");
        FakePsiElement innerObj = fake("JSObjectLiteralExpression", "{ key: TARGET }")
                .child(innerLBrace)
                .child(fake("JSProperty", "key: TARGET")
                        .child(fake("JSReferenceExpression", "key"))
                        .child(fake("PsiElement(JS:COLON)", ":"))
                        .child(fake("JSReferenceExpression", "TARGET")))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement lBrace = fake("PsiElement(JS:LBRACE)", "{");
        FakePsiElement outerObj = fake("JSObjectLiteralExpression", "{ nested: { key: TARGET } }")
                .child(lBrace)
                .child(fake("JSProperty", "nested: { key: TARGET }")
                        .child(fake("JSReferenceExpression", "nested"))
                        .child(fake("PsiElement(JS:COLON)", ":"))
                        .child(innerObj))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        assertScope(outerObj, lBrace.getTextRange().getEndOffset(), true, false, true, true);
        assertScope(innerObj, innerLBrace.getTextRange().getEndOffset(), true, false, true, true);
    }

    // =====================================================================
    // 十三、Vue/HTML 模板标签（getXmlTag）
    // =====================================================================

    private static FakePsiElement scriptTagWithBlock() {
        FakePsiElement scriptBlock = fake("JSBlockStatement", "{ TARGET(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "TARGET();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        return fake("XmlTag", "script", "<script>{ TARGET(); }</script>")
                .child(fake("XmlToken", "<script>"))
                .child(scriptBlock)
                .child(fake("XmlToken", "</script>"));
    }

    @Test
    public void testXmlTagVueScript() {
        // .vue 文件：<script> 块内含 JS 语句，插入到脚本块 { 之后
        FakePsiElement scriptTag = scriptTagWithBlock();
        FakePsiElement innerFile = fake("PsiFile", "file.vue");
        innerFile.child(scriptTag);
        // 让 scriptTag 的父节点成为真正的 PsiFile（instanceof 判断需要）
        scriptTag.parent = psiFile(innerFile);
        FakePsiElement jsBlock = (FakePsiElement) scriptTag.getChildren()[1];
        int expected = jsBlock.getFirstChild().getTextRange().getEndOffset();
        assertScope(scriptTag, expected, true, false, true, true);
    }

    @Test
    public void testXmlTagFallbackNoBlock() {
        // <script> 标签内没有 JSBlockStatement -> 退化默认
        FakePsiElement scriptTag = fake("XmlTag", "script", "<script></script>")
                .child(fake("XmlToken", "<script>"))
                .child(fake("XmlToken", "</script>"));
        FakePsiElement innerFile = fake("PsiFile", "file.vue");
        innerFile.child(scriptTag);
        scriptTag.parent = psiFile(innerFile);
        assertScope(scriptTag, scriptTag.getTextRange().getEndOffset(), false, true, true, false);
    }

    // =====================================================================
    // 十四、未知类型返回 null（default 分支）
    // =====================================================================

    @Test
    public void testUnknownElementTypeReturnsNull() {
        FakePsiElement ref = fake("JSReferenceExpression", "TARGET");
        assertNull(PsiPositionUtil.getScopeOffsetByType(ref));

        FakePsiElement id = fake("JSIdentifier", "TARGET");
        assertNull(PsiPositionUtil.getScopeOffsetByType(id));

        FakePsiElement bin = fake("JSBinaryExpression", "a + b");
        assertNull(PsiPositionUtil.getScopeOffsetByType(bin));
    }

    @Test
    public void testTypeScriptClassReturnsNull() {
        // 已知局限：.ts 文件中带类型参数的类解析为 TypeScriptClass（intellij.javascript.psi.ecma6），
        // 而 PsiPositionUtil 的 switch 只处理了 JSClass，TypeScriptClass 目前会返回 null。
        FakePsiElement tsClass = fake("TypeScriptClass", "class Foo<T> { bar = TARGET; }")
                .child(fake("PsiElement(JS:CLASS_KEYWORD)", "class"))
                .child(fake("JSIdentifier", "Foo"))
                .child(fake("JSType", "<T>"))
                .child(fake("JSBlockStatement", "{ bar = TARGET; }"));
        assertNull(PsiPositionUtil.getScopeOffsetByType(tsClass));
    }

    @Test
    public void testFindScopeOffsetInsideTypeScriptClass() {
        // 在 TypeScriptClass 内部的光标：JSClass 不匹配 -> 一直向上找到根 -> 默认插入到叶子末尾
        FakePsiElement ref = fake("JSReferenceExpression", "TARGET");
        FakePsiElement classBody = fake("JSBlockStatement", "{ bar = TARGET; }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSProperty", "bar = TARGET;")
                        .child(fake("JSReferenceExpression", "bar"))
                        .child(fake("PsiElement(JS:EQ)", "="))
                        .child(ref)
                        .child(fake("PsiElement(JS:SEMICOLON)", ";")))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement tsClass = fake("TypeScriptClass", "class Foo<T> { bar = TARGET; }")
                .child(fake("PsiElement(JS:CLASS_KEYWORD)", "class"))
                .child(fake("JSIdentifier", "Foo"))
                .child(fake("JSType", "<T>"))
                .child(classBody);
        ScopeOffset off = InsertConsoleLogAction.findScopeOffset(ref);
        assertScopeFields(off, ref, ref.getTextRange().getEndOffset(), false, true, true, false);
    }

    // =====================================================================
    // 十五、公开默认方法
    // =====================================================================

    @Test
    public void testGetDefault() {
        FakePsiElement el = fake("JSVarStatement", "const TARGET = 1;")
                .child(fake("PsiElement(JS:VAR_KEYWORD)", "const"));
        ScopeOffset off = PsiPositionUtil.getDefault(el);
        assertScopeFields(off, el, el.getTextRange().getEndOffset(), false, true, true, false);
    }

    @Test
    public void testGetAlignDefault() {
        FakePsiElement el = fake("JSVarStatement", "const TARGET = 1;")
                .child(fake("PsiElement(JS:VAR_KEYWORD)", "const"));
        ScopeOffset off = PsiPositionUtil.getAlignDefault(el);
        assertScopeFields(off, el, el.getTextRange().getEndOffset(), true, true, true, false);
    }

    @Test
    public void testGetUndefinedDefault() {
        ScopeOffset off = PsiPositionUtil.getUndefinedDefault(caret(42));
        assertNotNull(off);
        assertEquals(Integer.valueOf(42), off.getInsertEndOffset());
        assertEquals(false, off.getNeedTab());
        assertEquals(false, off.getDefault());
        assertEquals(true, off.getNeedBegLine());
        assertEquals(false, off.getNeedEndLine());
    }

    // =====================================================================
    // 十六、findScopeOffset 端到端（InsertConsoleLogAction 向上查找）
    // =====================================================================

    @Test
    public void testFindScopeOffsetFromVariable() {
        // function foo() { const TARGET = 1; }
        // 光标在 TARGET 上：JSReferenceExpression -> 向上找到 JSVarStatement
        FakePsiElement varStmt = fake("JSVarStatement", "const TARGET = 1;")
                .child(fake("PsiElement(JS:VAR_KEYWORD)", "const"))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:EQ)", "="))
                .child(fake("JSLiteralExpression", "1"))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        FakePsiElement fnBody = fake("JSBlockStatement", "{ const TARGET = 1; }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(varStmt)
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement fn = fake("JSFunction", "foo", "function foo() { const TARGET = 1; }")
                .child(fake("PsiElement(JS:FUNCTION_KEYWORD)", "function"))
                .child(fake("JSIdentifier", "foo"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(fnBody);
        FakePsiElement caretElement = (FakePsiElement) varStmt.getChildren()[1]; // JSReferenceExpression TARGET

        ScopeOffset off = InsertConsoleLogAction.findScopeOffset(caretElement);
        assertScopeFields(off, caretElement, varStmt.getTextRange().getEndOffset(), false, false, true, false);
    }

    @Test
    public void testFindScopeOffsetFromCallInFunctionBody() {
        // function foo() { TARGET(); }
        // 光标在 TARGET 上：JSReferenceExpression -> JSCallExpression -> JSExpressionStatement
        FakePsiElement call = fake("JSCallExpression", "TARGET()")
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"));
        FakePsiElement exprStmt = fake("JSExpressionStatement", "TARGET();")
                .child(call)
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        FakePsiElement fnBody = fake("JSBlockStatement", "{ TARGET(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(exprStmt)
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement fn = fake("JSFunction", "foo", "function foo() { TARGET(); }")
                .child(fake("PsiElement(JS:FUNCTION_KEYWORD)", "function"))
                .child(fake("JSIdentifier", "foo"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(fnBody);
        FakePsiElement caretElement = (FakePsiElement) call.getFirstChild(); // JSReferenceExpression TARGET

        ScopeOffset off = InsertConsoleLogAction.findScopeOffset(caretElement);
        // JSCallExpression 父级为 JSExpressionStatement -> 默认插入到语句末尾（分号后）
        assertScopeFields(off, caretElement, exprStmt.getTextRange().getEndOffset(), false, true, true, false);
    }

    @Test
    public void testFindScopeOffsetFromVariableInsideArrowBody() {
        // foo().then(() => { const TARGET = 1; })
        // 光标在 TARGET 上：向上找到 JSVarStatement（在箭头函数体内）
        FakePsiElement varStmt = fake("JSVarStatement", "const TARGET = 1;")
                .child(fake("PsiElement(JS:VAR_KEYWORD)", "const"))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:EQ)", "="))
                .child(fake("JSLiteralExpression", "1"))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        FakePsiElement arrowBody = fake("JSBlockStatement", "{ const TARGET = 1; }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(varStmt)
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement arrow = fake("JSArrowFunction", "() => { const TARGET = 1; }")
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(fake("PsiElement(JS:FAT_ARROW)", "=>"))
                .child(arrowBody);
        FakePsiElement caretElement = (FakePsiElement) varStmt.getChildren()[1];

        ScopeOffset off = InsertConsoleLogAction.findScopeOffset(caretElement);
        assertScopeFields(off, caretElement, varStmt.getTextRange().getEndOffset(), false, false, true, false);
    }

    @Test
    public void testFindScopeOffsetFromCondition() {
        // if (TARGET) { foo(); }
        // 光标在条件 TARGET 上：JSReferenceExpression -> JSIfStatement -> 块 { 后
        FakePsiElement block = fake("JSBlockStatement", "{ foo(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "foo();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement ifStmt = fake("JSIfStatement", "if (TARGET) { foo(); }")
                .child(fake("PsiElement(JS:IF_KEYWORD)", "if"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(block);
        FakePsiElement caretElement = (FakePsiElement) ifStmt.getChildren()[2];

        ScopeOffset off = InsertConsoleLogAction.findScopeOffset(caretElement);
        int expected = block.getFirstChild().getTextRange().getEndOffset();
        assertScopeFields(off, caretElement, expected, true, false, true, true);
    }

    @Test
    public void testFindScopeOffsetNoMatchFallsBackToDefault() {
        // 顶层孤立引用：没有任何匹配的祖先 -> 返回 getDefault(原始元素)
        FakePsiElement ref = fake("JSReferenceExpression", "TARGET");
        ScopeOffset off = InsertConsoleLogAction.findScopeOffset(ref);
        assertScopeFields(off, ref, ref.getTextRange().getEndOffset(), false, true, true, false);
    }

    @Test
    public void testFindScopeOffsetFromReturn() {
        // function f() { return TARGET; }
        // 光标在 TARGET 上：JSReferenceExpression -> JSReturnStatement -> 插入到 return 之前
        FakePsiElement ret = fake("JSReturnStatement", "return TARGET;")
                .child(fake("PsiElement(JS:RETURN_KEYWORD)", "return"))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        FakePsiElement fnBody = fake("JSBlockStatement", "{ return TARGET; }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(ret)
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement fn = fake("JSFunction", "f", "function f() { return TARGET; }")
                .child(fake("PsiElement(JS:FUNCTION_KEYWORD)", "function"))
                .child(fake("JSIdentifier", "f"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(fnBody);
        FakePsiElement caretElement = (FakePsiElement) ret.getChildren()[1];

        ScopeOffset off = InsertConsoleLogAction.findScopeOffset(caretElement);
        assertScopeFields(off, caretElement, ret.getTextRange().getStartOffset(), true, false, true, true);
    }

    @Test
    public void testScopeOffsetNullForUnknownThenDefault() {
        // 验证 PsiPositionUtil 对未知类型返回 null，且 findScopeOffset 会继续向上查找
        FakePsiElement innerRef = fake("JSReferenceExpression", "TARGET");
        FakePsiElement middle = fake("JSBinaryExpression", "TARGET + 1")
                .child(innerRef)
                .child(fake("PsiElement(JS:PLUS)", "+"))
                .child(fake("JSLiteralExpression", "1"));
        FakePsiElement ret = fake("JSReturnStatement", "return TARGET + 1;")
                .child(fake("PsiElement(JS:RETURN_KEYWORD)", "return"))
                .child(middle)
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        assertNull(PsiPositionUtil.getScopeOffsetByType(innerRef));
        assertNull(PsiPositionUtil.getScopeOffsetByType(middle));

        ScopeOffset off = InsertConsoleLogAction.findScopeOffset(innerRef);
        assertScopeFields(off, innerRef, ret.getTextRange().getStartOffset(), true, false, true, true);
    }

    @Test
    public void testFindScopeOffsetFromElseIfCondition() {
        // if (a) { foo(); } else if (TARGET) { }
        // 光标在 else-if 条件 TARGET 上：JSReferenceExpression -> 内层 JSIfStatement -> 块 { 后
        FakePsiElement elseIfBlock = fake("JSBlockStatement", "{ }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement elseIf = fake("JSIfStatement", "if (TARGET) { }")
                .child(fake("PsiElement(JS:IF_KEYWORD)", "if"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("JSReferenceExpression", "TARGET"))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(elseIfBlock);
        FakePsiElement elseClause = fake("JSElseClause", "else if (TARGET) { }")
                .child(fake("PsiElement(JS:ELSE_KEYWORD)", "else"))
                .child(elseIf);
        FakePsiElement ifBlock = fake("JSBlockStatement", "{ foo(); }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("JSExpressionStatement", "foo();"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement outerIf = fake("JSIfStatement", "if (a) { foo(); } else if (TARGET) { }")
                .child(fake("PsiElement(JS:IF_KEYWORD)", "if"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("JSReferenceExpression", "a"))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(ifBlock)
                .child(elseClause);
        FakePsiElement caretElement = (FakePsiElement) elseIf.getChildren()[2]; // TARGET

        ScopeOffset off = InsertConsoleLogAction.findScopeOffset(caretElement);
        int expected = elseIfBlock.getFirstChild().getTextRange().getEndOffset();
        assertScopeFields(off, caretElement, expected, true, false, true, true);
    }

    @Test
    public void testFindScopeOffsetFromChainedCallMapArrow() {
        // knowledgeList.value.push(...rows).map(row => { })
        // 光标在箭头函数参数 row 上：JSReferenceExpression -> JSArrowFunction -> 块 { 后
        FakePsiElement arrowBody = fake("JSBlockStatement", "{ }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement rowParam = fake("JSReferenceExpression", "row");
        FakePsiElement arrow = fake("JSArrowFunction", "row => { }")
                .child(rowParam)
                .child(fake("PsiElement(JS:FAT_ARROW)", "=>"))
                .child(arrowBody);
        FakePsiElement argumentList = fake("JSArgumentList", "(row => { })")
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(arrow)
                .child(fake("PsiElement(JS:RPAR)", ")"));
        FakePsiElement pushCall = fake("JSCallExpression", "knowledgeList.value.push(...rows)")
                .child(fake("JSReferenceExpression", "knowledgeList.value.push"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("JSSpreadExpression", "...rows"))
                .child(fake("PsiElement(JS:RPAR)", ")"));
        FakePsiElement mapRef = fake("JSReferenceExpression", "map", "knowledgeList.value.push(...rows).map")
                .child(pushCall)
                .child(fake("JSIdentifier", "map"));
        FakePsiElement mapCall = fake("JSCallExpression", "knowledgeList.value.push(...rows).map(row => { })")
                .child(mapRef)
                .child(argumentList);
        FakePsiElement caretElement = (FakePsiElement) arrow.getChildren()[0]; // row

        ScopeOffset off = InsertConsoleLogAction.findScopeOffset(caretElement);
        int expected = arrowBody.getFirstChild().getTextRange().getEndOffset();
        assertScopeFields(off, caretElement, expected, true, false, true, true);
    }

    @Test
    public void testFindScopeOffsetFromCallbackArrowParameters() {
        // this.chat.addEventListener("afterTagCheck", (tag, prefix) => { })
        // 光标在回调箭头函数参数 tag 上：JSReferenceExpression -> JSArrowFunction -> 块 { 后
        FakePsiElement arrowBody = fake("JSBlockStatement", "{ }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement tagParam = fake("JSReferenceExpression", "tag");
        FakePsiElement arrow = fake("JSArrowFunction", "(tag, prefix) => { }")
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(tagParam)
                .child(fake("PsiElement(JS:COMMA)", ","))
                .child(fake("JSReferenceExpression", "prefix"))
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(fake("PsiElement(JS:FAT_ARROW)", "=>"))
                .child(arrowBody);
        FakePsiElement call = fake("JSCallExpression", "this.chat.addEventListener(\"afterTagCheck\", (tag, prefix) => { })")
                .child(fake("JSReferenceExpression", "this.chat.addEventListener"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(fake("JSLiteralExpression", "\"afterTagCheck\""))
                .child(fake("PsiElement(JS:COMMA)", ","))
                .child(arrow)
                .child(fake("PsiElement(JS:RPAR)", ")"));
        FakePsiElement caretElement = (FakePsiElement) arrow.getChildren()[1]; // tag

        ScopeOffset off = InsertConsoleLogAction.findScopeOffset(caretElement);
        int expected = arrowBody.getFirstChild().getTextRange().getEndOffset();
        assertScopeFields(off, caretElement, expected, true, false, true, true);
    }

    @Test
    public void testFindScopeOffsetFromAsyncArrowParameter() {
        // const getDirFileListByDirId = async (selectNode) => { }
        // 光标在异步箭头函数参数 selectNode 上：JSReferenceExpression -> JSArrowFunction -> 块 { 后
        FakePsiElement arrowBody = fake("JSBlockStatement", "{ }")
                .child(fake("PsiElement(JS:LBRACE)", "{"))
                .child(fake("PsiElement(JS:RBRACE)", "}"));
        FakePsiElement selectNodeParam = fake("JSReferenceExpression", "selectNode");
        FakePsiElement arrow = fake("JSArrowFunction", "async (selectNode) => { }")
                .child(fake("PsiElement(JS:ASYNC_KEYWORD)", "async"))
                .child(fake("PsiElement(JS:LPAR)", "("))
                .child(selectNodeParam)
                .child(fake("PsiElement(JS:RPAR)", ")"))
                .child(fake("PsiElement(JS:FAT_ARROW)", "=>"))
                .child(arrowBody);
        FakePsiElement varStmt = fake("JSVarStatement", "const getDirFileListByDirId = async (selectNode) => { };")
                .child(fake("PsiElement(JS:VAR_KEYWORD)", "const"))
                .child(fake("JSReferenceExpression", "getDirFileListByDirId"))
                .child(fake("PsiElement(JS:EQ)", "="))
                .child(arrow)
                .child(fake("PsiElement(JS:SEMICOLON)", ";"));
        FakePsiElement caretElement = (FakePsiElement) arrow.getChildren()[2]; // selectNode

        ScopeOffset off = InsertConsoleLogAction.findScopeOffset(caretElement);
        int expected = arrowBody.getFirstChild().getTextRange().getEndOffset();
        assertScopeFields(off, caretElement, expected, true, false, true, true);
    }
}
