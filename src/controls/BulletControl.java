/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controls;

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author adamv_000
 */
public class BulletControl extends AbstractControl implements GeometryDisposed{

    private Spatial target;
    private float bulletSpeed;
    private boolean dispose;
    private int damage;

    public BulletControl(Spatial target, float bulletSpeed, int damage) {
        this.damage = damage;
        this.target = target;
        this.bulletSpeed = bulletSpeed;
        dispose = false;
    }

    @Override
    protected void controlUpdate(float tpf) {
        spatial.setLocalTranslation(calculateNewPosition());
        reachedTarget();
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    private Vector3f calculateNewPosition() {

        Vector3f sourceVector = spatial.getLocalTranslation();
        Vector3f destinationVector = target.getLocalTranslation();

        Vector3f direction = new Vector3f(destinationVector.getX() - sourceVector.getX(),
                destinationVector.getY() - sourceVector.getY(),
                destinationVector.getZ() - sourceVector.getZ());
        direction = direction.normalize();
        return new Vector3f(sourceVector.getX() + direction.getX() * bulletSpeed, sourceVector.getY() + direction.getY() * bulletSpeed, sourceVector.getZ() + direction.getZ() * bulletSpeed);
    }

    private void reachedTarget() {

        if (target != null && !isTargetDisposed()) {
            Vector3f sourceVector = spatial.getLocalTranslation();
            Vector3f destinationVector = target.getLocalTranslation();

            if (sourceVector.getX() > destinationVector.getX() - 0.5f && sourceVector.getX() < destinationVector.getX() + 0.5f
                    && sourceVector.getY() > destinationVector.getY() - 0.5f && sourceVector.getY() < destinationVector.getY() + 0.5f
                    && sourceVector.getZ() > destinationVector.getZ() - 0.5f && sourceVector.getZ() < destinationVector.getZ() + 0.5f) {
                ((CreepControl)target.getControl(0)).causeDamage(damage);
                dispose = true;
                target = null;
            }
        }else{
            dispose = true;
            target = null;
        }
        
    }
    
    @Override
    public boolean isDisposed() {
        return this.dispose;
    }
    
    //For bullets to disappear
    public boolean isTargetDisposed(){
        return ((GeometryDisposed)target.getControl(0)).isDisposed();
    }
}
