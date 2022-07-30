package org.intellij.sequencer.openapi.model;

import org.intellij.sequencer.openapi.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 2020/3/22.
 */
public class LambdaExprDescription extends MethodDescription {
    private final String _enclosedMethodName;
    private final List<String> _enclosedMethodArgTypes;

    public LambdaExprDescription(MethodDescription methodDescription, String returnType, List<String> argNames, List<String> argTypes, int offset) {
        super(methodDescription.getClassDescription(), new ArrayList<>(), Constants.Lambda_Invoke,returnType, argNames, argTypes, offset);
        this._enclosedMethodName = methodDescription.getMethodName();
        this._enclosedMethodArgTypes = methodDescription.getArgTypes();
    }

    public String getEnclosedMethodName() {
        return _enclosedMethodName;
    }

    public List<String> getEnclosedMethodArgTypes() {
        return _enclosedMethodArgTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LambdaExprDescription that = (LambdaExprDescription) o;
        return Objects.equals(_enclosedMethodName, that._enclosedMethodName) && Objects.equals(_enclosedMethodArgTypes, that._enclosedMethodArgTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), _enclosedMethodName, _enclosedMethodArgTypes);
    }
}
