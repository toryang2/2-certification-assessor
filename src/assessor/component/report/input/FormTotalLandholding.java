package assessor.component.report.input;

import assessor.auth.SessionManager;
import assessor.component.report.util.*;
import static assessor.component.report.util.DatabaseSaveHelper.saveAutocompleteValue;
import assessor.system.*;
import assessor.component.chart.CertificateTableTotalLandholding;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.JRootPane;
import javax.swing.JComponent;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.*;
import java.util.List;
import java.util.Locale;
import java.util.logging.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.DateSelectionAble;
import raven.modal.Drawer;
import java.sql.*;
import java.util.Arrays;
import javax.swing.table.TableColumnModel;
import com.formdev.flatlaf.FlatClientProperties;
import java.util.ArrayList;
import assessor.component.report.TextNormalizer;
import assessor.forms.FormDashboardTotalLandholding;
import assessor.menu.MyDrawerBuilder;
import raven.modal.drawer.DrawerPanel;
import raven.modal.Toast;
import raven.modal.toast.option.ToastOption;
import assessor.system.FormManager;

/**
 *
 * @author user
 */
public class FormTotalLandholding extends Form {
    private static final Logger logger = Logger.getLogger(FormTotalLandholding.class.getName());
    private Consumer<Boolean> saveCallBack;
    private DatePicker datePicker;
    private Timer messageTimer;
    private boolean saveSuccessful = false;
    private JCheckBox checkBoxSingle;
    private JCheckBox checkBoxMarried;
    private JTextField txtMaritalStatus;
    private JTextField txtOwner;
    private JTextField txtSpouse;
    private JTextField txtPurpose;
    private JTextField txtAmountPaid;
    private JFormattedTextField txtReceiptNo;
    private JTextField txtPlaceIssued;
    private JTextField txtRequestor;
    private JTextField receiptDateIssuedPicker;
    private JTextField txtContactNo;
    private JLabel labelMaritalStatus;
    private JLabel labelTitle;
    private JLabel labelOwner;
    private JLabel labelRequestor;
    private JLabel labelMandatoryOwner;
    private JLabel labelMandatoryRequestor;
    private JLabel labelSpouse;
    private JLabel labelPurpose;
    private JLabel labelMandatoryPurpose;
    private JLabel labelAmount;
    private JLabel labelReceiptNo;
    private JLabel labelDateIssued;
    private JLabel labelPlaceIssued;
    private JLabel labelMandatoryMessage;
    private JLabel labelContactNo;
    private JTable inputTable;
    private DefaultTableModel inputTableModel;
    private java.util.List<String> tableColumns;
    
    public boolean isSaveSuccessful() {
        return saveSuccessful;
    }
    
    public FormTotalLandholding() {
        setLayout(new MigLayout("al, insets 0"));
        initComponents();
        setupAmountField();
        setupReceiptNoField();
        applyUppercaseFilterToTextFields();
        
        checkBoxSingle.setSelected(true);
        txtSpouse.setEnabled(false);
        txtSpouse.setText("");
        JCheckBox[] maritalCheckboxes = {
            checkBoxMarried, 
            checkBoxSingle,
        };
        
        for (JCheckBox checkbox : maritalCheckboxes) {
            checkbox.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    // Uncheck other boxes when one is checked
                    for (JCheckBox other : maritalCheckboxes) {
                        if (other != e.getSource()) {
                            other.setSelected(false);
                            txtRequestor.requestFocusInWindow();
                            txtSpouse.setEnabled(false);
                            txtSpouse.setText("");
                        }
                    }
                    
                    // Update combos based on selection
                    if (checkbox == checkBoxMarried) {
                        SwingUtilities.invokeLater(() -> {
                            txtRequestor.requestFocusInWindow();
                            txtSpouse.setEnabled(true);
                            txtSpouse.setText("");
                        });
                    }
                }
                else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    // Check of any checkbox is still selected
                    boolean anySelected = false;
                    for (JCheckBox cb : maritalCheckboxes) {
                        if (cb.isSelected()) {
                            anySelected = true;
                            break;
                        }
                    }
                    // If none selected, force Married to be checked
                    if (!anySelected) {
                        checkBoxSingle.setSelected(true);
                        txtRequestor.requestFocusInWindow();
                    }
                }
            });
        }
        
        // Ensure the report type exists in the database
        DatabaseTotalLandholdingHelper.ensureReportTypeExists();
        
        // Initialize the table for report_sub_total_landholding
        createInputTable();
        
        new AutocompleteSupport(txtPlaceIssued, "txtPlaceIssued");
        
        datePicker = new DatePicker();
        JFormattedTextField receiptDateIssuedPicker = new JFormattedTextField();
        datePicker.setEditor(receiptDateIssuedPicker);
        datePicker.setDateSelectionMode(DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED);
        datePicker.setDateFormat("MM/dd/yyyy");
        datePicker.setUsePanelOption(true);
        datePicker.setCloseAfterSelected(true);
        datePicker.setDateSelectionAble(new DateSelectionAble(){
            @Override
            public boolean isDateSelectedAble(LocalDate localDate) {
                return !localDate.isAfter(LocalDate.now());
            }
        });
        
        labelMandatoryOwner.setVisible(false);
        labelMandatoryRequestor.setVisible(false);
        labelMandatoryPurpose.setVisible(false);
        labelMandatoryMessage.setVisible(false);
        
        messageTimer = new Timer(3000, e -> {
            labelMandatoryMessage.setVisible(false);
        });
        messageTimer.setRepeats(false);
        
        JPanel contentPanel = new JPanel(new MigLayout(
        "insets 20 30 20 30, gap 5 15",
        "[left][5:5:5][50:50:50][50:50:50][70:70:70][left][100:100:100][70:70:70][100:100:100][50:50:50][50:50:50][50:50:50]", 
        "[][][][][][][][][grow][][]"
        ));
        
        // Apply panel styling
