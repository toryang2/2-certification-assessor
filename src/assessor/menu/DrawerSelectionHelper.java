package assessor.menu;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import assessor.system.FormManager;


/**
 * Helper class for selecting specific drawer menu items without expanding menus
 */
public class DrawerSelectionHelper {
    
    private static final Logger LOGGER = Logger.getLogger(DrawerSelectionHelper.class.getName());
    
    /**
     * Selects the "Total Landholding" item under "Dashboard" without expanding other menus
     * @param container The drawer container to search in
     */
    public static void selectTotalLandholdingDashboard(Container container) {
        LOGGER.info("Starting Total Landholding Dashboard selection...");
        
        SwingUtilities.invokeLater(() -> {
            Timer timer = new Timer(100, e -> {
                try {
                    // Find and click the Total Landholding item under Dashboard
                    findAndClickTotalLandholding(container);
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Could not select Total Landholding Dashboard item: " + ex.getMessage());
                }
            });
            timer.setRepeats(false);
            timer.start();
        });
    }
    
    /**
     * Alternative method that tries to expand Dashboard first, then select Total Landholding
     * @param container The drawer container to search in
     */
    public static void selectTotalLandholdingDashboardWithExpansion(Container container) {
        LOGGER.info("Starting Total Landholding Dashboard selection with expansion...");
        
        SwingUtilities.invokeLater(() -> {
            Timer timer = new Timer(100, e -> {
                try {
                    // First, collapse Input menu if it's expanded
                    collapseInputMenu(container);
                    
                    // Then expand Dashboard if needed
                    expandDashboardIfNeeded(container);
                    
                    // Then select Total Landholding after a delay
                    Timer selectTimer = new Timer(300, selectEvent -> {
                        findAndClickTotalLandholding(container);
                    });
                    selectTimer.setRepeats(false);
                    selectTimer.start();
                    
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Could not select Total Landholding Dashboard item: " + ex.getMessage());
                }
            });
            timer.setRepeats(false);
            timer.start();
        });
    }
    
    /**
     * Simple method that just expands Dashboard and lets the automatic highlighting work
     * @param container The drawer container to search in
     */
    public static void prepareDashboardForHighlighting(Container container) {
        LOGGER.info("Preparing Dashboard for automatic highlighting...");
        
        SwingUtilities.invokeLater(() -> {
            Timer timer = new Timer(100, e -> {
                try {
                    // Just expand Dashboard and let the automatic highlighting do the rest
                    expandDashboardIfNeeded(container);
                    LOGGER.info("Dashboard expanded, automatic highlighting should work now");
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Could not prepare Dashboard: " + ex.getMessage());
                }
            });
            timer.setRepeats(false);
            timer.start();
        });
    }
    
