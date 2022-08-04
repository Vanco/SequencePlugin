package org.intellij.sequencer.diagram.app.actions;

import org.intellij.sequencer.diagram.Display;
import org.intellij.sequencer.diagram.app.Sequence;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class ExportAction extends SequenceAction {

    private final Display _display;

    public ExportAction(Display display) {
        super("ExportAction", true);
        _display = display;
    }

    public void actionPerformed(ActionEvent e) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setDialogTitle(getResource("dialogTitle"));

        int returnVal = chooser.showOpenDialog(Sequence.getInstance());
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            export(chooser.getSelectedFile());
        }
    }

    private void export(File file) {
        try {
            _display.saveImageToSvgFile(file, "svg");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
