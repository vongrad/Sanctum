/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder;

import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author adamv_000
 */
public class Vertex {

    private Vector3f center;
    private Vertex parent;
    private List<Neighbour> neighbours;
    private boolean visited;
    private boolean source;
    private boolean destination;
    private boolean obstacle;
    private int gCost;
    private int hCost;

    public Vertex(Vector3f center) {
        neighbours = new ArrayList<Neighbour>();
        visited = false;
        source = false;
        destination = false;
        obstacle = false;
        this.center = center;
    }

    public Vector3f getCenter() {
        return center;
    }

    public Vertex getParent() {
        return parent;
    }

    public boolean isObstacle() {
        return obstacle;
    }

    public void setObstacle(boolean obstacle) {
        this.obstacle = obstacle;
    }

    public void addNeighbour(String direction, Vertex vertex) {
        neighbours.add(new Neighbour(vertex, direction));
    }

    public boolean isVisited() {
        return visited;
    }

    public int getgCost() {
        return gCost;
    }

    public int gethCost() {
        return hCost;
    }

    public void setParent(Vertex parent) {
        this.parent = parent;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public void sethCost(int hCost) {
        this.hCost = hCost;
    }

    public void setgCost(int gCost) {
        this.gCost = gCost;
    }

    public boolean isSource() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;
    }

    public boolean isDestination() {
        return destination;
    }

    public void setDestination(boolean destination) {
        this.destination = destination;
    }

    public List<Neighbour> getNeighbours() {
        return neighbours;
    }
}
