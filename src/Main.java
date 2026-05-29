import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        // You can add startup logic here later, like checking if the database is online!
        System.out.println("Starting the Template Store Application...");

        // Safely launch the GUI
        SwingUtilities.invokeLater(() -> {
            CustomerApp loginWindow = new CustomerApp();
            loginWindow.setVisible(true);
        });
    }
}