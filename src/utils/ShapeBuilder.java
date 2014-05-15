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
import com.jme3.util.TangentBinormalGenerator;

/**
 *
 * @author adamv_000
 */
public class ShapeBuilder {

    private AssetManager assetManager;

    public ShapeBuilder(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public Geometry generateBox(String name, float x, float y, float z, String matPath, String texturePath, ColorRGBA color, Vector3f pos) {
        Box box = new Box(x, y, z);
        Geometry geom = new Geometry(name, box);

        Material mat;

        if (matPath == null) {
            mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        } else {
            mat = new Material(assetManager, matPath);
        }
        if (texturePath != null) {
            TangentBinormalGenerator.generate(geom);           // for lighting effect
            mat.setTexture("DiffuseMap",
                    assetManager.loadTexture(texturePath));
            mat.setBoolean("UseMaterialColors", true);
            mat.setColor("Diffuse", ColorRGBA.White);
            mat.setColor("Specular", ColorRGBA.White);
            mat.setFloat("Shininess", 64f);
        }
        else if (color != null) {
            mat.setColor("Color", color);
        }
        geom.setMaterial(mat);
        geom.setLocalTranslation(pos);
        return geom;
    }

    public Geometry generateBullet(String name, int zSamples, int radialSamples, float radius, String matPath, ColorRGBA color, Vector3f pos) {
        Sphere sphere = new Sphere(zSamples, radialSamples, radius);
        Geometry geom = new Geometry(name, sphere);
        sphere.setTextureMode(Sphere.TextureMode.Projected);
        TangentBinormalGenerator.generate(sphere);
        
        Material mat;

        if (matPath == null) {
            mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
           
        } else {
            mat = new Material(assetManager, matPath);
            mat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Rock/Pond.jpg"));
            mat.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Rock/Pond_normal.png"));
            mat.setBoolean("UseMaterialColors",true);
        }
        
        if (color != null) {
            mat.setColor("Diffuse", color);
            mat.setColor("Specular",color);
        }
        mat.setFloat("Shininess", 64f);
        geom.setMaterial(mat);
        geom.setLocalTranslation(pos);
        return geom;
    }
}
