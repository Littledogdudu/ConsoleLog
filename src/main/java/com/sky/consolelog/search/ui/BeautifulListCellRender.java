package com.sky.consolelog.search.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.util.ui.JBUI;
import com.sky.consolelog.entities.ConsoleLogSearchInfo;
import com.sky.consolelog.setting.storage.ConsoleLogSettingState;

import javax.swing.*;
import java.awt.*;

/**
 * 列表样式渲染器
 *
 * @author SkySource
 * @Date: 2025/4/18 21:41:30
 */
public class BeautifulListCellRender extends JPanel implements ListCellRenderer<ConsoleLogSearchInfo> {
    private static JLabel textLabel;
    private static JLabel lineLabel;
    private static JLabel arrowLabel;

    public BeautifulListCellRender() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(JBColor.WHITE);
        setBorder(JBUI.Borders.empty(10));

        ConsoleLogSettingState settings = ApplicationManager.getApplication().getService(ConsoleLogSettingState.class);
        setSideListItemFontSize(settings.sideFontSize);
        // 表达式文本标签
        textLabel.setOpaque(false);
        textLabel.setForeground(JBColor.BLACK);
        // 行号标签
        lineLabel.setOpaque(false);
        lineLabel.setForeground(JBColor.GRAY);
        // 箭头标签
        arrowLabel.setOpaque(false);
        arrowLabel.setForeground(JBColor.GRAY);

        // 创建一个中间面板来容纳 lineLabel、arrowLabel 和 textLabel
        JPanel lineArrow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        lineArrow.setOpaque(false);
        lineArrow.add(lineLabel);
        lineArrow.add(arrowLabel);

        add(lineArrow, BorderLayout.WEST);
        add(textLabel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        GraphicsUtil.setupAntialiasing(g2);
        g2.setColor(new JBColor(new Color(0, 0, 0, 20), new Color(169, 169, 169, 20)));
        g2.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 10, 10);
        g2.dispose();
        super.paintComponent(g2);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ConsoleLogSearchInfo> list, ConsoleLogSearchInfo value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value != null) {
            String line = String.valueOf(value.getLine() + 1);
            textLabel.setText(value.getText());
            lineLabel.setText(line);
            setToolTipText(createToolTipText(value.getText(), line));
        } else {
            textLabel.setText("");
            lineLabel.setText("");
            setToolTipText(null);
        }

        if (isSelected) {
            setBackground(JBColor.LIGHT_GRAY);
            textLabel.setForeground(JBColor.BLACK);
            lineLabel.setForeground(JBColor.BLACK);
        } else {
            setBackground(JBColor.WHITE);
            textLabel.setForeground(JBColor.BLACK);
            lineLabel.setForeground(JBColor.GRAY);
        }

        return this;
    }

    private String createToolTipText(String text, String line) {
        if (text.length() > 50) {
            return text + "【" + line + "】";
        }
        return null;
    }

    public static void setSideListItemFontSize(int size) {
        Font font = new Font(UIManager.getFont("Label.font").getFontName(), Font.PLAIN, size);
        if (textLabel == null || lineLabel == null || arrowLabel == null) {
            textLabel = new JLabel();
            lineLabel = new JLabel();
            arrowLabel = new JLabel("→");
        }
        textLabel.setFont(font);
        lineLabel.setFont(font);
        arrowLabel.setFont(font);
    }
}
