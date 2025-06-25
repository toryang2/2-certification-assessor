package assessor.forms;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.UIScale;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import assessor.component.ToolBarSelection;
import assessor.component.chart.*;
import assessor.component.chart.renderer.other.ChartCandlestickRenderer;
import assessor.component.chart.themes.ColorThemes;
import assessor.component.chart.themes.DefaultChartTheme;
import assessor.component.chart.utils.ToolBarCategoryOrientation;
import assessor.component.chart.utils.ToolBarTimeSeriesChartRenderer;
import assessor.component.dashboard.CardBox;
import assessor.component.report.util.DashboardHelper;
import assessor.component.report.util.DataChangeNotifier;
import assessor.component.report.util.DatabaseSaveHelper;
import assessor.component.report.util.UppercaseDocumentFilter;
import assessor.sample.SampleData;
import assessor.system.Form;
import assessor.system.FormManager;
import assessor.utils.SystemForm;

import javax.swing.*;
import java.awt.*;
import java.util.logging.*;
import javax.swing.text.AbstractDocument;

@SystemForm(name = "Dashboard", description = "dashboard form display some details")
public class FormDashboard extends Form {
    
    private final DataChangeNotifier.DataChangeListener dataChangeListener = new DataChangeNotifier.DataChangeListener() {
        @Override
        public void onDataChanged() {
            // Always update GUI on Swing EDT
            SwingUtilities.invokeLater(() -> loadData());
        }
    };
    
    public FormDashboard() {
        init();
        DataChangeNotifier.getInstance().addListener(dataChangeListener);
        applyUppercaseFilterToTextFields();
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        DataChangeNotifier.getInstance().removeListener(dataChangeListener);
    }

    private void init() {
        setLayout(new MigLayout("wrap,fill", "[fill]", "[grow 0][fill]"));
        createTitle();
        createPanelLayout();
        createCard();
        createFilterPanel();
        createChart();
        formRefresh();
//        createOtherChart();
    }

    @Override
    public void formInit() {
        loadData();
    }

    @Override
    public void formRefresh() {
        loadData();
        if (certificateTable != null) {
            certificateTable.formRefresh();
        }
    }

    private void loadData() {
    try {
        int totalRows = DashboardHelper.getTotalRows();
        int todayClientsServed = DashboardHelper.getTodayClientsServed();
        int yesterdayClientsServed = DashboardHelper.getYesterdayClientsServed();
        
        // Calculate percentage change
        String percentage;
        boolean isPositive;
        
        if (todayClientsServed > 0) {
            if (yesterdayClientsServed == 0) {
                // Handle division by zero - consider 100% increase
                percentage = "+100%";
                isPositive = true;
            } else {
                double change = ((double) todayClientsServed / yesterdayClientsServed) * 100;
                percentage = String.format("%+.0f%%", change);
                isPositive = change >= 0;
            }
        } else {
            // No registrations today, show percentage from yesterday
            if (yesterdayClientsServed == 0) {
                percentage = "0%";  // No change if both days are zero
                isPositive = false;
            } else {
                percentage = "-100%";
                isPositive = false;
            }
        }

        // Load data card with pluralization
        cardBox.setValueAt(0, 
            String.format("%,d", totalRows),
            todayClientsServed > 0 ? 
                String.format("+%d %s served", 
                    todayClientsServed, 
                    (todayClientsServed == 1 ? "client" : "clients")) : 
                "No clients served",
            percentage, 
            isPositive
        );
        
    } catch (Exception e) {
        // Fallback to sample data if there's an error
        cardBox.setValueAt(0, "1,205", "+305 clients served", "+25%", true);
        Logger.getLogger(FormDashboard.class.getName())
            .log(Level.SEVERE, "Error loading dashboard data", e);
    }

        cardBox.setValueAt(1, "$52,420.55", "less then previous month", "-5%", false);
        cardBox.setValueAt(2, "$3,180.00", "more then previous month", "+12%", true);
        cardBox.setValueAt(3, "$49,240.55", "more then previous month", "+7%", true);

        // load data chart
//        timeSeriesChart.setDataset(DashboardHelper.getDailyClientsDataset());
//        candlestickChart.setDataset(SampleData.getOhlcDataset());
//        barChart.setDataset(SampleData.getCategoryDataset());
//        spiderChart.setDataset(SampleData.getCategoryDataset());
//        pieChart.setDataset(SampleData.getPieDataset());
    }

