package org.intellij.sequencer.openapi.model;

import org.intellij.sequencer.openapi.Constants;

import java.util.*;

public class ClassDescription {
    private final String _className;
    private final List<String> _attributes;

    public ClassDescription(String className, List<String> attributes) {
        _className = className != null ? className : Constants.ANONYMOUS_CLASS_NAME;
        _attributes = attributes;
    }

    public String getClassShortName() {
        return _className.substring(_className.lastIndexOf('.') + 1);
    }

    public String getClassName() {
        return _className;
    }

    public List<String> getAttributes() { return Collections.unmodifiableList(_attributes); }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        for (String attribute : _attributes) {
            buffer.append('|').append(attribute);
        }
        buffer.append("|@").append(_className);
        return buffer.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassDescription that = (ClassDescription) o;
        return Objects.equals(_className, that._className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_className);
    }

    public static ClassDescription ANONYMOUS_CLASS = new ClassDescription(Constants.ANONYMOUS_CLASS_NAME,new ArrayList<>());

    public static ClassDescription TOP_LEVEL_FUN = new ClassDescription(Constants.TOP_LEVEL_FUN, new ArrayList<>());

    public static ClassDescription getFileNameAsClass(String filename) {
        return new ClassDescription(filename, new ArrayList<>());
    }
}
