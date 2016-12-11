package org.intellij.sequencer.diagram;

import java.util.EventListener;

public interface ModelTextListener extends EventListener {

    public void modelTextChanged(ModelTextEvent mte);
}

