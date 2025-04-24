package assessor.forms;

import assessor.component.report.GenerateReport;
import assessor.component.report.util.ConfigHelper;
import assessor.component.report.ReportLoader;
import assessor.component.report.util.*;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import assessor.model.ModelEmployee;
import assessor.model.ModelProfile;
import assessor.sample.SampleData;
import assessor.simple.SimpleInputForms;
import assessor.system.Form;
import assessor.utils.SystemForm;
import assessor.utils.table.CheckBoxTableHeaderRenderer;
import assessor.utils.table.TableHeaderAlignment;
import assessor.utils.table.TableProfileCellRenderer;
import raven.modal.option.Location;
import raven.modal.option.Option;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

@SystemForm(name = "Table", description = "table is a user interface component", tags = {"list"})
public class FormTable extends Form {
    private JTabbedPane tabb;

    public FormTable() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("fillx,wrap", "[fill]", "[][fill,grow]"));
        add(createInfo("Custom Table", "A table is a user interface component that displays a collection of records in a structured, tabular format. It allows users to view, sort, and manage data or other resources.", 1));
        add(createTab(), "gapx 7 7");
    }

    private JPanel createInfo(String title, String description, int level) {
        JPanel panel = new JPanel(new MigLayout("fillx,wrap", "[fill]"));
        JLabel lbTitle = new JLabel(title);
        JTextPane text = new JTextPane();
        text.setText(description);
        text.setEditable(false);
        text.setBorder(BorderFactory.createEmptyBorder());
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold +" + (4 - level));
        panel.add(lbTitle);
        panel.add(text, "width 500");
        return panel;
    }

    private Component createTab() {
        tabb = new JTabbedPane();
        tabb.putClientProperty(FlatClientProperties.STYLE, "" +
                "tabType:card");
        tabb.addTab("Certification", new FlatSVGIcon("assessor/icons/certificate.svg").derive(16, 16), createBorder(createCertificationTable()));
        tabb.addTab("Custom table", new FlatSVGIcon("assessor/icons/printer.svg").derive(16, 16), createBorder(createCustomTable()));
        return tabb;
    }

    private Component createBorder(Component component) {
        JPanel panel = new JPanel(new MigLayout("fill,insets 7 0 7 0", "[fill]", "[fill]"));
        panel.add(component);
        return panel;
    }

    private Component createCustomTable() {
        JPanel panel = new JPanel(new MigLayout("fillx,wrap,insets 10 0 10 0", "[fill]", "[][]0[fill,grow]"));

        // create table model
        Object columns[] = new Object[]{"SELECT", "#", "NAME", "DATE", "SALARY", "POSITION", "DESCRIPTION"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // allow cell editable at column 0 for checkbox
                return column == 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // use boolean type at column 0 for checkbox
                if (columnIndex == 0)
                    return Boolean.class;
                // use profile class
                if (columnIndex == 2) {
                    return ModelProfile.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };

        // create table
        JTable table = new JTable(model);

        // table scroll
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // table option
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(250);

        // disable reordering table column
        table.getTableHeader().setReorderingAllowed(false);

        // apply profile cell renderer
        table.setDefaultRenderer(ModelProfile.class, new TableProfileCellRenderer(table));

        // apply checkbox custom to table header
        table.getColumnModel().getColumn(0).setHeaderRenderer(new CheckBoxTableHeaderRenderer(table, 0));

        // alignment table header
        table.getTableHeader().setDefaultRenderer(new TableHeaderAlignment(table) {
            @Override
            protected int getAlignment(int column) {
                if (column == 1) {
                    return SwingConstants.CENTER;
                }
                return SwingConstants.LEADING;
            }
        });

        // style
        panel.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:20;" +
                "background:$Table.background;");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "" +
                "height:30;" +
                "hoverBackground:null;" +
                "pressedBackground:null;" +
                "separatorColor:$TableHeader.background;");
        table.putClientProperty(FlatClientProperties.STYLE, "" +
                "rowHeight:70;" +
                "showHorizontalLines:true;" +
                "intercellSpacing:0,1;" +
                "cellFocusColor:$TableHeader.hoverBackground;" +
                "selectionBackground:$TableHeader.hoverBackground;" +
                "selectionInactiveBackground:$TableHeader.hoverBackground;" +
                "selectionForeground:$Table.foreground;");
        scrollPane.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "" +
                "trackArc:$ScrollBar.thumbArc;" +
                "trackInsets:3,3,3,3;" +
                "thumbInsets:3,3,3,3;" +
                "background:$Table.background;");

        // create title
        JLabel title = new JLabel("Custom table");
        title.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold +2");
        panel.add(title, "gapx 20");

        // create header
        panel.add(createHeaderAction());
        panel.add(scrollPane);

        // sample data
        for (ModelEmployee d : SampleData.getSampleEmployeeData(false)) {
            model.addRow(d.toTableRowCustom(table.getRowCount() + 1));
        }
        return panel;
    }

    public Component createCertificationTable() {
        JPanel panelTable = new JPanel(new MigLayout("fill,wrap,insets 10 0 10 0", "[fill]", "[grow]"));

        // Create table model with temporary columns
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Loading..."}, 0 // Temporary column to show loading state
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // create table
        JTable table = new JTable(model);
//        certificationTable = new JTable(model);
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleReportGeneration(table);
                }
            }
        });

        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleReportGeneration(table);
                }
            }
        });

        // table scroll
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        table.setAutoCreateColumnsFromModel(true);
        
        table.getTableHeader().setDefaultRenderer(new TableHeaderAlignment(table) {
            @Override
            protected int getAlignment(int column) {
                return column == 0 ? SwingConstants.CENTER : SwingConstants.LEADING;
            }
        });

        // style
        panelTable.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc:20;" +
                "background:$Table.background;");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, "" +
                "height:30;" +
                "hoverBackground:null;" +
                "pressedBackground:null;" +
                "separatorColor:$TableHeader.background;");
        table.putClientProperty(FlatClientProperties.STYLE, "" +
                "rowHeight:30;" +
                "showHorizontalLines:true;" +
                "intercellSpacing:0,1;" +
                "cellFocusColor:$TableHeader.hoverBackground;" +
                "selectionBackground:$TableHeader.hoverBackground;" +
                "selectionInactiveBackground:$TableHeader.hoverBackground;" +
                "selectionForeground:$Table.foreground;");
        scrollPane.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "" +
                "trackArc:$ScrollBar.thumbArc;" +
                "trackInsets:3,3,3,3;" +
                "thumbInsets:3,3,3,3;" +
                "background:$Table.background;");
        scrollPane.getHorizontalScrollBar().putClientProperty(FlatClientProperties.STYLE, "" +
                "trackArc:$ScrollBar.thumbArc;" +
                "trackInsets:3,3,3,3;" +
                "thumbInsets:3,3,3,3;" +
                "background:$Table.background;");

        // create title
