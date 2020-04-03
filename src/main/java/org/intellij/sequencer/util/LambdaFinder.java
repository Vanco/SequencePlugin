package org.intellij.sequencer.util;

import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLambdaExpression;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 2020/3/22.
 */
public class LambdaFinder extends JavaElementVisitor {

    private final List<String> _argTypes;
    private final String _returnType;
    private PsiElement _psiElement;


    public LambdaFinder(List<String> _argTypes, String _returnType) {
        this._argTypes = _argTypes;
        this._returnType = _returnType;
    }

    @Override
    public void visitElement(@NotNull PsiElement element) {
        element.acceptChildren(this);
    }

    @Override
    public void visitLambdaExpression(PsiLambdaExpression expression) {
        super.visitLambdaExpression(expression);

        if (PsiUtil.isLambdaExpression(expression, _argTypes, _returnType)) {
            _psiElement = expression;
        }
    }

    public PsiElement getPsiElement() {
        return _psiElement;
    }
}
