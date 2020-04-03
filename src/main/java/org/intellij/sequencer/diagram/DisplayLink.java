package org.intellij.sequencer.diagram;

import com.intellij.ui.JBColor;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class DisplayLink extends ScreenObject {
    private static final Paint TEXT_COLOR = JBColor.foreground();
    private static final Paint LINK_COLOR = JBColor.foreground();
    private static final Stroke DASH_STROKE = new BasicStroke(1.0f,
          BasicStroke.CAP_SQUARE,
          BasicStroke.JOIN_MITER,
          4.0f,
          new float[]{4.0f, 2.0f},
          0.0f);

    protected Link _link;
    protected DisplayObject _from;
    protected DisplayObject _to;
    protected TextBox _textBox;
    int _textXOffset = -1;

    int _y = -1;
    int _seq;

    int _lineStartX = -1;
    int _lineEndX = -1;

    DisplayLink(Link link, DisplayObject from, DisplayObject to, int seq) {
        _from = from;
        _to = to;
        _seq = seq;
        _link = link;
        _textBox = new TextBox(link.getName());
    }

    void initOne(Graphics2D g2) {
        _textBox.init(g2);
    }

    void initTwo() {
        if(_from.getSeq() < _to.getSeq()) {
            _lineStartX = _from.getRightX(_seq);
            _lineEndX = _to.getLeftX(_seq);
        } else {
            _lineStartX = _from.getLeftX(_seq);
            _lineEndX = _to.getRightX(_seq);
        }

        int gap = Math.abs(_lineEndX - _lineStartX);
        _textXOffset = gap - _textBox.getWidth();
    }

    public Link getLink() {
        return _link;
    }

    DisplayObject getFrom() {
        return _from;
    }

    DisplayObject getTo() {
        return _to;
    }

    public Info getMethodInfo() {
        return _link.getMethodInfo();
    }

    int getSeq() {
        return _seq;
    }

    void setY(int y) {
        _y = y;
    }

    public int getY() {
        return _y;
    }

    public int getX() {
        return _from.getSeq() <= _to.getSeq()? _lineStartX: _lineEndX;
    }

    public int getWidth() {
        return Math.max(Math.abs(_lineEndX - _lineStartX), getTextWidth());
    }

    public int getHeight() {
        return getEndY() - getY();
    }

    public int getTextWidth() {
        return _textBox.getWidth();
    }

    public int getTextHeight() {
        return _textBox.getHeight();
    }

    int getLinkHeight() {
        return 0;
    }

    public String getToolTip() {
        return _link.getName();
    }

    boolean isSelfCall() {
        return _from.getSeq() == _to.getSeq();
    }

    public void paint(Graphics2D g2) {
        Rectangle clipBounds = g2.getClipBounds();
        if(clipBounds != null && !clipBounds.intersects(getX(), getY(), getWidth(), getHeight()))
            return;
        Stroke oldStroke = g2.getStroke();
        if(isSelected())
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawText(g2);
        drawLine(g2);
        if (isReturnLink()) {
            drawArrow(g2);
        } else {
            fillArrow(g2);
        }
        g2.setStroke(oldStroke);
    }
 
    void drawText(Graphics2D g2) {
        g2.setPaint(TEXT_COLOR);
        int textX = getX() + _textXOffset + _textBox.getPad();
        int textY = getY() + _textBox.getTextOffset();
        if(isSelected()) {
            Stroke oldStroke = g2.getStroke();
            g2.setStroke(DASH_STROKE);
            g2.drawRect(textX - 2, getY() + _textBox.getPad(),
                  _textBox.getRealWidth() + 2, _textBox.getRealHeight() + 2);
            g2.setStroke(oldStroke);
        }
        Font oldFont = g2.getFont();
        if(_link.getMethodInfo().hasAttribute(Info.ABSTRACT_ATTRIBUTE))
            g2.setFont(new Font(oldFont.getFontName(), Font.ITALIC, oldFont.getSize()));
        g2.drawString(_link.getName(), textX, textY);
        if(_link.getMethodInfo().hasAttribute(Info.STATIC_ATTRIBUTE)) {
            int y = textY + g2.getFontMetrics().getDescent() - 1;
            g2.drawLine(textX, y, textX + _textBox.getRealWidth(), y);
        }
        g2.setFont(oldFont);
    }

    void drawLine(Graphics2D g2) {
        g2.setPaint(LINK_COLOR);
        g2.drawLine(_lineStartX, getEndY(), _lineEndX, getEndY());
    }

    protected int getEndY() {
        int lineY = getY() + _textBox.getHeight();
        if(isSelected())
            lineY++;
        return lineY;
    }

    void drawArrow(Graphics2D g2) {
        int arrowTailX = _lineEndX;

        if(_lineStartX < _lineEndX)
            arrowTailX -= 4;
        else
            arrowTailX += 4;

        g2.drawLine(arrowTailX, getEndY() - 3, _lineEndX, getEndY());
        g2.drawLine(arrowTailX, getEndY() + 3, _lineEndX, getEndY());
    }

    void fillArrow(Graphics2D g2) {
        int arrowTailX = _lineEndX;

        if(_lineStartX < _lineEndX)
            arrowTailX -= 4;
        else
            arrowTailX += 4;

        int[] xPoints = {arrowTailX, _lineEndX, arrowTailX};
        int[] yPoints = {getEndY() - 3, getEndY(), getEndY() + 3};
        fillPolygon(g2, xPoints, yPoints);
    }

    protected void fillPolygon(Graphics2D g2, int[] xPoints, int[] yPoints) {
        GeneralPath filledPolygon = new GeneralPath();
        filledPolygon.moveTo(xPoints[0], yPoints[0]);
        for (int idx = 1; idx < xPoints.length; idx ++ ) {
            filledPolygon.lineTo(xPoints[idx], yPoints[idx]);
        }
        filledPolygon.closePath();
        g2.fill(filledPolygon);
    }

    public boolean isReturnLink() {
        return false;
    }
}

