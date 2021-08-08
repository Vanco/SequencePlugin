package org.intellij.sequencer.impl;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.concurrency.NonUrgentExecutor;
import org.intellij.sequencer.SequenceNavigable;
import org.intellij.sequencer.generator.filters.CompositeMethodFilter;
import org.intellij.sequencer.generator.filters.MethodFilter;
import org.intellij.sequencer.util.MyPsiUtil;
import org.jetbrains.kotlin.psi.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KtSequenceNavigable extends JavaSequenceNavigable implements SequenceNavigable {

    public KtSequenceNavigable(Project project) {
        super(project);
    }

    @Override
    public void openClassInEditor(String className) {
        super.openClassInEditor(className);
    }

    @Override
    public void openMethodInEditor(String className, String methodName, List<String> argTypes) {
        if (MyPsiUtil.isKtFileName(className)) {
            ReadAction
                    .nonBlocking(() -> {
                        final String filename = className.replace("_", ".");
                        final PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, filename, GlobalSearchScope.allScope(project));
                        for (PsiFile psiFile : psiFiles) {
                            final KtNamedFunction[] functions = ((KtFile) psiFile).findChildrenByClass(KtNamedFunction.class);
                            for (KtNamedFunction function : functions) {
                                if (methodName.equals(function.getName()) && MyPsiUtil.isParameterEquals(argTypes, function.getValueParameters())) {
                                    return new Pair<>(psiFile.getVirtualFile(), function.getTextOffset());
                                }
                            }
                        }

                        return null;

                    })
                    .finishOnUiThread(ModalityState.defaultModalityState(), p -> {
                        if (p != null)
                            openInEditor(p.first, p.second);
                    })
                    .inSmartMode(project)
                    .submit(NonUrgentExecutor.getInstance());

        } else {
            super.openMethodInEditor(className, methodName, argTypes);
        }
    }

    @Override
    public boolean isInsideAMethod() {
        return false;
    }

    @Override
    public void openMethodCallInEditor(MethodFilter filter, String fromClass, String fromMethod, List<String> fromArgTypes, String toClass, String toMethod, List<String> toArgType, int offset) {
        if (MyPsiUtil.isKtFileName(fromClass)) {
            ReadAction
                    .nonBlocking(() -> {
                        final String filename = fromClass.replace("_", ".");
                        final PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, filename, GlobalSearchScope.allScope(project));

                        for (PsiFile psiFile : psiFiles) {
                            final KtNamedFunction[] functions = ((KtFile) psiFile).findChildrenByClass(KtNamedFunction.class);
                            for (KtNamedFunction function : functions) {
                                if (fromMethod.equals(function.getName()) && MyPsiUtil.isParameterEquals(fromArgTypes, function.getValueParameters())) {
                                    return psiFile.getVirtualFile();
                                }
                            }
                        }

                        return null;
                    })
                    .finishOnUiThread(ModalityState.defaultModalityState(), containingClass -> openInEditor(containingClass, offset))
                    .inSmartMode(project)
                    .submit(NonUrgentExecutor.getInstance());

        } else {
            super.openMethodCallInEditor(filter, fromClass, fromMethod, fromArgTypes, toClass, toMethod, toArgType, offset);
        }

    }

    @Override
    public List<String> findImplementations(String className) {
        return null;
    }

    @Override
    public List<String> findImplementations(String className, String methodName, List<String> argTypes) {
        return null;
    }

    @Override
    public void openLambdaExprInEditor(String fromClass, String fromMethod, List<String> fromArgTypes, List<String> argTypes, String returnType, int offset) {
        super.openLambdaExprInEditor(fromClass, fromMethod, fromArgTypes, argTypes, returnType, offset);
    }

    @Override
    public void openMethodCallInsideLambdaExprInEditor(CompositeMethodFilter methodFilter, String fromClass, String enclosedMethodName, List<String> enclosedMethodArgTypes, List<String> argTypes, String returnType, String toClass, String toMethod, List<String> toArgTypes, int offset) {
        super.openMethodCallInsideLambdaExprInEditor(methodFilter, fromClass, enclosedMethodName, enclosedMethodArgTypes, argTypes, returnType, toClass, toMethod, toArgTypes, offset);
    }

}
