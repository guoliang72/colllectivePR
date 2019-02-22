package cise.graphcomponent;

import cise.utils.StringType;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 融合图中的节点
 */
public class MergedVertex implements Serializable{

    Set<Vertex> vertexSet;

    public StringType type;
    public int id = -1;
    public String name = "";
    /**
     * 所属的融合图
     */
    public MergedGraph mergedGraph;

    public MergedVertex(StringType type, MergedGraph mergedGraph) {
        this.type = type;
        this.mergedGraph = mergedGraph;
        this.vertexSet = new HashSet<>();
    }

    public MergedVertex(MergedVertex another) {
        this.type = another.type;
        this.mergedGraph = another.mergedGraph;
        this.vertexSet = new HashSet<>(another.vertexSet);
    }

    /**
     * 将一个待融合图节点添加到这个融合节点
     *
     * @param v 待融合图中节点
     */
    public void addVertex(Vertex v) {
        vertexSet.add(v);
    }

    /**
     * 将一组待融合图节点添加到这个融合节点
     *
     * @param vertexCollection 待融合图节点的容器
     */
    public void addAllVertex(Collection<Vertex> vertexCollection) {
        vertexSet.addAll(vertexCollection);
    }

    public Set<Vertex> getAllVertex() {
        return vertexSet;
    }

    public void removeVertex(Vertex v) {
        this.vertexSet.remove(v);
    }

    public MergedGraph getGraph() {
        return mergedGraph;
    }

    /**
     * 找到当前融合节点中，来自指定待融合图的待融合节点
     * @param g
     * @return
     */
    public Vertex getVertexByGraph(Graph g) {
        for (Vertex v : vertexSet) {
            if (v.graph.equals(g)) {
                return v;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (Vertex v : vertexSet) {
            stringBuilder.append(v).append(", ");
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
