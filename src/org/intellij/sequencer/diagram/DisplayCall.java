package org.intellij.sequencer.diagram;

public class DisplayCall extends DisplayLink {

    DisplayCall(Link link, DisplayObject from, DisplayObject to, int seq) {
        super(link, from, to, seq);
        from.addCall(this);
    }

    public String toString() {
        return "DisplayCall " + _link.getName() + " from " + _from + " to " + _to + " seq " + _seq;
    }
}

