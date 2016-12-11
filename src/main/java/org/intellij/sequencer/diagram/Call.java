package org.intellij.sequencer.diagram;

import org.intellij.sequencer.config.Configuration;

public class Call extends Link {

    public Call(ObjectInfo from, ObjectInfo to) {
        super(from, to);
    }

    public String getName() {
        if(getMethodInfo() == null)
            return super.getName();
        if(Configuration.getInstance().SHOW_CALL_NUMBERS)
            return getMethodInfo().getNumbering().getName() + ':' + super.getName();
        else
            return super.getName();
    }

    public String toString() {
        return "Calling " + getName() + " on " + _to + " from " + _from;
    }
}
