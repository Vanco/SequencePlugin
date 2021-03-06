package org.intellij.sequencer.impl;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.util.Query;
import org.intellij.sequencer.SequenceNavigable;
import org.intellij.sequencer.generator.filters.CompositeMethodFilter;
import org.intellij.sequencer.generator.filters.MethodFilter;
import org.intellij.sequencer.util.MyPsiUtil;

import java.util.ArrayList;
import java.util.List;

public class JavaSequenceNavigable implements SequenceNavigable {
    private final Project _project;

    public JavaSequenceNavigable(Project project) {
        this._project = project;
    }

    public void openClassInEditor(final String className) {
        Query<PsiClass> search = AllClassesSearch.search(GlobalSearchScope.projectScope(_project), _project, className::endsWith);
        PsiClass psiClass = search.findFirst();
        if (psiClass == null)
            return;
        openInEditor(psiClass, psiClass);
    }

    @Override
    public void openMethodInEditor(String className, String methodName, List<String> argTypes) {
        PsiMethod psiMethod = MyPsiUtil.findPsiMethod(getPsiManager(), className, methodName, argTypes);
        if (psiMethod == null)
            return;
        openInEditor(psiMethod.getContainingClass(), psiMethod);
    }

    @Override
    public boolean isInsideAMethod() {
        return getCurrentPsiMethod() != null;
    }

    @Override
    public void openMethodCallInEditor(MethodFilter filter, String fromClass, String fromMethod, List<String> fromArgTypes,
                                       String toClass, String toMethod, List<String> toArgType, int callNo) {

        PsiMethod fromPsiMethod = MyPsiUtil.findPsiMethod(getPsiManager(), fromClass, fromMethod, fromArgTypes);
        if (fromPsiMethod == null) {
            return;
        }
        PsiMethod toPsiMethod = MyPsiUtil.findPsiMethod(getPsiManager(), toClass, toMethod, toArgType);
        if (toPsiMethod == null) {
            return;
        }

        PsiElement psiElement = MyPsiUtil.findPsiCallExpression(filter, fromPsiMethod, toPsiMethod, callNo);
        if (psiElement == null) {
            return;
        }
        PsiClass containingClass = fromPsiMethod.getContainingClass();

        openInEditor(containingClass, psiElement);
    }

    @Override
    public void openLambdaExprInEditor(String fromClass, String methodName, List<String> methodArgTypes, List<String> argTypes, String returnType) {
        PsiClass containingClass = MyPsiUtil.findPsiClass(getPsiManager(), fromClass);
        if (containingClass == null) return;

        PsiMethod psiMethod = MyPsiUtil.findPsiMethod(containingClass, methodName, methodArgTypes);
        if (psiMethod == null) return;

        PsiElement psiElement = MyPsiUtil.findLambdaExpression(psiMethod, argTypes, returnType);

        openInEditor(containingClass, psiElement);

    }

    @Override
    public void openMethodCallInsideLambdaExprInEditor(CompositeMethodFilter methodFilter, String fromClass,
                                                       String enclosedMethodName, List<String> enclosedMethodArgTypes,
                                                       List<String> argTypes, String returnType,
                                                       String toClass, String toMethod, List<String> toArgTypes, int callNo) {
        PsiClass containingClass = MyPsiUtil.findPsiClass(getPsiManager(), fromClass);
        if (containingClass == null) return;

        PsiMethod psiMethod = MyPsiUtil.findPsiMethod(containingClass, enclosedMethodName, enclosedMethodArgTypes);
        if (psiMethod == null) return;

        PsiLambdaExpression lambdaPsiElement = (PsiLambdaExpression) MyPsiUtil.findLambdaExpression(psiMethod, argTypes, returnType);

        PsiMethod toPsiMethod = MyPsiUtil.findPsiMethod(getPsiManager(), toClass, toMethod, toArgTypes);
        if (toPsiMethod == null || lambdaPsiElement == null) {
            return;
        }

        PsiElement psiElement = MyPsiUtil.findPsiCallExpression(methodFilter, lambdaPsiElement, toPsiMethod, callNo);
        if (psiElement == null) {
            return;
        }

        openInEditor(containingClass, psiElement);
    }

