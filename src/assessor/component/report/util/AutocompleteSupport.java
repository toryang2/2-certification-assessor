package assessor.component.report.util;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
//import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Attach this to a JTextField for popup autocomplete from a database or cache.
 * Usage:
 *   new AutocompleteSupport(txtField, "fieldKey", DatabaseSaveHelper::getAutocompleteSuggestions);
 * 
 * TAB and ENTER will both accept the highlighted suggestion (no focus change).
 */
public class AutocompleteSupport implements DocumentListener, KeyListener, FocusListener {
    private final JTextField textField;
    private final String fieldKey;
    private final BiFunction<String, String, List<String>> suggestionsProvider;
    private final Timer delayTimer;
    private JPopupMenu popupMenu;
    private SwingWorker<List<String>, Void> worker;
    private List<String> currentSuggestions = new ArrayList<>();
    private int selectedIndex = -1;
    private boolean popupVisible = false;
    private String lastQuery = "";
    private boolean ignoreNextFocusGain = false;
    // Store default focus traversal keys for Tab
    private final Set<AWTKeyStroke> defaultForwardKeys;

    public AutocompleteSupport(JTextField textField, String fieldKey,
                              BiFunction<String, String, List<String>> suggestionsProvider) {
        this.textField = textField;
        this.fieldKey = fieldKey;
        this.suggestionsProvider = suggestionsProvider;

        this.popupMenu = new JPopupMenu();
        popupMenu.setLayout(new BoxLayout(popupMenu, BoxLayout.Y_AXIS));
        popupMenu.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        popupMenu.setFocusable(false);

        this.delayTimer = new Timer(0, e -> fetchSuggestions());
        delayTimer.setRepeats(false);

        textField.getDocument().addDocumentListener(this);
        textField.addKeyListener(this);
        textField.addFocusListener(this);

        // TAB handling: while popup visible, TAB accepts suggestion instead of moving focus
        defaultForwardKeys = textField.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        InputMap im = textField.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = textField.getActionMap();
        im.put(KeyStroke.getKeyStroke("TAB"), "autocompleteTab");
        am.put("autocompleteTab", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (popupVisible && selectedIndex >= 0 && selectedIndex < popupMenu.getComponentCount()) {
                    JMenuItem item = (JMenuItem) popupMenu.getComponent(selectedIndex);
                    handleItemSelection(item);
                    // DO NOT move focus - Tab is now "accept suggestion"
                } else {
                    // If popup not visible, do normal tab focus traversal
                    textField.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, defaultForwardKeys);
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent(textField);
                }
            }
        });
    }
    
    public AutocompleteSupport(JTextField textField, String fieldKey) {
        this(textField, fieldKey, DatabaseSaveHelper::getAutocompleteSuggestions);
    }

    private void fetchSuggestions() {
        String text = textField.getText().trim();
        lastQuery = text;

        if (text.isEmpty()) {
            hidePopup();
            return;
        }

        if (worker != null && !worker.isDone()) {
            worker.cancel(true);
        }

        worker = new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                return suggestionsProvider.apply(fieldKey, text);
            }
            @Override
            protected void done() {
                try {
                    if (!isCancelled()) {
                        List<String> suggestions = get();
                        if (text.equals(lastQuery)) {
                            currentSuggestions = suggestions;
                            showSuggestions();
                        }
                    }
                } catch (CancellationException e) {
                    // ignore
                } catch (Exception ex) {
                    ex.printStackTrace();
                    currentSuggestions = Collections.emptyList();
                    hidePopup();
                }
            }
        };
        worker.execute();
    }

    private void showSuggestions() {
        if (ignoreNextFocusGain) {
            ignoreNextFocusGain = false;
            return;
        }

        popupMenu.removeAll();

        final Color background = UIManager.getColor("PopupMenu.background") != null ?
                    UIManager.getColor("PopupMenu.background") : Color.WHITE;
        final Color foreground = UIManager.getColor("PopupMenu.foreground") != null ?
                    UIManager.getColor("PopupMenu.foreground") : Color.BLACK;
        final Color selectionBg = UIManager.getColor("MenuItem.selectionBackground") != null ?
                    UIManager.getColor("MenuItem.selectionBackground") : new Color(51, 153, 255);
        final Color selectionFg = UIManager.getColor("MenuItem.selectionForeground") != null ?
                    UIManager.getColor("MenuItem.selectionForeground") : Color.WHITE;
        final Color matchedColor = UIManager.getColor("Component.linkColor") != null ?
                    UIManager.getColor("Component.linkColor") : new Color(0, 100, 200);

        if (currentSuggestions == null || currentSuggestions.isEmpty()) {
            hidePopup();
            return;
        }

        String currentText = textField.getText().toLowerCase();
        List<String> filteredSuggestions = currentSuggestions.stream()
                .filter(s -> s.toLowerCase().contains(currentText))
                .collect(Collectors.toList());

        if (filteredSuggestions.isEmpty()) {
            hidePopup();
            return;
        }

        for (String suggestion : filteredSuggestions) {
            JMenuItem item = new JMenuItem() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (isArmed()) {
                        g2.setColor(selectionBg);
                    } else {
                        g2.setColor(background);
                    }
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    String currentText = textField.getText().toLowerCase();
                    String suggestionLower = suggestion.toLowerCase();

                    int matchStart = suggestionLower.indexOf(currentText);
                    int matchEnd = (matchStart >= 0) ? matchStart + currentText.length() : -1;

                    int xPos = 5;
                    Font normalFont = getFont().deriveFont(Font.PLAIN);
                    Font boldFont = getFont().deriveFont(Font.BOLD);
                    g2.setFont(normalFont);
                    FontMetrics fm = g2.getFontMetrics();
                    int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

                    if (matchStart >= 0 && !currentText.isEmpty()) {
                        if (matchStart > 0) {
                            String prefix = suggestion.substring(0, matchStart);
                            g2.setFont(normalFont);
                            g2.setColor(isArmed() ? selectionFg : foreground);
                            g2.drawString(prefix, xPos, textY);
                            xPos += fm.stringWidth(prefix);
                        }
                        String match = suggestion.substring(matchStart, matchEnd);
                        g2.setFont(boldFont);
                        g2.setColor(isArmed() ? selectionFg : matchedColor);
                        g2.drawString(match, xPos, textY);
                        xPos += g2.getFontMetrics().stringWidth(match);

                        if (matchEnd < suggestion.length()) {
                            String suffix = suggestion.substring(matchEnd);
                            g2.setFont(normalFont);
                            g2.setColor(isArmed() ? selectionFg : foreground);
                            g2.drawString(suffix, xPos, textY);
                        }
                    } else {
                        g2.setFont(normalFont);
                        g2.setColor(isArmed() ? selectionFg : foreground);
                        g2.drawString(suggestion, xPos, textY);
                    }
                }

                @Override
                public Dimension getPreferredSize() {
                    Dimension dim = super.getPreferredSize();
                    dim.width = textField.getWidth();
                    dim.height = 25;
                    return dim;
                }
            };

            item.putClientProperty("suggestion", suggestion);
            item.setFont(textField.getFont());
            item.setOpaque(false);
            item.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 2));

            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleItemSelection(item);
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    for (Component comp : popupMenu.getComponents()) {
                        if (comp instanceof JMenuItem) {
                            ((JMenuItem) comp).setArmed(false);
                        }
                    }
                    item.setArmed(true);
                    selectedIndex = popupMenu.getComponentIndex(item);
                }
            });
            item.addActionListener(e -> handleItemSelection(item));
            popupMenu.add(item);
        }

        popupMenu.setBackground(background);
        popupMenu.setPreferredSize(new Dimension(
            textField.getWidth(),
            Math.min(200, filteredSuggestions.size() * 25)
        ));

        popupMenu.pack();

        Dimension prefSize = popupMenu.getPreferredSize();
        int maxHeight = Math.min(200, prefSize.height);
        if (prefSize.height > maxHeight) {
            popupMenu.setPreferredSize(new Dimension(prefSize.width, maxHeight));
        }

        popupMenu.show(textField, 0, textField.getHeight());
        popupVisible = true;

        if (popupMenu.getComponentCount() > 0) {
            selectedIndex = 0;
            JMenuItem firstItem = (JMenuItem) popupMenu.getComponent(0);
            firstItem.setArmed(true);
        }
        // Disable Tab as focus traversal when popup is visible
        textField.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());
    }

    private void handleItemSelection(JMenuItem item) {
        String selected = (String) item.getClientProperty("suggestion");
        if (selected != null) {
            Document doc = textField.getDocument();
            doc.removeDocumentListener(this);

            textField.setText(selected);
            textField.setCaretPosition(selected.length());

            doc.addDocumentListener(this);
        }
        hidePopup();
        ignoreNextFocusGain = true;
        textField.requestFocusInWindow();
    }

    private void hidePopup() {
        popupMenu.setVisible(false);
        popupVisible = false;
        selectedIndex = -1;
        for (Component comp : popupMenu.getComponents()) {
            if (comp instanceof JMenuItem) {
                ((JMenuItem) comp).setArmed(false);
            }
        }
        // Restore normal tab focus traversal keys
        textField.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, defaultForwardKeys);
    }

    @Override
    public void insertUpdate(DocumentEvent e) { delayTimer.restart(); }
    @Override
    public void removeUpdate(DocumentEvent e) { delayTimer.restart(); }
    @Override
    public void changedUpdate(DocumentEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (!popupVisible) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_DOWN:
                moveSelection(1);
                e.consume();
                break;
            case KeyEvent.VK_UP:
                moveSelection(-1);
                e.consume();
                break;
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_TAB:
                if (selectedIndex >= 0 && selectedIndex < popupMenu.getComponentCount()) {
                    JMenuItem item = (JMenuItem) popupMenu.getComponent(selectedIndex);
                    handleItemSelection(item);
                    textField.selectAll();
                }
                e.consume();
                break;
            case KeyEvent.VK_ESCAPE:
                hidePopup();
                textField.requestFocus();
                e.consume();
                break;
        }
    }
    private void moveSelection(int direction) {
        int count = popupMenu.getComponentCount();
        if (count == 0) return;

        if (selectedIndex >= 0 && selectedIndex < count) {
            JMenuItem prevItem = (JMenuItem) popupMenu.getComponent(selectedIndex);
            prevItem.setArmed(false);
        }

        selectedIndex = (selectedIndex + direction + count) % count;

        JMenuItem newItem = (JMenuItem) popupMenu.getComponent(selectedIndex);
        newItem.setArmed(true);

        Rectangle bounds = newItem.getBounds();
        Rectangle visible = popupMenu.getVisibleRect();
        visible.y = bounds.y;
        popupMenu.scrollRectToVisible(visible);
    }
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    @Override
    public void focusGained(FocusEvent e) {
        if (!ignoreNextFocusGain) {
            delayTimer.restart();
        } else {
            ignoreNextFocusGain = false;
        }
    }
    @Override
    public void focusLost(FocusEvent e) {
        Component opposite = e.getOppositeComponent();
        if (opposite == null || !SwingUtilities.isDescendingFrom(opposite, popupMenu)) {
            hidePopup();
        }
    }
}