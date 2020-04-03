package org.intellij.sequencer.diagram;

import java.awt.*;

public class DisplayCall extends DisplayLink {

    private static final Stroke DASH_STROKE = new BasicStroke(1.0f,
            BasicStroke.CAP_SQUARE,
            BasicStroke.JOIN_MITER,
            6.0f,
            new float[]{6.0f, 3.0f},
            0.0f);

    DisplayCall(Link link, DisplayObject from, DisplayObject to, int seq) {
        super(link, from, to, seq);
        from.addCall(this);
    }

    @Override
    void drawLine(Graphics2D g2) {
        if (_from.getObjectInfo().hasAttribute(Info.INTERFACE_ATTRIBUTE) || _from.getObjectInfo().hasAttribute(Info.ABSTRACT_ATTRIBUTE)) {
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(DASH_STROKE);
            super.drawLine(g2);
            g2.setStroke(oldStroke);
        } else {
            super.drawLine(g2);
        }
    }


    public String toString() {
        return "DisplayCall " + _link.getName() + " from " + _from + " to " + _to + " seq " + _seq;
    }
}

