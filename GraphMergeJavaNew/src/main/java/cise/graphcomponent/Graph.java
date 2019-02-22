package cise.graphcomponent;

import java.io.Serializable;
import org.jgrapht.graph.DefaultDirectedGraph;

/**
 * 表示待融合图的类
 */
public class Graph extends DefaultDirectedGraph<Vertex, Edge> implements Comparable<Graph>,
    Serializable{

    public final int id;
    public String name;

    public static Graph DEFAULT = new Graph(-1);

    public Graph() {
        super(Edge.class);
        this.id = getId();
    }

    public Graph(int id) {
        super(Edge.class);
        this.id = id;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (o == null || getClass() != o.getClass()) {
//            return false;
//        }
//        if (!super.equals(o)) {
//            return false;
//        }
//
//        Graph graph = (Graph) o;
//
//        return id == graph.id;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = super.hashCode();
//        result = 31 * result + id;
//        return result;
//    }

    public static int idPool = 0;

    public static int getId() {
        int ret = idPool;
        idPool++;
        return ret;
    }




    @Override
    public int compareTo(Graph o) {
        return this.id - o.id;
    }
}
