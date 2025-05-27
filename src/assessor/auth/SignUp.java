package assessor.auth;

import assessor.MainFrame;
import assessor.component.ButtonLink;
import assessor.component.report.util.NameCapitalizationFilter;
import assessor.component.report.util.UppercaseDocumentFilter;
import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;
import raven.modal.component.DropShadowBorder;
import assessor.system.Form;
import assessor.system.FormManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;
import raven.modal.ModalDialog;

public class SignUp extends Form {
    
    public SignUp() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("al center center"));
        createSignUp();
    }

    private void createSignUp() {
        JPanel panelSignUp = new JPanel(new BorderLayout()) {
            @Override
            public void updateUI() {
                super.updateUI();
                applyShadowBorder(this);
            }
        };
        panelSignUp.setOpaque(false);
        applyShadowBorder(panelSignUp);

        JPanel signUpContent = new JPanel(new MigLayout("fillx,wrap,insets 35 35 25 35", "[fill,300]"));

        JLabel lbTitle = new JLabel("Create your account");
        JLabel lbDescription = new JLabel("Please fill in the form to sign up");
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "" +
                "font:bold +12;");

        signUpContent.add(lbTitle);
        signUpContent.add(lbDescription);

        JTextField txtUsername = new JTextField();
        JTextField txtName = new JTextField();
        ((AbstractDocument) txtName.getDocument()).setDocumentFilter(new NameCapitalizationFilter());
        JTextField txtInitial = new JTextField(); // New: Initial textbox
        ((AbstractDocument) txtInitial.getDocument()).setDocumentFilter(new UppercaseDocumentFilter());
        JPasswordField txtPassword = new JPasswordField();
        JPasswordField txtConfirmPassword = new JPasswordField();
        JButton cmdSignUp = new JButton("Sign Up") {
            @Override
            public boolean isDefaultButton() {
                return true;
            }
        };

        // Add focus listeners to highlight text
        addHighlightOnFocus(txtUsername);
        addHighlightOnFocus(txtName);
        addHighlightOnFocus(txtInitial); // New: Initial
        addHighlightOnFocus(txtPassword);
        addHighlightOnFocus(txtConfirmPassword);

        // ---- ENTER KEY LISTENERS ----
        txtUsername.addActionListener(e -> {
            txtName.requestFocusInWindow();
            txtName.selectAll();
        });
        txtName.addActionListener(e -> {
            txtInitial.requestFocusInWindow(); // New: move to Initial
            txtInitial.selectAll();
        });
        txtInitial.addActionListener(e -> { // New: Initial -> Password
            txtPassword.requestFocusInWindow();
            txtPassword.selectAll();
        });
        txtPassword.addActionListener(e -> {
            txtConfirmPassword.requestFocusInWindow();
            txtConfirmPassword.selectAll();
        });
        txtConfirmPassword.addActionListener(e -> {
            cmdSignUp.doClick();
        });
        // ---- END ENTER KEY LISTENERS ----

        // style
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your username");
        txtName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your name");
        txtInitial.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your initials");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your password");
        txtConfirmPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Confirm your password");

        panelSignUp.putClientProperty(FlatClientProperties.STYLE, "" +
                "[dark]background:tint($Panel.background,1%);");

        signUpContent.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:null;");

        txtUsername.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:4,10,4,10;" +
                "arc:12;");
        txtName.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:4,10,4,10;" +
                "arc:12;");
        txtInitial.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:4,10,4,10;" +
                "arc:12;");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:4,10,4,10;" +
                "arc:12;" +
                "showRevealButton:true;");
        txtConfirmPassword.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:4,10,4,10;" +
                "arc:12;" +
                "showRevealButton:true;");

        cmdSignUp.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:4,10,4,10;" +
                "arc:12;");
//        cmdBackToLogin.putClientProperty(FlatClientProperties.STYLE, "" +
//                "margin:4,10,4,10;" +
//                "arc:12;");

        signUpContent.add(new JLabel("Username"), "gapy 25");
        signUpContent.add(txtUsername);
        signUpContent.add(new JLabel("Name"), "gapy 10");
        signUpContent.add(txtName);
        signUpContent.add(new JLabel("Initial"), "gapy 10"); // New: Initial
        signUpContent.add(txtInitial);
        signUpContent.add(new JLabel("Password"), "gapy 10");
        signUpContent.add(txtPassword);
        signUpContent.add(new JLabel("Confirm Password"), "gapy 10");
        signUpContent.add(txtConfirmPassword);
        signUpContent.add(cmdSignUp, "gapy 20");
        signUpContent.add(new JLabel("Already have an account?"), "split 2, gapx push n");
        ButtonLink cmdBackToLogin = new ButtonLink("Login");
        signUpContent.add(cmdBackToLogin, "gapx n push");

        panelSignUp.add(signUpContent);
        add(panelSignUp);

        // event
        cmdSignUp.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String name = txtName.getText().trim();
            String initial = txtInitial.getText().trim(); // New: get initial
            String password = new String(txtPassword.getPassword());
            String confirmPassword = new String(txtConfirmPassword.getPassword());

            // Simple validation
            if (username.isEmpty() || name.isEmpty() || initial.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Sign Up Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Sign Up Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Call registration logic (pass initial)
            boolean registrationSuccess = Authenticator.register(username, password, name, initial);

            if (registrationSuccess) {
                JOptionPane.showMessageDialog(this, "Account created successfully! You can now log in.", "Sign Up Success", JOptionPane.INFORMATION_MESSAGE);
                // Optionally, return to login form
                FormManager.backtologin();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create account. Username may already exist.", "Sign Up Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        cmdBackToLogin.addActionListener(e -> {
            FormManager.backtologin();
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