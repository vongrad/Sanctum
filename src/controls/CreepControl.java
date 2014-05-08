/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controls;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author adamv_000
 */
public class CreepControl extends AbstractControl{
    
    private Ray ray;
    private final Camera cam;
    private final Node rootNode;
    private final Vector3f basePoint;

    public CreepControl(Camera cam, Node rootNode, Vector3f basePoint) {
        this.cam = cam;
        this.rootNode = rootNode;
        this.basePoint = basePoint;
        ray = new Ray();
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        spatial.rotate(0, 0, -tpf);
        ray.setOrigin(spatial.getLocalTranslation());
        ray.setDirection(basePoint);
        
        CollisionResults colisionRes = new CollisionResults();
        rootNode.collideWith(ray, colisionRes);
        
      
        
        
        
        spatial.setLocalTranslation(spatial.getLocalTranslation().add(new Vector3f(tpf * 15, 0.0f, 0.0f)));
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
    
}
