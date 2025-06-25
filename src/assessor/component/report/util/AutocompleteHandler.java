package assessor.component.report.util;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * AutocompleteHandler - attaches to a JTextField and shows a suggestion popup,
 * accepts suggestion on Enter or Tab, navigates suggestions with arrows.
 * Tab will accept the highlighted suggestion (does NOT move focus).
 * Adapts popup and list colors to FlatLaf MacOS Light/Dark themes if available.
 * Uses custom painting for suggestion items to match design guidelines.
 * Removes scrollbars from the suggestions popup.
 */
public class AutocompleteHandler {
    private final JTextField textField;
    private final String fieldKey;
    private final JPopupMenu suggestionPopup = new JPopupMenu();
    private JList<String> suggestionList;
    private DefaultListModel<String> listModel;
    private boolean ignoreDocumentChange = false;
    // Store default focus traversal keys for Tab/Shift+Tab
    private final Set<AWTKeyStroke> defaultForwardKeys;
    private final Set<AWTKeyStroke> defaultBackwardKeys;

    public AutocompleteHandler(JTextField textField, String fieldKey) {
        this.textField = textField;
        this.fieldKey = fieldKey;
        this.defaultForwardKeys = textField.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        this.defaultBackwardKeys = textField.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);

        // Disable focus traversal for Tab and Shift+Tab so we can handle them for autocomplete
        textField.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());
        textField.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.emptySet());

        setupListeners();
    }

    private void setupListeners() {
        // Listen for changes in the text field
        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { fetchSuggestions(); }
            public void removeUpdate(DocumentEvent e) { fetchSuggestions(); }
            public void changedUpdate(DocumentEvent e) { fetchSuggestions(); }
        });

        // Listen for key events to handle navigation/selection and show popup on Tab/Shift+Tab
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Always handle Tab and Shift+Tab to show suggestions if popup is not visible
                if (e.getKeyCode() == KeyEvent.VK_TAB && !suggestionPopup.isVisible()) {
                    String input = textField.getText();
                    if (input != null && !input.trim().isEmpty()) {
                        fetchSuggestions();
                        if (suggestionList != null && listModel != null && !listModel.isEmpty()) {
                            suggestionList.setSelectedIndex(0);
                        }
                        e.consume();
                        return;
                    }
                }

                if (suggestionPopup.isVisible()) {
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        moveSelection(1);
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        moveSelection(-1);
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER
                            || e.getKeyCode() == KeyEvent.VK_TAB) {
                        acceptSelection();
                        e.consume(); // Prevents Tab/Shift+Tab from moving focus
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        suggestionPopup.setVisible(false);
                        restoreFocusTraversalKeys();
                        e.consume();
                    }
                }
            }
        });

        // Hide popup when focus is lost and restore tab traversal
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                suggestionPopup.setVisible(false);
                restoreFocusTraversalKeys();
            }
        });
    }

    private void restoreFocusTraversalKeys() {
        textField.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, defaultForwardKeys);
        textField.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, defaultBackwardKeys);
    }

    private void fetchSuggestions() {
        if (ignoreDocumentChange) return;
        String input = textField.getText();
        if (input == null || input.trim().isEmpty()) {
            suggestionPopup.setVisible(false);
            restoreFocusTraversalKeys();
            return;
        }

        List<String> suggestions = DatabaseSaveHelper.getAutocompleteSuggestions(fieldKey, input.trim());
        if (suggestions.isEmpty()) {
            suggestionPopup.setVisible(false);
            restoreFocusTraversalKeys();
            return;
        }

        showSuggestions(suggestions);
    }

    private void showSuggestions(List<String> suggestions) {
        if (suggestionList == null) {
            listModel = new DefaultListModel<>();
            suggestionList = new JList<>(listModel) {
                @Override
                public Dimension getPreferredSize() {
                    Dimension dim = super.getPreferredSize();
                    dim.width = textField.getWidth();
                    dim.height = 25 * Math.max(1, getModel().getSize());
                    return dim;
                }
            };
            suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // FlatLaf MacOS styling and custom painter
            suggestionList.setFont(textField.getFont());
            suggestionList.setFocusable(false);
            suggestionList.setBackground(UIManager.getColor("PopupMenu.background"));
            suggestionList.setForeground(UIManager.getColor("PopupMenu.foreground"));
            suggestionList.setSelectionBackground(UIManager.getColor("List.selectionBackground"));
            suggestionList.setSelectionForeground(UIManager.getColor("List.selectionForeground"));
            suggestionList.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            suggestionList.setCellRenderer(new SuggestionPanelRenderer(textField));

            suggestionList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        acceptSelection();
                    }
                }
            });
        }

        listModel.clear();
        for (String s : suggestions) listModel.addElement(s);
        suggestionList.setSelectedIndex(0);

        suggestionPopup.removeAll();

        // Remove scrollbars by using a plain JPanel wrapper instead of JScrollPane
        JPanel listPanel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                int rowHeight = 25;
                int maxRows = Math.min(listModel.size(), 6); // adjust max visible rows
                return new Dimension(
                    textField.getWidth(),
                    maxRows * rowHeight
                );
            }
        };
        suggestionList.setVisibleRowCount(Math.min(listModel.size(), 6));
        listPanel.add(suggestionList, BorderLayout.CENTER);
        listPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        suggestionPopup.add(listPanel);
        suggestionPopup.setFocusable(false);

        // Remove popup border and margin to avoid space at top
        suggestionPopup.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Show below text field
        try {
            Rectangle rect = textField.getUI().modelToView(textField, textField.getCaretPosition());
            Point p = new Point(rect.x, rect.y + rect.height);
            SwingUtilities.convertPointToScreen(p, textField);
            suggestionPopup.setPreferredSize(listPanel.getPreferredSize());
            suggestionPopup.show(textField, 0, textField.getHeight());
        } catch (Exception ex) {
            // Fallback if modelToView fails
            suggestionPopup.setPreferredSize(listPanel.getPreferredSize());
            suggestionPopup.show(textField, 0, textField.getHeight());
        }
    }

    private void moveSelection(int delta) {
        if (suggestionList == null || listModel.isEmpty()) return;
        int sel = suggestionList.getSelectedIndex();
        int newSel = Math.max(0, Math.min(listModel.size() - 1, sel + delta));
        suggestionList.setSelectedIndex(newSel);
        suggestionList.ensureIndexIsVisible(newSel);
    }

    private void acceptSelection() {
        if (suggestionList == null || listModel.isEmpty()) return;
        int idx = suggestionList.getSelectedIndex();
        String selected = (idx != -1) ? suggestionList.getModel().getElementAt(idx) : null;
        if (selected == null && listModel.size() == 1) {
            selected = listModel.getElementAt(0);
        }
        if (selected != null) {
            ignoreDocumentChange = true;
            textField.setText(selected);
            ignoreDocumentChange = false;
            DatabaseSaveHelper.saveAutocompleteValue(fieldKey, selected);
        }
        suggestionPopup.setVisible(false);
        restoreFocusTraversalKeys();
    }

    /**
     * Custom cell renderer for suggestion list that paints background and match highlighting
     */
    private static class SuggestionPanelRenderer extends JPanel implements ListCellRenderer<String> {
        private String suggestion;
        private boolean selected;
        private JTextField textField;

        // Colors (match JMenuItem design and UIManager fallbacks)
        private final Color background = UIManager.getColor("PopupMenu.background") != null
                ? UIManager.getColor("PopupMenu.background") : Color.WHITE;
        private final Color foreground = UIManager.getColor("PopupMenu.foreground") != null
                ? UIManager.getColor("PopupMenu.foreground") : Color.BLACK;
        private final Color selectionBg = UIManager.getColor("MenuItem.selectionBackground") != null
                ? UIManager.getColor("MenuItem.selectionBackground") : new Color(51, 153, 255);
        private final Color selectionFg = UIManager.getColor("MenuItem.selectionForeground") != null
                ? UIManager.getColor("MenuItem.selectionForeground") : Color.WHITE;
        private final Color matchedColor = UIManager.getColor("Component.linkColor") != null
                ? UIManager.getColor("Component.linkColor") : new Color(0, 100, 200);

        public SuggestionPanelRenderer(JTextField field) {
            this.textField = field;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); // No border
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            this.suggestion = value;
            this.selected = isSelected;
            setFont(list.getFont());
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            return this;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension dim = super.getPreferredSize();
            dim.width = textField.getWidth();
            dim.height = 25;
            return dim;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Highlight background (full width, no padding)
            if (selected) {
                g2.setColor(selectionBg);
            } else {
                g2.setColor(background);
            }
            g2.fillRect(0, 0, width, height);

            // Draw the text, matching and highlighting like AutocompleteSupport
            String currentText = textField.getText().toLowerCase();
            String suggestionLower = suggestion.toLowerCase();

            int matchStart = suggestionLower.indexOf(currentText);
            int matchEnd = (matchStart >= 0) ? matchStart + currentText.length() : -1;

            int xPos = 5; // match JMenuItem left padding
            Font normalFont = getFont().deriveFont(Font.PLAIN);
            Font boldFont = getFont().deriveFont(Font.BOLD);
            g2.setFont(normalFont);
            FontMetrics fm = g2.getFontMetrics();
            int textY = (height - fm.getHeight()) / 2 + fm.getAscent();

            if (matchStart >= 0 && !currentText.isEmpty()) {
                if (matchStart > 0) {
                    String prefix = suggestion.substring(0, matchStart);
                    g2.setFont(normalFont);
                    g2.setColor(selected ? selectionFg : foreground);
                    g2.drawString(prefix, xPos, textY);
                    xPos += fm.stringWidth(prefix);
                }
                String match = suggestion.substring(matchStart, matchEnd);
                g2.setFont(boldFont);
                g2.setColor(selected ? selectionFg : matchedColor);
                g2.drawString(match, xPos, textY);
                xPos += g2.getFontMetrics().stringWidth(match);

                if (matchEnd < suggestion.length()) {
                    String suffix = suggestion.substring(matchEnd);
                    g2.setFont(normalFont);
                    g2.setColor(selected ? selectionFg : foreground);
                    g2.drawString(suffix, xPos, textY);
                }
            } else {
                g2.setFont(normalFont);
                g2.setColor(selected ? selectionFg : foreground);
                g2.drawString(suggestion, xPos, textY);
            }

            g2.dispose();
        }
    }
}