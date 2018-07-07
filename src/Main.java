import java.awt.*;

public class Main {
    public static void main(String[] args) {
        fmMain dialog = new fmMain();
        dialog.setAlwaysOnTop(true);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
