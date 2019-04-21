/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameObject;
import Geometry.*;
import java.util.ArrayList;
/**
 *
 * @author macle
 */
public class Model {
//Model is just a way to pass the information from the model manager to the object
    public Hitbox collisionBox;
    public Hitbox bulletBox;
    public Point[] points;
    public int[][] lines;
    public int[][] nonCosmeticLines;
    
    public Model(Hitbox collisionBox, Hitbox bulletBox, Point[] points, int[][] lines, int[][] nonCosmeticLines) {
        this.collisionBox = collisionBox;
        this.bulletBox = bulletBox;
        this.points = points;
        this.lines = lines;
        this.nonCosmeticLines = nonCosmeticLines;
    }
}
