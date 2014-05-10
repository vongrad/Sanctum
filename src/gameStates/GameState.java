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
import de.lessvoid.nifty.effects.impl.Gradient;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import pathfinder.Constants;
import pathfinder.Graph;
import pathfinder.PathFinder;
import pathfinder.Utils;
import pathfinder.Vertex;
import sun.misc.Queue;

/**
 *
 * @author adamv_000
 */
public class GameState extends AbstractAppState {

    private SimpleApplication app;
    private Camera cam;
    private AssetManager assetManager;
    private Ray ray;
    private Node rootNode;
    private Node creepNode;
    private Node towerNode;
    private Node pathNode;
    private Node obstacleNode;
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

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        //thread.start();
        creepListeners = new ArrayList<CreepPathUpdatedListener>();

        action = Constants.ACTION_OBSTACLE;

        this.app = (SimpleApplication) app;
        this.cam = this.app.getCamera();
        this.rootNode = this.app.getRootNode();
        this.towerNode = new Node();
        this.obstacleNode = new Node();
        this.creepNode = new Node();
        this.pathNode = new Node();

        this.assetManager = this.app.getAssetManager();
        ray = new Ray();
        rootNode.attachChild(creepNode);
        rootNode.attachChild(towerNode);
        rootNode.attachChild(obstacleNode);
        rootNode.attachChild(pathNode);

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

                    if (action == 1) {
                        Vector3f contactPoint = collisionResults.getClosestCollision().getContactPoint();
                        contactPoint.setZ(contactPoint.getZ());
                        int[] grid = Utils.calculateGrid(contactPoint);
                        obstacleNode.attachChild(generateBox("Obscale", 0.5f, 10f, 0.5f, null, ColorRGBA.Yellow, Utils.calculateCenter(grid[0], grid[1])));
                    } else if (action == 2) {
                    }


                }
            }
            if (name.equals(MAPPING_ACTION_OBSTACLE)) {
                action = 1;
            }
            if (name.equals(MAPPING_ACTION_TOWER)) {
                action = 2;
            }
            if (name.equals(MAPPING_START_WAVE)) {
                calculateCreepPath();
                generateCreep();

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
        rootNode.attachChild(generateBox("Platform", xBlock * blockSize, 1f, zBlock * blockSize, "Common/MatDefs/Misc/Unshaded.j3md", ColorRGBA.Blue, Vector3f.ZERO));
    }

    private void initBase() {
        rootNode.attachChild(generateBox("Base", 3 * blockSize, 3 * blockSize, 5 * blockSize, "Common/MatDefs/Misc/Unshaded.j3md", ColorRGBA.Pink, new Vector3f(basePoint.add(new Vector3f(-blockSize * 3f, 3.0f, 0.0f)))));
    }

    private Geometry generateBox(String name, float x, float y, float z, String matPath, ColorRGBA color, Vector3f pos) {
        Box box = new Box(x, y, z);
        Geometry geom = new Geometry(name, box);

        Material mat;

        if (matPath == null) {
            mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        } else {
            mat = new Material(assetManager, matPath);
        }

        if (color != null) {
            mat.setColor("Color", color);
        }
        geom.setMaterial(mat);
        geom.setLocalTranslation(pos);
        return geom;
    }

    private void generateCreep() {
        Geometry creep = generateBox("Creep", 1f, 1f, 1f, "Common/MatDefs/Misc/Unshaded.j3md", ColorRGBA.Red, spawnPoint.add(new Vector3f(1.0f, 1.0f, 1.0f)));
        CreepControl control = new CreepControl(cam, rootNode, basePoint);
        addCreepListener(control);
        creep.addControl(control);
        creepNode.attachChild(creep);
    }

    @Override
    public void update(float tpf) {

        //Draw path
        if (creepPath != null && first) {
            first = false;

            pathNode.getChildren().clear();

            for (Vertex vertex : creepPath) {
                pathNode.attachChild(generateBox("Path", 0.05f, 10, 0.05f, null, ColorRGBA.Orange, vertex.getCenter()));
            }

            for (Spatial obstacle : obstacleNode.getChildren()) {
                ((Geometry) obstacle).getMaterial().setColor("Color", ColorRGBA.Red);
            }
        }
        super.update(tpf);
    }

    private void calculateCreepPath() {

        Thread thread = new Thread() {
            @Override
            public void run() {

                pathFinder = new PathFinder();

                graphBuilder = new Graph(xBlock * 2, xBlock * 2);
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
