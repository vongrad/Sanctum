/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder;

import com.jme3.math.Vector3f;

/**
 *
 * @author adamv_000
 */
public class Utils {

    public static Vector3f calculateCenter(int x, int y) {
        return new Vector3f((float) x - 80f + 0.5f, 0f, 50f - (float) y - 0.5f);
    }

    public static int[] calculateGrid(Vector3f center) {
        return new int[]{(int) (center.getX() + 80 - 0.5), (int) (center.getZ() + 50 + 0.5)};
    }
}
