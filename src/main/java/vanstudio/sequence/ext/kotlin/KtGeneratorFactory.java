package vanstudio.sequence.ext.kotlin;

import vanstudio.sequence.config.ExcludeEntry;
import vanstudio.sequence.config.SequenceParamsState;
import vanstudio.sequence.config.SequenceSettingsState;
import vanstudio.sequence.ext.kotlin.filters.KtNoConstructorsFilter;
import vanstudio.sequence.ext.kotlin.filters.KtNoGetterSetterFilter;
import vanstudio.sequence.ext.kotlin.filters.KtNoPrivateMethodsFilter;
import vanstudio.sequence.ext.kotlin.filters.KtSingleClassFilter;
import vanstudio.sequence.openapi.GeneratorFactory;
import vanstudio.sequence.openapi.IGenerator;
import vanstudio.sequence.openapi.SequenceParams;
import vanstudio.sequence.openapi.filters.PackageFilter;
import vanstudio.sequence.openapi.filters.ProjectOnlyFilter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class KtGeneratorFactory extends GeneratorFactory {
    @NotNull
    @Override
    public IGenerator getGenerator(@NotNull SequenceParams params) {
        return new KtSequenceGenerator(params);
    }

    @Override
    @NotNull
    public IGenerator getGenerator(@NotNull SequenceParams params, int offset) {
        return new KtSequenceGenerator(params, offset);
    }

    @Override
    public void loadParams(@NotNull SequenceParams params) {
        SequenceParamsState state = SequenceParamsState.getInstance();

        params.setMaxDepth(state.callDepth);
//        params.setSmartInterface(state.smartInterface);
        params.getMethodFilter().addFilter(new ProjectOnlyFilter(state.projectClassesOnly));
        params.getMethodFilter().addFilter(new KtNoGetterSetterFilter(state.noGetterSetters));
        params.getMethodFilter().addFilter(new KtNoPrivateMethodsFilter(state.noPrivateMethods));
        params.getMethodFilter().addFilter(new KtNoConstructorsFilter(state.noConstructors));

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
                params.getMethodFilter().addFilter(new KtSingleClassFilter(excludeName));
        }

    }
}
