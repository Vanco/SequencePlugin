package org.intellij.sequencer.diagram;

public class DisplaySelfCall extends DisplaySelfLink {

    DisplaySelfCall(Link link, DisplayObject from, DisplayObject to, int seq) {
        super(link, from, to, seq);
        from.addCall(this);
    }

    public String toString() {
        return "DisplaySelfCall " + _link.getName() + " from " + _from + " to " + _to + " seq " + _seq;
    }
}

