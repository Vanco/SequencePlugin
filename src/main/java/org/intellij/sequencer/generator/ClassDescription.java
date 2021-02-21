package org.intellij.sequencer.generator;

import org.intellij.sequencer.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ClassDescription {
    private String _className;
    private List<String> _attributes;

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

    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof ClassDescription)) return false;

        final ClassDescription classDescription = (ClassDescription)o;

        return _className.equals(classDescription._className);
    }

    public int hashCode() {
        return _className.hashCode();
    }

    public static ClassDescription ANONYMOUS_CLASS = new ClassDescription(Constants.ANONYMOUS_CLASS_NAME,new ArrayList<>());

    public static ClassDescription TOP_LEVEL_FUN = new ClassDescription(Constants.TOP_LEVEL_FUN, new ArrayList<>());
}
