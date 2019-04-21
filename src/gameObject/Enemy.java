/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameObject;


/**
 *
 * @author macle
 */
public interface Enemy {
    public double getX();
    public double getZ();
    public boolean getDead();
    public void setYRot(double d);
    public void setDead(boolean b);
}
