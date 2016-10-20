package org.intellij.sequencer.diagram;

import com.intellij.util.ui.UIUtil;
import org.intellij.sequencer.config.ConfigListener;
import org.intellij.sequencer.config.Configuration;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Display extends JComponent implements ModelTextListener, Scrollable, ConfigListener {
    private int _inset = 5;

    private Model _model = null;
    private SequenceListener _listener;

    private boolean _initialized = false;
    private DisplayHeader _displayHeader;
    private Diagram _diagram;

    public Display(Model model, SequenceListener listener) {
        _model = model;
        _diagram = new Diagram();
        _listener = listener;
        if(_listener == null)
            _listener = new NullListener();

        setPreferredSize(new Dimension(200, 200));
        setFocusable(true);
        setBackground(UIUtil.getListBackground());

        DisplayMouseAdapter displayMouseAdapter = new DisplayMouseAdapter();
        addMouseListener(displayMouseAdapter);
        model.addModelTextListener(this);

        _displayHeader = new DisplayHeader();
        _displayHeader.addMouseListener(displayMouseAdapter);
        _displayHeader.setBackground(getBackground());

        setToolTipText(" ");

        setQuery(model.getText());
    }

    public void dispose() {
        _model.removeModelTextListener(this);
    }

    public void modelTextChanged(ModelTextEvent event) {
        setQuery(event.getText());
    }

    private void setQuery(String query) {
        _diagram.build(query);
        _initialized = false;
        revalidate();
        repaint();
    }

    public synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;
        setupGraphics(g2);

        Insets insets = getInsets();
        g2.translate(insets.left, insets.top);

        if(!_initialized)
            layout(g2);

        _diagram.paint(g2);
    }

    private void setupGraphics(Graphics2D g2) {
        Configuration configuration = Configuration.getInstance();
        g2.setFont(new Font(configuration.FONT_NAME, Font.PLAIN, configuration.FONT_SIZE));
        if(configuration.USE_ANTIALIASING) {
            HashMap hintsMap = new HashMap();
            hintsMap.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.addRenderingHints(hintsMap);
        }
    }

    public String getToolTipText(MouseEvent event) {
        ScreenObject screenObject = _diagram.findScreenObjectByXY(event.getX(), event.getY());
        if(screenObject == null)
            return null;
        return screenObject.getToolTip();
    }

    private void layout(Graphics2D g2) {
        _initialized = true;
        Dimension dimension = _diagram.layoutObjects(g2, _inset);
        setPreferredSize(dimension);
        revalidate();
    }

    public void revalidate() {
        super.revalidate();
        if(_displayHeader != null)
            _displayHeader.revalidate();
    }

    public Diagram getDiagram() {
        return _diagram;
    }

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? 30 : 60;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? 60 : 120;
    }

    public boolean getScrollableTracksViewportWidth() {
        return getParent() instanceof JViewport && getParent().getWidth() > getPreferredSize().width;
//        return false;
    }

    public boolean getScrollableTracksViewportHeight() {
        return getParent() instanceof JViewport && getParent().getHeight() > getPreferredSize().height;
//        return false;
    }

    public void addNotify() {
        super.addNotify();
        Configuration.getInstance().addConfigListener(this);
        setScrollPaneHeaderView(getHeader());
    }

    public void removeNotify() {
        Configuration.getInstance().removeConfigListener(this);
        setScrollPaneHeaderView(null);
        super.removeNotify();
    }
    
    private void setScrollPaneHeaderView(Component header) {
        Component component = getParent();
        if(component instanceof JViewport) {
            Container container = component.getParent();
            if(container instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane)container;
                JViewport jViewport = scrollPane.getViewport();
                if(jViewport == null || jViewport.getView() != this)
                    return;
                scrollPane.setColumnHeaderView(header);
            }
        }
    }

    public void configChanged() {
        _initialized = false;
        repaintAll();
    }

    private void repaintAll() {
        _displayHeader.repaint();
        Display.this.repaint();
    }

    public Component getHeader() {
        return _displayHeader;
    }

    public Dimension getFullSize() {
        Dimension size = getPreferredSize();
        Dimension headerSize = _displayHeader.getPreferredSize();

        int width = Math.min(headerSize.width, size.width);
        int height = headerSize.height + size.height;
        return new Dimension(width, height);
    }

    public void saveImageToFile(File file) throws IOException {
        Color color = getBackground();
        setBackground(Color.white);
        Dimension size = getFullSize();
        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = image.createGraphics();
        paintComponentWithHeader(graphics);

        setBackground(color);
        ImageIO.write(image, "png", file);
    }

    public void paintComponentWithHeader(Graphics2D graphics) {
        Dimension size = getFullSize();
        graphics.fillRect(0, 0, size.width, size.height);
        _displayHeader.paintComponent(graphics);
        graphics.translate(0, _displayHeader.getHeight());
        paintComponent(graphics);
    }

    private class DisplayHeader extends JComponent {
        public DisplayHeader() {
            setToolTipText(" ");
        }

        public Dimension getPreferredSize() {
            return _diagram.getPreferredHeaderSize();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            setupGraphics(g2);
            _diagram.paintHeader(g2);
        }

        public String getToolTipText(MouseEvent event) {
            return Display.this.getToolTipText(event);
        }
    }

    private class DisplayMouseAdapter extends MouseAdapter {
        private ScreenObject selectedScreenObject;

        public void mouseReleased(MouseEvent e) {
            if(selectedScreenObject != null) {
                selectedScreenObject.setSelected(false);
                selectedScreenObject = null;
                repaint();
            }

            ScreenObject screenObject = _diagram.findScreenObjectByXY(e.getX(), e.getY());
            if(screenObject == null)
                return;
            setSelected(screenObject);
            if(isDoubleClick(e))
                _listener.selectedScreenObject(screenObject);
            else if(e.isPopupTrigger())
                _listener.displayMenuForScreenObject(screenObject, e.getX(), e.getY());
        }

        @Override
        public void mousePressed(MouseEvent e) {

            ScreenObject screenObject = _diagram.findScreenObjectByXY(e.getX(), e.getY());
            if(screenObject == null)
                return;
//            setSelected(screenObject);
            if(e.isPopupTrigger())
                _listener.displayMenuForScreenObject(screenObject, e.getX(), e.getY());
        }

        private boolean isDoubleClick(MouseEvent e) {
            return e.getClickCount() >= 2;
        }

        private void setSelected(ScreenObject screenObject) {
            selectedScreenObject = screenObject;
            selectedScreenObject.setSelected(true);
            repaint();
        }

        private void repaint() {
            repaintAll();
        }
    }

    private class NullListener implements SequenceListener {
        public void selectedScreenObject(ScreenObject screenObject) {
        }

        public void displayMenuForScreenObject(ScreenObject screenObject, int x, int y) {
        }
    }
}
