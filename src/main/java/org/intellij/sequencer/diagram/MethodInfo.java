package org.intellij.sequencer.diagram;

import org.intellij.sequencer.openapi.Constants;
import org.intellij.sequencer.openapi.model.GenericType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MethodInfo extends Info {

    private final ObjectInfo _objectInfo;
    private final Numbering _numbering;
    private final String _name;
    private String _returnType;
    private final List<String> _argNames;
    private final List<String> _argTypes;
    private final int _startSeq;
    private final int _endSeq;
    private String _htmlDescription;

    public MethodInfo(ObjectInfo obj, Numbering numbering, List<String> attributes,
                      String method, String returnType, List<String> argNames, List<String> argTypes,
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
            String argName = _argNames.get(i);
            String argType = _argTypes.get(i);
            argType = shortTypeName(argType);
            sb.append(argName).append(": ").append(argType);
        }
        sb.append(")").append(": ").append(shortTypeName(_returnType));
        return sb.toString();
    }

    @NotNull
    private String shortTypeName(String argType) {
        GenericType genericType = GenericType.create(argType);
        return genericType.getName();
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

    public List<String> getArgNames() {
        return _argNames;
    }

    public List<String> getArgTypes() {
        return _argTypes;
    }

    public String getReturnType() {
        return  _returnType;
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

            buffer.append("<tr><td colspan=2><b>Arguments:</b>");
            if(!getArgNames().isEmpty()) {
                buffer.append("</td>");
                for(int i = 0; i < getArgNames().size(); i++) {
                    appendArgValue(buffer, getArgNames().get(i),
                            getArgTypes().get(i));
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
        buffer.append("<td><em>").append(argName).append("</em></td>");
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
        buffer.append("<td><b>").append(title).append(":</b></td>");
    }

    private void appendValue(StringBuffer buffer, String value) {
        String htmlValue = value.replace("\u003c","&lt;")
                .replace("\u003e","&gt;").replace(",", ",<br>");
        buffer.append("<td>").append(htmlValue).append("</td>");
    }


    public String toString() {
        return "Method " + _name + " on " + _objectInfo + " from " + _startSeq + " to " + _endSeq;
    }
}
