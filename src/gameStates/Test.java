/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gameStates;

import com.jme3.math.Vector3f;
import pathfinder.Utils;

/**
 *
 * @author adamv_000
 */
public class Test {

    public static void main(String[] args) {
        Vector3f v = new Vector3f(-79.36f, 10f, 42f);
        int[] grid = Utils.calculateGrid(v);
        System.out.println("X: " + grid[0] + " Y: " + grid[1]);
    }
}
