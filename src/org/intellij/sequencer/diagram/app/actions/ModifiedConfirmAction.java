package org.intellij.sequencer.diagram.app.actions;

import org.intellij.sequencer.diagram.Model;
import org.intellij.sequencer.diagram.app.Sequence;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class ModifiedConfirmAction extends ModelAction {

    protected ModifiedConfirmAction(String resourcePrefix, Model model, boolean withIcon) {
        super(resourcePrefix, model, withIcon);
    }

    public void actionPerformed(ActionEvent e) {
        if(confirmed())
            doIt();
    }

    boolean confirmed() {
        if(getModel().isModified()) {
            int ret = JOptionPane.showConfirmDialog(Sequence.getInstance(),
                  getResource("confirmMessage"),
                  getResource("confirmTitle"),
                  JOptionPane.YES_NO_CANCEL_OPTION);
            if(ret == JOptionPane.CANCEL_OPTION)
                return false;
            if(ret == JOptionPane.YES_OPTION)
            // todo
//                if (!getModel().getSaveAction().doIt())
                return false;
        }
        return true;
    }
}
