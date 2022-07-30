package org.intellij.sequencer.formatter;

import org.intellij.sequencer.openapi.Constants;
import org.intellij.sequencer.config.SequenceSettingsState;
import org.intellij.sequencer.openapi.model.CallStack;
import org.intellij.sequencer.openapi.model.MethodDescription;

/**
 * Generate <a href="https://plantuml.com/sequence-diagram">PlantUml sequence diagram</a> format.
 *
 */
public class PlantUMLFormatter implements IFormatter{
    @Override
    public String format(CallStack callStack) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("@startuml").append('\n');
        buffer.append("participant Actor").append('\n');
        String classA = callStack.getMethod().getClassDescription().getClassShortName();
        String method = getMethodName(callStack.getMethod());
        if (Constants.CONSTRUCTOR_METHOD_NAME.equals(callStack.getMethod().getMethodName())) {
            buffer.append("create ").append(classA).append('\n');
        }
        buffer.append("Actor").append(" -> ").append(classA).append(" : ").append(method).append('\n');
        buffer.append("activate ").append(classA).append('\n');
        generate(buffer, callStack);
        buffer.append("return").append('\n');
        buffer.append("@enduml");
        return buffer.toString();
    }

    private void generate(StringBuffer buffer, CallStack parent) {
        String classA = parent.getMethod().getClassDescription().getClassShortName();

        for (CallStack callStack : parent.getCalls()) {
            String classB = callStack.getMethod().getClassDescription().getClassShortName();
            String method = getMethodName(callStack.getMethod());
            if (Constants.CONSTRUCTOR_METHOD_NAME.equals(callStack.getMethod().getMethodName())) {
                buffer.append("create ").append(classB).append('\n');
            }
            buffer.append(classA).append(" -> ").append(classB).append(" : ").append(method).append('\n');
            buffer.append("activate ").append(classB).append('\n');
            generate(buffer, callStack);
            buffer.append(classB).append(" --> ").append(classA).append('\n');
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
