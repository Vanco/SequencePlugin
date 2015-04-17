package org.intellij.sequencer.diagram;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class PreviewFrame extends JDialog {
    private static final int INIT_WIDTH = 200;
    private static final int INIT_HEIGHT = 150;
    public PreviewFrame(JScrollPane scrollPane, Display display) throws HeadlessException {
        super(findFrame(scrollPane));
        setUndecorated(true);
        setResizable(true);
        getContentPane().add(new PreviewPanel(scrollPane, display));
        Point point = new Point(scrollPane.getWidth(), scrollPane.getHeight());
        SwingUtilities.convertPointToScreen(point, scrollPane);
        setBounds((int)point.getX() - INIT_WIDTH, (int)point.getY() - INIT_HEIGHT,
              INIT_WIDTH, INIT_HEIGHT);
        addFocusListener(new DisposeFocusListener());
    }

    private static Frame findFrame(JComponent jComponent) {
        Frame frame = (Frame)SwingUtilities.getAncestorOfClass(Frame.class, jComponent);
        if(frame == null)
            frame = JOptionPane.getRootFrame();
        return frame;
    }

    private class DisposeFocusListener implements FocusListener {
        public void focusGained(FocusEvent e) {

        }

        public void focusLost(FocusEvent e) {
            dispose();
        }
    }

}
