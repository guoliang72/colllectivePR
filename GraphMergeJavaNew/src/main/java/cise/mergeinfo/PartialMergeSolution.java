package cise.mergeinfo;

import cise.graphcomponent.Vertex;
import cise.utils.StringType;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 在融合方案片段中，被标记融合在一起的一组节点的集合。 它们拥有共同的类型<code>type</code>
 */
class PartialMergeVertexSet implements Serializable{

    StringType type;
    Set<Vertex> vertexSet = new HashSet<>();

    public PartialMergeVertexSet(StringType type) {
        this.type = type;
    }
}

/**
 * 融合方案片段 <p> 一个融合方案片段是一个集合，集合中的每个元素是一个节点集合。每个节点集合中的节点在融合图中必须属于同一个融合节点 </p>
 */
public class PartialMergeSolution implements Serializable {

    private Set<PartialMergeVertexSet> allVertexSets;
    private Map<StringType, Set<PartialMergeVertexSet>> typeToVertexSetMap;

    /**
     * 构造函数
     */
    public PartialMergeSolution() {
        allVertexSets = new HashSet<>();
        typeToVertexSetMap = new HashMap<>();
    }

    public static PartialMergeSolution EMPTY = new PartialMergeSolution();

    /**
     * 当前融合方案片段的大小
     */
    public int getSize() {
        return allVertexSets.size();
    }

    public Set<PartialMergeVertexSet> getAllVertexSets() {
        return allVertexSets;
    }
}
