package org.intellij.sequencer.ext.kotlin;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.concurrency.NonUrgentExecutor;
import org.intellij.sequencer.openapi.SequenceNavigable;
import org.intellij.sequencer.generator.JavaSequenceNavigable;
import org.intellij.sequencer.util.MyPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        if (MyPsiUtil.isKtFileName(className) || MyPsiUtil.isKtObjectLiteral(className)) {
            ReadAction
                    .nonBlocking(() -> {
                        final int idx = className.lastIndexOf(".");
                        final int odx = className.indexOf("#");
                        final String objLiteralType = odx == -1 ? "": className.substring(odx + 1);
                        final String className1 = odx == -1 ? className : className.substring(0, odx);

                        final String pathSegment = className1.replace(".", "/").replace("_kt", ".kt");
                        final String filename = className1.substring(idx + 1).replace("_", ".");

                        final PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, filename, GlobalSearchScope.allScope(project));

                        for (PsiFile psiFile : psiFiles) {
                            final String path = psiFile.getVirtualFile().getPath();
                            if (!path.endsWith(pathSegment)) continue;

                            if (odx == -1) {
                                final @NotNull Collection<KtNamedFunction> functions = PsiTreeUtil.findChildrenOfAnyType(psiFile, KtNamedFunction.class);
                                for (KtNamedFunction function : functions) {
                                    if (methodName.equals(function.getName()) && MyPsiUtil.isParameterEquals(argTypes, function.getValueParameters())) {
                                        return new Pair<>(psiFile.getVirtualFile(), function.getTextOffset());
                                    }
                                }
                            } else {

                                final @NotNull Collection<KtObjectDeclaration> ktObjectDeclarations = PsiTreeUtil.findChildrenOfAnyType(psiFile, KtObjectDeclaration.class);
                                for (KtObjectDeclaration ktObjectDeclaration : ktObjectDeclarations) {
                                    for (KtSuperTypeListEntry entry : ktObjectDeclaration.getSuperTypeListEntries()) {
                                        if (objLiteralType.equals(entry.getTypeAsUserType().getReferencedName())) {
                                            final Collection<KtNamedFunction> children = PsiTreeUtil.findChildrenOfAnyType(ktObjectDeclaration, KtNamedFunction.class);
                                            for (KtNamedFunction child : children) {
                                                if (methodName.equals(child.getName()) && MyPsiUtil.isParameterEquals(argTypes, child.getValueParameters())) {
                                                    return new Pair<>(psiFile.getVirtualFile(), child.getTextOffset());
                                                }
                                            }
                                        }
                                    }
                                }

                            }


                        }

                        return null;

                    })
                    .finishOnUiThread(ModalityState.defaultModalityState(), p -> {
                        if (p != null)
                            openInEditor(p.first, p.second, project);
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
    public void openMethodCallInEditor(String fromClass, String fromMethod, List<String> fromArgTypes, String toClass, String toMethod, List<String> toArgType, int offset) {
        if (MyPsiUtil.isKtFileName(fromClass) || MyPsiUtil.isKtObjectLiteral(fromClass)) {
            ReadAction
                    .nonBlocking(() -> {
                        final int idx = fromClass.lastIndexOf(".");
                        final int odx = fromClass.indexOf("#");
                        final String objLiteralType = odx == -1 ? "": fromClass.substring(odx + 1);
                        final String fromClass1 = odx == -1 ? fromClass : fromClass.substring(0, odx);

                        final String pathSegment = fromClass1.replace(".", "/").replace("_kt", ".kt");

                        final String filename = fromClass1.substring(idx + 1).replace("_", ".");

                        final PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, filename, GlobalSearchScope.allScope(project));

                        for (PsiFile psiFile : psiFiles) {
                            final String path = psiFile.getVirtualFile().getPath();
                            if (!path.endsWith(pathSegment)) continue;

                            if (odx == -1) {
                                final @NotNull Collection<KtNamedFunction> functions = PsiTreeUtil.findChildrenOfAnyType(psiFile, KtNamedFunction.class);
                                for (KtNamedFunction function : functions) {
                                    if (fromMethod.equals(function.getName()) && MyPsiUtil.isParameterEquals(fromArgTypes, function.getValueParameters())) {
                                        return psiFile.getVirtualFile();
                                    }
                                }
                            } else {
                                final @NotNull Collection<KtObjectDeclaration> ktObjectDeclarations = PsiTreeUtil.findChildrenOfAnyType(psiFile, KtObjectDeclaration.class);
                                for (KtObjectDeclaration ktObjectDeclaration : ktObjectDeclarations) {
                                    for (KtSuperTypeListEntry entry : ktObjectDeclaration.getSuperTypeListEntries()) {
                                       if (objLiteralType.equals(entry.getTypeAsUserType().getReferencedName())) {
                                           final Collection<KtNamedFunction> children = PsiTreeUtil.findChildrenOfAnyType(ktObjectDeclaration, KtNamedFunction.class);
                                           for (KtNamedFunction child : children) {
                                               if (fromMethod.equals(child.getName()) && MyPsiUtil.isParameterEquals(fromArgTypes, child.getValueParameters())) {
                                                   return psiFile.getVirtualFile();
                                               }
                                           }
                                       }
                                    }
                                }
                            }
                        }

                        return null;
                    })
                    .finishOnUiThread(ModalityState.defaultModalityState(), containingClass -> openInEditor(containingClass, offset, project))
                    .inSmartMode(project)
                    .submit(NonUrgentExecutor.getInstance());

        } else {
            super.openMethodCallInEditor(fromClass, fromMethod, fromArgTypes, toClass, toMethod, toArgType, offset);
        }

    }

    @Override
    public List<String> findImplementations(String className) {
        return new ArrayList<>();
    }

    @Override
    public List<String> findImplementations(String className, String methodName, List<String> argTypes) {
        return new ArrayList<>();
    }

    @Override
    public void openLambdaExprInEditor(String fromClass, String fromMethod, List<String> fromArgTypes, List<String> argTypes, String returnType, int offset) {
        super.openLambdaExprInEditor(fromClass, fromMethod, fromArgTypes, argTypes, returnType, offset);
    }

    @Override
    public void openMethodCallInsideLambdaExprInEditor(String fromClass, String enclosedMethodName, List<String> enclosedMethodArgTypes, List<String> argTypes, String returnType, String toClass, String toMethod, List<String> toArgTypes, int offset) {
        super.openMethodCallInsideLambdaExprInEditor(fromClass, enclosedMethodName, enclosedMethodArgTypes, argTypes, returnType, toClass, toMethod, toArgTypes, offset);
    }

}
