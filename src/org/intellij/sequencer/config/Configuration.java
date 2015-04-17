package org.intellij.sequencer.config;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.intellij.sequencer.SequencePlugin;
import org.jdom.Element;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class Configuration implements ApplicationComponent, JDOMExternalizable, Configurable {
    public Color CLASS_COLOR = new Color(0xcccc00);
    public Color EXTERNAL_CLASS_COLOR = new Color(0xff6666);
    public Color METHOD_BAR_COLOR = new Color(0x9999ff);
    public Color SELECTED_METHOD_BAR_COLOR = new Color(0x3399ff);
    public boolean USE_3D_VIEW = false;
    public boolean USE_ANTIALIASING = true;
    public boolean SHOW_RETURN_ARROWS = true;
    public boolean SHOW_CALL_NUMBERS = true;
    public String FONT_NAME = "Dialog";
    public int FONT_SIZE = 11;

    private ConfigurationUI _configurationUI;
    private java.util.List _listeners = new ArrayList();
    private java.util.List _excludeList = new Vector();

    private static Configuration _instance;

    private static final String EXCLUDE_ENTRY_ELEMENT = "excludeEntry";
    private static final String EXCLUDE_NAME_ELEMENT = "excludeName";
    private static final String ENABLED_ELEMENT = "enabled";
    private static final String EXCLUDE_LIST_ELEMENT = "excludeList";

    public static Configuration getInstance() {
        Application application = ApplicationManager.getApplication();
        if(application == null) {
            if(_instance == null)
                _instance = new Configuration();
            return _instance;
        }
        return (Configuration)application.getComponent(Configuration.class);
    }

    public void readExternal(Element element) throws InvalidDataException {
        DefaultJDOMExternalizer.readExternal(this, element);
        _excludeList.clear();
        Element excludeListElement = element.getChild(EXCLUDE_LIST_ELEMENT);
        if(excludeListElement == null)
            return;
        List children = excludeListElement.getChildren(EXCLUDE_ENTRY_ELEMENT);
        for(Iterator iterator = children.iterator(); iterator.hasNext();) {
            Element excludeElement = (Element)iterator.next();
            _excludeList.add(new ExcludeEntry(excludeElement.getChildText(EXCLUDE_NAME_ELEMENT),
                  Boolean.valueOf(excludeElement.getChildText(ENABLED_ELEMENT)).booleanValue()));
        }
    }

    public void writeExternal(Element element) throws WriteExternalException {
        DefaultJDOMExternalizer.writeExternal(this, element);
        Element excludeElement = new Element(EXCLUDE_LIST_ELEMENT);
        for(Iterator iterator = _excludeList.iterator(); iterator.hasNext();) {
            ExcludeEntry excludeEntry = (ExcludeEntry)iterator.next();
            Element excludeEntryElement = new Element(EXCLUDE_ENTRY_ELEMENT);
            excludeElement.addContent(excludeEntryElement);
            Element excludeNameElement = new Element(EXCLUDE_NAME_ELEMENT);
            excludeEntryElement.addContent(excludeNameElement);
            excludeNameElement.setText(excludeEntry.getExcludeName());
            Element enabledElement = new Element(ENABLED_ELEMENT);
            excludeEntryElement.addContent(enabledElement);
            enabledElement.setText(String.valueOf(excludeEntry.isEnabled()));
        }
        element.addContent(excludeElement);
    }

    public String getComponentName() {
        return "SequencePlugin.Configuration";
    }

    public void initComponent() {
        _configurationUI = new ConfigurationUI();
    }

    public void disposeComponent() {
    }

    public String getDisplayName() {
        return "Sequence";
    }

    public Icon getIcon() {
        return SequencePlugin.loadIcon("SequenceDiagram.gif");
    }

    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        return _configurationUI.getMainPanel();
    }

    public boolean isModified() {
        return _configurationUI.isModified(this);
    }

    public void apply() throws ConfigurationException {
        _configurationUI.apply(this);
        fireConfigChanged();
    }

    public void reset() {
        _configurationUI.reset(this);
    }

    public void disposeUIResources() {
    }

    public void addConfigListener(ConfigListener listener) {
        _listeners.add(listener);
    }

    public void removeConfigListener(ConfigListener listener) {
        _listeners.remove(listener);
    }

    private void fireConfigChanged() {
        for(Iterator iterator = _listeners.iterator(); iterator.hasNext();) {
            ConfigListener configListener = (ConfigListener)iterator.next();
            configListener.configChanged();
        }
    }

    public static void main(String[] args) {
        Configuration configuration = getInstance();
        configuration.initComponent();
        JFrame jFrame = new JFrame();
        jFrame.getContentPane().add(configuration.createComponent());
        jFrame.pack();
        jFrame.setVisible(true);
        jFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    public List getExcludeList() {
        return _excludeList;
    }

    public void setExcludeList(List excludeList) {
        this._excludeList = excludeList;
    }
}
