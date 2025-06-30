package assessor;

import com.formdev.flatlaf.FlatClientProperties;
import raven.modal.Drawer;
import assessor.menu.MyDrawerBuilder;
import assessor.system.FormManager;
import com.formdev.flatlaf.extras.FlatSVGUtils;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public static final String VERSION = "1.1.0";

    public MainFrame() {
        init();
    }

    private void init() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getRootPane().putClientProperty(FlatClientProperties.FULL_WINDOW_CONTENT, true);
        Drawer.installDrawer(this, new MyDrawerBuilder());
        System.out.println("Drawer installed!");
        FormManager.install(this);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        int width = (int) (screenSize.width * 0.9);
        int height = (int) (screenSize.height * 0.9);
        
        setSize(new Dimension(width, height));
        setLocationRelativeTo(null);
        setAppIcon();
    }
    
    public void forceDrawerRefresh() {
        raven.modal.Drawer.installDrawer(this, new assessor.menu.MyDrawerBuilder());
        this.revalidate();
        this.repaint();
    }
    private void setAppIcon() {
        // Make sure the path is correct and the resource exists
        setIconImages( FlatSVGUtils.createWindowIconImages( "/assessor/icons/favicon.svg" ) );
    }
}
