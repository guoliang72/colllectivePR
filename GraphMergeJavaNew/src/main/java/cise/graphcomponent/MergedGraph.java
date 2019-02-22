package cise.graphcomponent;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.jgrapht.graph.DirectedPseudograph;

/**
 * 表示一个融合图
 */
public class MergedGraph extends DirectedPseudograph<MergedVertex, MergedEdge> implements
    Serializable {

    public MergedGraph() {
        super(MergedEdge.class);
    }

//    @Override
//    public String toString() {
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("[");
//        for (MergedVertex mv : vertexSet()) {
//            stringBuilder.append(mv);
//        }
//        stringBuilder.append("]");
//        return stringBuilder.toString();
//    }


    /**
     * 判断两个融合图是否相同
     * @param mg1
     * @param mg2
     * @return
     */
    public static boolean isSame(MergedGraph mg1, MergedGraph mg2) {
        Set<MergedVertex> mVertexSet1 = mg1.vertexSet();
        Set<MergedVertex> mVertexSet2 = mg2.vertexSet();

        if (mVertexSet1.size() != mVertexSet2.size()) {
            return false;
        }


        // 判断依据是，每个mg1的融合节点都出现在了mg2中
        // 由于两个图包含一样的被融合点集合，所以不用反向判断
        // 判断融合节点相同，根据其包含的被融合节点集是否相同
        Set<Set<Vertex>> allVertexSet1 = new HashSet<>();
        Set<Set<Vertex>> allVertexSet2 = new HashSet<>();

        mVertexSet1.forEach(mv -> allVertexSet1.add(mv.vertexSet));
        mVertexSet2.forEach(mv -> allVertexSet2.add(mv.vertexSet));

        if (allVertexSet2.containsAll(allVertexSet1)) {
            return true;
        }

        return false;
    }
}
