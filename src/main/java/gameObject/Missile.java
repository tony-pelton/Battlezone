/*
 * File added by Nathan MacLeod 2019
 */
package gameObject;


import battlezone.Battlezone;

import java.util.ArrayList;

/**
 * @author Nathan
 */
public class Missile extends MovingObject implements Enemy {
    private final double turnSpeed = 0.1;
    private final double numWaypoints = 4;
    private final double fallSpeed = 60;
    private final double verticleTraverseSpeed = 80;
    private final double maxDivertDistance = 60;
    private ArrayList<double[]> waypoints;
    private boolean initiallyLanded = false;
    private boolean dead = false;

    public Missile(double[] position, double scale) {
        super(position, ModelManager.getMissileModel(scale), 80, new double[]{0, position[4]});
    }

    private void initializeWaypoints() {
        waypoints = new ArrayList<>();
        double previousY = 0;
        Battlezone battlezone = Battlezone.getInstance();
        for (int i = 0; i < numWaypoints; i++) {
            double x = Battlezone.MAP_RADIUS * (i + 1) / (numWaypoints + 1);
            double y = ((Math.random() * 2 * maxDivertDistance) - maxDivertDistance);
            if (i != 0 && ((y < 0 && previousY < 0) || (y > 0 && previousY > 0))) {
                y *= -1;
            }
            previousY = y;
            PlayerTank player = battlezone.getPlayer();
            double playerAngle = player.getYRot() + Math.PI / 2;

            double[] point = new double[]{player.getX() + (Math.cos(playerAngle) * x) - (Math.sin(playerAngle) * y), player.getZ() + (Math.sin(playerAngle) * x) + (Math.cos(playerAngle) * y)};
            waypoints.add(0, point);
        }
        if (!waypoints.isEmpty()) {
            setDirectionToNextWaypoint();
        }
    }

    private double getAngleToPoint(double[] point) {
        double relativeX = point[0] - getX();
        double relativeZ = (point[1] - getZ());
        double angle = Math.atan(relativeZ / relativeX);
        if (relativeX < 0 && relativeZ >= 0) {
            angle += Math.PI;
        }
        if (relativeX > 0 && relativeZ <= 0) {
            angle += Math.PI * 2;
        }
        if (relativeX <= 0 && relativeZ <= 0) {
            angle += Math.PI;
        }
        return angle;
    }

    private void setDirectionToNextWaypoint() {
        double angle = getAngleToPoint(waypoints.get(0));
        setDirection(new double[]{0, angle});
        setYRot(angle - Math.PI / 2);
    }

    private double getSquaredDistToNextWaypoint() {
        double[] p = waypoints.get(0);
        return Math.pow(p[0] - getX(), 2) + Math.pow(p[1] - getZ(), 2);
    }

    private void checkForWaypointAchieved(double timePassed) {
        if (getSquaredDistToNextWaypoint() <= Math.pow(getVelocity() * timePassed, 2)) {
            waypoints.remove(0);
            if (!waypoints.isEmpty()) {
                setDirectionToNextWaypoint();
            } else {
                PlayerTank player = Battlezone.getInstance().getPlayer();
                double angle = getAngleToPoint(new double[]{player.getX(), player.getZ()});
                setDirection(new double[]{0, angle});
                setYRot(angle - Math.PI / 2);
            }

        }
    }

    private void trackPlayer(double timePassed) {
        double[] angles = getAngularDistsToTank(Battlezone.getInstance().getPlayer());
        if (angles[0] > angles[1]) {
            setDirection(new double[]{0, getDirection()[1] + (turnSpeed * timePassed)});
        } else {
            setDirection(new double[]{0, getDirection()[1] - (turnSpeed * timePassed)});
        }
        setYRot(getDirection()[1] - Math.PI / 2);
    }

    public double[] getAngularDistsToTank(Tank t) {
        double angle = getAngleToPoint(new double[]{t.getX(), t.getZ()});
        double clockwiseDist = ((getYRot() + (Math.PI / 2)) % (Math.PI * 2)) - angle;
        if (clockwiseDist < 0) {
            clockwiseDist += Math.PI * 2;
        }
        return new double[]{clockwiseDist, Math.PI * 2 - clockwiseDist};
    }

    private void missileLogic(double timePassed) {
        if (waypoints == null) {
            initializeWaypoints();
        }
        if (!waypoints.isEmpty()) {
            checkForWaypointAchieved(timePassed);
        }
        if (waypoints.isEmpty()) {
            trackPlayer(timePassed);
        }

    }

    public boolean getDead() {
        return dead;
    }

    public void setDead(boolean b) {
        dead = b;
    }

    public boolean getInitiallyLanded() {
        return initiallyLanded;
    }

    public void update() {
        super.update();
        missileLogic(Battlezone.getDeltaTime());
    }

    public void move() {
        Battlezone battlezone = Battlezone.getInstance();
        ArrayList<Obstacle> obstacles = battlezone.getObstacles();
        PlayerTank player = battlezone.getPlayer();
        super.move();

        if (!initiallyLanded) {
            setY(getY() + fallSpeed * Battlezone.getDeltaTime());
            if (getY() > 0) {
                setY(0);
                initiallyLanded = true;
            }
            return;
        }

        boolean obstacleCollision = false;
        for (Obstacle ob : obstacles) {
            if (ob.bulletBoxCollision(getCollisionBox())) {
                obstacleCollision = true;
                break;
            }
        }
        if (obstacleCollision) {
            setY(getY() - (Battlezone.getDeltaTime() * verticleTraverseSpeed));
            if (getY() < Obstacle.getObstacleHeight()) {
                setY(Obstacle.getObstacleHeight());
            }
        } else {
            setY(getY() + (Battlezone.getDeltaTime() * verticleTraverseSpeed));
            if (getY() > 0) {
                setY(0);
            }
        }


        if (player != null && player.collisionBoxCollision(getCollisionBox())) {
            FreefallingDebris.explode(getPosition());
            player.setDead(true);
            battlezone.removeUpdatable(player);

            setDead(true);
            battlezone.removeUpdatable(this);
        }
    }
}
