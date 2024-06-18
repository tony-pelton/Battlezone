/*
 * File added by Nathan MacLeod 2019
 */
package gameObject;

import Geometry.Point;

/**
 * @author macle
 */
public class Obstacle extends CollideableObject {
    private static double obstacleHeight = 0;

    public Obstacle(double[] position, double scale, double bulletHeight) {
        super(position, ModelManager.getRandomObstacleModel(scale, bulletHeight));
        obstacleHeight = ModelManager.getObstacleHeight(scale);

    }

    public static double getObstacleHeight() {
        return obstacleHeight;
    }

}
