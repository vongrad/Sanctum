/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gameStates;

import Listeners.CreepPathUpdatedListener;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import controls.BaseControll;
import controls.BulletControl;
import controls.CreepControl;
import controls.IBase;
import controls.IGeometryDisposed;
import controls.TowerControl;
import de.lessvoid.nifty.effects.impl.Gradient;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.css.RGBColor;
import pathfinder.Constants;
import pathfinder.Graph;
import pathfinder.PathFinder;
import utils.GridCalculator;
import pathfinder.Vertex;
import sun.misc.Queue;
import utils.ShapeBuilder;

/**
 *
 * @author adamv_000
 */
public class GameState extends AbstractAppState {

    private ShapeBuilder shapeBuilder;
    private SimpleApplication app;
    private Camera cam;
    private AssetManager assetManager;
    private AppSettings appSettings;
    private Ray ray;
    private Node rootNode;
    private Node creepNode;
    private Node towerNode;
    private Node pathNode;
    private Node obstacleNode;
    private Node bulletNode;
    private Node baseNode;
    private Node explosionNode;
    private AudioNode notEnoughGold;
    private List<CreepPathUpdatedListener> creepListeners;
    private PathFinder pathFinder;
    private static final int xBlock = 80;
    private static final int zBlock = 50;
    private static final float blockSize = 1.0f;
    private static Vector3f spawnPoint;
    private static Vector3f basePoint;
    private List<Vertex> creepPath;
    private Graph graphBuilder;
    private static final Trigger TRIGGER_BUILD = new MouseButtonTrigger(MouseInput.BUTTON_LEFT);
    private static final Trigger TRIGGER_ACTION_OBSTACLE = new KeyTrigger(KeyInput.KEY_1);
    private static final Trigger TRIGGER_ACTION_TOWER = new KeyTrigger(KeyInput.KEY_2);
    private static final Trigger TRIGGER_ACTION_TOWER2 = new KeyTrigger(KeyInput.KEY_3);
    private static final Trigger TRIGGER_ACTION_TOWER3 = new KeyTrigger(KeyInput.KEY_4);
    private static final Trigger TRIGGER_START_WAVE = new KeyTrigger(KeyInput.KEY_F1);
    private static final String MAPPING_BUILD = "Build";
    private static final String MAPPING_ACTION_OBSTACLE = "Action_Obstacle";
    private static final String MAPPING_ACTION_TOWER = "Action_Tower";
    private static final String MAPPING_ACTION_TOWER2 = "Action_Tower2";
    private static final String MAPPING_ACTION_TOWER3 = "Action_Tower3";
    private static final String MAPPING_START_WAVE = "Start_Wave";
    private int action;
    private boolean first = true;
    private int waveIndex;
    private int baseHealth;
    private int waveCount;
    private boolean gameStarted;
    private boolean waveFinished;
    private ParticleEmitter explosion;
    private static int gold = 100;
    //For counting down the time between the waves - doing this way for performance increase
    private int timeCounter;
    private float timePassed;
    private final int priceObstacle = 5;
    private final int priceTower1 = 15;
    private final int priceTower2 = 25;
    private final int priceTower3 = 100;

//    private Timer timer;
//    private TimerTask timerTask = new TimerTask() {
//
//        @Override
//        public void run() {
//            waveCounter--;
//            if (waveCounter > 0){
//                showMessage("Time to next wave: " + waveCount);
//            }
//            else{
//                waveCounter = 30;
//                generateCreepWave();
//                timerTask.cancel();
//            } 
//        }
//    };
    public GameState(AppSettings appSettings) {
        this.appSettings = appSettings;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        creepListeners = new ArrayList<CreepPathUpdatedListener>();

        pathFinder = new PathFinder();
        graphBuilder = new Graph(xBlock * 2, xBlock * 2);

        action = Constants.ACTION_OBSTACLE;
        waveIndex = 1;
        baseHealth = 500;
        waveCount = 5;
        waveFinished = true;
        gameStarted = false;

        timeCounter = 0;
        timePassed = 0;

        this.app = (SimpleApplication) app;
        this.cam = this.app.getCamera();
        this.rootNode = this.app.getRootNode();
        this.towerNode = new Node();
        this.bulletNode = new Node();
        this.obstacleNode = new Node();
        this.creepNode = new Node();
        this.pathNode = new Node();
        this.baseNode = new Node();

        this.assetManager = this.app.getAssetManager();
        ray = new Ray();
        rootNode.attachChild(creepNode);
        rootNode.attachChild(towerNode);
        rootNode.attachChild(obstacleNode);
        rootNode.attachChild(pathNode);
        rootNode.attachChild(bulletNode);
        rootNode.attachChild(baseNode);


        //Explosion Textures
        explosion = new ParticleEmitter(
                "My explosion effect", ParticleMesh.Type.Triangle, 30);
        explosion.setStartSize(2f);
        explosion.setEndSize(5f);
        Material flash_mat = new Material(
                assetManager, "Common/MatDefs/Misc/Particle.j3md");
        flash_mat.setTexture("Texture",
                assetManager.loadTexture("Effects/Explosion/flash.png"));
        explosion.setMaterial(flash_mat);
        explosion.setImagesX(2); // columns
        explosion.setImagesY(2); // rows
        explosion.setSelectRandomImage(true);
        explosion.setStartColor(new ColorRGBA(1f, 0f, 0f, 1f));   // red
        explosion.setEndColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
        rootNode.attachChild(explosion);
        //
        shapeBuilder = new ShapeBuilder(assetManager);

        gold = 100;
        initLight();
        initConstants();
        initTriggers();
        initPlatform();
        initBase();
        initSound();
        initHUD();
        //generateCreep();
    }

