/*
* File added by Nathan MacLeod 2019
*/

package gameObject;

import Geometry.Hitbox;
import Geometry.Point;
import battlezone.Battlezone;
import java.util.ArrayList;







public class Tank extends MovingObject {
  private int track1;
  private int track2;
  private double trackTurnValue;
  private double trackMoveValue;
  private Point shellPoint;
  private double shellSize = 0.3;
  private double shootCooldown = 2.5;
  private double shootCooldownCounter = 0;
  private Hitbox forwardCollisionLine;
  private Hitbox reverseCollisionLine;
  private boolean dead = false;
  
  public Tank(double[] position, double scale, double bulletHeight, double speed, double trackVal) {
    super(position, ModelManager.getTankModel(scale, bulletHeight), 10.0D, new double[] { position[3], position[4] });
    trackTurnValue = trackVal;
    trackMoveValue = speed;
    shellPoint = getPoints()[22];
    getCollisionLines();
    updateHitboxes();
  }
  
  public void updateHitboxes() {
    super.updateHitboxes();
    if (forwardCollisionLine != null) {
      forwardCollisionLine.updateWorldPos();
      reverseCollisionLine.updateWorldPos();
    }
  }
  
  public int getTrack1() {
      return track1;
  }

  public int getTrack2() {
      return track2;
  }
  
  public boolean getDead() {
    return dead;
  }
  
  public void setDead(boolean b) {
    dead = b;
  }
  
  private void getCollisionLines() {
    Point[] collisionBox = getCollisionBox().getCopyModelPoints();
    Point[] forwardLine = { collisionBox[0], new Point(collisionBox[0].getX(), collisionBox[0].getY(), 0.0D), new Point(collisionBox[3].getX(), collisionBox[0].getY(), 0.0D), collisionBox[3] };
    Point[] reverseLine = { collisionBox[1], forwardLine[1], forwardLine[2], collisionBox[2] };
    
    forwardCollisionLine = new Hitbox(this, forwardLine);
    reverseCollisionLine = new Hitbox(this, reverseLine);
  }
  
  public double getMinCrossection() {
    return getBulletBox().getDomain()[1] - getBulletBox().getDomain()[0];
  }
  
  public double getTrackTurnValue() {
    return trackTurnValue;
  }
  
  public double getTrackMoveValue() {
    return trackMoveValue;
  }
  
  public double getBulletHeight() {
    return shellPoint.getY() + shellSize / 2.0D;
  }
  
  public void setTrack1(int i) {
    track1 = i;
  }
  

  public void setTrack2(int i) { 
    track2 = i; 
  }
  
  protected void bumpedObstacle(Obstacle o) {}
  
  public void tryToFire() {
    if (!dead && shootCooldownCounter <= 0.0D) {
      shoot();
      shootCooldownCounter = shootCooldown;
    }
  }
  
  public boolean getOnFireCooldown() {
    return shootCooldownCounter > 0.0D;
  }
  
  private void shoot() {
    double[] position = { getX() + shellPoint.getZ() * Math.cos(getYRot() + 1.5707963267948966D), shellPoint.getY(), getZ() + shellPoint.getZ() * Math.sin(getYRot() + 1.5707963267948966D), getXRot(), getYRot(), getZRot() };
    TankShell shell = new TankShell(position, ModelManager.getShellModel(shellSize), this instanceof PlayerTank);
    Battlezone.getInstance().addUpdatable(shell);
  }
  
  public void update(double timePassed) {
    super.update(timePassed);
    if (shootCooldownCounter > 0.0D)
      shootCooldownCounter -= timePassed;
  }
  
  public double getAngleToTank(Tank t) {
        double relativeX = t.getX() - getX();
        double relativeZ = (t.getZ() - getZ());
        double angleToTank = Math.atan(relativeZ/relativeX);
        if(relativeX < 0 && relativeZ >= 0) {
            angleToTank += Math.PI;
        }
        if(relativeX > 0 && relativeZ <= 0) {
            angleToTank += Math.PI * 2;
        }
        if(relativeX <= 0 && relativeZ <= 0) {
            angleToTank += Math.PI;
        }
        return angleToTank;
  }
  
  public double[] getAngularDistsToTank(Tank t) {
      double angle = getAngleToTank(t);
      double clockwiseDist = ((getYRot() + (Math.PI/2)) % (Math.PI * 2)) - angle;
      if(clockwiseDist < 0)
          clockwiseDist += Math.PI * 2;
      return new double[] {clockwiseDist, Math.PI * 2 - clockwiseDist}; 
  }
  
  protected void move(double timePassed) {
    double oneTrackBonus = 1.0D;
    if ((track1 == 0) || (track2 == 0)) {
      oneTrackBonus = 1.5D;
    }
    Battlezone battlezone = Battlezone.getInstance();
    ArrayList<Obstacle> obstacles = battlezone.getObstacles();
    double moveValue = trackMoveValue * oneTrackBonus * (track1 + track2);
    boolean forwardCollision = false;
    boolean reverseCollision = false;
    for (Obstacle ob : obstacles) {
      if (ob.collisionBoxCollision(forwardCollisionLine)) {
        forwardCollision = true;
        bumpedObstacle(ob);
        break;
      }
      if (ob.collisionBoxCollision(reverseCollisionLine)) {
        reverseCollision = true;
        bumpedObstacle(ob);
        break;
      }
    }
    for (Tank t : battlezone.getTanks()) {
      if (!t.equals(this)) {
        if (t.collisionBoxCollision(forwardCollisionLine)) {
          forwardCollision = true;
          break;
        }
        if (t.collisionBoxCollision(reverseCollisionLine)) {
          reverseCollision = true;
          break;
        }
      }
    }
    if (((moveValue > 0.0D) && (!forwardCollision)) || ((moveValue < 0.0D) && (!reverseCollision))) {
      setVelocity(moveValue);
      super.move(timePassed);
    }
    rotate(new double[] { 0.0D, trackTurnValue * (track1 - track2), 0.0D }, timePassed);
    setDirectionToAngle();
    updateHitboxes();
  }
}
