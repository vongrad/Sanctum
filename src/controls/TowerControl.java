/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controls;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import utils.ShapeBuilder;

/**
 *
 * @author adamv_000
 */
public class TowerControl extends AbstractControl {

    private int damage;
    private long fireSpeed;
    private float bulletSpeed;
    private float range;
    private Node creepNode;
    private Node bulletNode;
    private Spatial currentTarget;
    private ShapeBuilder shapeBuilder;
    private boolean canShoot;

    public TowerControl(Node bulletNode, int damage, long fireSpeed, float range, Node creepNode, ShapeBuilder shapeBuilder) {
        this.bulletNode = bulletNode;
        canShoot = true;
        this.shapeBuilder = shapeBuilder;
        this.damage = damage;
        this.fireSpeed = fireSpeed;
        this.creepNode = creepNode;
        this.range = range;
        this.bulletSpeed = 0.35f;
        scheduleTimer();
    }

    @Override
    protected void controlUpdate(float tpf) {
        getCreepInRange();
        if (currentTarget != null && canShoot) {
            canShoot = false;
            Geometry bullet = shapeBuilder.generateBullet("Bullet", 16, 16, bulletSpeed, null, ColorRGBA.Red, new Vector3f(spatial.getLocalTranslation().getX(), spatial.getLocalTranslation().getY() + 11f, spatial.getLocalTranslation().getZ()));
            bullet.addControl(new BulletControl(currentTarget, bulletSpeed, damage));
            bulletNode.attachChild(bullet);
        }
        //bulletChecker();
    }

    private void scheduleTimer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, new Date(), fireSpeed);
    }
//    private void bulletChecker() {
//        for (Spatial bullet : bulletNode.getChildren()) {
//            ((Geometry) bullet).getControl(0);
//            BulletControl control = (BulletControl)((Geometry) bullet).getControl(0);
//            if (control.bulletReachedTarget()){
//                bulletNode.getChildren().remove(bullet);
//            }
//            
//        }
//    }
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            canShoot = true;
        }
    };

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    private void getCreepInRange() {

        if (creepNode.getChildren().size() > 0) {
            if (currentTarget != null) {
                if (calculateDistance(currentTarget) > range || ((GeometryDisposed) currentTarget.getControl(0)).isDisposed()) {
                    currentTarget = null;
                }
            } else if (currentTarget == null) {

                Spatial closest = creepNode.getChildren().get(0);
                float closestDistance = calculateDistance(closest);

                for (Spatial creep : creepNode.getChildren()) {
                    if (closestDistance > calculateDistance(creep)) {
                        closest = creep;
                    }
                }

                if (closestDistance < range) {
                    currentTarget = closest;
                }
            }
        } else {
            currentTarget = null;
        }
    }

    private float calculateDistance(Spatial creep) {
        return creep.getLocalTranslation().distance(spatial.getLocalTranslation());
    }
}
