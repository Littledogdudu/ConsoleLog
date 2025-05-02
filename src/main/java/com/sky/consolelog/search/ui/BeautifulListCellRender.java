package com.sky.consolelog.search.ui;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.sky.consolelog.entities.ConsoleLogSearchInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentListener;

/**
 * 列表样式渲染器
 *
 * @author SkySource
 * @Date: 2025/4/18 21:41:30
 */
public class BeautifulListCellRender extends JPanel implements ListCellRenderer<ConsoleLogSearchInfo> {
    private final JLabel textLabel;
    private final JLabel lineLabel;

    public BeautifulListCellRender() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(JBColor.WHITE);
        setBorder(JBUI.Borders.empty(10));

        textLabel = new JLabel();
        textLabel.setOpaque(false);
        textLabel.setFont(UIUtil.getLabelFont(UIUtil.FontSize.SMALL));
        textLabel.setForeground(JBColor.BLACK);

        lineLabel = new JLabel();
        lineLabel.setOpaque(false);
        lineLabel.setFont(UIUtil.getLabelFont(UIUtil.FontSize.SMALL));
        lineLabel.setForeground(JBColor.GRAY);

        add(textLabel, BorderLayout.CENTER);
        add(lineLabel, BorderLayout.EAST);
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
}
