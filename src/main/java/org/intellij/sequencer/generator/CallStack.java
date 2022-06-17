package org.intellij.sequencer.generator;

import org.apache.commons.lang.StringEscapeUtils;
import org.intellij.sequencer.Constants;
import org.intellij.sequencer.config.SequenceSettingsState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CallStack {
    private final MethodDescription _method;
    private CallStack _parent;
    private final List<CallStack> _calls = new ArrayList<>();

    public CallStack(@NotNull MethodDescription method) {
        _method = method;
    }

    public CallStack(@NotNull MethodDescription method, CallStack parent) {
        _method = method;
        _parent = parent;
    }

    public CallStack methodCall(@NotNull MethodDescription method) {
        CallStack callStack = new CallStack(method, this);
        _calls.add(callStack);
        return callStack;
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

    public List<CallStack> getCalls() {
        return _calls;
    }

    private void generate(StringBuffer buffer) {
        buffer.append('(').append('\n').append(_method.toJson()).append('\n');
        for(Iterator<CallStack> iterator = _calls.iterator(); iterator.hasNext();) {
            CallStack callStack = iterator.next();
            callStack.generate(buffer);
        }
        buffer.append(')').append('\n');
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
        String method = getMethodName(_method);
        if (Constants.CONSTRUCTOR_METHOD_NAME.equals(_method.getMethodName())) {
            buffer.append("create ").append(classA).append('\n');
        }
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
            String method = getMethodName(callStack.getMethod());
            if (Constants.CONSTRUCTOR_METHOD_NAME.equals(callStack.getMethod().getMethodName())) {
                buffer.append("create ").append(classB).append('\n');
            }
            buffer.append(classA).append(" -> ").append(classB).append(" : ").append(method).append('\n');
            buffer.append("activate ").append(classB).append('\n');
            callStack.generatePumlStr(buffer);
            buffer.append(classB).append(" --> ").append(classA).append('\n');
            buffer.append("deactivate ").append(classB).append('\n');
        }

    }

    // Generate mermaid file https://mermaid-js.github.io/mermaid/
    public String generateMmd() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("sequenceDiagram").append('\n');
        buffer.append("actor User").append('\n');
        String classA = _method.getClassDescription().getClassShortName();
        String method = getMethodName(_method);
        buffer.append("User").append(" ->> ").append(classA).append(" : ").append(escape(method)).append('\n');
        buffer.append("activate ").append(classA).append('\n');
        generateMmdStr(buffer);
        buffer.append("deactivate ").append(classA).append('\n');
//        buffer.append("@enduml");
        return buffer.toString();
    }

    private String escape(String method) {
        return StringEscapeUtils.escapeHtml(method);
    }

    private void generateMmdStr(StringBuffer buffer) {
        String classA = _method.getClassDescription().getClassShortName();

        for (CallStack callStack : _calls) {
            String classB = callStack.getMethod().getClassDescription().getClassShortName();
            String method = getMethodName(callStack.getMethod());
            buffer.append(classA).append(" ->> ").append(classB).append(" : ").append(escape(method)).append('\n');
            buffer.append("activate ").append(classB).append('\n');
            callStack.generateMmdStr(buffer);
            buffer.append(classB).append(" -->> ").append(classA).append(" : #32; ").append('\n');
            buffer.append("deactivate ").append(classB).append('\n');
        }

    }

    private String getMethodName(MethodDescription method) {
        if (method == null) return "";

        if (SequenceSettingsState.getInstance().SHOW_SIMPLIFY_CALL_NAME) {
            return method.getMethodName();
        } else {
            return method.getFullName();
        }

    }
}
