/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder;

import com.jme3.math.Vector3f;
import java.util.List;

/**
 *
 * @author adamv_000
 */
public class Graph {

    private int x;
    private int y;
    private Vertex[][] graph;
    private int[] sourceXY;
    private int[] destinationXY;

    public Graph(int x, int y) {
        this.x = x;
        this.y = y;
        graph = new Vertex[x][y];
    }

    public void initializeGraph() {
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                graph[i][j] = new Vertex(Utils.calculateCenter(i, j));
            }
        }
    }

    public void initializeEdges() {

        Vertex current;

        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                current = graph[i][j];

                if (!current.isObstacle()) {

                    if (j - 1 >= 0 && j - 1 < y) {
                        if (!graph[i][j - 1].isObstacle()) {
                            current.addNeighbour(Constants.NORTH, graph[i][j - 1]);
                        }
                    }
                    if (j + 1 >= 0 && j + 1 < y) {
                        if (!graph[i][j + 1].isObstacle()) {
                            current.addNeighbour(Constants.SOUTH, graph[i][j + 1]);
                        }
                    }
                    if (i + 1 >= 0 && i + 1 < x) {
                        if (!graph[i + 1][j].isObstacle()) {
                            current.addNeighbour(Constants.EAST, graph[i + 1][j]);
                        }
                    }
                    if (i - 1 >= 0 && i - 1 < x) {
                        if (!graph[i - 1][j].isObstacle()) {
                            current.addNeighbour(Constants.WEST, graph[i - 1][j]);
                        }
                    }
                    if (j - 1 >= 0 && j - 1 < y && i + 1 >= 0 && i + 1 < x) {
                        if (!graph[i + 1][j - 1].isObstacle()) {
                            current.addNeighbour(Constants.NORTHEAST, graph[i + 1][j - 1]);
                        }
                    }
                    if (j + 1 >= 0 && j + 1 < y && i + 1 >= 0 && i + 1 < x) {
                        if (!graph[i + 1][j + 1].isObstacle()) {
                            current.addNeighbour(Constants.EASTSOUTH, graph[i + 1][j + 1]);
                        }
                    }
                    if (j + 1 >= 0 && j + 1 < y && i - 1 >= 0 && i - 1 < x) {
                        if (!graph[i - 1][j + 1].isObstacle()) {
                            current.addNeighbour(Constants.SOUTHWEST, graph[i - 1][j + 1]);
                        }
                    }
                    if (j - 1 >= 0 && j - 1 < y && i - 1 >= 0 && i - 1 < x) {
                        if (!graph[i - 1][j - 1].isObstacle()) {
                            current.addNeighbour(Constants.WESTNORTH, graph[i - 1][j - 1]);
                        }
                    }
                }
            }
        }
    }
    
    public void setObstacles(List<int[]> obsatacles){
        for (int[] pos : obsatacles) {
            graph[pos[0]][pos[1]].setObstacle(true);
        }
    }

    public Vertex[][] getGraph() {
        return this.graph;
    }

    public Vertex getVertex(int x, int y) {
        return this.graph[x][y];
    }

    public int[] getSourceXY() {
        return sourceXY;
    }

    public void setObsticle(int[] pos) {
        this.graph[pos[0]][pos[1]] = null;
    }

    public void setSourceXY(int[] sourceXY) {
        graph[sourceXY[0]][sourceXY[1]].setSource(true);
        this.sourceXY = sourceXY;
    }

    public int[] getDestinationXY() {
        return destinationXY;
    }

    public void setDestinationXY(int[] destinationXY) {
        graph[destinationXY[0]][destinationXY[1]].setDestination(true);
        this.destinationXY = destinationXY;
    }
}
