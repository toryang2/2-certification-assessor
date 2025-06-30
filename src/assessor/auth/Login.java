package assessor.auth;

import assessor.MainFrame;
import assessor.component.ButtonLink;
import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;
import raven.modal.component.DropShadowBorder;
import assessor.system.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.JTextComponent;

public class Login extends Form {

    public static final String ID = "login_id";

    public Login() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("al center center"));
        createLogin();
    }

    private void createLogin() {
        JPanel panelLogin = new JPanel(new BorderLayout()) {
            @Override
            public void updateUI() {
                super.updateUI();
                applyShadowBorder(this);
            }
        };
        panelLogin.setOpaque(false);
        applyShadowBorder(panelLogin);

        JPanel loginContent = new JPanel(new MigLayout("fillx,wrap,insets 35 35 25 35", "[fill,300]"));

        // ---- ICON + TITLE ----
        JPanel panelTitle = new JPanel(new MigLayout("al center center, wrap 1, insets 0", "[center]"));
        panelTitle.setOpaque(false);
        // You can use any SVG or PNG icon you have in your resources (adjust the path as needed)
        // If you don't have an SVG, you can use new ImageIcon(getClass().getResource("/path/to/icon.png"))
        JLabel lbIcon = new JLabel(new ImageIcon(getClass().getResource("/assessor/icons/logo.png")));
        lbIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lbIcon.putClientProperty(FlatClientProperties.STYLE, "");
        JLabel lbTitle = new JLabel("Welcome back!");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +12;");
        lbTitle.setHorizontalAlignment(SwingConstants.CENTER);
        panelTitle.add(lbIcon);
        panelTitle.add(Box.createHorizontalStrut(10));
        panelTitle.add(lbTitle);
        // ---- END ICON + TITLE ----

        JLabel lbDescription = new JLabel("Please sign in to access your account");
        lbDescription.setHorizontalAlignment(SwingConstants.CENTER);

        loginContent.add(panelTitle, "gapy 10");
        loginContent.add(lbDescription);

        JTextField txtUsername = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JCheckBox chRememberMe = new JCheckBox("Remember Me");
        JButton cmdLogin = new JButton("Login") {
            @Override
            public boolean isDefaultButton() {
                return true;
            }
        };

        // Add focus listeners to highlight text
        addHighlightOnFocus(txtUsername);
        addHighlightOnFocus(txtPassword);

        // ---- ENTER KEY LISTENERS ----
        txtUsername.addActionListener((ActionEvent e) -> {
            txtPassword.requestFocusInWindow();
            txtPassword.selectAll();
        });

        txtPassword.addActionListener((ActionEvent e) -> {
            cmdLogin.doClick();
        });
        // ---- END ENTER KEY LISTENERS ----

        // style
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your username or email");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your password");

        panelLogin.putClientProperty(FlatClientProperties.STYLE, "" +
                "[dark]background:tint($Panel.background,1%);");

        loginContent.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:null;");

        txtUsername.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:4,10,4,10;" +
                "arc:12;");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:4,10,4,10;" +
                "arc:12;" +
                "showRevealButton:true;");

        cmdLogin.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:4,10,4,10;" +
                "arc:12;");

        loginContent.add(new JLabel("Username"), "gapy 25");
        loginContent.add(txtUsername);

        loginContent.add(new JLabel("Password"), "gapy 10");
        loginContent.add(txtPassword);
        loginContent.add(chRememberMe);
        loginContent.add(cmdLogin, "gapy 20");
        loginContent.add(new JLabel("Don't have an account?"), "split 2, gapx push n");
        ButtonLink cmdSignUp = new ButtonLink("Sign up");
        loginContent.add(cmdSignUp, "gapx n push");

        panelLogin.add(loginContent);
        add(panelLogin);

        // event
        cmdLogin.addActionListener(e -> {
            String username = txtUsername.getText();
            String password = new String(txtPassword.getPassword());

            if (Authenticator.authenticate(username, password)) {
                System.out.println("Login successful!");

                // Refresh drawer header to reflect new session info
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                if (topFrame instanceof MainFrame) {
                    ((MainFrame) topFrame).forceDrawerRefresh();
                }

                // Continue with your original app flow
                FormManager.login();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        cmdSignUp.addActionListener(e -> {
            FormManager.signUp();
        });
    }

    private void addHighlightOnFocus(JTextComponent textComponent) {
        textComponent.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textComponent.selectAll(); // Select all text when the component gains focus
            }
        });
    }

    private void applyShadowBorder(JPanel panel) {
        if (panel != null) {
            panel.setBorder(new DropShadowBorder(new Insets(5, 8, 12, 8), 1, 25));
        }
    }
}