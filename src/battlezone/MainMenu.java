/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
            return ImageIO.read(new File("./src/sprites/MainMenu.png"));
        }
        catch(Exception e) {
            System.out.println("Failed to load menu Image");
            return null;
        }
    }
    
}
