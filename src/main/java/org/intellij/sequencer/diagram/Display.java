package org.intellij.sequencer.diagram;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.ImageUtil;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.image.TIFFTranscoder;
import org.intellij.sequencer.config.ConfigListener;
import org.intellij.sequencer.config.SequenceSettingsState;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;

public class Display extends JComponent implements ModelTextListener, Scrollable, ConfigListener {
    private int _inset = 5;

    private final Model _model;
    private SequenceListener _listener;

    private boolean _initialized = false;
    private final DisplayHeader _displayHeader;
    private final Diagram _diagram;

    public Display(Model model, SequenceListener listener) {
        _model = model;
        _diagram = new Diagram();
        _listener = listener;
        if (_listener == null)
            _listener = new NullListener();

        setPreferredSize(new Dimension(200, 200));
        setFocusable(true);
        setBackground(JBColor.background());

        DisplayMouseAdapter displayMouseAdapter = new DisplayMouseAdapter();
        addMouseListener(displayMouseAdapter);
        _model.addModelTextListener(this);

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

        Graphics2D g2 = (Graphics2D) g;
        setupGraphics(g2);

        Insets insets = getInsets();
        g2.translate(insets.left, insets.top);

        if (!_initialized)
            layout(g2);

        _diagram.paint(g2);
    }

    private void setupGraphics(Graphics2D g2) {
        SequenceSettingsState sequenceSettingsState = SequenceSettingsState.getInstance();
        g2.setFont(new Font(sequenceSettingsState.FONT_NAME, Font.PLAIN, sequenceSettingsState.FONT_SIZE));
        if (sequenceSettingsState.USE_ANTIALIASING) {
            HashMap<Object, Object> hintsMap = new HashMap<>();
            hintsMap.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.addRenderingHints(hintsMap);
        }
    }

    public String getToolTipText(MouseEvent event) {
        ScreenObject screenObject = _diagram.findScreenObjectByXY(event.getX(), event.getY());
        if (screenObject == null)
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
        if (_displayHeader != null)
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
        SequenceSettingsState.getInstance().addConfigListener(this);
        setScrollPaneHeaderView(getHeader());
    }

    public void removeNotify() {
        SequenceSettingsState.getInstance().removeConfigListener(this);
        setScrollPaneHeaderView(null);
        super.removeNotify();
    }

    private void setScrollPaneHeaderView(Component header) {
        Component component = getParent();
        if (component instanceof JViewport) {
            Container container = component.getParent();
            if (container instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) container;
                JViewport jViewport = scrollPane.getViewport();
                if (jViewport == null || jViewport.getView() != this)
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

        int width = Math.max(headerSize.width, size.width);
        int height = headerSize.height + size.height;
        return new Dimension(width, height);
    }

    /**
     * Save image as png file.
     * Known issue: when image width * height greater than Int.MAX_VALUE, will throw <code> java.lang.NegativeArraySizeException</code>.
     * which is the limits of <code>Raster</code>
     * @param file file to be saved
     * @throws IOException
     */
    @Deprecated
    public void saveImageToFile(File file) throws IOException {
        Dimension size = getFullSize();
        BufferedImage image = ImageUtil.createImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
        try {
            String systemLookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(systemLookAndFeelClassName);
        } catch (Exception e) {
            //ignore
        }
        Graphics2D graphics = image.createGraphics();
        paintComponentWithHeader(graphics);

        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (UnsupportedLookAndFeelException e) {
            //ignore
        }
        ImageIO.write(image, "png", file);
    }

    public void saveImageToSvgFile(File file, String extension) throws IOException {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);
        // backup look and feel
        LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
        // set look and feel to system default
        try {
            String systemLookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(systemLookAndFeelClassName);
        } catch (Exception e) {
            //ignore
        }
        SVGGraphics2D svgGraphics2D = new SVGGraphics2D(document);

        Dimension size = getFullSize();
        svgGraphics2D.setSVGCanvasSize(size);

        paintComponentWithHeader(svgGraphics2D);

        // reset look and fell
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (UnsupportedLookAndFeelException e) {
            //ignore
        }

        exportImage(file, extension,  svgGraphics2D);

    }

    private static void exportImage(File exportFile, String extension, SVGGraphics2D svgGraphics2D) throws IOException {
        // write the svg file
        File svgFile = exportFile;

        if (!"svg".equals(extension)) {
            svgFile = new File(exportFile.getAbsolutePath() + ".temp");
        }

        try(OutputStream outputStream = Files.newOutputStream(svgFile.toPath())) {
            Writer out = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            svgGraphics2D.stream(out, true /* use css */);
            outputStream.flush();
        }

        if (!"svg".equals(extension)) {

            try (OutputStream outputStream = Files.newOutputStream(exportFile.toPath())) {
                TranscoderOutput output = new TranscoderOutput(outputStream);

                TranscoderInput svgInputFile = new TranscoderInput(svgFile.toURI().toString());

                switch (extension) {
                    case "jpg":
                        Transcoder transcoder = new JPEGTranscoder();
                        transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, 1.0F);
                        transcoder.transcode(svgInputFile, output);
                        break;
                    case "png":
                        Transcoder pngTranscoder = new PNGTranscoder();
                        pngTranscoder.addTranscodingHint(PNGTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, 0.084666f);
                        pngTranscoder.transcode(svgInputFile, output);
                        break;
                    case "tif":
                        TIFFTranscoder tiffTranscoder = new TIFFTranscoder();
                        tiffTranscoder.addTranscodingHint(TIFFTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, 0.084666f);
                        tiffTranscoder.addTranscodingHint(TIFFTranscoder.KEY_FORCE_TRANSPARENT_WHITE, true);
                        tiffTranscoder.transcode(svgInputFile, output);
                        break;
                    default:
                        break;
                }

                outputStream.flush();
            } catch (TranscoderException e) {
                throw new IOException(e);
            }

            // delete the temp svg file.
            if (svgFile.exists()) {
                svgFile.delete();
            }
        }
    }

    public void paintComponentWithHeader(Graphics2D graphics) {
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
            Graphics2D g2 = (Graphics2D) g;
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
            if (selectedScreenObject != null) {
                selectedScreenObject.setSelected(false);
                selectedScreenObject = null;
                repaint();
            }

            ScreenObject screenObject = _diagram.findScreenObjectByXY(e.getX(), e.getY());
            if (screenObject == null)
                return;
            setSelected(screenObject);
            if (isDoubleClick(e))
                _listener.selectedScreenObject(screenObject);
            else if (e.isPopupTrigger())
                _listener.displayMenuForScreenObject(screenObject, e.getX(), e.getY());
        }

        @Override
        public void mousePressed(MouseEvent e) {

            ScreenObject screenObject = _diagram.findScreenObjectByXY(e.getX(), e.getY());
            if (screenObject == null)
                return;
//            setSelected(screenObject);
            if (e.isPopupTrigger())
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

    private static class NullListener implements SequenceListener {
        public void selectedScreenObject(ScreenObject screenObject) {
        }

        public void displayMenuForScreenObject(ScreenObject screenObject, int x, int y) {
        }
    }
}
