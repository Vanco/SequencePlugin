package org.intellij.sequencer.config;

import com.intellij.util.xmlb.annotations.OptionTag;
import com.intellij.util.xmlb.annotations.Tag;

import java.awt.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Tag("colorMapEntry")
public class ColorMapEntry {
    private String regex;
    @OptionTag(converter = ColorConverter.class)
    private Color color;

    public ColorMapEntry() {}

    public ColorMapEntry(String regex, Color color) {
        this.regex = regex;
        this.color = color;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        matcher = null; // invalidate
        this.regex = regex;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    private Predicate<String> matcher;

    public boolean matches(String fullName) {
        if(matcher==null) {
            // simply pattern to actual regex
            String regex = getRegex().replace(".", "\\.").replace("*", ".*");
            matcher = Pattern.compile(regex).asMatchPredicate();
        }
        if(fullName==null) {
            return false;
        }
        return matcher.test(fullName);
    }
}
