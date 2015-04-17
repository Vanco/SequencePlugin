package org.intellij.sequencer.config;

public class ExcludeEntry {
    private String _excludeName;
    private boolean _isEnabled;

    public ExcludeEntry(String excludeName, boolean enabled) {
        _excludeName = excludeName;
        _isEnabled = enabled;
    }

    public String getExcludeName() {
        return _excludeName;
    }

    public void setExcludeName(String excludeName) {
        _excludeName = excludeName;
    }

    public boolean isEnabled() {
        return _isEnabled;
    }

    public void setEnabled(boolean enabled) {
        _isEnabled = enabled;
    }
}
