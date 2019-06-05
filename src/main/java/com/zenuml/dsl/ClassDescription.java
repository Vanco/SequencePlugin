package com.zenuml.dsl;

import com.google.gson.GsonBuilder;
import org.intellij.sequencer.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ClassDescription {
    private String _className;
    private List _attributes;

    public ClassDescription(String className, List attributes) {
        _className = className != null ? className : Constants.ANONYMOUS_CLASS_NAME;
        _attributes = attributes;
    }

    public String getClassShortName() {
        return _className.substring(_className.lastIndexOf('.') + 1);
    }

    public String getClassName() {
        return _className;
    }

    public List getAttributes() { return Collections.unmodifiableList(_attributes); }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for(Iterator iterator = _attributes.iterator(); iterator.hasNext();) {
            String attribute = (String)iterator.next();
            buffer.append('|').append(attribute);
        }
        buffer.append("|@").append(_className);
        return buffer.toString();
    }

    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof ClassDescription)) return false;

        final ClassDescription classDescription = (ClassDescription)o;

        if(!_className.equals(classDescription._className)) return false;

        return true;
    }

    public int hashCode() {
        return _className.hashCode();
    }

    public static ClassDescription ANONYMOUS_CLASS = new ClassDescription(Constants.ANONYMOUS_CLASS_NAME,new ArrayList());
}
