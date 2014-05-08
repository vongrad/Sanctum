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
import java.util.ArrayList;
import java.util.List;

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
    
    private static final int xBlock = 80;
    private static final int zBlock = 50;
    private static final float blockSize = 1.0f;
    private static Vector3f spawnPoint;
    private static Vector3f basePoint;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        this.cam = this.app.getCamera();
        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        ray = new Ray();

        initLight();
        initConstants();
        
        Geometry platform = generateBox("Platform", xBlock * blockSize, 1f, zBlock * blockSize, "Common/MatDefs/Misc/Unshaded.j3md", ColorRGBA.Blue, Vector3f.ZERO);
        Geometry base = generateBox("Base", 3 * blockSize, 3 * blockSize, 5 * blockSize, "Common/MatDefs/Misc/Unshaded.j3md", ColorRGBA.Pink, new Vector3f(basePoint.add(new Vector3f(-blockSize * 3f, 3.0f, 0.0f))));
        generateCreep();
        calculatePath();
        
        rootNode.attachChild(platform);
        rootNode.attachChild(base);

    }

    private void initLight() {
        rootNode.addLight(new AmbientLight());
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-10.0f, -1.3f, 1.3f));
        rootNode.addLight(sun);
    }
    
    private List<Vector3f> calculatePath(){
        
        List<Vector3f> points = new ArrayList<Vector3f>();
        float moveSpeed = 0.5f;
        
        Vector3f creepPos = new Vector3f(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ());
        CollisionResults colResults = new CollisionResults();

        while (!(creepPos.getX() > basePoint.getX() && (creepPos.getZ() < 5 && creepPos.getZ() > -5))){
            
            ray.setOrigin(creepPos);
            ray.setDirection(basePoint);
            ray.collideWith(rootNode, colResults);
            
            if (colResults.size() > 0){
                Geometry target = colResults.getClosestCollision().getGeometry();
                
                float distance = creepPos.distance(target.getLocalTranslation());
                
                if (distance < moveSpeed){
                    
                }
                
            }
            
            
            
         
            
            creepPos = creepPos.add(new Vector3f(1f, 0f, 0f));
            
            

        }
        
        
        return null;
    }
    
    private boolean isInside(Vector3f creepPosition){
        
        //if (creepPosition.getX() < basePoint.getX() + 3 && creepPosition.getX() > basePoint.getX() - 3 )
        
        
        return false;
    }

    private void initConstants() {
        spawnPoint = new Vector3f(-xBlock * blockSize, 1.0f, 0.0f);
        basePoint = new Vector3f(xBlock * blockSize, 1.0f, 0.0f);
    }

    private Geometry generateBox(String name, float x, float y, float z, String matPath, ColorRGBA color, Vector3f pos) {
        Box box = new Box(x, y, z);
        Geometry geom = new Geometry(name, box);
      
        Material mat = new Material(assetManager, matPath);
        if (color != null) {
            mat.setColor("Color", color);
        }
        geom.setMaterial(mat);
        geom.setLocalTranslation(pos);
        return geom;
    }
    
    private void generateCreep() {
        Geometry creep = generateBox("Creep", 1f, 1f, 1f, "Common/MatDefs/Misc/Unshaded.j3md", ColorRGBA.Red, spawnPoint.add(new Vector3f(1.0f, 1.0f, 1.0f)));
        creep.addControl(new CreepControl(cam, rootNode, basePoint));
        rootNode.attachChild(creep);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf); //To change body of generated methods, choose Tools | Templates.
    }
}
