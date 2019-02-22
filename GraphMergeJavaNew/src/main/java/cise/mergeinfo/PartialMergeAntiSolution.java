package cise.mergeinfo;

import cise.graphcomponent.Vertex;
import cise.utils.Result;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

class AntiMergePair implements Serializable{

    Vertex v1;
    Vertex v2;
}

/**
 * 反融合方案片段。 <p> 一个反融合方案片段是一个集合，集合中的元素是二元组。每个二元组是来自两个不同待融合图的两个节点，这两个节点不能包含在同一个融合节点中 </p>
 */
public class PartialMergeAntiSolution implements Serializable{

    Set<AntiMergePair> antiMergePairSet;

    public PartialMergeAntiSolution() {
        antiMergePairSet = new HashSet<>();
    }

    /**
     * 反融合方案片段中，不能融合的节点对个数
     */
    public int getSize() {
        return antiMergePairSet.size();
    }

    /**
     * 给定一个节点，查找在这个反融合方案片段中，与给定节点冲突的节点的集合
     *
     * @param v 待查找节点
     * @return 与给定节点冲突的节点集合，不包含自身
     */
    public Set<Vertex> getConflictVertexSet(Vertex v) {
        Set<Vertex> retSet = new HashSet<>();
        // 遍历每个pair查找
        for (AntiMergePair pair : antiMergePairSet) {
            if (pair.v1.equals(v)) {
                retSet.add(pair.v2);
            }
            if (pair.v2.equals(v)) {
                retSet.add(pair.v1);
            }
        }
        return retSet;
    }

    public static PartialMergeAntiSolution EMPTY = new PartialMergeAntiSolution();

    /**
     * 检查一个反融合方案片段的正确性 <p> 反融合方案片段合理性的检查准则包括如下3条: <ol> <li>每个节点对包含的节点必须是所在图的合法节点</li>
     * <li>每个节点对包含的节点必须具有相同的类型 </li> <li>每个节点对包含的节点不能来自同一个图 </li> </ol> </p>
     *
     * @param partialMergeAntiSolution 待检查的片段
     * @param graphsInfo 待融合图信息
     * @return 是否合法
     */
    public static Result isValid(final PartialMergeAntiSolution partialMergeAntiSolution,
        final GraphsInfo graphsInfo) {
        if (partialMergeAntiSolution.getSize() == 0) {
            return Result.success();
        }

        StringBuilder msgBuilder = new StringBuilder();

        // 遍历集合中每一个二元组
        for (AntiMergePair pair : partialMergeAntiSolution.antiMergePairSet) {
            Vertex v1 = pair.v1;
            Vertex v2 = pair.v2;
            // 准则3: 每个节点对包含的节点不能来自同一个图
            if (v1.graph.equals(v2.graph)) {
                msgBuilder.append("PartialMergeAntiSolution.isValid: ");
                msgBuilder.append("一个节点对中的两个节点来自同一个图");
                return Result.failed(msgBuilder.toString());
            }

            //准则1： 每个节点对包含的节点必须是所在图的合法节点
            if (!v1.graph.containsVertex(v1) || !v2.graph.containsVertex(v2)) {
                msgBuilder.append("PartialMergeAntiSolution.isValid: ");
                msgBuilder.append("存在不合法节点");
                return Result.failed(msgBuilder.toString());
            }

            // 准则2：每个节点对包含的节点必须具有相同的类型
            if (!v1.type.equals(v2.type)) {
                msgBuilder.append("PartialMergeAntiSolution.isValid: ");
                msgBuilder.append("一个节点对中的两个节点具有不同的类型");
                return Result.failed(msgBuilder.toString());
            }
        }

        return Result.success();
    }
}
