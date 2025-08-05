package assessor.component.chart;

import assessor.component.report.*;
import assessor.component.report.util.*;
import assessor.auth.*;
import assessor.system.Form;
import assessor.utils.AdvancedLogger;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.*;

public class CertificateTable extends Form {

    private static final Logger logger = AdvancedLogger.getLogger(CertificateTable.class.getName());
    public JTabbedPane tabb;
    public JTable certificationTable;
    private final DefaultTableModel tableModel;
    private java.util.List<Runnable> dataLoadListeners = new CopyOnWriteArrayList<>();
    public final ReportLoader reportLoader;
    private JCheckBox selectAllCheckBox;

    public CertificateTable() {
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (SessionManager.getInstance().getAccessLevel() == 1) {
                    // If any row is checked, allow editing for all columns except the checkbox
                    boolean anyChecked = false;
                    for (int i = 0; i < getRowCount(); i++) {
                        Object val = getValueAt(i, 0);
                        if (Boolean.TRUE.equals(val)) {
                            anyChecked = true;
                            break;
                        }
                    }
                    if (anyChecked && column != 0) return true;
                    // Always allow checkbox column
                    if (column == 0) return true;
                }
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (SessionManager.getInstance().getAccessLevel() == 1 && columnIndex == 0)
                    return Boolean.class;
                return super.getColumnClass(columnIndex);
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
                SwingUtilities.invokeLater(() -> configureColumns(certificationTable));
            }
            @Override
            public void onLoadError(String message) {
                Logger.getLogger(CertificateTable.class.getName()).log(Level.SEVERE, "Error loading data: {0}", message);
            }
        });
        reportLoader.startPolling(3000); // Poll every 3 seconds
        add(createTab());
        loadData();
    }

    private Component createTab() {
        tabb = new JTabbedPane();
        tabb.putClientProperty(FlatClientProperties.STYLE, "tabType:card");
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

        certificationTable = new JTable(tableModel) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (SessionManager.getInstance().getAccessLevel() == 1 && column == 0) {
                    return Boolean.class;
                }
                return super.getColumnClass(column);
            }
        };
        certificationTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrollPane = new JScrollPane(certificationTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        certificationTable.getTableHeader().setReorderingAllowed(false);
        certificationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        certificationTable.setRowSorter(sorter);

        // Double-click/Enter: Edit if admin+any checked, else report
        certificationTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = certificationTable.rowAtPoint(e.getPoint());
                    if (row < 0) return;
                    int modelRow = certificationTable.convertRowIndexToModel(row);

                    boolean isAdmin = SessionManager.getInstance().getAccessLevel() == 1;
                    boolean anyChecked = false;
                    if (isAdmin) {
                        for (int i = 0; i < certificationTable.getRowCount(); i++) {
                            int modelIdx = certificationTable.convertRowIndexToModel(i);
                            Object val = certificationTable.getModel().getValueAt(modelIdx, 0);
                            if (Boolean.TRUE.equals(val)) {
                                anyChecked = true;
                                break;
                            }
                        }
                    }

                    if (isAdmin && anyChecked) {
                        // Find first editable column after checkbox for admin
                        int editCol = 1;
                        for (int c = 1; c < certificationTable.getColumnCount(); c++) {
                            if (certificationTable.isCellEditable(row, c)) {
                                editCol = c; break;
                            }
                        }
                        certificationTable.editCellAt(row, editCol);
                        certificationTable.requestFocusInWindow();
                    } else {
                        handleReportGeneration(certificationTable);
                    }
                }
            }
        });
        InputMap inputMap = certificationTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = certificationTable.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none");
        actionMap.put("none", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleReportGeneration(certificationTable);
            }
        });

        certificationTable.setAutoCreateColumnsFromModel(true);
        certificationTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.LEFT);
                return label;
            }
        });

        panelTable.putClientProperty(FlatClientProperties.STYLE, "arc:20;background:$Table.background;");
        certificationTable.getTableHeader().putClientProperty(FlatClientProperties.STYLE,
            "height:30;hoverBackground:null;pressedBackground:null;separatorColor:$TableHeader.background;");
        certificationTable.putClientProperty(FlatClientProperties.STYLE,
            "rowHeight:30;showHorizontalLines:true;showVerticalLines:true;intercellSpacing:0,1;");
        scrollPane.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
            "trackArc:$ScrollBar.thumbArc;trackInsets:3,3,3,3;thumbInsets:3,3,3,3;background:$Table.background;");
        scrollPane.getHorizontalScrollBar().putClientProperty(FlatClientProperties.STYLE,
            "trackArc:$ScrollBar.thumbArc;trackInsets:3,3,3,3;thumbInsets:3,3,3,3;background:$Table.background;");

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
        reportLoader.stopPolling();
        reportLoader.cleanup();
    }

    private void configureColumns(JTable table) {
        if (table == null || table.getColumnCount() == 0) return;

        TableColumnModel columnModel = table.getColumnModel();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int startIdx = 0;

        if (SessionManager.getInstance().getAccessLevel() == 1) {
            TableColumn selectColumn = table.getColumnModel().getColumn(0);

            if (selectAllCheckBox == null) {
                selectAllCheckBox = new JCheckBox();
                selectAllCheckBox.setOpaque(false);
                selectAllCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
                selectAllCheckBox.setMargin(new Insets(0, 0, 0, 0));
                selectAllCheckBox.addActionListener(e -> {
                    boolean checked = selectAllCheckBox.isSelected();
                    for (int i = 0; i < model.getRowCount(); i++) {
                        model.setValueAt(checked, i, 0);
                    }
                });
                model.addTableModelListener(e -> {
                    if (e.getColumn() == 0) {
                        boolean all = true;
                        for (int i = 0; i < model.getRowCount(); i++) {
                            Boolean val = (Boolean) model.getValueAt(i, 0);
                            if (!Boolean.TRUE.equals(val)) {
                                all = false;
                                break;
                            }
                        }
                        selectAllCheckBox.setSelected(all && model.getRowCount() > 0);
                    }
                });
            }
            selectColumn.setHeaderRenderer((table1, value, isSelected, hasFocus, row, column) -> {
                JPanel panel = new JPanel(new GridBagLayout());
                panel.setOpaque(false);
                panel.setPreferredSize(new Dimension(35, 35));
                selectAllCheckBox.setPreferredSize(new Dimension(20, 20));
                panel.add(selectAllCheckBox);
                return panel;
            });
            selectColumn.setMinWidth(30);
            selectColumn.setMaxWidth(40);
            selectColumn.setPreferredWidth(35);
            JTableHeader header = table.getTableHeader();
            for (MouseListener ml : header.getMouseListeners()) {
                if (ml instanceof HeaderCheckBoxMouseListener) {
                    header.removeMouseListener(ml);
                }
            }
            header.addMouseListener(new HeaderCheckBoxMouseListener(selectAllCheckBox, table, 0));
            startIdx = 1;
        } else {
            selectAllCheckBox = null;
        }

        for (int i = startIdx; i < model.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);
            String colName = model.getColumnName(i).toLowerCase();
            switch (colName) {
                case "id":
                    column.setHeaderValue("ID");
                    setColumnWidth(column, 0, 0, 0); break;
                case "patient":
                    column.setHeaderValue("Patient");
                    setColumnWidth(column, 300, 300, 300); break;
                case "relationship":
                    setColumnWidth(column, 80, 80, 80); break;
                case "hospital":
                    column.setHeaderValue("Hospital");
                    setColumnWidth(column, 300, 300, 300); break;
                case "hospital_address":
                    column.setHeaderValue("Hospital Address");
                    column.setCellRenderer(new UppercaseRenderer());
                    setColumnWidth(column, 200, 200, 200); break;
                case "barangay":
                    column.setHeaderValue("Barangay");
                    setColumnWidth(column, 120, 120, 120); break;
                case "marital_status":
                    column.setHeaderValue("Marital Status");
                    setColumnWidth(column, 90, 90, 90); break;
                case "contact_no":
                    column.setHeaderValue("Contact No.");
                    setColumnWidth(column, 100, 100, 00); break;
//                case "parentguardian":
//                    column.setHeaderValue("Parent");
//                    setColumnWidth(column, 300, 300, 300); break;
//                case "parentguardian2":
//                    column.setHeaderValue("Parent");
//                    setColumnWidth(column, 300, 300, 300); break;
                case "parent_sex_if_single":
                    setColumnWidth(column, 0, 0, 0); column.setResizable(false); break;
                case "certification_date":
                    column.setHeaderValue("Certification Date");
                    column.setCellRenderer(new DateRenderer());
                    setColumnWidth(column, 120, 120, 120); break;
                case "certification_time":
                    column.setHeaderValue("Certification Time");
                    column.setCellRenderer(new TimeRenderer());
                    setColumnWidth(column, 100, 120, 120); break;
                case "type":
                    column.setHeaderValue("Type");
                    setColumnWidth(column, 100, 100, 100); break;
                case "amount_paid":
                    column.setHeaderValue("Amount Paid");
                    column.setCellRenderer(new CurrencyRenderer());
                    setColumnWidth(column, 80, 80, 80); break;
                case "receipt_no":
                    column.setHeaderValue("Receipt No.");
                    column.setCellRenderer(new RedTextRenderer());
                    setColumnWidth(column, 80, 80, 80); break;
                case "receipt_date_issued":
                    column.setHeaderValue("Date Issued");
                    column.setCellRenderer(new DateRenderer());
                    setColumnWidth(column, 100, 100, 100); break;
                case "place_issued":
                    column.setHeaderValue("Place Issued");
                    setColumnWidth(column, 100, 100, 100); break;
                case "signatory":
                    column.setHeaderValue("Signatory");
                    setColumnWidth(column, 250, 250, 250); break;
                case "legal_age":
                    column.setHeaderValue("Legal Age");
                    setColumnWidth(column, 70, 70, 70); break;
                default:
                    setColumnWidth(column, 250, 250, 250);
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
                    int patientColumn = certificationTable.convertColumnIndexToModel(certificationTable.getColumn("Patient").getModelIndex());
                    Object rawPatientName = certificationTable.getValueAt(row, patientColumn);

                    if (rawPatientName != null) {
                        return rawPatientName.toString().trim();
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving patient name by record ID", e);
        }
        return null;
    }

    private void updateReportTab(JPanel reportPanel, String patientName) {
        final String DEFAULT_TAB_TITLE = "Report";
        String tabTitle = (patientName != null && !patientName.isEmpty()) ? patientName : DEFAULT_TAB_TITLE;

        int reportIndex = -1;

        for (int i = 0; i < tabb.getTabCount(); i++) {
            if (tabTitle.equals(tabb.getTitleAt(i))) {
                reportIndex = i;
                break;
            }
        }

        Component tabContent = createBorder(reportPanel);

        if (reportIndex == -1) {
            reportIndex = tabb.getTabCount();
            tabb.addTab(tabTitle, null, tabContent);
            tabb.setTabComponentAt(reportIndex, createTabHeader(tabContent, tabTitle));
        } else {
            tabb.setComponentAt(reportIndex, tabContent);
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

        for (ActionListener al : closeButton.getActionListeners()) {
            closeButton.removeActionListener(al);
        }
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

    public void setMultiColumnFilter(String patient, String barangay, String hospital, String type) {
        if (certificationTable != null && certificationTable.getRowSorter() instanceof TableRowSorter) {
            TableRowSorter<?> sorter = (TableRowSorter<?>) certificationTable.getRowSorter();

            RowFilter<Object, Object> rf = new RowFilter<Object, Object>() {
                @Override
                public boolean include(RowFilter.Entry<?, ?> entry) {
                    JTable table = certificationTable;
                    boolean matches = true;

                    int patientCol = getColIndex(table, "PATIENT");
                    int barangayCol = getColIndex(table, "Barangay");
                    int hospitalCol = getColIndex(table, "HOSPITAL");
                    int typeCol = getColIndex(table, "TYPE");

                    if (patientCol >= 0 && patient != null && !patient.isEmpty()) {
                        Object val = entry.getValue(patientCol);
                        matches &= val != null && val.toString().toLowerCase().contains(patient.toLowerCase());
                    }
                    if (barangayCol >= 0 && barangay != null && !barangay.isEmpty()) {
                        Object val = entry.getValue(barangayCol);
                        matches &= val != null && val.toString().toLowerCase().contains(barangay.toLowerCase());
                    }
                    if (hospitalCol >= 0 && hospital != null && !hospital.isEmpty()) {
                        Object val = entry.getValue(hospitalCol);
                        matches &= val != null && val.toString().toLowerCase().contains(hospital.toLowerCase());
                    }
                    if (typeCol >= 0 && type != null && !type.isEmpty()) {
                        Object val = entry.getValue(typeCol);
                        matches &= val != null && val.toString().toLowerCase().contains(type.toLowerCase());
                    }
                    return matches;
                }
                private int getColIndex(JTable table, String name) {
                    if (table == null) return -1;
                    for (int i = 0; i < table.getColumnCount(); i++) {
                        if (name.equalsIgnoreCase(table.getColumnName(i))) return i;
                    }
                    return -1;
                }
            };

            if ((patient == null || patient.isEmpty()) &&
                (barangay == null || barangay.isEmpty()) &&
                (hospital == null || hospital.isEmpty()) &&
                (type == null || type.isEmpty())) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(rf);
            }
        }
    }

    private static class HeaderCheckBoxMouseListener extends MouseAdapter {
        private final JCheckBox headerCheckBox;
        private final JTable table;
        private final int columnIndex;

        public HeaderCheckBoxMouseListener(JCheckBox headerCheckBox, JTable table, int columnIndex) {
            this.headerCheckBox = headerCheckBox;
            this.table = table;
            this.columnIndex = columnIndex;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            JTableHeader header = table.getTableHeader();
            int col = header.columnAtPoint(e.getPoint());
            if (col == columnIndex) {
                boolean newState = !headerCheckBox.isSelected();
                headerCheckBox.setSelected(newState);
                for (ActionListener al : headerCheckBox.getActionListeners()) {
                    al.actionPerformed(new ActionEvent(headerCheckBox, ActionEvent.ACTION_PERFORMED, ""));
                }
                header.repaint();
            }
        }
    }
}