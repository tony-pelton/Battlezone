/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameObject;

import Geometry.Point;

/**
 *
 * @author macle
 */
public class Obstacle extends CollideableObject {
    private static double obstacleHeight = 0;
    public Obstacle(Point[] points, int[][] lines, double[] position, Point[] bulletHitBox, Point[] collisionBox) {
        super(points, lines, position, bulletHitBox, collisionBox);
    }
    
    public Obstacle(double[] position, double scale, double bulletHeight) {
        super(position, ModelManager.getRandomObstacleModel(scale, bulletHeight));
        obstacleHeight = ModelManager.getObstacleHeight(scale);
        
    }
    
    public static double getObstacleHeight() {
        return obstacleHeight;
    }
    
}
