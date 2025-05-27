package assessor.auth;

import raven.modal.component.Modal;
import java.awt.*;

public class SignupModal extends Modal {
    public SignupModal() {
        setLayout(new BorderLayout());
        add(new SignUp(), BorderLayout.CENTER);
    }
}