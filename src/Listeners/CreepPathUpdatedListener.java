/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Listeners;

import java.util.List;
import pathfinder.Vertex;

/**
 *
 * @author adamv_000
 */
public interface CreepPathUpdatedListener {
    public void setPath(List<Vertex> path);
}
