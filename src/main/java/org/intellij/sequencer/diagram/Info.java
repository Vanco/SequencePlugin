package org.intellij.sequencer.diagram;

import java.util.Iterator;
import java.util.List;

public class Info {
    public static final String PRIVATE_ATTRIBUTE = "private";
    public static final String PROTECTED_ATTRIBUTE = "protected";
    public static final String PUBLIC_ATTRIBUTE = "public";
    public static final String PACKAGE_LOCAL_ATTRIBUTE = "packageLocal";
    public static final String ABSTRACT_ATTRIBUTE = "abstract";
    public static final String NATIVE_ATTRIBUTE = "native";
    public static final String SYNCHRONIZED_ATTRIBUTE = "synchronized";
    public static final String STATIC_ATTRIBUTE = "static";
    public static final String FINAL_ATTRIBUTE = "final";
    public static final String STRICTFP_ATTRIBUTE = "strictfp";

    public static final String[] RECOGNIZED_METHOD_ATTRIBUTES =
          {
              PRIVATE_ATTRIBUTE, PUBLIC_ATTRIBUTE, PROTECTED_ATTRIBUTE,
              ABSTRACT_ATTRIBUTE, SYNCHRONIZED_ATTRIBUTE, FINAL_ATTRIBUTE,
              NATIVE_ATTRIBUTE, STATIC_ATTRIBUTE, PACKAGE_LOCAL_ATTRIBUTE,
              STRICTFP_ATTRIBUTE
          };

    public static final String EXTERNAL_ATTRIBUTE = "external";
    /**
     * Indicate a class is an interface.
     */
    public static final String INTERFACE_ATTRIBUTE = "interface";

    protected List<String> _attributes;

    public Info(List<String> attributes) {
        _attributes = attributes;
    }

    public boolean hasAttribute(String attribute) {
        return _attributes.contains(attribute);
    }

    protected String getAttributesStr() {
        StringBuilder buffer = new StringBuilder();
        for(Iterator<String> iterator = _attributes.iterator(); iterator.hasNext();) {
            String attribute = iterator.next();
            buffer.append(attribute);
            if(iterator.hasNext())
                buffer.append(", ");
        }
        return buffer.toString();
    }
}
