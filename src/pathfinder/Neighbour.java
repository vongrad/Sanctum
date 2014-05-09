/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder;

/**
 *
 * @author adamv_000
 */
public class Neighbour {

    private Vertex vertex;
    private String direction;

    public Neighbour(Vertex north, String direction) {
        this.vertex = north;
        this.direction = direction;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public void setVertex(Vertex vertex) {
        this.vertex = vertex;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
