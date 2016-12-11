package org.intellij.sequencer.diagram;

import java.util.EventObject;

public class ModelTextEvent extends EventObject {

    private String _text = null;

    ModelTextEvent(Object source, String text) {
        super(source);
        _text = text;
    }

    public String getText() {
        return _text;
    }
}
