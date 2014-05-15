/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controls;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adamv_000
 */
public class BulletControl extends AbstractControl implements IGeometryDisposed {

    private Spatial target;
    private float bulletSpeed;
    private boolean dispose;
    private int damage;
    private Node explosionNode;
    private AssetManager assetManager;

    public BulletControl(Spatial target, float bulletSpeed, int damage, Node explosionNode, AssetManager assetManager) {
        this.damage = damage;
        this.target = target;
        this.bulletSpeed = bulletSpeed;
        this.explosionNode = explosionNode;
        this.assetManager = assetManager;
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
                ParticleEmitter explosionParticle = new ParticleEmitter(
                        "My explosion effect", ParticleMesh.Type.Triangle, 30);
                explosionParticle.setStartSize(5f);
                explosionParticle.setEndSize(5f);
                Material flash_mat = new Material(
                        assetManager, "Common/MatDefs/Misc/Particle.j3md");
                flash_mat.setTexture("Texture",
                        assetManager.loadTexture("Effects/Explosion/flash.png"));
                explosionParticle.setMaterial(flash_mat);
                explosionParticle.setImagesX(2); // columns
                explosionParticle.setImagesY(2); // rows
                explosionParticle.setSelectRandomImage(true);
                explosionParticle.setStartColor(new ColorRGBA(1f, 0f, 0f, 1f));   // red
                explosionParticle.setEndColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
                explosionParticle.setLocalTranslation(target.getLocalTranslation());
//              target.getLocalTranslation().getX(), target.getLocalTranslation().getY() + 1f, target.getLocalTranslation().getZ());
                explosionNode.attachChild(explosionParticle);
                explosionParticle.emitAllParticles();
                explosionParticle.killAllParticles();
                ((CreepControl) target.getControl(0)).causeDamage(damage); 
                explosionNode.detachChild(explosionParticle);
                dispose = true;
                target = null;
            }
        } else {
            dispose = true;
            target = null;
        }

    }

    @Override
    public boolean isDisposed() {
        return this.dispose;
    }

    
    //For bullets to disappear

    public boolean isTargetDisposed() {
        return ((IGeometryDisposed) target.getControl(0)).isDisposed();
    }
}