    @Override
    public List<String> findImplementations(String className) {
        PsiClass psiClass = MyPsiUtil.findPsiClass(getPsiManager(), className);

        if (MyPsiUtil.isAbstract(psiClass)) {
            PsiElement[] psiElements = DefinitionsScopedSearch.search(psiClass).toArray(PsiElement.EMPTY_ARRAY);
            ArrayList<String> result = new ArrayList<>();

            for (PsiElement element : psiElements) {
                if (element instanceof PsiClass) {
                    PsiClass implClass = (PsiClass) element;
                    result.add(implClass.getQualifiedName());
                }
            }

            return result;
        }
        return new ArrayList<>();

    }

    @Override
    public List<String> findImplementations(String className, String methodName, List<String> argTypes) {
        ArrayList<String> result = new ArrayList<>();

        PsiMethod psiMethod = MyPsiUtil.findPsiMethod(getPsiManager(), className, methodName, argTypes);
        if (psiMethod == null) return result;

        PsiClass containingClass = psiMethod.getContainingClass();
        if (containingClass == null) {
            containingClass = (PsiClass) psiMethod.getParent().getContext();
        }
        if (MyPsiUtil.isAbstract(containingClass)) {
            PsiElement[] psiElements = DefinitionsScopedSearch.search(psiMethod).toArray(PsiElement.EMPTY_ARRAY);

            for (PsiElement element : psiElements) {
                if (element instanceof PsiMethod) {

                    PsiMethod method = (PsiMethod) element;
                    PsiClass implClass = method.getContainingClass();
                    if (implClass == null) {
                        implClass = (PsiClass) method.getParent().getContext();
                    }
                    if (implClass != null) {
                        result.add(implClass.getQualifiedName());
                    }
                }
            }

            return result;
        }
        return result;
    }

    private PsiMethod getCurrentPsiMethod() {
        Editor editor = getSelectedEditor();
        if (editor == null)
            return null;
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (virtualFile == null)
            return null;
        PsiFile psiFile = getPsiFile(virtualFile);
        return MyPsiUtil.getEnclosingMethod(psiFile, editor.getCaretModel().getOffset());
    }

    private Editor getSelectedEditor() {
        return getFileEditorManager().getSelectedTextEditor();
    }

    private FileEditorManager getFileEditorManager() {
        return FileEditorManager.getInstance(_project);
    }

    private PsiFile getPsiFile(VirtualFile virtualFile) {
        return PsiManager.getInstance(_project).findFile(virtualFile);
    }

    private PsiManager getPsiManager() {
        return PsiManager.getInstance(_project);
    }

    private void openInEditor(PsiClass psiClass, PsiElement psiElement) {
        VirtualFile virtualFile = MyPsiUtil.findVirtualFile(psiClass);
        if (virtualFile == null)
            return;
        final int offset = findBestOffset(psiElement);

        getFileEditorManager().openTextEditor(new OpenFileDescriptor(_project,
                virtualFile, offset), true);
    }

    private int findBestOffset(PsiElement psiElement) {
        if (psiElement instanceof PsiMethod) {
            return psiElement.getNavigationElement().getTextOffset();
        } else if (psiElement instanceof PsiMethodCallExpression) {
            return psiElement.getFirstChild().getNavigationElement().getTextOffset();
        } else if (psiElement instanceof PsiLambdaExpression) {
            return psiElement.getNavigationElement().getTextOffset();
        } else if (psiElement instanceof PsiNewExpression) {
            return psiElement.getNavigationElement().getTextOffset();
        }
        return psiElement.getNavigationElement().getTextOffset();
    }

}