//        JLabel title = new JLabel("Certification");
//        title.putClientProperty(FlatClientProperties.STYLE, "" +
//                "font:bold +2");
//        panelTable.add(title, "align center");

    table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            return label;
        }
    });

    ReportLoader reportLoader = new ReportLoader(model, new ReportLoader.LoadCallbacks() {
        private JComponent loadingOverlay;
        
        @Override
        public void onLoadStart() {
            System.out.println("Load started..."); // Debug log
            SwingUtilities.invokeLater(() -> {
                model.setRowCount(0);
                model.setColumnIdentifiers(new Object[]{"Loading..."});
                if (loadingOverlay == null) {
                    loadingOverlay = createLoadingOverlay();
                    panelTable.add(loadingOverlay, "pos 0 0 100% 100%");
                }
                panelTable.revalidate();
                panelTable.repaint();
            });
        }

        @Override
        public void onLoadComplete() {
            SwingUtilities.invokeLater(() -> {
                try {
                    configureColumns(table); // Move column config to separate method
                    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    table.setAutoCreateRowSorter(true);
                    table.getTableHeader().setReorderingAllowed(false);
                    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                } finally {
                    if (loadingOverlay != null) {
                        panelTable.remove(loadingOverlay);
                        panelTable.revalidate();
                        panelTable.repaint();
                    }
                }
            });
        }
        
        private void configureColumns(JTable table) {
            TableColumnModel columnModel = table.getColumnModel();
            DefaultTableModel model = (DefaultTableModel) table.getModel();

            for (int i = 0; i < model.getColumnCount(); i++) {
                TableColumn column = columnModel.getColumn(i);
                String colName = model.getColumnName(i).toLowerCase();
                
                switch (colName) {
                    case "id":
                        column.setHeaderValue("ID");
                        column.setCellRenderer(new TableRightRenderer());
                        setColumnWidth(column, 50, 50, 50);
                        break;
                    case "patient":
                        column.setHeaderValue("PATIENT");
                        setColumnWidth(column, 200, 200, 200);
                        break;
                    case "relationship":
                        setColumnWidth(column, 80, 80, 80);
                        break;
                    case "hospitaladdress":
                        column.setHeaderValue("Hospital Address");
                        setColumnWidth(column, 250, 250, 250);
                        break;
                    case "maritalstatus":
                        column.setHeaderValue("Marital Status");
                        setColumnWidth(column, 90, 90, 90);
                        break;
                    case "parentguardian":
                        column.setHeaderValue("Parent");
                        setColumnWidth(column, 120, 200, 200);
                        break;
                    case "parentguardian2":
                        column.setHeaderValue("Parent");
                        setColumnWidth(column, 200, 200, 200);
                        break;
                    case "parentsexifsingle":
                        setColumnWidth(column, 0, 0, 0);
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
                    case "parent2":
                        column.setHeaderValue("Parent");
                        setColumnWidth(column, 200, 200, 200);
                        break;
                    case "type":
                        setColumnWidth(column, 100, 100, 100);
                        break;
                    case "amountpaid":
                        column.setHeaderValue("Amount Paid");
                        column.setCellRenderer(new CurrencyRenderer());
                        setColumnWidth(column, 80, 80, 80);
                        break;
                    case "receiptno":
                        column.setHeaderValue("Receipt No.");
                        column.setCellRenderer(new RedTextRenderer());
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
                        setColumnWidth(column, 200, 200, 200);
                }
            }
        }

        private void setColumnWidth(TableColumn column, int pref, int max, int min) {
            column.setPreferredWidth(pref);
            column.setMaxWidth(max);
            column.setMinWidth(min);
        }

        @Override
        public void onLoadError(String message) {
            System.err.println("Load error: " + message); // Debug log
            SwingUtilities.invokeLater(() -> {
                model.setColumnIdentifiers(new Object[]{"Error Loading Data"});
                model.setRowCount(0);
                JOptionPane.showMessageDialog(panelTable,
                    "Database Error: " + message,
                    "Loading Failed",
                    JOptionPane.ERROR_MESSAGE);
            });
        }
    });

    // Verify database configuration
    System.out.println("Database Config:");
    System.out.println("URL: " + ConfigHelper.getDbUrl());
    System.out.println("User: " + ConfigHelper.getDbUser());

    // Load data with error protection
    try {
        reportLoader.loadData();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(panelTable,
            "Initialization Error: " + e.getMessage(),
            "Critical Error",
            JOptionPane.ERROR_MESSAGE);
    }
        panelTable.add(scrollPane, "grow, push");
        reportLoader.loadData();

        // sample data
