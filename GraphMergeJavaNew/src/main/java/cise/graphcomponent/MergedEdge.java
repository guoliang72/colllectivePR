package cise.graphcomponent;

import cise.utils.StringType;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 融合图中的边
 */
public class MergedEdge implements Serializable{

    public StringType type;
    Set<Edge> edgeSet;

    private MergedVertex source;
    private MergedVertex target;

    private MergedGraph mergedGraph;

    public MergedEdge(StringType type, MergedGraph mergedGraph, MergedVertex source,
        MergedVertex target) {
        this.type = type;
        this.mergedGraph = mergedGraph;
        this.source = source;
        this.target = target;
        edgeSet = new HashSet<>();
    }

    /**
     * 将一个待融合图中的边添加到这个融合边
     */
    public void addEdge(Edge e) {
        edgeSet.add(e);
    }

    /**
     * 将一组待融合图中的边添加到这个融合边
     */
    public void addAllEdge(Collection<Edge> edgeCollection) {
        edgeSet.addAll(edgeCollection);
    }

    /**
     * 这个融合边所在的融合图
     */
    public MergedGraph getMergedGraph() {
        return mergedGraph;
    }

    public Set<Edge> getEdgeSet() {
        return this.edgeSet;
    }
}
