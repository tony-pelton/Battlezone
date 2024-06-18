/*
 * File added by Nathan MacLeod 2019
 */
package gameObject;

/**
 * @author macle
 */
public class PlayerTank extends Tank {
    public PlayerTank(double[] position, double scale, double bulletHeight) {
        super(position, scale, bulletHeight + 0.15, 10, 0.14);
    }
}
