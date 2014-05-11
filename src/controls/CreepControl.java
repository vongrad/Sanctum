/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controls;

import Listeners.CreepPathUpdatedListener;
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
import java.util.Timer;
import java.util.TimerTask;
import pathfinder.Vertex;

/**
 *
 * @author adamv_000
 */
public class CreepControl extends AbstractControl implements CreepPathUpdatedListener, GeometryDisposed {

    private int life;
    private float speed;
    private Ray ray;
    private final Camera cam;
    private final Node rootNode;
    private final Vector3f basePoint;
    private List<Vertex> creepPath;
    private int pathIndex;
    private boolean isDeath;
    private Timer timer;
    private long delay;
    private boolean canWalk;

    public CreepControl(List<Vertex> creepPath, Camera cam, Node rootNode, Vector3f basePoint, int life, float speed, long delay) {
        this.delay = delay;
        this.creepPath = creepPath;
        this.life = life;
        this.speed = speed;
        this.cam = cam;
        this.rootNode = rootNode;
        this.basePoint = basePoint;
        ray = new Ray();
        pathIndex = 0;
        isDeath = false;
        timer = new Timer();
        this.canWalk = false;
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
        if (creepPath != null && canWalk) {
            if (spatial.getLocalTranslation().getX() >= creepPath.get(pathIndex).getCenter().getX() - 0.5f && spatial.getLocalTranslation().getX() <= spatial.getLocalTranslation().getX() + 0.5f
                    && spatial.getLocalTranslation().getZ() >= creepPath.get(pathIndex).getCenter().getZ() - 0.5f && spatial.getLocalTranslation().getZ() <= creepPath.get(pathIndex).getCenter().getZ() + 0.5f) {
                if (pathIndex > 1) {
                    pathIndex--;
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
        return new Vector3f(a.getX() + direction.getX() * speed, a.getY(), a.getZ() + direction.getZ() * speed);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public void setPath(List<Vertex> path) {
        creepPath = path;
        pathIndex = creepPath.size() - 1;
        timer.schedule(timerTask, delay);
    }
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            canWalk = true;
        }
    };

    public void causeDamage(int damage) {
        this.life -= damage;

        if (life <= 0) {
            isDeath = true;
            System.out.println("Death!");
        } else {
            isDeath = false;
        }

    }

    public boolean isDisposed() {
        return isDeath;
    }
}
