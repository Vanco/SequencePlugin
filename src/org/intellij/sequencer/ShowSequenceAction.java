package org.intellij.sequencer;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.intellij.sequencer.generator.SequenceParams;
import org.intellij.sequencer.generator.filters.NoConstructorsFilter;
import org.intellij.sequencer.generator.filters.NoGetterSetterFilter;
import org.intellij.sequencer.generator.filters.NoPrivateMethodsFilter;
import org.intellij.sequencer.generator.filters.ProjectOnlyFilter;

import javax.swing.*;
import java.awt.*;

public class ShowSequenceAction extends AnAction {
    private int _callDepth = 3;
    private boolean _projectClassesOnly = true;
    private boolean _noGetterSetters = true;
    private boolean _noPrivateMethods;
    private boolean _noConstructors;

    public ShowSequenceAction() {
    }

    public void update(AnActionEvent event) {
        super.update(event);

        Presentation presentation = event.getPresentation();
        SequencePlugin plugin = getPlugin(event);
        presentation.setEnabled(plugin != null && plugin.isInsideAMethod());
    }

    public void actionPerformed(AnActionEvent event) {
        SequencePlugin plugin = getPlugin(event);
        OptionsDialogWrapper dialogWrapper = new OptionsDialogWrapper(getProject(event));
        dialogWrapper.show();
        if(dialogWrapper.isOK()) {
            _callDepth = dialogWrapper.getCallStackDepth();
            _projectClassesOnly = dialogWrapper.isProjectClassesOnly();
            _noGetterSetters = dialogWrapper.isNoGetterSetters();
            _noPrivateMethods = dialogWrapper.isNoPrivateMethods();
            _noConstructors = dialogWrapper.isNoConstructors();

            SequenceParams params = new SequenceParams();
            params.setMaxDepth(dialogWrapper.getCallStackDepth());
            params.getMethodFilter().addFilter(new ProjectOnlyFilter(_projectClassesOnly));
            params.getMethodFilter().addFilter(new NoGetterSetterFilter(_noGetterSetters));
            params.getMethodFilter().addFilter(new NoPrivateMethodsFilter(_noPrivateMethods));
            params.getMethodFilter().addFilter(new NoConstructorsFilter(_noConstructors));
            plugin.showSequence(params);
        }
    }

    private SequencePlugin getPlugin(AnActionEvent event) {
        Project project = getProject(event);
        if(project == null)
            return null;
        return getPlugin(project);
    }

    private SequencePlugin getPlugin(Project project) {
        return SequencePlugin.getInstance(project);
    }

    private Project getProject(AnActionEvent event) {
        return (Project)event.getDataContext().getData(DataConstants.PROJECT);
    }

    private class DialogPanel extends JPanel {
        private JSpinner jSpinner;
        private JCheckBox jCheckBoxPFO;
        private JCheckBox jCheckBoxNGS;
        private JCheckBox jCheckBoxNPM;
        private JCheckBox jCheckBoxNC;

        public DialogPanel() {
            super(new GridBagLayout());
            setBorder(BorderFactory.createTitledBorder("Options"));
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = 0;
            gc.gridy = 0;
            gc.insets = new Insets(5, 5, 5, 5);
            gc.anchor = GridBagConstraints.WEST;
            JLabel jLabel = new JLabel("Call depth:");
            add(jLabel, gc);

            gc.gridx = 1;
            gc.anchor = GridBagConstraints.CENTER;
            jSpinner = new JSpinner(new SpinnerNumberModel(_callDepth, 1, 10, 1));
            jLabel.setLabelFor(jSpinner);
            add(jSpinner, gc);

            gc.gridx = 0;
            gc.gridy = 1;
            gc.anchor = GridBagConstraints.WEST;
            gc.gridwidth = 2;
            gc.insets = new Insets(0, 0, 0, 0);
            jCheckBoxPFO = new JCheckBox("Display only project classes", _projectClassesOnly);
            add(jCheckBoxPFO, gc);

            gc.gridx = 0;
            gc.gridy = 2;
            gc.anchor = GridBagConstraints.WEST;
            gc.gridwidth = 2;
            gc.insets = new Insets(0, 0, 0, 0);
            jCheckBoxNGS = new JCheckBox("Skip getters/setters", _noGetterSetters);
            add(jCheckBoxNGS, gc);

            gc.gridx = 2;
            gc.gridy = 1;
            gc.anchor = GridBagConstraints.WEST;
            gc.gridwidth = 2;
            gc.insets = new Insets(0, 0, 0, 0);
            jCheckBoxNPM = new JCheckBox("Skip private methods", _noPrivateMethods);
            add(jCheckBoxNPM, gc);

            gc.gridx = 2;
            gc.gridy = 2;
            gc.anchor = GridBagConstraints.WEST;
            gc.gridwidth = 2;
            gc.insets = new Insets(0, 0, 0, 0);
            jCheckBoxNC = new JCheckBox("Skip constructors", _noConstructors);
            add(jCheckBoxNC, gc);
        }
    }

    private class OptionsDialogWrapper extends DialogWrapper {
        private DialogPanel dialogPanel = new DialogPanel();

        public OptionsDialogWrapper(Project project) {
            super(project, false);
            setResizable(false);
            setTitle("Sequence Diagram Options");
            init();
        }

        protected JComponent createCenterPanel() {
            return dialogPanel;
        }

        public JComponent getPreferredFocusedComponent() {
            return dialogPanel.jSpinner;
        }

        public int getCallStackDepth() {
            return ((Integer)dialogPanel.jSpinner.getValue()).intValue();
        }

        public boolean isProjectClassesOnly() {
            return dialogPanel.jCheckBoxPFO.isSelected();
        }

        public boolean isNoGetterSetters() {
            return dialogPanel.jCheckBoxNGS.isSelected();
        }

        public boolean isNoPrivateMethods() {
            return dialogPanel.jCheckBoxNPM.isSelected();
        }

        public boolean isNoConstructors() {
            return dialogPanel.jCheckBoxNC.isSelected();
        }
    }

}
