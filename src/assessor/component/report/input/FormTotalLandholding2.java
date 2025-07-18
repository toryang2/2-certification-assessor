package assessor.component.report.input;

import assessor.auth.SessionManager;
import assessor.component.report.util.*;
import static assessor.component.report.util.DatabaseSaveHelper.saveAutocompleteValue;
import assessor.system.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.*;
import java.sql.SQLException;
import java.text.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.DateSelectionAble;

/**
 *
 * @author user
 */
public class FormTotalLandholding2 extends Form {
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
    private JTextField txtType;
    private JTextField txtUserInitials;
    private JTextField txtSignatory;
    private JTextField receiptDateIssuedPicker;
    private JLabel labelMaritalStatus;
    private JLabel labelTitle;
    private JLabel labelOwner;
    private JLabel labelMandatoryOwner;
    private JLabel labelSpouse;
    private JLabel labelPurpose;
    private JLabel labelMandatoryPurpose;
    private JLabel labelAmount;
    private JLabel labelReceiptNo;
    private JLabel labelDateIssued;
    private JLabel labelPlaceIssued;
    private JLabel labelMandatoryMessage;
    private JTable inputTable;
    private DefaultTableModel inputTableModel;
    private JButton btnAddRow, btnRemoveRow;
    
    public boolean isSaveSuccessful() {
        return saveSuccessful;
    }
    
    public FormTotalLandholding2() {
        setLayout(new MigLayout("al, insets 0"));
        initComponents();
        setupAmountField();
        setupReceiptNoField();
        applyUppercaseFilterToTextFields();
        List<String> columns;
        try {
            columns = TotalLandholding_TableUtil.getColumnNames("reports_total_landholding");
        } catch (Exception e) {
            e.printStackTrace();
            columns = List.of();
        }
        DefaultTableModel inputTableModel = new DefaultTableModel(columns.toArray(), 0);
        inputTable = new JTable(inputTableModel);
        add(new JScrollPane(inputTable), "span, growx, wrap");
    
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
        labelMandatoryPurpose.setVisible(false);
        labelMandatoryMessage.setVisible(false);
        
        messageTimer = new Timer(3000, e -> {
            labelMandatoryMessage.setVisible(false);
        });
        messageTimer.setRepeats(false);
        removeAll();
        
        JPanel contentPanel = new JPanel(new MigLayout(
        "insets 20 30 20 30, gap 5 15",
        "[left][5:5:5][50:50:50][50:50:50][70:70:70][left][100:100:100]", 
        "[][][][][][][][][][][]"
        ));
        //Row 0: Title
        contentPanel.add(labelTitle, "span 7, center, wrap");
        //Row 1: Marital Status
        JPanel maritalPanel = new JPanel(new MigLayout("gap 15, insets 0, align center"));
        maritalPanel.add(checkBoxSingle);
        maritalPanel.add(checkBoxMarried);
        contentPanel.add(maritalPanel, "span 7, center, wrap");
        //Row 2: Owner
        contentPanel.add(labelOwner, "cell 0 2");
        contentPanel.add(labelMandatoryOwner);
        contentPanel.add(txtOwner, "cell 2 2 5 1, growx, pushx, w 100%");
        
        contentPanel.add(labelSpouse, "cell 0 3");
        contentPanel.add(txtSpouse, "cell 2 3 5 1, growx, pushx, w 100%");
        
        contentPanel.add(labelPurpose, "cell 0 4");
        contentPanel.add(labelMandatoryPurpose);
        contentPanel.add(txtPurpose, "cell 2 4 5 1, growx, pushx, w 100%");
        
        contentPanel.add(labelAmount, "cell 0 5");
        contentPanel.add(txtAmountPaid, "cell 2 5 3 1, w 120");
        contentPanel.add(labelReceiptNo, "cell 5 5");
        contentPanel.add(txtReceiptNo, "cell 6 5, growx, wrap");
        
        contentPanel.add(labelDateIssued, "cell 0 6");
        contentPanel.add(receiptDateIssuedPicker, "cell 2 6 2 1, w 120");
        contentPanel.add(labelPlaceIssued, "cell 4 6");
        contentPanel.add(txtPlaceIssued, "cell 5 6 2 1, growx, wrap");
        
        // --- Save button logic ---
        contentPanel.add(labelMandatoryMessage, "cell 0 7 4 1, left");
        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> onSaveButtonClick());
        contentPanel.add(btnSave, "span 7, right, wrap");
        
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.add(contentPanel);
        
