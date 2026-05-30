package util;

import javax.swing.ImageIcon;
import java.awt.Image;

public class ImageLoader {
    public static ImageIcon scaleImage(String imagePath, int targetSize, boolean lockWidth) {
        ImageIcon originalIcon = new ImageIcon(imagePath);
        Image originalImage = originalIcon.getImage();

        if (originalImage == null || originalIcon.getIconWidth() == -1) {
            return originalIcon;
        }

        Image scaledImage;
        if (lockWidth) {
            scaledImage = originalImage.getScaledInstance(targetSize, -1, Image.SCALE_SMOOTH);
        } else {
            scaledImage = originalImage.getScaledInstance(-1, targetSize, Image.SCALE_SMOOTH);
        }
        return new ImageIcon(scaledImage);
    }
}