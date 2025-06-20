package assessor.auth;

import assessor.MainFrame;
import assessor.component.ButtonLink;
//import assessor.component.report.util.NameCapitalizationFilter;
//import assessor.component.report.util.UppercaseDocumentFilter;
import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;
import raven.modal.component.DropShadowBorder;
import assessor.system.Form;
import assessor.system.FormManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
//        ((AbstractDocument) txtName.getDocument()).setDocumentFilter(new NameCapitalizationFilter());
        JTextField txtInitial = new JTextField(); // New: Initial textbox
//        ((AbstractDocument) txtInitial.getDocument()).setDocumentFilter(new UppercaseDocumentFilter());
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
            String name = capitalizeName(txtName.getText().trim());
            String initial = uppercaseInitial(txtInitial.getText().trim()); // New: get initial
            String password = new String(txtPassword.getPassword());
            String confirmPassword = new String(txtConfirmPassword.getPassword());

//            // Simple validation
//            if (username.isEmpty() || name.isEmpty() || initial.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
//                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Sign Up Failed", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
            // Validate empty fields and focus on the first empty one
            // Validate empty fields and collect all empty ones
            // Reset all fields to default background before validation
            // Reset all fields to default background before validation
            for (JTextComponent field : new JTextComponent[]{txtUsername, txtName, txtInitial, txtPassword, txtConfirmPassword}) {
                field.setBackground(SystemColor.window);
            }

            // Validate empty fields and collect all empty ones
            JTextComponent[] fields = {txtUsername, txtName, txtInitial, txtPassword, txtConfirmPassword};
            String[] fieldNames = {"Username", "Name", "Initial", "Password", "Confirm Password"};
            java.util.List<Integer> emptyIndices = new java.util.ArrayList<>();
            for (int i = 0; i < fields.length; i++) {
                String text = fields[i] instanceof JPasswordField ? new String(((JPasswordField) fields[i]).getPassword()) : fields[i].getText().trim();
                if (text.isEmpty()) {
                    emptyIndices.add(i);
                    fields[i].setBackground(new Color(255, 255, 204)); // Faint yellow for empty fields
                }
            }

            // Show appropriate error message and focus on first empty field
            if (!emptyIndices.isEmpty()) {
                String message;
                if (emptyIndices.size() == 1) {
                    message = fieldNames[emptyIndices.get(0)] + " cannot be empty.";
                } else {
                    String emptyFields = emptyIndices.stream()
                            .map(i -> fieldNames[i])
                            .collect(Collectors.joining(", "));
                    message = "The following fields cannot be empty: " + emptyFields + ".";
                }
                JOptionPane.showMessageDialog(this, message, "Sign Up Failed", JOptionPane.ERROR_MESSAGE);
                fields[emptyIndices.get(0)].requestFocusInWindow();
                fields[emptyIndices.get(0)].selectAll();
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Sign Up Failed", JOptionPane.ERROR_MESSAGE);
                txtConfirmPassword.setBackground(new Color(255, 255, 204)); // Highlight confirm password
                txtConfirmPassword.requestFocusInWindow();
                txtConfirmPassword.selectAll();
                return;
            }

            // Call registration logic (pass initial)
            boolean registrationSuccess = Authenticator.register(username, password, name, initial);

            if (registrationSuccess) {
                JOptionPane.showMessageDialog(this, "Account created successfully! You can now log in.", "Sign Up Success", JOptionPane.INFORMATION_MESSAGE);
                // Clear all fields
                txtUsername.setText("");
                txtName.setText("");
                txtInitial.setText("");
                txtPassword.setText("");
                txtConfirmPassword.setText("");
                // Optionally, return to login form
                FormManager.backtologin();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create account. Username may already exist.", "Sign Up Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        cmdBackToLogin.addActionListener(e -> {
                txtUsername.setText("");
                txtName.setText("");
                txtInitial.setText("");
                txtPassword.setText("");
                txtConfirmPassword.setText("");
            FormManager.backtologin();
        });
    }
    
        // Helper method to capitalize the first letter of each word in the name
    private String capitalizeName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        String[] words = name.trim().split("\\s+");
        StringBuilder capitalized = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                capitalized.append(Character.toUpperCase(word.charAt(0)))
                           .append(word.substring(1).toLowerCase())
                           .append(" ");
            }
        }
        return capitalized.toString().trim();
    }

    // Helper method to convert initial to uppercase
    private String uppercaseInitial(String initial) {
        if (initial == null || initial.isEmpty()) {
            return initial;
        }
        return initial.toUpperCase();
    }
    
    private void addHighlightOnFocus(JTextComponent textComponent) {
        // Store the original background color
        final Color originalBackground = textComponent.getBackground();

        textComponent.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textComponent.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                textComponent.setBackground(originalBackground); // Revert to original on focus lost
            }
        });

        textComponent.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Revert to original background when typing starts, but only if the field is not empty
                String text = textComponent instanceof JPasswordField ? new String(((JPasswordField) textComponent).getPassword()) : textComponent.getText().trim();
                if (!text.isEmpty()) {
                    textComponent.setBackground(originalBackground);
                }
            }
        });
    }

    private void applyShadowBorder(JPanel panel) {
        if (panel != null) {
            panel.setBorder(new DropShadowBorder(new Insets(5, 8, 12, 8), 1, 25));
        }
    }
}