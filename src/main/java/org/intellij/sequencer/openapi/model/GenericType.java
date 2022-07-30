package org.intellij.sequencer.openapi.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericType {
    private static final Pattern PATTERN = Pattern.compile("<.*>");
    private final String original;
    private boolean generic = false;
    private String name;
    private String fullName;
    private GenericType genericType;

    private GenericType(String original) {
        this.original = original;
    }

    public String getName() {
        StringBuilder sb = new StringBuilder(name);
        if (generic) {
            sb.append("<").append(genericType.getName()).append(">");
        }
        return sb.toString();
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder(fullName);
        if (generic) {
            sb.append("<").append(genericType.getFullName()).append(">");
        }
        return sb.toString();
    }

    public static GenericType create(String original) {
        GenericType type = new GenericType(original);
        type.build();
        return type;
    }

    private void build() {
        Matcher matcher = PATTERN.matcher(original);
        generic = matcher.find();
        if (generic) {
            fullName = original.substring(0, matcher.start());
            genericType = create(original.substring(matcher.start() +1, matcher.end() -1));
        } else {
            fullName = original;
        }
        name = shortName(fullName);
    }

    /** Return the last part of a qualified name from its string representation
     *  @param name the string representation of the qualified name
     *  @return the last part of the qualified name
     */
    private static String shortName(String name) {
        return name.substring(name.lastIndexOf('.') + 1);
    }
}
