package vanstudio.sequence;

import com.intellij.psi.PsiElement;
import org.jetbrains.uast.UMethod;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 2020/2/23.
 */
public interface SequenceService {
    String PLUGIN_ID = "SequenceDiagram";
    String PLUGIN_NAME = "Sequence Diagram";

    void showSequence(PsiElement psiElement);

    void showSequence(UMethod method);

}
