package cise.mergeinfo;

import cise.ga.GAProcess;
import cise.graphcomponent.Graph;
import cise.graphcomponent.MergedGraph;
import cise.graphcomponent.MergedVertex;
import cise.graphcomponent.Vertex;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class VertexPair {
    Vertex v1;
    Vertex v2;
    public VertexPair(Vertex v1, Vertex v2) {
        this.v1 = v1;
        this.v2 = v2;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VertexPair that = (VertexPair) o;

        // 顺序无关
        if (v1.equals(that.v1) && v2.equals(that.v2)) {
            return true;
        }
        return v1.equals(that.v2) && v2.equals(that.v1);
    }

    @Override
    public int hashCode() {
        int result = v1.hashCode() + v2.hashCode();
        return result;
    }
}

public class VertexSimilarity {
    private static Map<VertexPair, Double> similarityMap = new ConcurrentHashMap<>();


    public static void reset() {
        similarityMap.clear();
    }

    /**
     * 计算两个节点相似度
     * @param v1
     * @param v2
     * @return
     */
    public static double calcSimilarity(Vertex v1, Vertex v2) {
        if (v1.equals(v2)) {
            return 1.0;
        }

        if ((v1.type.value.equals("VAR") && v2.type.value.equals("ARRVAR")) ||
            (v2.type.value.equals("VAR") && v1.type.value.equals("ARRVAR")) ) {
            return 0.5;
        }

        if (!v1.type.equals(v2.type)) {
            return 0.0;
        }

        VertexPair vpair = new VertexPair(v1, v2);
        if (similarityMap.containsKey(vpair)) {
            return similarityMap.get(vpair);
        }

        double sim = 0.0;

        // 根据节点类型使用不同判断方法
        switch (v1.type.value) {
            // 无条件相似的
            case "ENTRY":
            case "ASSIGNOP":
            case "VAR":
            case "ARRVAR":
            case "RETURN":
            case "EXPRS":{
                sim = 1.0;
                break;
            }
            // 根据节点内容判断相似的
            case "OP":
            case "CONST":
            case "FUNCNAME": {
                sim = v1.name.equals(v2.name) ? 1.0 : 0.0;
                break;
            }

            // DECL节点根据类型判断相似
            case "DECL": {
                if (v1.varType.equals(v2.varType)) {
                    sim = 1.0;
                }
                else if (v1.varType.startsWith(v2.varType) || v2.varType.startsWith(v1.varType)) {
                    // 例如int 和 int[]
                    sim = 0.5;
                }
                else {
                    sim = 0.0;
                }
                break;
            }

            // 根据节点结构判断相似性
            case "ASSIGN":
            case "CONTROL":
            case "CALL": {
                sim = mergeSimilarity(v1, v2);
                break;
            }
            default:
                sim = 0.0;
        }
        similarityMap.put(vpair, sim);
        return sim;

    }

    /**
     * 用融合的方法计算两个节点AST的相似度
     * @param v1
     * @param v2
     * @return
     */
    private static Double mergeSimilarity(Vertex v1, Vertex v2) {
        if (v1.astSubgraph == null && v2.astSubgraph == null) {
            return 1.0;
        }
        if (v1.astSubgraph == null || v2.astSubgraph == null) {
            return 0.0;
        }
        // 开一个极简GA
        List<Graph> graphList = Arrays.asList(v1.astSubgraph, v2.astSubgraph);
        GAProcess ga = new GAProcess(50, 0.1, 100, 10, graphList);
        ga.parallel = false;

        MergedGraphInfo bestResultInfo = ga.Run();
        MergedGraph bestResult = bestResultInfo.getMergedGraph();
        // 相似度 = 0.5 * 2*(融合了的节点的相似度) / 总节点数 + 0.5 * 2 * (总边数 - 总融合边数) / 总边数
        double totalSim = 0.0;
        for (MergedVertex mv : bestResult.vertexSet()) {
            if (mv.getAllVertex().size() >= 2) {
                List<Vertex> vList= new ArrayList<>(mv.getAllVertex());
                totalSim += VertexSimilarity.calcSimilarity(vList.get(0), vList.get(1));
            }
        }
        totalSim = 0.5 * 2 * totalSim / (v1.astSubgraph.vertexSet().size() + v2.astSubgraph.vertexSet().size());
        int totalEdge = v1.astSubgraph.edgeSet().size() + v2.astSubgraph.edgeSet().size();
        totalSim += 0.5 * 2 * (totalEdge - bestResult.edgeSet().size()) / totalEdge;
//        if (Double.isNaN(totalSim)) {
//            System.out.println("????????????");
//        }
        assert totalSim >= 0 && totalSim <= 1.0;

//        System.out.println("Sim: " + v1.name + " : " + v2.name + " : " + totalSim + " Entropy: " + bestResultInfo.entropy);

        return totalSim;
    }

    /**
     * AST
     * @param v1
     * @param v2
     * @return
     */
    private static Double astSimilarity(Vertex v1, Vertex v2) {
        if (v1.astSubgraph == null && v2.astSubgraph == null) {
            return 1.0;
        }
        if (v1.astSubgraph == null || v2.astSubgraph == null) {
            return 0.0;
        }

        return 1.0;
    }
}
