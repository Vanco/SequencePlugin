package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * &copy; fanhuagang@gmail.com
 * Created by van on 10/12/2016.
 */
public interface SequencePluginIcons {
    //svg can't display
    Icon SEQUENCE_ICON = icon("/icons/sequence.svg");
    Icon SEQUENCE_ICON_13 = icon("/icons/sequence_13.svg");

    Icon EXPORT_ICON = icon("/icons/image.svg");
    Icon PLAY_ICON = icon("/icons/play.svg");
    Icon SAVE_ICON = icon("/icons/save.svg");
    Icon OPEN_ICON = icon("/icons/folder.svg");
    Icon PUML_ICON = icon("/icons/puml.svg");
    Icon SETTING_ICON = icon("/icons/settings.svg");

    static Icon icon(String path) {return IconLoader.getIcon(path, SequencePluginIcons.class);}
}
