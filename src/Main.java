import view.auth.AuthFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AuthFrame loginWindow = new AuthFrame();
            loginWindow.setVisible(true);
        });
    }
}