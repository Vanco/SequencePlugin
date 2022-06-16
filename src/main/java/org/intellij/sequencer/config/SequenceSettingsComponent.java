package org.intellij.sequencer.config;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Objects;

public class SequenceSettingsComponent implements ActionListener {
    public static final String[] FONT_SIZES =
          {"8", "9", "10", "11", "12", "13", "14",
           "16", "18", "20", "24", "28", "32", "48", "64"};

    /**
     * Default Icon(13x13) show color of background.
     */
    public static final Icon DEFAULT_ICON = new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(c.getBackground());
            g.fillRect(x, y, 13, 13);
        }

        @Override
        public int getIconWidth() {
            return 13;
        }

        @Override
        public int getIconHeight() {
            return 13;
        }
    };

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
    private JButton _addColorMapEntry;
    private JButton _removeColorMapEntry;
    private JTable _excludeTable;
    private JTable _colorMapTable;
    private JButton _interfaceColor;
    private JCheckBox _showSimplifyCallName;
    private JCheckBox _showLambdaCall;

    private final ExcludeTableModel _excludeTableModel;
    private final ColorMapTableModel _colorMapTableModel;

    public SequenceSettingsComponent() {
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = environment.getAvailableFontFamilyNames();
        for (String font : fonts) {
            _fontName.addItem(font);
        }
        for (String fontSize : FONT_SIZES) {
            _fondSize.addItem(fontSize);
        }
        _classColor.addActionListener(this);
        _classColor.setIcon(DEFAULT_ICON);
        _externalClassColor.addActionListener(this);
        _externalClassColor.setIcon(DEFAULT_ICON);
        _methodBarColor.addActionListener(this);
        _methodBarColor.setIcon(DEFAULT_ICON);
        _selectedMethodBarColor.addActionListener(this);
        _selectedMethodBarColor.setIcon(DEFAULT_ICON);
        _interfaceColor.addActionListener(this);
        _interfaceColor.setIcon(DEFAULT_ICON);
        _addExcludeEntry.addActionListener(this);
        _removeExcludeEntry.addActionListener(this);
        _addColorMapEntry.addActionListener(this);
        _removeColorMapEntry.addActionListener(this);

        _excludeTableModel = new ExcludeTableModel();
        _excludeTable.setModel(_excludeTableModel);
        _excludeTable.getTableHeader().getColumnModel().getColumn(1).setMaxWidth(100);

        _colorMapTableModel = new ColorMapTableModel();
        _colorMapTable.setDefaultRenderer(Color.class,
                new ColorSupport.ColorRenderer(true));
        _colorMapTable.setDefaultEditor(Color.class,
                new ColorSupport.ColorEditor());
        _colorMapTable.setModel(_colorMapTableModel);
        _colorMapTable.getTableHeader().getColumnModel().getColumn(1).setMaxWidth(100);

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

    public boolean isModified(SequenceSettingsState sequenceSettingsState) {
        if(!_classColor.getBackground().equals(sequenceSettingsState.CLASS_COLOR))
            return true;
        if(!_externalClassColor.getBackground().equals(sequenceSettingsState.EXTERNAL_CLASS_COLOR))
            return true;
        if(!_methodBarColor.getBackground().equals(sequenceSettingsState.METHOD_BAR_COLOR))
            return true;
        if(!_selectedMethodBarColor.getBackground().equals(sequenceSettingsState.SELECTED_METHOD_BAR_COLOR))
            return true;
        if(!_interfaceColor.getBackground().equals(sequenceSettingsState.INTERFACE_COLOR))
            return true;
        if(sequenceSettingsState.USE_ANTIALIASING != _antialiasing.isSelected())
            return true;
        if(sequenceSettingsState.SHOW_RETURN_ARROWS != _showReturnArrows.isSelected())
            return true;
        if(sequenceSettingsState.SHOW_CALL_NUMBERS != _showCallNumbers.isSelected())
            return true;
        if(sequenceSettingsState.SHOW_SIMPLIFY_CALL_NAME != _showSimplifyCallName.isSelected())
            return true;
        if(sequenceSettingsState.SHOW_LAMBDA_CALL != _showLambdaCall.isSelected()) {
            return true;
        }
        if(sequenceSettingsState.USE_3D_VIEW != _use3dView.isSelected())
            return true;
        if(!Objects.equals(_fontName.getSelectedItem(), sequenceSettingsState.FONT_NAME))
            return true;
        if(sequenceSettingsState.FONT_SIZE != Integer.parseInt((String) Objects.requireNonNull(_fondSize.getSelectedItem())))
            return true;
        if(_excludeTableModel.isChanged())
            return true;
        if(_colorMapTableModel.isChanged())
            return true;
        return false;
    }

    public void apply(SequenceSettingsState sequenceSettingsState) {
        sequenceSettingsState.CLASS_COLOR = _classColor.getBackground();
        sequenceSettingsState.EXTERNAL_CLASS_COLOR = _externalClassColor.getBackground();
        sequenceSettingsState.METHOD_BAR_COLOR = _methodBarColor.getBackground();
        sequenceSettingsState.SELECTED_METHOD_BAR_COLOR = _selectedMethodBarColor.getBackground();
        sequenceSettingsState.INTERFACE_COLOR = _interfaceColor.getBackground();
        sequenceSettingsState.USE_ANTIALIASING = _antialiasing.isSelected();
        sequenceSettingsState.SHOW_RETURN_ARROWS = _showReturnArrows.isSelected();
        sequenceSettingsState.SHOW_CALL_NUMBERS = _showCallNumbers.isSelected();
        sequenceSettingsState.SHOW_SIMPLIFY_CALL_NAME = _showSimplifyCallName.isSelected();
        sequenceSettingsState.SHOW_LAMBDA_CALL = _showLambdaCall.isSelected();
        sequenceSettingsState.USE_3D_VIEW = _use3dView.isSelected();
        sequenceSettingsState.FONT_NAME = (String)_fontName.getSelectedItem();
        sequenceSettingsState.FONT_SIZE = Integer.parseInt(((String) Objects.requireNonNull(_fondSize.getSelectedItem())));
        sequenceSettingsState.setExcludeList(_excludeTableModel.getExcludeList());
        sequenceSettingsState.setColorMappingList(_colorMapTableModel.getColorMapEntryList());
    }

    public void reset(SequenceSettingsState sequenceSettingsState) {
        _classColor.setBackground(sequenceSettingsState.CLASS_COLOR);
        _externalClassColor.setBackground(sequenceSettingsState.EXTERNAL_CLASS_COLOR);
        _methodBarColor.setBackground(sequenceSettingsState.METHOD_BAR_COLOR);
        _selectedMethodBarColor.setBackground(sequenceSettingsState.SELECTED_METHOD_BAR_COLOR);
        _interfaceColor.setBackground(sequenceSettingsState.INTERFACE_COLOR);
        _antialiasing.setSelected(sequenceSettingsState.USE_ANTIALIASING);
        _showReturnArrows.setSelected(sequenceSettingsState.SHOW_RETURN_ARROWS);
        _showCallNumbers.setSelected(sequenceSettingsState.SHOW_CALL_NUMBERS);
        _showSimplifyCallName.setSelected(sequenceSettingsState.SHOW_SIMPLIFY_CALL_NAME);
        _showLambdaCall.setSelected(sequenceSettingsState.SHOW_LAMBDA_CALL);
        _use3dView.setSelected(sequenceSettingsState.USE_3D_VIEW);
        _fontName.setSelectedItem(sequenceSettingsState.FONT_NAME);
        _fondSize.setSelectedItem(String.valueOf(sequenceSettingsState.FONT_SIZE));
        _excludeTableModel.setExcludeList(new ArrayList<>(sequenceSettingsState.getExcludeList()));
        _colorMapTableModel.setColorMapEntryList(new ArrayList<>(sequenceSettingsState.getColorMappingList()));
    }

    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "classColor":
                handleColor(_classColor);
                break;
            case "externalClassColor":
                handleColor(_externalClassColor);
                break;
            case "methodBarColor":
                handleColor(_methodBarColor);
                break;
            case "selectedMethodBarColor":
                handleColor(_selectedMethodBarColor);
                break;
            case "interfaceColor":
                handleColor(_interfaceColor);
                break;
            case "addExcludeEntry":
                String excludeName = JOptionPane.showInputDialog(_mainPanel,
                        "Enter package or class name.\nFor example, \njava.lang.* \njava.util.** \njava.io.PrintStream:", "Exclude Entry",
                        JOptionPane.PLAIN_MESSAGE);
                if (excludeName != null && excludeName.trim().length() != 0)
                    _excludeTableModel.addExcludeEntry(excludeName, true);
                break;
            case "removeExcludeEntry": {
                int index = _excludeTable.getSelectedRow();
                if (index != -1) {
                    _excludeTableModel.removeExcludeEntry(index);
                }
                break;
            }
            case "addColorMapEntry":
                String regex = JOptionPane.showInputDialog(_mainPanel,
                        "Enter matcher for package or class name.\nFor example, java.lang.*", "Color Map Entry",
                        JOptionPane.PLAIN_MESSAGE);
                if (regex != null && regex.trim().length() != 0)
                    _colorMapTableModel.addColorMapEntry(regex, Color.ORANGE);
                break;
            case "removeColorMapEntry": {
                int index = _excludeTable.getSelectedRow();
                if (index != -1) {
                    _colorMapTableModel.removeColorMapEntry(index);
                }
                break;
            }
        }
    }

    private class ExcludeTableModel extends AbstractTableModel {
        private java.util.List<ExcludeEntry> _excludeList = new ArrayList<>();
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
            for (ExcludeEntry excludeEntry : _excludeList) {
                if (excludeEntry.getExcludeName().equals(excludeName))
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

        public Class<?> getColumnClass(int columnIndex) {
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
                    excludeEntry.setEnabled((Boolean) aValue);
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

    private class ColorMapTableModel extends AbstractTableModel {
        private java.util.List<ColorMapEntry> colorMapEntries = new ArrayList<>();
        private boolean _isChanged;

        public int getRowCount() {
            return colorMapEntries.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public void addColorMapEntry(String regex, Color color) {
            if(isAlreadyThere(regex))
                return;
            colorMapEntries.add(new ColorMapEntry(regex, color));
            fireTableRowsInserted(colorMapEntries.size() - 1, colorMapEntries.size() - 1);
            _isChanged = true;
        }

        private boolean isAlreadyThere(String regex) {
            for (ColorMapEntry entry : colorMapEntries) {
                if (entry.getRegex().equals(regex))
                    return true;
            }
            return false;
        }

        public void removeColorMapEntry(int index) {
            colorMapEntries.remove(index);
            fireTableRowsDeleted(index, index);
            _isChanged = true;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if(rowIndex >= colorMapEntries.size() || rowIndex < 0)
                return null;
            ColorMapEntry entry = colorMapEntries.get(rowIndex);
            switch(columnIndex) {
                case 0: return entry.getRegex();
                case 1: return entry.getColor();
                default: return null;
            }
        }

        public Class getColumnClass(int columnIndex) {
            switch(columnIndex) {
                case 0: return String.class;
                case 1: return Color.class;
                default: return super.getColumnClass(columnIndex);
            }
        }

        public String getColumnName(int column) {
            switch(column) {
                case 0: return "class or package (matcher pattern)";
                case 1: return "Color";
                default: return "";
            }
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if(rowIndex >= colorMapEntries.size() || rowIndex < 0)
                return;
            ColorMapEntry colorMapEntry = colorMapEntries.get(rowIndex);
            switch(columnIndex) {
                case 0:
                    String regex = (String)aValue;
                    if(regex == null || regex.trim().length() == 0) {
                        JOptionPane.showMessageDialog(_mainPanel, "Matcher cannot be empty.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    colorMapEntry.setRegex(regex);
                    break;
                case 1:
                    Color newColor = (Color)aValue;
                    colorMapEntry.setColor(newColor);
                    break;
            }
            _isChanged = true;
        }

        public java.util.List<ColorMapEntry> getColorMapEntryList() {
            return colorMapEntries;
        }

        public boolean isChanged() {
            return _isChanged;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public void setColorMapEntryList(java.util.List<ColorMapEntry> colorMapEntries) {
            this.colorMapEntries = colorMapEntries;
            _isChanged = false;
            fireTableDataChanged();
        }
    }

}
