package org.intellij.sequencer.diagram;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class DisplaySelfLink extends DisplayLink {

    DisplaySelfLink(Link link, DisplayObject from, DisplayObject to, int seq) {
        super(link, from, to, seq);
    }

    void initTwo() {

        _lineStartX = _from.getRightX(_seq);
        _lineEndX = _lineStartX + _textBox.getWidth();

        int gap = _lineEndX - _lineStartX;
        _textXOffset = (gap - _textBox.getWidth());
        _lineEndX = (_lineEndX - _lineStartX)/2 < 10? _lineEndX: (_lineEndX - _lineStartX)/2 + _lineStartX;
    }

    int getLinkHeight() {
        return getTextHeight() / 3;
    }

    void drawLine(Graphics2D g2) {
        super.drawLine(g2);
        g2.drawLine(_lineEndX, getEndY(), _lineEndX, getEndY() + getLinkHeight());
        g2.drawLine(_lineStartX, getEndY() + getLinkHeight(), _lineEndX, getEndY() + getLinkHeight());
    }

    void drawArrow(Graphics2D g2) {
        int lineY = getEndY() + getLinkHeight();
        int arrowTailX = _lineStartX + 4;
        g2.drawLine(arrowTailX, lineY - 3, _lineStartX, lineY);
        g2.drawLine(arrowTailX, lineY + 3, _lineStartX, lineY);
    }

    @Override
    void fillArrow(Graphics2D g2) {
        int lineY = getEndY() + getLinkHeight();
        int arrowTailX = _lineStartX + 4;

        int[] xPoints = {arrowTailX, _lineStartX, arrowTailX};
        int[] yPoints = {lineY - 3, lineY, lineY + 3};
        fillPolygon(g2, xPoints, yPoints);
    }

    public String toString() {
        return "DisplaySelfLink " + _link.getName() + " from " + _from + " to " + _to + " seq " + _seq;
    }
}

