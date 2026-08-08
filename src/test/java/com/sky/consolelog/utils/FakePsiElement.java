package com.sky.consolelog.utils;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试用假 PsiElement。
 *
 * <p>用于在纯 JUnit 单元测试中模拟 IntelliJ JavaScript/TypeScript 插件的真实 PSI 树结构，
 * 从而验证 {@link PsiPositionUtil} 的插入位置计算逻辑，无需加载完整的 IDE 运行时。
 *
 * <p>树结构（typeName/text/children）严格参照 WebStorm 的 JavaScript PSI 模型建模：
 * <ul>
 *   <li>JSVarStatement: [VAR_KEYWORD, JSReferenceExpression, EQ, initializer, SEMICOLON]</li>
 *   <li>JSIfStatement / JSSwitchStatement / JSTryStatement / JSWhileStatement / JSForStatement /
 *       JSForInStatement / JSDoWhileStatement / JSFunction* / JSClass / JSCatchBlock:
 *       内部包含一个 JSBlockStatement 子节点（[LBRACE, ..., RBRACE]）</li>
 *   <li>JSCaseClause: [CASE_KEYWORD, caseExpression, COLON, statements...]</li>
 *   <li>JSExpressionStatement: [expression, SEMICOLON]</li>
 *   <li>JSArrowFunction: [LPAR, RPAR, FAT_ARROW, JSBlockStatement 或表达式体]</li>
 *   <li>JSObjectLiteralExpression: [LBRACE, properties..., RBRACE]</li>
 *   <li>JSReturnStatement: [RETURN_KEYWORD, expression, SEMICOLON]</li>
 * </ul>
 *
 * <p>子节点按顺序以单个空格拼接自动计算文本范围（text range），父节点范围为第一个子节点
 * 起点到最后一个子节点终点。
 */
public class FakePsiElement implements PsiElement {

    /** 标记动态代理包装的真实 FakePsiElement（用于 layout 时定位偏移量）。 */
    public interface FakePsiWrapper {
        FakePsiElement inner();
    }

    protected final String typeName;
    protected final String name; // 可选，追加到 toString() 形成 "Type:name"
    protected String text;
    protected int startOffset;
    protected int endOffset;
    protected PsiElement parent;
    protected final List<PsiElement> children = new ArrayList<>();

    public FakePsiElement(String typeName, String name, String text) {
        this.typeName = typeName;
        this.name = name;
        this.text = text;
        this.startOffset = 0;
        this.endOffset = text == null ? 0 : text.length();
    }

    public FakePsiElement(String typeName, String text) {
        this(typeName, null, text);
    }

    /** 设置该节点起始偏移量并自动计算整棵子树的文本范围。 */
    public FakePsiElement at(int startOffset) {
        this.startOffset = startOffset;
        layout();
        return this;
    }

    /** 追加子节点（父节点的 text/范围会根据子节点自动重新布局）。 */
    public FakePsiElement child(PsiElement child) {
        children.add(child);
        if (child instanceof FakePsiElement) {
            ((FakePsiElement) child).parent = this;
        }
        layout();
        return this;
    }

    private void layout() {
        if (children.isEmpty()) {
            endOffset = startOffset + (text == null ? 0 : text.length());
            return;
        }
        StringBuilder sb = new StringBuilder();
        int cur = startOffset;
        for (PsiElement child : children) {
            FakePsiElement f;
            if (child instanceof FakePsiElement) {
                f = (FakePsiElement) child;
            } else if (child instanceof FakePsiWrapper) {
                // 动态代理包装的真实节点（如 JSFunctionExpression 代理）
                f = ((FakePsiWrapper) child).inner();
            } else {
                sb.append(child.getText());
                cur += child.getText().length() + 1;
                continue;
            }
            f.startOffset = cur;
            f.endOffset = cur + (f.text == null ? 0 : f.text.length());
            f.layout();
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(f.text);
            cur = f.endOffset + 1; // 子节点之间以单个空格分隔
        }
        this.text = sb.toString();
        this.endOffset = cur - 1;
    }

    // ------------------------------------------------------------------
    // PsiElement 接口：与 PsiPositionUtil 相关的方法精确实现
    // ------------------------------------------------------------------

