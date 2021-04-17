package org.intellij.sequencer.generator;

import com.google.gson.GsonBuilder;
import org.intellij.sequencer.Constants;

import java.util.*;

public class MethodDescription {
    private ClassDescription _classDescription;

    private String _methodName;
    private List<String> _attributes;
    private List<String> _argNames;
    private List<String> _argTypes;
    private String _returnType;
    private int _hashCode = -1;

    protected MethodDescription(ClassDescription classDescription, List<String> attributes,
                              String methodName, String returnType, List<String> argNames, List<String> argTypes) {
        _attributes = attributes;
        _returnType = returnType;
        _argNames = argNames;
        _argTypes = argTypes;
        _classDescription = classDescription;
        _methodName = methodName;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        for (String attribute : _attributes) {
            buffer.append('|').append(attribute);
        }
        buffer.append("|@").append(_methodName).append('[');
        for (int i = 0; i < _argNames.size(); i++) {
            buffer.append(_argNames.get(i)).append('=');
            buffer.append(_argTypes.get(i));
            if (i != _argNames.size() - 1)
                buffer.append(',');
        }
        buffer.append("]:").append(_returnType);
        return buffer.toString();
    }

    public String toJson() {
        return new GsonBuilder().create().toJson(this);
    }

    public ClassDescription getClassDescription() {
        return _classDescription;
    }

    public String getMethodName() {
        return _methodName;
    }

    public String getTitleName() {
        return getClassDescription().getClassShortName() + '.' +
                getMethodName() + "()";
    }

    public List<String> getAttributes() {
        return Collections.unmodifiableList(_attributes);
    }

    public List<String> getArgNames() {
        return Collections.unmodifiableList(_argNames);
    }

    public List<String> getArgTypes() {
        return Collections.unmodifiableList(_argTypes);
    }

    public String getReturnType() {
        return _returnType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodDescription that = (MethodDescription) o;
        return _hashCode == that._hashCode
                && Objects.equals(_classDescription, that._classDescription)
                && Objects.equals(_methodName, that._methodName)
                && Objects.equals(_argTypes, that._argTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_classDescription, _methodName, _argTypes, _hashCode);
    }


    static MethodDescription createMethodDescription(ClassDescription classDescription,
                                                     List<String> attributes, String methodName,
                                                     String returnType,
                                                     List<String> argNames, List<String> argTypes) {
        return new MethodDescription(classDescription, attributes, methodName, returnType, argNames, argTypes);
    }

    static MethodDescription createConstructorDescription(ClassDescription classDescription,
                                                          List<String> attributes, List<String> argNames,
                                                          List<String> argTypes) {
        return new MethodDescription(classDescription, attributes, Constants.CONSTRUCTOR_METHOD_NAME,
                classDescription.getClassName(), argNames, argTypes);
    }

    static MethodDescription createLambdaDescription(ClassDescription classDescription,
                                                     List<String> argNames, List<String> argTypes, String returnType) {
        return new MethodDescription(classDescription,
                new ArrayList<>(), Constants.Lambda_Invoke, returnType, argNames, argTypes);
    }
}
