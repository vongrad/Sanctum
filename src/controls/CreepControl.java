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
import de.lessvoid.nifty.tools.LinearInterpolator.Point;
import java.util.List;
import pathfinder.Vertex;

/**
 *
 * @author adamv_000
 */
public class CreepControl extends AbstractControl {

    private Ray ray;
    private final Camera cam;
    private final Node rootNode;
    private final Vector3f basePoint;
    private List<Vertex> creepPath;
    private int pathIndex;

    public CreepControl(Camera cam, Node rootNode, Vector3f basePoint) {
        this.cam = cam;
        this.rootNode = rootNode;
        this.basePoint = basePoint;
        ray = new Ray();
        pathIndex = 0;
    }

    public void setCreepPath(List<Vertex> creepPath) {
        this.creepPath = creepPath;
        pathIndex = creepPath.size() - 1;
    }

    @Override
    protected void controlUpdate(float tpf) {
        spatial.rotate(0, 0, -tpf);
//        ray.setOrigin(spatial.getLocalTranslation());
//        ray.setDirection(basePoint);
//        
//        CollisionResults colisionRes = new CollisionResults();
//        rootNode.collideWith(ray, colisionRes);

        if (creepPath != null) {
            if (spatial.getLocalTranslation().getX() > creepPath.get(pathIndex).getCenter().getX() - 0.5f && spatial.getLocalTranslation().getX() < spatial.getLocalTranslation().getX() + 0.5f
                    && spatial.getLocalTranslation().getZ() > creepPath.get(pathIndex).getCenter().getZ() - 0.5f && spatial.getLocalTranslation().getZ() < creepPath.get(pathIndex).getCenter().getZ() + 0.5f) {
                if (pathIndex > 1){
                    pathIndex --;
                }
            }

            Vector3f direction = new Vector3f(creepPath.get(pathIndex).getCenter().getX() - spatial.getLocalTranslation().getX(), 0, creepPath.get(pathIndex).getCenter().getX() + spatial.getLocalTranslation().getZ());
            direction.normalizeLocal();
            //System.out.println("X: " + direction.getX() + "Z: " + direction.getZ() );


            //spatial.setLocalTranslation(spatial.getLocalTranslation().add(creepPath.get(pathIndex).getCenter().normalize().mult(0.05f)));
            spatial.setLocalTranslation(MovePointTowards(spatial.getLocalTranslation(), creepPath.get(pathIndex).getCenter()));
        }
    }

    public Vector3f MovePointTowards(Vector3f a, Vector3f b) {
        Vector3f direction = new Vector3f(b.getX() - a.getX(), 0, b.getZ() - a.getZ());
        direction = direction.normalize();
        return new Vector3f(a.getX() + direction.getX() * 0.3f, a.getY(), a.getZ() + direction.getZ() * 0.3f);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
