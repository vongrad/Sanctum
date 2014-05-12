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
import com.jme3.collision.CollisionResults;
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
import controls.CreepControl;
import controls.GeometryDisposed;
import controls.TowerControl;
import de.lessvoid.nifty.effects.impl.Gradient;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private Ray ray;
    private Node rootNode;
    private Node creepNode;
    private Node towerNode;
    private Node pathNode;
    private Node obstacleNode;
    private Node bulletNode;
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
    private static final Trigger TRIGGER_START_WAVE = new KeyTrigger(KeyInput.KEY_F1);
    private static final String MAPPING_BUILD = "Build";
    private static final String MAPPING_ACTION_OBSTACLE = "Action_Obstacle";
    private static final String MAPPING_ACTION_TOWER = "Action_Tower";
    private static final String MAPPING_START_WAVE = "Start_Wave";
    private int action;
    private boolean first = true;
    private int waveIndex;
    //private int waveCount;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        creepListeners = new ArrayList<CreepPathUpdatedListener>();

        pathFinder = new PathFinder();
        graphBuilder = new Graph(xBlock * 2, xBlock * 2);

        action = Constants.ACTION_OBSTACLE;
        waveIndex = 1;
        //waveCount = 5;

        this.app = (SimpleApplication) app;
        this.cam = this.app.getCamera();
        this.rootNode = this.app.getRootNode();
        this.towerNode = new Node();
        this.bulletNode = new Node();
        this.obstacleNode = new Node();
        this.creepNode = new Node();
        this.pathNode = new Node();

        this.assetManager = this.app.getAssetManager();
        ray = new Ray();
        rootNode.attachChild(creepNode);
        rootNode.attachChild(towerNode);
        rootNode.attachChild(obstacleNode);
        rootNode.attachChild(pathNode);
        rootNode.attachChild(bulletNode);

        shapeBuilder = new ShapeBuilder(assetManager);

        initLight();
        initConstants();
        initTriggers();
        initPlatform();
        initBase();
        //generateCreep();
    }

    private void initTriggers() {
        app.getInputManager().addMapping(MAPPING_BUILD, TRIGGER_BUILD);
        app.getInputManager().addMapping(MAPPING_ACTION_OBSTACLE, TRIGGER_ACTION_OBSTACLE);
        app.getInputManager().addMapping(MAPPING_ACTION_TOWER, TRIGGER_ACTION_TOWER);
        app.getInputManager().addMapping(MAPPING_START_WAVE, TRIGGER_START_WAVE);
        app.getInputManager().addListener(actionListener, new String[]{MAPPING_BUILD, MAPPING_ACTION_OBSTACLE, MAPPING_ACTION_TOWER, MAPPING_START_WAVE});
    }
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {

            if (name.equals(MAPPING_BUILD) && !isPressed) {
                ray.setOrigin(cam.getLocation());
                ray.setDirection(cam.getDirection());

                CollisionResults collisionResults = new CollisionResults();
                rootNode.collideWith(ray, collisionResults);

                if (collisionResults.size() > 0) {
                    int[] grid = GridCalculator.calculateGrid(collisionResults.getClosestCollision().getContactPoint());
                    if (action == 1 && gridAvailable(grid)) {
                        obstacleNode.attachChild(shapeBuilder.generateBox("Obscale", 0.5f, 10f, 0.5f, "Common/MatDefs/Light/Lighting.j3md","Textures/Terrain/Rock/bricks.jpg", ColorRGBA.Yellow, GridCalculator.calculateCenter(grid[0], grid[1])));
                    } else if (action == 2 && gridTowerAvailable(grid)) {
                        generateTower(grid);
                    }


                }
            }
            if (name.equals(MAPPING_ACTION_OBSTACLE)) {
                action = 1;
            }
            if (name.equals(MAPPING_ACTION_TOWER)) {
                action = 2;
            }
            if (name.equals(MAPPING_START_WAVE) && !isPressed) {
                creepNode.getChildren().clear();
                creepListeners.clear();
                calculateCreepPath();
                generateCreepWave();
                //generateCreep(0);
                //generateCreep(0);
                //generateCreep(0);

            }
        }
    };

    private void initLight() {
        rootNode.addLight(new AmbientLight());
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-10.0f, -1.3f, 1.3f));
        rootNode.addLight(sun);
    }

    private void initConstants() {
        spawnPoint = new Vector3f(-xBlock * blockSize, 1.0f, 0.0f);
        basePoint = new Vector3f(xBlock * blockSize, 1.0f, 0.0f);
    }

    private void initPlatform() {
        rootNode.attachChild(shapeBuilder.generateBox("Platform", xBlock * blockSize, 1f, zBlock * blockSize, "Common/MatDefs/Light/Lighting.j3md","Textures/Terrain/Grass/grass.jpg", ColorRGBA.White, Vector3f.ZERO));
    }

    private void initBase() {
        rootNode.attachChild(shapeBuilder.generateBox("Base", 3 * blockSize, 3 * blockSize, 5 * blockSize, "Common/MatDefs/Light/Lighting.j3md","Textures/Terrain/Wood/wood.jpg", ColorRGBA.Brown, new Vector3f(basePoint.add(new Vector3f(-blockSize * 3f, 3.0f, 0.0f)))));
    }

    private boolean gridTowerAvailable(int[] gridPos) {

        boolean available = true;

        for (int i = gridPos[0] - 1; i <= gridPos[0] + 1; i++) {
            for (int j = gridPos[1] - 1; j <= gridPos[1] + 1; j++) {
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

        Geometry tower = shapeBuilder.generateBox("Tower", 0.5f, 12.0f, 0.5f, null,null, ColorRGBA.Green, GridCalculator.calculateCenter(gridPos[0], gridPos[1]));
        tower.addControl(new TowerControl(bulletNode, 5, 200, 15.0f, creepNode, shapeBuilder));
        towerNode.attachChild(tower);
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f,"Common/MatDefs/Light/Lighting.j3md","Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0], gridPos[1] + 1)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f,"Common/MatDefs/Light/Lighting.j3md","Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0], gridPos[1] - 1)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f,"Common/MatDefs/Light/Lighting.j3md","Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] + 1, gridPos[1])));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f,"Common/MatDefs/Light/Lighting.j3md","Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] - 1, gridPos[1])));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f,"Common/MatDefs/Light/Lighting.j3md","Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] + 1, gridPos[1] + 1)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f,"Common/MatDefs/Light/Lighting.j3md","Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] - 1, gridPos[1] - 1)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f,"Common/MatDefs/Light/Lighting.j3md","Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] + 1, gridPos[1] - 1)));
        obstacleNode.attachChild(shapeBuilder.generateBox("Obstacle", 0.5f, 10.0f, 0.5f,"Common/MatDefs/Light/Lighting.j3md","Textures/Terrain/Building/Building.jpg", ColorRGBA.Gray, GridCalculator.calculateCenter(gridPos[0] - 1, gridPos[1] + 1)));


    }

    private void generateCreep(long delay) {
        Geometry creep = shapeBuilder.generateBox("Creep", 1f, 1f, 1f, "Common/MatDefs/Misc/Unshaded.j3md",null, ColorRGBA.Red, spawnPoint.add(new Vector3f(1.0f, 1.0f, 1.0f)));
        System.out.println("Creep path == null: " + (creepPath == null ? "true" : "false"));
        CreepControl control = new CreepControl(creepPath, cam, rootNode, basePoint, 100, 0.3f, delay);

        addCreepListener(control);
        creep.addControl(control);
        creepNode.attachChild(creep);
    }

    public void disposeGeometries() {
        for (Spatial spatial : bulletNode.getChildren()) {
            if (((GeometryDisposed) spatial.getControl(0)).isDisposed()) {
                spatial.removeFromParent();
            }
        }
        for (Spatial spatial : creepNode.getChildren()) {
            if (((GeometryDisposed) spatial.getControl(0)).isDisposed()) {
                //creepNode.getChildren().remove(spatial);
                spatial.removeFromParent();

            }
        }
    }

    @Override
    public void update(float tpf) {
        disposeGeometries();
        //Draw path
        if (creepPath != null && first) {
            first = false;

            pathNode.getChildren().clear();

            for (Vertex vertex : creepPath) {
                pathNode.attachChild(shapeBuilder.generateBox("Path", 0.05f, 10, 0.05f, null,null, ColorRGBA.Orange, vertex.getCenter()));
            }

            for (Spatial obstacle : obstacleNode.getChildren()) {
                //((Geometry) obstacle).getMaterial().setColor("Color", ColorRGBA.Red);
            }
        }
        super.update(tpf);
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
                    Logger.getLogger(GameState.class.getName()).log(Level.SEVERE, null, ex);
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
        
        Random rnd = new Random();
        int numberOfCreeps = rnd.nextInt(5) + 6;
        numberOfCreeps *= waveIndex;
        
        for (int i = 0; i < numberOfCreeps; i++) {
            generateCreep(i * 250);
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
}
