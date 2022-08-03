package org.intellij.sequencer.generator;

import org.intellij.sequencer.openapi.SequenceNavigable;

import java.util.List;

public class EmptySequenceNavigable implements SequenceNavigable {
    @Override
    public void openClassInEditor(String className) {

    }

    @Override
    public void openMethodInEditor(String className, String methodName, List<String> argTypes) {

    }

    @Override
    public boolean isInsideAMethod() {
        return false;
    }

    @Override
    public void openMethodCallInEditor(String fromClass, String fromMethod, List<String> fromArgTypes, String toClass, String toMethod, List<String> toArgType, int offset) {

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
    public void openLambdaExprInEditor(String fromClass, String fromMethod, List<String> fromArgTypes, List<String> argTypes, String returnType, int integer) {

    }

    @Override
    public void openMethodCallInsideLambdaExprInEditor(String fromClass, String enclosedMethodName, List<String> enclosedMethodArgTypes, List<String> argTypes, String returnType, String toClass, String toMethod, List<String> toArgTypes, int offset) {

    }

    @Override
    public String[] findSuperClass(String className) {
        return new String[0];
    }
}