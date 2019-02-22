package cise.graphcomponent;


import cise.utils.StringType;
import java.io.Serializable;

/**
 * 节点类
 */
public class Vertex implements Comparable<Vertex>, Serializable{

    public StringType type;
    public String id;
    public String name;
    public int line = -1;
    // 节点所属的图
    public Graph graph;

    // DECL类型专用
    public String varType;
    public String varName;

    // PDG节点的AST子图
    public Graph astSubgraph = null;

    /**
     * 默认构造函数 缺省情况下id为""
     */
    public Vertex() {
        new Vertex(StringType.defult(), "", "", Graph.DEFAULT);
    }

    public Vertex(StringType type, String id, String name, Graph g) {
        this.type = type;
        this.id = id == null ? "" : id;
        this.name = name;
        this.graph = g == null? Graph.DEFAULT: g;
    }
    

    @Override
    public String toString() {
        return this.graph.id + "." + this.id + " line:" + this.line;
    }

    @Override
    public int compareTo(Vertex o) {
        if (o.graph.equals(o.graph)) {
            return this.id.compareTo(o.id);
        }
        return this.graph.compareTo(o.graph);
    }
}
