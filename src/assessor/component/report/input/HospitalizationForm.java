/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package assessor.component.report.input;

import assessor.component.chart.CertificateTable;
import assessor.component.report.util.DataChangeNotifier;
import assessor.component.report.util.DatabaseSaveHelper;
import assessor.system.AllForms;
import assessor.system.Form;
import assessor.system.FormManager;
import com.formdev.flatlaf.extras.FlatSVGUtils;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Set;
import javax.swing.*;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.*;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.DateSelectionAble;
import java.util.logging.*;
import raven.modal.component.SimpleModalBorder;

/**
 *
 * @author Toryang
 */
public class HospitalizationForm extends Form {
    private static final Logger logger = Logger.getLogger(HospitalizationForm.class.getName());
    private Consumer<Boolean> saveCallback;
    private DatePicker datePicker;
    private Timer messageTimer;
    private boolean saveSuccessful = false;

    public boolean isSaveSuccessful() {
        return saveSuccessful;
    }
    
//    @Override
//    public void dispose(){
//        if (datePicker !=null) {
//            datePicker.closePopup();
//        }
//        super.dispose();
//    }
//    private DatePicker receiptDateIssuedPickerUI;
    /**
     * Creates new form Input
     */
    public HospitalizationForm() {
        setLayout(new MigLayout("al center center, insets 0"));
        initComponents();
//        setupActions();
        setupAmountField();
        setupReceiptNoField();
//        setTitle("Hospitalization");
//        setIconImages( FlatSVGUtils.createWindowIconImages( "/assessor/ui/icons/certificate.svg" ) );
        
        // Gender combo model
        DefaultComboBoxModel<String> defaultParentSexModel = new DefaultComboBoxModel<>();
        defaultParentSexModel.addElement("Male");
        defaultParentSexModel.addElement("Female");

        // Relationship combo model
        DefaultComboBoxModel<String> relationshipModel = new DefaultComboBoxModel<>();
        relationshipModel.addElement("son");
        relationshipModel.addElement("daughter");
        relationshipModel.addElement("spouse");

        DefaultComboBoxModel<String> guardianModel = new DefaultComboBoxModel<>();
        guardianModel.addElement("son");

        JCheckBox[] maritalCheckboxes = {
            checkBoxMarried, 
            checkBoxSingle, 
            checkBoxGuardian
        };

        // Add item listener to each checkbox
        for (JCheckBox checkbox : maritalCheckboxes) {
            checkbox.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    // Uncheck other boxes when one is checked
                    for (JCheckBox other : maritalCheckboxes) {
                        if (other != e.getSource()) {
                            other.setSelected(false);
                        }
                    }

                    // Update combos based on selection
                    if (checkbox == checkBoxMarried) {
                        comboParentSex.setEnabled(false);
                        comboParentSex.setSelectedItem("Married");
                        comboRelationship.setEnabled(true);
                        comboRelationship.setModel(relationshipModel);
                        comboRelationship.setSelectedIndex(0);
                    } 
                    else if (checkbox == checkBoxSingle) {
                        comboParentSex.setEnabled(true);
                        comboParentSex.setModel(defaultParentSexModel);
                        comboParentSex.setSelectedIndex(0);
                        comboRelationship.setEnabled(true);
                        comboRelationship.setModel(relationshipModel);
                        comboRelationship.setSelectedIndex(0);
                    }
                    else if (checkbox == checkBoxGuardian) {
                        comboParentSex.setEnabled(false);
                        comboParentSex.setSelectedItem("Guardian");
                        comboRelationship.setEnabled(false);
                        comboRelationship.setModel(guardianModel);
                        comboRelationship.setSelectedIndex(0);
                    }
                }
                else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    // Check if any checkbox is still selected
                    boolean anySelected = false;
                    for (JCheckBox cb : maritalCheckboxes) {
                        if (cb.isSelected()) {
                            anySelected = true;
                            break;
                        }
                    }

                    // If none selected, force Married to be checked
                    if (!anySelected) {
                        checkBoxMarried.setSelected(true);
                    }
                }