    @Override
    public String toString() {
        return name == null ? typeName : typeName + ":" + name;
    }

    @Override
    public PsiElement getParent() {
        return parent;
    }

    @Override
    public PsiElement[] getChildren() {
        return children.toArray(new PsiElement[0]);
    }

    @Override
    public PsiElement getFirstChild() {
        return children.isEmpty() ? null : children.get(0);
    }

    @Override
    public PsiElement getLastChild() {
        return children.isEmpty() ? null : children.get(children.size() - 1);
    }

    @Override
    public TextRange getTextRange() {
        return new TextRange(startOffset, endOffset);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public int getTextOffset() {
        return startOffset;
    }

    @Override
    public int getTextLength() {
        return endOffset - startOffset;
    }

    // ------------------------------------------------------------------
    // PsiElement 接口：其余方法提供空实现（测试中不会使用）
    // ------------------------------------------------------------------

    @Override public PsiElement getNextSibling() { return null; }
    @Override public PsiElement getPrevSibling() { return null; }
    @Override public PsiFile getContainingFile() { return null; }
    @Override public int getStartOffsetInParent() { return 0; }
    @Override public PsiElement findElementAt(int offset) { return null; }
    @Override public PsiReference findReferenceAt(int offset) { return null; }
    @Override public char[] textToCharArray() { return text.toCharArray(); }
    @Override public PsiElement getNavigationElement() { return this; }
    @Override public PsiElement getOriginalElement() { return this; }
    @Override public boolean textMatches(CharSequence text) { return false; }
    @Override public boolean textMatches(PsiElement element) { return false; }
    @Override public boolean textContains(char c) { return text != null && text.indexOf(c) >= 0; }
    @Override public void accept(PsiElementVisitor visitor) { }
    @Override public void acceptChildren(PsiElementVisitor visitor) { }
    @Override public PsiElement copy() { return this; }
    @Override public PsiElement add(PsiElement element) throws IncorrectOperationException { return element; }
    @Override public PsiElement addBefore(PsiElement element, PsiElement anchor) throws IncorrectOperationException { return element; }
    @Override public PsiElement addAfter(PsiElement element, PsiElement anchor) throws IncorrectOperationException { return element; }
    @Override public void checkAdd(PsiElement element) throws IncorrectOperationException { }
    @Override public PsiElement addRange(PsiElement first, PsiElement last) throws IncorrectOperationException { return first; }
    @Override public PsiElement addRangeBefore(PsiElement first, PsiElement last, PsiElement anchor) throws IncorrectOperationException { return first; }
    @Override public PsiElement addRangeAfter(PsiElement first, PsiElement last, PsiElement anchor) throws IncorrectOperationException { return first; }
    @Override public void delete() throws IncorrectOperationException { }
    @Override public void checkDelete() throws IncorrectOperationException { }
    @Override public void deleteChildRange(PsiElement first, PsiElement last) throws IncorrectOperationException { }
    @Override public PsiElement replace(PsiElement newElement) throws IncorrectOperationException { return newElement; }
    @Override public boolean isValid() { return true; }
    @Override public boolean isWritable() { return true; }
    @Override public PsiReference getReference() { return null; }
    @Override public PsiReference[] getReferences() { return PsiReference.EMPTY_ARRAY; }
    @Override public <T> T getCopyableUserData(Key<T> key) { return null; }
    @Override public <T> void putCopyableUserData(Key<T> key, T value) { }
    @Override public <T> T getUserData(Key<T> key) { return null; }
    @Override public <T> void putUserData(Key<T> key, T value) { }
    @Override public boolean processDeclarations(PsiScopeProcessor processor, ResolveState state, PsiElement lastParent, PsiElement place) { return true; }
    @Override public PsiElement getContext() { return getParent(); }
    @Override public boolean isPhysical() { return false; }
    @Override public GlobalSearchScope getResolveScope() { return null; }
    @Override public SearchScope getUseScope() { return null; }
    @Override public ASTNode getNode() { return null; }
    @Override public boolean isEquivalentTo(PsiElement another) { return this == another; }
    @Override public javax.swing.Icon getIcon(int flags) { return null; }
    @Override public Project getProject() { return null; }
    @Override public Language getLanguage() { return null; }
    @Override public PsiManager getManager() { return null; }
}
