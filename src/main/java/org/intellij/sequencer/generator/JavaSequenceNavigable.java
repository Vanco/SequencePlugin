package org.intellij.sequencer.generator;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.psi.util.ClassUtil;
import com.intellij.util.concurrency.NonUrgentExecutor;
import org.intellij.sequencer.openapi.SequenceNavigable;
import org.intellij.sequencer.util.MyPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.CancellablePromise;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.intellij.sequencer.util.MyPsiUtil.findNaviOffset;

public class JavaSequenceNavigable implements SequenceNavigable {
    protected final Project project;

    public JavaSequenceNavigable(Project project) {
        this.project = project;
    }

    public void openClassInEditor(final String className) {
        ReadAction
                .nonBlocking(() -> {
                    final PsiClass psiClass = ClassUtil.findPsiClass(getPsiManager(project), className);
                    if (psiClass == null) return null;

                    VirtualFile virtualFile = MyPsiUtil.findVirtualFile(psiClass);
                    final int offset = findNaviOffset(psiClass);

                    return new Pair<>(virtualFile, offset);
                })
                .finishOnUiThread(ModalityState.defaultModalityState(), p -> {
                    if (p != null)
                        openInEditor(p.first, p.second, project);
                })
                .inSmartMode(project)
                .submit(NonUrgentExecutor.getInstance());

    }

    @Override
    public void openMethodInEditor(String className, String methodName, List<String> argTypes) {

        ReadAction
                .nonBlocking(() -> {
                    final PsiMethod psiMethod = MyPsiUtil.findPsiMethod(getPsiManager(project), className, methodName, argTypes);
                    if (psiMethod == null) return null;

                    final PsiClass containingClass = psiMethod.getContainingClass();
                    if (containingClass == null) return null;

                    VirtualFile virtualFile = MyPsiUtil.findVirtualFile(containingClass);

                    final int offset = findNaviOffset(psiMethod);

                    return new Pair<>(virtualFile, offset);

                })
                .finishOnUiThread(ModalityState.defaultModalityState(), p -> {
                    if (p != null)
                        openInEditor(p.first, p.second, project);
                })
                .inSmartMode(project)
                .submit(NonUrgentExecutor.getInstance());
    }

    @Override
    public boolean isInsideAMethod() {
        return getCurrentPsiMethod(project) != null;
    }

    @Override
    public void openMethodCallInEditor(String fromClass, String fromMethod, List<String> fromArgTypes,
                                       String toClass, String toMethod, List<String> toArgType, int offset) {

        ReadAction
                .nonBlocking(() -> {
                    PsiMethod fromPsiMethod = MyPsiUtil.findPsiMethod(getPsiManager(project), fromClass, fromMethod, fromArgTypes);
                    if (fromPsiMethod == null) {
                        return null;
                    }

                    final PsiClass containingClass = fromPsiMethod.getContainingClass();
                    if (containingClass == null) return null;

                    return MyPsiUtil.findVirtualFile(containingClass);
                })
                .finishOnUiThread(ModalityState.defaultModalityState(), containingClass -> openInEditor(containingClass, offset, project))
                .inSmartMode(project)
                .submit(NonUrgentExecutor.getInstance());

    }

    @Override
    public void openLambdaExprInEditor(String fromClass, String methodName, List<String> methodArgTypes, List<String> argTypes, String returnType, int offset) {

        ReadAction
                .nonBlocking(() -> {
                    final PsiClass psiClass = MyPsiUtil.findPsiClass(getPsiManager(project), fromClass);
                    if (psiClass == null) return null;

                    return MyPsiUtil.findVirtualFile(psiClass);
                })
                .finishOnUiThread(ModalityState.defaultModalityState(), containingClass -> openInEditor(containingClass, offset, project))
                .inSmartMode(project)
                .submit(NonUrgentExecutor.getInstance());

    }

