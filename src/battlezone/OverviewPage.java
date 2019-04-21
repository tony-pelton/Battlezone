/*
 * File added by Nathan MacLeod 2019
 */
package battlezone;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 *
 * @author macle
 */
public class OverviewPage extends ImagePage{
    public OverviewPage(int[] screenDimensions) {
        super(screenDimensions);
    }
    
    public int getStateChange(int keyPressed) {
        switch(keyPressed) {
            case KeyEvent.VK_SPACE:
                return 0;
            case KeyEvent.VK_W:
                return 3;
        }
        return -1;
    } 
    
    protected BufferedImage setImage() {
        try {
            return ImageIO.read(new File("./src/sprites/Overview.png"));
        }
        catch(Exception e) {
            System.out.println("Failed to load menu Image");
            return null;
        }
    }
}
