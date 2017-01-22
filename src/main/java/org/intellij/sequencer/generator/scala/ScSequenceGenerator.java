package org.intellij.sequencer.generator.scala;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.intellij.sequencer.generator.CallStack;
import org.intellij.sequencer.generator.SequenceParams;
import org.jetbrains.plugins.scala.lang.psi.api.ScalaElementVisitor;
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunctionDefinition;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 08/12/2016.
 */
public class ScSequenceGenerator extends ScalaElementVisitor {
    private CallStack topStack;
    private CallStack currentStack;
    private int depth;
    private final SequenceParams params;

    public ScSequenceGenerator(SequenceParams params) {
        this.params = params;
    }

    @Override
    public void visitElement(PsiElement element) {
        element.acceptChildren(this);
        super.visitElement(element);
    }

    public CallStack generate(ScFunctionDefinition fun) {
        PsiClass containingClass = fun.getContainingClass();
        if (containingClass == null) {
            containingClass = (PsiClass) fun.getParent();
        }
        return topStack;
    }
}
