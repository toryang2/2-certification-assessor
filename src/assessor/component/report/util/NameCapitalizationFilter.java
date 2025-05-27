package assessor.component.report.util;

import javax.swing.text.*;

public class NameCapitalizationFilter extends DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
        if (string != null) {
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.insert(offset, string);
            fb.insertString(offset, capitalizeName(sb.toString()), attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs)
            throws BadLocationException {
        if (string != null) {
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
            sb.replace(offset, offset + length, string);
            fb.replace(0, fb.getDocument().getLength(), capitalizeName(sb.toString()), attrs);
        }
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length)
            throws BadLocationException {
        StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
        sb.delete(offset, offset + length);
        fb.replace(0, fb.getDocument().getLength(), capitalizeName(sb.toString()), null);
    }

    private String capitalizeName(String input) {
        StringBuilder result = new StringBuilder(input.length());
        boolean capitalizeNext = true;
        for (char c : input.toCharArray()) {
            if (capitalizeNext && Character.isLetter(c)) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
        }
        return result.toString();
    }
}