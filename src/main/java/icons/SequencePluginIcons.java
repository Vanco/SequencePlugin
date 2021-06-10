package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 10/12/2016.
 */
public interface SequencePluginIcons {
    //svg can't display
    Icon SEQUENCE_ICON = icon("/icons/sequence.png");

    Icon CLOSE_ICON = icon("/icons/close.svg");
    Icon EXPORT_ICON = icon("/icons/image.svg");
    Icon PREVIEW_ICON = icon("/icons/preview.svg");
    Icon REFRESH_ICON = icon("/icons/refresh.svg");
    Icon LOCKED_ICON = icon("/icons/lock.svg");
    Icon SAVE_ICON = icon("/icons/save.svg");
    Icon OPEN_ICON = icon("/icons/open.svg");
    Icon PUML_ICON = icon("/icons/puml.svg");

    static Icon icon(String path) {return IconLoader.getIcon(path, SequencePluginIcons.class);}
}
