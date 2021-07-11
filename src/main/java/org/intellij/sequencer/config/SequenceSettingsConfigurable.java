package org.intellij.sequencer.config;

import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SequenceSettingsConfigurable implements SearchableConfigurable {

    private SequenceSettingsComponent _sequenceSettingsComponent;
    private SequenceSettingsState sequenceSettingsState;

    @Override
    public String getDisplayName() {
        return "Sequence Diagram";
    }

    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public JComponent createComponent() {
        sequenceSettingsState = SequenceSettingsState.getInstance();
        return getForm().getMainPanel();
    }

    @Override
    public boolean isModified() {
        return getForm().isModified(sequenceSettingsState);
    }

    @Override
    public void apply() {
        getForm().apply(sequenceSettingsState);
        fireConfigChanged();
    }

    @Override
    public void reset() {
        getForm().reset(sequenceSettingsState);
    }

    @Override
    public void disposeUIResources() {
        _sequenceSettingsComponent = null;
    }

    private void fireConfigChanged() {
        sequenceSettingsState.fireConfigChanged();
    }

    @NotNull
    private SequenceSettingsComponent getForm() {
        if (_sequenceSettingsComponent == null) {
            _sequenceSettingsComponent = new SequenceSettingsComponent();
        }
        return _sequenceSettingsComponent;
    }

    @NotNull
    @Override
    public String getId() {
        return "Settings.Sequence.Configuration";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String s) {
        return null;
    }

}
