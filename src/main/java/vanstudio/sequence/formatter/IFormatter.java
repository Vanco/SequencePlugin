package vanstudio.sequence.formatter;

import vanstudio.sequence.openapi.model.CallStack;

public interface IFormatter {
    String format(CallStack callStack);
}
