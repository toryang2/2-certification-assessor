package assessor.menu;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import raven.extras.AvatarIcon;
import assessor.MainFrame;
import assessor.auth.Authenticator;
import assessor.auth.SessionManager;
import assessor.component.report.input.*;
import assessor.forms.*;
import assessor.system.AllForms;
import assessor.system.Form;
import assessor.system.FormManager;
import raven.modal.drawer.DrawerPanel;
import raven.modal.drawer.item.Item;
import raven.modal.drawer.item.MenuItem;
import raven.modal.drawer.menu.MenuAction;
import raven.modal.drawer.menu.MenuEvent;
import raven.modal.drawer.menu.MenuOption;
import raven.modal.drawer.menu.MenuStyle;
import raven.modal.drawer.renderer.DrawerStraightDotLineStyle;
import raven.modal.drawer.simple.SimpleDrawerBuilder;
import raven.modal.drawer.simple.footer.LightDarkButtonFooter;
import raven.modal.drawer.simple.footer.SimpleFooterData;
import raven.modal.drawer.simple.header.SimpleHeaderData;
import raven.modal.option.Location;
import raven.modal.option.Option;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import raven.modal.ModalDialog;
import java.util.logging.Logger;

public class MyDrawerBuilder extends SimpleDrawerBuilder {

    private static final Logger LOGGER = Logger.getLogger(MyDrawerBuilder.class.getName());
    private final int SHADOW_SIZE = 12;

    public MyDrawerBuilder() {
        super(createSimpleMenuOption());
        LightDarkButtonFooter lightDarkButtonFooter = (LightDarkButtonFooter) getFooter();
        lightDarkButtonFooter.addModeChangeListener(isDarkMode -> {
            LOGGER.fine("Light/Dark mode changed to: " + isDarkMode);
            // event for light dark mode changed
        });
    }

@Override
public SimpleHeaderData getSimpleHeaderData() {
    // Retrieve the username and initials from SessionManager
    String username = SessionManager.getInstance().getLoggedInUsername();
    if (username != null) {
        username = username.toLowerCase();
    }
    String userInitials = SessionManager.getInstance().getUserInitials();

    // Debugging: Verify fetched values
    System.out.println("getSimpleHeaderData - Username: " + username);
    System.out.println("getSimpleHeaderData - User Initials: " + userInitials);
    
    if ("admin".equalsIgnoreCase(userInitials)) {
        userInitials = "admin";
    }

    AvatarIcon icon = new AvatarIcon(new FlatSVGIcon("assessor/drawer/image/avatar_male.svg", 100, 100), 50, 50, 3.5f);
    icon.setType(AvatarIcon.Type.MASK_SQUIRCLE);
    icon.setBorder(2, 2);

    changeAvatarIconBorderColor(icon);

    UIManager.addPropertyChangeListener(evt -> {
        if (evt.getPropertyName().equals("lookAndFeel")) {
            changeAvatarIconBorderColor(icon);
        }
    });

    return new SimpleHeaderData()
            .setIcon(icon)
            .setTitle("Welcome back!")
            .setDescription(
    username != null && !username.isEmpty() && userInitials != null && !userInitials.isEmpty()
        ? username + " (" + userInitials + ")"
        : (username != null ? username : "Default Username")
);
}

