package vanstudio.sequence.diagram.app.actions;

import vanstudio.sequence.diagram.Model;

import java.awt.event.ActionEvent;

public abstract class ModelAction extends SequenceAction {

    private Model _model = null;

    ModelAction(String resourcePrefix, Model model) {
        this(resourcePrefix, model, true);
    }

    ModelAction(String resourcePrefix, Model model, boolean withIcon) {
        super(resourcePrefix, withIcon);
        _model = model;
    }

    protected Model getModel() {
        return _model;
    }

    public void actionPerformed(ActionEvent e) {
        doIt();
    }

    public abstract boolean doIt();
}