                // Focus on parent guardian field
                SwingUtilities.invokeLater(() -> {
                    txtParentGuardian.requestFocusInWindow();
                });
            });
        }

        // Initial state setup
        checkBoxMarried.setSelected(true);
        comboParentSex.setEnabled(false);
        comboParentSex.setSelectedItem("Married");
        comboRelationship.setModel(relationshipModel);
        comboRelationship.setSelectedIndex(0);
        
        SwingUtilities.invokeLater(() -> {
            txtParentGuardian.requestFocusInWindow();
        });
        
        datePicker = new DatePicker();
        JFormattedTextField receiptDateIssuedPicker = new JFormattedTextField();
        datePicker.setEditor(receiptDateIssuedPicker);
        datePicker.setDateSelectionMode(DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED);
        datePicker.setDateFormat("MM/dd/yyyy");
        datePicker.setUsePanelOption(true);
        datePicker.setCloseAfterSelected(true);
        datePicker.setDateSelectionAble(new DateSelectionAble() {
            @Override
            public boolean isDateSelectedAble(LocalDate localDate) {
                return !localDate.isAfter(LocalDate.now());
            }
        });
        
        jLabelMandatoryMessage.setVisible(false);
        jLabelMandatoryParentGuardian.setVisible(false); 
        jLabelMandatoryParentStudent.setVisible(false); 
        jLabelMandatoryAddress.setVisible(false); 
        jLabelMandatoryHospital.setVisible(false); 
        jLabelMandatoryHospitalAddress.setVisible(false); 
        jLabelMandatoryMessage.setVisible(false);
            
        messageTimer = new Timer(3000, e -> {
            jLabelMandatoryMessage.setVisible(false);
        });
        messageTimer.setRepeats(false); // Only trigger once
        
        // Clear existing layout
//        Container contentPane = getContentPane();
        removeAll();

        // Set up MigLayout
        JPanel contentPanel = new JPanel(new MigLayout(
            "insets 20 30 20 30, gap 5 15",
            // 6-column layout: [labels][field1][field2][labels][field3][fill]
            "[left][5:5:5][50:50:50][50:50:50][70:70:70][left][100:100:100]", 
            "[][][][][][][][][][][]"
        ));

        // Row 0: Title
        contentPanel.add(labelTitle, "span 7, center, wrap");

        // Row 1: Marital Status
        JPanel maritalPanel = new JPanel(new MigLayout("gap 15, insets 0, align center"));
        maritalPanel.add(checkBoxMarried);
        maritalPanel.add(checkBoxSingle);
        maritalPanel.add(checkBoxGuardian);
        contentPanel.add(maritalPanel, "span 7, center, wrap");

        // Row 2: Parent/Guardian
        contentPanel.add(labelParentGuardian, "cell 0 2");
        contentPanel.add(jLabelMandatoryParentGuardian);
        contentPanel.add(txtParentGuardian, "cell 2 2 4 1, growx, pushx, w 100%");
        contentPanel.add(comboParentSex, "cell 6 2, growx, pushx, w 100%, wrap");

        // Row 3: Parent/Guardian 2
        contentPanel.add(labelParentGuardian2, "cell 0 3");
        contentPanel.add(txtParentGuardian2, "cell 2 3 5 1, growx, pushx, w 100%, wrap");

        // Row 4: Patient/Student
        contentPanel.add(labelPatientStudent);
        contentPanel.add(jLabelMandatoryParentStudent);
        contentPanel.add(txtPatientStudent, "cell 2 4 5 1, growx, pushx, w 100%, wrap");

        // Row 5: Address
        contentPanel.add(labelAddress);
        contentPanel.add(jLabelMandatoryAddress);
        contentPanel.add(txtAddress, "cell 2 5 3 1, growx, pushx, w 100%, wrap");
        contentPanel.add(labelRelationship, "cell 5 5");
        contentPanel.add(comboRelationship, "cell 6 5, growx, wrap");

        // Row 6: Hospital
        contentPanel.add(labelHospital, "cell 0 6");
        contentPanel.add(jLabelMandatoryHospital);
        contentPanel.add(txtHospital, "cell 2 6 5 1, growx, pushx, w 100%, wrap");

        // Row 7: Hospital Address
        contentPanel.add(labelHospitalAddress, "cell 0 7");
        contentPanel.add(jLabelMandatoryHospitalAddress);
        contentPanel.add(txtHospitalAddress, "cell 2 7 5 1, growx, pushx, w 100%, wrap");

        // Row 8: Amount & Receipt
        contentPanel.add(labelAmount, "cell 0 8");
        contentPanel.add(txtAmount, "cell 2 8 3 1, w 120");
        contentPanel.add(labelReceiptNo, "cell 5 8");
        contentPanel.add(txtReceiptNo, "cell 6 8, growx, wrap");

        // Row 9: Date & Place
        contentPanel.add(labelDateIssued, "cell 0 9");
        contentPanel.add(receiptDateIssuedPicker, "cell 2 9 2 1, w 120"); // Using DatePicker
        contentPanel.add(labelPlaceIssued, "cell 4 9");
        contentPanel.add(txtPlaceIssued, "cell 5 9 2 1, growx, wrap");

        // Row 10: Buttons
        contentPanel.add(jLabelMandatoryMessage,"cell 0 10 3 1, left");
