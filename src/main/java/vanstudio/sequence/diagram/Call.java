package vanstudio.sequence.diagram;

import vanstudio.sequence.config.SequenceSettingsState;

public class Call extends Link {

    public Call(ObjectInfo from, ObjectInfo to) {
        super(from, to);
    }

    public String getName() {
        if(getMethodInfo() == null)
            return super.getName();
        if(SequenceSettingsState.getInstance().SHOW_CALL_NUMBERS)
            return getMethodInfo().getNumbering().getName() + ':' + super.getName();
        else
            return super.getName();
    }

    public String toString() {
        return "Calling " + getName() + " on " + _to + " from " + _from;
    }
}