        add(wrapper);
    }
    
    private void initComponents() {
        labelOwner = new JLabel("Owner");
        labelMandatoryOwner = new JLabel("<html><font color='red'>*</font></html>"); // or whatever indicator you want for mandatory
        labelTitle = new JLabel("Total Landholding Form"); // or your preferred title
        labelSpouse = new JLabel("Spouse");
        labelPurpose = new JLabel("Purpose");
        labelMandatoryPurpose = new JLabel("<html><font color='red'>*</font></html>");
        labelMandatoryMessage = new JLabel("<html><font color='red'>*</font> Mandatory fields required</html>");
        labelAmount = new JLabel("Amount");
        labelReceiptNo = new JLabel("Receipt No.");
        labelDateIssued = new JLabel("Date Issued");
        labelPlaceIssued = new JLabel("Place Issued");
        labelMaritalStatus = new JLabel();
        checkBoxSingle = new JCheckBox("Single");
        checkBoxMarried = new JCheckBox("Married");
        txtMaritalStatus = new JTextField();
        txtOwner = new JTextField();
        txtSpouse = new JTextField();
        txtPurpose = new JTextField();
        txtAmountPaid = new JTextField();
        txtReceiptNo = new JFormattedTextField();
        receiptDateIssuedPicker = new JTextField();
        txtPlaceIssued = new JTextField();
        txtType = new JTextField();
        txtUserInitials = new JTextField();
        txtSignatory = new JTextField();
    }
    
    private void applyUppercaseFilterToTextFields() {
        UppercaseDocumentFilter uppercaseFilter = new UppercaseDocumentFilter();
        JTextField[] textFields = {
//            txtTemp,
//            txtTemp
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
                saveAutocompleteValue("txtPlaceIssued", txtPlaceIssued.getText().trim());
                
                String signatory = DatabaseTotalLandholdingHelper.getAssessorName(1);
                if (signatory != null) {
                    reportData.put("signatory", signatory);
                    logger.log(Level.INFO, "Signatory found : {0}", signatory);
                } else {
                    logger.log(Level.WARNING, "No default assessor configured");
                    JOptionPane.showMessageDialog(this,
                            "Default assessor not configured.",
                            "Configuration Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
                String userInitials = SessionManager.getInstance().getUserInitials();
                if (userInitials != null) {
                    reportData.put("user_initials", userInitials);
                } else {
                    logger.log(Level.WARNING, "User initial not found");
                }
                if (datePicker.getSelectedDate() != null) {
                    reportData.put("receipt_date_issued", datePicker.getSelectedDate());
                }
                // Collect data from your form fields
                reportData.put("marital_status", getMaritalStatus());
                reportData.put("owner", txtOwner.getText());
                reportData.put("spouse", txtSpouse.getText());
                reportData.put("purpose", txtPurpose.getText());
                reportData.put("requested_date", LocalDate.now());
                reportData.put("requested_time", LocalTime.now());
                reportData.put("amount_paid", getAmountPaid());
                reportData.put("receipt_no", getReceiptNumber());
                reportData.put("place_issued", txtPlaceIssued.getText());
                // Add other fields as needed, matching your database column names

                // Save to DB
                boolean success = DatabaseTotalLandholdingHelper.saveReport("Total Landholding", reportData);

                // Show result
                if (success) {
                    JOptionPane.showMessageDialog(this, "Saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    saveSuccessful = true;
                    if (saveCallBack != null) {
                        saveCallBack.accept(true);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
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
            return null; // Or return ""; if you prefer an empty string instead of null
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
                String numericPart = text.substring(1).replace(",", ""); // Allow commas by ignoring them in validation
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
                            // Fallback to unformatted if invalid number
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
}
