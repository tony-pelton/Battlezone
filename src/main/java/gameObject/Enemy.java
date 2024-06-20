/*
 * File added by Nathan MacLeod 2019
 */
package gameObject;


/**
 * @author macle
 */
public interface Enemy extends Updatable {
    double getX();

    double getZ();

    boolean getDead();

    void setDead(boolean b);

    void setYRot(double d);
}
