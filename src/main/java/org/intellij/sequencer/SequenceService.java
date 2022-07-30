package org.intellij.sequencer;

import com.intellij.psi.PsiElement;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 2020/2/23.
 */
public interface SequenceService {
    String PLUGIN_ID = "SequenceDiagram";
    String PLUGIN_NAME = "Sequence Diagram";

    void showSequence(PsiElement psiElement);

}