    private void initHUD() {
        addHUDText("gold", "Gold: " + String.valueOf(gold), 1, new Vector3f(appSettings.getWidth() / 4, appSettings.getHeight() * 0.98f, 0f), ColorRGBA.Yellow);
        addHUDText("baseHealth", "Base Health: " + String.valueOf(baseHealth), 1, new Vector3f(appSettings.getWidth() / 4 * 3, appSettings.getHeight() * 0.98f, 0f), ColorRGBA.Blue);
        addHUDText("nextWaveTimer", "", 4, new Vector3f(appSettings.getWidth() / 3, appSettings.getHeight() * 0.66f, 0), ColorRGBA.White);
        addHUDText("waveOver", "", 4, new Vector3f(appSettings.getWidth() / 3, appSettings.getHeight() * 0.76f, 0), ColorRGBA.White);
        addHUDText("", "Tower Prices:", 1, new Vector3f(10, appSettings.getHeight() * 0.75f, 0), ColorRGBA.White);
        addHUDText("obstacle", "Obstacle - 5g", 1, new Vector3f(10, appSettings.getHeight() * 0.70f, 0), ColorRGBA.White);
        addHUDText("t1", "Tower 1 - 15g", 1, new Vector3f(10, appSettings.getHeight() * 0.65f, 0), ColorRGBA.White);
        addHUDText("t2", "Tower 2 - 25g", 1, new Vector3f(10, appSettings.getHeight() * 0.60f, 0), ColorRGBA.White);
        addHUDText("t3", "Tower 3 - 100g", 1, new Vector3f(10, appSettings.getHeight() * 0.55f, 0), ColorRGBA.White);

    }