    private void changeAvatarIconBorderColor(AvatarIcon icon) {
        icon.setBorderColor(new AvatarIcon.BorderColor(UIManager.getColor("Component.accentColor"), 0.7f));
    }
    
// Change this method in MyDrawerBuilder
private static void showHospitalizationModal() {
    FormDashboard.resetFilters();
    LOGGER.info("Showing Hospitalization modal...");
    Option option = ModalDialog.createOption()
        .setAnimationEnabled(true)
        .setCloseOnPressedEscape(true)
        .setBackgroundClickType(Option.BackgroundClickType.CLOSE_MODAL);
    option.getLayoutOption()
        .setLocation(Location.CENTER, Location.CENTER)
        .setAnimateDistance(0, 0);

    LOGGER.info("Creating new HospitalizationForm instance...");
    FormHospitalization form = new FormHospitalization();
    form.setSaveCallback(success -> {
        if (success) {
            LOGGER.info("Save successful. DataChangeNotifier will refresh the table.");
            // No need to manually call hardRefresh() or similar methods
        } else {
            LOGGER.warning("Save failed. No action taken.");
        }
    });

    ModalDialog.showModal(FormManager.getFrame(), form.createCustomBorder(), option);
        
    SwingUtilities.invokeLater(() -> {
        form.txtParentGuardian.requestFocusInWindow();
    });
}

private static void showScholarshipModal() {
    FormDashboard.resetFilters();
    LOGGER.info("Showing Scholarship modal...");
    Option option = ModalDialog.createOption()
        .setAnimationEnabled(true)
        .setCloseOnPressedEscape(true)
        .setBackgroundClickType(Option.BackgroundClickType.CLOSE_MODAL);
    option.getLayoutOption()
        .setLocation(Location.CENTER, Location.CENTER)
        .setAnimateDistance(0, 0);

    LOGGER.info("Creating new Scholarship instance...");
    FormScholarship form = new FormScholarship();
    form.setSaveCallback(success -> {
        if (success) {
            LOGGER.info("Save successful. DataChangeNotifier will refresh the table.");
            // No need to manually call hardRefresh() or similar methods
        } else {
            LOGGER.warning("Save failed. No action taken.");
        }
    });

    ModalDialog.showModal(FormManager.getFrame(), form.createCustomBorder(), option);
}

private static void showPhilHealthModal() {
    FormDashboard.resetFilters();
    LOGGER.info("Showing Scholarship modal...");
    Option option = ModalDialog.createOption()
        .setAnimationEnabled(true)
        .setCloseOnPressedEscape(true)
        .setBackgroundClickType(Option.BackgroundClickType.CLOSE_MODAL);
    option.getLayoutOption()
        .setLocation(Location.CENTER, Location.CENTER)
        .setAnimateDistance(0, 0);

    LOGGER.info("Creating new Scholarship instance...");
    FormPhilHealth2 form = new FormPhilHealth2();
    form.setSaveCallback(success -> {
        if (success) {
            LOGGER.info("Save successful. DataChangeNotifier will refresh the table.");
            // No need to manually call hardRefresh() or similar methods
        } else {
            LOGGER.warning("Save failed. No action taken.");
        }
    });

    ModalDialog.showModal(FormManager.getFrame(), form.createCustomBorder(), option);
}

    @Override
    public SimpleFooterData getSimpleFooterData() {
        return new SimpleFooterData()
                .setTitle("Swing Modal Dialog")
                .setDescription("Version " + MainFrame.VERSION);
    }

    @Override
    public Option createOption() {
        Option option = super.createOption();
        option.setOpacity(0.3f);
        option.getBorderOption()
                .setShadowSize(new Insets(0, 0, 0, SHADOW_SIZE));
        return option;
    }

