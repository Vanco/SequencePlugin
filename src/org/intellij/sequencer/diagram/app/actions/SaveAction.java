package org.intellij.sequencer.diagram.app.actions;

import org.intellij.sequencer.diagram.Model;

public class SaveAction extends ModifiedEnabledAction {

    public SaveAction(Model model) {
        super("SaveAction", model);
    }

    public boolean doIt() {
        if(getModel().getFile() != null) {
            return getModel().writeToFile(getModel().getFile());
        } else {
            // todo
            return false; //getModel().getSaveAsAction().doIt();
        }
    }
}
