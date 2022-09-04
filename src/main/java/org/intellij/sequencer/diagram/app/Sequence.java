package org.intellij.sequencer.diagram.app;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBScrollPane;
import org.intellij.sequencer.diagram.Display;
import org.intellij.sequencer.diagram.Model;
import org.intellij.sequencer.diagram.PreviewFrame;
import org.intellij.sequencer.diagram.app.actions.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ResourceBundle;

public class Sequence extends JFrame implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getInstance(Sequence.class);

    private static Sequence sequence = null;

    private Model _model = null;
    private Editor _editor = null;
    private Display _disp = null;

    private ModelAction _newAction = null;
    private ModelAction _openAction = null;
    private ModelAction _saveAction = null;
    private ModelAction _saveAsAction = null;

    private ResourceBundle _bundle = null;

    public static Sequence getInstance() {
        return sequence;
    }

    private Sequence() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        _bundle = ResourceBundle.getBundle("org.intellij.sequencer.diagram.app.Sequence");

        setTitle((File)null);

        Model model = new Model();

        _newAction = new NewAction(model);
        _openAction = new OpenAction(model);
        _saveAction = new SaveAction(model);
        _saveAsAction = new SaveAsAction(model);

        _disp = new Display(model, null);
        final JScrollPane displayScrollPane = new JBScrollPane(_disp);
        displayScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        final JButton jButton = new JButton("B");
        jButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PreviewFrame frame = new PreviewFrame(displayScrollPane, _disp);
                frame.setVisible(true);
            }
        });
        displayScrollPane.setCorner(JScrollPane.LOWER_RIGHT_CORNER, jButton);
        split.setTopComponent(displayScrollPane);

        _editor = new Editor(model);
        final JScrollPane jScrollPane = new JBScrollPane(_editor,
                  JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                  JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane.setFocusable(true);

        jScrollPane.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                JScrollBar horizontalScrollBar = jScrollPane.getVerticalScrollBar();
                if(e.getKeyCode() == KeyEvent.VK_UP)
                    horizontalScrollBar.getModel().setValue(horizontalScrollBar.getValue() -
                            horizontalScrollBar.getUnitIncrement());
                else if(e.getKeyCode() == KeyEvent.VK_DOWN)
                    horizontalScrollBar.getModel().setValue(horizontalScrollBar.getValue() +
                            horizontalScrollBar.getUnitIncrement());
            }
        });

        split.setBottomComponent(jScrollPane);

        model.addModelTextListener(_editor);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu(_bundle.getString("Sequence.menu.file.label"));

        menuBar.add(fileMenu);

        fileMenu.add(new JMenuItem(_newAction));
        fileMenu.add(new JMenuItem(_openAction));
        fileMenu.add(new JMenuItem(_saveAction));
        fileMenu.add(new JMenuItem(_saveAsAction));
        fileMenu.add(new JMenuItem(new ExportAction(_disp)));
        fileMenu.add(new JSeparator());
        final ExitAction exitAction = new ExitAction(model);
        fileMenu.add(new JMenuItem(exitAction));

        JMenu editMenu =
              new JMenu(_bundle.getString("Sequence.menu.edit.label"));

        menuBar.add(editMenu);

        editMenu.add(new JMenuItem(_editor.getCutAction()));
        editMenu.add(new JMenuItem(_editor.getCopyAction()));
        editMenu.add(new JMenuItem(_editor.getPasteAction()));

        JMenu helpMenu =
              new JMenu(_bundle.getString("Sequence.menu.help.label"));

        menuBar.add(helpMenu);

        helpMenu.add(new JMenuItem(new ExampleAction(model)));

        getContentPane().add(split, BorderLayout.CENTER);

        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        int width = size.width / 2;
        int height = size.height / 2;

        pack();
        setSize(new Dimension(width, height));
        setLocation(width / 2, height / 2);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitAction.doIt();
            }
        });

        setVisible(true);

        split.setDividerLocation(0.75d);

        model.addPropertyChangeListener("file", this);
    }

    Model getModel() {
        return _model;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(!evt.getPropertyName().equals("file"))
            return;
        setTitle((File)evt.getNewValue());
    }

    public void setTitle(File f) {

        String name = (f == null) ? "" : f.getName();
        super.setTitle(_bundle.getString("Sequence.frame.title") + " " + name);
    }

    /**
     * This method should be called whenever an exception is
     * caught. It will display the exception to the user.
     *
     * @param e the exception
     */
    public void exception(Exception e) {
        LOGGER.error("exception", e);
    }

    public static void main(String[] s) {
        sequence = new Sequence();
    }
}