    public static MenuOption createSimpleMenuOption() {

        // create simple menu option
        MenuOption simpleMenuOption = new MenuOption();

        MenuItem items[] = new MenuItem[]{
                new Item.Label("MAIN"),
                new Item("Dashboard", "dashboard.svg", FormDashboard.class),
                new Item.Label("No Landholding"),
//                new Item("Forms", "forms.svg")
//                        .subMenu(new Item("Input")
//                                    .subMenu("TestInput", FormInput.class)
//                                    .subMenu("Hospitalization", HospitalizationForm.class)
//                        )
////                        .subMenu("Table", FormTable.class)
//                        .subMenu("Responsive Layout", FormResponsiveLayout.class),
                new Item("Hospitalization", "forms.svg", FormHospitalization.class),
                new Item("Scholarship", "forms.svg", FormScholarship.class),
                new Item("PhilHealth", "forms.svg", FormPhilHealth2.class),
                new Item("Components", "components.svg")
                        .subMenu("Modal", FormModal.class)
                        .subMenu("Toast", FormToast.class)
                        .subMenu("Date Time", FormDateTime.class)
                        .subMenu("Avatar Icon", FormAvatarIcon.class)
                        .subMenu("Slide Pane", FormSlidePane.class),
                new Item("Email", "email.svg")
                        .subMenu("Inbox")
                        .subMenu(
                                new Item("Group Read")
                                        .subMenu("Read 1")
                                        .subMenu("Read 2")
                                        .subMenu(
                                                new Item("Group Item")
                                                        .subMenu("Item 1")
                                                        .subMenu("Item 2")
                                                        .subMenu("Item 3")
                                                        .subMenu("Item 4")
                                                        .subMenu("Item 5")
                                                        .subMenu("Item 6")
                                        )
                                        .subMenu("Read 3")
                                        .subMenu("Read 4")
                                        .subMenu("Read 5")
                        )
                        .subMenu("Compost"),
                new Item("Chat", "chat.svg"),
                new Item("Calendar", "calendar.svg"),
                new Item.Label("OTHER"),
                new Item("Plugin", "plugin.svg")
                        .subMenu("Plugin 1")
                        .subMenu("Plugin 2")
                        .subMenu("Plugin 3"),
                new Item("Setting", "setting.svg", FormSetting.class),
                new Item("About", "about.svg"),
                new Item("Logout", "logout.svg")
        };

        simpleMenuOption.setMenuStyle(new MenuStyle() {

            @Override
            public void styleMenuItem(JButton menu, int[] index, boolean isMainItem) {
                boolean isTopLevel = index.length == 1;
                if (isTopLevel) {
                    // adjust item menu at the top level because it's contain icon
                    menu.putClientProperty(FlatClientProperties.STYLE, "" +
                            "margin:-1,0,-1,0;");
                }
            }

            @Override
            public void styleMenu(JComponent component) {
                component.putClientProperty(FlatClientProperties.STYLE, getDrawerBackgroundStyle());
            }
        });

        simpleMenuOption.getMenuStyle().setDrawerLineStyleRenderer(new DrawerStraightDotLineStyle());
        simpleMenuOption.setMenuItemAutoSelectionMode(MenuOption.MenuItemAutoSelectionMode.SELECT_SUB_MENU_LEVEL);
        simpleMenuOption.addMenuEvent(new MenuEvent() {
            @Override
            public void selected(MenuAction action, int[] index) {
                System.out.println("Drawer menu selected " + Arrays.toString(index));
                Class<?> itemClass = action.getItem().getItemClass();
                int i = index[0];
                if (i == 10) {
                    action.consume();
                    FormManager.showAbout();
                    return;
                } else if (i == 11) {
                    action.consume();
                    FormManager.logout();
                    return;
                }
                if (itemClass == null || !Form.class.isAssignableFrom(itemClass)) {
                    action.consume();
                    return;
                }
                Class<? extends Form> formClass = (Class<? extends Form>) itemClass;

                // Special handling for HospitalizationForm
                if (formClass == FormHospitalization.class) {
                    showHospitalizationModal();
                    action.consume();
                    return;
                }
                
                if(formClass == FormScholarship.class) {
                    showScholarshipModal();
                    action.consume();
                    return;
                }
                
                if(formClass == FormPhilHealth2.class) {
                    showPhilHealthModal();
                    action.consume();
                    return;
                }

                // Default handling for other forms
                Form existingForm = FormManager.getActiveForm(formClass);
                if (existingForm == null) {
                    Form newForm = AllForms.getForm(formClass);
                    FormManager.showForm(newForm);
                } else {
                    FormManager.showForm(existingForm);
                }
            }
        });

        simpleMenuOption.setMenus(items)
                .setBaseIconPath("assessor/drawer/icon")
                .setIconScale(0.45f);

        return simpleMenuOption;
    }

    @Override
    public int getDrawerWidth() {
        return 270 + SHADOW_SIZE;
    }

    @Override
    public int getDrawerCompactWidth() {
        return 80 + SHADOW_SIZE;
    }

    @Override
    public int getOpenDrawerAt() {
        return 1000;
    }

    @Override
    public boolean openDrawerAtScale() {
        return false;
    }

    @Override
    public void build(DrawerPanel drawerPanel) {
        drawerPanel.putClientProperty(FlatClientProperties.STYLE, getDrawerBackgroundStyle());
    }

    private static String getDrawerBackgroundStyle() {
        return "" +
                "[light]background:tint($Panel.background,20%);" +
                "[dark]background:tint($Panel.background,5%);";
    }
}
