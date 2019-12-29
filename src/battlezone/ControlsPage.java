package battlezone;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 *
 * @author macle
 */
public class ControlsPage extends ImagePage{
    public ControlsPage(int[] screenDimensions) {
        super(screenDimensions);
    }

    public int getStateChange(int keyPressed) {
        switch(keyPressed) {
            case KeyEvent.VK_SPACE:
                return 0;
            case KeyEvent.VK_W:
                return 4;
            case KeyEvent.VK_S:
                return 2;
        }
        return -1;
    }

    protected BufferedImage setImage() {
        try {
            return ImageIO.read(new File("./src/sprites/Controls.png"));
        }
        catch(Exception e) {
            System.out.println("Failed to load menu Image");
            return null;
        }
    }
}
