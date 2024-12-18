package at.alirezamoh.whisperer_for_laravel.actions.views;

import at.alirezamoh.whisperer_for_laravel.actions.models.EloquentModel;
import at.alirezamoh.whisperer_for_laravel.actions.models.dataTables.Field;
import at.alirezamoh.whisperer_for_laravel.support.codeGeneration.ComponentEditor;
import at.alirezamoh.whisperer_for_laravel.support.strUtil.StrUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EloquentView extends BaseDialog {
    /**
     * Text field for entering the eloquent model name
     */
    private JTextField eloquentNameTextField;

    /**
     * Contains the list of all fields
     */
    private JBTable fieldsTable;

    private DefaultTableModel fieldsTableModel;

    private JTextField migrationFileNameTextField;
    private JCheckBox migrationCheckBox;

    private JTextField controllerNameTextField;
    private JCheckBox controllerCheckBox;

    private String tableName;

    /**
     * @param project The current project
     */
    public EloquentView(Project project) {
        super(project);

        setTitle("Create Eloquent Model");
        setSize(800, 500);
        setResizable(false);
        init();
    }

    /**
     * Returns the View model representing a controller
     *
     * @return The config model
     */
    public EloquentModel getEloquentModel() {
        return new EloquentModel(
            projectSettingState,
            eloquentNameTextField.getText(),
            getUnformattedModuleFullPath(this.moduleNameComboBox.getItem()),
            getSelectedFormattedModuleFullPath(),
            getFields()
        );
    }

    public boolean withController() {
        return controllerCheckBox.isSelected();
    }

    public String getControllerName() {
        return controllerNameTextField.getText();
    }

    public boolean withMigration() {
        return migrationCheckBox.isSelected();
    }

    public String getTableName() {
        return tableName;
    }

    public String getMigrationFileName() {
        return migrationFileNameTextField.getText();
    }

    /**
     * Returns the focused component when the dialog opens
     *
     * @return The preferred focused component
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return eloquentNameTextField;
    }

    /**
     * Validates the dialog input
     *
     * @return Validation info if there are errors, null otherwise
     */
    @Override
    protected @Nullable ValidationInfo doValidate() {
        String text = eloquentNameTextField.getText().trim();

        if (text.isEmpty()) {
            return new ValidationInfo("", eloquentNameTextField);
        }
        return null;
    }

    /**
     * Creates the center panel of the dialog
     *
     * @return The center panel
     */
    @Override
    protected JComponent createCenterPanel() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());

        eloquentNameTextField = new JTextField();

        initDefaultContentPaneSettings();

        gbc.insets = JBUI.insetsLeft(3);
        contentPane.add(new JLabel("Model name:"), gbc);
        gbc.insets = JBUI.insetsLeft(0);
        gbc.insets = JBUI.insetsBottom(15);
        gbc.gridy++;
        contentPane.add(this.eloquentNameTextField, gbc);
        eloquentNameTextField.requestFocusInWindow();

        createAndListenToAddNewFieldButton();
        setupFieldsPanel();
        createOptionsPanel();
        listenToModelNameField();

        return contentPane;
    }

    private void createAndListenToAddNewFieldButton() {
        JButton addNewFieldButton = new JButton("Add New Field");
        gbc.gridy++;
        contentPane.add(addNewFieldButton, gbc);

        addNewFieldButton.addActionListener(e -> {
            createFieldPairPanel();
        });
    }

    private void setupFieldsPanel() {
        gbc.gridy++;

        String[] columnNames = {"Field Name", "Type", "Nullable"};
        fieldsTableModel = new DefaultTableModel(columnNames, 0);

        fieldsTable = new JBTable(fieldsTableModel) {
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                return (table, value, isSelected, hasFocus, row1, column1) -> (Component) value;
            }

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                return new ComponentEditor();
            }
        };

        TableColumn firstColumn = fieldsTable.getColumnModel().getColumn(0);
        firstColumn.setPreferredWidth(300);

        JBScrollPane scrollPane = new JBScrollPane(fieldsTable);
        scrollPane.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMaximumSize(new Dimension(755, 300));
        scrollPane.setPreferredSize(new Dimension(750, 300));
        scrollPane.setMinimumSize(new Dimension(750, 300));

        contentPane.add(scrollPane, gbc);

        createFieldPairPanel();
    }

    private void createFieldPairPanel() {
        JBTextField fieldTextField = new JBTextField();
        fieldTextField.setPreferredSize(new Dimension(300, 38));
        ComboBox<String> fieldTypeComboBox = createTypeSelectBox();

        JCheckBox fieldNullable = new JCheckBox("Nullable");
        fieldNullable.setPreferredSize(new Dimension(100, 38));

        fieldsTableModel.addRow(new Object[]{fieldTextField, fieldTypeComboBox, fieldNullable});
    }

    private @NotNull ComboBox<String> createTypeSelectBox() {
        ComboBox<String> fieldTypeComboBox = new ComboBox<>(
            new String[]{
                "string", "text", "mediumText", "longText", "tinyText", "fullText",
                "integer", "boolean", "bigInteger", "smallInteger", "mediumInteger",
                "tinyInteger", "unsignedBigInteger", "unsignedInteger", "unsignedSmallInteger",
                "unsignedMediumInteger", "unsignedTinyInteger", "float", "double", "decimal",
                "unsignedFloat", "unsignedDouble", "unsignedDecimal", "timestamp", "time",
                "date", "datetime", "json", "uuid"
            }
        );
        fieldTypeComboBox.setPreferredSize(new Dimension(150, 38)); // Adjust size for the ComboBox
        return fieldTypeComboBox;
    }

    private List<Field> getFields() {
        List<Field> fields = new ArrayList<>();

        int rowCount = fieldsTableModel.getRowCount();

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            JBTextField fieldTextField = (JBTextField) fieldsTableModel.getValueAt(rowIndex, 0);
            String fieldName = fieldTextField.getText().trim();

            if (fieldName.isEmpty()) {
                continue;
            }

            ComboBox<String> fieldTypeComboBox = (ComboBox<String>) fieldsTableModel.getValueAt(rowIndex, 1);
            String fieldType = (String) fieldTypeComboBox.getSelectedItem();

            JCheckBox fieldNullableCheckBox = (JCheckBox) fieldsTableModel.getValueAt(rowIndex, 2);
            boolean isNullable = fieldNullableCheckBox.isSelected();

            Field field = new Field(fieldType, fieldName, isNullable);
            fields.add(field);
        }

        return fields;
    }

    private void createOptionsPanel() {
        JPanel optionPanel = new JPanel(new GridBagLayout());

        migrationFileNameTextField = new JTextField();
        migrationCheckBox = new JCheckBox("Migration");

        controllerNameTextField = new JTextField();
        controllerCheckBox = new JCheckBox("Controller");

        gbc.gridy++;
        createOptionPair(migrationFileNameTextField, migrationCheckBox, optionPanel);

        gbc.gridy++;
        createOptionPair(controllerNameTextField, controllerCheckBox, optionPanel);

        gbc.gridy++;
        this.contentPane.add(optionPanel, gbc);
    }

    private void createOptionPair(JTextField textField, JCheckBox checkBox, @NotNull JPanel optionPanel) {
        JPanel wrapper = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = JBUI.insetsRight(10);
        wrapper.add(checkBox, gbc);

        textField.setPreferredSize(new Dimension(700, 38));
        textField.setMinimumSize(new Dimension(700, 38));
        gbc.gridx = 1;
        wrapper.add(textField, gbc);

        optionPanel.add(wrapper, this.gbc);
    }

    private void listenToModelNameField() {
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setOptionsText();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setOptionsText();
                checkIfEmpty();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            private void checkIfEmpty() {
                if (eloquentNameTextField.getText().isEmpty()) {
                    unsetOptionsText();
                }
            }
        };

        eloquentNameTextField.getDocument().addDocumentListener(documentListener);
    }

    private void setOptionsText() {
        String realModelName = this.getRealModelName();
        tableName = StrUtil.plural(StrUtil.snake(realModelName));

        controllerNameTextField.setText(capitalizeFirstLetter(realModelName) + "Controller");
        migrationFileNameTextField.setText(
            StrUtil.getCurrentDate()
                + "_"
                + StrUtil.generateRandomId()
                + "create_"
                + tableName
                + "_table"
        );
    }

    private void unsetOptionsText() {
        controllerNameTextField.setText("");
        migrationFileNameTextField.setText("");
        tableName = "";
    }

    private String getRealModelName() {
        String[] names = getLowerCase(eloquentNameTextField.getText()).split("[\\\\/]");
        return names[names.length - 1];
    }

    private String getLowerCase(@NotNull String text) {
        if (!text.isEmpty() && Character.isLowerCase(text.charAt(0))) {
            return text;
        } else if (!text.isEmpty()) {
            return text.substring(0, 1).toLowerCase() + text.substring(1);
        } else {
            return "";
        }
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }
}
