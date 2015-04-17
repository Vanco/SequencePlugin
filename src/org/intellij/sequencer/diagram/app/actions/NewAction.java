package org.intellij.sequencer.diagram.app.actions;

import org.intellij.sequencer.diagram.Model;

public class NewAction extends ModifiedConfirmAction {

    public NewAction(Model model) {
        super("NewAction", model, true);
    }

    public boolean doIt() {
        getModel().loadNew();
        return true;
    }


}
