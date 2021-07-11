package org.intellij.sequencer.config;

import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Optional;

public class ColorSupport {

    public static Optional<Paint> lookupMappedColorFor(SequenceSettingsState sequenceSettingsState, String fullName) {
        final List<ColorMapEntry> colorMappings = sequenceSettingsState.getColorMappingList();
        if(colorMappings!=null) {
            return sequenceSettingsState.getColorMappingList()
                    .stream()
                    .filter(entry->entry.matches(fullName))
                    .findFirst()
                    .map(ColorMapEntry::getColor);
        }
        return Optional.empty();
    }

    public static Color withTransparency(Color c, float alpha) {
        int transparency = Math.round(alpha * 255.f);
        return new JBColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), transparency), new Color(c.getRed(), c.getGreen(), c.getBlue(), transparency));
    }

    public static class ColorEditor extends AbstractCellEditor
            implements TableCellEditor,
            ActionListener {
        Color currentColor;
        JButton button;
        protected static final String EDIT = "edit";

        public ColorEditor() {
            //Set up the editor (from the table's point of view),
            //which is a button.
            //This button brings up the color chooser dialog,
            //which is the editor from the user's point of view.
            button = new JButton();
            button.setActionCommand(EDIT);
            button.addActionListener(this);
            button.setBorderPainted(false);

        }

        /**
         * Handles events from the editor button and from
         * the dialog's OK button.
         */
        public void actionPerformed(ActionEvent e) {
            if (EDIT.equals(e.getActionCommand())) {
                //The user has clicked the cell, so
                //bring up the dialog.
                button.setBackground(currentColor);

                Color newColor = JColorChooser.showDialog(button, "Pick a Color",
                        currentColor);
                if(newColor != null)
                    currentColor = newColor;

                //Make the renderer reappear.
                fireEditingStopped();

            }
        }

        //Implement the one CellEditor method that AbstractCellEditor doesn't.
        public Object getCellEditorValue() {
            return currentColor;
        }

        //Implement the one method defined by TableCellEditor.
        public Component getTableCellEditorComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     int row,
                                                     int column) {
            currentColor = (Color)value;
            return button;
        }
    }

    public static class ColorRenderer extends JLabel
            implements TableCellRenderer {
        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered;

        public ColorRenderer(boolean isBordered) {
            this.isBordered = isBordered;
            setOpaque(true); //MUST do this for background to show up.
        }

        public Component getTableCellRendererComponent(
                JTable table, Object color,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            Color newColor = (Color)color;
            setBackground(newColor);
            if (isBordered) {
                if (isSelected) {
                    if (selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }

            setToolTipText("RGB value: " + newColor.getRed() + ", "
                    + newColor.getGreen() + ", "
                    + newColor.getBlue());
            return this;
        }
    }
}
