package com.zenuml.dsl;

import com.intellij.psi.PsiMethod;
import com.siyeh.ig.psiutils.MethodUtils;
import org.intellij.sequencer.util.PsiUtil;

public class DslHepler {
    public static FunctionNode fromPsiMethod(PsiMethod psiMethod) {
        String functionName = psiMethod.getName();
        String result=psiMethod.getReturnType().toString();
        return null;
    }
}
