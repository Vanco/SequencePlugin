package org.intellij.sequencer.config;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class ConfigurationUI implements ActionListener {
    public static final String[] FONT_SIZES =
          {"8", "9", "10", "11", "12", "13", "14",
           "16", "18", "20", "24", "28", "32", "48", "64"};

    private JPanel _mainPanel;
    private JButton _classColor;
    private JButton _externalClassColor;
    private JButton _methodBarColor;
    private JButton _selectedMethodBarColor;
    private JComboBox<String> _fontName;
    private JComboBox<String> _fondSize;
    private JCheckBox _antialiasing;
    private JCheckBox _use3dView;
    private JCheckBox _showReturnArrows;
    private JCheckBox _showCallNumbers;
    private JButton _addExcludeEntry;
    private JButton _removeExcludeEntry;
    private JTable _excludeTable;
    private JButton _interfaceColor;
    private JCheckBox _showSimplifyCallName;

    private ExcludeTableModel _excludeTableModel;

    public ConfigurationUI() {
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = environment.getAvailableFontFamilyNames();
        for (String font : fonts) {
            _fontName.addItem(font);
        }
        for (String fontSize : FONT_SIZES) {
            _fondSize.addItem(fontSize);
        }
        _classColor.addActionListener(this);
        _externalClassColor.addActionListener(this);
        _methodBarColor.addActionListener(this);
        _selectedMethodBarColor.addActionListener(this);
        _interfaceColor.addActionListener(this);
        _addExcludeEntry.addActionListener(this);
        _removeExcludeEntry.addActionListener(this);

        _excludeTableModel = new ExcludeTableModel();
        _excludeTable.setModel(_excludeTableModel);
        _excludeTable.getTableHeader().getColumnModel().getColumn(1).setMaxWidth(100);
    }

    private void handleColor(JButton colorButton) {
        Color newColor = JColorChooser.showDialog(_mainPanel, colorButton.getText(),
              colorButton.getBackground());
        if(newColor != null)
            colorButton.setBackground(newColor);
    }

    public JPanel getMainPanel() {
        return _mainPanel;
    }

    public boolean isModified(Configuration configuration) {
        if(!_classColor.getBackground().equals(configuration.CLASS_COLOR))
            return true;
        if(!_externalClassColor.getBackground().equals(configuration.EXTERNAL_CLASS_COLOR))
            return true;
        if(!_methodBarColor.getBackground().equals(configuration.METHOD_BAR_COLOR))
            return true;
        if(!_selectedMethodBarColor.getBackground().equals(configuration.SELECTED_METHOD_BAR_COLOR))
            return true;
        if(!_interfaceColor.getBackground().equals(configuration.INTERFACE_COLOR))
            return true;
        if(configuration.USE_ANTIALIASING != _antialiasing.isSelected())
            return true;
        if(configuration.SHOW_RETURN_ARROWS != _showReturnArrows.isSelected())
            return true;
        if(configuration.SHOW_CALL_NUMBERS != _showCallNumbers.isSelected())
            return true;
        if(configuration.SHOW_SIMPLIFY_CALL_NAME != _showSimplifyCallName.isSelected())
            return true;
        if(configuration.USE_3D_VIEW != _use3dView.isSelected())
            return true;
        if(!Objects.equals(_fontName.getSelectedItem(), configuration.FONT_NAME))
            return true;
        if(configuration.FONT_SIZE != Integer.parseInt((String) Objects.requireNonNull(_fondSize.getSelectedItem())))
            return true;
        if(_excludeTableModel.isChanged())
            return true;
        return false;
    }

    public void apply(Configuration configuration) {
        configuration.CLASS_COLOR = _classColor.getBackground();
        configuration.EXTERNAL_CLASS_COLOR = _externalClassColor.getBackground();
        configuration.METHOD_BAR_COLOR = _methodBarColor.getBackground();
        configuration.SELECTED_METHOD_BAR_COLOR = _selectedMethodBarColor.getBackground();
        configuration.INTERFACE_COLOR = _interfaceColor.getBackground();
        configuration.USE_ANTIALIASING = _antialiasing.isSelected();
        configuration.SHOW_RETURN_ARROWS = _showReturnArrows.isSelected();
        configuration.SHOW_CALL_NUMBERS = _showCallNumbers.isSelected();
        configuration.SHOW_SIMPLIFY_CALL_NAME = _showSimplifyCallName.isSelected();
        configuration.USE_3D_VIEW = _use3dView.isSelected();
        configuration.FONT_NAME = (String)_fontName.getSelectedItem();
        configuration.FONT_SIZE = Integer.parseInt(((String) Objects.requireNonNull(_fondSize.getSelectedItem())));
        configuration.setExcludeList(_excludeTableModel.getExcludeList());
    }

    public void reset(Configuration configuration) {
        _classColor.setBackground(configuration.CLASS_COLOR);
        _externalClassColor.setBackground(configuration.EXTERNAL_CLASS_COLOR);
        _methodBarColor.setBackground(configuration.METHOD_BAR_COLOR);
        _selectedMethodBarColor.setBackground(configuration.SELECTED_METHOD_BAR_COLOR);
        _interfaceColor.setBackground(configuration.INTERFACE_COLOR);
        _antialiasing.setSelected(configuration.USE_ANTIALIASING);
        _showReturnArrows.setSelected(configuration.SHOW_RETURN_ARROWS);
        _showCallNumbers.setSelected(configuration.SHOW_CALL_NUMBERS);
        _showSimplifyCallName.setSelected(configuration.SHOW_SIMPLIFY_CALL_NAME);
        _use3dView.setSelected(configuration.USE_3D_VIEW);
        _fontName.setSelectedItem(configuration.FONT_NAME);
        _fondSize.setSelectedItem(String.valueOf(configuration.FONT_SIZE));
        _excludeTableModel.setExcludeList(new ArrayList<>(configuration.getExcludeList()));
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("classColor"))
            handleColor(_classColor);
        else if(e.getActionCommand().equals("externalClassColor"))
            handleColor(_externalClassColor);
        else if(e.getActionCommand().equals("methodBarColor"))
            handleColor(_methodBarColor);
        else if(e.getActionCommand().equals("selectedMethodBarColor"))
            handleColor(_selectedMethodBarColor);
        else if(e.getActionCommand().equals("interfaceColor"))
            handleColor(_interfaceColor);
        else if(e.getActionCommand().equals("addExcludeEntry")) {
            String excludeName = JOptionPane.showInputDialog(_mainPanel,
                  "Enter package or class name.\nFor example, java.lang.* or java.io.PrintStream:", "Exclude Entry",
                  JOptionPane.PLAIN_MESSAGE);
            if(excludeName != null && excludeName.trim().length() != 0)
                _excludeTableModel.addExcludeEntry(excludeName, true);
        }
        else if(e.getActionCommand().equals("removeExcludeEntry")) {
            int index = _excludeTable.getSelectedRow();
            if(index != -1) {
                _excludeTableModel.removeExcludeEntry(index);
            }
        }
    }

    private class ExcludeTableModel extends AbstractTableModel {
        private java.util.List<ExcludeEntry> _excludeList = new ArrayList<ExcludeEntry>();
        private boolean _isChanged;

        public int getRowCount() {
            return _excludeList.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public void addExcludeEntry(String excludeName, boolean isEnabled) {
            if(isAlreadyThere(excludeName))
                return;
            _excludeList.add(new ExcludeEntry(excludeName, isEnabled));
            fireTableRowsInserted(_excludeList.size() - 1, _excludeList.size() - 1);
            _isChanged = true;
        }

        private boolean isAlreadyThere(String excludeName) {
            for (Iterator<ExcludeEntry> iterator = _excludeList.iterator(); iterator.hasNext();) {
                ExcludeEntry excludeEntry = iterator.next();
                if(excludeEntry.getExcludeName().equals(excludeName))
                    return true;
            }
            return false;
        }

        public void removeExcludeEntry(int index) {
            _excludeList.remove(index);
            fireTableRowsDeleted(index, index);
            _isChanged = true;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if(rowIndex >= _excludeList.size() || rowIndex < 0)
                return null;
            ExcludeEntry entry = _excludeList.get(rowIndex);
            switch(columnIndex) {
                case 0: return entry.getExcludeName();
                case 1: return entry.isEnabled();
                default: return null;
            }
        }

        public Class getColumnClass(int columnIndex) {
            switch(columnIndex) {
                case 0: return String.class;
                case 1: return Boolean.class;
                default: return super.getColumnClass(columnIndex);
            }
        }

        public String getColumnName(int column) {
            switch(column) {
                case 0: return "Exclude class or package";
                case 1: return "Enabled";
                default: return "";
            }
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if(rowIndex >= _excludeList.size() || rowIndex < 0)
                return;
            ExcludeEntry excludeEntry = _excludeList.get(rowIndex);
            switch(columnIndex) {
                case 0:
                    String excludeName = (String)aValue;
                    if(excludeName == null || excludeName.trim().length() == 0) {
                        JOptionPane.showMessageDialog(_mainPanel, "Name cannot be empty.",
                              "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    excludeEntry.setExcludeName(excludeName);
                    break;
                case 1:
                    excludeEntry.setEnabled(((Boolean)aValue).booleanValue());
                    break;
            }
            _isChanged = true;
        }

        public java.util.List<ExcludeEntry> getExcludeList() {
            return _excludeList;
        }

        public boolean isChanged() {
            return _isChanged;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public void setExcludeList(java.util.List<ExcludeEntry> excludeList) {
            _excludeList = excludeList;
            _isChanged = false;
            fireTableDataChanged();
        }
    }
}
