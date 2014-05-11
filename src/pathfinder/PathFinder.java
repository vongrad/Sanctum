/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pathfinder;

import java.util.ArrayList;
import java.util.List;
import sun.misc.Queue;

/**
 *
 * @author adamv_000
 */
public class PathFinder {

    private ArrayList<Vertex> open;
    private ArrayList<Vertex> closed;

    public List<Vertex> calculate(Vertex[][] graph, int[] source, int[] destination) throws InterruptedException {

        open = new ArrayList<Vertex>();
        closed = new ArrayList<Vertex>();

        Vertex destVertex = null;

        Vertex current = graph[source[0]][source[1]];
        open.add(current);
        current.setgCost(0);

        while (!open.isEmpty() && destVertex == null) {

            //System.out.println("Looking: " + Utils.calculateGrid(current.getCenter())[0] + " " + Utils.calculateGrid(current.getCenter())[1]);
            current.setVisited(true);
            destVertex = calculateNext(current, destination);

            //closed.add(current);
            open.remove(current);
            current = findLowestFCost();

        }
        System.out.println("Dest Vertex: " + Utils.calculateGrid(destVertex.getCenter())[0] + " " + Utils.calculateGrid(destVertex.getCenter())[1]);
        return calculateFinalPath(destVertex);
    }

    private Vertex findLowestFCost() {

        Vertex lowest = open.get(0);

        for (Vertex vertex : open) {
            if (lowest.getgCost() + lowest.gethCost() > vertex.getgCost() + vertex.gethCost()) {
                lowest = vertex;
            }
        }
        return lowest;
    }

    private boolean vertexInList(Vertex vertex) {
        for (Vertex vertex1 : open) {
            if (vertex1.getCenter().equals(vertex.getCenter())) {
                return true;
            }
        }
        return false;
    }

    private int calculateHCost(int[] current, int[] destination) {
        return Math.abs(destination[0] - current[0]) + Math.abs(destination[1] - current[1]);
    }

    private Vertex calculateNext(Vertex current, int[] destination) {

        Neighbour closest = null;

        int hCost = 0;
        //System.out.println("Neighbours size: " + current.getNeighbours().size());
        for (Neighbour neighbour : current.getNeighbours()) {
            if (!neighbour.getVertex().isObstacle()) {

                if (neighbour.getVertex().isDestination()) {
                    neighbour.getVertex().setParent(current);
                    return neighbour.getVertex();
                }
                //if (neighbour.getVertex().isDestination()) {
                //System.out.println("Dest: " + Utils.calculateGrid(neighbour.getVertex().getCenter())[1]);
                //return current;
                //}

                hCost = calculateHCost(Utils.calculateGrid(neighbour.getVertex().getCenter()), destination);
                neighbour.getVertex().sethCost(hCost * 10);

                calculateGCost(neighbour, current);
                
                if (closest == null) {
                    closest = neighbour;
                    continue;
                }

                if (closest.getVertex().getgCost() + closest.getVertex().gethCost() > neighbour.getVertex().getgCost() + neighbour.getVertex().gethCost()) {
                    closest = neighbour;
                }
            }
        }


        for (Neighbour neighbour : current.getNeighbours()) {
            //System.out.println("Setting path for: |" + Utils.calculateGrid(neighbour.getVertex().getCenter())[0] + "," + Utils.calculateGrid(neighbour.getVertex().getCenter())[1] + "| to |" + Utils.calculateGrid(current.getCenter())[0] + "," + Utils.calculateGrid(current.getCenter())[1] + "|");
            
            if (neighbour.getVertex().getParent() == null){
                 neighbour.getVertex().setParent(current);
            }
            else{
//                if (neighbour.getVertex().getParent().getgCost() > current.getParent().getgCost()){
//                    neighbour.getVertex().setParent(current);
//                    
//                    calculateGCost(neighbour, current);
//               }
            }
           

            if (!vertexInList(neighbour.getVertex()) && neighbour.getVertex().isVisited() == false && !neighbour.getVertex().isObstacle()) {
                //if (!neighbour.getDirection().equals(closest.getDirection())) {
                open.add(neighbour.getVertex());
                //open.enqueue(neighbour.getVertex());
                //}
            }
        }
        //return closest.getVertex();
        return null;
    }
    
    private void calculateGCost(Neighbour neighbour, Vertex current){
        if (neighbour.getDirection().equals(Constants.NORTHEAST)
                        || neighbour.getDirection().equals(Constants.EASTSOUTH)
                        || neighbour.getDirection().equals(Constants.SOUTHWEST)
                        || neighbour.getDirection().equals(Constants.WESTNORTH)) {
                    neighbour.getVertex().setgCost(current.getgCost() + 14);
                } else {
                    neighbour.getVertex().setgCost(current.getgCost() + 10);
                }
    }

    private List<Vertex> calculateFinalPath(Vertex dest) {

        List<Vertex> shortestPath = new ArrayList<Vertex>();

        while (!dest.getParent().isSource()) {
            //System.out.println("Dest: " + Utils.calculateGrid(dest.getCenter())[0] + " " + Utils.calculateGrid(dest.getCenter())[1]);
            shortestPath.add(dest);
            dest = dest.getParent();
        }

        return shortestPath;
    }
}
