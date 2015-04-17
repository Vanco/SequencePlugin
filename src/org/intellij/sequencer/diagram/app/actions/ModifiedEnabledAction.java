package org.intellij.sequencer.diagram.app.actions;

import org.apache.log4j.Logger;
import org.intellij.sequencer.diagram.Model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public abstract class ModifiedEnabledAction extends ModelAction implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(ModifiedEnabledAction.class);

    ModifiedEnabledAction(String resourcePrefix, Model model) {
        super(resourcePrefix, model);
        setEnabled(false);
        model.addPropertyChangeListener("modified", this);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(!evt.getPropertyName().equals("modified"))
            return;
        Boolean modified = (Boolean)evt.getNewValue();
        if(LOGGER.isDebugEnabled())
            LOGGER.debug("propertyChange(...) modified is " +
                  modified);
        setEnabled(modified.booleanValue());
    }
}
