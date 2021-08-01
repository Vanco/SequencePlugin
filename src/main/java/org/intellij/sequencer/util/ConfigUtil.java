package org.intellij.sequencer.util;

import org.intellij.sequencer.config.SequenceParamsState;
import org.intellij.sequencer.generator.SequenceParams;
import org.intellij.sequencer.generator.filters.NoConstructorsFilter;
import org.intellij.sequencer.generator.filters.NoGetterSetterFilter;
import org.intellij.sequencer.generator.filters.NoPrivateMethodsFilter;
import org.intellij.sequencer.generator.filters.ProjectOnlyFilter;
import org.jetbrains.annotations.NotNull;

public class ConfigUtil {

    private ConfigUtil(){}

    @NotNull
    public static SequenceParams loadSequenceParams() {
        SequenceParamsState state = SequenceParamsState.getInstance();

        SequenceParams params = new SequenceParams();
        params.setMaxDepth(state.callDepth);
        params.setSmartInterface(state.smartInterface);
        params.getMethodFilter().addFilter(new ProjectOnlyFilter(state.projectClassesOnly));
        params.getMethodFilter().addFilter(new NoGetterSetterFilter(state.noGetterSetters));
        params.getMethodFilter().addFilter(new NoPrivateMethodsFilter(state.noPrivateMethods));
        params.getMethodFilter().addFilter(new NoConstructorsFilter(state.noConstructors));
        return params;
    }
}
