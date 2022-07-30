package org.intellij.sequencer.formatter;

import org.apache.commons.lang.StringEscapeUtils;
import org.intellij.sequencer.config.SequenceSettingsState;
import org.intellij.sequencer.openapi.model.CallStack;
import org.intellij.sequencer.openapi.model.MethodDescription;

/**
 * Generate Mermaid Sequence Diagram format.
 *
 * <a href="https://mermaid-js.github.io/mermaid/">Mermaid</a>
 */
public class MermaidFormatter implements IFormatter{
    @Override
    public String format(CallStack callStack) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("sequenceDiagram").append('\n');
        buffer.append("actor User").append('\n');
        String classA = callStack.getMethod().getClassDescription().getClassShortName();
        String method = getMethodName(callStack.getMethod());
        buffer.append("User").append(" ->> ").append(classA).append(" : ").append(escape(method)).append('\n');
        buffer.append("activate ").append(classA).append('\n');
        generate(buffer, callStack);
        buffer.append("deactivate ").append(classA).append('\n');
//        buffer.append("@enduml");
        return buffer.toString();
    }

    private void generate(StringBuffer buffer, CallStack parent) {
        String classA = parent.getMethod().getClassDescription().getClassShortName();

        for (CallStack callStack : parent.getCalls()) {
            String classB = callStack.getMethod().getClassDescription().getClassShortName();
            String method = getMethodName(callStack.getMethod());
            buffer.append(classA).append(" ->> ").append(classB).append(" : ").append(escape(method)).append('\n');
            buffer.append("activate ").append(classB).append('\n');
            generate(buffer, callStack);
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

    private String escape(String method) {
        return StringEscapeUtils.escapeHtml(method);
    }
}
