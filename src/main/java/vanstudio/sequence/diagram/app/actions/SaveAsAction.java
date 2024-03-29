package vanstudio.sequence.diagram.app.actions;

import vanstudio.sequence.diagram.Model;
import vanstudio.sequence.diagram.app.Sequence;

import javax.swing.*;
import java.io.File;

public class SaveAsAction extends ModifiedEnabledAction {

    public SaveAsAction(Model model) {
        super("SaveAsAction", model);
    }

    public boolean doIt() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setDialogTitle(getResource("dialogTitle"));

        int returnVal = chooser.showSaveDialog(Sequence.getInstance());
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            return getModel().writeToFile(file);
        } else {
            return false;
        }
    }
}
