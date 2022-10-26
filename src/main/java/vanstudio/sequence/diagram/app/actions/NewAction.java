package vanstudio.sequence.diagram.app.actions;

import vanstudio.sequence.diagram.Model;

public class NewAction extends ModifiedConfirmAction {

    public NewAction(Model model) {
        super("NewAction", model, true);
    }

    public boolean doIt() {
        getModel().loadNew();
        return true;
    }


}
