package org.intellij.sequencer.formatter;

import org.intellij.sequencer.openapi.model.CallStack;

public class SdtFormatter implements IFormatter{
    @Override
    public String format(CallStack callStack) {
        StringBuffer buffer = new StringBuffer();
        generate(buffer, callStack);
        return buffer.toString();
    }

    private void generate(StringBuffer buffer, CallStack parent) {
        buffer.append('(').append('\n').append(parent.getMethod().toJson()).append('\n');
        for (CallStack callStack : parent.getCalls()) {
            generate(buffer, callStack);
        }
        buffer.append(')').append('\n');
    }
}
