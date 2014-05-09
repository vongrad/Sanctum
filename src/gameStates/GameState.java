/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gameStates;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResults;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import controls.CreepControl;
import de.lessvoid.nifty.effects.impl.Gradient;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import pathfinder.Graph;
import pathfinder.PathFinder;
import pathfinder.Vertex;

/**
 *
 * @author adamv_000
 */
public class GameState extends AbstractAppState {

  
    private SimpleApplication app;
    private Node rootNode;
    private Camera cam;
    private AssetManager assetManager;
    private Ray ray;
    private PathFinder pathFinder;
    private static final int xBlock = 80;
    private static final int zBlock = 50;
    private static final float blockSize = 1.0f;
    private static Vector3f spawnPoint;
    private static Vector3f basePoint;
    private List<Vertex> creepPath;
    private Graph graphBuilder;
    private CreepControl test;
    
    private boolean first = true;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);



        thread.start();



        this.app = (SimpleApplication) app;
        this.cam = this.app.getCamera();
        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        ray = new Ray();

        initLight();
        initConstants();

//        for (int i = 0; i < xBlock * 2; i++) {
//            for (int j = 0; j < zBlock * 2; j++) {
//                rootNode.attachChild(generateBox("Test" + i + j, 0.5f, 3.0f, 0.5f, null, ColorRGBA.Orange, graphBuilder.getVertex(i, j).getCenter()));
//            }
//        }



        Geometry platform = generateBox("Platform", xBlock * blockSize, 1f, zBlock * blockSize, "Common/MatDefs/Misc/Unshaded.j3md", ColorRGBA.Blue, Vector3f.ZERO);
        Geometry base = generateBox("Base", 3 * blockSize, 3 * blockSize, 5 * blockSize, "Common/MatDefs/Misc/Unshaded.j3md", ColorRGBA.Pink, new Vector3f(basePoint.add(new Vector3f(-blockSize * 3f, 3.0f, 0.0f))));
        generateCreep();
        //calculatePath();

        rootNode.attachChild(platform);
        rootNode.attachChild(base);

    }
    private Thread thread = new Thread() {
        @Override
        public void run() {

            List<int[]> obstacles = new ArrayList<int[]>();
            obstacles.add(new int[]{139, 0});
            obstacles.add(new int[]{139, 1});
            obstacles.add(new int[]{139, 2});
            obstacles.add(new int[]{139, 3});
            obstacles.add(new int[]{139, 4});
            obstacles.add(new int[]{139, 5});
            obstacles.add(new int[]{139, 6});
            obstacles.add(new int[]{139, 7});
            obstacles.add(new int[]{139, 8});
            obstacles.add(new int[]{139, 9});
            obstacles.add(new int[]{139, 10});
            obstacles.add(new int[]{139, 11});
            obstacles.add(new int[]{139, 12});
            obstacles.add(new int[]{139, 13});
            obstacles.add(new int[]{139, 14});
            obstacles.add(new int[]{139, 15});
            obstacles.add(new int[]{139, 16});
            obstacles.add(new int[]{139, 17});
            obstacles.add(new int[]{139, 18});
            obstacles.add(new int[]{139, 19});
            obstacles.add(new int[]{139, 20});
            obstacles.add(new int[]{139, 21});
            obstacles.add(new int[]{139, 22});
            obstacles.add(new int[]{139, 23});
            obstacles.add(new int[]{139, 24});
            obstacles.add(new int[]{139, 25});
            obstacles.add(new int[]{139, 26});
            obstacles.add(new int[]{139, 27});
            obstacles.add(new int[]{139, 28});
            obstacles.add(new int[]{139, 29});
            obstacles.add(new int[]{139, 30});
            obstacles.add(new int[]{139, 31});
            obstacles.add(new int[]{139, 32});
            obstacles.add(new int[]{139, 33});
            obstacles.add(new int[]{139, 34});
            obstacles.add(new int[]{139, 35});
            obstacles.add(new int[]{139, 36});
            obstacles.add(new int[]{139, 37});
            obstacles.add(new int[]{139, 38});
            obstacles.add(new int[]{140, 39});
            obstacles.add(new int[]{139, 40});
            obstacles.add(new int[]{139, 41});
            obstacles.add(new int[]{139, 42});
            obstacles.add(new int[]{139, 43});
            obstacles.add(new int[]{139, 44});
            obstacles.add(new int[]{139, 45});
            obstacles.add(new int[]{139, 46});
            obstacles.add(new int[]{139, 47});
            obstacles.add(new int[]{139, 48});
            obstacles.add(new int[]{139, 49});
            obstacles.add(new int[]{139, 50});
            obstacles.add(new int[]{139, 51});
            obstacles.add(new int[]{139, 52});
            obstacles.add(new int[]{139, 53});
            obstacles.add(new int[]{139, 54});
            obstacles.add(new int[]{139, 55});
            obstacles.add(new int[]{139, 56});
            obstacles.add(new int[]{139, 57});
            obstacles.add(new int[]{139, 58});
            obstacles.add(new int[]{139, 59});
            obstacles.add(new int[]{139, 60});
            
            
                        
            obstacles.add(new int[]{60, 0});
            obstacles.add(new int[]{60, 1});
            obstacles.add(new int[]{60, 2});
            obstacles.add(new int[]{60, 3});
            obstacles.add(new int[]{60, 4});
            obstacles.add(new int[]{60, 5});
            obstacles.add(new int[]{60, 6});
            obstacles.add(new int[]{60, 7});
            obstacles.add(new int[]{60, 8});
            obstacles.add(new int[]{60, 9});
            obstacles.add(new int[]{60, 10});
            obstacles.add(new int[]{60, 11});
            obstacles.add(new int[]{60, 12});
            obstacles.add(new int[]{60, 13});
            obstacles.add(new int[]{60, 14});
            obstacles.add(new int[]{60, 15});
            obstacles.add(new int[]{60, 16});
            obstacles.add(new int[]{60, 17});
            obstacles.add(new int[]{60, 18});
            obstacles.add(new int[]{60, 19});
            obstacles.add(new int[]{60, 20});
            obstacles.add(new int[]{60, 21});
            obstacles.add(new int[]{60, 22});
            obstacles.add(new int[]{60, 23});
            obstacles.add(new int[]{60, 24});
            obstacles.add(new int[]{60, 25});
            obstacles.add(new int[]{60, 26});
            obstacles.add(new int[]{60, 27});
            obstacles.add(new int[]{60, 28});
            obstacles.add(new int[]{60, 29});
            obstacles.add(new int[]{60, 30});
            obstacles.add(new int[]{60, 31});
            obstacles.add(new int[]{60, 32});
            obstacles.add(new int[]{60, 33});
            obstacles.add(new int[]{60, 34});
            obstacles.add(new int[]{60, 35});
            obstacles.add(new int[]{60, 36});
            obstacles.add(new int[]{60, 37});
            obstacles.add(new int[]{60, 38});
            obstacles.add(new int[]{60, 39});
            obstacles.add(new int[]{60, 40});
            obstacles.add(new int[]{60, 41});
            obstacles.add(new int[]{60, 42});
            obstacles.add(new int[]{60, 43});
            obstacles.add(new int[]{60, 44});
            obstacles.add(new int[]{60, 45});
            obstacles.add(new int[]{60, 46});
            obstacles.add(new int[]{60, 47});
            obstacles.add(new int[]{60, 48});
            obstacles.add(new int[]{60, 49});
            obstacles.add(new int[]{60, 50});
            obstacles.add(new int[]{60, 51});
            obstacles.add(new int[]{60, 52});
            obstacles.add(new int[]{60, 53});
            obstacles.add(new int[]{60, 54});
            obstacles.add(new int[]{60, 55});
            obstacles.add(new int[]{60, 56});
            obstacles.add(new int[]{60, 57});
            obstacles.add(new int[]{60, 58});
            obstacles.add(new int[]{60, 59});
            obstacles.add(new int[]{60, 60});


            pathFinder = new PathFinder();

            graphBuilder = new Graph(xBlock * 2, xBlock * 2);
            graphBuilder.initializeGraph();
            graphBuilder.setObstacles(obstacles);
            graphBuilder.initializeEdges();
            graphBuilder.setSourceXY(new int[]{0, 50});
            graphBuilder.setDestinationXY(new int[]{159, 50});

            try {
                creepPath = pathFinder.calculate(graphBuilder.getGraph(), graphBuilder.getSourceXY(), graphBuilder.getDestinationXY());


                test.setCreepPath(creepPath);
            } catch (InterruptedException ex) {
                Logger.getLogger(GameState.class.getName()).log(Level.SEVERE, null, ex);
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
        test = new CreepControl(cam, rootNode, basePoint);
        creep.addControl(test);
        rootNode.attachChild(creep);
    }

    @Override
    public void update(float tpf) {
        if (creepPath != null && first) {
            first = false;
            for (Vertex vertex : creepPath) {
                rootNode.attachChild(generateBox("Test", 0.5f, 10, 0.5f, null, ColorRGBA.Orange, vertex.getCenter()));
            }
            
        
        }
        super.update(tpf); //To change body of generated methods, choose Tools | Templates.
    }
}
