package com.zenuml.dsl;

import com.google.gson.GsonBuilder;
import org.intellij.sequencer.Constants;

import java.util.*;

public class MethodDescription {
    private ClassDescription _classDescription;

    private String _methodName;
    private List _attributes;
    private List _argNames;
    private List _argTypes;
    private String _returnType;
    private int _hashCode = -1;

    private MethodDescription(ClassDescription classDescription, List attributes,
                              String methodName, String returnType, List argNames, List argTypes) {
        _attributes = attributes;
        _returnType = returnType;
        _argNames = argNames;
        _argTypes = argTypes;
        _classDescription = classDescription;
        _methodName = methodName;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (Iterator iterator = _attributes.iterator(); iterator.hasNext(); ) {
            String attribute = (String) iterator.next();
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

    public String getMethodSignature(){
        return _methodName+"()";
    }

    public String getTitleName() {
        return getClassDescription().getClassShortName() + '.' +
                getMethodName() + "()";
    }

    public List getAttributes() {
        return Collections.unmodifiableList(_attributes);
    }

    public List getArgNames() {
        return Collections.unmodifiableList(_argNames);
    }

    public List getArgTypes() {
        return Collections.unmodifiableList(_argTypes);
    }

    public String getReturnType() {
        return _returnType;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodDescription)) return false;

        final MethodDescription method = (MethodDescription) o;

        if (!_classDescription.equals(method._classDescription)) return false;
        if (!_methodName.equals(method._methodName)) return false;
        for (ListIterator iterator = _argTypes.listIterator(); iterator.hasNext(); ) {
            String argType = (String) iterator.next();
            if (!argType.equals(method._argTypes.get(iterator.previousIndex())))
                return false;
        }

        return true;
    }

    public int hashCode() {
        if (_hashCode == -1) {
            _hashCode = _classDescription.hashCode();
            _hashCode = 29 * _hashCode + _methodName.hashCode();
            for (Iterator iterator = _argTypes.iterator(); iterator.hasNext(); ) {
                String argType = (String) iterator.next();
                _hashCode = 29 * _hashCode + argType.hashCode();
            }
        }
        return _hashCode;
    }

    static MethodDescription createMethodDescription(ClassDescription classDescription,
                                                     List attributes, String methodName,
                                                     String returnType,
                                                     List argNames, List argTypes) {
        return new MethodDescription(classDescription, attributes, methodName, returnType, argNames, argTypes);
    }

    static MethodDescription createConstructorDescription(ClassDescription classDescription,
                                                          List attributes, List argNames,
                                                          List argTypes) {
        return new MethodDescription(classDescription, attributes, Constants.CONSTRUCTOR_METHOD_NAME,
                classDescription.getClassName(), argNames, argTypes);
    }

    static MethodDescription createLambdaDescription(ClassDescription classDescription,
                                                     List argNames, List argTypes, String returnType) {
        return new MethodDescription(classDescription,
                new ArrayList(), Constants.Lambda_Invoke, returnType, argNames, argTypes);
    }
}