    private void initTriggers() {
        app.getInputManager().addMapping(MAPPING_BUILD, TRIGGER_BUILD);
        app.getInputManager().addMapping(MAPPING_ACTION_OBSTACLE, TRIGGER_ACTION_OBSTACLE);
        app.getInputManager().addMapping(MAPPING_ACTION_TOWER, TRIGGER_ACTION_TOWER);
        app.getInputManager().addMapping(MAPPING_ACTION_TOWER2, TRIGGER_ACTION_TOWER2);
        app.getInputManager().addMapping(MAPPING_ACTION_TOWER3, TRIGGER_ACTION_TOWER3);
        app.getInputManager().addMapping(MAPPING_START_WAVE, TRIGGER_START_WAVE);
        app.getInputManager().addListener(actionListener, new String[]{MAPPING_BUILD, MAPPING_ACTION_OBSTACLE, MAPPING_ACTION_TOWER, MAPPING_START_WAVE, MAPPING_ACTION_TOWER2, MAPPING_ACTION_TOWER3});
    }
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {

            if (name.equals(MAPPING_BUILD) && !isPressed && waveFinished) {
                ray.setOrigin(cam.getLocation());
                ray.setDirection(cam.getDirection());

                CollisionResults collisionResults = new CollisionResults();
                rootNode.collideWith(ray, collisionResults);

                if (collisionResults.size() > 0) {
                    int[] grid = GridCalculator.calculateGrid(collisionResults.getClosestCollision().getContactPoint());
                    if (action == 1 && gridAvailable(grid)) {
                        if (gold >= priceObstacle) {
                            takeGold(priceObstacle);
                            obstacleNode.attachChild(shapeBuilder.generateBox("Obscale", 0.5f, 10f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Rock/bricks.jpg", ColorRGBA.Yellow, GridCalculator.calculateCenter(grid[0], grid[1])));
                        } else {
                            notEnoughGold.playInstance();
                        }
                    } else if (action == 2 && gridTowerAvailable(grid, 1, 1)) {
                        if (gold >= priceTower1) {
                            takeGold(priceTower1);
                            generateTower(grid);
                        } else {
                            notEnoughGold.playInstance();
                        }
                    } else if (action == 3 && gridTowerAvailable(grid, 1, 2)) {
                        if (gold >= priceTower2) {
                            takeGold(priceTower2);
                            generateTower2(grid);
                        } else {
                            notEnoughGold.playInstance();
                        }

                    } else if (action == 4 && gridTowerAvailable(grid, 2, 2)) {
                        if (gold >= priceTower3) {
                            takeGold(priceTower3);
                            generateTower3(grid);
                        } else {
                            notEnoughGold.playInstance();
                        }
                    }



                }
            }

            if (name.equals(MAPPING_BUILD)
                    && !isPressed && !waveFinished) {
                ray.setOrigin(cam.getLocation());
                ray.setDirection(cam.getDirection());
                CollisionResults colResults = new CollisionResults();
                creepNode.collideWith(ray, colResults);

                if (colResults.size() > 0) {
                    Geometry target = colResults.getClosestCollision().getGeometry();

                    Geometry bullet = shapeBuilder.generateBullet("Bullet", 16, 16, 0.5f, null, ColorRGBA.Red, cam.getLocation());
                    bullet.addControl(new BulletControl(target, 0.5f, 10, explosion));
                    bulletNode.attachChild(bullet);
                }
            }

            if (name.equals(MAPPING_ACTION_OBSTACLE)) {
                action = 1;
                ((BitmapText) app.getGuiNode().getChild("obstacle")).setColor(ColorRGBA.Blue);
                ((BitmapText) app.getGuiNode().getChild("t1")).setColor(ColorRGBA.White);
                ((BitmapText) app.getGuiNode().getChild("t2")).setColor(ColorRGBA.White);
                ((BitmapText) app.getGuiNode().getChild("t3")).setColor(ColorRGBA.White);
            }

            if (name.equals(MAPPING_ACTION_TOWER)) {
                action = 2;
                ((BitmapText) app.getGuiNode().getChild("obstacle")).setColor(ColorRGBA.White);
                ((BitmapText) app.getGuiNode().getChild("t1")).setColor(ColorRGBA.Blue);
                ((BitmapText) app.getGuiNode().getChild("t2")).setColor(ColorRGBA.White);
                ((BitmapText) app.getGuiNode().getChild("t3")).setColor(ColorRGBA.White);
            }

            if (name.equals(MAPPING_ACTION_TOWER2)) {
                ((BitmapText) app.getGuiNode().getChild("obstacle")).setColor(ColorRGBA.White);
                ((BitmapText) app.getGuiNode().getChild("t1")).setColor(ColorRGBA.White);
                ((BitmapText) app.getGuiNode().getChild("t2")).setColor(ColorRGBA.Blue);
                ((BitmapText) app.getGuiNode().getChild("t3")).setColor(ColorRGBA.White);
                action = 3;
            }
            if (name.equals(MAPPING_ACTION_TOWER3)) {
                ((BitmapText) app.getGuiNode().getChild("obstacle")).setColor(ColorRGBA.White);
                ((BitmapText) app.getGuiNode().getChild("t1")).setColor(ColorRGBA.White);
                ((BitmapText) app.getGuiNode().getChild("t2")).setColor(ColorRGBA.White);
                ((BitmapText) app.getGuiNode().getChild("t3")).setColor(ColorRGBA.Blue);
                action = 4;
            }

            if (name.equals(MAPPING_START_WAVE)
                    && !isPressed && waveFinished) {
                creepNode.getChildren().clear();
                creepListeners.clear();
                calculateCreepPath();
                waveFinished = false;
                //Might generate 2 waves because of timer task execution that is not stopped immediatelly -> needs to be fixed
                generateCreepWave();
                gameStarted = true;
                timeCounter = 0;
                timePassed = 0.0f;
//                app.getGuiNode().getChild("startWave").removeFromParent();
            }
        }
    };

    private void initLight() {
        rootNode.addLight(new AmbientLight());
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(0.0f, -1f, 0f));
        rootNode.addLight(sun);
    }

