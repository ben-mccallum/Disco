package uk.ac.strath;

        import javax.swing.*;
        import java.awt.*;
        import java.io.ByteArrayInputStream;
        import java.io.IOException;
        import javax.imageio.ImageIO;
        import java.awt.image.BufferedImage;
public class Image extends JFrame {

    public Image(byte[] imageData) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));

            JLabel label = new JLabel(new ImageIcon(img));

            getContentPane().add(label, BorderLayout.CENTER);

            setTitle("Image Display");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}