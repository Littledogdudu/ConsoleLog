package com.sky.consolelog.utils;

import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpressionStatement;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlToken;
import com.sky.consolelog.constant.SettingConstant;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * ConsoleLog PSI处理工具
 *
 * @author SkySource
 * @Date: 2025/2/2 9:30
 */
public class ConsoleLogPsiUtil {

    /**
     * 检测当前文件所有文本的console.log语句位置
     *
     * @param psiFile 文件PSI对象
     * @return console.log调用表达式文本位置集合，返回的集合包括用户自己编写的console.log也包括符合插件编写规范的console.log
     */
    public static List<TextRange> detectAll(PsiFile psiFile, Document document) {
        List<TextRange> consoleLogRangeList = new ArrayList<>();
        psiFile.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);

                if (element instanceof JSCallExpression callExpression) {
                    if (isConsoleLog(callExpression)) {
                        TextRange textRange = getTextRangeWithSemicolonAndPrevWhitespace(callExpression, document);
                        consoleLogRangeList.add(textRange);
                    }
                } else if (element instanceof PsiComment comment) {
                    if (isConsoleLog(comment)) {
                        TextRange textRange = getTextRangeWithSemicolonAndPrevWhitespace(comment);
                        consoleLogRangeList.add(textRange);
                    }
                } else if (element instanceof XmlToken xmlToken) {
                    int endOffset = getEndOffsetNonContainSpace(xmlToken);
                    if (endOffset != -1) {
                        TextRange textRange = getTextRangeWithSemicolonAndPrevWhitespace(xmlToken, document, endOffset);
                        if (textRange != null) {
                            consoleLogRangeList.add(textRange);
                        }
                    }
                }
            }
        });
        return consoleLogRangeList;
    }

    /**
     * 检测当前文件所有文本的console.log语句位置，但是按textRange的距离【近->远】排序
     * （暂未使用，本来考虑在文件比较大的时候使用异步的，以后再说吧）
     *
     * @param psiFile   文件PSI对象
     * @param textRange 原目的是当前光标所在文本范围
     * @return 按textRange的距离【近->远】排序的console.log调用表达式文本位置集合，
     * 返回的集合包括用户自己编写的console.log也包括符合插件编写规范的console.log
     */
    public static List<TextRange> detectAll(PsiFile psiFile, Document document, TextRange textRange) {
        List<TextRange> consoleLogRangeList = new ArrayList<>();
        psiFile.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);

                if (element instanceof JSCallExpression callExpression) {
                    if (isConsoleLog(callExpression)) {
                        TextRange textRange = getTextRangeWithSemicolonAndPrevWhitespace(callExpression, document);
                        consoleLogRangeList.add(textRange);
                    }
                }
                if (element instanceof PsiComment comment) {
                    if (isConsoleLog(comment)) {
                        TextRange textRange = getTextRangeWithSemicolonAndPrevWhitespace(comment);
                        consoleLogRangeList.add(textRange);
                    }
                }
            }
        });
        consoleLogRangeList.sort(
                Comparator.comparingInt(x -> Math.abs(x.getStartOffset() - textRange.getStartOffset()))
        );
        return consoleLogRangeList;
    }

    /**
     * 检测当前文件所有文本的console.log语句行号
     *
     * @param psiFile 文件PSI对象
     * @return console.log调用表达式文本行号集合，返回的集合包括用户自己编写的console.log也包括符合插件编写规范的console.log
     */
    public static TreeMap<TextRange, List<Integer>> detectAllButSkipComment(PsiFile psiFile, Document document) {
        TreeMap<TextRange, List<Integer>> result = new TreeMap<>(Comparator.comparingInt(TextRange::getStartOffset));
        psiFile.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);

                if (element instanceof JSCallExpression callExpression) {
                    if (isConsoleLog(callExpression)) {
                        TextRange textRange = callExpression.getTextRange();
                        List<Integer> consoleLogLineNumberList = new ArrayList<>();
                        int startLineNumber = document.getLineNumber(textRange.getStartOffset());
                        int endLineNumber = document.getLineNumber(textRange.getEndOffset());
                        for (; startLineNumber <= endLineNumber; ++startLineNumber) {
                            consoleLogLineNumberList.add(startLineNumber);
                        }
                        result.put(textRange, consoleLogLineNumberList);
                    }
                }
            }
        });
        return result;
    }

    /**
     * 检测当前文件所有文本的console.log语句位置
     *
     * @param psiFile 文件PSI对象
     * @return console.log调用表达式文本位置集合，返回的集合包括用户自己编写的console.log也包括符合插件编写规范的console.log
     */
    public static List<TextRange> detectAllOnlyComment(PsiFile psiFile) {
        List<TextRange> consoleLogRangeList = new ArrayList<>();
        psiFile.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);

                if (element instanceof PsiComment comment) {
                    if (isConsoleLog(comment)) {
                        TextRange textRange = getCommentSignalTextRange(comment);
                        if (textRange != null && !textRange.isEmpty()) {
                            consoleLogRangeList.add(textRange);
                        }
                    }
                }
            }
        });
        return consoleLogRangeList;
    }

    /**
     * 判断是否为console.log调用表达式
     *
     * @param callExpression 调用表达式对象
     * @return 如果时console.log调用表达式则返回true，否则返回false
     */
    public static boolean isConsoleLog(JSCallExpression callExpression) {
        String callExpressionText = callExpression.getText();
        if (StringUtils.isNotEmpty(callExpressionText) &&
                callExpressionText.matches(
                        SettingConstant.ALL_REGEX +
                                SettingConstant.CONSOLE_LOG_BEGIN_REGEX +
                                SettingConstant.ALL_REGEX
                )
        ) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否为console.log调用表达式（哦我的老天爷，console语句改的太炸裂的真的很难检测得到，例如下面）<br/>
     * // console.<br/>
     * // log("<br/>
     * // ai ~ abstract:", abstract<br/>
     * // );<br/>
     * 需要针对多行做处理，额，应该只有我在写这个插件的时候会改成这么逆天的格式吧。<br/>
     * 不多做处理了，只能管在同一行的
     *
     * @param psiComment 注释掉的表达式
     * @return 如果时console.log调用表达式则返回true，否则返回false
     */
    public static boolean isConsoleLog(PsiComment psiComment) {
        String comment = psiComment.getText();
        if (comment != null) {
            return comment.contains("console") && isMatchCommonConsoleLog(comment.substring(comment.indexOf("console")));
        }
        return false;
    }

    private static boolean isMatchCommonConsoleLog(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.matches(TextFormatContext.CONSOLE_LOG_BEGIN_REGEX + SettingConstant.ALL_REGEX
                + TextFormatContext.CONSOLE_LOG_END_COMPOSITE_NO_VARIABLE_REGEX);
    }

    /**
     * 获取带分号范围的表达式文本范围+表达式左边空白字符
     *
     * @param callExpression 调用表达式对象
     * @return 表达式文本范围对象
     */
    public static TextRange getTextRangeWithSemicolonAndPrevWhitespace(JSCallExpression callExpression, Document document) {
        TextRange originalRange = callExpression.getTextRange();
        int startOffset = originalRange.getStartOffset();
        int endOffset = originalRange.getEndOffset();
        CharSequence text = document.getCharsSequence();

        startOffset = getStartOffsetContainSpace(startOffset, text);

        PsiElement callExpressionStatement = PsiTreeUtil.getParentOfType(callExpression, JSExpressionStatement.class);
        if (callExpressionStatement != null) {
            // 获取带分号结束符的对象
            endOffset = callExpressionStatement.getTextRange().getEndOffset();
        } else {
            PsiElement nextSibling = callExpression.getNextSibling();
            // 检查下一个兄弟节点是否是分号
            if (nextSibling != null && ";".equals(nextSibling.getText())) {
                endOffset = nextSibling.getTextRange().getEndOffset();
            }
        }
        return new TextRange(startOffset, endOffset);
    }

    /**
     * 获取带分号范围的注释文本范围+表达式左边空白字符
     * 示例：\n   console.log
     *
     * @param comment 注释对象
     * @return 注释整体文本范围对象
     */
    public static TextRange getTextRangeWithSemicolonAndPrevWhitespace(PsiComment comment) {
        TextRange originalRange = comment.getTextRange();
        PsiElement preSibling = comment.getPrevSibling();
        int startOffset = originalRange.getStartOffset();
        int endOffset = originalRange.getEndOffset();

        if (preSibling != null && preSibling.getText().matches("\\s*")) {
            startOffset = preSibling.getTextRange().getStartOffset();
        }
        return new TextRange(startOffset, endOffset);
    }

    /**
     * 获取带分号范围的注释文本范围+表达式左边空白字符
     * 示例：\n   console.log
     *
     * @param xmlToken 注释对象
     * @return 注释整体文本范围对象
     */
    public static TextRange getTextRangeWithSemicolonAndPrevWhitespace(XmlToken xmlToken, Document document, Integer endOffset) {
        String xmlTokenText = xmlToken.getText();
        if (xmlTokenText != null && xmlTokenText.contains("console")) {
            TextRange originalRange = xmlToken.getTextRange();
            // 获取包括所有空格的前半段
            int startOffset = originalRange.getStartOffset();
            CharSequence text = document.getCharsSequence();
            startOffset = getStartOffsetContainSpace(startOffset, text);
            return new TextRange(startOffset, endOffset);
        }
        return null;
    }

    /**
     * 扩大开始偏移量到上一语句末尾
     *
     * @param startOffset 开始偏移量
     * @param text        文本
     * @return 包含空格的开始偏移量
     */
    private static int getStartOffsetContainSpace(int startOffset, CharSequence text) {
        for (startOffset = startOffset - 1; startOffset >= 0; --startOffset) {
            char c = text.charAt(startOffset);

            if (!Character.isWhitespace(c)) {
                // 遇到非空格字符停下并回撤偏移量
                ++startOffset;
                break;
            }
            if (c == '\n') {
                // 遇到换行停下
                break;
            }
        }
        return startOffset;
    }

    /**
     * 扩大开始偏移量到console.log表达式末尾
     *
     * @param element console.log起始文本
     * @return 末尾偏移量
     */
    private static int getEndOffsetNonContainSpace(PsiElement element) {
        if (element.getText().contains("console")) {
            StringBuilder str = new StringBuilder();
            str.append(element.getText());
            if (isMatchCommonConsoleLog(str.toString())) {
                return element.getTextRange().getEndOffset();
            }
            while (element.getNextSibling() != null) {
                str.append(element.getNextSibling().getText());
                element = element.getNextSibling();
                if (isMatchCommonConsoleLog(str.toString())) {
                    break;
                }
            }
            if (element.getNextSibling() != null) {
                if (";".equals(element.getNextSibling().getText())) {
                    return element.getNextSibling().getTextRange().getEndOffset();
                } else {
                    return element.getTextRange().getEndOffset();
                }
            }
        }
        return -1;
    }

    /**
     * 获取带分号范围的注释文本范围+表达式左边空白字符
     * 示例：\n   console.log
     *
     * @param comment 注释对象
     * @return 注释整体文本范围对象
     */
    public static TextRange getCommentSignalTextRange(PsiComment comment) {
        TextRange originalRange = comment.getTextRange();
        int startOffset = originalRange.getStartOffset();
        int commentSignalIndex = comment.getText().indexOf("// ");
        if (commentSignalIndex != -1) {
            // 此处获取完整的console.log表达式语句用于UnCommentAllConsoleLogAction正则匹配文本
            // 匹配ok后通过startOffset - COMMENT_SIGNAL_LENGTH获取起始位置，startOffset作为结束位置删除注释符号【// 】
            startOffset += SettingConstant.COMMENT_SIGNAL_LENGTH;
            return new TextRange(startOffset, originalRange.getEndOffset());
        }
        return null;
    }
}