    @Override
    public void openMethodCallInsideLambdaExprInEditor(String fromClass,
                                                       String enclosedMethodName, List<String> enclosedMethodArgTypes,
                                                       List<String> argTypes, String returnType,
                                                       String toClass, String toMethod, List<String> toArgTypes, int offset) {
        ReadAction
                .nonBlocking(() -> {
                    final PsiClass psiClass = MyPsiUtil.findPsiClass(getPsiManager(project), fromClass);
                    if (psiClass == null) return null;
                    return MyPsiUtil.findVirtualFile(psiClass);
                })
                .finishOnUiThread(ModalityState.defaultModalityState(), containingClass -> openInEditor(containingClass, offset, project))
                .inSmartMode(project)
                .submit(NonUrgentExecutor.getInstance());

    }

    @Override
    public List<String> findImplementations(String className) {

        final @NotNull CancellablePromise<ArrayList<String>> readAction =
                ReadAction
                        .nonBlocking(() -> {
                            ArrayList<String> result = new ArrayList<>();
                            PsiClass psiClass = MyPsiUtil.findPsiClass(getPsiManager(project), className);

                            if (MyPsiUtil.isAbstract(psiClass)) {
                                PsiElement[] psiElements = DefinitionsScopedSearch.search(psiClass).toArray(PsiElement.EMPTY_ARRAY);

                                for (PsiElement element : psiElements) {
                                    if (element instanceof PsiClass) {
                                        PsiClass implClass = (PsiClass) element;
                                        result.add(implClass.getQualifiedName());
                                    }
                                }

                            }
                            return result;
                        })
                        .inSmartMode(project)
                        .submit(NonUrgentExecutor.getInstance());

        try {
            return readAction.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();

    }

    @Override
    public List<String> findImplementations(String className, String methodName, List<String> argTypes) {

        final @NotNull CancellablePromise<ArrayList<String>> readAction =
                ReadAction
                        .nonBlocking(() -> {
                            ArrayList<String> result = new ArrayList<>();

                            PsiMethod psiMethod = MyPsiUtil.findPsiMethod(getPsiManager(project), className, methodName, argTypes);
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
                        })
                        .inSmartMode(project)
                        .submit(NonUrgentExecutor.getInstance());


        try {
            return readAction.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();

    }

    @Override
    public String[] findSuperClass(String className) {

        final @NotNull CancellablePromise<String[]> readAction =
                ReadAction
                        .nonBlocking(() -> {
                            ArrayList<String> result = new ArrayList<>();
                            result.add(className);

                            PsiClass psiClass = MyPsiUtil.findPsiClass(getPsiManager(project), className);

                            PsiClass[] supers = psiClass.getSupers();

                            for (PsiClass aSuper : supers) {
                                result.add(aSuper.getQualifiedName());
                            }

                            return result.toArray(new String[0]);
                        })
                        .inSmartMode(project)
                        .submit(NonUrgentExecutor.getInstance());

        try {
            return readAction.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return new String[0];
    }

    private PsiMethod getCurrentPsiMethod(Project project) {
        Editor editor = getSelectedEditor(project);
        if (editor == null)
            return null;
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (virtualFile == null)
            return null;
        PsiFile psiFile = getPsiFile(virtualFile, project);
        return MyPsiUtil.getEnclosingMethod(psiFile, editor.getCaretModel().getOffset());
    }

    private Editor getSelectedEditor(Project project) {
        return getFileEditorManager(project).getSelectedTextEditor();
    }

    private FileEditorManager getFileEditorManager(@NotNull Project project) {
        return FileEditorManager.getInstance(project);
    }

    private PsiFile getPsiFile(VirtualFile virtualFile, @NotNull Project project) {
        return PsiManager.getInstance(project).findFile(virtualFile);
    }

    protected PsiManager getPsiManager(@NotNull Project project) {
        return PsiManager.getInstance(project);
    }

    protected void openInEditor(VirtualFile virtualFile, int offset, @NotNull Project project) {
        if (virtualFile == null)
            return;

        // temporary check offset MUST less than File length
        long length = virtualFile.getLength();
        getFileEditorManager(project).openTextEditor(new OpenFileDescriptor(project,
                virtualFile, offset > length ? (int) length : offset), true);
    }


}
