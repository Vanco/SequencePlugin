package vanstudio.sequence.generator.filters;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLambdaExpression;
import com.intellij.psi.PsiMethod;
import vanstudio.sequence.openapi.Constants;
import vanstudio.sequence.openapi.filters.MethodFilter;
import vanstudio.sequence.openapi.model.MethodDescription;
import vanstudio.sequence.util.MyPsiUtil;

import java.util.List;
import java.util.Objects;

/**
 * The method should be excluded.
 */
public class SingleMethodFilter implements MethodFilter {
    private final String _className;
    private final String _methodName;
    private final List<String> _argTypes;
    private final int _offset;

    public SingleMethodFilter(String className, String methodName, List<String> argTypes, int offset) {
        _className = className;
        _methodName = methodName;
        _argTypes = argTypes;
        _offset = offset;
    }

    public boolean allow(PsiElement psiElement) {
        if (psiElement instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) psiElement;

            PsiClass containingClass = psiMethod.getContainingClass();
            if (isSameClass(containingClass) && MyPsiUtil.isMethod(psiMethod, _methodName, _argTypes))
                return false;
        }
        return true;
    }

    public boolean allowLambda(MethodDescription method) {
        if (method.getClassDescription().getClassName().equals(_className) && method.getOffset() == _offset) {
            return false;
        }
        return true;
    }

    private boolean isSameClass(PsiClass containingClass) {
        return _className.equals(Constants.ANONYMOUS_CLASS_NAME) && containingClass.getQualifiedName() == null ||
              _className.equals(containingClass.getQualifiedName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SingleMethodFilter)) return false;
        SingleMethodFilter that = (SingleMethodFilter) o;
        return _className.equals(that._className) && _methodName.equals(that._methodName)
                && _argTypes.equals(that._argTypes) && _offset == that._offset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_className, _methodName, _argTypes, _offset);
    }
}
