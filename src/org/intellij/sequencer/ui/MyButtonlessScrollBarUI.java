package org.intellij.sequencer.ui;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.LightColors;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 20/10/2016.
 */
public class MyButtonlessScrollBarUI extends BasicScrollBarUI {

    public static JBColor getGradientLightColor() {
        return new JBColor(Gray._251, Gray._95);
    }

    public static JBColor getGradientDarkColor() {
        return new JBColor(Gray._215, Gray._80);
    }

    private static JBColor getGradientThumbBorderColor() {
        return new JBColor(Gray._201, Gray._85);
    }

    public static JBColor getTrackBackground() {
        return new JBColor(LightColors.SLIGHTLY_GRAY, UIUtil.getListBackground());
    }

    public static JBColor getTrackBorderColor() {
        return new JBColor(Gray._230, UIUtil.getListBackground());
    }

    private static final BasicStroke BORDER_STROKE = new BasicStroke();

    private static int getAnimationColorShift() {
        return UIUtil.isUnderDarcula() ? 20 : 40;
    }

    private int myAnimationColorShift = 0;
    private boolean myMouseIsOverThumb = true;
    public static final int DELAY_FRAMES = 4;
    public static final int FRAMES_COUNT = 10 + DELAY_FRAMES;

    protected MyButtonlessScrollBarUI() {
    }

    @Override
    public void layoutContainer(Container scrollbarContainer) {
        try {
            super.layoutContainer(scrollbarContainer);
        } catch (NullPointerException ignore) {
            //installUI is not performed yet or uninstallUI has set almost every field to null. Just ignore it //IDEA-89674
        }
    }

    @Override
    protected ModelListener createModelListener() {
        return new ModelListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (scrollbar != null) {
                    super.stateChanged(e);
                }
            }
        };
    }

    public int getDecrementButtonHeight() {
        return decrButton.getHeight();
    }

    public int getIncrementButtonHeight() {
        return incrButton.getHeight();
    }

    public static BasicScrollBarUI createNormal() {
        return new MyButtonlessScrollBarUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        scrollbar.setFocusable(false);
    }

    @Override
    protected void installDefaults() {
        final int incGap = UIManager.getInt("ScrollBar.incrementButtonGap");
        final int decGap = UIManager.getInt("ScrollBar.decrementButtonGap");
        try {
            UIManager.put("ScrollBar.incrementButtonGap", 0);
            UIManager.put("ScrollBar.decrementButtonGap", 0);
            super.installDefaults();
        } finally {
            UIManager.put("ScrollBar.incrementButtonGap", incGap);
            UIManager.put("ScrollBar.decrementButtonGap", decGap);
        }
    }

    @Override
    protected void installListeners() {
        super.installListeners();
    }

    @Override
    public Rectangle getThumbBounds() {
        return super.getThumbBounds();
    }

    @Override
    protected void uninstallListeners() {
        if (scrollTimer != null) {
            // it is already called otherwise
            super.uninstallListeners();
        }
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle bounds) {
        g.setColor(getTrackBackground());
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

        g.setColor(getTrackBorderColor());
        if (isVertical()) {
            g.drawLine(bounds.x, bounds.y, bounds.x, bounds.y + bounds.height);
        } else {
            g.drawLine(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y);
        }
    }

    @Override
    protected Dimension getMinimumThumbSize() {
        final int thickness = getThickness();
        return isVertical() ? new Dimension(thickness, thickness * 2) : new Dimension(thickness * 2, thickness);
    }

    protected int getThickness() {
        return 13;
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        int thickness = getThickness();
        return new Dimension(thickness, thickness);
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        return getMaximumSize(c);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return getMaximumSize(c);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }

        g.translate(thumbBounds.x, thumbBounds.y);
        paintMaxiThumb((Graphics2D) g, thumbBounds);
        g.translate(-thumbBounds.x, -thumbBounds.y);
    }

    private void paintMaxiThumb(Graphics2D g, Rectangle thumbBounds) {
        final boolean vertical = isVertical();
        int hGap = vertical ? 2 : 1;
        int vGap = vertical ? 1 : 2;

        int w = adjustThumbWidth(thumbBounds.width - hGap * 2);
        int h = thumbBounds.height - vGap * 2;

        // leave one pixel between thumb and right or bottom edge
        if (vertical) {
            h -= 1;
        } else {
            w -= 1;
        }

        final Paint paint;
        final Color start = adjustColor(getGradientLightColor());
        final Color end = adjustColor(getGradientDarkColor());

        if (vertical) {
            paint = UIUtil.getGradientPaint(1, 0, start, w + 1, 0, end);
        } else {
            paint = UIUtil.getGradientPaint(0, 1, start, 0, h + 1, end);
        }

        g.setPaint(paint);
        g.fillRect(hGap + 1, vGap + 1, w - 1, h - 1);

        final Stroke stroke = g.getStroke();
        g.setStroke(BORDER_STROKE);
        g.setColor(getGradientThumbBorderColor());
        g.drawRoundRect(hGap, vGap, w, h, 3, 3);
        g.setStroke(stroke);
    }

    @Override
    public boolean getSupportsAbsolutePositioning() {
        return true;
    }

    protected int adjustThumbWidth(int width) {
        return width;
    }

    protected Color adjustColor(Color c) {
        if (myAnimationColorShift == 0) return c;
        final int sign = UIUtil.isUnderDarcula() ? -1 : 1;
        return Gray.get(Math.max(0, Math.min(255, c.getRed() - sign * myAnimationColorShift)));
    }

    private boolean isVertical() {
        return scrollbar.getOrientation() == Adjustable.VERTICAL;
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return new EmptyButton();
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return new EmptyButton();
    }

    private static class EmptyButton extends JButton {
        private EmptyButton() {
            setFocusable(false);
            setRequestFocusEnabled(false);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(0, 0);
        }

        @Override
        public Dimension getPreferredSize() {
            return getMaximumSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return getMaximumSize();
        }
    }
}