//        buttonPanel.add(btnSave);
//        buttonPanel.add(btnCancel);
//        contentPanel.add(buttonPanel, "span 6, right");

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.add(contentPanel);
        
        add(wrapper);
    }
    
    public void cleanup() {
        if (datePicker != null) {
            datePicker.closePopup();
        }
    }
    
    private void setupAmountField() {
        // Initial placeholder
        txtAmount.setText("₱0.00");
        
        // Focus listeners for formatting
        txtAmount.addFocusListener(new java.awt.event.FocusAdapter() {
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
        ((AbstractDocument) txtAmount.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) 
                throws BadLocationException {
                super.insertString(fb, offset, text, attr);
                SwingUtilities.invokeLater(() -> reformatAmount(fb.getDocument()));
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) 
                throws BadLocationException {
                super.remove(fb, offset, length);
                SwingUtilities.invokeLater(() -> reformatAmount(fb.getDocument()));
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                throws BadLocationException {
                super.replace(fb, offset, length, text, attrs);
                SwingUtilities.invokeLater(() -> reformatAmount(fb.getDocument()));
            }

            private boolean isValid(String text) {
                if (!text.startsWith("₱")) return false;
                String numericPart = text.substring(1).replace(",", ""); // Allow commas by ignoring them in validation
                return numericPart.matches("^\\d*(\\.\\d*)?$");
            }
            
            private void reformatAmount(Document doc) {
                try {
                    String originalText = doc.getText(0, doc.getLength());
                    String text = originalText.replace("₱", "").replaceAll(",", "");

                    // Handle empty case
                    if (text.isEmpty()) {
                        txtAmount.setText("₱");
                        
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
                            // Fallback to unformatted if invalid number
                            integerPart = text.split("\\.")[0];
                        }
                    }

                    int originalCaretPos = txtAmount.getCaretPosition();

                    // Build new formatted text
                    String newText = "₱" + integerPart + decimalPart;
                    txtAmount.setText(newText);

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
                    txtAmount.setCaretPosition(newCaretPos);

                } catch (BadLocationException | NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });
        txtAmount.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                handleAmountFocusGained();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                handleAmountFocusLost();
            }
        });
    }
    
    private void handleAmountFocusGained() {
        if (txtAmount.getText().equals("₱0.00")) {
            txtAmount.setText("₱.00");
            txtAmount.setCaretPosition(1);
        }
    }

    private void handleAmountFocusLost() {
        try {
            double amount = parseDouble(txtAmount.getText());
            txtAmount.setText(formatAmount(amount));
        } catch (NumberFormatException ex) {
            txtAmount.setText("₱0.00");
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
    
    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(
            this, 
            message,
            "Validation Error",
            JOptionPane.WARNING_MESSAGE
        );
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
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                throws BadLocationException {
            super.insertString(fb, offset, text.replaceAll("[^\\d]", ""), attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
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
    
    public void setSaveCallback(Consumer<Boolean> callback) {
        this.saveCallback = callback;
    }
//    private void setupActions() {
//        btnSave.addActionListener(this::saveAction);
//        btnCancel.addActionListener(e ->  {
//            cleanup();
//            
//            Window window = SwingUtilities.getWindowAncestor(HospitalizationForm.this);
//            if (window != null) {
//                window.dispose();
//            }
//        });
//    }

public void saveAction(ActionEvent e) {
    try {
        handleAmountFocusLost();
        logger.log(Level.INFO, "Starting save action...");

        if (validateInput()) {
            logger.log(Level.INFO, "Input validation passed");

            Map<String, Object> reportData = collectReportData();
            int newestRecordId = DatabaseSaveHelper.saveReportAndGetNewestId("Hospitalization", reportData);

            if (newestRecordId != -1) {
                logger.log(Level.INFO, "Database save successful. Newest record ID: {0}", newestRecordId);

                // Fetch CertificateTable from FormManager
                CertificateTable certificateTable = FormManager.getActiveForm(CertificateTable.class);
                if (certificateTable == null) {
                    logger.log(Level.SEVERE, "CertificateTable is not available in FormManager!");
                    JOptionPane.showMessageDialog(this,
                            "Failed to refresh the table. CertificateTable is not active.",
                            "Table Refresh Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (certificateTable.reportLoader == null) {
                    logger.log(Level.SEVERE, "CertificateTable's reportLoader is null!");
                    JOptionPane.showMessageDialog(this,
                            "Failed to refresh the table. reportLoader is not initialized.",
                            "Table Refresh Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Refresh the table
                logger.log(Level.INFO, "Found CertificateTable and reportLoader. Initiating data refresh...");
                certificateTable.reportLoader.loadData(() -> {
                    logger.log(Level.INFO, "Table refresh complete. Generating report...");
                    monitorTableInitialization(certificateTable, newestRecordId);
                });

                // Notify success callback and close modal
                saveSuccessful = true;
                if (saveCallback != null) {
                    saveCallback.accept(true);
                }
            } else {
                logger.log(Level.WARNING, "Failed to save hospitalization record.");
                JOptionPane.showMessageDialog(this,
                        "Failed to save hospitalization record.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    } catch (Exception ex) {
        logger.log(Level.SEVERE, "Critical error in save action", ex);
        handleSaveError(ex);
    }
}

private void monitorTableInitialization(CertificateTable certificateTable, int recordId) {
    SwingUtilities.invokeLater(() -> {
        if (isTableInitialized(certificateTable.certificationTable)) {
            try {
                logger.log(Level.INFO, "Table is ready. Generating report...");
                certificateTable.handleReportGeneration(recordId);
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

private Map<String, Object> collectReportData() {
    Map<String, Object> reportData = new HashMap<>();

    // Add signatory
    String signatory = DatabaseSaveHelper.getAssessorName(1);
    if (signatory != null) {
        reportData.put("Signatory", signatory);
        logger.log(Level.INFO, "Signatory found: {0}", signatory);
    } else {
        logger.log(Level.WARNING, "No default assessor configured");
        JOptionPane.showMessageDialog(this,
                "Default assessor not configured.",
                "Configuration Warning",
                JOptionPane.WARNING_MESSAGE);
    }

    // Add marital status
    reportData.put("MaritalStatus", getMaritalStatus());

    // Add text fields
    reportData.put("ParentGuardian", txtParentGuardian.getText());
    reportData.put("ParentGuardian2", txtParentGuardian2.getText());
    reportData.put("Patient", txtPatientStudent.getText());
    reportData.put("Barangay", txtAddress.getText());
    reportData.put("Hospital", txtHospital.getText());
    reportData.put("HospitalAddress", txtHospitalAddress.getText());
    reportData.put("PlaceIssued", txtPlaceIssued.getText());

    // Add parent sex
    reportData.put("ParentSexIfSingle", getParentSex());

    // Add relationship
    reportData.put("Relationship", getRelationship());

    // Add amount paid
    reportData.put("AmountPaid", getAmountPaid());

    // Add receipt number
    reportData.put("ReceiptNo", getReceiptNumber());

    // Add dates
    reportData.put("CertificationDate", LocalDate.now());
    reportData.put("CertificationTime", LocalTime.now());
    if (datePicker.getSelectedDate() != null) {
        reportData.put("ReceiptDateIssued", datePicker.getSelectedDate());
    }

    return reportData;
}

private String getMaritalStatus() {
    if (checkBoxMarried.isSelected()) return "MARRIED";
    if (checkBoxSingle.isSelected()) return "SINGLE";
    if (checkBoxGuardian.isSelected()) return "GUARDIAN";
    return "Unknown";
}

private String getParentSex() {
    if (comboParentSex.isEnabled()) {
        return comboParentSex.getSelectedItem().toString();
    }
    return checkBoxMarried.isSelected() ? "Married" : "Guardian";
}

private String getRelationship() {
    if (checkBoxGuardian.isSelected()) return "Legal Guardian";
    return comboRelationship.getSelectedItem().toString();
}

private String getAmountPaid() {
    try {
        double amount = parseDouble(txtAmount.getText());
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
        return null; // Or return ""; if you prefer an empty string instead of null
    } catch (Exception ex) {
        logger.log(Level.SEVERE, "Error processing receipt number", ex);
        throw new RuntimeException("Invalid receipt number", ex);
    }
}
    
public SimpleModalBorder createCustomBorder() {
    return new SimpleModalBorder(
        this, 
        "Hospitalization", 
        SimpleModalBorder.OK_CANCEL_OPTION,
        (controller, action) -> {
            if (action == SimpleModalBorder.OK_OPTION) {
                saveAction(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
                if (saveSuccessful) {
                    controller.close();
                    SwingUtilities.invokeLater(() -> {
                        // Notify DataChangeNotifier to refresh tables
                        DataChangeNotifier.getInstance().notifyDataChange();
                    });
                } else {
                    controller.consume(); // Prevent closing the modal if save fails
                }
            }
        }
    );
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
    
    // Update validation to match your actual form fields
    private boolean validateInput() {
        logger.log(Level.FINE, "Starting input validation");
        
        Object[][] requiredFields = {
            { jLabelMandatoryParentGuardian, txtParentGuardian },
            { jLabelMandatoryParentStudent, txtPatientStudent },
            { jLabelMandatoryAddress, txtAddress },
            { jLabelMandatoryHospital, txtHospital },
            { jLabelMandatoryHospitalAddress, txtHospitalAddress }
        };

        boolean isValid = true;
        
        // Combo box validation
        if (comboParentSex.getSelectedItem() == null) {
            logger.log(Level.WARNING, "Parent sex not selected");
            isValid = false;
        }
    
        if (comboRelationship.getSelectedItem() == null) {
            logger.log(Level.WARNING, "Relationship not selected");
            isValid = false;
        }

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
            jLabelMandatoryMessage.setVisible(true);
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
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelTitle = new javax.swing.JLabel();
        labelParentGuardian = new javax.swing.JLabel();
        txtParentGuardian = new javax.swing.JTextField();
        jLabelMandatoryParentGuardian = new javax.swing.JLabel();
        comboParentSex = new javax.swing.JComboBox<>();
        labelParentGuardian2 = new javax.swing.JLabel();
        txtParentGuardian2 = new javax.swing.JTextField();
        checkBoxSingle = new javax.swing.JCheckBox();
        checkBoxMarried = new javax.swing.JCheckBox();
        checkBoxGuardian = new javax.swing.JCheckBox();
        labelPatientStudent = new javax.swing.JLabel();
        txtPatientStudent = new javax.swing.JTextField();
        labelRelationship = new javax.swing.JLabel();
        txtAddress = new javax.swing.JTextField();
        comboRelationship = new javax.swing.JComboBox<>();
        labelAddress = new javax.swing.JLabel();
        labelHospital = new javax.swing.JLabel();
        txtHospital = new javax.swing.JTextField();
        labelHospitalAddress = new javax.swing.JLabel();
        txtHospitalAddress = new javax.swing.JTextField();
        labelAmount = new javax.swing.JLabel();
        txtAmount = new javax.swing.JTextField();
        labelReceiptNo = new javax.swing.JLabel();
        labelDateIssued = new javax.swing.JLabel();
        labelPlaceIssued = new javax.swing.JLabel();
        txtPlaceIssued = new javax.swing.JTextField();
        btnSave = new javax.swing.JToggleButton();
        jLabelMandatoryParentStudent = new javax.swing.JLabel();
        jLabelMandatoryAddress = new javax.swing.JLabel();
        jLabelMandatoryHospital = new javax.swing.JLabel();
        jLabelMandatoryHospitalAddress = new javax.swing.JLabel();
        btnCancel = new javax.swing.JButton();
        receiptDateIssuedPicker = new javax.swing.JTextField();
        jLabelMandatoryMessage = new javax.swing.JLabel();
        txtReceiptNo = new javax.swing.JFormattedTextField();

        labelTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        labelTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelTitle.setText("CLIENT INFORMATION");
        add(labelTitle);

        labelParentGuardian.setText("Parent/ Guardian  ");
        add(labelParentGuardian);

        txtParentGuardian.setToolTipText("");
        add(txtParentGuardian);

        jLabelMandatoryParentGuardian.setForeground(new java.awt.Color(255, 0, 0));
        jLabelMandatoryParentGuardian.setText("*");
        add(jLabelMandatoryParentGuardian);

        comboParentSex.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Male", "Female" }));
        add(comboParentSex);

        labelParentGuardian2.setText("Parent/ Guardian");
        add(labelParentGuardian2);
        add(txtParentGuardian2);

        checkBoxSingle.setText("Single");
        checkBoxSingle.setToolTipText("");
        add(checkBoxSingle);

        checkBoxMarried.setText("Married");
        add(checkBoxMarried);

        checkBoxGuardian.setText("Guardian");
        add(checkBoxGuardian);

        labelPatientStudent.setText("Patient/ Student");
        add(labelPatientStudent);
        add(txtPatientStudent);

        labelRelationship.setText("Relationship");
        add(labelRelationship);
        add(txtAddress);

        comboRelationship.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "son", "daughter", "spouse" }));
        comboRelationship.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboRelationshipActionPerformed(evt);
            }
        });
        add(comboRelationship);

        labelAddress.setText("Address");
        add(labelAddress);

        labelHospital.setText("Hospital");
        add(labelHospital);
        add(txtHospital);

        labelHospitalAddress.setText("Hospital Address");
        add(labelHospitalAddress);
        add(txtHospitalAddress);

        labelAmount.setText("Amount");
        add(labelAmount);
        add(txtAmount);

        labelReceiptNo.setText("Receipt No.");
        add(labelReceiptNo);

        labelDateIssued.setText("Date Issued");
        add(labelDateIssued);

        labelPlaceIssued.setText("Place Issued");
        add(labelPlaceIssued);
        add(txtPlaceIssued);

        btnSave.setText("Save");
        add(btnSave);

        jLabelMandatoryParentStudent.setForeground(new java.awt.Color(255, 0, 0));
        jLabelMandatoryParentStudent.setText("*");
        add(jLabelMandatoryParentStudent);

        jLabelMandatoryAddress.setForeground(new java.awt.Color(255, 0, 0));
        jLabelMandatoryAddress.setText("*");
        add(jLabelMandatoryAddress);

        jLabelMandatoryHospital.setForeground(new java.awt.Color(255, 0, 0));
        jLabelMandatoryHospital.setText("*");
        add(jLabelMandatoryHospital);

        jLabelMandatoryHospitalAddress.setForeground(new java.awt.Color(255, 0, 0));
        jLabelMandatoryHospitalAddress.setText("*");
        add(jLabelMandatoryHospitalAddress);

        btnCancel.setText("Cancel");
        add(btnCancel);
        add(receiptDateIssuedPicker);

        jLabelMandatoryMessage.setText("<html><font color='red'>*</font> Mandatory fields required</html>");
        add(jLabelMandatoryMessage);

        txtReceiptNo.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        add(txtReceiptNo);
    }// </editor-fold>//GEN-END:initComponents

    private void comboRelationshipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboRelationshipActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comboRelationshipActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JToggleButton btnSave;
    private javax.swing.JCheckBox checkBoxGuardian;
    private javax.swing.JCheckBox checkBoxMarried;
    private javax.swing.JCheckBox checkBoxSingle;
    private javax.swing.JComboBox<String> comboParentSex;
    private javax.swing.JComboBox<String> comboRelationship;
    private javax.swing.JLabel jLabelMandatoryAddress;
    private javax.swing.JLabel jLabelMandatoryHospital;
    private javax.swing.JLabel jLabelMandatoryHospitalAddress;
    private javax.swing.JLabel jLabelMandatoryMessage;
    private javax.swing.JLabel jLabelMandatoryParentGuardian;
    private javax.swing.JLabel jLabelMandatoryParentStudent;
    private javax.swing.JLabel labelAddress;
    private javax.swing.JLabel labelAmount;
    private javax.swing.JLabel labelDateIssued;
    private javax.swing.JLabel labelHospital;
    private javax.swing.JLabel labelHospitalAddress;
    private javax.swing.JLabel labelParentGuardian;
    private javax.swing.JLabel labelParentGuardian2;
    private javax.swing.JLabel labelPatientStudent;
    private javax.swing.JLabel labelPlaceIssued;
    private javax.swing.JLabel labelReceiptNo;
    private javax.swing.JLabel labelRelationship;
    private javax.swing.JLabel labelTitle;
    private javax.swing.JTextField receiptDateIssuedPicker;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JTextField txtAmount;
    private javax.swing.JTextField txtHospital;
    private javax.swing.JTextField txtHospitalAddress;
    private javax.swing.JTextField txtParentGuardian;
    private javax.swing.JTextField txtParentGuardian2;
    private javax.swing.JTextField txtPatientStudent;
    private javax.swing.JTextField txtPlaceIssued;
    private javax.swing.JFormattedTextField txtReceiptNo;
    // End of variables declaration//GEN-END:variables
}