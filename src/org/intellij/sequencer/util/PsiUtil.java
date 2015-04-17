package org.intellij.sequencer.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.util.Query;
import org.intellij.sequencer.generator.filters.MethodFilter;

import java.util.List;

public class PsiUtil {

    private PsiUtil() {
    }

    public static boolean isInsideAMethod(PsiFile psiFile, int position) {
        return getEnclosingMethod(psiFile, position) != null;
    }

    public static PsiMethod getEnclosingMethod(PsiFile psiFile, int position) {
        PsiElement psiElement = psiFile.findElementAt(position);
        while(psiElement != null) {
            if(psiElement instanceof PsiMethod)
                return (PsiMethod)psiElement;
            psiElement = psiElement.getContext();
        }
        return null;
    }

    public static boolean isInClassFile(PsiElement psiElement) {
        VirtualFile virtualFile = psiElement.getContainingFile().getVirtualFile();
        return virtualFile == null || virtualFile.getName().endsWith(".class");
    }

    public static boolean isInJarFileSystem(PsiElement psiElement) {
        if(psiElement == null)
            return false;
        VirtualFile virtualFile = psiElement.getContainingFile().getVirtualFile();
        if(virtualFile == null)
            return true;
        String protocol = virtualFile.getFileSystem().getProtocol();
        return protocol.equalsIgnoreCase("jar") || protocol.equalsIgnoreCase("zip");
    }

    public static VirtualFile findVirtualFile(PsiClass psiClass) {
        PsiFile containingFile = psiClass.getContainingFile();
        if(containingFile == null)
            return null;
        return containingFile.getVirtualFile();
    }

    public static PsiMethod findPsiMethod(PsiMethod[] psiMethods, String methodName, List argTypes) {
        for(int i = 0; i < psiMethods.length; i++) {
            PsiMethod psiMethod = psiMethods[i];
            if(PsiUtil.isMethod(psiMethod, methodName, argTypes))
                return psiMethod;
        }
        return null;
    }

    public static boolean isMethod(PsiMethod psiMethod, String methodName, List argTypes) {
        if(!psiMethod.getName().equals(methodName))
            return false;
        PsiParameter[] psiParameters =  psiMethod.getParameterList().getParameters();
        if(psiParameters.length != argTypes.size())
            return false;
        for(int i = 0; i < psiParameters.length; i++) {
            PsiParameter psiParameter = psiParameters[i];
            if(psiParameter.getType() == null)
                return false;
            if(!psiParameter.getType().getCanonicalText().equals(argTypes.get(i)))
                return false;
        }
        return true;
    }

    public static PsiMethod findPsiMethod(Project project, PsiManager psiManager,
                                          final String className, String methodName, List argTypes) {
        Query<PsiClass> search = AllClassesSearch.search(GlobalSearchScope.projectScope(project), project, new Condition<String>() {
            public boolean value(String s) {
                return className.equals(s);
            }
        });
        PsiClass psiClass = search.findFirst();
//        PsiClass psiClass = psiManager.findClass(className,
//              GlobalSearchScope.projectScope(project));
        if(psiClass == null)
            return null;
        PsiMethod[] psiMethods = psiClass.findMethodsByName(methodName, false);
        if(psiMethods == null || psiMethods.length == 0)
            return null;
        return PsiUtil.findPsiMethod(psiMethods, methodName, argTypes);
    }

    public static PsiElement findPsiCallExpression(final MethodFilter methodFilter,
                                                          PsiMethod fromPsiMethod,
                                                          final PsiMethod toPsiMethod,
                                                          final int callNo) {
        PsiCodeBlock psiCodeBlock = fromPsiMethod.getBody();
        CallFinder callFinder = new CallFinder(callNo, methodFilter, toPsiMethod);
        psiCodeBlock.accept(callFinder);
        return callFinder.getPsiElement();
    }

    public static String getPackageName(PsiMethod psiMethod) {
        PsiElement psiElement = psiMethod.getParent();
        while(psiElement != null) {
            if(psiElement instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile)psiElement;
                return psiJavaFile.getPackageName();
            }
            psiElement = psiElement.getParent();
        }
        return null;
    }

    public static void acceptChildren(PsiElement psiElement, PsiElementVisitor visitor) {
        psiElement.acceptChildren(visitor);
    }

}
