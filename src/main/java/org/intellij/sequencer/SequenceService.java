package org.intellij.sequencer;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.intellij.sequencer.diagram.ModelTextListener;
import org.intellij.sequencer.generator.SequenceParams;
import org.jetbrains.annotations.NotNull;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 2020/2/23.
 */
public interface SequenceService {
    String PLUGIN_NAME = "Sequence Diagram";

    static SequenceService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, SequenceService.class);
    }

    void showSequence(SequenceParams params, PsiElement psiElement);

}
