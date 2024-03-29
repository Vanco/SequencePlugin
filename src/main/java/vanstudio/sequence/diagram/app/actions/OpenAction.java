package vanstudio.sequence.diagram.app.actions;

import vanstudio.sequence.diagram.Model;
import vanstudio.sequence.diagram.app.Sequence;

import javax.swing.*;
import java.io.File;

public class OpenAction extends ModifiedConfirmAction {

    public OpenAction(Model model) {
        super("OpenAction", model, true);
    }

    public boolean doIt() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setDialogTitle(getResource("dialogTitle"));

        int returnVal = chooser.showOpenDialog(Sequence.getInstance());
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            return getModel().readFromFile(file);
        } else {
            return false;
        }
    }


}
