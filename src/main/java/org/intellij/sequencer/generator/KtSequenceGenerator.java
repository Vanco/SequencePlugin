package org.intellij.sequencer.generator;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiCallExpression;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.Stack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KtSequenceGenerator extends KtVisitorVoid implements IGenerator {
    private final Stack<PsiCallExpression> exprStack = new Stack<>();
    private final Stack<CallStack> callStack = new Stack<>();
    private static final Logger LOGGER = Logger.getInstance(KtSequenceGenerator.class.getName());

    private CallStack topStack;
    private CallStack currentStack;
    private int depth;
    private final SequenceParams params;

    public KtSequenceGenerator(SequenceParams params) {
        this.params = params;
    }

    @Override
    public CallStack generate(PsiElement psiElement) {
        return generate((KtFunction) psiElement);
    }

    public CallStack generate(KtFunction ktFunction) {
        ktFunction.accept(this);
        return topStack;
    }

    @Override
    public void visitKtElement(@NotNull KtElement element) {
        super.visitKtElement(element);

        element.acceptChildren(this);
    }

    @Override
    public void visitNamedFunction(@NotNull KtNamedFunction function) {
        MethodDescription method = createMethod(function);
        if (makeMethodCallExceptCurrentStackIsRecursive(method)) return;
        super.visitNamedFunction(function);
    }

    @Override
    public void visitCallExpression(@NotNull KtCallExpression expression) {
        PsiElement psiElement = expression.getReference().resolve();
//        KtNameReferenceExpression nameReferenceExpression = (KtNameReferenceExpression) expression.getCalleeExpression();
//        PsiElement psiElement = nameReferenceExpression.getReferencedNameElement();
        methodCall(psiElement);
        super.visitCallExpression(expression);
    }

    private void methodCall(PsiElement psiElement) {
        if (psiElement == null) return;
       // if (!params.getMethodFilter().allow(psiMethod)) return;

        if (depth < params.getMaxDepth() - 1) {
            CallStack oldStack = currentStack;
            depth++;
            LOGGER.debug("+ depth = " + depth + " method = " + psiElement.getText());
            generate(psiElement);
            depth--;
            LOGGER.debug("- depth = " + depth + " method = " + psiElement.getText());
            currentStack = oldStack;
        } else {
            currentStack.methodCall(createMethod((KtNamedFunction) psiElement));
        }
    }

    private MethodDescription createMethod(KtNamedFunction function) {
        ParamPair paramPair = extractParameters(function.getValueParameters());
        ClassDescription classDescription;
        if (function.isTopLevel()) {
            classDescription = ClassDescription.TOP_LEVEL_FUN;
        } else if (function.isLocal()) {
            classDescription = ClassDescription.ANONYMOUS_CLASS;
        } else {
            if (function.getFqName() != null) {
                String className = function.getFqName().parent().asString();
                classDescription = new ClassDescription(className, new ArrayList<>());
            } else {
                classDescription = ClassDescription.ANONYMOUS_CLASS;
            }
        }
        List<String> attributes = createAttributes(function.getModifierList());
        String returnType = "Unit";
        if (function.hasDeclaredReturnType()) {
            returnType = getType(function.getTypeReference());
        }

        return MethodDescription.createMethodDescription(
                classDescription,
                attributes, function.getName(), returnType,
                paramPair.argNames, paramPair.argTypes
        );
    }

    private List<String> createAttributes(KtModifierList modifierList) {

        return new ArrayList<>();
    }

    private ParamPair extractParameters(List<KtParameter> parameters) {
        List<String> argNames = new ArrayList<>();
        List<String> argTypes = new ArrayList<>();
        for (KtParameter parameter : parameters) {
            argNames.add(parameter.getName());
            argTypes.add(getType(parameter.getTypeReference()));
        }
        return new ParamPair(argNames, argTypes);
    }

    /**
     * Get Type reference string.
     *
     * @param typeReference KtTypeReference
     * @return String
     */
    private String getType(KtTypeReference typeReference) {
        KtUserType type = (KtUserType) Objects.requireNonNull(typeReference).getTypeElement();
        return Objects.requireNonNull(type).getReferencedName();
    }

    private boolean makeMethodCallExceptCurrentStackIsRecursive(MethodDescription method) {
        if (topStack == null) {
            topStack = new CallStack(method);
            currentStack = topStack;
        } else {
            if (params.isNotAllowRecursion() && currentStack.isRecursive(method))
                return true;
            currentStack = currentStack.methodCall(method);
        }
        return false;
    }

    private static class ParamPair {
        final List<String> argNames;
        final List<String> argTypes;

        public ParamPair(List<String> argNames, List<String> argTypes) {
            this.argNames = argNames;
            this.argTypes = argTypes;
        }
    }
}
