package cise.graphcomponent;

import cise.utils.StringType;
import java.io.Serializable;

public class Edge implements Serializable{

    public Vertex source;
    public Vertex target;
    // 所属的图
    public Graph graph;
    public StringType type;

    public Edge(Vertex src, Vertex tgt, StringType type, Graph g) {
        source = src;
        target = tgt;
        this.type = type;
        graph = g;
        //type = new StringType("");
    }

    public Vertex getSource() {
        return source;
    }

    public Vertex getTarget() {
        return target;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (o == null || getClass() != o.getClass()) {
//            return false;
//        }
//
//        Edge edge = (Edge) o;
//
//        if (!source.equals(edge.source)) {
//            return false;
//        }
//        if (!target.equals(edge.target)) {
//            return false;
//        }
//        if (!graph.equals(edge.graph)) {
//            return false;
//        }
//        return type.equals(edge.type);
//    }
//
//    @Override
//    public int hashCode() {
//        int result = source.hashCode();
//        result = 31 * result + target.hashCode();
//        if (graph != null) {
//            result = 31 * result + graph.hashCode();
//        }
//        result = 31 * result + type.hashCode();
//        return result;
//    }

    @Override
    public String toString() {
        return "(" + this.source.id + "-" + this.type + "->" + this.target.id + ")";
    }

}
