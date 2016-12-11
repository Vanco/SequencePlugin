package org.intellij.sequencer.config;

import com.intellij.ui.JBColor;
import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 4/6/16.
 */
public class ColorConverter extends Converter<Color> {

    public ColorConverter() {}

    @Nullable
    @Override
    public Color fromString(@NotNull String s) {
        int rgb = (int) Long.parseLong(s, 16);
        return new JBColor(new Color(rgb), new Color(rgb));
    }

    @NotNull
    @Override
    public String toString(@NotNull Color color) {
        return Integer.toHexString(color.getRGB());
    }

}