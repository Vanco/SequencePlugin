package org.intellij.sequencer.impl;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.ContentManagerListener;
import com.intellij.util.Query;
import icons.SequencePluginIcons;
import org.intellij.sequencer.SequencePanel;
import org.intellij.sequencer.SequenceService;
import org.intellij.sequencer.generator.SequenceParams;
import org.intellij.sequencer.generator.filters.CompositeMethodFilter;
import org.intellij.sequencer.generator.filters.MethodFilter;
import org.intellij.sequencer.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 2020/2/23.
 */
public class SequenceServiceImpl implements SequenceService {
    private static final String PLUGIN_NAME = "Sequence Diagram";
    private static final Icon S_ICON = SequencePluginIcons.SEQUENCE_ICON_13;

    private final Project _project;
    private final ToolWindow _toolWindow;

    public SequenceServiceImpl(Project project) {

        _project = project;
        _toolWindow = ToolWindowManager.getInstance(_project)
                .registerToolWindow(RegisterToolWindowTask.closable(PLUGIN_NAME, S_ICON));
        _toolWindow.setAvailable(false, null);
        _toolWindow.getContentManager().addContentManagerListener(new ContentManagerListener() {

            @Override
            public void contentRemoved(@NotNull ContentManagerEvent event) {
                if (_toolWindow.getContentManager().getContentCount() == 0) {
                    _toolWindow.setAvailable(false, null);
                }
            }

        });
    }

    @Override
    public void showSequence(SequenceParams params) {
        PsiMethod enclosingPsiMethod = getCurrentPsiMethod();
        if (enclosingPsiMethod == null)
            return;
        _toolWindow.setAvailable(true, null);

        final SequencePanel sequencePanel = new SequencePanel(this, enclosingPsiMethod, params);
        Runnable postAction = () -> {
            sequencePanel.generate();
            addSequencePanel(sequencePanel);
        };
        if (_toolWindow.isActive())
            _toolWindow.show(postAction);
        else
            _toolWindow.activate(postAction);
    }

    @Override
    public void openClassInEditor(final String className) {
        Query<PsiClass> search = AllClassesSearch.search(GlobalSearchScope.projectScope(_project), _project, className::endsWith);
        PsiClass psiClass = search.findFirst();
        if (psiClass == null)
            return;
        openInEditor(psiClass, psiClass);
    }

    @Override
    public void openMethodInEditor(String className, String methodName, List<String> argTypes) {
        PsiMethod psiMethod = PsiUtil.findPsiMethod(getPsiManager(), className, methodName, argTypes);
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

        PsiMethod fromPsiMethod = PsiUtil.findPsiMethod(getPsiManager(), fromClass, fromMethod, fromArgTypes);
        if (fromPsiMethod == null) {
            return;
        }
        PsiMethod toPsiMethod = PsiUtil.findPsiMethod(getPsiManager(), toClass, toMethod, toArgType);
        if (toPsiMethod == null) {
            return;
        }

        PsiElement psiElement = PsiUtil.findPsiCallExpression(filter, fromPsiMethod, toPsiMethod, callNo);
        if (psiElement == null) {
            return;
        }
        PsiClass containingClass = fromPsiMethod.getContainingClass();

        openInEditor(containingClass, psiElement);
    }

    @Override
    public void openLambdaExprInEditor(String fromClass, String methodName, List<String> methodArgTypes, List<String> argTypes, String returnType) {
        PsiClass containingClass = PsiUtil.findPsiClass(getPsiManager(), fromClass);

        PsiMethod psiMethod = PsiUtil.findPsiMethod(containingClass, methodName, methodArgTypes);
        if (psiMethod == null) return;

        PsiElement psiElement = PsiUtil.findLambdaExpression(psiMethod, argTypes, returnType);

        openInEditor(containingClass, psiElement);

    }

    @Override
    public void openMethodCallInsideLambdaExprInEditor(CompositeMethodFilter methodFilter, String fromClass,
                                                       String enclosedMethodName, List<String> enclosedMethodArgTypes,
                                                       List<String> argTypes, String returnType,
                                                       String toClass, String toMethod, List<String> toArgTypes, int callNo) {
        PsiClass containingClass = PsiUtil.findPsiClass(getPsiManager(), fromClass);

        PsiMethod psiMethod = PsiUtil.findPsiMethod(containingClass, enclosedMethodName, enclosedMethodArgTypes);
        if (psiMethod == null) return;

        PsiLambdaExpression lambdaPsiElement = (PsiLambdaExpression) PsiUtil.findLambdaExpression(psiMethod, argTypes, returnType);

        PsiMethod toPsiMethod = PsiUtil.findPsiMethod(getPsiManager(), toClass, toMethod, toArgTypes);
        if (toPsiMethod == null) {
            return;
        }

        PsiElement psiElement = PsiUtil.findPsiCallExpression(methodFilter, lambdaPsiElement, toPsiMethod, callNo);
        if (psiElement == null) {
            return;
        }

        openInEditor(containingClass, psiElement);
    }

    @Override
    public List<String> findImplementations(String className) {
        PsiClass psiClass = PsiUtil.findPsiClass(getPsiManager(), className);

        if (PsiUtil.isAbstract(psiClass)) {
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

        PsiMethod psiMethod = PsiUtil.findPsiMethod(getPsiManager(), className, methodName, argTypes);
        if (psiMethod == null) return result;

        PsiClass containingClass = psiMethod.getContainingClass();
        if (containingClass == null) {
            containingClass = (PsiClass) psiMethod.getParent().getContext();
        }
        if (PsiUtil.isAbstract(containingClass)) {
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
        return PsiUtil.getEnclosingMethod(psiFile, editor.getCaretModel().getOffset());
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

    private void addSequencePanel(final SequencePanel sequencePanel) {
        final Content content = ServiceManager.getService(ContentFactory.class).createContent(sequencePanel, sequencePanel.getTitleName(), false);
        _toolWindow.getContentManager().addContent(content);
        _toolWindow.getContentManager().setSelectedContent(content);
    }

    private PsiManager getPsiManager() {
        return PsiManager.getInstance(_project);
    }

    private void openInEditor(PsiClass psiClass, PsiElement psiElement) {
        VirtualFile virtualFile = PsiUtil.findVirtualFile(psiClass);
        if (virtualFile == null)
            return;
        getFileEditorManager().openTextEditor(new OpenFileDescriptor(_project,
                virtualFile, psiElement.getTextOffset()), true);
    }


}
