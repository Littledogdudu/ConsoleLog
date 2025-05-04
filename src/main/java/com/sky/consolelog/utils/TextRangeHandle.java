package com.sky.consolelog.utils;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;

import java.util.*;

/**
 * 处理当前选中文本区域与表达式位置工具
 *
 * @author SkySource
 * @Date: 2025/5/3 22:14
 */
public class TextRangeHandle {
    private TextRangeHandle() {}

    /**
     * 当设置为仅处理选中区域内表达式时，返回 选中区域内 文本域
     * @param editor 编辑器对象
     * @param consoleLogRangeList 当前编辑器内文本所有表达式对应的文本域集合
     * @param isOperationInSelectionSetting 选中文本时是否仅在选中区域操作
     * @return 选中区域内 指定 文本域
     */
    public static List<TextRange> handleSelectedAndConsoleLogTextRange(Editor editor, List<TextRange> consoleLogRangeList, boolean isOperationInSelectionSetting) {
        List<TextRange> consoleLogNewRangeList = consoleLogRangeList;
        if (editor.getSelectionModel().getSelectedText() == null || editor.getSelectionModel().getSelectedText().isEmpty()) {
            return consoleLogNewRangeList;
        }
        if (isOperationInSelectionSetting) {
            int[] selectedStartOffset = editor.getSelectionModel().getBlockSelectionStarts();
            int[] selectedEndOffset = editor.getSelectionModel().getBlockSelectionEnds();
            consoleLogNewRangeList = new ArrayList<>();
            int selectedIndex = 0;
            int consoleLogIndex = 0;
            int selectionLength = selectedStartOffset.length;
            int consoleLogRangeLength = consoleLogRangeList.size();
            while (selectedIndex < selectionLength && consoleLogIndex < consoleLogRangeLength) {
                TextRange range = consoleLogRangeList.get(consoleLogIndex);
                if (selectedEndOffset[selectedIndex] <= range.getStartOffset()) {
                    // 选中的结束位置小于匹配项的开始位置（匹配项在选中位置后面）
                    // 选中位置：    -----↓--------↓----------
                    // 匹配项位置:   -----------------↓---↓---
                    ++selectedIndex;
                } else if (selectedStartOffset[selectedIndex] >= range.getEndOffset()) {
                    // 选中的开始位置大于匹配项的结束位置（选中位置在匹配项后面）
                    // 选中位置：    -------------↓---------↓-
                    // 匹配项位置:   -----↓---↓---------------
                    ++consoleLogIndex;
                } else {
                    consoleLogNewRangeList.add(range);
                    ++consoleLogIndex;
                }
            }
        }
        return consoleLogNewRangeList;
    }

    /**
     * 当设置为仅处理选中区域内表达式时，返回 选中区域内 文本域
     * @param editor 编辑器对象
     * @param consoleLogRangeMap 当前编辑器内文本所有表达式对应的文本域集合
     * @param isOperationInSelectionSetting 选中文本时是否仅在选中区域操作
     * @return 选中区域内 指定 文本域
     */
    public static Map<TextRange, List<Integer>> handleSelectedAndConsoleLogTextRange(Editor editor, Map<TextRange, List<Integer>> consoleLogRangeMap, boolean isOperationInSelectionSetting) {
        Map<TextRange, List<Integer>> consoleLogNewRangeMap = consoleLogRangeMap;
        if (editor.getSelectionModel().getSelectedText() == null || editor.getSelectionModel().getSelectedText().isEmpty()) {
            return consoleLogNewRangeMap;
        }
        if (isOperationInSelectionSetting) {
            int[] selectedStartOffset = editor.getSelectionModel().getBlockSelectionStarts();
            int[] selectedEndOffset = editor.getSelectionModel().getBlockSelectionEnds();
            consoleLogNewRangeMap = new TreeMap<>(Comparator.comparingInt(TextRange::getStartOffset));
            List<TextRange> consoleLogStartOffsets = new ArrayList<>(consoleLogRangeMap.keySet());
            int selectedIndex = 0;
            int consoleLogIndex = 0;
            int selectionLength = selectedStartOffset.length;
            int consoleLogRangeLength = consoleLogStartOffsets.size();
            while (selectedIndex < selectionLength && consoleLogIndex < consoleLogRangeLength) {
                TextRange range = consoleLogStartOffsets.get(consoleLogIndex);
                if (selectedEndOffset[selectedIndex] <= range.getStartOffset()) {
                    // 选中的结束位置小于匹配项的开始位置（匹配项在选中位置后面）
                    // 选中位置：    -----↓--------↓----------
                    // 匹配项位置:   -----------------↓---↓---
                    ++selectedIndex;
                } else if (selectedStartOffset[selectedIndex] >= range.getEndOffset()) {
                    // 选中的开始位置大于匹配项的结束位置（选中位置在匹配项后面）
                    // 选中位置：    -------------↓---------↓-
                    // 匹配项位置:   -----↓---↓---------------
                    ++consoleLogIndex;
                } else {
                    consoleLogNewRangeMap.put(range, consoleLogRangeMap.get(range));
                    ++consoleLogIndex;
                }
            }
        }
        return consoleLogNewRangeMap;
    }
}