    private void createTitle() {
        JPanel panel = new JPanel(new MigLayout("fillx", "[]push[][]"));
        JLabel title = new JLabel("Dashboard");

        title.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold +3");

        ToolBarSelection<ColorThemes> toolBarSelection = new ToolBarSelection<>(ColorThemes.values(), colorThemes -> {
            if (DefaultChartTheme.setChartColors(colorThemes)) {
                DefaultChartTheme.applyTheme(timeSeriesChart.getFreeChart());
                DefaultChartTheme.applyTheme(candlestickChart.getFreeChart());
                DefaultChartTheme.applyTheme(barChart.getFreeChart());
                DefaultChartTheme.applyTheme(pieChart.getFreeChart());
                DefaultChartTheme.applyTheme(spiderChart.getFreeChart());
                cardBox.setCardIconColor(0, DefaultChartTheme.getColor(0));
                cardBox.setCardIconColor(1, DefaultChartTheme.getColor(1));
                cardBox.setCardIconColor(2, DefaultChartTheme.getColor(2));
                cardBox.setCardIconColor(3, DefaultChartTheme.getColor(3));
            }
        });
        panel.add(title);
        panel.add(toolBarSelection);
        add(panel);
    }

    private void createPanelLayout() {
        panelLayout = new JPanel(new DashboardLayout());
        JScrollPane scrollPane = new JScrollPane(panelLayout);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "" +
                "width:5;" +
                "trackArc:$ScrollBar.thumbArc;" +
                "trackInsets:0,0,0,0;" +
                "thumbInsets:0,0,0,0;");
        add(scrollPane);
    }

    private void createCard() {
        JPanel panel = new JPanel(new MigLayout("fillx", "[fill]"));
        cardBox = new CardBox();
        cardBox.addCardItem(createIcon("assessor/icons/dashboard/customer.svg", DefaultChartTheme.getColor(0)), "Total Customer");
        cardBox.addCardItem(createIcon("assessor/icons/dashboard/income.svg", DefaultChartTheme.getColor(1)), "Total Income");
        cardBox.addCardItem(createIcon("assessor/icons/dashboard/expense.svg", DefaultChartTheme.getColor(2)), "Total Expense");
        cardBox.addCardItem(createIcon("assessor/icons/dashboard/profit.svg", DefaultChartTheme.getColor(3)), "Last Profit");
        panel.add(cardBox);
        panelLayout.add(panel);
    }

        private void applyUppercaseFilterToTextFields() {
        // Create an instance of the UppercaseDocumentFilter
        UppercaseDocumentFilter uppercaseFilter = new UppercaseDocumentFilter();

        // Apply the filter to all relevant text fields
        JTextField[] textFields = {
            filterPatientField,
            filterHospitalField
        };

        for (JTextField textField : textFields) {
            ((AbstractDocument) textField.getDocument()).setDocumentFilter(uppercaseFilter);
        }
    }
    
        // New: filter panel goes below cardbox, but above the chart
    private void createFilterPanel() {
        JPanel filterPanel = new JPanel(new MigLayout("insets 5 8 5 0", "[][]10[][]10[][]10[][]", "[]"));
        filterPanel.setOpaque(false);

        JLabel labelPatient = new JLabel("Patient:");
        filterPatientField = new JTextField(12);
        JLabel labelBarangay = new JLabel("Barangay:");
        filterBarangayField = new JComboBox<>();
        filterBarangayField.setEditable(false);
        SwingUtilities.invokeLater(() -> {
            filterBarangayField.addItem(""); // for "Any"/blank filter
            for (String barangay : DatabaseSaveHelper.fetchBarangays()) {
                filterBarangayField.addItem(barangay);
            }
        });
        JLabel labelHospital = new JLabel("Hospital:");
        filterHospitalField = new JTextField(12);
        JLabel labelType = new JLabel("Type:");
        filterTypeField = new JComboBox<>();
        filterTypeField.setEditable(false);
        SwingUtilities.invokeLater(() -> {
            filterTypeField.addItem(""); // for "Any"/blank filter
            for (String typeset : DatabaseSaveHelper.fetchTypeSet()) {
                filterTypeField.addItem(typeset);
            }
        });

        filterPanel.add(labelPatient);
        filterPanel.add(filterPatientField);
        filterPanel.add(labelBarangay);
        filterPanel.add(filterBarangayField);
        filterPanel.add(labelHospital);
        filterPanel.add(filterHospitalField);
        filterPanel.add(labelType);
        filterPanel.add(filterTypeField);

        panelLayout.add(filterPanel);

        // --- Filter logic: update on any change in the fields ---
        javax.swing.event.DocumentListener filterListener = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                if (certificateTable != null) {
                    certificateTable.setMultiColumnFilter(
                        filterPatientField.getText(),
                        getSelectedBarangay(),
                        filterHospitalField.getText(),
                        getSelectedTypeSet()
                    );
                }
            }
        };
        filterPatientField.getDocument().addDocumentListener(filterListener);
        filterHospitalField.getDocument().addDocumentListener(filterListener);
        
        // ComboBox filter listener
        filterBarangayField.addActionListener(e -> {
            if (certificateTable != null) {
                certificateTable.setMultiColumnFilter(
                    filterPatientField.getText(),
                    getSelectedBarangay(),
                    filterHospitalField.getText(),
                    getSelectedTypeSet()
                );
            }
        });
        
                // ComboBox filter listener
        filterTypeField.addActionListener(e -> {
            if (certificateTable != null) {
                certificateTable.setMultiColumnFilter(
                    filterPatientField.getText(),
                    getSelectedBarangay(),
                    filterHospitalField.getText(),
                    getSelectedTypeSet()
                );
            }
        });
    }
    
    private String getSelectedBarangay() {
        Object selected = filterBarangayField.getSelectedItem();
        return selected == null ? "" : selected.toString();
    }
    
    private String getSelectedTypeSet() {
        Object selected = filterTypeField.getSelectedItem();
        return selected == null ? "" : selected.toString();
    }
    
    public static void resetFilters() {
        FormDashboard dashboard = (FormDashboard) FormManager.getActiveForm(FormDashboard.class);
        if (dashboard != null) {
            // Reset Patient and Hospital fields
            if (dashboard.filterPatientField != null)
                dashboard.filterPatientField.setText("");
            if (dashboard.filterHospitalField != null)
                dashboard.filterHospitalField.setText("");

            // Reset Barangay ComboBox
            if (dashboard.filterBarangayField != null && dashboard.filterBarangayField instanceof JComboBox) {
                JComboBox<?> combo = (JComboBox<?>) dashboard.filterBarangayField;
                if (combo.getItemCount() > 0) combo.setSelectedIndex(0);
            }

            // Reset Type ComboBox
            if (dashboard.filterTypeField != null && dashboard.filterTypeField instanceof JComboBox) {
                JComboBox<?> combo = (JComboBox<?>) dashboard.filterTypeField;
                if (combo.getItemCount() > 0) combo.setSelectedIndex(0);
            }
        }
    }

