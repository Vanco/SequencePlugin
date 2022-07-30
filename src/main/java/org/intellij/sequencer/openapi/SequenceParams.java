package org.intellij.sequencer.openapi;

import org.intellij.sequencer.config.SequenceSettingsState;
import org.intellij.sequencer.config.ExcludeEntry;
import org.intellij.sequencer.openapi.filters.CompositeElementFilter;
import org.intellij.sequencer.openapi.filters.ImplementationWhiteList;
import org.intellij.sequencer.openapi.filters.PackageFilter;
import org.intellij.sequencer.generator.filters.SingleClassFilter;

import java.util.List;

public class SequenceParams {
    private static final String PACKAGE_INDICATOR = ".*";
    private static final String RECURSIVE_PACKAGE_INDICATOR = ".**";

    private int _maxDepth = 3;
    private boolean _allowRecursion = false;
    @Deprecated(since = "2.2.0", forRemoval = true)
    private boolean smartInterface = false;
    private final CompositeElementFilter _methodFilter = new CompositeElementFilter();
    private final ImplementationWhiteList _implFilter = new ImplementationWhiteList();

    public SequenceParams() {
        List<ExcludeEntry> excludeList = SequenceSettingsState.getInstance().getExcludeList();
        for (ExcludeEntry excludeEntry : excludeList) {
            if (!excludeEntry.isEnabled())
                continue;
            String excludeName = excludeEntry.getExcludeName();
            if (excludeName.endsWith(PACKAGE_INDICATOR)) {
                int index = excludeName.lastIndexOf(PACKAGE_INDICATOR);
                _methodFilter.addFilter(new PackageFilter(excludeName.substring(0, index)));
            } else if (excludeName.endsWith(RECURSIVE_PACKAGE_INDICATOR)) {
                int index = excludeName.lastIndexOf(RECURSIVE_PACKAGE_INDICATOR);
                _methodFilter.addFilter(new PackageFilter(excludeName.substring(0, index), true));
            } else
                _methodFilter.addFilter(new SingleClassFilter(excludeName));
        }
    }

    public int getMaxDepth() {
        return _maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this._maxDepth = maxDepth;
    }

    public boolean isNotAllowRecursion() {
        return !_allowRecursion;
    }

    public void setAllowRecursion(boolean allowRecursion) {
        this._allowRecursion = allowRecursion;
    }

    @Deprecated(since = "2.2.0", forRemoval = true)
    public boolean isSmartInterface() {
        return smartInterface;
    }

    @Deprecated(since = "2.2.0", forRemoval = true)
    public void setSmartInterface(boolean smartInterface) {
        this.smartInterface = smartInterface;
    }

    public CompositeElementFilter getMethodFilter() {
        return _methodFilter;
    }

    public ImplementationWhiteList getImplementationWhiteList() {
        return _implFilter;
    }
}

