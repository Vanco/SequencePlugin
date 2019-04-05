package org.intellij.sequencer.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.ClassUtil;
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
        while (psiElement != null) {
            if (psiElement instanceof PsiMethod)
                return (PsiMethod) psiElement;
            psiElement = psiElement.getContext();
        }
        return null;
    }

    public static boolean isInClassFile(PsiElement psiElement) {
        VirtualFile virtualFile = psiElement.getContainingFile().getVirtualFile();
        return virtualFile == null || virtualFile.getName().endsWith(".class");
    }

    public static boolean isInJarFileSystem(PsiElement psiElement) {
        if (psiElement == null)
            return false;
        VirtualFile virtualFile = psiElement.getContainingFile().getVirtualFile();
        if (virtualFile == null)
            return true;
        String protocol = virtualFile.getFileSystem().getProtocol();
        return protocol.equalsIgnoreCase("jar") || protocol.equalsIgnoreCase("zip");
    }

    public static VirtualFile findVirtualFile(PsiClass psiClass) {
        PsiFile containingFile = psiClass.getContainingFile();
        if (containingFile == null)
            return null;
        return containingFile.getVirtualFile();
    }

    public static PsiMethod findPsiMethod(PsiMethod[] psiMethods, String methodName, List argTypes) {
        for (int i = 0; i < psiMethods.length; i++) {
            PsiMethod psiMethod = psiMethods[i];
            if (PsiUtil.isMethod(psiMethod, methodName, argTypes))
                return psiMethod;
        }
        return null;
    }

    public static boolean isMethod(PsiMethod psiMethod, String methodName, List argTypes) {
        if (!psiMethod.getName().equals(methodName))
            return false;
        PsiParameter[] psiParameters = psiMethod.getParameterList().getParameters();
        if (psiParameters.length != argTypes.size())
            return false;
        for (int i = 0; i < psiParameters.length; i++) {
            PsiParameter psiParameter = psiParameters[i];
            if (psiParameter.getType() == null)
                return false;
            if (!psiParameter.getType().getCanonicalText().equals(argTypes.get(i)))
                return false;
        }
        return true;
    }

    public static boolean isAbstract(PsiClass psiClass) {
        return psiClass != null
                && (psiClass.isInterface()
                        || psiClass.getModifierList() != null
                        && psiClass.getModifierList().hasModifierProperty("abstract")
        );
    }

    public static boolean isExternal(PsiClass psiClass) {
        return isInClassFile(psiClass) || isInJarFileSystem(psiClass);
    }

    public static PsiClass findPsiClass(Project project, PsiManager psiManager, String className) {
        PsiClass psiClass = ClassUtil.findPsiClass(psiManager, className);
        return psiClass;
    }

    public static PsiMethod findPsiMethod(Project project, PsiManager psiManager,
                                          final String className, String methodName, List argTypes) {
        PsiClass psiClass = ClassUtil.findPsiClass(psiManager, className);
        if (psiClass == null)
            return null;
        PsiMethod[] psiMethods = psiClass.findMethodsByName(methodName, false);
        if (psiMethods == null || psiMethods.length == 0)
            return null;
        return PsiUtil.findPsiMethod(psiMethods, methodName, argTypes);
    }

    public static PsiElement findPsiCallExpression(final MethodFilter methodFilter,
                                                   PsiMethod fromPsiMethod,
                                                   final PsiMethod toPsiMethod,
                                                   final int callNo) {
        PsiCodeBlock psiCodeBlock = fromPsiMethod.getBody();
        if (psiCodeBlock == null) return null;
        CallFinder callFinder = new CallFinder(callNo, methodFilter, toPsiMethod);
        psiCodeBlock.accept(callFinder);
        return callFinder.getPsiElement();
    }

    public static String getPackageName(PsiMethod psiMethod) {
        PsiElement psiElement = psiMethod.getParent();
        while (psiElement != null) {
            if (psiElement instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiElement;
                return psiJavaFile.getPackageName();
            }
            psiElement = psiElement.getParent();
        }
        return null;
    }

    public static void acceptChildren(PsiElement psiElement, PsiElementVisitor visitor) {
        psiElement.acceptChildren(visitor);
    }

    public static boolean isPipeline(PsiCallExpression callExpression) {
        PsiElement[] children = callExpression.getChildren();
        for (PsiElement child : children) {
            if (child instanceof PsiReferenceExpression) {
                for (PsiElement psiElement : child.getChildren()) {
                    if (psiElement instanceof PsiMethodCallExpression) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    public static boolean isComplexCall(PsiCallExpression callExpression) {
        PsiExpressionList argumentList = callExpression.getArgumentList();
        if (argumentList != null) {
            PsiExpression[] expressions = argumentList.getExpressions();
            for (PsiExpression expression : expressions) {
                if (expression instanceof PsiCallExpression) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isInterface(PsiModifierList psiModifierList) {
        PsiElement parent = psiModifierList.getParent();
        if (parent instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) parent;
            return psiClass.isInterface();
        }
        return false;
    }

    public static PsiMethod findEncolsedPsiMethod(PsiLambdaExpression expression) {
        PsiElement parent = expression.getParent();
        while (!(parent instanceof PsiMethod)) {
            parent = parent.getParent();
        }
        return (PsiMethod) parent;
    }
}