private void createChart() {
    JPanel panel = new JPanel(new MigLayout("fill,wrap", "[fill]", "[grow, push]"));

        certificateTable = new CertificateTable();
        FormManager.showForm(certificateTable);
    
    panel.add(certificateTable, "grow, push");

    panelLayout.add(panel, "grow, push");
}

    private Icon createIcon(String icon, Color color) {
        return new FlatSVGIcon(icon, 0.4f).setColorFilter(new FlatSVGIcon.ColorFilter(color1 -> color));
    }

    private JPanel panelLayout;
    private CardBox cardBox;
    private JTextField filterPatientField, filterHospitalField;
    private CertificateTable certificateTable;
    private JComboBox<String> filterBarangayField, filterTypeField;

    private TimeSeriesChart timeSeriesChart;
    private CandlestickChart candlestickChart;
    private BarChart barChart;
    private SpiderChart spiderChart;
    private PieChart pieChart;

private class DashboardLayout implements LayoutManager {

    private int gap = 0;

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int width = insets.left + insets.right;
            int height = insets.top + insets.bottom;
            int g = UIScale.scale(gap);
            int count = parent.getComponentCount();
            for (int i = 0; i < count; i++) {
                Component com = parent.getComponent(i);
                Dimension size = com.getPreferredSize();
                height += size.height;
            }
            if (count > 1) {
                height += (count - 1) * g;
            }
            return new Dimension(width, height);
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            return new Dimension(10, 10);
        }
    }

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int x = insets.left;
            int y = insets.top;
            int width = parent.getWidth() - (insets.left + insets.right);
            int height = parent.getHeight() - (insets.top + insets.bottom);
            int g = UIScale.scale(gap);
            int count = parent.getComponentCount();

            // Calculate total fixed height of components
            int fixedHeight = 0;
            for (int i = 0; i < count; i++) {
                Component com = parent.getComponent(i);
                Dimension size = com.getPreferredSize();
                fixedHeight += size.height;
            }
            fixedHeight += (count - 1) * g;

            // Allocate remaining height to the last component
            for (int i = 0; i < count; i++) {
                Component com = parent.getComponent(i);
                Dimension size = com.getPreferredSize();
                if (i == count - 1) { // Last component
                    com.setBounds(x, y, width, height - fixedHeight + size.height);
                } else {
                    com.setBounds(x, y, width, size.height);
                    y += size.height + g;
                }
            }
        }
    }
}
}
