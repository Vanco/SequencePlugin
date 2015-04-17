package org.intellij.sequencer.diagram;

public interface SequenceListener {
    void selectedScreenObject(ScreenObject screenObject);

    void displayMenuForScreenObject(ScreenObject screenObject, int x, int y);
}
