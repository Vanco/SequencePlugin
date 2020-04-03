package org.intellij.sequencer.diagram;

import java.util.List;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 2020/3/23.
 */
public class LambdaExprInfo extends MethodInfo {
    private final String _enclosedMethodName;
    private final List<String> _enclosedMethodArgTypes;

    public LambdaExprInfo(ObjectInfo obj, Numbering numbering, List<String> attributes,
                          String method, String returnType, List<String> argNames, List<String> argTypes,
                          int startSeq, int endSeq,
                          String enclosedMethodName, List<String> enclosedMethodArgTypes) {
        super(obj, numbering, attributes, method, returnType, argNames, argTypes, startSeq, endSeq);
        this._enclosedMethodName = enclosedMethodName;
        this._enclosedMethodArgTypes = enclosedMethodArgTypes;
    }

    public String getEnclosedMethodName() {
        return _enclosedMethodName;
    }

    public List<String> getEnclosedMethodArgTypes() {
        return _enclosedMethodArgTypes;
    }
}
