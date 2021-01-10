package org.intellij.sequencer;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.util.ui.JBUI;
import org.intellij.sequencer.generator.SequenceParams;
import org.intellij.sequencer.generator.filters.NoConstructorsFilter;
import org.intellij.sequencer.generator.filters.NoGetterSetterFilter;
import org.intellij.sequencer.generator.filters.NoPrivateMethodsFilter;
import org.intellij.sequencer.generator.filters.ProjectOnlyFilter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

//import org.jetbrains.kotlin.idea.KotlinLanguage;
//import org.jetbrains.kotlin.psi.KtFunction;

/**
 * Show Sequence generate options dialog.
 */
public class ShowSequenceAction extends AnAction {
    private int _callDepth = 5;
    private boolean _projectClassesOnly = true;
    private boolean _noGetterSetters = true;
    private boolean _noPrivateMethods;
    private boolean _noConstructors;
    private boolean _smartInterface = false;

    public ShowSequenceAction() {
    }

    /**
     * Enable or disable the menu base on file type. Current only java file will enable the menu.
     * @param event event
     */
    public void update(@NotNull AnActionEvent event) {
        super.update(event);

        Presentation presentation = event.getPresentation();

        PsiElement psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
        presentation.setEnabled(isEnabled(psiElement));

    }

    private boolean isEnabled(PsiElement psiElement) {
        // only JAVA method will enable the generator.
        return psiElement != null
                && (psiElement.getLanguage().is(JavaLanguage.INSTANCE)
                && psiElement instanceof PsiMethod
                /*|| psiElement.getLanguage().is(KotlinLanguage.INSTANCE)
                && psiElement instanceof KtFunction*/);
    }

    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;

        SequenceService plugin = project.getService(SequenceService.class);

        OptionsDialogWrapper dialogWrapper = new OptionsDialogWrapper(project);
        dialogWrapper.show();
        if (dialogWrapper.isOK()) {
            _callDepth = dialogWrapper.getCallStackDepth();
            _projectClassesOnly = dialogWrapper.isProjectClassesOnly();
            _noGetterSetters = dialogWrapper.isNoGetterSetters();
            _noPrivateMethods = dialogWrapper.isNoPrivateMethods();
            _noConstructors = dialogWrapper.isNoConstructors();
            _smartInterface = dialogWrapper.isSmartInterface();

            SequenceParams params = new SequenceParams();
            params.setMaxDepth(dialogWrapper.getCallStackDepth());
            params.setSmartInterface(dialogWrapper.isSmartInterface());
            params.getMethodFilter().addFilter(new ProjectOnlyFilter(_projectClassesOnly));
            params.getMethodFilter().addFilter(new NoGetterSetterFilter(_noGetterSetters));
            params.getMethodFilter().addFilter(new NoPrivateMethodsFilter(_noPrivateMethods));
            params.getMethodFilter().addFilter(new NoConstructorsFilter(_noConstructors));
            if (plugin != null) {
                PsiElement psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
                plugin.showSequence(params, psiElement);
            }
        }
    }

    private class DialogPanel extends JPanel {
        private final JSpinner jSpinner;
        private final JCheckBox jCheckBoxPFO;
        private final JCheckBox jCheckBoxNGS;
        private final JCheckBox jCheckBoxNPM;
        private final JCheckBox jCheckBoxNC;
        private final JCheckBox jCheckBoxSI;

        public DialogPanel() {
            super(new GridBagLayout());
            setBorder(BorderFactory.createTitledBorder("Options"));
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = 0;
            gc.gridy = 0;
            gc.insets = JBUI.insets(5);
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
            gc.insets = JBUI.emptyInsets();
            jCheckBoxPFO = new JCheckBox("Display only project classes", _projectClassesOnly);
            add(jCheckBoxPFO, gc);

            gc.gridx = 0;
            gc.gridy = 2;
            gc.anchor = GridBagConstraints.WEST;
            gc.gridwidth = 2;
            gc.insets = JBUI.emptyInsets();
            jCheckBoxNGS = new JCheckBox("Skip getters/setters", _noGetterSetters);
            add(jCheckBoxNGS, gc);

            gc.gridx = 0;
            gc.gridy = 3;
            gc.anchor = GridBagConstraints.WEST;
            gc.gridwidth = 2;
            gc.insets = JBUI.emptyInsets();
            jCheckBoxSI = new JCheckBox("Smart interface (experimental)", _smartInterface);
            add(jCheckBoxSI, gc);

            gc.gridx = 2;
            gc.gridy = 1;
            gc.anchor = GridBagConstraints.WEST;
            gc.gridwidth = 2;
            gc.insets = JBUI.emptyInsets();
            jCheckBoxNPM = new JCheckBox("Skip private methods", _noPrivateMethods);
            add(jCheckBoxNPM, gc);

            gc.gridx = 2;
            gc.gridy = 2;
            gc.anchor = GridBagConstraints.WEST;
            gc.gridwidth = 2;
            gc.insets = JBUI.emptyInsets();
            jCheckBoxNC = new JCheckBox("Skip constructors", _noConstructors);
            add(jCheckBoxNC, gc);
        }
    }

    private class OptionsDialogWrapper extends DialogWrapper {
        private final DialogPanel dialogPanel = new DialogPanel();

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
            return (Integer) dialogPanel.jSpinner.getValue();
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

        public boolean isSmartInterface() {
            return dialogPanel.jCheckBoxSI.isSelected();
        }
    }

}
