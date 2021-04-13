package org.intellij.sequencer.config;

import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConfigurationOptions implements SearchableConfigurable {

    private ConfigurationUI _configurationUI;
    private Configuration configuration;

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
        configuration = Configuration.getInstance();
        return getForm().getMainPanel();
    }

    @Override
    public boolean isModified() {
        return getForm().isModified(configuration);
    }

    @Override
    public void apply() {
        getForm().apply(configuration);
        fireConfigChanged();
    }

    @Override
    public void reset() {
        getForm().reset(configuration);
    }

    @Override
    public void disposeUIResources() {

    }

    private void fireConfigChanged() {
        configuration.fireConfigChanged();
    }

    @NotNull
    private ConfigurationUI getForm() {
        if (_configurationUI == null) {
            _configurationUI = new ConfigurationUI();
        }
        return _configurationUI;
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
