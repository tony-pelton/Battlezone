/*
 * File added by Nathan MacLeod 2019
 */
package battlezone;

import Geometry.Object3D;
import gameObject.*;
import render.RenderManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author macle
 */
public class Battlezone extends JFrame {

    private static final Logger logger = Logger.getLogger(Battlezone.class.getName());

    public static final double MAP_RADIUS = 300;
    private static final double OBSTACLE_OBSTRUCTION_RADIUS = 30;
    private static final double TANK_OBSTRUCTION_RADIUS = 10;
    private static final int NUMBER_OF_OBSTACLES = 15;
    private static Battlezone battlezone;
    private static Graphics graphics;
    private static MenuPage[] menuPages;
    private static double deltaTime = 0.0;
    private boolean running;
    private boolean w, s, i, k;
    private final double fov;
    private final ArrayList<Object3D> objects = new ArrayList<>();
    private final ArrayList<Updatable> updatable = new ArrayList<>();
    private final ArrayList<Updatable> toAddUpdatable = new ArrayList<>();
    private final ArrayList<Updatable> toRemoveUpdatable = new ArrayList<>();
    private final ArrayList<Obstacle> obstacles = new ArrayList<>();
    private final ArrayList<Tank> tanks = new ArrayList<>();
    private PlayerTank player;
    private Enemy enemy;
    private HUD hud;
    private double enemyRespawnCounter;
    private double playerRespawnCounter;
    private BackgroundImage backgroundImage;
    private int state; //0 main menu, 1 in game, 2 guide page 1, 3 guide page 2, 4 guide page 3, 5 high scores, 6 game over/ add high score screen
    private int requestedStateChange = -1;
    private double menuPanAngle;

    public Battlezone() {
        setResizable(false);
        setSize(1600, 900);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        if (false) { // full screen
            setUndecorated(true);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            gd.setFullScreenWindow(this);
        }
        setVisible(true);

        createMenus();
        state = -1;
        changeState(0);

        fov = (getWidth() / 2.0) * Math.sqrt(3);
        this.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    running = false;
                    System.exit(0);
                }

