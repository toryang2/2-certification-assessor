package assessor.forms;

import assessor.component.report.GenerateReport;
import assessor.component.report.util.ConfigHelper;
import assessor.component.report.util.ReportLoader;
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
    public JTabbedPane tabb;
    public ReportLoader reportLoader;
    private boolean autoGenerateReport = false;
    public JTable certificationTable;
    private List<Runnable> dataLoadListeners = new ArrayList<>();
    
    public void addDataLoadListener(Runnable listener) {
        dataLoadListeners.add(listener);
    }

    private void configureColumns(JTable table) {
        if (table == null) return;

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

    public void hardRefresh() {
        if (reportLoader != null && certificationTable != null) {
            DefaultTableModel model = (DefaultTableModel) certificationTable.getModel();
            model.setRowCount(0);
            model.setColumnIdentifiers(new Object[]{"Loading..."});
            reportLoader.loadData();
        }
    }

    public void reloadData() {
        if (reportLoader != null && !reportLoader.hasActiveRefresh()) {
            SwingUtilities.invokeLater(() -> {
                try {
                    Component tabComponent = tabb.getComponentAt(0);
                    if (tabComponent instanceof JPanel) {
                        JPanel panel = (JPanel) tabComponent;
                        for (Component comp : panel.getComponents()) {
                            if (comp instanceof JScrollPane) {
                                JScrollPane scrollPane = (JScrollPane) comp;
                                JTable table = (JTable) scrollPane.getViewport().getView();
                                ((DefaultTableModel) table.getModel()).fireTableDataChanged();
                                table.repaint();
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public FormTable() {
        init();
        initializeReportLoader();
    }

    private void init() {
        setLayout(new MigLayout("fillx,wrap", "[fill]", "[][fill,grow]"));
        add(createInfo("Custom Table", "A table is a user interface component that displays a collection of records in a structured, tabular format. It allows users to view, sort, and manage data or other resources.", 1));
        add(createTab(), "gapx 7 7");
    }

    private void initializeReportLoader() {
        try {
            Component tabComponent = tabb.getComponentAt(0);
            if (tabComponent instanceof Container) {
                Container container = (Container) tabComponent;

                if (certificationTable == null) {
                    certificationTable = (JTable) ((JScrollPane) container.getComponent(0)).getViewport().getView();
                }

                if (certificationTable != null) {
                    DefaultTableModel model = (DefaultTableModel) certificationTable.getModel();

                    if (reportLoader == null) {
                        reportLoader = new ReportLoader(model, new ReportLoader.LoadCallbacks() {
                            private JComponent loadingOverlay;

                            @Override
                            public void onLoadStart() {
                                SwingUtilities.invokeLater(() -> {
                                    model.setRowCount(0);
                                    model.setColumnIdentifiers(new Object[]{"Loading..."});
                                    if (loadingOverlay == null) {
                                        loadingOverlay = createLoadingOverlay();
                                        container.add(loadingOverlay, "pos 0 0 100% 100%");
                                    }
                                    container.revalidate();
                                    container.repaint();
                                });
                            }

                            @Override
                            public void onLoadComplete() {
                                SwingUtilities.invokeLater(() -> {
                                    try {
                                        configureColumns(certificationTable);
                                        certificationTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                                        certificationTable.setAutoCreateRowSorter(true);
                                        certificationTable.getTableHeader().setReorderingAllowed(false);
                                        certificationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                                    } finally {
                                        if (loadingOverlay != null) {
                                            container.remove(loadingOverlay);
                                            container.revalidate();
                                            container.repaint();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onLoadError(String message) {
                                SwingUtilities.invokeLater(() -> {
                                    model.setColumnIdentifiers(new Object[]{"Error Loading Data"});
                                    model.setRowCount(0);
                                    JOptionPane.showMessageDialog(FormTable.this,
                                            "Database Error: " + message,
                                            "Loading Failed",
                                            JOptionPane.ERROR_MESSAGE);
                                });
                            }
                        });

                        reportLoader.loadData();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        Object columns[] = new Object[]{"SELECT", "#", "NAME", "DATE", "SALARY", "POSITION", "DESCRIPTION"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0)
                    return Boolean.class;
                if (columnIndex == 2)
                    return ModelProfile.class;
                return super.getColumnClass(columnIndex);
            }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(250);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(ModelProfile.class, new TableProfileCellRenderer(table));
        table.getColumnModel().getColumn(0).setHeaderRenderer(new CheckBoxTableHeaderRenderer(table, 0));
        table.getTableHeader().setDefaultRenderer(new TableHeaderAlignment(table) {
            @Override
            protected int getAlignment(int column) {
                return column == 1 ? SwingConstants.CENTER : SwingConstants.LEADING;
            }
        });
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:20;"
                + "background:$Table.background;");
        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE, ""
                + "height:30;"
                + "hoverBackground:null;"
                + "pressedBackground:null;"
                + "separatorColor:$TableHeader.background;");
        table.putClientProperty(FlatClientProperties.STYLE, ""
                + "rowHeight:70;"
                + "showHorizontalLines:true;"
                + "intercellSpacing:0,1;"
                + "cellFocusColor:$TableHeader.hoverBackground;"
                + "selectionBackground:$TableHeader.hoverBackground;"
                + "selectionInactiveBackground:$TableHeader.hoverBackground;"
                + "selectionForeground:$Table.foreground;");
        scrollPane.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, ""
                + "trackArc:$ScrollBar.thumbArc;"
                + "trackInsets:3,3,3,3;"
                + "thumbInsets:3,3,3,3;"
                + "background:$Table.background;");
        JLabel title = new JLabel("Custom table");
        title.putClientProperty(FlatClientProperties.STYLE, ""
                + "font:bold +2");
        panel.add(title, "gapx 20");
        panel.add(createHeaderAction());
        panel.add(scrollPane);
        for (ModelEmployee d : SampleData.getSampleEmployeeData(false)) {
            model.addRow(d.toTableRowCustom(table.getRowCount() + 1));
        }
        return panel;
    }

    public Component createCertificationTable() {
        JPanel panelTable = new JPanel(new MigLayout("fill,wrap,insets 10 0 10 0", "[fill]", "[grow]"));
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Loading..."}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        certificationTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(certificationTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        certificationTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleReportGeneration(certificationTable);
                }
            }
        });
        certificationTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleReportGeneration(certificationTable);
                }
            }
        });
        certificationTable.setAutoCreateColumnsFromModel(true);
        certificationTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });
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
                + "intercellSpacing:0,1;"
                + "cellFocusColor:$TableHeader.hoverBackground;"
                + "selectionBackground:$TableHeader.hoverBackground;"
                + "selectionInactiveBackground:$TableHeader.hoverBackground;"
                + "selectionForeground:$Table.foreground;");
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
        panelTable.add(scrollPane, "grow, push");
        return panelTable;
    }

    private JComponent createLoadingOverlay() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:999;"
                + "background:$Loading.background");
        JLabel label = new JLabel("Loading data...");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel centerPanel = new JPanel(new MigLayout("wrap,insets 20,align center center"));
        centerPanel.add(progressBar, "w 200!, align center, gapbottom 10");
        centerPanel.add(label, "align center");
        centerPanel.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:$Loading.background;"
                + "arc:10");
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
        panel.putClientProperty(FlatClientProperties.STYLE, ""
                + "background:null;");
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

public void handleReportGeneration(JTable table) {
    int row = table.getSelectedRow();
    if (row == -1) return;

    try {
        int idColumn = table.convertColumnIndexToModel(table.getColumn("ID").getModelIndex());
        int typeColumn = table.convertColumnIndexToModel(table.getColumn("Type").getModelIndex());

        Object rawId = table.getValueAt(row, idColumn);
        Object rawType = table.getValueAt(row, typeColumn);

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

        Map<String, Object> params = new HashMap<>();
        params.put("SelectedIDs", selectedIDs);
        params.put("ReportType", reportType);

        JPanel reportViewer = GenerateReport.generateReportPanel(params, reportType + " Report", "/assessor/ui/icons/printer.png");
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

        for (int i = 0; i < tabb.getTabCount(); i++) {
            if (TAB_TITLE.equals(tabb.getTitleAt(i))) {
                reportIndex = i;
                break;
            }
        }

        Component tabContent = createBorder(reportPanel);

        if (reportIndex == -1) {
            reportIndex = tabb.getTabCount();
            tabb.addTab(TAB_TITLE, null, tabContent);

            JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            tabHeader.setOpaque(false);

            JLabel title = new JLabel(TAB_TITLE);
            title.setIcon(new FlatSVGIcon("assessor/icons/printer.svg").derive(16, 16));
            title.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

            JButton closeButton = new JButton(new FlatSVGIcon("assessor/icons/close.svg", 12, 12));
            closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            closeButton.setFocusable(true);
            closeButton.setOpaque(false);
            closeButton.setContentAreaFilled(true);
            closeButton.addActionListener(e -> {
                int index = tabb.indexOfComponent(tabContent);
                if (index != -1) {
                    tabb.remove(index);
                }
            });

            tabHeader.add(title);
            tabHeader.add(closeButton);
            tabb.setTabComponentAt(reportIndex, tabHeader);
        } else {
            tabb.setComponentAt(reportIndex, tabContent);
        }

        tabb.setSelectedIndex(reportIndex);
    }
    
    public JTable getCertificationTable() {
        return certificationTable;
    }
}
