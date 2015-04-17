package org.intellij.sequencer.diagram;

import java.awt.*;

public class DisplayCallReturn extends DisplayLink {

    private static final Stroke DASH_STROKE = new BasicStroke(1.0f,
          BasicStroke.CAP_SQUARE,
          BasicStroke.JOIN_MITER,
          10.0f,
          new float[]{10.0f, 5.0f},
          0.0f);

    DisplayCallReturn(Link link, DisplayObject from, DisplayObject to, int seq) {
        super(link, from, to, seq);
        from.addCallReturn(this);
    }

    void drawText(Graphics2D g2) {
    }

    void drawLine(Graphics2D g2) {
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(DASH_STROKE);
        super.drawLine(g2);
        g2.setStroke(oldStroke);
    }

    public String toString() {
        return "DisplayCallReturn " + _link.getName() + " from <" + _from + "> to <" + _to + "> seq " + _seq;
    }

    public boolean isReturnLink() {
        return true;
    }
}

