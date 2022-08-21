package org.intellij.sequencer.openapi.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CallStack {
    private final MethodDescription _method;
    private final CallStack _parent;
    private final List<CallStack> _calls = new ArrayList<>();

    public CallStack(@NotNull MethodDescription method) {
        this(method, null);
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

    public boolean isRecursive(MethodDescription method) {
        CallStack current = this;
        while(current != null) {
            if(current._method.equals(method))
                return true;
            current = current._parent;
        }
        return false;
    }

    public MethodDescription getMethod() {
        return _method;
    }

    public List<CallStack> getCalls() {
        return _calls;
    }

    public int level() {
        return _parent == null ? 1 : _parent.level() + 1;
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

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        int deep = 0;
        generateFormatStr(buffer, deep);
        return buffer.toString();
    }

    public static final CallStack EMPTY = new CallStack(MethodDescription.DUMMY_METHOD);

    public boolean isEmpty() {
        return EMPTY.equals(this);
    }
}
