package assessor.component.report.util;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class CurrencyRenderer extends DefaultTableCellRenderer {
    private final NumberFormat currencyFormat;

    public CurrencyRenderer() {
        setHorizontalAlignment(SwingConstants.RIGHT);
        Locale phLocale = new Locale("en", "PH");
        currencyFormat = NumberFormat.getCurrencyInstance(phLocale);
        
        // Customize currency symbol and formatting
        DecimalFormatSymbols symbols = ((java.text.DecimalFormat) currencyFormat).getDecimalFormatSymbols();
        symbols.setCurrencySymbol("");
        symbols.setGroupingSeparator(','); // Explicitly set grouping separator
        symbols.setDecimalSeparator('.');  // Explicitly set decimal separator
        ((java.text.DecimalFormat) currencyFormat).setDecimalFormatSymbols(symbols);
        
        // Ensure grouping is enabled (commas for thousands)
        currencyFormat.setGroupingUsed(true);
    }

    @Override
    protected void setValue(Object value) {
        if (value instanceof Number) {
            // Format the number with currency symbol, commas, and two decimal places
            setText(currencyFormat.format(value));
        } else {
            super.setValue(value);
        }
    }
}
