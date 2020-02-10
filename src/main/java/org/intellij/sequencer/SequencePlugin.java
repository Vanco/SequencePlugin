package org.intellij.sequencer;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.ui.content.*;
import com.intellij.util.Query;
import icons.SequencePluginIcons;
import org.intellij.sequencer.generator.SequenceParams;
import org.intellij.sequencer.generator.filters.MethodFilter;
import org.intellij.sequencer.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class SequencePlugin implements ProjectComponent {
    private static final String PLUGIN_NAME = "Sequence";
//    private static final Icon DISABLED_ICON = SequencePluginIcons.LOCKED_ICON;
    private static final Icon S_ICON = SequencePluginIcons.SEQUENCE_ICON_13;

    private final Project _project;
    private ToolWindow _toolWindow;

    public static Icon loadIcon(String name) {
        return IconLoader.findIcon(SequencePlugin.class.getResource("/icons/" + name),true);
    }

    public SequencePlugin(Project project) {
        _project = project;
    }

    public void projectOpened() {
        _toolWindow = getToolWindowManager().registerToolWindow(
                PLUGIN_NAME, true, ToolWindowAnchor.BOTTOM);
        _toolWindow.setIcon(S_ICON);
        _toolWindow.setAvailable(false, null);
        _toolWindow.getContentManager().addContentManagerListener(new ContentManagerAdapter() {

            @Override
            public void contentRemoved(@NotNull ContentManagerEvent event) {
                if (_toolWindow.getContentManager().getContentCount() == 0) {
                    _toolWindow.setAvailable(false, null);
                }
            }

        });
    }

    private ToolWindowManager getToolWindowManager() {
        return ToolWindowManager.getInstance(_project);
    }

    public static SequencePlugin getInstance(Project project) {
        return (SequencePlugin)project.getComponent(SequencePlugin.class);
    }

    public void projectClosed() {
        getToolWindowManager().unregisterToolWindow(PLUGIN_NAME);
    }

    public String getComponentName() {
        return PLUGIN_NAME;
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    public void showSequence(SequenceParams params) {
        PsiMethod enclosingPsiMethod = getCurrentPsiMethod();
        if(enclosingPsiMethod == null)
            return;
        _toolWindow.setAvailable(true, null);

        final SequencePanel sequencePanel = new SequencePanel(this, enclosingPsiMethod, params);
        Runnable postAction = new Runnable() {
            public void run() {
                sequencePanel.generate();
                addSequencePanel(sequencePanel);
            }
        };
        if(_toolWindow.isActive())
            _toolWindow.show(postAction);
        else
            _toolWindow.activate(postAction);
    }

    private void addSequencePanel(final SequencePanel sequencePanel) {
        final Content content = ServiceManager.getService(ContentFactory.class).createContent(sequencePanel, sequencePanel.getTitleName(), false);
        _toolWindow.getContentManager().addContent(content);
        _toolWindow.getContentManager().setSelectedContent(content);
    }


    public boolean isInsideAMethod() {
        return getCurrentPsiMethod() != null;
    }

    private Editor getSelectedEditor() {
        Editor selectedEditor = getFileEditorManager().getSelectedTextEditor();
        if(selectedEditor == null)
            return null;
        return selectedEditor;
    }

    private PsiManager getPsiManager() {
        return PsiManager.getInstance(_project);
    }


    private PsiFile getPsiFile(VirtualFile virtualFile) {
        return PsiManager.getInstance(_project).findFile(virtualFile);
    }

    private FileEditorManager getFileEditorManager() {
        return FileEditorManager.getInstance(_project);
    }

    private PsiMethod getCurrentPsiMethod() {
        Editor editor = getSelectedEditor();
        if(editor == null)
            return null;
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if(virtualFile == null)
            return null;
        PsiFile psiFile = getPsiFile(virtualFile);
        return PsiUtil.getEnclosingMethod(psiFile, editor.getCaretModel().getOffset());
    }

    public void openClassInEditor(final String className) {
        Query<PsiClass> search = AllClassesSearch.search(GlobalSearchScope.projectScope(_project), _project, new Condition<String>() {
            public boolean value(String s) {
                return className.endsWith(s);
            }
        });
        PsiClass psiClass = search.findFirst();
        if(psiClass == null)
            return;
        openInEditor(psiClass, psiClass);
    }

    public void openMethodInEditor(String className, String methodName, List argTypes) {
        PsiMethod psiMethod = PsiUtil.findPsiMethod(_project, getPsiManager(), className, methodName, argTypes);
        if(psiMethod == null)
            return;
        openInEditor(psiMethod.getContainingClass(), psiMethod);
    }

    private void openInEditor(PsiClass psiClass, PsiElement psiElement) {
        VirtualFile virtualFile = PsiUtil.findVirtualFile(psiClass);
        if(virtualFile == null)
            return;
        getFileEditorManager().openTextEditor(new OpenFileDescriptor(_project,
                virtualFile, psiElement.getTextOffset()), true);
    }

    public void openMethodCallInEditor(MethodFilter filter, String fromClass, String fromMethod, List fromArgTypes,
                                       String toClass, String toMethod, List toArgType, int callNo) {
        PsiMethod fromPsiMethod = PsiUtil.findPsiMethod(_project, getPsiManager(), fromClass, fromMethod, fromArgTypes);
        if(fromPsiMethod == null)
            return;
        PsiMethod toPsiMethod = PsiUtil.findPsiMethod(_project, getPsiManager(), toClass, toMethod, toArgType);
        if(toPsiMethod == null)
            return;
        PsiElement psiElement = PsiUtil.findPsiCallExpression(filter, fromPsiMethod, toPsiMethod, callNo);
        if(psiElement == null)
            return;
        openInEditor(fromPsiMethod.getContainingClass(), psiElement);
    }


    public List<String> findImplementations(String className) {
        PsiClass psiClass = PsiUtil.findPsiClass(_project, getPsiManager(), className);

        if (PsiUtil.isAbstract(psiClass)) {
            PsiElement[] psiElements = DefinitionsScopedSearch.search(psiClass).toArray(PsiElement.EMPTY_ARRAY);
            ArrayList<String> result = new ArrayList<String>();

            for (PsiElement element : psiElements) {
                if (element instanceof PsiClass) {
                    PsiClass implClass = (PsiClass) element;
                    result.add(implClass.getQualifiedName());
                }
            }

            return result;
        }
        return new ArrayList<String>();

    }

    public List<String> findImplementations(String className, String methodName, List argTypes) {
        PsiMethod psiMethod = PsiUtil.findPsiMethod(_project, getPsiManager(), className, methodName, argTypes);
        PsiClass containingClass = psiMethod.getContainingClass();
        if (containingClass == null) {
            containingClass = (PsiClass) psiMethod.getParent().getContext();
        }
        if (PsiUtil.isAbstract(containingClass)) {
            PsiElement[] psiElements = DefinitionsScopedSearch.search(psiMethod).toArray(PsiElement.EMPTY_ARRAY);
            ArrayList<String> result = new ArrayList<String>();

            for (PsiElement element : psiElements) {
                if (element instanceof PsiMethod) {

                    PsiMethod method = (PsiMethod) element;
                    PsiClass implClass = method.getContainingClass();
                    if (implClass == null) {
                        implClass = (PsiClass) method.getParent().getContext();
                    }
                    result.add(implClass.getQualifiedName());
                }
            }

            return result;
        }
        return new ArrayList<String>();
    }


}