    private void initConstants() {
        spawnPoint = new Vector3f(-xBlock * blockSize, 1.0f, 0.0f);
        basePoint = new Vector3f(xBlock * blockSize, 1.0f, 0.0f);
    }

    private void initPlatform() {
        rootNode.attachChild(shapeBuilder.generateBox("Platform", xBlock * blockSize, 1f, zBlock * blockSize, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Grass/grass.jpg", ColorRGBA.White, Vector3f.ZERO));
    }

    private void initBase() {
        Geometry base = shapeBuilder.generateBox("Base", 3 * blockSize, 3 * blockSize, 5 * blockSize, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Wood/wood.jpg", ColorRGBA.Brown, new Vector3f(basePoint.add(new Vector3f(-blockSize * 3f, 3.0f, 0.0f))));
        base.addControl(new BaseControll(baseHealth));
        baseNode.attachChild(base);
    }

    private void initSound() {
        notEnoughGold = new AudioNode(assetManager, "Sounds/Effects/Not_Enough_Gold.wav", false);
        notEnoughGold.setPositional(false);
        notEnoughGold.setLooping(false);
        notEnoughGold.setVolume(2);
        rootNode.attachChild(notEnoughGold);
    }

    private boolean gridTowerAvailable(int[] gridPos, int rangeX, int rangeY) {

        boolean available = true;

        for (int i = gridPos[0] - rangeX; i <= gridPos[0] + rangeX; i++) {
            for (int j = gridPos[1] - rangeY; j <= gridPos[1] + rangeY; j++) {
                available = gridAvailable(new int[]{i, j});
                if (!available) {
                    return available;
                }
            }
        }
        return available;
    }

    private boolean gridAvailable(int[] gridPos) {

        int[] obstacleGrid;

        for (Spatial obstacle : obstacleNode.getChildren()) {
            obstacleGrid = GridCalculator.calculateGrid(obstacle.getLocalTranslation());
            if (obstacleGrid[0] == gridPos[0] && obstacleGrid[1] == gridPos[1]) {
                System.out.println("Not available!");
                return false;
            }
        }
        return true;
    }

    private void generateTower(int[] gridPos) {

        Geometry tower = shapeBuilder.generateBox("Tower", 0.5f, 12.0f, 0.5f, null, null, ColorRGBA.Green, GridCalculator.calculateCenter(gridPos[0], gridPos[1]));
        tower.addControl(new TowerControl(bulletNode, 5, 200, 15.0f, creepNode, shapeBuilder, explosion));
        towerNode.attachChild(tower);
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0], gridPos[1] + 1)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0], gridPos[1] - 1)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] + 1, gridPos[1])));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] - 1, gridPos[1])));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] + 1, gridPos[1] + 1)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] - 1, gridPos[1] - 1)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] + 1, gridPos[1] - 1)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] - 1, gridPos[1] + 1)));


    }

    private void generateTower2(int[] gridPos) {

        Geometry tower = shapeBuilder.generateBox("Tower2", 0.5f, 12.0f, 0.5f, null, null, ColorRGBA.Green, GridCalculator.calculateCenter(gridPos[0], gridPos[1] + 1));
        tower.addControl(new TowerControl(bulletNode, 8, 200, 15.0f, creepNode, shapeBuilder, explosion));
        towerNode.attachChild(tower);

        tower = shapeBuilder.generateBox("Tower2", 0.5f, 12.0f, 0.5f, null, null, ColorRGBA.Green, GridCalculator.calculateCenter(gridPos[0], gridPos[1] - 1));
        tower.addControl(new TowerControl(bulletNode, 8, 200, 15.0f, creepNode, shapeBuilder, explosion));
        towerNode.attachChild(tower);

        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0], gridPos[1])));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0], gridPos[1] + 2)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0], gridPos[1] - 2)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] + 1, gridPos[1])));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] - 1, gridPos[1])));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] + 1, gridPos[1] + 1)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] + 1, gridPos[1] + 2)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] - 1, gridPos[1] + 2)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] - 1, gridPos[1] + 1)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] + 1, gridPos[1] - 1)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] + 1, gridPos[1] - 2)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] - 1, gridPos[1] - 2)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] - 1, gridPos[1] - 1)));
    }

    private void generateTower3(int[] gridPos) {

        Geometry tower = shapeBuilder.generateBox("Tower3", 0.5f, 12.0f, 0.5f, null, null, ColorRGBA.Green, GridCalculator.calculateCenter(gridPos[0], gridPos[1]));
        tower.addControl(new TowerControl(bulletNode, 8, 200, 15.0f, creepNode, shapeBuilder, explosion));
        towerNode.attachChild(tower);

        tower = shapeBuilder.generateBox("Tower3", 0.5f, 12.0f, 0.5f, null, null, ColorRGBA.Green, GridCalculator.calculateCenter(gridPos[0], gridPos[1] - 1));
        tower.addControl(new TowerControl(bulletNode, 8, 200, 15.0f, creepNode, shapeBuilder, explosion));
        towerNode.attachChild(tower);

        tower = shapeBuilder.generateBox("Tower3", 0.5f, 12.0f, 0.5f, null, null, ColorRGBA.Green, GridCalculator.calculateCenter(gridPos[0], gridPos[1] + 1));
        tower.addControl(new TowerControl(bulletNode, 8, 200, 15.0f, creepNode, shapeBuilder, explosion));
        towerNode.attachChild(tower);

        tower = shapeBuilder.generateBox("Tower3", 0.5f, 12.0f, 0.5f, null, null, ColorRGBA.Green, GridCalculator.calculateCenter(gridPos[0] + 1, gridPos[1] + 1));
        tower.addControl(new TowerControl(bulletNode, 8, 200, 15.0f, creepNode, shapeBuilder, explosion));
        towerNode.attachChild(tower);

        tower = shapeBuilder.generateBox("Tower3", 0.5f, 12.0f, 0.5f, null, null, ColorRGBA.Green, GridCalculator.calculateCenter(gridPos[0] + 1, gridPos[1] - 1));
        tower.addControl(new TowerControl(bulletNode, 8, 200, 15.0f, creepNode, shapeBuilder, explosion));
        towerNode.attachChild(tower);

        tower = shapeBuilder.generateBox("Tower3", 0.5f, 12.0f, 0.5f, null, null, ColorRGBA.Green, GridCalculator.calculateCenter(gridPos[0] - 1, gridPos[1] - 1));
        tower.addControl(new TowerControl(bulletNode, 8, 200, 15.0f, creepNode, shapeBuilder, explosion));
        towerNode.attachChild(tower);

        tower = shapeBuilder.generateBox("Tower3", 0.5f, 12.0f, 0.5f, null, null, ColorRGBA.Green, GridCalculator.calculateCenter(gridPos[0] - 1, gridPos[1] + 1));
        tower.addControl(new TowerControl(bulletNode, 8, 200, 15.0f, creepNode, shapeBuilder, explosion));
        towerNode.attachChild(tower);

        tower = shapeBuilder.generateBox("Tower3", 0.5f, 12.0f, 0.5f, null, null, ColorRGBA.Green, GridCalculator.calculateCenter(gridPos[0] + 1, gridPos[1]));
        tower.addControl(new TowerControl(bulletNode, 8, 200, 15.0f, creepNode, shapeBuilder, explosion));
        towerNode.attachChild(tower);

        tower = shapeBuilder.generateBox("Tower3", 0.5f, 12.0f, 0.5f, null, null, ColorRGBA.Green, GridCalculator.calculateCenter(gridPos[0] - 1, gridPos[1]));
        tower.addControl(new TowerControl(bulletNode, 8, 200, 15.0f, creepNode, shapeBuilder, explosion));
        towerNode.attachChild(tower);

        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] + 2, gridPos[1])));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] + 2, gridPos[1] + 1)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] + 2, gridPos[1] + 2)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] + 2, gridPos[1] - 1)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] + 2, gridPos[1] - 2)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] + 1, gridPos[1] + 2)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] + 1, gridPos[1] - 2)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0], gridPos[1] + 2)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0], gridPos[1] - 2)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] - 2, gridPos[1] + 2)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] - 2, gridPos[1] + 1)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] - 2, gridPos[1])));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] - 2, gridPos[1] - 1)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] - 2, gridPos[1] - 2)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] - 1, gridPos[1] + 2)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f, "Common/MatDefs/Light/Lighting.j3md", "Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] - 1, gridPos[1] - 2)));

    }

    private void generateCreep(long delay) {
        Geometry creep = shapeBuilder.generateBox("Creep", 1f, 1f, 1f, "Common/MatDefs/Misc/Unshaded.j3md", null, ColorRGBA.Red, spawnPoint.add(new Vector3f(1.0f, 1.0f, 1.0f)));
        System.out.println("Creep path == null: " + (creepPath == null ? "true" : "false"));
        CreepControl control = new CreepControl(creepPath, cam, rootNode, basePoint, 100, 0.1f, delay, 50);

        addCreepListener(control);
        creep.addControl(control);
        creepNode.attachChild(creep);
    }

    public void disposeGeometries() {
        for (Spatial spatial : bulletNode.getChildren()) {
            if (((IGeometryDisposed) spatial.getControl(0)).isDisposed()) {
                spatial.removeFromParent();
            }
        }
        for (Spatial spatial : creepNode.getChildren()) {
            if (((IGeometryDisposed) spatial.getControl(0)).isDisposed()) {
                //creepNode.getChildren().remove(spatial);
                spatial.removeFromParent();


            }
        }

        for (Spatial spatial : baseNode.getChildren()) {
            if (((IGeometryDisposed) spatial.getControl(0)).isDisposed()) {
                spatial.removeFromParent();
                System.out.println("Game over!!!");
            }
        }

        if (!waveFinished && gameStarted) {
            if (creepNode.getChildren().isEmpty()) {
                waveFinished = true;
                System.out.println("Wave index: " + waveIndex);
                waveIndex++;
                ((BitmapText) (app.getGuiNode().getChild("waveOver"))).setText(Constants.WAVE_WIN);
                creepListeners.clear();
                // timer.scheduleAtFixedRate(timerTask, 0, 31000);
            }
        }
    }

    @Override
    public void update(float tpf) {
        disposeGeometries();
        updateHUD();
        //Draw path
        if (creepPath != null && first) {
            first = false;

            pathNode.getChildren().clear();

            for (Vertex vertex : creepPath) {
                pathNode.attachChild(shapeBuilder.generateBox("Path", 0.05f, 10, 0.05f, null, null, ColorRGBA.Orange, vertex.getCenter()));
            }

            for (Spatial obstacle : obstacleNode.getChildren()) {
                //((Geometry) obstacle).getMaterial().setColor("Color", ColorRGBA.Red);
            }
        }
        if (gameStarted && waveFinished) {

            timePassed += tpf;
            ((BitmapText) app.getGuiNode().getChild("nextWaveTimer")).setText("Time to next wave: " + String.valueOf(10 - timeCounter));
            if (timePassed >= 1.0f) {
                timeCounter++;
                timePassed = 0.0f;
            }

            if (timeCounter - 1 == 10) {
                ((BitmapText) app.getGuiNode().getChild("nextWaveTimer")).setText("");
                timeCounter = 0;
                timePassed = 0.0f;
                waveFinished = false;
                calculateCreepPath();
                generateCreepWave();
            }
        }

        if (waveIndex == waveCount + 1 && waveFinished) {
            destroyGame(true);
        } else if (baseNode.getChildren().isEmpty()) {
            destroyGame(false);
        }
        super.update(tpf);
    }

    private void destroyGame(boolean win) {
        gameStarted = false;
        addHUDText("gameOver", win == true ? Constants.GAME_WIN : Constants.GAME_LOSS, 4, new Vector3f(appSettings.getWidth() / 2, appSettings.getHeight() * 0.66f, 0), ColorRGBA.White);
        app.getInputManager().clearMappings();
    }

    private void calculateCreepPath() {

        Thread thread = new Thread() {
            @Override
            public void run() {

                graphBuilder.initializeGraph();
                graphBuilder.setObstacles(obstacleNode, towerNode);
                graphBuilder.initializeEdges();
                graphBuilder.setSourceXY(new int[]{0, 50});
                graphBuilder.setDestinationXY(new int[]{159, 50});

                try {
                    creepPath = pathFinder.calculate(graphBuilder.getGraph(), graphBuilder.getSourceXY(), graphBuilder.getDestinationXY());
                    first = true;
                    notifyCreeps(creepPath);


                } catch (InterruptedException ex) {
                    Logger.getLogger(GameState.class
                            .getName()).log(Level.SEVERE, null, ex);
                }


            }
        };
        thread.start();
    }

