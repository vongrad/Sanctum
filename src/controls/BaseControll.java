/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controls;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author adamv_000
 */
public class BaseControll extends AbstractControl implements IGeometryDisposed, IBase {

    private int health;
    private boolean destroyed;

    public BaseControll(int health) {
        this.health = health;
        destroyed = false;
    }

    @Override
    protected void controlUpdate(float tpf) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public boolean isDisposed() {
        return destroyed;
    }

    public void causeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            destroyed = true;
        }
    }

    public int getHealth() {
        return health;
    }
}
