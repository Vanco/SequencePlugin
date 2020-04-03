package org.intellij.sequencer.generator;

import org.intellij.sequencer.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 2020/3/22.
 */
public class LambdaExprDescription extends MethodDescription {
    private final String _enclosedMethodName;
    private final List<String> _enclosedMethodArgTypes;

    public LambdaExprDescription(MethodDescription methodDescription, String returnType, List<String> argNames, List<String> argTypes) {
        super(methodDescription.getClassDescription(), new ArrayList<>(), Constants.Lambda_Invoke,returnType, argNames, argTypes);
        this._enclosedMethodName = methodDescription.getMethodName();
        this._enclosedMethodArgTypes = methodDescription.getArgTypes();
    }

    public String getEnclosedMethodName() {
        return _enclosedMethodName;
    }

    public List<String> getEnclosedMethodArgTypes() {
        return _enclosedMethodArgTypes;
    }
}
