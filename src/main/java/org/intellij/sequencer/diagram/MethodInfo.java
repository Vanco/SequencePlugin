package org.intellij.sequencer.diagram;

import org.intellij.sequencer.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MethodInfo extends Info {

    private ObjectInfo _objectInfo;
    private Numbering _numbering;
    private String _name;
    private String _returnType;
    private List _argNames;
    private List _argTypes;
    private int _startSeq;
    private int _endSeq;
    private String _htmlDescription;

    public MethodInfo(ObjectInfo obj, Numbering numbering, List attributes,
                      String method, String returnType, List argNames, List argTypes,
                      int startSeq, int endSeq) {
        super(attributes);
        _objectInfo = obj;
        _numbering = numbering;
        _name = method;
        _returnType = returnType;
        if(_returnType == null)
            _returnType = "void";
        _argNames = argNames;
        _argTypes = argTypes;
        _startSeq = startSeq;
        _endSeq = endSeq;
    }

    public String getName() {
        return Constants.CONSTRUCTOR_METHOD_NAME.equals(_name)? "<<create>>" : _name;
    }

    public String getRealName() {
        return Constants.CONSTRUCTOR_METHOD_NAME.equals(_name)? _objectInfo.getName() : _name;
    }

    public String getFullName() {
        String name = Constants.CONSTRUCTOR_METHOD_NAME.equals(_name) ? "<<create>>" : _name;
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("(");
        for (int i = 0; i < _argNames.size(); i++) {
            if (i > 0) sb.append(", ");
            String argName = (String) _argNames.get(i);
            String argType = (String) _argTypes.get(i);
            argType = shortTypeName(argType);
            sb.append(argName).append(": ").append(argType);
        }
        sb.append(")").append(": ").append(shortTypeName(_returnType));
        return sb.toString();
    }

    @NotNull
    private String shortTypeName(String argType) {
        String result = argType;
        int idx = argType.lastIndexOf(".");
        if (idx != -1) result = argType.substring(idx + 1);
        return result;
    }

    public ObjectInfo getObjectInfo() {
        return _objectInfo;
    }

    public Numbering getNumbering() {
        return _numbering;
    }

    public String getHtmlName() {
        return Constants.CONSTRUCTOR_METHOD_NAME.equals(_name)? "&lt;constructor&gt;": _name;
    }

    public List getArgNames() {
        return _argNames;
    }

    public List getArgTypes() {
        return _argTypes;
    }

    public int getStartSeq() {
        return _startSeq;
    }

    public int getEndSeq() {
        return _endSeq;
    }

    public String getHtmlDescription() {
        if(_htmlDescription == null) {
            StringBuffer buffer = new StringBuffer();

            buffer.append("<html><table border=0>");

            appendTitleValue(buffer, "Class", _objectInfo.getName());
            appendTitleValue(buffer, "Package", _objectInfo.getHtmlPackageName());
            appendTitleValue(buffer, "Method", getHtmlName());
            appendTitleValue(buffer, "Returns", _returnType);
            if(_attributes.size() != 0) {
                appendTitleValue(buffer, "Modifiers", getAttributesStr());
            }
            appendTitleValue(buffer, "No", getNumbering().getName());

            buffer.append("<tr><td colspan=2><b><font color=black>Arguments:</font></b>");
            if(!getArgNames().isEmpty()) {
                buffer.append("</td>");
                for(int i = 0; i < getArgNames().size(); i++) {
                    appendArgValue(buffer, (String)getArgNames().get(i),
                          (String)getArgTypes().get(i));
                }
            }
            else
                buffer.append("&nbsp;<i>None</i></td>");
            buffer.append("</table></html>");
            _htmlDescription = buffer.toString();
        }
        return _htmlDescription;
    }

    private void appendArgValue(StringBuffer buffer, String argName, String argType) {
        buffer.append("<tr>");
        buffer.append("<td><font color=blue><em>").append(argName).append("</em></font></td>");
        appendValue(buffer, argType);
        buffer.append("</tr>");

    }

    private void appendTitleValue(StringBuffer buffer, String title, String value) {
        buffer.append("<tr>");
        appendTitle(buffer, title);
        appendValue(buffer, value);
        buffer.append("</tr>");
    }

    private void appendTitle(StringBuffer buffer, String title) {
        buffer.append("<td><b><font color=black>" + title + ":</font></b></td>");
    }

    private void appendValue(StringBuffer buffer, String value) {
        buffer.append("<td><font color=blue>").append(value).append("</font></td>");
    }


    public String toString() {
        return "Method " + _name + " on " + _objectInfo + " from " + _startSeq + " to " + _endSeq;
    }
}