    /**
     * Finds the drawer panel and prepares Dashboard for highlighting
     * This method works without needing MyDrawerBuilder
     */
    public static void prepareDashboardForHighlighting() {
        LOGGER.info("Finding drawer panel and preparing Dashboard...");
        
        SwingUtilities.invokeLater(() -> {
            Timer timer = new Timer(100, e -> {
                try {
                    // Find the drawer panel by searching through the main frame
                    Container drawerPanel = findDrawerPanel();
                    if (drawerPanel != null) {
                        LOGGER.info("Found drawer panel, preparing Dashboard...");
                        prepareDashboardForHighlighting(drawerPanel);
                    } else {
                        LOGGER.warning("Could not find drawer panel");
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Could not prepare Dashboard: " + ex.getMessage());
                }
            });
            timer.setRepeats(false);
            timer.start();
        });
    }
    
    /**
     * Finds the drawer panel by searching through the main frame
     * @return The drawer panel container, or null if not found
     */
    private static Container findDrawerPanel() {
        try {
            // Get the main frame
            JFrame mainFrame = FormManager.getFrame();
            if (mainFrame == null) {
                LOGGER.warning("Main frame is null");
                return null;
            }
            
            // Search for the drawer panel in the main frame
            return findDrawerPanelRecursive(mainFrame);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error finding drawer panel: " + ex.getMessage());
            return null;
        }
    }
    
    /**
     * Recursively searches for the drawer panel
     * @param container The container to search in
     * @return The drawer panel container, or null if not found
     */
    private static Container findDrawerPanelRecursive(Container container) {
        if (container == null) {
            return null;
        }
        
        // Check if this is a drawer panel by class name
        String className = container.getClass().getSimpleName();
        if (className.contains("DrawerPanel") || className.contains("drawer")) {
            LOGGER.info("Found drawer panel: " + className);
            return container;
        }
        
        // Search in child components
        for (Component comp : container.getComponents()) {
            if (comp instanceof Container) {
                Container result = findDrawerPanelRecursive((Container) comp);
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Expands the Dashboard menu if it's not already expanded
     * @param container The container to search in
     */
    private static void expandDashboardIfNeeded(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if (button.getText() != null && button.getText().contains("Dashboard")) {
                    LOGGER.info("Found Dashboard button, checking if expanded...");
                    // If the button is not selected, click it to expand
                    if (!button.isSelected()) {
                        LOGGER.info("Dashboard not expanded, clicking to expand...");
                        button.doClick();
                    } else {
                        LOGGER.info("Dashboard already expanded");
                    }
                    return;
                }
            } else if (comp instanceof Container) {
                expandDashboardIfNeeded((Container) comp);
            }
        }
    }
    
    /**
     * Collapses the Input menu if it's expanded
     * @param container The container to search in
     */
    private static void collapseInputMenu(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if (button.getText() != null && button.getText().contains("Input")) {
                    LOGGER.info("Found Input button, checking if expanded...");
                    // If the button is selected (expanded), click it to collapse
                    if (button.isSelected()) {
                        LOGGER.info("Input menu is expanded, clicking to collapse...");
                        button.doClick();
                    } else {
                        LOGGER.info("Input menu already collapsed");
                    }
                    return;
                }
            } else if (comp instanceof Container) {
                collapseInputMenu((Container) comp);
            }
        }
    }
    

    
    /**
     * Recursively searches for and clicks the "Total Landholding" button under Dashboard
     * @param container The container to search in
     */
    private static void findAndClickTotalLandholding(Container container) {
        LOGGER.info("Searching for Total Landholding in container: " + container.getClass().getSimpleName());
        
        for (Component comp : container.getComponents()) {
            LOGGER.info("Found component: " + comp.getClass().getSimpleName() + 
                       (comp instanceof JButton ? " - Text: " + ((JButton) comp).getText() : ""));
            
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if (button.getText() != null && button.getText().contains("Total Landholding")) {
                    LOGGER.info("Found Total Landholding button: " + button.getText());
                    // Check if this is under Dashboard (not under Input)
                    if (isUnderDashboard(container)) {
                        LOGGER.info("Confirmed under Dashboard, clicking...");
                        button.doClick();
                        return;
                    } else {
                        LOGGER.info("Not under Dashboard, skipping...");
                    }
                }
            } else if (comp instanceof Container) {
                findAndClickTotalLandholding((Container) comp);
            }
        }
    }
    
    /**
     * Checks if the current container is under the Dashboard menu
     * @param container The container to check
     * @return true if under Dashboard, false otherwise
     */
    private static boolean isUnderDashboard(Container container) {
        LOGGER.info("Checking if container is under Dashboard...");
        
        // Look for Dashboard button in parent containers
        Container parent = container.getParent();
        int level = 0;
        while (parent != null && level < 10) { // Prevent infinite loop
            LOGGER.info("Checking parent level " + level + ": " + parent.getClass().getSimpleName());
            
            for (Component comp : parent.getComponents()) {
                if (comp instanceof JButton) {
                    JButton button = (JButton) comp;
                    if (button.getText() != null && button.getText().contains("Dashboard")) {
                        LOGGER.info("Found Dashboard button in parent: " + button.getText());
                        return true;
                    }
                }
            }
            parent = parent.getParent();
            level++;
        }
        
        LOGGER.info("No Dashboard button found in parent hierarchy");
        return false;
    }
}
