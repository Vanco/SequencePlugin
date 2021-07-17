package org.intellij.sequencer.generator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CallStack {
    private final MethodDescription _method;
    private CallStack _parent;
    private final List<CallStack> _calls = new ArrayList<>();

    public CallStack(MethodDescription method) {
        _method = method;
    }

    public CallStack(MethodDescription method, CallStack parent) {
        _method = method;
        _parent = parent;
    }

    public CallStack methodCall(MethodDescription method) {
        CallStack callStack = new CallStack(method, this);
        _calls.add(callStack);
        return callStack;
    }

    public CallStack merge(CallStack callStack) {
        callStack.setParent(this);
        _calls.add(callStack);
        return this;
    }

    private void setParent(CallStack callStack) {
        this._parent = callStack;
    }

    public boolean isRecursive(MethodDescription method) {
        CallStack current = this;
        while(current != null) {
            if(current._method.equals(method))
                return true;
            current = current._parent;
        }
        return false;
    }

    public String generateSequence() {
        StringBuffer buffer = new StringBuffer();
        generate(buffer);
        return buffer.toString();
    }

    public MethodDescription getMethod() {
        return _method;
    }

    private void generate(StringBuffer buffer) {
        buffer.append('(').append(_method.toJson()).append(' ');
        for(Iterator<CallStack> iterator = _calls.iterator(); iterator.hasNext();) {
            CallStack callStack = iterator.next();
            callStack.generate(buffer);
            if(iterator.hasNext()) {
                buffer.append(' ');
            }
        }
        buffer.append(')');
    }

    public String generateText() {
        StringBuffer buffer = new StringBuffer();
        int deep = 0;
        generateFormatStr(buffer, deep);
        return buffer.toString();
    }

    private void generateFormatStr(StringBuffer buffer, int deep) {
        for (int i = 0; i< deep; i ++) {
            buffer.append("    ");
        }
        buffer.append(_method.toJson()).append('\n');
        for (CallStack callStack : _calls) {
            callStack.generateFormatStr(buffer, deep + 1);
        }
    }

    // Generate platUML file
    public String generatePuml() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("@startuml").append('\n');
        buffer.append("participant Actor").append('\n');
        String classA = _method.getClassDescription().getClassShortName();
        String method = _method.getMethodName();
        buffer.append("Actor").append(" -> ").append(classA).append(" : ").append(method).append('\n');
        buffer.append("activate ").append(classA).append('\n');
        generatePumlStr(buffer);
        buffer.append("return").append('\n');
        buffer.append("@enduml");
        return buffer.toString();
    }

    private void generatePumlStr(StringBuffer buffer) {
        String classA = _method.getClassDescription().getClassShortName();

        for (CallStack callStack : _calls) {
            String classB = callStack.getMethod().getClassDescription().getClassShortName();
            String method = callStack.getMethod().getMethodName();
            buffer.append(classA).append(" -> ").append(classB).append(" : ").append(method).append('\n');
            buffer.append("activate ").append(classB).append('\n');
            callStack.generatePumlStr(buffer);
            buffer.append(classB).append(" --> ").append(classA).append('\n');
            buffer.append("deactivate ").append(classB).append('\n');
        }

    }

}
