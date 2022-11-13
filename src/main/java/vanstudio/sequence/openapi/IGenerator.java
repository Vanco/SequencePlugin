package vanstudio.sequence.openapi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;
import vanstudio.sequence.openapi.model.CallStack;

import java.util.List;

public interface IGenerator {
    /**
     * Generate <code>CallStack</code> based on <code>PsiElement</code>.
     * @param psiElement instanceof PsiMethod, KtFunction
     * @param parent current CallStack or null if called from UI
     * @return <code>CallStack</code> includes method call of PsiMethod/KtFunction and calls in its body.
     */
    CallStack generate(PsiElement psiElement, @Nullable CallStack parent);

    class ParamPair {
        public final List<String> argNames;
        public final List<String> argTypes;

        public ParamPair(List<String> argNames, List<String> argTypes) {
            this.argNames = argNames;
            this.argTypes = argTypes;
        }
    }
}
