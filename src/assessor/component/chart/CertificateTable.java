package assessor.component.chart;

import assessor.component.report.GenerateReport;
import assessor.component.report.util.CurrencyRenderer;
import assessor.component.report.util.DateRenderer;
import assessor.component.report.util.RedTextRenderer;
import assessor.component.report.util.ReportLoader;
import assessor.component.report.util.TableRightRenderer;
import assessor.component.report.util.TimeRenderer;
import assessor.system.Form;
import assessor.utils.AdvancedLogger;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.util.logging.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

/**
 * CertificateTable class that displays a table with loaded data using ReportLoader.
 */
public class CertificateTable extends Form {

    private static final Logger logger = AdvancedLogger.getLogger(CertificateTable.class.getName());
    public JTabbedPane tabb;
    public JTable certificationTable;
    private final DefaultTableModel tableModel;
    private java.util.List<Runnable> dataLoadListeners = new CopyOnWriteArrayList<>();
    public final ReportLoader reportLoader;

    public CertificateTable() {
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel(new Object[]{"Loading..."}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reportLoader = new ReportLoader(tableModel, new ReportLoader.LoadCallbacks() {
            @Override
            public void onLoadStart() {
                Logger.getLogger(CertificateTable.class.getName()).log(Level.INFO, "Loading data...");
            }

            @Override
            public void onLoadComplete() {
                Logger.getLogger(CertificateTable.class.getName()).log(Level.INFO, "Data load complete.");
                SwingUtilities.invokeLater(() -> configureColumns(certificationTable)); // Configure columns after data load
            }

            @Override
            public void onLoadError(String message) {
                Logger.getLogger(CertificateTable.class.getName()).log(Level.SEVERE, "Error loading data: {0}", message);
            }
        });
        add(createTab());
        loadData();
    }
    
    private Component createTab() {
        tabb = new JTabbedPane();
        tabb.putClientProperty(FlatClientProperties.STYLE, "" +
                "tabType:card");

        // Add the Certification Table tab
        tabb.addTab("Certification", 
            new FlatSVGIcon("assessor/icons/certificate.svg").derive(16, 16), 
            createBorder(createCertificationTable())
        );

        return tabb;
    }
    
    private Component createBorder(Component component) {
        JPanel panel = new JPanel(new MigLayout("fill,insets 7 0 7 0", "[fill]", "[fill]"));
        panel.add(component);
        return panel;
    }

    private JPanel createCertificationTable() {
        JPanel panelTable = new JPanel(new MigLayout("fill,wrap", "[fill]", "[grow]"));

        // Create the table
        certificationTable = new JTable(tableModel);
        certificationTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrollPane = new JScrollPane(certificationTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        certificationTable.getTableHeader().setReorderingAllowed(false);
        certificationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        certificationTable.setRowSorter(sorter);

        // Add listeners for double-click and Enter key
        certificationTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleReportGeneration(certificationTable);
                }
            }
        });
        // Override VK_ENTER to disable moving to the next row
        InputMap inputMap = certificationTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = certificationTable.getActionMap();

        // Remove the default action for VK_ENTER
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none");

        // Add a custom action for VK_ENTER (optional)
        actionMap.put("none", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle the VK_ENTER key without moving to the next row
                handleReportGeneration(certificationTable);
            }
        });

        // Configure table properties
        certificationTable.setAutoCreateColumnsFromModel(true);
        certificationTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });

        // Apply custom styles
        panelTable.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:$Table.background;");
        certificationTable.getTableHeader().putClientProperty(FlatClientProperties.STYLE, ""
                + "height:30;"
                + "hoverBackground:null;"
                + "pressedBackground:null;"
                + "separatorColor:$TableHeader.background;");
        certificationTable.putClientProperty(FlatClientProperties.STYLE, ""
                + "rowHeight:30;"
                + "showHorizontalLines:true;"
                + "intercellSpacing:0,1;");
        scrollPane.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, ""
                + "trackArc:$ScrollBar.thumbArc;"
                + "trackInsets:3,3,3,3;"
                + "thumbInsets:3,3,3,3;"
                + "background:$Table.background;");
        scrollPane.getHorizontalScrollBar().putClientProperty(FlatClientProperties.STYLE, ""
                + "trackArc:$ScrollBar.thumbArc;"
                + "trackInsets:3,3,3,3;"
                + "thumbInsets:3,3,3,3;"
                + "background:$Table.background;");

        // Add the table to the panel
        panelTable.add(scrollPane, "grow, push");
        add(panelTable, BorderLayout.CENTER);
        return panelTable;
    }

    private void loadData() {
        reportLoader.loadData(() -> Logger.getLogger(CertificateTable.class.getName()).log(Level.INFO, "Data loaded successfully."));
    }

    @Override
    public void formRefresh() {
        Logger.getLogger(CertificateTable.class.getName()).log(Level.INFO, "Refreshing data...");
        reportLoader.loadData(() -> Logger.getLogger(CertificateTable.class.getName()).log(Level.INFO, "Data refreshed successfully."));
    }

    public void formDispose() {
        Logger.getLogger(CertificateTable.class.getName()).log(Level.INFO, "Disposing form...");
        reportLoader.cleanup();
    }

    private void configureColumns(JTable table) {
        if (table == null) return;

        TableColumnModel columnModel = table.getColumnModel();
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);
            String colName = model.getColumnName(i).toLowerCase(); // Convert to lowercase for case-insensitive comparison

            // Dynamically configure columns based on column name
            switch (colName) {
                case "id": // Ensure the "ID" column is properly configured
                    column.setHeaderValue("ID");
                    column.setCellRenderer(new TableRightRenderer()); // Right align for readability
                    setColumnWidth(column, 50, 50, 50); // Fixed width
                    break;
                case "patient":
                    column.setHeaderValue("PATIENT");
                    setColumnWidth(column, 200, 200, 200); // Wider for long names
                    break;
                case "relationship":
                    setColumnWidth(column, 80, 80, 80); // Standard width
                    break;
                case "hospitaladdress":
                    column.setHeaderValue("Hospital Address");
                    setColumnWidth(column, 250, 250, 250); // Wider for detailed addresses
                    break;
                case "maritalstatus":
                    column.setHeaderValue("Marital Status");
                    setColumnWidth(column, 90, 90, 90); // Standard width
                    break;
                case "parentguardian":
                    column.setHeaderValue("Parent");
                    setColumnWidth(column, 120, 200, 200); // Flexible width
                    break;
                case "parentguardian2":
                    column.setHeaderValue("Parent");
                    setColumnWidth(column, 200, 200, 200); // Flexible width
                    break;
                case "parentsexifsingle":
                    setColumnWidth(column, 0, 0, 0); // Hidden column
                    column.setResizable(false);
                    break;
                case "certificationdate":
                    column.setHeaderValue("Certification Date");
                    column.setCellRenderer(new DateRenderer());
                    setColumnWidth(column, 120, 120, 120);
                    break;
                case "certificationtime":
                    column.setHeaderValue("Certification Time");
                    column.setCellRenderer(new TimeRenderer());
                    setColumnWidth(column, 100, 120, 120);
                    break;
                case "type":
                    setColumnWidth(column, 100, 100, 100); // Standard width for type
                    break;
                case "amountpaid":
                    column.setHeaderValue("Amount Paid");
                    column.setCellRenderer(new CurrencyRenderer()); // Custom renderer for currency
                    setColumnWidth(column, 80, 80, 80);
                    break;
                case "receiptno":
                    column.setHeaderValue("Receipt No.");
                    column.setCellRenderer(new RedTextRenderer()); // Highlight receipts
                    setColumnWidth(column, 80, 80, 80);
                    break;
                case "receiptdateissued":
                    column.setHeaderValue("Receipt Date Issued");
                    column.setCellRenderer(new DateRenderer());
                    setColumnWidth(column, 120, 120, 120);
                    break;
                case "placeissued":
                    column.setHeaderValue("Place Issued");
                    setColumnWidth(column, 120, 120, 120);
                    break;
                default:
                    setColumnWidth(column, 200, 200, 200); // Default column width
            }
        }
    }
    
    private void setColumnWidth(TableColumn column, int pref, int max, int min) {
        column.setPreferredWidth(pref);
        column.setMaxWidth(max);
        column.setMinWidth(min);
    }

    private void handleReportGeneration(JTable table) {
        try {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null,
                        "Please select a row to generate a report.",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int recordId = getRecordIdFromTable(table, selectedRow);
            String reportType = getReportTypeFromTable(table, selectedRow);

            generateReport(recordId, reportType);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error generating report from table selection", e);
            JOptionPane.showMessageDialog(null,
                    "Error generating report: " + e.getMessage(),
                    "Generation Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void handleReportGeneration(int recordId) {
        if (!isTableInitialized(certificationTable)) {
            JOptionPane.showMessageDialog(null,
                    "Error generating report: Table is not fully initialized.",
                    "Generation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int typeColumn = certificationTable.convertColumnIndexToModel(certificationTable.getColumn("Type").getModelIndex());
            String reportType = getReportTypeFromTable(certificationTable, recordId, typeColumn);

            generateReport(recordId, reportType);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error generating report for record ID: " + recordId, e);
            JOptionPane.showMessageDialog(null,
                    "Error generating report: " + e.getMessage(),
                    "Generation Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isTableInitialized(JTable table) {
        return table.getColumnCount() > 0 && table.getRowCount() > 0;
    }
    
    private int getRecordIdFromTable(JTable table, int row) throws Exception {
        int idColumn = table.convertColumnIndexToModel(table.getColumn("ID").getModelIndex());
        Object rawId = table.getValueAt(row, idColumn);

        if (rawId == null) {
            throw new Exception("Invalid record ID.");
        }

        try {
            return Integer.parseInt(rawId.toString());
        } catch (NumberFormatException e) {
            throw new Exception("Invalid ID format: " + rawId);
        }
    }
    
    private String getReportTypeFromTable(JTable table, int row) throws Exception {
        int typeColumn = table.convertColumnIndexToModel(table.getColumn("Type").getModelIndex());
        Object rawType = table.getValueAt(row, typeColumn);

        if (rawType == null) {
            throw new Exception("Report type is missing.");
        }

        return rawType.toString().trim();
    }

    private String getReportTypeFromTable(JTable table, int recordId, int typeColumn) throws Exception {
        for (int row = 0; row < table.getRowCount(); row++) {
            int idColumn = table.convertColumnIndexToModel(table.getColumn("ID").getModelIndex());
            Object rawId = table.getValueAt(row, idColumn);

            if (rawId != null && Integer.parseInt(rawId.toString()) == recordId) {
                Object rawType = table.getValueAt(row, typeColumn);
                if (rawType != null) {
                    return rawType.toString().trim();
                } else {
                    throw new Exception("Report type is missing for record ID: " + recordId);
                }
            }
        }

        throw new Exception("Record ID not found in the table: " + recordId);
    }
    
private void generateReport(int recordId, String reportType) throws Exception {
    java.util.List<Integer> selectedIDs = new ArrayList<>();
    selectedIDs.add(recordId);

    Map<String, Object> params = new HashMap<>();
    params.put("SelectedIDs", selectedIDs);
    params.put("ReportType", reportType);

    // Get the patient's name for the tab title
    String patientName = getPatientNameByRecordId(recordId);

    JPanel reportViewer = GenerateReport.generateReportPanel(params, reportType + " Report", "/assessor/ui/icons/printer.png");
    updateReportTab(reportViewer, patientName);
}

private String getPatientNameByRecordId(int recordId) {
    try {
        for (int row = 0; row < certificationTable.getRowCount(); row++) {
            int idColumn = certificationTable.convertColumnIndexToModel(certificationTable.getColumn("ID").getModelIndex());
            Object rawId = certificationTable.getValueAt(row, idColumn);

            if (rawId != null && Integer.parseInt(rawId.toString()) == recordId) {
                int patientColumn = certificationTable.convertColumnIndexToModel(certificationTable.getColumn("PATIENT").getModelIndex());
                Object rawPatientName = certificationTable.getValueAt(row, patientColumn);

                if (rawPatientName != null) {
                    return rawPatientName.toString().trim();
                }
            }
        }
    } catch (Exception e) {
        logger.log(Level.SEVERE, "Error retrieving patient name by record ID", e);
    }
    return null; // Return null if the patient name cannot be found
}

private void updateReportTab(JPanel reportPanel, String patientName) {
    final String DEFAULT_TAB_TITLE = "Report";
    String tabTitle = (patientName != null && !patientName.isEmpty()) ? patientName : DEFAULT_TAB_TITLE;

    int reportIndex = -1;

    // Check if the tab with the same title already exists
    for (int i = 0; i < tabb.getTabCount(); i++) {
        if (tabTitle.equals(tabb.getTitleAt(i))) {
            reportIndex = i;
            break;
        }
    }

    Component tabContent = createBorder(reportPanel);

    if (reportIndex == -1) {
        // Create a new tab with the patient's name
        reportIndex = tabb.getTabCount();
        tabb.addTab(tabTitle, null, tabContent);

        // Add a custom tab header with a close button
        tabb.setTabComponentAt(reportIndex, createTabHeader(tabContent, tabTitle));
    } else {
        // Update the existing tab content
        tabb.setComponentAt(reportIndex, tabContent);

        // Reinitialize the tab header to avoid stale event listeners
        tabb.setTabComponentAt(reportIndex, createTabHeader(tabContent, tabTitle));
    }

    tabb.setSelectedIndex(reportIndex);
}

private JPanel createTabHeader(Component tabContent, String tabTitle) {
    JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    tabHeader.setOpaque(false);

    JLabel title = new JLabel(tabTitle);
    title.setIcon(new FlatSVGIcon("assessor/icons/printer.svg").derive(16, 16));
    title.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

    JButton closeButton = new JButton(new FlatSVGIcon("assessor/icons/close.svg", 12, 12));
    closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    closeButton.setFocusable(true);
    closeButton.setOpaque(false);
    closeButton.setContentAreaFilled(true);

    // Remove old ActionListeners to avoid duplicates
    for (ActionListener al : closeButton.getActionListeners()) {
        closeButton.removeActionListener(al);
    }

    // Add the action listener for the close button
    closeButton.addActionListener(e -> {
        int index = tabb.indexOfComponent(tabContent);
        if (index != -1) {
            tabb.remove(index);
        }
    });

    tabHeader.add(title);
    tabHeader.add(closeButton);

    return tabHeader;
}
}