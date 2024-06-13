/*
 * File added by Nathan MacLeod 2019
 */
package gameObject;


/**
 *
 * @author macle
 */
public interface Enemy {
    double getX();
    double getZ();
    boolean getDead();
    void setYRot(double d);
    void setDead(boolean b);
}
