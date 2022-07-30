package org.intellij.sequencer.formatter;

import org.intellij.sequencer.openapi.model.CallStack;

public interface IFormatter {
    String format(CallStack callStack);
}
