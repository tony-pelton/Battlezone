/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameObject;

import java.util.ArrayList;
import Geometry.Object3D;
import battlezone.Battlezone;
/**
 *
 * @author macle
 */
public interface Updatable {
    
    public void update(double time, Battlezone battlezone);
    
}
