package org.intellij.sequencer.diagram.app;

import com.intellij.openapi.diagnostic.Logger;
import org.intellij.sequencer.diagram.Model;
import org.intellij.sequencer.diagram.ModelTextEvent;
import org.intellij.sequencer.diagram.ModelTextListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class Editor extends JPanel implements DocumentListener, ModelTextListener {

    private static final Logger LOGGER = Logger.getInstance(Editor.class);

    private Model _model = null;
    private final JEditorPane _editPane = new JEditorPane();

    private Action _cutAction = null;
    private Action _copyAction = null;
    private Action _pasteAction = null;

    private ResourceBundle _bundle = null;

    private boolean _ignoreChange = false;
    private boolean _documentChanged = false;
    private long _lastChangeTime = 0;

    public Editor(Model model) {
        _model = model;
        setLayout(new BorderLayout());
        _editPane.setFont(new Font("Monospaced",
              Font.PLAIN,
              _editPane.getFont().getSize() + 1));
        add(_editPane, BorderLayout.CENTER);

        _bundle = ResourceBundle.getBundle("org.intellij.sequencer.diagram.app.Sequence");

        _editPane.setText(model.getText());
        _editPane.getDocument().addDocumentListener(this);
        new Thread(new ChangeNotifier()).start();
    }

    public synchronized Action getCutAction() {
        if(_cutAction == null) {
            _cutAction = new DefaultEditorKit.CutAction();
            initAction(_cutAction, "CutAction");
        }
        return _cutAction;
    }

    public synchronized Action getPasteAction() {
        if(_pasteAction == null) {
            _pasteAction = new DefaultEditorKit.PasteAction();
            initAction(_pasteAction, "PasteAction");
        }
        return _pasteAction;
    }

    public synchronized Action getCopyAction() {
        if(_copyAction == null) {
            _copyAction = new DefaultEditorKit.CopyAction();
            initAction(_copyAction, "CopyAction");
        }
        return _copyAction;
    }

    private void initAction(Action act, String resourcePrefix) {
        act.putValue(Action.NAME,
              getResource(resourcePrefix, "name"));
        act.putValue(Action.SHORT_DESCRIPTION,
              getResource(resourcePrefix, "shortDesc"));
        act.putValue(Action.SMALL_ICON,
              getIcon(resourcePrefix, "icon"));
    }

    String getResource(String resourcePrefix, String key) {
        return _bundle.getString(resourcePrefix + "." + key);
    }

    ImageIcon getIcon(String resourcePrefix, String key) {
        URL iconURL =
              ClassLoader.getSystemResource(getResource(resourcePrefix, key));
        return new ImageIcon(iconURL, key);
    }

    public void modelTextChanged(ModelTextEvent mte) {
        if(mte.getSource() == this)
            return;

        if(LOGGER.isDebugEnabled())
            LOGGER.debug("modelTextChanged(...) changing");

        Document doc = _editPane.getDocument();
        try {
            _ignoreChange = true;
            doc.remove(0, doc.getLength());
            doc.insertString(0, mte.getText(), null);
        } catch(BadLocationException ble) {
            Sequence.getInstance().exception(ble);
        } finally {
            _ignoreChange = false;
        }
    }

    private void documentChanged(DocumentEvent e) {
        if(LOGGER.isDebugEnabled())
            LOGGER.debug("documentChanged(...) ignoreChange " + _ignoreChange);
        if(_ignoreChange)
            return;
        _documentChanged = true;
        _lastChangeTime = System.currentTimeMillis();
    }

    public void changedUpdate(DocumentEvent e) {
        documentChanged(e);
    }

    public void insertUpdate(DocumentEvent e) {
        documentChanged(e);
    }

    public void removeUpdate(DocumentEvent e) {
        documentChanged(e);
    }

    private class ChangeNotifier
          implements Runnable {

        public void run() {
            while(true) {
                synchronized(Editor.this) {
                    if(_documentChanged) {
                        if((System.currentTimeMillis() - _lastChangeTime) > 500) {
                            try {
                                Document doc = _editPane.getDocument();
                                String s = doc.getText(0, doc.getLength());
                                _model.setText(s, Editor.this);
                                _documentChanged = false;
                            } catch(BadLocationException ble) {
                                Sequence.getInstance().exception(ble);
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(250);
                } catch(InterruptedException ie) {
                }
            }
        }
    }
}
