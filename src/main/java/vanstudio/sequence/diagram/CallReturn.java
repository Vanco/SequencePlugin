package vanstudio.sequence.diagram;

public class CallReturn extends Link {

    public CallReturn(ObjectInfo from, ObjectInfo to) {
        super(from, to);
    }

    public String getName() {
        // todo should return methodinfo return type name
        return "";
    }

    public String toString() {
        return "Returning " + getName() + " on " + _from + " to " + _to;
    }
}
