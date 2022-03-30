package org.intellij.sequencer.diagram;

import java.awt.*;
import java.awt.geom.Rectangle2D;

class TextBox {

    private final int _pad = 5;

    private String _text = null;
    private int _boxWidth = -1;
    private int _boxHeight = -1;
    private int _textOffset = -1;
    private Rectangle2D _rect;

    TextBox(String text) {
        _text = text;
    }

    public int getPad() {
        return _pad;
    }

    public int getWidth() {
        return _boxWidth;
    }

    public int getHeight() {
        return _boxHeight;
    }

    public int getTextOffset() {
        return _textOffset;
    }

    public int getRealWidth() {
        return (int)_rect.getWidth();
    }

    public int getRealHeight() {
        return (int)_rect.getHeight();
    }

    void init(Graphics2D g2) {
        FontMetrics fm = g2.getFontMetrics();
        int height = fm.getMaxAscent() + fm.getMaxDescent();

        _rect = fm.getStringBounds(_text, g2);

        _boxHeight = height + (_pad * 2);
        _boxWidth = (int)_rect.getWidth() + (_pad * 2);
        _textOffset = fm.getMaxAscent() + _pad;
    }
}

