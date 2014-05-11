/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

/**
 *
 * @author adamv_000
 */
public class ShapeBuilder {
    
    private AssetManager assetManager;

    public ShapeBuilder(AssetManager assetManager) {
        this.assetManager = assetManager;
    }
    
    public Geometry generateBox(String name, float x, float y, float z, String matPath, ColorRGBA color, Vector3f pos) {
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
    
     public Geometry generateBullet(String name, int zSamples, int radialSamples, float radius, String matPath, ColorRGBA color, Vector3f pos) {
        Sphere sphere = new Sphere(zSamples, radialSamples, radius);
        Geometry geom = new Geometry(name, sphere);

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
}
