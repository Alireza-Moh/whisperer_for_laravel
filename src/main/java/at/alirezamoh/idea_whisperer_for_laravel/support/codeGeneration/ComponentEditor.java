package at.alirezamoh.idea_whisperer_for_laravel.support.codeGeneration;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class ComponentEditor extends AbstractCellEditor implements TableCellEditor {
    private Component editorComponent;

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        editorComponent = (Component) value;
        return editorComponent;
    }

    @Override
    public Object getCellEditorValue() {
        return editorComponent;
    }
}
