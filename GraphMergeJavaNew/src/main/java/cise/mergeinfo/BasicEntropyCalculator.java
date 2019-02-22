package cise.mergeinfo;

import cise.graphcomponent.Edge;
import cise.graphcomponent.Graph;
import cise.graphcomponent.MergedEdge;
import cise.graphcomponent.MergedVertex;
import cise.graphcomponent.Vertex;
import cise.utils.StringType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BasicEntropyCalculator implements Entropy {

    private boolean ignoreEdgeType = false;

    private int entropyFunctionType = 0;

    public BasicEntropyCalculator() {

    }


    public BasicEntropyCalculator(boolean ignoreEdgeType) {
        this.ignoreEdgeType = ignoreEdgeType;
    }

    @Override
    public double calculateEntropy(MergedGraphInfo mergedGraphInfo) {

        if (!mergedGraphInfo.isChanged) {
            return mergedGraphInfo.getEntropy();
        }

        // 熵计算时的一个常量
        int DLT = mergedGraphInfo.getGraphsInfo().getGraphNum();

        // 初始化熵
        double finalEntropy = 0.0;

        // 之后计算用到的一些常量
        // 融合图的边数
        int edgeNum = mergedGraphInfo.getMergedGraph().edgeSet().size();

        // 遍历每个融合节点，计算熵
        Set<MergedVertex> vertices = mergedGraphInfo.getMergedGraph().vertexSet();
        for (MergedVertex mv : vertices) {

            // 获得当前融合节点中，包含的待融合图节点所在的图集合
            // 即：引用当前融合节点的被融合图集合
            Set<Graph> graphInThisMV = new HashSet<>();
//            mv.getAllVertex().forEach(v -> graphInThisMV.add(v.graph));
            for (Vertex v : mv.getAllVertex()) {
                graphInThisMV.add(v.graph);
            }

            double currentEntropy = 0.0;

            // 当前融合节点所有入边的类型集合
            Set<StringType> inEdgeTypeSet = new HashSet<>();
//            mergedGraphInfo.getMergedGraph().incomingEdgesOf(mv).forEach(me -> inEdgeTypeSet.add(me.type));
            // 上面的lambda表达式等价与下面的循环，下同
            for (MergedEdge me : mergedGraphInfo.getMergedGraph().incomingEdgesOf(mv)) {
                inEdgeTypeSet.add(me.type);
            }

            // 当前融合节点所有出边类型集合
            Set<StringType> outEdgeTypeSet = new HashSet<>();
            // mergedGraphInfo.getMergedGraph().outgoingEdgesOf(mv).forEach(me -> outEdgeTypeSet.add(me.type));
            for (MergedEdge me : mergedGraphInfo.getMergedGraph().outgoingEdgesOf(mv)) {
                outEdgeTypeSet.add(me.type);
            }

            // 如果不考虑边类型，计算所有
            if (this.ignoreEdgeType) {
                // 计算当前融合点
                currentEntropy = calculateEdgeEntropyForVertex(mergedGraphInfo, mv, graphInThisMV, StringType.defult());

            }
            else {
                // 分别计算每个类型入边和出边的熵
                for (StringType inType : inEdgeTypeSet) {
                    currentEntropy += calculateEdgeEntropyForVertex(mergedGraphInfo, mv, graphInThisMV, inType, EdgeType.IN);
                }
                for (StringType outType : outEdgeTypeSet) {
                    currentEntropy += calculateEdgeEntropyForVertex(mergedGraphInfo, mv, graphInThisMV, outType, EdgeType.OUT);
                }
            }

            currentEntropy += 5 * calculateVertexContentEntropy(mergedGraphInfo, graphInThisMV, mv);
//            if (Double.isNaN(currentEntropy)) {
//                System.out.println("?????????????");
//            }
            // 更新图的熵
            //finalEntropy += (currentEntropy + DLT) / graphInThisMV.size() * edgeNum;

            // 测试新的熵算法
            double delta = Math.log(DLT) / Math.log(2);
            delta = delta <= 2.0 ? 2.0 : delta;
            // 形式2
            finalEntropy += (currentEntropy + delta - Math.pow(delta, (double)graphInThisMV.size() / DLT)) * edgeNum;
//            System.out.println(finalEntropy);
//            if (Double.isNaN(finalEntropy)) {
//                System.out.println("?????????????");
//            }

        }

        mergedGraphInfo.isChanged = false;



        return finalEntropy;
    }

    /**
     * 计算融合节点自身的熵，这个熵不考虑节点相关的边，只计算来自融合节点自身内容的不一致
     * @return
     */
    private double calculateVertexContentEntropy(MergedGraphInfo mergedGraphInfo, Set<Graph> graphInThisMv, MergedVertex mergedVertex) {



        int graphNum = graphInThisMv.size();

        double entropy = 0.0;

        for (Vertex vX : mergedVertex.getAllVertex()) {
            double pX = 1.0 / graphNum;
            double rstSumPySxy = 0.0;
            for (Vertex vY : mergedVertex.getAllVertex()) {
                double pY = 1.0 / graphNum;
                double sXY = VertexSimilarity.calcSimilarity(vX, vY);
                if (vX.line == vY.line && sXY < 1.0) {
                    int i = 0;
                }
                rstSumPySxy += pY * sXY;
            }
            entropy += pX * Math.log(rstSumPySxy) / Math.log(2);
        }

       //  System.out.println(mergedVertex + " " + entropy);

        return Math.abs(entropy);
    }

    /**
     * 给定一个节点，计算这个节点的入边与出边的熵，
     * @param mergedGraphInfo
     * @param mergedVertex
     * @return
     */
    private double calculateEdgeEntropyForVertex(MergedGraphInfo mergedGraphInfo,
                                                 MergedVertex mergedVertex,
                                                 Set<Graph> graphInThisMV,
                                                 StringType type) {
        return calculateEdgeEntropyForVertex(mergedGraphInfo, mergedVertex, graphInThisMV, type, EdgeType.ALL);
    }

    /**
     * 给定一个融合节点，计算这个节点指定类型的边（入边或出边）的熵
     * @param mergedGraphInfo
     * @param mergedVertex
     * @param edgeInOrOut
     * @return
     */
    private double calculateEdgeEntropyForVertex(MergedGraphInfo mergedGraphInfo,
                                                 MergedVertex mergedVertex,
                                                 Set<Graph> graphInThisMV,
                                                 StringType type,
                                                 EdgeType edgeInOrOut) {

        if (edgeInOrOut.equals(EdgeType.ALL)) {
            double inEntropy = calculateEdgeEntropyForVertex(mergedGraphInfo, mergedVertex, graphInThisMV, type, EdgeType.IN);
            double outEntropy = calculateEdgeEntropyForVertex(mergedGraphInfo, mergedVertex, graphInThisMV, type, EdgeType.OUT);
            return inEntropy + outEntropy;
        }

        /**
         * 每个被融合图引用的融合边集合
         */
        Map<Graph, Set<MergedEdge>> graphToReferencedMergedEdgeSetMap = new HashMap<>();
        // 方法是：对于每个融合边，得到其包含的被融合边所在的被融合图集合，分别向每个被融合图的引用融合边集合中添加当前融合边

        // 根据入边、出边、是否忽略类型不同，在计算时考虑的融合边集合也不同
        Set<MergedEdge> targetMergedEdgeSet = new HashSet<>();

        if (edgeInOrOut.equals(EdgeType.IN)) {
            Set<MergedEdge> inEdgeSet = mergedGraphInfo.getMergedGraph().incomingEdgesOf(mergedVertex);
            for (MergedEdge me : inEdgeSet) {
                if (this.ignoreEdgeType || me.type.equals(type)) {
                    targetMergedEdgeSet.add(me);
                }
            }
        }

        else if (edgeInOrOut.equals(EdgeType.OUT)) {
            Set<MergedEdge> outEdgeSet = mergedGraphInfo.getMergedGraph().outgoingEdgesOf(mergedVertex);
            for (MergedEdge me : outEdgeSet) {
                if (this.ignoreEdgeType || me.type.equals(type)) {
                    targetMergedEdgeSet.add(me);
                }
            }
        }

        // 对于目标集合中的每个融合边，
        for (MergedEdge me : targetMergedEdgeSet) {
            // 找到其包含的被融合边集合
            Set<Edge> edgeSet = me.getEdgeSet();
            // 找到这些被融合边来自的被融合图的集合
            Set<Graph> referencedGraphSet = new HashSet<>();
            //edgeSet.forEach(edge -> referencedGraphSet.add(edge.graph));
            for (Edge edge : edgeSet) {
                referencedGraphSet.add(edge.graph);
            }

            // 对于找到的每个被融合图
            for (Graph graph : referencedGraphSet) {
                // 将当前的融合边添加到它引用的融合边集合中
                if (!graphToReferencedMergedEdgeSetMap.containsKey(graph)) {
                    graphToReferencedMergedEdgeSetMap.put(graph, new HashSet<>());
                }
                graphToReferencedMergedEdgeSetMap.get(graph).add(me);
            }
        }

        // 逆置这个映射，对于每个“融合边的组合”，计算引用了这个组合的被融合图的集合
        Map<Set<MergedEdge>, Set<Graph>> mergedEdgeSetToReferenceGraphSetMap = new HashMap<>();

        Set<MergedEdge> emptyEdgeSet = new HashSet<>();

        for (Graph graph : graphInThisMV) {
            Set<MergedEdge> mergedEdges;
            mergedEdges = graphToReferencedMergedEdgeSetMap.getOrDefault(graph, emptyEdgeSet);
            if (!mergedEdgeSetToReferenceGraphSetMap.containsKey(mergedEdges)) {
                mergedEdgeSetToReferenceGraphSetMap.put(mergedEdges, new HashSet<>());
            }
            mergedEdgeSetToReferenceGraphSetMap.get(mergedEdges).add(graph);

        }

//        graphToReferencedMergedEdgeSetMap.forEach((graph, mergedEdges) -> {
//            if (!mergedEdgeSetToReferenceGraphSetMap.containsKey(mergedEdges)) {
//                mergedEdgeSetToReferenceGraphSetMap.put(mergedEdges, new HashSet<>());
//            }
//            mergedEdgeSetToReferenceGraphSetMap.get(mergedEdges).add(graph);
//        });

        // 到这里，mergedEdgeSetToReferenceGraphSetMap映射中
        // key是被待融合图引用的每一种融合边的组合
        // value是引用这种组合的图的集合

//        // 获得当前融合节点中，包含的待融合图节点所在的图集合
//        // 即：引用当前融合节点的被融合图集合
//        Set<Graph> graphInThisMV = new HashSet<>();
//        mergedVertex.getAllVertex().forEach(v -> graphInThisMV.add(v.graph));
        int graphNum = graphInThisMV.size();


        /*
         * 下面是计算熵的数学部分
         * 公式是SUM(x in allEdgeSet, p(x) * log(SUM(y_in_allEdgeSet, p(y) * s(x,y))))
         */

        double entropy = 0;

        Set<Entry<Set<MergedEdge>, Set<Graph>>> allEdgeComb = mergedEdgeSetToReferenceGraphSetMap.entrySet();

        for (Entry<Set<MergedEdge>, Set<Graph>> edgeCombinationX : allEdgeComb) {
            Set<Graph> usersX = edgeCombinationX.getValue();
            double pX = ((double)usersX.size()) / graphNum;

            double rstSumPySxy = 0.0;

            for (Entry<Set<MergedEdge>, Set<Graph>> edgeCombinationY : allEdgeComb) {
                Set<Graph> usersY = edgeCombinationY.getValue();
                double pY = ((double)usersY.size())/ graphNum;

                double similarity;

                // 如果两个融合边组合是一样的，则相似度为1
                if (edgeCombinationX.equals(edgeCombinationY)) {
                    similarity = 1.0;
                }
                // 如果其中一方为空，则相似度为0
                else if (edgeCombinationX.getKey().isEmpty() || edgeCombinationY.getKey().isEmpty()) {
                    similarity = 0;
                }
                // 否则，二者相似度等于 交集大小/并集大小
                else {
                    // 计算X和Y相似度时需要的交集和并集
                    Set<MergedEdge> intersection = new HashSet<>(edgeCombinationX.getKey());
                    intersection.retainAll(edgeCombinationY.getKey());
                    Set<MergedEdge> union = new HashSet<>(edgeCombinationX.getKey());
                    union.addAll(edgeCombinationY.getKey());
                    similarity = ((double)intersection.size()) / union.size();
                }
                rstSumPySxy += pY * similarity;
            }
            if (rstSumPySxy == 0) {
                System.out.println("here");
            }
            // 将log换位2为底
            entropy += pX * Math.log(rstSumPySxy) / Math.log(2);
        }


        return Math.abs(entropy);
    }

}


enum EdgeType {
    IN,
    OUT,
    ALL
}