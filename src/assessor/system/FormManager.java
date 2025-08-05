package assessor.system;

import assessor.component.chart.CertificateTable;
import raven.modal.Drawer;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import assessor.auth.Login;
import assessor.auth.SessionManager;
import assessor.auth.SignUp;
import assessor.component.About;
import assessor.forms.FormDashboard;
import assessor.utils.UndoRedo;
import java.util.*;

import javax.swing.*;

public class FormManager {

    protected static final UndoRedo<Form> FORMS = new UndoRedo<>();
    private static final Map<Class<? extends Form>, Form> ACTIVE_FORMS = new HashMap<>();
    private static JFrame frame;
    private static MainForm mainForm;
    private static Login login;
    private static SignUp signup;

    public static void install(JFrame f) {
        frame = f;
        install();
        logout();
    }

    private static void install() {
        FormSearch.getInstance().installKeyMap(getMainForm());
    }

    public static void showForm(Form form) {
        Form current = FORMS.getCurrent();
        if (form != current) {
            if (current instanceof CertificateTable) {
                ((CertificateTable) current).formDispose();
            }
            FORMS.add(form);
//        if (form != FORMS.getCurrent()) {
//            FORMS.add(form);
            ACTIVE_FORMS.put(form.getClass(), form); // Track active form instances
            form.formCheck();
            form.formOpen();
            mainForm.setForm(form);
            mainForm.refresh();
        }
    }

    public static <T extends Form> T getActiveForm(Class<T> formClass) {
        return formClass.cast(ACTIVE_FORMS.get(formClass));
    }

    public static void undo() {
        if (FORMS.isUndoAble()) {
            Form form = FORMS.undo();
            form.formCheck();
            form.formOpen();
            mainForm.setForm(form);
            Drawer.setSelectedItemClass(form.getClass());
        }
    }

    public static void redo() {
        if (FORMS.isRedoAble()) {
            Form form = FORMS.redo();
            form.formCheck();
            form.formOpen();
            mainForm.setForm(form);
            Drawer.setSelectedItemClass(form.getClass());
        }
    }

    public static void refresh() {
        if (FORMS.getCurrent() != null) {
            FORMS.getCurrent().formRefresh();
            mainForm.refresh();
        }
    }

    public static void login() {
        Drawer.setVisible(true);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(getMainForm());

        // Ensure CertificateTable is created fresh and data is loaded
        FormDashboard formDashboard = new FormDashboard();
        ACTIVE_FORMS.put(FormDashboard.class, formDashboard);
        formDashboard.formRefresh();

        Drawer.setSelectedItemClass(FormDashboard.class);
        frame.repaint();
        frame.revalidate();
    }

    public static void logout() {
        ACTIVE_FORMS.remove(FormDashboard.class);
        SessionManager.getInstance().clearSession();
        Drawer.setVisible(false);
        frame.getContentPane().removeAll();
        Form login = getLogin();
        login.formCheck();
        if (login instanceof Login) {
            ((Login) login).clearFields();
        }
        frame.getContentPane().add(login);
        FORMS.clear();
        frame.repaint();
        frame.revalidate();
    }

    public static void signUp() {
        Drawer.setVisible(false);
        frame.getContentPane().removeAll();
        Form signup = getSignUp();
        signup.formCheck();
        frame.getContentPane().add(signup);
        FORMS.clear();
        frame.repaint();
        frame.revalidate();
    }

    public static void backtologin() {
        Drawer.setVisible(false);
        frame.getContentPane().removeAll();
        Form login = getLogin();
        login.formCheck();
        if (login instanceof Login) {
            ((Login) login).clearFields();
        }
        frame.getContentPane().add(login);
        FORMS.clear();
        frame.repaint();
        frame.revalidate();
    }

    public static JFrame getFrame() {
        return frame;
    }

    public static MainForm getMainForm() {
        if (mainForm == null) {
            mainForm = new MainForm();
        }
        return mainForm;
    }

    private static Login getLogin() {
        if (login == null) {
            login = new Login();
        }
        return login;
    }

    private static SignUp getSignUp() {
        if (signup == null) {
            signup = new SignUp();
        }
        return signup;
    }

    public static void showAbout() {
        ModalDialog.showModal(frame, new SimpleModalBorder(new About(), "About"),
                ModalDialog.createOption().setAnimationEnabled(false)
        );
    }
}
