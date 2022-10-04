package org.intellij.sequencer.util;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.ClassUtil;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.psi.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class MyPsiUtil {

    private MyPsiUtil() {
    }

    public static PsiMethod getEnclosingMethod(PsiFile psiFile, int position) {
        PsiElement psiElement = psiFile.findElementAt(position);
        return PsiTreeUtil.getParentOfType(psiElement, PsiMethod.class);
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
        PsiFile containingFile = psiClass.getNavigationElement().getContainingFile();
        if (containingFile == null)
            return null;
        return containingFile.getVirtualFile();
    }

    public static PsiMethod findPsiMethod(PsiMethod[] psiMethods, String methodName, List<String> argTypes) {
        for (PsiMethod psiMethod : psiMethods) {
            if (MyPsiUtil.isMethod(psiMethod, methodName, argTypes))
                return psiMethod;
        }
        return null;
    }

    public static boolean isMethod(PsiMethod psiMethod, String methodName, List<String> argTypes) {
        if (!psiMethod.getName().equals(methodName))
            return false;
        PsiParameter[] psiParameters = psiMethod.getParameterList().getParameters();
        return parameterEquals(psiParameters, argTypes);
    }

    public static boolean isAbstract(PsiClass psiClass) {
        return psiClass != null
                && (psiClass.isInterface()
                || psiClass.getModifierList() != null
                && psiClass.getModifierList().hasModifierProperty(PsiModifier.ABSTRACT)
        );
    }

    public static boolean isExternal(PsiClass psiClass) {
        return isInClassFile(psiClass) || isInJarFileSystem(psiClass);
    }

    public static PsiClass findPsiClass(PsiManager psiManager, String className) {
        return ClassUtil.findPsiClass(psiManager, className);
    }

    public static PsiMethod findPsiMethod(PsiManager psiManager,
                                          final String className, String methodName, List<String> argTypes) {
        PsiClass psiClass = ClassUtil.findPsiClass(psiManager, className);
        if (psiClass == null)
            return null;
        return findPsiMethod(psiClass, methodName, argTypes);
    }

    @Nullable
    public static PsiMethod findPsiMethod(PsiClass psiClass, String methodName, List<String> argTypes) {
        PsiMethod[] psiMethods = psiClass.findMethodsByName(methodName, false);
        if (psiMethods.length == 0)
            return null;
        return MyPsiUtil.findPsiMethod(psiMethods, methodName, argTypes);
    }

    public static String getPackageName(PsiElement param) {
        PsiElement psiElement = param;
        while (psiElement != null) {
            if (psiElement instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiElement;
                return psiJavaFile.getPackageName();
            }
            psiElement = psiElement.getParent();
        }
        return null;
    }

    /**
     * Check PsiCallExpression is like:
     * <code> a().b().c() </code>
     * Calls after one by one.
     *
     * @param callExpression the expression
     * @return true if the expression are calls one by one.
     */
    public static boolean isPipeline(PsiCallExpression callExpression) {
        PsiElement[] children = callExpression.getChildren();
        for (PsiElement child : children) {
            if (child instanceof PsiReferenceExpression) {
                for (PsiElement psiElement : child.getChildren()) {
                    if (psiElement instanceof PsiMethodCallExpression || psiElement instanceof PsiNewExpression) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    /**
     * Check PsiCallExpression is like:
     * <code> a(b(),c) </code>
     * The argument is another call.
     *
     * @param callExpression the expression
     * @return true when argument is another call.
     */
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

    /**
     * Check KtCallExpression is like:
     * <code> a(b(),c) </code>
     * <code> a(b(),c -> {...}) </code>
     * The argument is another call.
     *
     * @param ktCallExpression the expression
     * @return true when the argument is another call
     */
    public static boolean isComplexCall(KtCallExpression ktCallExpression) {
        final List<KtValueArgument> valueArguments = ktCallExpression.getValueArguments();
        for (KtValueArgument valueArgument : valueArguments) {
            final KtExpression argumentExpression = valueArgument.getArgumentExpression();
            if (argumentExpression instanceof KtDotQualifiedExpression
                    || argumentExpression instanceof KtCallExpression
                    || argumentExpression instanceof KtLambdaExpression) {
                return true;
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

    public static PsiMethod findEnclosedPsiMethod(PsiLambdaExpression expression) {
        PsiElement parent = expression.getParent();
        while (!(parent instanceof PsiMethod)) {
            parent = parent.getParent();
        }
        return (PsiMethod) parent;
    }

    public static boolean isLambdaExpression(PsiLambdaExpression expression, List<String> argTypes, String returnType) {
        PsiType psiType = expression.getFunctionalInterfaceType();
        if (psiType == null)
            return false;
        if (!Objects.equals(returnType, psiType.getCanonicalText())) {
            return false;
        }

        PsiParameter[] psiParameters = expression.getParameterList().getParameters();
        return parameterEquals(psiParameters, argTypes);
    }

    private static boolean parameterEquals(PsiParameter[] psiParameters, List<String> argTypes) {
        if (psiParameters.length != argTypes.size())
            return false;
        for (int i = 0; i < psiParameters.length; i++) {
            PsiParameter psiParameter = psiParameters[i];
            if (psiParameter.getLanguage().equals(KotlinLanguage.INSTANCE)) {
                final KtTypeReference typeReference = ((KtParameter) psiParameter.getNavigationElement()).getTypeReference();
                if (!getKtType(typeReference).equals(argTypes.get(i))) {
                    return false;
                }
            } else {
                if (!psiParameter.getType().getCanonicalText().equals(argTypes.get(i)))
                    return false;
            }
        }
        return true;
    }

    /**
     * Find the best offset for navigation
     *
     * @param psiElement PsiElement like PsiClass, PsiMethod, PsiCallExpression etc.
     * @return the offset.
     */
    public static int findNaviOffset(PsiElement psiElement) {
        if (psiElement == null)
            return 0;
        int offset;
        if (psiElement instanceof PsiMethodCallExpression) {
            offset = psiElement.getFirstChild().getNavigationElement().getTextOffset();
        } else {
            offset = psiElement.getNavigationElement().getTextOffset();
        }
        return offset;
    }

    /**
     * Create .sdt file chooser.
     *
     * @return JFileChooser
     */
    @NotNull
    public static JFileChooser getFileChooser() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setDialogTitle("Open Diagram");
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith("sdt");
            }

            public String getDescription() {
                return "SequenceDiagram (.sdt) File";
            }
        });
        return chooser;
    }


    public static boolean isKtFileName(String fromClass) {
        return fromClass != null && fromClass.endsWith("_kt");
    }

    public static boolean isKtObjectLiteral(String fromClass) {
        return fromClass != null && fromClass.contains("#");
    }

    public static boolean isParameterEquals(List<String> argTypes, List<KtParameter> typeParameters) {
        if (typeParameters.size() != argTypes.size())
            return false;
        for (int i = 0; i < typeParameters.size(); i++) {
            KtParameter ktParameter = typeParameters.get(i);
            final KtTypeReference typeReference = ktParameter.getTypeReference();
            final String stype = argTypes.get(i);
            if (typeReference == null || !stype.equals(getKtType(typeReference)))
                return false;
        }
        return true;
    }

    /**
     * Get Type reference string.
     *
     * @param typeReference KtTypeReference
     * @return String
     */
    public static String getKtType(@Nullable KtTypeReference typeReference) {
        if (typeReference == null) {
            return "Unit";
        }

        KtTypeElement typeElement = typeReference.getTypeElement();

        if (typeElement instanceof KtNullableType) {
            typeElement = ((KtNullableType) typeElement).getInnerType();
        }

        if (typeElement instanceof KtUserType) {
            return ((KtUserType) typeElement).getReferencedName();
        }

        if (typeElement instanceof KtFunctionType) {
            return typeElement.getText().replaceAll("[\\(|\\)]", "_").replaceAll(" ", "");//.replaceAll("->", "→");
        }

        return "Unit";
    }

    /**
     * Find PisClass in the PsiFile exclude PsiTypeParameter.
     *
     * @param psiFile
     * @param psiClassClass
     * @return
     */
    public static <T extends PsiElement> Collection<T> findChildrenOfType(PsiFile psiFile, Class<? extends T> psiClassClass) {
        @NotNull Collection<T> children = PsiTreeUtil.findChildrenOfType(psiFile, psiClassClass);
        children.removeIf(it -> it instanceof PsiTypeParameter);
        return children;
    }
}
