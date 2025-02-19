package com.sky.consolelog.utils;

import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 * @author by: SkySource
 * @Description: 专门用来检查并获取完整变量名的工具类
 * @Date: 2025/2/19 12:09
 */
public class PsiVariableUtil {
    public static String getVariableNameByOffsetIndex(Editor editor, PsiFile psiFile, Caret caret, int elementAtCaretIndex) {
        // 光标处的元素
        PsiElement elementAtCaret = psiFile.findElementAt(elementAtCaretIndex);
        PsiElement parent = elementAtCaret.getParent();

        // 光标向前一个字符的元素（因为当光标在元素末尾时会判定为该变量父级）
        int elementAtCartNearly = elementAtCaretIndex - 1;
        PsiElement elementAtCaretNearly = psiFile.findElementAt(elementAtCartNearly);
        PsiElement nearlyElementParent = null;
        if (elementAtCaretNearly != null) {
            nearlyElementParent = elementAtCaretNearly.getParent();
        }

        PsiElement parentParentElement = parent.getParent();
        PsiElement nearlyParentParentElement = null;
        if (nearlyElementParent != null) {
            nearlyParentParentElement = nearlyElementParent.getParent();
        }

        if (parentParentElement instanceof JSIndexedPropertyAccessExpression) {
            //如果是数组类型
            String word = parentParentElement.getText();
            if (word != null) {
                return word;
            }
        } else if (nearlyParentParentElement instanceof JSIndexedPropertyAccessExpression) {
            //如果是数组类型
            String word = nearlyParentParentElement.getText();
            if (word != null) {
                return word;
            }
        } else if (parent instanceof JSReferenceExpression) {
            String word = parent.getText();
            if (word != null) {
                return word;
            }
        } else if (nearlyElementParent instanceof JSReferenceExpression) {
            String word = nearlyElementParent.getText();
            if (word != null) {
                return word;
            }
        }
        return defaultGetVariableName(editor, caret);
    }

    public static String defaultGetVariableName(Editor editor, Caret caret) {
        // 首先获取光标的偏移量
        int offset = caret.getOffset();
        Document document = editor.getDocument();
        // 获取光标之前的所有文本
        String line = document.getText().substring(0, offset);

        // 获取单词前后内容，直到遇到空格
        int wordStart = findWordStart(line, offset);
        int wordEnd = findWordEnd(document, offset);

        if (wordStart >= 0 && wordEnd > wordStart) {
            return document.getText().substring(wordStart, wordEnd);
        }
        return null;
    }

    private static int findWordStart(String text, int offset) {
        for (int i = offset - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '-') {
                return i + 1;
            }
        }
        return 0;
    }

    private static int findWordEnd(Document document, int offset) {
        int length = document.getTextLength();
        for (int i = offset; i < length; i++) {
            char c = document.getCharsSequence().charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '-') {
                return i;
            }
        }
        return length;
    }
}