//    private void generateCreepWave() {
//
//        Thread thread = new Thread() {
//            @Override
//            public void run() {
//
//                Random rnd = new Random();
//                int numberOfCreeps = rnd.nextInt(5) + 6;
//                numberOfCreeps *= waveIndex;
//
//                while (creepPath == null) {
//                    try {
//                        Thread.sleep(50);
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(GameState.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//
//                for (int i = 0; i < numberOfCreeps; i++) {
//
//                    generateCreep();
//                    try {
//                        Thread.sleep(250);
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(GameState.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            }
//        };
//
//        thread.start();
//    }
    //Probably not the best solution, we gotta ask Tobias...
    private void generateCreepWave() {
        ((BitmapText) app.getGuiNode().getChild("waveOver")).setText("");
        Random rnd = new Random();
        int numberOfCreeps = rnd.nextInt(5) + 6;
        numberOfCreeps *= waveIndex;

        //change to numberOfCreeps
        for (int i = 0; i < numberOfCreeps; i++) {
            generateCreep(i * 750);
        }
    }

    private void notifyCreeps(List<Vertex> path) {
        for (CreepPathUpdatedListener creepPathUpdatedListener : creepListeners) {
            creepPathUpdatedListener.setPath(path);
        }
    }

    private boolean addCreepListener(CreepControl creep) {
        if (!creepListeners.contains(creep)) {
            creepListeners.add(creep);
            return true;
        }
        return false;
    }

    private boolean removeCreepListener(CreepControl creep) {
        if (creepListeners.contains(creep)) {
            creepListeners.remove(creep);
            return true;
        }
        return false;
    }

    public void updateHUD() {
        baseHealth = ((IBase) baseNode.getChild("Base").getControl(0)).getHealth();
        ((BitmapText) app.getGuiNode().getChild("gold")).setText("Gold: " + String.valueOf(gold));
        ((BitmapText) app.getGuiNode().getChild("baseHealth")).setText("Base Heath: " + String.valueOf(baseHealth));

    }

    private void addHUDText(String name, String text, int size, Vector3f position, ColorRGBA color) {
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText bmpText = new BitmapText(font, false);
        bmpText.setColor(color);
        bmpText.setName(name);
        bmpText.setSize(font.getCharSet().getRenderedSize() * size);
        bmpText.setText(text);
        bmpText.setLocalTranslation(position);
        app.getGuiNode().attachChild(bmpText);
    }

    //Clear GUI node except crosshair
    private void clearGUINode() {
        while (app.getGuiNode().getChildren().size() > 1) {
            app.getGuiNode().getChildren().get(app.getGuiNode().getChildren().size() - 1).removeFromParent();
        }
    }

    public static void addGold(int g) {
        gold += g;
    }

    public static void takeGold(int g) {
        gold -= g;
    }
}