//        contentPanel.putClientProperty(FlatClientProperties.STYLE, "arc:20;background:$Table.background;");
        
        //Row 0: Title
        contentPanel.add(labelTitle, "span 12, center, wrap");
        //Row 1: Marital Status
        JPanel maritalPanel = new JPanel(new MigLayout("gap 15, insets 0, align center"));
        maritalPanel.add(checkBoxSingle);
        maritalPanel.add(checkBoxMarried);
        contentPanel.add(maritalPanel, "span 12, center, wrap");
        //Row 2: Owner
        contentPanel.add(labelRequestor, "cell 0 2");
        contentPanel.add(labelMandatoryRequestor);
        contentPanel.add(txtRequestor, "cell 2 2 10 1, growx, pushx, w 100%, wrap");
        
        contentPanel.add(labelOwner, "cell 0 3");
        contentPanel.add(labelMandatoryOwner);
        contentPanel.add(txtOwner, "cell 2 3 10 1, growx, pushx, w 100%, wrap");
        
        contentPanel.add(labelSpouse, "cell 0 4");
        contentPanel.add(txtSpouse, "cell 2 4 10 1, growx, pushx, w 100%");
        
        contentPanel.add(labelPurpose, "cell 0 5");
        contentPanel.add(labelMandatoryPurpose);
        contentPanel.add(txtPurpose, "cell 2 5 12 1, growx, pushx, wrap");
        
        contentPanel.add(labelAmount, "cell 0 6");
        contentPanel.add(txtAmountPaid, "cell 2 6 2 1, growx");
        contentPanel.add(labelReceiptNo, "cell 4 6 4 1, split 2");
        contentPanel.add(txtReceiptNo, "cell 4 6 4 1, growx, wrap");
        
        contentPanel.add(labelDateIssued, "cell 8 6 5 1, split 2");
        contentPanel.add(receiptDateIssuedPicker, "cell 8 6 5 1, growx");
        
        contentPanel.add(labelPlaceIssued, "cell 0 7");
        contentPanel.add(txtPlaceIssued, "cell 2 7 4 1, growx");
        contentPanel.add(labelContactNo, "cell 6 7 2 1, split 2");
        contentPanel.add(txtContactNo, "cell 6 7 2 1, growx, wrap");
        
        // --- Save button logic ---
        
        // Add the table to the form in a scroll pane
        JScrollPane tableScrollPane = new JScrollPane(inputTable);
        
        // Apply scroll pane styling
        tableScrollPane.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
            "trackArc:$ScrollBar.thumbArc;trackInsets:3,3,3,3;thumbInsets:3,3,3,3;background:$Table.background;");
        tableScrollPane.getHorizontalScrollBar().putClientProperty(FlatClientProperties.STYLE,
            "trackArc:$ScrollBar.thumbArc;trackInsets:3,3,3,3;thumbInsets:3,3,3,3;background:$Table.background;");
        
        contentPanel.add(tableScrollPane, "cell 0 8 12 1, grow, wrap"); // Span all columns
        
        contentPanel.add(labelMandatoryMessage, "cell 0 9 4 1, left");
        JButton btnClear = new JButton("Clear");
        btnClear.addActionListener(e -> clearAllFields());
        contentPanel.add(btnClear, "span 12, right, split 2");
        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> onSaveButtonClick());
        contentPanel.add(btnSave, "span 12, right, wrap");
        
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.add(contentPanel);
        
        add(wrapper);
        
        // Add Enter key listener to the entire form after the form is fully initialized
        SwingUtilities.invokeLater(() -> {
            // Delay the setup to ensure the form is properly displayed and has a root pane
            Timer setupTimer = new Timer(200, e -> {
                setupEnterKeyListener();
                ((Timer) e.getSource()).stop();
            });
            setupTimer.setRepeats(false);
            setupTimer.start();
        });
    }
    

    
    private void initComponents() {
        labelOwner = new JLabel("Owner");
        labelRequestor = new JLabel("Requestor");
        labelMandatoryOwner = new JLabel("<html><font color='red'>*</font></html>");
        labelMandatoryRequestor = new JLabel("<html><font color='red'>*</font></html>");
        labelTitle = new JLabel("CLIENT INFORMATION");
        labelSpouse = new JLabel("Spouse");
        labelPurpose = new JLabel("Purpose");
        labelMandatoryPurpose = new JLabel("<html><font color='red'>*</font></html>");
        labelMandatoryMessage = new JLabel("<html><font color='red'>*</font> Mandatory fields required</html>");
        labelAmount = new JLabel("Amount");
        labelReceiptNo = new JLabel("Receipt No.");
        labelDateIssued = new JLabel("Date Issued");
        labelPlaceIssued = new JLabel("Place Issued");
        labelContactNo = new JLabel("Contact No.");
        labelMaritalStatus = new JLabel();
        checkBoxSingle = new JCheckBox("Single");
        checkBoxMarried = new JCheckBox("Married");
        txtMaritalStatus = new JTextField();
        txtOwner = new JTextField();
        txtRequestor = new JTextField();
        txtSpouse = new JTextField();
        txtPurpose = new JTextField();
        txtAmountPaid = new JTextField();
        txtContactNo = new JTextField();
        txtReceiptNo = new JFormattedTextField();
        receiptDateIssuedPicker = new JTextField();
        txtPlaceIssued = new JTextField();
        
        txtPlaceIssued.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "MTO, Kitaotao, Bukidnon");
        txtRequestor.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "JUAN DELA CRUZ (SON)");
        txtRequestor.setToolTipText("example: JUAN DELA CRUZ (SON)");
        txtPurpose.setToolTipText("example: DAR Purpose");
        txtContactNo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "09XXXXXXXXX");
        ((AbstractDocument) txtContactNo.getDocument()).setDocumentFilter(new NumericDocumentFilter());
    }
    
    // Create the input table for report_sub_total_landholding
    private void createInputTable() {
        // Get columns from the report_sub_total_landholding table
        try {
            tableColumns = TotalLandholding_TableUtil.getColumnNames("report_sub_total_landholding");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to get columns from database, using default columns", e);
            // Fallback to default columns for report_sub_total_landholding
            tableColumns = Arrays.asList("rtlid", "td", "location", "lot_no", "total_area", "kind", "objid");
        }

        // Remove 'rtlid' and 'objid' from the UI table columns
        tableColumns = tableColumns.stream()
            .filter(col -> !col.equalsIgnoreCase("rtlid") && !col.equalsIgnoreCase("objid"))
            .collect(java.util.stream.Collectors.toList());

        // Create the table model
        inputTableModel = new DefaultTableModel(tableColumns.toArray(), 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                // All columns editable in UI (no rtlid/objid in UI)
                return true;
            }
        };

        // Create the table
        inputTable = new JTable(inputTableModel);
        
        // Set table properties
        inputTable.setFillsViewportHeight(true);
        inputTable.setRowHeight(25);
        // Only allow single row or single cell selection
        inputTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        inputTable.setCellSelectionEnabled(false); // Allows single cell selection
        inputTable.getTableHeader().setReorderingAllowed(false);
        
        // Add many empty rows to make table appear infinite
        for (int i = 0; i < 50; i++) {
            addEmptyRow();
        }
        
        // Apply table styling
        inputTable.getTableHeader().putClientProperty(FlatClientProperties.STYLE,
            "height:30;hoverBackground:null;pressedBackground:null;separatorColor:$TableHeader.background;");
        inputTable.putClientProperty(FlatClientProperties.STYLE,
            "rowHeight:30;showHorizontalLines:true;showVerticalLines:true;intercellSpacing:0,1;");
        
        // Set column widths based on column names
        TableColumnModel columnModel = inputTable.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            String columnName = tableColumns.get(i).toLowerCase();
            switch (columnName) {
                case "td":
                    columnModel.getColumn(i).setHeaderValue("Tax Declaration No.");
                    columnModel.getColumn(i).setPreferredWidth(150);
                    columnModel.getColumn(i).setMaxWidth(150);
                    columnModel.getColumn(i).setMinWidth(150);
                    break;
                case "location":
                    columnModel.getColumn(i).setHeaderValue("Location");
                    columnModel.getColumn(i).setPreferredWidth(150);
                    columnModel.getColumn(i).setMaxWidth(150);
                    columnModel.getColumn(i).setMinWidth(150);
                    break;
                case "market_value":
                    columnModel.getColumn(i).setHeaderValue("Market Value");
                    columnModel.getColumn(i).setPreferredWidth(150);
                    columnModel.getColumn(i).setMaxWidth(150);
                    columnModel.getColumn(i).setMinWidth(150);
                    break;
                case "total_area":
                    columnModel.getColumn(i).setHeaderValue("Total Area");
                    columnModel.getColumn(i).setPreferredWidth(80);
                    columnModel.getColumn(i).setMaxWidth(80);
                    columnModel.getColumn(i).setMinWidth(80);
                    break;
                case "kind":
                    columnModel.getColumn(i).setHeaderValue("Kind");
                    columnModel.getColumn(i).setPreferredWidth(180);
                    columnModel.getColumn(i).setMaxWidth(180);
                    columnModel.getColumn(i).setMinWidth(180);
                    break;
                default:
                    columnModel.getColumn(i).setPreferredWidth(100);
                    break;
            }
        }
        
        // Set up custom cell editors for specific columns
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            String columnName = tableColumns.get(i).toLowerCase();
            
            if (columnName.equals("total_area")) {
                columnModel.getColumn(i).setCellEditor(new DefaultCellEditor(new JTextField()) {
                    @Override
                    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                        JTextField textField = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
                        
                        // Set up document filter for this text field
                        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
                            @Override
                            public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) 
                                throws BadLocationException {
                                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                                String newText = currentText.substring(0, offset) + text + currentText.substring(offset);
                                if (isValidDecimalInput(newText)) {
                                    super.insertString(fb, offset, text, attr);
                                }
                            }
                            
                            @Override
                            public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                                throws BadLocationException {
                                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                                String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
                                if (isValidDecimalInput(newText)) {
                                    super.replace(fb, offset, length, text, attrs);
                                }
                            }
                            
                            private boolean isValidDecimalInput(String text) {
                                // Allow empty string
                                if (text.isEmpty()) {
                                    return true;
                                }
                                
                                // Allow partial decimal input (will be formatted later)
                                if (text.matches("^\\d{0,6}$") || text.matches("^\\d{0,6}\\.$") || text.matches("^\\d{0,6}\\.\\d{0,4}$")) {
                                    return true;
                                }
                                
                                return false;
                            }
                        });
                        
                        // Add focus listener to format the value when focus is lost
                        textField.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                formatDecimalValue(textField);
                            }
                        });
                        
                        return textField;
                    }
                    
                    @Override
                    public Object getCellEditorValue() {
                        JTextField textField = (JTextField) getComponent();
                        formatDecimalValue(textField);
                        return textField.getText();
                    }
                });
            } else if (columnName.equals("location") || columnName.equals("lot_no") || columnName.equals("kind")) {
                columnModel.getColumn(i).setCellEditor(new DefaultCellEditor(new JTextField()) {
                    @Override
                    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                        JTextField textField = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
                        
                        // Set up document filter to convert to uppercase
                        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
                            @Override
                            public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) 
                                throws BadLocationException {
                                super.insertString(fb, offset, text.toUpperCase(), attr);
                            }
                            
                            @Override
                            public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                                throws BadLocationException {
                                super.replace(fb, offset, length, text.toUpperCase(), attrs);
                            }
                        });
                        
                        return textField;
                    }
                });
            }
        }
    }
    
    // Get table values
    private String getTableValue(int row, int col) {
        Object value = inputTableModel.getValueAt(row, col);
        return value != null ? value.toString().trim() : "";
    }
    
    // Add empty row to table
    private void addEmptyRow() {
        int columnCount = inputTableModel.getColumnCount();
        Object[] rowData = new Object[columnCount];
        
        // Initialize with empty values, rtlid will be set during save
        for (int i = 0; i < columnCount; i++) {
            String columnName = inputTableModel.getColumnName(i).toLowerCase();
            if (columnName.equals("rtlid")) {
                rowData[i] = ""; // Will be set during save
            } else {
                rowData[i] = "";
            }
        }
        
        inputTableModel.addRow(rowData);
    }
    
    // Prepare table data for saving to database
    private List<Map<String, Object>> prepareTableData(String parentObjid) {
        List<Map<String, Object>> tableData = new ArrayList<>();
        int rowCount = inputTableModel.getRowCount();

        for (int i = 0; i < rowCount; i++) {
            Map<String, Object> rowData = new HashMap<>();
            // Get column names and values dynamically (except rtlid)
            for (int col = 0; col < inputTableModel.getColumnCount(); col++) {
                String columnName = inputTableModel.getColumnName(col);
                String value = getTableValue(i, col);
                if (columnName.equalsIgnoreCase("total_area")) {
                    // Convert area to numeric
                    if (!value.isEmpty()) {
                        try {
                            rowData.put(columnName, Double.parseDouble(value));
                        } catch (NumberFormatException e) {
                            rowData.put(columnName, null);
                        }
                    } else {
                        rowData.put(columnName, null);
                    }
                } else if (columnName.equalsIgnoreCase("kind")) {
                    rowData.put(columnName, TextNormalizer.toProperCase(value));
                } else {
                    rowData.put(columnName, value);
                }
            }
            // Always add rtlid for backend
            rowData.put("rtlid", parentObjid);
            tableData.add(rowData);
        }
        return tableData;
    }
    
    // Format decimal value to always have 4 decimal places
    private void formatDecimalValue(JTextField textField) {
        String text = textField.getText().trim();
        
        if (text.isEmpty()) {
            return;
        }
        
        try {
            // Handle different input formats
            if (text.matches("^\\d+$")) {
                // Just digits (e.g., "123") -> add ".0000"
                textField.setText(text + ".0000");
            } else if (text.matches("^\\d+\\.$")) {
                // Digits with decimal point (e.g., "123.") -> add "0000"
                textField.setText(text + "0000");
            } else if (text.matches("^\\d+\\.\\d+$")) {
                // Digits with decimal (e.g., "123.4") -> pad with zeros
                String[] parts = text.split("\\.");
                String wholePart = parts[0];
                String decimalPart = parts[1];
                
                // Pad decimal part with zeros to make it 4 digits
                while (decimalPart.length() < 4) {
                    decimalPart += "0";
                }
                
                // Truncate if more than 4 decimal places
                if (decimalPart.length() > 4) {
                    decimalPart = decimalPart.substring(0, 4);
                }
                
                textField.setText(wholePart + "." + decimalPart);
            }
        } catch (Exception e) {
            // If formatting fails, leave the text as is
            logger.log(Level.WARNING, "Error formatting decimal value: " + text, e);
        }
    }


    
    private void applyUppercaseFilterToTextFields() {
        UppercaseDocumentFilter uppercaseFilter = new UppercaseDocumentFilter();
        JTextField[] textFields = {
            txtRequestor,
            txtOwner,
            txtSpouse
//            txtPurpose,
//            txtPlaceIssued
        };
        for (JTextField textField : textFields) {
            ((AbstractDocument) textField.getDocument()).setDocumentFilter(uppercaseFilter);
        }
    }
    
    private void onSaveButtonClick() {
        try {
            handleAmountFocusLost();
            logger.log(Level.INFO, "Starting save action...");

            if (validateInput()) {
                logger.log(Level.INFO, "Input validation passed");

                Map<String, Object> reportData = new HashMap<>();
                // Add signatory
                String signatory = DatabaseTotalLandholdingHelper.getAssessorName(1);
                if (signatory != null) {
                    reportData.put("signatory", signatory);
                } else {
                    logger.log(Level.WARNING, "No default assessor configured");
                }

                String userInitials = SessionManager.getInstance().getUserInitials();
                if (userInitials != null) {
                    reportData.put("user_initials", userInitials);
                }

                if (datePicker.getSelectedDate() != null) {
                    reportData.put("receipt_date_issued", datePicker.getSelectedDate());
                }

                // Collect data from form fields
                reportData.put("requestor", txtRequestor.getText());
                reportData.put("marital_status", getMaritalStatus());
                reportData.put("owner", txtOwner.getText());
                reportData.put("spouse", txtSpouse.getText());
                reportData.put("purpose", txtPurpose.getText());
                reportData.put("requested_date", LocalDate.now());
                reportData.put("requested_time", LocalTime.now());
                reportData.put("amount_paid", getAmountPaid());
                reportData.put("receipt_no", getReceiptNumber());
                reportData.put("place_issued", txtPlaceIssued.getText());
                reportData.put("contact_no", txtContactNo.getText());

                // Save main report and get the generated id directly
                int generatedId = DatabaseTotalLandholdingHelper.saveReportAndGetNewestId("Total Landholding", reportData);

                if (generatedId != -1) {
                    logger.log(Level.INFO, "Main report saved successfully with ID: {0}", generatedId);
                    // Get the generated objid for the foreign key relationship
                    String generatedObjid = DatabaseTotalLandholdingHelper.getLastGeneratedObjid();
                    if (generatedObjid != null) {
                        logger.log(Level.INFO, "Retrieved generated objid: {0}", generatedObjid);
                        // Prepare table data for saving
                        List<Map<String, Object>> tableData = prepareTableData(generatedObjid);
                        // Save table data to sub-table using the foreign key relationship
                        int savedRows = DatabaseTotalLandholdingHelper.saveTableDataWithForeignKey(generatedObjid, tableData);
                        // Fetch CertificateTableTotalLandholding from FormManager
                        CertificateTableTotalLandholding certificateTable = FormManager.getActiveForm(CertificateTableTotalLandholding.class);
                        if (certificateTable == null) {
                            logger.log(Level.SEVERE, "CertificateTableTotalLandholding is not available in FormManager!");
                            JOptionPane.showMessageDialog(this,
                                    "Failed to refresh the table. CertificateTableTotalLandholding is not active.",
                                    "Table Refresh Error",
                                    JOptionPane.ERROR_MESSAGE);
                        } else if (certificateTable.reportLoader == null) {
                            logger.log(Level.SEVERE, "CertificateTableTotalLandholding's reportLoader is null!");
                            JOptionPane.showMessageDialog(this,
                                    "Failed to refresh the table. reportLoader is not initialized.",
                                    "Table Refresh Error",
                                    JOptionPane.ERROR_MESSAGE);
                        } else {
                            // Refresh the table
                            logger.log(Level.INFO, "Found CertificateTableTotalLandholding and reportLoader. Initiating data refresh...");
                            certificateTable.reportLoader.loadData(() -> {
                                logger.log(Level.INFO, "Table refresh complete. Generating report...");
                                monitorTableInitialization(certificateTable, generatedId);
                            });
                        }
                    }
                } else {
                    logger.log(Level.SEVERE, "Failed to retrieve generated objid");
                    JOptionPane.showMessageDialog(this, 
                        "Failed to retrieve report ID. Please try again.",
                        "System Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Critical error in save action", ex);
            handleSaveError(ex);
        }
    }
    

    
    private String getMaritalStatus() {
        if (checkBoxMarried.isSelected()) return "MARRIED";
        if (checkBoxSingle.isSelected()) return "SINGLE";
        return "Unknown";
    }
    
    private String getAmountPaid() {
        try {
            double amount = parseDouble(txtAmountPaid.getText());
            return formatAmount(amount);
        } catch (NumberFormatException ex) {
            logger.log(Level.WARNING, "Invalid amount format, defaulting to ₱0.00");
            return "₱0.00";
        }
    }
    
    private String getReceiptNumber() {
        try {
            Object receiptValue = txtReceiptNo.getValue();
            if (receiptValue instanceof Number) {
                long numberValue = ((Number) receiptValue).longValue();
                return numberValue == 0L ? null : String.valueOf(numberValue);
            }
            return null;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error processing receipt number", ex);
            throw new RuntimeException("Invalid receipt number", ex);
        }
    }
    
    private void setupAmountField() {
        // Initial placeholder
        txtAmountPaid.setText("₱0.00");
        
        // Focus listeners for formatting
        txtAmountPaid.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                handleAmountFocusGained();
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                handleAmountFocusLost();
            }
        });

        // Document filter for input validation
        ((AbstractDocument) txtAmountPaid.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) 
                throws BadLocationException {
                super.insertString(fb, offset, text, attr);
                SwingUtilities.invokeLater(() -> reformatAmount(fb.getDocument()));
            }

            @Override
            public void remove(DocumentFilter.FilterBypass fb, int offset, int length) 
                throws BadLocationException {
                super.remove(fb, offset, length);
                SwingUtilities.invokeLater(() -> reformatAmount(fb.getDocument()));
            }

            @Override
            public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                throws BadLocationException {
                super.replace(fb, offset, length, text, attrs);
                SwingUtilities.invokeLater(() -> reformatAmount(fb.getDocument()));
            }

            private boolean isValid(String text) {
                if (!text.startsWith("₱")) return false;
                String numericPart = text.substring(1).replace(",", "");
                return numericPart.matches("^\\d*(\\.\\d*)?$");
            }
            
            private void reformatAmount(Document doc) {
                try {
                    String originalText = doc.getText(0, doc.getLength());
                    String text = originalText.replace("₱", "").replaceAll(",", "");

                    // Handle empty case
                    if (text.isEmpty()) {
                        txtAmountPaid.setText("₱");
                        return;
                    }

                    // Handle multiple decimal points
                    int decimalIndex = text.indexOf('.');
                    String integerPart = decimalIndex != -1 ? text.substring(0, decimalIndex) : text;
                    String decimalPart = decimalIndex != -1 ? 
                        "." + text.substring(decimalIndex + 1).replaceAll("\\.00", "") : "";

                    // Format integer part with commas
                    if (!integerPart.isEmpty()) {
                        try {
                            long num = Long.parseLong(integerPart);
                            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
                            integerPart = nf.format(num);
                        } catch (NumberFormatException e) {
                            integerPart = text.split("\\.")[0];
                        }
                    }

                    int originalCaretPos = txtAmountPaid.getCaretPosition();

                    // Build new formatted text
                    String newText = "₱" + integerPart + decimalPart;
                    txtAmountPaid.setText(newText);

                    // Calculate new caret position
                    String cleanOriginal = originalText.replaceAll("[^0-9.]", "");
                    String cleanNew = newText.substring(1).replaceAll("[^0-9.]", "");

                    int newCaretPos = originalCaretPos;
                    if (originalCaretPos > 0) {
                        // Count valid characters before original caret
                        int validCharsBefore = 0;
                        for (int i = 0; i < originalCaretPos && i < originalText.length(); i++) {
                            char c = originalText.charAt(i);
                            if (Character.isDigit(c) || c == '.') validCharsBefore++;
                        }

                        // Find equivalent position in new text
                        int currentValidCount = 0;
                        newCaretPos = 1; // Start after currency symbol
                        while (newCaretPos < newText.length() && currentValidCount < validCharsBefore) {
                            char c = newText.charAt(newCaretPos);
                            if (Character.isDigit(c) || c == '.') currentValidCount++;
                            newCaretPos++;
                        }
                    }

                    // Ensure valid position
                    newCaretPos = Math.min(Math.max(newCaretPos, 1), newText.length());
                    txtAmountPaid.setCaretPosition(newCaretPos);

                } catch (BadLocationException | NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    
    private void handleAmountFocusGained() {
        if (txtAmountPaid.getText().equals("₱0.00")) {
            txtAmountPaid.setText("₱.00");
            txtAmountPaid.setCaretPosition(1);
        }
    }

    private void handleAmountFocusLost() {
        try {
            double amount = parseDouble(txtAmountPaid.getText());
            txtAmountPaid.setText(formatAmount(amount));
        } catch (NumberFormatException ex) {
            txtAmountPaid.setText("₱0.00");
        }
    }
    
    private String formatAmount(double amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return "₱" + nf.format(amount);
    }

    private double parseDouble(String text) throws NumberFormatException {
        try {
            String cleaned = text.replace("₱", "")
                                .replaceAll(",", "")
                                .replaceAll("[^\\d.]", "");
            
            if (cleaned.isEmpty()) {
                logger.log(Level.WARNING, "Empty amount field");
                throw new NumberFormatException("Empty amount");
            }
            
            return new BigDecimal(cleaned).doubleValue();
        } catch (NumberFormatException | ArithmeticException e) {
            logger.log(Level.SEVERE, "Error parsing amount: {0}", text);
            throw new NumberFormatException("Invalid amount format: " + text);
        }
    }
    
    private void setupReceiptNoField() {
        // Custom formatter with strict numeric enforcement
        txtReceiptNo = new JFormattedTextField(new DefaultFormatter() {
            @Override
            public Object stringToValue(String text) throws ParseException {
                if (text == null || text.trim().isEmpty()) {
                    return null;
                }
                if (!text.matches("\\d*")) {
                    throw new ParseException("Only digits allowed", 0);
                }
                return Long.parseLong(text);
            }

            @Override
            public String valueToString(Object value) throws ParseException {
                return value == null ? "" : value.toString();
            }
        }) {
            @Override
            public void replaceSelection(String content) {
                // Filter non-digits during text selection replacement
                super.replaceSelection(content.replaceAll("[^\\d]", ""));
            }
        };

        // Document filter for direct input handling
        ((PlainDocument) txtReceiptNo.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException {
                super.insertString(fb, offset, text.replaceAll("[^\\d]", ""), attr);
            }

            @Override
            public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                super.replace(fb, offset, length, text.replaceAll("[^\\d]", ""), attrs);
            }
        });

        // Visual setup
        txtReceiptNo.setHorizontalAlignment(JTextField.RIGHT);
        txtReceiptNo.setValue(null); // Initial empty state

        // Focus handling
        txtReceiptNo.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        txtReceiptNo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    txtReceiptNo.commitEdit();
                } catch (ParseException ex) {
                    logger.log(Level.WARNING, "Invalid receipt number format", ex);
                    txtReceiptNo.setValue(null);
                }
            }
        });
    }
    
    private void handleSaveError(Exception ex) {
        String errorMessage = "Save failed: " + ex.getMessage();
        logger.log(Level.SEVERE, errorMessage, ex);
        
        if (ex.getCause() instanceof SQLException) {
            SQLException sqlEx = (SQLException) ex.getCause();
            errorMessage += "\nSQL State: " + sqlEx.getSQLState();
            logger.log(Level.SEVERE, "SQL Exception details", sqlEx);
        }
        
        JOptionPane.showMessageDialog(this,
            errorMessage,
            "Save Error",
            JOptionPane.ERROR_MESSAGE);
    }
    
    private boolean validateInput() {
        logger.log(Level.FINE, "Starting input validation");
        
        Object[][] requiredFields = {
            {labelMandatoryRequestor, txtRequestor},
            { labelMandatoryOwner, txtOwner },
            { labelMandatoryPurpose, txtPurpose }
        };

        boolean isValid = true;

        // Text field validation
        for (Object[] pair : requiredFields) {
            JLabel mandatoryLabel = (JLabel) pair[0];
            JTextField field = (JTextField) pair[1];
            boolean isEmpty = field.getText().trim().isEmpty();

            if (isEmpty) {
                logger.log(Level.WARNING, "Required field empty: {0}", field.getName());
                mandatoryLabel.setVisible(true);
                if (isValid) field.requestFocus();
                isValid = false;
            }
        }

        if (!isValid) {
            logger.log(Level.WARNING, "Validation failed with {0} errors", 
                countValidationErrors(requiredFields));
            labelMandatoryMessage.setVisible(true);
            messageTimer.start();
        }
        
        return isValid;
    }

    private int countValidationErrors(Object[][] fields) {
        int count = 0;
        for (Object[] pair : fields) {
            JTextField field = (JTextField) pair[1];
            if (field.getText().trim().isEmpty()) count++;
        }
        return count;
    }
    
    // Clear all form fields after successful save
    private void clearAllFields() {
        // Clear form text fields
        txtRequestor.setText("");
        txtOwner.setText("");
        txtSpouse.setText("");
        txtPurpose.setText("");
        txtAmountPaid.setText("₱0.00");
        txtReceiptNo.setValue(null);
        txtPlaceIssued.setText("");
        txtContactNo.setText("");
        
        datePicker.clearSelectedDate();
        
        // Reset date picker - clear the receiptDateIssuedPicker field
        try {
            receiptDateIssuedPicker.setText("");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not clear date picker field", e);
        }
        
        // Reset checkboxes
        checkBoxSingle.setSelected(false);
        checkBoxMarried.setSelected(false);
        
        // Clear table data
        inputTableModel.setRowCount(0);
        
        // Add empty rows back to table
        for (int i = 0; i < 50; i++) {
            addEmptyRow();
        }
        
        // Reset focus to first field
        txtRequestor.requestFocus();
        
        logger.log(Level.INFO, "All form fields cleared after successful save");
    }
    
    /**
     * Sets up Enter key listener for the entire form using KeyBindings
     * This allows the save action to be triggered by pressing Enter anywhere on the form
     */
    private void setupEnterKeyListener() {
        try {
            // Try to get the root pane from the top-level ancestor
            Container topLevel = this.getTopLevelAncestor();
            JRootPane rootPane = null;
            
            if (topLevel instanceof JFrame) {
                rootPane = ((JFrame) topLevel).getRootPane();
            } else if (topLevel instanceof JDialog) {
                rootPane = ((JDialog) topLevel).getRootPane();
            } else if (topLevel instanceof JWindow) {
                rootPane = ((JWindow) topLevel).getRootPane();
            }
            
            if (rootPane != null) {
                // Create a key stroke for Enter key
                KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
                
                // Create an action for the Enter key
                Action enterAction = new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("Enter key pressed - triggering save");
                        onSaveButtonClick();
                    }
                };
                
                // Register the key binding with the root pane
                rootPane.registerKeyboardAction(
                    enterAction,
                    "EnterKeyAction",
                    enterKey,
                    JComponent.WHEN_IN_FOCUSED_WINDOW
                );
                
                System.out.println("Enter key binding registered successfully on root pane");
            } else {
                System.out.println("Could not find root pane for key binding, using fallback");
                // Fallback: use a simpler approach with key listeners
                setupFallbackEnterKeyListener();
            }
        } catch (Exception e) {
            System.out.println("Error setting up Enter key binding: " + e.getMessage());
            e.printStackTrace();
            // Fallback: use a simpler approach with key listeners
            setupFallbackEnterKeyListener();
        }
    }
    
    /**
     * Fallback method using key listeners if KeyBindings don't work
     */
    private void setupFallbackEnterKeyListener() {
        System.out.println("Setting up fallback Enter key listener");
        
        // Create a key listener that responds to Enter key
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    System.out.println("Fallback: Enter key pressed - triggering save");
                    onSaveButtonClick();
                    e.consume();
                }
            }
        };
        
        // Add the key listener to the form itself
        this.addKeyListener(enterKeyListener);
        
        // Make the form focusable so it can receive key events
        this.setFocusable(true);
        
        // Add key listener to all child components recursively
        addKeyListenerToComponent(this, enterKeyListener);
        
        // Also try to add to the top-level ancestor
        Container topLevel = this.getTopLevelAncestor();
        if (topLevel != null && topLevel != this) {
            topLevel.addKeyListener(enterKeyListener);
            System.out.println("Added key listener to top-level ancestor");
        }
    }
    
    /**
     * Recursively adds key listener to a component and all its children
     */
    private void addKeyListenerToComponent(Container container, KeyAdapter keyListener) {
        // Add key listener to all child components
        for (Component component : container.getComponents()) {
            if (component instanceof Container) {
                addKeyListenerToComponent((Container) component, keyListener);
            } else {
                // Add key listener to non-container components
                component.addKeyListener(keyListener);
            }
        }
    }
    
    private void monitorTableInitialization(CertificateTableTotalLandholding certificateTable, int recordId) {
        SwingUtilities.invokeLater(() -> {
            if (isTableInitialized(certificateTable.certificationTable)) {
                try {
                    logger.log(Level.INFO, "Table is ready. Generating report...");
                    certificateTable.handleReportGeneration(recordId);
                    
                    // After report generation, switch to dashboard and highlight it
                    SwingUtilities.invokeLater(() -> {
                        try {
                            // Get or create FormDashboardTotalLandholding
                            final FormDashboardTotalLandholding dashboard = FormManager.getActiveForm(FormDashboardTotalLandholding.class) != null ? 
                                FormManager.getActiveForm(FormDashboardTotalLandholding.class) : new FormDashboardTotalLandholding();
                            
                            // Show the dashboard form
                            
                            clearAllFields();
                            FormManager.showForm(dashboard);
                            
                            // Refresh the dashboard to show latest data
                            dashboard.formRefresh();
                            
                            // Select the latest row in the table (the newly saved record)
                            Timer selectionTimer = new Timer(500, e -> {
                                if (dashboard.certificateTable != null && dashboard.certificateTable.certificationTable != null) {
                                    JTable table = dashboard.certificateTable.certificationTable;
                                    if (table.getRowCount() > 0) {
                                        // Select the first row (latest record since data is ordered by ID DESC)
                                        table.setRowSelectionInterval(0, 0);
                                        table.scrollRectToVisible(table.getCellRect(0, 0, true));
                                    }
                                }
                            });
                            selectionTimer.setRepeats(false);
                            selectionTimer.start();
                            
                            // Ensure Dashboard is expanded before highlighting
                            SwingUtilities.invokeLater(() -> {
                                Timer drawerTimer = new Timer(100, e -> {
                                    try {
                                        // Get the drawer panel and select Total Landholding
                                        DrawerPanel drawerPanel = MyDrawerBuilder.getDrawerPanelInstance();
                                        if (drawerPanel != null) {
                                            MyDrawerBuilder.selectTotalLandholdingDrawer(drawerPanel);
                                        }
                                    } catch (Exception ex) {
                                        logger.log(Level.WARNING, "Could not select Total Landholding: " + ex.getMessage());
                                    }
                                });
                                drawerTimer.setRepeats(false);
                                drawerTimer.start();
                            });
                            
                            // Highlight the dashboard in the drawer (let automatic highlighting work)
                            Drawer.setSelectedItemClass(FormDashboardTotalLandholding.class);
                            
                            logger.log(Level.INFO, "Successfully switched to FormDashboard2 and highlighted in drawer");
                            
                            // Show success toast message
                            ToastOption option = Toast.createOption()
                                .setAnimationEnabled(true)
                                .setAutoClose(true)
                                .setCloseOnClick(true);
                            
                            // Use the main frame as parent to avoid null parent error
                            Toast.show(FormManager.getFrame(), Toast.Type.SUCCESS, 
                                "Data saved successfully! Report generated and redirected to dashboard.", 
                                option);
                        } catch (Exception ex) {
                            logger.log(Level.SEVERE, "Error switching to dashboard: " + ex.getMessage(), ex);
                        }
                    });
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Error generating report for record ID: " + recordId, ex);
                    JOptionPane.showMessageDialog(this,
                            "Error generating report: " + ex.getMessage(),
                            "Report Generation Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                logger.log(Level.INFO, "Table is not ready. Retrying...");
                monitorTableInitialization(certificateTable, recordId);
            }
        });
    }

    // Method to check if the table is initialized
    private boolean isTableInitialized(JTable table) {
        logger.log(Level.INFO, "Checking table initialization: columns={0}, rows={1}",
                new Object[]{table.getColumnCount(), table.getRowCount()});
        return table.getColumnCount() > 0 && table.getRowCount() > 0;
    }
    

}
