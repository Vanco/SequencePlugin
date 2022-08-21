package org.intellij.sequencer.generator;

import org.intellij.sequencer.config.ExcludeEntry;
import org.intellij.sequencer.config.SequenceParamsState;
import org.intellij.sequencer.config.SequenceSettingsState;
import org.intellij.sequencer.generator.filters.NoConstructorsFilter;
import org.intellij.sequencer.generator.filters.NoGetterSetterFilter;
import org.intellij.sequencer.generator.filters.NoPrivateMethodsFilter;
import org.intellij.sequencer.generator.filters.SingleClassFilter;
import org.intellij.sequencer.openapi.GeneratorFactory;
import org.intellij.sequencer.openapi.IGenerator;
import org.intellij.sequencer.openapi.SequenceParams;
import org.intellij.sequencer.openapi.filters.PackageFilter;
import org.intellij.sequencer.openapi.filters.ProjectOnlyFilter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JavaGeneratorFactory extends GeneratorFactory {
    @NotNull
    @Override
    public IGenerator getGenerator(@NotNull SequenceParams params) {
        return new SequenceGenerator(params);
    }

    @Override
    @NotNull
    public IGenerator getGenerator(@NotNull SequenceParams params, int offset) {
        return new SequenceGenerator(params, offset);
    }

    @Override
    @NotNull
    public SequenceParams loadParams(SequenceParams params) {
        SequenceParamsState state = SequenceParamsState.getInstance();

        params.setMaxDepth(state.callDepth);
//        params.setSmartInterface(state.smartInterface);
        params.getMethodFilter().addFilter(new ProjectOnlyFilter(state.projectClassesOnly));
        params.getMethodFilter().addFilter(new NoGetterSetterFilter(state.noGetterSetters));
        params.getMethodFilter().addFilter(new NoPrivateMethodsFilter(state.noPrivateMethods));
        params.getMethodFilter().addFilter(new NoConstructorsFilter(state.noConstructors));

        List<ExcludeEntry> excludeList = SequenceSettingsState.getInstance().getExcludeList();
        for (ExcludeEntry excludeEntry : excludeList) {
            if (!excludeEntry.isEnabled())
                continue;
            String excludeName = excludeEntry.getExcludeName();
            if (excludeName.endsWith(SequenceParams.PACKAGE_INDICATOR)) {
                int index = excludeName.lastIndexOf(SequenceParams.PACKAGE_INDICATOR);
                params.getMethodFilter().addFilter(new PackageFilter(excludeName.substring(0, index)));
            } else if (excludeName.endsWith(SequenceParams.RECURSIVE_PACKAGE_INDICATOR)) {
                int index = excludeName.lastIndexOf(SequenceParams.RECURSIVE_PACKAGE_INDICATOR);
                params.getMethodFilter().addFilter(new PackageFilter(excludeName.substring(0, index), true));
            } else
                params.getMethodFilter().addFilter(new SingleClassFilter(excludeName));
        }

        return params;
    }
}
