package mygame;

import gameStates.GameState;
import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import org.lwjgl.opengl.Display;

/**
 * test
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    public static void main(String[] args) {
        Main app = new Main();
        app.start();      
    }

    @Override
    public void simpleInitApp() {
        initCrossHairs();
        Display.setVSyncEnabled(true);
        GameState gameState = new GameState();
        stateManager.attach(gameState);  
        flyCam.setMoveSpeed(30);
        
        cam.setLocation(new Vector3f(0.0f, 20.0f, 0.0f));
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    protected void initCrossHairs() {
        setDisplayStatView(false);
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+");
        ch.setLocalTranslation(settings.getWidth() / 2 - ch.getLineWidth() / 2, settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
