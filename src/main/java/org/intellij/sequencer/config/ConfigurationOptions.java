package org.intellij.sequencer.config;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConfigurationOptions implements SearchableConfigurable {

    private ConfigurationUI _configurationUI;
    private Configuration configuration;


    public ConfigurationOptions(@NotNull Configuration configuration) {
        this.configuration = configuration;
    }

    public String getDisplayName() {
        return "Sequence Diagram";
    }

    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        return getForm().getMainPanel();
    }

    public boolean isModified() {
        return getForm().isModified(configuration);
    }

    public void apply() throws ConfigurationException {
        getForm().apply(configuration);
        fireConfigChanged();
    }

    public void reset() {
        getForm().reset(configuration);
    }

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
