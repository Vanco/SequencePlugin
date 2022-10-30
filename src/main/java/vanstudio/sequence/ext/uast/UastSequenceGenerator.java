package vanstudio.sequence.ext.uast;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.util.containers.Stack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.*;
import org.jetbrains.uast.visitor.AbstractUastVisitor;
import vanstudio.sequence.config.SequenceSettingsState;
import vanstudio.sequence.openapi.GeneratorFactory;
import vanstudio.sequence.openapi.IGenerator;
import vanstudio.sequence.openapi.SequenceParams;
import vanstudio.sequence.openapi.model.CallStack;
import vanstudio.sequence.openapi.model.MethodDescription;
import vanstudio.sequence.util.MyPsiUtil;
import vanstudio.sequence.util.MyUastUtilKt;

import java.util.ArrayList;
import java.util.List;

public class UastSequenceGenerator extends AbstractUastVisitor implements IGenerator {
    private static final Logger LOGGER = Logger.getInstance(UastSequenceGenerator.class);

    private final Stack<Integer> offsetStack = new Stack<>();

    private final ArrayList<String> imfCache = new ArrayList<>();
    private CallStack topStack;
    private CallStack currentStack;
    private final SequenceParams params;

    private final boolean SHOW_LAMBDA_CALL;

    public UastSequenceGenerator(SequenceParams params) {
        this.params = params;
        SHOW_LAMBDA_CALL = SequenceSettingsState.getInstance().SHOW_LAMBDA_CALL;
    }

    public UastSequenceGenerator(SequenceParams params, int offset) {
        this(params);
        offsetStack.push(offset);
    }

    @Override
    public CallStack generate(PsiElement psiElement, @Nullable CallStack parent) {
        if (parent != null) {
            topStack = parent;
            currentStack = topStack;
        }
        return generateMethod(psiElement);
    }

    @Override
    public CallStack generate(UElement node, CallStack parent) {
        if (parent != null) {
            topStack = parent;
            currentStack = topStack;
        }
        if (node instanceof ULambdaExpression)
            return generateLambda((ULambdaExpression) node);

        return topStack;
    }

    private CallStack generateLambda(ULambdaExpression node) {
        MethodDescription method = MyUastUtilKt.createMethod(node, MyPsiUtil.findNaviOffset(node.getSourcePsi()));
        makeMethodCallExceptCurrentStackIsRecursive(method);
        node.getBody().accept(this);
        return topStack;
    }

    private CallStack generateMethod(PsiElement psiElement) {
        UMethod uMethod = UastContextKt.toUElement(psiElement, UMethod.class);
        if (uMethod != null)
            uMethod.accept(this);
        return topStack;
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

    private void methodCall(PsiMethod psiMethod, int offset) {
        if (psiMethod == null) return;
        if (!params.getMethodFilter().allow(psiMethod)) return;

        if (currentStack.level() < params.getMaxDepth()) {
            CallStack oldStack = currentStack;
            LOGGER.debug("+ depth = " + currentStack.level() + " method = " + psiMethod.getName());
            offsetStack.push(offset);
            generateMethod(psiMethod);
            LOGGER.debug("- depth = " + currentStack.level() + " method = " + psiMethod.getName());
            currentStack = oldStack;
        } else {
            UMethod uMethod = UastContextKt.toUElement(psiMethod, UMethod.class);
            if (uMethod != null) currentStack.methodCall(MyUastUtilKt.createMethod(uMethod, offset));
        }
    }

    // -- visitor -- //
    @Override
    public boolean visitMethod(@NotNull UMethod node) {
        int offset = offsetStack.isEmpty() ? MyPsiUtil.findNaviOffset(node.getSourcePsi()) : offsetStack.pop();

        MethodDescription method = MyUastUtilKt.createMethod(node, offset);
        return makeMethodCallExceptCurrentStackIsRecursive(method);
//        return super.visitMethod(node);
    }

    @Override
    public boolean visitCallExpression(@NotNull UCallExpression node) {
        boolean isComplexCall = false;
        List<UExpression> valueArguments = node.getValueArguments();
        for (UExpression valueArgument : valueArguments) {
            if (valueArgument instanceof UQualifiedReferenceExpression
                    || valueArgument instanceof ULambdaExpression
                    || valueArgument instanceof UCallExpression) {
                // generate value argument before call expression
                valueArgument.accept(this);
                isComplexCall = true;
            }
        }
        PsiMethod method = node.resolve();
        methodCall(method, MyPsiUtil.findNaviOffset(node.getSourcePsi()));
        return isComplexCall;
    }

    @Override
    public boolean visitLambdaExpression(@NotNull ULambdaExpression node) {
        if (SHOW_LAMBDA_CALL) {
            // generate dummy () -> call, and it's body in separate generator
            GeneratorFactory.createGenerator(node.getLang(), params)
                    .generate(node, currentStack);
           //true:  do not need to generate lambda body in this generator.
            return true;
        }
        return super.visitLambdaExpression(node);
    }
}
