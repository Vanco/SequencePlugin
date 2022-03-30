package org.intellij.sequencer.diagram;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class PreviewPanel extends JPanel {
    private final JScrollPane _scrollPane;
    private final Display _display;
    private BufferedImage _image;
    private double _xScale;
    private double _yScale;

    public PreviewPanel(JScrollPane scrollPane, Display display) {
        super(false);
        _scrollPane = scrollPane;
        _display = display;
        setBorder(BorderFactory.createCompoundBorder(
              BorderFactory.createRaisedBevelBorder(),
              BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                moveViewport(e);
            }

            public void mouseClicked(MouseEvent e) {
                moveViewport(e);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                moveViewport(e);
            }
        });
    }

    private void updateImage() {
        Dimension displaySize = _display.getFullSize();
        _xScale = (double)getWidth()/displaySize.width;
        _yScale = (double)getHeight()/displaySize.height;
        if(_xScale > 1.)
            _xScale = 1.;
        if(_yScale > 1.)
            _yScale = 1.;
//        _xScale = _yScale = Math.min(_xScale, _yScale);
        _image = ImageUtil.createImage(getWidth(), getHeight(), BufferedImage.TYPE_USHORT_555_RGB);
        Graphics2D g2 = _image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(JBColor.WHITE);
        g2.fillRect(0, 0, getWidth(), getHeight());
        AffineTransform at = AffineTransform.getScaleInstance(_xScale, _yScale);
        g2.setTransform(at);
        _display.paintComponentWithHeader(g2);
    }

    private void moveViewport(MouseEvent e) {
        int newX = (int)Math.round(e.getX() / _xScale);
        int newY = (int)Math.round(e.getY() / _yScale);

        int eW = _scrollPane.getViewport().getExtentSize().width;
        int eH = _scrollPane.getViewport().getExtentSize().height;
        newX -= eW / 2;
        newY -= eH / 2;

        newX = Math.max(newX, 0);
        newY = Math.max(newY, 0);

        Dimension fullSize = _display.getFullSize();
        newX = Math.min(newX, fullSize.width - eW);
        newY = Math.min(newY, fullSize.height - eH);

        if(eW >= fullSize.width)
            newX = 0;
        if(eH >= fullSize.height)
            newY = 0;

        Point newPos = new Point(newX, newY);
        _scrollPane.getViewport().setViewPosition(newPos);
        repaint();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(_image == null)
            updateImage();
        Graphics2D g2 = (Graphics2D)g;
        g2.drawImage(_image, 0, 0, this);
        Rectangle viewRect = _scrollPane.getViewport().getViewRect();
        int x = (int)Math.round(viewRect.x * _xScale) + 2;
        int y = (int)Math.round(viewRect.y * _yScale) + 2;
        int width = (int)Math.round(viewRect.width * _xScale);
        if(width > getWidth())
            width = getWidth();
        if(width > 6) width -= 6;
        int height = (int)Math.round(viewRect.height * _yScale);
        if(height > getHeight())
            height = getHeight();
        if(height > 6) height -= 6;
        g2.drawRect(x, y, width, height);
    }
}
