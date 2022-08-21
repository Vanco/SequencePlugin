package org.intellij.sequencer.openapi.model;

import com.google.gson.GsonBuilder;
import org.intellij.sequencer.openapi.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MethodDescription {
    private final ClassDescription _classDescription;

    private final String _methodName;
    private final List<String> _attributes;
    private final List<String> _argNames;
    private final List<String> _argTypes;
    private final String _returnType;
    private final int offset;

    protected MethodDescription(ClassDescription classDescription, List<String> attributes,
                                String methodName, String returnType, List<String> argNames, List<String> argTypes, int offset) {
        _attributes = attributes;
        _returnType = returnType;
        _argNames = argNames;
        _argTypes = argTypes;
        _classDescription = classDescription;
        _methodName = methodName;
        this.offset = offset;
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
                getMethodName() ;
    }

    public String getFullName() {
        String name = Constants.CONSTRUCTOR_METHOD_NAME.equals(_methodName) ? "<<create>>" : _methodName;
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("(");
        for (int i = 0; i < _argNames.size(); i++) {
            if (i > 0) sb.append(", ");
            String argName = _argNames.get(i);
            String argType = _argTypes.get(i);
            argType = shortTypeName(argType);
            sb.append(argName).append(": ").append(argType);
        }
        sb.append(")").append(": ").append(shortTypeName(_returnType));
        return sb.toString();
    }

    @NotNull
    private String shortTypeName(String argType) {
        GenericType genericType = GenericType.create(argType);
        return genericType.getName();
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
        return Objects.equals(_classDescription, that._classDescription)
                && Objects.equals(_methodName, that._methodName)
                && Objects.equals(_argTypes, that._argTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_classDescription, _methodName, _argTypes);
    }


    public static MethodDescription createMethodDescription(ClassDescription classDescription,
                                                     List<String> attributes, String methodName,
                                                     String returnType,
                                                     List<String> argNames, List<String> argTypes, int offset) {
        return new MethodDescription(classDescription, attributes, methodName, returnType, argNames, argTypes, offset);
    }

    public static MethodDescription createConstructorDescription(ClassDescription classDescription,
                                                          List<String> attributes, List<String> argNames,
                                                          List<String> argTypes, int offset) {
        return new MethodDescription(classDescription, attributes, Constants.CONSTRUCTOR_METHOD_NAME,
                classDescription.getClassName(), argNames, argTypes, offset);
    }

    public int getOffset() {
        return offset;
    }

//    static MethodDescription createLambdaDescription(ClassDescription classDescription,
//                                                     List<String> argNames, List<String> argTypes, String returnType) {
//        return new MethodDescription(classDescription,
//                new ArrayList<>(), Constants.Lambda_Invoke, returnType, argNames, argTypes);
//    }

    public static final MethodDescription DUMMY_METHOD = new MethodDescription(ClassDescription.ANONYMOUS_CLASS, new ArrayList<>(), "dummy", "",new ArrayList<>(), new ArrayList<>(), 0);
}
