package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 10/12/2016.
 */
public interface SequencePluginIcons {
    Icon SEQUENCE_ICON_13 = icon("/icons/sequence-13x13.png");
    Icon SEQUENCE_ICON = icon("/icons/sequence-16x16.png");
    Icon CLOSE_ICON = icon("/icons/close-16x16.png");
    Icon EXPORT_ICON = icon("/icons/export-16x16.png");
    Icon PREVIEW_ICON_13 = icon("/icons/preview-13x13.png");
    Icon PREVIEW_ICON = icon("/icons/preview-16x16.png");
    Icon REFRESH_ICON = icon("/icons/refresh-16x16.png");
    Icon LOCKED_ICON = icon("/icons/locked.png");
    Icon EXPORT_TEXT_ICON = icon("/icons/exporttext-16x16.png");

    static Icon icon(String path) {return IconLoader.getIcon(path, SequencePluginIcons.class);}
}
