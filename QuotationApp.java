import javax.swing.*;
import java.awt.*;

/**
 * Standalone launcher: runs the QuotationApplet inside a normal window.
 * Useful because modern browsers no longer run applets.
 *
 * Usage: java QuotationApp [serverUrl]
 */
@SuppressWarnings({"deprecation", "removal"})
public class QuotationApp {
    public static void main(String[] args) throws Exception {
        final String server = args.length > 0 ? args[0] : "http://localhost:8080";

        QuotationApplet applet = new QuotationApplet() {
            @Override
            public String getParameter(String name) {
                return "server".equals(name) ? server : null;
            }
        };

        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame("Smart Quotation Generator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(applet, BorderLayout.CENTER);
            frame.setSize(760, 560);
            frame.setLocationRelativeTo(null);
            applet.init();
            applet.start();
            frame.setVisible(true);
        });
    }
}
