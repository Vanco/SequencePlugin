package org.intellij.sequencer.diagram;

import java.awt.*;

public abstract class ScreenObject {

    private boolean _selected;

    public abstract String getToolTip();

    public abstract int getX();

    public abstract int getY();

    public abstract int getWidth();

    public abstract int getHeight();

    public boolean isInRange(int x, int y) {
        return getX() <= x && x <= getX() + getWidth() &&
                getY() <= y && y <= getY() + getHeight();
    }

    public boolean isSelected() {
        return _selected;
    }

    public void setSelected(boolean selected) {
        this._selected = selected;
    }

    public abstract void paint(Graphics2D g2);
}
