package org.intellij.sequencer;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.intellij.sequencer.generator.SequenceParams;
import org.intellij.sequencer.generator.filters.MethodFilter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 2020/2/23.
 */
public interface SequenceService {
    static SequenceService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, SequenceService.class);
    }

    abstract void showSequence(SequenceParams params);

    void openClassInEditor(String className);

    void openMethodInEditor(String className, String methodName, List argTypes);

    boolean isInsideAMethod();

    void openMethodCallInEditor(MethodFilter filter, String fromClass, String fromMethod, List fromArgTypes,
                                String toClass, String toMethod, List toArgType, int callNo);

    List<String> findImplementations(String className);

    List<String> findImplementations(String className, String methodName, List argTypes);
}
