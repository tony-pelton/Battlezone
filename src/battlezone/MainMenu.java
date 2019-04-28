/*
 * File added by Nathan MacLeod 2019
 */
package battlezone;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import javax.imageio.ImageIO;
import java.awt.event.KeyEvent;

/**
 *
 * @author macle
 */
public class MainMenu extends ImagePage{
    
    public MainMenu(int[] screenDimensions) {
        super(screenDimensions);
    }
    
    public int getStateChange(int keyPressed) {
        switch(keyPressed) {
            case KeyEvent.VK_SPACE:
                return 1;
            case KeyEvent.VK_W:
                return 2;
            case KeyEvent.VK_S:
                return 6;
        }
        return -1;
    } 
    
    protected BufferedImage setImage() {
        try {
            return ImageIO.read(Class.class.getResourceAsStream("/sprites/MainMenu.png"));
        }
        catch(Exception e) {
            System.out.println("Failed to load menu Image");
            return null;
        }
    }
    
}
