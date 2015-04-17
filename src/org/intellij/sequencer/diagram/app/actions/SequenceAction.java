package org.intellij.sequencer.diagram.app.actions;

import javax.swing.*;
import java.net.URL;
import java.util.ResourceBundle;

public abstract class SequenceAction extends AbstractAction {

    private ResourceBundle _bundle = null;
    private String _resourcePrefix = null;

    protected SequenceAction(String resourcePrefix) {
        this(resourcePrefix, true);
    }

    protected SequenceAction(String resourcePrefix, boolean withIcon) {

        this._resourcePrefix = resourcePrefix;
        _bundle = ResourceBundle.getBundle("org.intellij.sequencer.diagram.app.Sequence");

        putValue(NAME, getResource("name"));

        putValue(SHORT_DESCRIPTION, getResource("shortDesc"));

        if(withIcon)
            putValue(SMALL_ICON, getIcon("icon"));
    }

    protected String getResource(String key) {
        return _bundle.getString(_resourcePrefix + "." + key);
    }

    protected ImageIcon getIcon(String key) {
        URL iconURL = ClassLoader.getSystemResource(getResource(key));
        return new ImageIcon(iconURL, key);
    }
}
