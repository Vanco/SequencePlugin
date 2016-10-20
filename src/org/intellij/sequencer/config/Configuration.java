package org.intellij.sequencer.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.OptionTag;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

@State(name = "sequencePlugin", storages = {@Storage(file = "sequencePlugin.xml")})
public class Configuration implements PersistentStateComponent<Configuration> {
    @OptionTag(converter = ColorConverter.class)
    public Color CLASS_COLOR = new Color(0xffee00);
    @OptionTag(converter = ColorConverter.class)
    public Color EXTERNAL_CLASS_COLOR = new Color(0xff6666);
    @OptionTag(converter = ColorConverter.class)
    public Color METHOD_BAR_COLOR = new Color(0xD1DEFF);
    @OptionTag(converter = ColorConverter.class)
    public Color SELECTED_METHOD_BAR_COLOR = new Color(0x3399ff);
    @OptionTag(converter = ColorConverter.class)
    public Color INTERFACE_COLOR = new Color(0xC0FAC6);
    public boolean USE_3D_VIEW = false;
    public boolean USE_ANTIALIASING = true;
    public boolean SHOW_RETURN_ARROWS = true;
    public boolean SHOW_CALL_NUMBERS = true;
    public String FONT_NAME = "Dialog";
    public int FONT_SIZE = 11;

    @Transient
    private java.util.List _listeners = new ArrayList();
    private java.util.List<ExcludeEntry> _excludeList = new Vector<ExcludeEntry>();

    public Configuration() {}

    public static Configuration getInstance() {
        return ServiceManager.getService(Configuration.class);
    }

    public void addConfigListener(ConfigListener listener) {
        _listeners.add(listener);
    }

    public void removeConfigListener(ConfigListener listener) {
        _listeners.remove(listener);
    }

    public List<ExcludeEntry> getExcludeList() {
        return _excludeList;
    }

    public void setExcludeList(List<ExcludeEntry> excludeList) {
        this._excludeList = excludeList;
    }

    public void fireConfigChanged() {
        for(Iterator iterator = _listeners.iterator(); iterator.hasNext();) {
            ConfigListener configListener = (ConfigListener)iterator.next();
            configListener.configChanged();
        }
    }

    @Nullable
    @Override
    public Configuration getState() {
        return this;
    }

    @Override
    public void loadState(Configuration configuration) {
        XmlSerializerUtil.copyBean(configuration, this);
    }


}