//        for (ModelEmployee d : SampleData.getSampleBasicEmployeeData()) {
//            model.addRow(d.toTableRowBasic(table.getRowCount() + 1));
//        }
        return panelTable;
    }
    
    private JComponent createLoadingOverlay() {
        JPanel panel = new JPanel(new GridBagLayout()); // Use GridBagLayout for centering
        panel.setOpaque(false);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.putClientProperty(FlatClientProperties.STYLE, "" +
            "arc:999;" +
            "background:$Loading.background");

        JLabel label = new JLabel("Loading data...");
        label.setHorizontalAlignment(SwingConstants.CENTER);

        // Create centered container with MigLayout
        JPanel centerPanel = new JPanel(new MigLayout("wrap,insets 20,align center center"));
        centerPanel.add(progressBar, "w 200!, align center, gapbottom 10");
        centerPanel.add(label, "align center");
        centerPanel.putClientProperty(FlatClientProperties.STYLE, "" +
            "background:$Loading.background;" +
            "arc:10");

        // Add to main panel with GridBag constraints
        panel.add(centerPanel);
        return panel;
    }

    private Component createHeaderAction() {
        JPanel panel = new JPanel(new MigLayout("insets 5 20 5 20", "[fill,230]push[][]"));

        JTextField txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search...");
        txtSearch.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSVGIcon("assessor/icons/search.svg", 0.4f));
        JButton cmdCreate = new JButton("Create");
        JButton cmdEdit = new JButton("Edit");
        JButton cmdDelete = new JButton("Delete");

        cmdCreate.addActionListener(e -> showModal());
        panel.add(txtSearch);
        panel.add(cmdCreate);
        panel.add(cmdEdit);
        panel.add(cmdDelete);

        panel.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:null;");
        return panel;
    }

    private void showModal() {
        Option option = ModalDialog.createOption();
        option.getLayoutOption().setSize(-1, 1f)
                .setLocation(Location.TRAILING, Location.TOP)
                .setAnimateDistance(0.7f, 0);
        ModalDialog.showModal(this, new SimpleModalBorder(
                new SimpleInputForms(), "Create", SimpleModalBorder.YES_NO_OPTION,
                (controller, action) -> {

                }), option);
    }
    
    private void handleReportGeneration(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) return;

        try {
            // Get column indices by name
            int idColumn = table.getColumn("ID").getModelIndex();
            int typeColumn = table.getColumn("Type").getModelIndex();

            Object rawId = table.getValueAt(row, idColumn);
            Object rawType = table.getValueAt(row, typeColumn);

            // Validate and convert
            List<Integer> selectedIDs = new ArrayList<>();
            try {
                selectedIDs.add(Integer.parseInt(rawId.toString()));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, 
                    "Invalid ID format", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            String reportType = rawType != null ? rawType.toString().trim() : "unknown";

            // Prepare parameters
            Map<String, Object> params = new HashMap<>();
            params.put("SelectedIDs", selectedIDs);
            params.put("ReportType", reportType);

            // Generate and display report
            JPanel reportViewer = GenerateReport.generateReportPanel(
                params,
                reportType + " Report",
                "/assessor/ui/icons/printer.png"
            );

            updateReportTab(reportViewer);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Error generating report: " + e.getMessage(), 
                "Generation Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateReportTab(JPanel reportPanel) {
        int reportIndex = -1;
        final String TAB_TITLE = "Report";

        // Find existing report tab
        for (int i = 0; i < tabb.getTabCount(); i++) {
            if (TAB_TITLE.equals(tabb.getTitleAt(i))) {
                reportIndex = i;
                break;
            }
        }

        Component tabContent = createBorder(reportPanel);

        if (reportIndex == -1) {
            // Add new tab with close button
            reportIndex = tabb.getTabCount();
            tabb.addTab(TAB_TITLE, null, tabContent);

            // Create custom tab component with close button
            JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            tabHeader.setOpaque(false);

            // Title with icon
            JLabel title = new JLabel(TAB_TITLE);
            title.setIcon(new FlatSVGIcon("assessor/icons/printer.svg").derive(16, 16));
            title.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

            // Close button with proper styling
            JButton closeButton = new JButton(new FlatSVGIcon("assessor/icons/close.svg", 12, 12));

            closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            closeButton.setFocusable(true);
            closeButton.setOpaque(false);
            closeButton.setContentAreaFilled(true);
            closeButton.setBackground(UIManager.getColor("null;"));
            UIManager.getDefaults().keySet().forEach(System.out::println);

            closeButton.addActionListener(e -> {
                int index = tabb.indexOfComponent(tabContent);
                if (index != -1) {
                    tabb.remove(index);
                }
            });

            // Remove mouse listeners - let FlatLaf handle styling
            tabHeader.add(title);
            tabHeader.add(closeButton);
            tabb.setTabComponentAt(reportIndex, tabHeader);
        } else {
            // Update existing tab
            tabb.setComponentAt(reportIndex, tabContent);
        }

        tabb.setSelectedIndex(reportIndex);
    }
}
