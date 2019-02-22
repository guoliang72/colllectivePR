package cise.mergeinfo;

import cise.graphcomponent.Edge;
import cise.graphcomponent.Graph;
import cise.graphcomponent.Vertex;
import cise.utils.StringType;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 存放一组待融合图的信息的类
 */
public class GraphsInfo implements Serializable{

    /**
     * 存放所有待融合图
     */
    protected Set<Graph> graphSet;

    /**
     * 存放待融合图中所有节点类型
     */
    Set<StringType> vertexTypeSet;

    /**
     * 存放所有待融合图的边类型
     */
    Set<StringType> edgeTypeSet;

    /**
     * 节点类型到对应节点集合的映射
     */
    private Map<StringType, Set<Vertex>> typeToVertexSetMap;

    /**
     * 构造函数
     *
     */
    public GraphsInfo(Collection<Graph> graphs) {

        graphSet = new HashSet<>(graphs);

        vertexTypeSet = new HashSet<>();
        edgeTypeSet = new HashSet<>();
        typeToVertexSetMap = new HashMap<>();

        // 更新上面那些集合的信息
        for (Graph g : graphSet) {
            for (Vertex v : g.vertexSet()) {
                vertexTypeSet.add(v.type);
                if (!typeToVertexSetMap.containsKey(v.type)) {
                    typeToVertexSetMap.put(v.type, new HashSet<>());
                }
                typeToVertexSetMap.get(v.type).add(v);
            }
            for (Edge e : g.edgeSet()) {
                edgeTypeSet.add(e.type);
            }
        }
    }

    /**
     * 获得某个类型的节点总数
     *
     * @param type 指定的节点类型
     * @return 该类型节点总数
     */
    public int getVertexNumByType(StringType type) {
        // 不存在该类型，返回0
        if (!typeToVertexSetMap.containsKey(type)) {
            return 0;
        }
        int ret = 0;
        for (Vertex v : typeToVertexSetMap.get(type)) {
                // 这个判断是冗余的，作为保险
                if (v.type.equals(type)) {
                    ret++;
                }

        }
        return ret;
    }

    /**
     * 给定节点类型，返回所有待融合图中该类型节点的集合
     *
     * @param type 查找类型
     * @return 指定类型的节点集合
     */
    public Set<Vertex> getVertexSetsByType(StringType type) {
        if (typeToVertexSetMap.containsKey(type)) {
            return typeToVertexSetMap.get(type);
        }
        return new HashSet<>();
    }

    public int getGraphNum() {
        return graphSet.size();
    }
}