                if (state == 1) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_W:
                            w = true;
                            break;
                        case KeyEvent.VK_S:
                            s = true;
                            break;
                        case KeyEvent.VK_K:
                            k = true;
                            break;
                        case KeyEvent.VK_I:
                            i = true;
                            break;
                        case KeyEvent.VK_SPACE:
                            player.tryToFire();
                            break;
                    }
                } else {
                    if (state == 5) {
                        ((WriteHighScorePage) menuPages[5]).enterHighScore(e.getKeyCode());
                    }
                    requestedStateChange = menuPages[state].getStateChange(e.getKeyCode());
                }
            }

            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        w = false;
                        break;
                    case KeyEvent.VK_S:
                        s = false;
                        break;
                    case KeyEvent.VK_K:
                        k = false;
                        break;
                    case KeyEvent.VK_I:
                        i = false;
                        break;
                }
            }
        });
    }

    public static MenuPage[] getMenuPages() {
        return menuPages;
    }

    public static double getDeltaTime() {
        return deltaTime;
    }

    public static Battlezone getInstance() {
        return battlezone;
    }

    public static Graphics getGraphicsSurface() {
        return graphics;
    }

    public static void main(String[] args) {
        battlezone = new Battlezone();
        battlezone.run();
    }

    private void createMenus() {
        int[] dim = new int[]{getWidth(), getHeight()};
        menuPages = new MenuPage[]{new MainMenu(dim), null, new OverviewPage(dim), new ControlsPage(dim), new TipsPage(dim), new WriteHighScorePage(dim),
                new ViewHighScorePage(dim)};
    }

    private void runMenu() {
        if (state == 5) {
            ((WriteHighScorePage) menuPages[5]).update();
        }
        menuUpdate();
    }

    private void initializeMenu() {
        resetNonPlayerEnteties();
        menuPanAngle = 0;
    }

    private double[] getRandomValidLocation(double objectObstruction, double mapRatio) {
        double mapRadius = MAP_RADIUS * mapRatio;
        while (true) {
            double playerX;
            double playerZ;
            if (player != null) {
                playerX = player.getX();
                playerZ = player.getZ();
            } else {
                playerX = 0;
                playerZ = 0;
            }

            double randomX = playerX + (Math.random() * 2 * mapRadius) - mapRadius;
            double randomZ = playerZ + (Math.random() * 2 * mapRadius) - mapRadius;

            double calculation = Math.pow(randomX - playerX, 2) + Math.pow(randomZ - playerZ, 2);
            if (calculation > Math.pow(mapRadius, 2)) {
                continue;
            }


            if (calculation < Math.pow(TANK_OBSTRUCTION_RADIUS + objectObstruction, 2)) {
                continue;
            }


            boolean fail = false;
            for (Obstacle o : obstacles) {
                if (Math.pow(randomX - o.getX(), 2) + Math.pow(randomZ - o.getZ(), 2) < Math.pow(OBSTACLE_OBSTRUCTION_RADIUS + objectObstruction, 2)) {
                    fail = true;
                    break;
                }
            }
            if (fail) {
                continue;
            }

            return new double[]{randomX, randomZ};
        }
    }

    private void addTank() {
        double[] pos = getRandomValidLocation(TANK_OBSTRUCTION_RADIUS, 3.0 / 4);
        if (player == null) {
            enemy = new EnemyTank(new double[]{pos[0], 0, pos[1], 0, Math.PI * 2 * Math.random(), 0}, 10, -1);
            addUpdatable((Updatable) enemy);
            return;
        }

        enemy = new EnemyTank(new double[]{pos[0], 0, pos[1], 0, 0, 0}, 10, player.getBulletHeight());
        double[] angles = player.getAngularDistsToTank((Tank) enemy);
        double angleFromPlayer = angles[0];
        if (angles[1] < angles[0]) {
            angleFromPlayer = angles[1];
        }

        if (angleFromPlayer >= Math.PI / 6) {

            double enemyAngle = (0.5 * angleFromPlayer) + (Math.random() * 2 * angleFromPlayer);
            if ((int) (2 * Math.random()) == 0) {
                enemyAngle *= -1;
            }

            double angleToTank = -(Math.PI / 2) + ((Tank) enemy).getAngleToTank(player);
            // TODO += is postfix, so angleToTank is effectively unused
            enemy.setYRot(angleToTank += enemyAngle);
        } else {
            enemy.setYRot(Math.random() * 2 * Math.PI);
        }

        addUpdatable((Updatable) enemy);
    }

    private void addMissile() {
        double playerAngle;
        if (player == null) {
            return;
        } else {
            playerAngle = player.getYRot() + Math.PI / 2;
        }

        enemy = new Missile(new double[]{player.getX() + (Math.cos(playerAngle) * MAP_RADIUS), -80, player.getZ() + (Math.sin(playerAngle) * MAP_RADIUS), 0, playerAngle + Math.PI / 2, 0}, 5);
        addUpdatable((Updatable) enemy);
    }

    private void replaceOutOfBoundsObstacles() {
        for (int i = 0; i < obstacles.size(); i++) {
            Obstacle o = obstacles.get(i);
            if (Math.pow(o.getX() - player.getX(), 2) + Math.pow(o.getZ() - player.getZ(), 2) <= Math.pow(MAP_RADIUS, 2)) {
                continue;
            }

            do {
                double angle = player.getYRot() + (Math.random() * Math.PI);
                if (player.getTrack1() == -1 || player.getTrack2() == -1) {
                    angle += Math.PI;
                }
                double randomX = player.getX() + (Math.cos(angle) * MAP_RADIUS);
                double randomZ = player.getZ() + (Math.sin(angle) * MAP_RADIUS);
                boolean fail = false;
                for (Obstacle ob : obstacles) {
                    if (Math.pow(randomX - ob.getX(), 2) + Math.pow(randomZ - ob.getZ(), 2) < Math.pow(OBSTACLE_OBSTRUCTION_RADIUS * 2, 2)) {
                        fail = true;
                        break;
                    }
                }
                if (fail) {
                    continue;
                }
                removeUpdatable(obstacles.remove(i));
                addUpdatable(new Obstacle(new double[]{randomX, 0, randomZ, 0, Math.random() * 2 * Math.PI, 0}, 10, player.getBulletHeight()));
                break;
            } while (true);
        }
    }

    private void addObstaclesRandomLocation(int numObstacles) {
        for (int i = 0; i < numObstacles; i++) {
            double[] pos = getRandomValidLocation(OBSTACLE_OBSTRUCTION_RADIUS, 1);
            double bulletHeight = -1;
            if (player != null) {
                bulletHeight = player.getBulletHeight();
            }
            Obstacle o = new Obstacle(new double[]{pos[0], 0, pos[1], 0, Math.random() * 2 * Math.PI, 0}, 10, bulletHeight);
            addUpdatable(o);
        }
    }

    private void addEnemy() {
        if (player == null) {
            return;
        }

        double missileProbability = 0.33 * (ScoreManager.getScore() / 1000.0) * 0.35;
        if (missileProbability > 0.33) {
            missileProbability = 0.33;
        }

        if (Math.random() <= missileProbability) {
            addMissile();
        } else {
            addTank();
        }

    }

    private void removeOutOfBoundsShells() {
        for (Updatable value : updatable) {
            if (!(value instanceof TankShell)) {
                continue;
            }
            TankShell shell = (TankShell) value;
            if (Math.pow(shell.getX() - player.getX(), 2) + Math.pow(shell.getZ() - player.getZ(), 2) > Math.pow(MAP_RADIUS, 2)) {
                removeUpdatable(shell);
            }
        }
    }

    private void replaceOutOfBoundsEnemy() {

        double playerX;
        double playerZ;
        if (player != null) {
            playerX = player.getX();
            playerZ = player.getZ();
        } else {
            playerX = 0;
            playerZ = 0;
        }

        if (enemy == null || (enemy instanceof Missile && !((Missile) enemy).getInitiallyLanded()) || Math.pow(enemy.getX() - playerX, 2) + Math.pow(enemy.getZ() - playerZ, 2) <= Math.pow(MAP_RADIUS, 2)) {
            return;
        }

        removeUpdatable((Updatable) enemy);
        if (state == 1) {
            addEnemy();
        } else {
            addTank();
        }

    }

    private void addUpdatables() {
        while (!toAddUpdatable.isEmpty()) {
            Updatable u = toAddUpdatable.get(0);
            updatable.add(u);
            if (u instanceof Object3D) {
                objects.add((Object3D) u);
            }
            if (u instanceof Obstacle) {
                obstacles.add((Obstacle) u);
            }
            if (u instanceof Tank) {
                tanks.add((Tank) u);
            }
            toAddUpdatable.remove(u);
        }
    }

    private void removeUpdatables() {
        while (!toRemoveUpdatable.isEmpty()) {
            Updatable u = toRemoveUpdatable.get(0);
            updatable.remove(u);
            if (u instanceof Object3D) {
                objects.remove((Object3D) u);
            }
            if (u instanceof Obstacle) {
                obstacles.remove((Obstacle) u);
            }
            if (u instanceof Tank) {
                tanks.remove((Tank) u);
            }
            toRemoveUpdatable.remove(u);
        }
    }

    private void clearEntities() {
        obstacles.clear();
        updatable.clear();
        objects.clear();
        tanks.clear();
        toAddUpdatable.clear();
        toRemoveUpdatable.clear();
    }

    private void resetScene() {
        clearEntities();
        enemyRespawnCounter = 0;
        playerRespawnCounter = 0;

        player = new PlayerTank(new double[]{0, 0, 0, 0, 0, 0}, 10, ModelManager.getModelBulletHeight(10));
        addUpdatable(player);
        hud = new HUD(4, new int[]{getWidth() / 2, getHeight() / 6}, getHeight() * 80.0 / 900, new int[]{getWidth() / 2, (getHeight() / 2) - 9}, getHeight() * 100.0 / 900);
        addUpdatable(hud);
        addObstaclesRandomLocation(NUMBER_OF_OBSTACLES);
        addEnemy();
        backgroundImage = new BackgroundImage(new int[]{getWidth(), getHeight()});
        updatable.add(backgroundImage);
    }

    private void resetNonPlayerEnteties() {
        clearEntities();
        addObstaclesRandomLocation(NUMBER_OF_OBSTACLES);
        addTank();
        backgroundImage = new BackgroundImage(new int[]{getWidth(), getHeight()});
        updatable.add(backgroundImage);
    }

    private void gameUpdate() {
        if (w && !s) {
            player.setTrack2(1);
        } else if (s && !w) {
            player.setTrack2(-1);
        } else {
            player.setTrack2(0);
        }

        if (i && !k) {
            player.setTrack1(1);
        } else if (k && !i) {
            player.setTrack1(-1);
        } else {
            player.setTrack1(0);
        }

        for (Updatable toUpdate : updatable) {
            toUpdate.update();
        }

        addUpdatables();
        removeUpdatables();
        replaceOutOfBoundsObstacles();
        replaceOutOfBoundsEnemy();
        removeOutOfBoundsShells();
    }

    private void menuUpdate() {
        menuPanAngle += 0.1 * deltaTime;

        for (Updatable toUpdate : updatable) {
            toUpdate.update();
        }

        addUpdatables();
        removeUpdatables();
        replaceOutOfBoundsEnemy();
    }

    /*
     *
     * render
     *
     */
    private void gameRender() {
        RenderManager.renderObjects(objects, new double[]{player.getX(), player.getBulletHeight() - 0.125, player.getZ() - 0.001}, new double[]{player.getXRot(), player.getYRot()}, fov, new int[]{getWidth(), getHeight()});
        backgroundImage.drawBackground(player.getYRot());
        hud.draw(new int[]{getWidth(), getHeight()});
    }

    private void menuRender() {
        RenderManager.renderObjects(objects, new double[]{0, (double) -16, 0}, new double[]{0, menuPanAngle}, fov, new int[]{getWidth(), getHeight()});
        backgroundImage.drawBackground(menuPanAngle);
        if (state == 5) {
            ((WriteHighScorePage) menuPages[5]).draw(ScoreManager.getScore());
        }
        if (state == 6) {
            menuPages[6].draw();
        }
        menuPages[state].draw();

    }

    private void render() {

        BufferedImage doubleBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        graphics = doubleBuffer.getGraphics();
        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, getWidth(), getHeight());

        if (state == 1) {
            gameRender();
        } else {
            menuRender();
        }

        getGraphics().drawImage(doubleBuffer, 0, 0, null);

    }

    private void changeState(int newState) {
        logger.info(String.format("current=%d new=%d", state, newState));
        if (newState == 1) {
            initializeGame();
        } else if (state == 1 || state == -1) {
            initializeMenu();
        }
        state = newState;
    }

    private void runState() {
        if (state == 1) {
            runGame();
        } else {
            runMenu();
        }
    }

    private void gameLogic() {
        if (playerRespawnCounter > 0) {
            playerRespawnCounter -= deltaTime;
            if (playerRespawnCounter <= 0) {
                ScoreManager.decrementLives();
                if (ScoreManager.getLives() <= 0) {
                    if (ScoreManager.getScorePlacement() != -1) {
                        changeState(5);
                    } else {
                        changeState(0);
                    }
                } else {
                    resetScene();
                }
            }
        } else if (player.getDead()) {
            playerRespawnCounter = 3;
        }

        if (enemyRespawnCounter > 0) {
            enemyRespawnCounter -= deltaTime;
            if (enemyRespawnCounter <= 0) {
                addEnemy();
            }
        } else if (enemy.getDead()) {
            ScoreManager.updateScore(enemy);
            enemyRespawnCounter = 2.5;
        }

    }

    private void runGame() {
        gameUpdate();
        gameLogic();
    }

    private void initializeGame() {
        ScoreManager.initialize();
        resetScene();
    }

    public Enemy getEnemy() {
        return enemy;
    }

    public void addUpdatable(Updatable u) {
        toAddUpdatable.add(u);
    }

    public void removeUpdatable(Updatable u) {
        toRemoveUpdatable.add(u);
    }

    public ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }

    public ArrayList<Tank> getTanks() {
        return tanks;
    }

    public PlayerTank getPlayer() {
        return player;
    }

    public void run() {
        logger.info("run()");
        long previousTime = System.currentTimeMillis();
        long currentTime;
        running = true;
        while (running) {
            currentTime = System.currentTimeMillis();
            // 1000's of a second
            deltaTime = 0.001 * (currentTime - previousTime);
            previousTime = currentTime;
            if (requestedStateChange != -1) {
                changeState(requestedStateChange);
                requestedStateChange = -1;
            }
            runState();
            render();
        }
    }

}
