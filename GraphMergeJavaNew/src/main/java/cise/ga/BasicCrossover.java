package cise.ga;

import cise.graphcomponent.Graph;
import cise.graphcomponent.MergedVertex;
import cise.graphcomponent.Vertex;
import cise.mergeinfo.GraphsInfo;
import cise.mergeinfo.MergedGraphInfo;
import cise.utils.StringType;
import cise.utils.UtilFunction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jgrapht.alg.interfaces.MatchingAlgorithm;
import org.jgrapht.alg.interfaces.MatchingAlgorithm.Matching;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

public class BasicCrossover implements Crossover {

    /**
     * 思想：寻找从融合图1到融合图2的最短“修改路径”，
     * 即通过交换两个融合节点中的待融合图节点，将融合图1变为融合图2。
     * <br/>
     * 1：寻找融合图1的融合节点 和 融合图2的融合节点 之间的一个最大匹配（节点尽量对应，使修改最少）<br/>
     * 2：对应的融合节点间，不一致的待融合图节点即为需要交换的节点。<br/>
     * 3：以
     * @param mergedGraph1
     * @param mergedGraph2
     * @return
     */
    @Override
    public MergedGraphInfo crossover(MergedGraphInfo mergedGraph1, MergedGraphInfo mergedGraph2) {




        // 用于返回的新融合图
        GraphsInfo graphsInfo = mergedGraph1.getGraphsInfo();
        MergedGraphInfo newMergedGraph = new MergedGraphInfo(graphsInfo);

        // 对每个类型分别进行如下
        for (StringType type : mergedGraph1.getAllTypes()) {

            // long currentTime = System.currentTimeMillis();
            // entry类型不交叉
            if (type.equals(StringType.entryType)) {
                // 直接复制到新图
                for (MergedVertex v : mergedGraph1.getMergedVertexByType(type)) {
                    newMergedGraph.getMergedGraph().addVertex(new MergedVertex(v));
                }
            }
            else {
                // 首先取出两个融合图的融合节点集合做副本
                // 之后需要修改融合节点集合
                Set<MergedVertex> mergedVertexSet1 = new HashSet<>();
                Set<MergedVertex> mergedVertexSet2 = new HashSet<>();

                // 新造一组融合节点，这些节点不在原来两个融合图中
                for (MergedVertex v : mergedGraph1.getMergedVertexByType(type)) {
                    mergedVertexSet1.add(new MergedVertex(v));
                }

                for (MergedVertex v : mergedGraph2.getMergedVertexByType(type)) {
                    mergedVertexSet2.add(new MergedVertex(v));
                }

                // 向较小的一组融合节点集添加空融合节点，使得两个集合大小相同，用来寻找一一对应
                Set<MergedVertex> smallerSet =
                    (mergedVertexSet1.size() < mergedVertexSet2.size()) ? mergedVertexSet1
                        : mergedVertexSet2;

                int howManyShouldBeInserted = Math
                    .abs(mergedVertexSet1.size() - mergedVertexSet2.size());
                for (int i = 0; i < howManyShouldBeInserted; i++) {
                    // 新加入的空节点不属于任何融合图
                    smallerSet.add(new MergedVertex(type, null));
                }

                // 1: 对两个融合图的节点找到一个最大匹配
                Set<MergedVertexMatching> matchings = MergedVertexMatching
                    .matching(mergedVertexSet1, mergedVertexSet2, type);
                MatchingSet matchingSet = new MatchingSet(matchings);

                //System.out.println("Matching: \n" + matchingSet);

                // 如果要将图1的融合方式，通过交换待融合节点，变为图二方式。变换到1/2位置时，可认为是两个融合方式的交叉
                // 以融合节点集2为目标，交换融合节点集1中的待融合节点。

                // 对图1和图2的每个待融合节点，找到它们在两个图中所在的融合节点
                Map<Vertex, MergedVertex> fromToMergedMap = new HashMap<>();
                Map<Vertex, MergedVertex> toToMergedMap = new HashMap<>();

                // 构建上面的映射
                for (MergedVertex mv : mergedVertexSet1) {
                    for (Vertex v : mv.getAllVertex()) {
                        fromToMergedMap.put(v, mv);
                    }
                }
                for (MergedVertex mv : mergedVertexSet2) {
                    for (Vertex v : mv.getAllVertex()) {
                        toToMergedMap.put(v, mv);
                    }
                }

                // 由于两个融合节点集所包含的带融合节点应该是一样的，故在此断言一下
                assert fromToMergedMap.keySet().containsAll(toToMergedMap.keySet());
                assert toToMergedMap.keySet().containsAll(fromToMergedMap.keySet());

                // 给定一个图1的待融合节点，可以查到它在图2中所属的融合节点，是否和图1中融合节点匹配了
                // 如果没有，则它是一个需要移动的节点，同时记下它应当属于图1中哪个节点
                class NeedMoveVertex {

                    Vertex v;
                    MergedVertex belongsTo;
                    MergedVertex shouldGoTo;

                    public NeedMoveVertex(Vertex v, MergedVertex belongsTo,
                        MergedVertex shouldGoTo) {
                        this.v = v;
                        this.belongsTo = belongsTo;
                        this.shouldGoTo = shouldGoTo;
                    }
                }

                Set<Vertex> allVertex = new HashSet<>(fromToMergedMap.keySet());
                Set<NeedMoveVertex> needMoveVertices = new HashSet<>();

                // 遍历每个待融合节点
                for (Vertex v : allVertex) {
                    // 找到在图1和图2中分别所属的融合组
                    MergedVertex mvInGraph1 = fromToMergedMap.get(v);
                    MergedVertex mvInGraph2 = toToMergedMap.get(v);
                    // 是否在同一个匹配对中
                    boolean isMatched = matchingSet.isMatched(mvInGraph1, mvInGraph2);
                    // 如果不在
                    if (!isMatched) {
                        // 查找mvInGraph2对应的图1中融合节点，作为这个待融合节点的目标位置
                        MergedVertex shouldGoTo = matchingSet.getMatchingByTo(mvInGraph2).getFrom();
                        needMoveVertices.add(new NeedMoveVertex(v, mvInGraph1, shouldGoTo));
                    }
                }

                // 将图1变为图2，需要移动的节点数量，也就是两个图的差异大小
                int needToMove = needMoveVertices.size();

                // System.out.println("need to move " + needToMove);

                // 交叉希望找到介于两图之间的融合方案，故循环进行交换，直到差异减小到needToMove/2

                while (needMoveVertices.size() > needToMove / 2 && !needMoveVertices.isEmpty()) {
                    // 随机选择一个需要移动的节点，将其和目标融合节点中，属于同一个待融合图的另一节点，交换
                    // 如果目标融合节点中不包含来自同一个待融合图的节点，则是一个简单的移动，否则是交换
                    NeedMoveVertex randomChose = new UtilFunction.CollectionUtil<NeedMoveVertex>()
                        .pickRandom(needMoveVertices);

                    MergedVertex source = randomChose.belongsTo;
                    MergedVertex target = randomChose.shouldGoTo;

                    Graph theGraph = randomChose.v.graph;

                    // 在target中找到属于theGraph的vertex
                    Vertex vFromTarget = null;
                    for (Vertex v : target.getAllVertex()) {
                        if (v.graph.equals(theGraph)) {
                            vFromTarget = v;
                            break;
                        }
                    }

                    // 将当前节点移动到目标融合节点
                    target.addVertex(randomChose.v);
                    source.removeVertex(randomChose.v);
                    // 从需要移动的节点集合中移除当前节点\
                    needMoveVertices.remove(randomChose);

                    // 如果对面有个同一图的节点，交换过来
                    if (vFromTarget != null) {
                        source.addVertex(vFromTarget);
                        target.removeVertex(vFromTarget);

                        // 判断这个节点通过这次移动是否到位，更新信息(这个节点一定需要移动)
                        NeedMoveVertex theOtherVertex = null;
                        for (NeedMoveVertex nmv : needMoveVertices) {
                            if (nmv.v.equals(vFromTarget)) {
                                theOtherVertex = nmv;
                            }
                        }

                        // 一定存在
                        assert theOtherVertex != null;
                        // 它被移动到了source这个融合节点里
                        theOtherVertex.belongsTo = source;
                        // 如果恰好被移动到了正确位置
                        if (theOtherVertex.shouldGoTo.equals(theOtherVertex.belongsTo)) {
                            needMoveVertices.remove(theOtherVertex);
                        }

                    }

                }

                // System.out.println("Matching after crossover: " + needMoveVertices.size() + "\n" + matchingSet);

                // 循环结束后，应当是执行完一半的交换
                // mergedVertexSet1构成了交叉图的融合节点集和
                // 将它们加入到新的融合图中
                for (MergedVertex mv : mergedVertexSet1) {
                    newMergedGraph.getMergedGraph().addVertex(mv);
                }

            }
            // System.out.println("crossover-" + type.value + ": " + (System.currentTimeMillis() - currentTime)  );
        }
        for (MergedVertex mv : newMergedGraph.getMergedGraph().vertexSet()) {
            mv.mergedGraph = newMergedGraph.getMergedGraph();
        }
        // 循环结束后，新的融合图应当构造完毕
        // 删除空节点，更新映射，融合边
        newMergedGraph.trim();
        newMergedGraph.updateMapping();
        newMergedGraph.mergeEdges();

        newMergedGraph.isChanged = true;



        return newMergedGraph;
    }



}

/**
 * 提供匹配的检索。
 * 使用两个map
 */
class MatchingSet {
    Map<MergedVertex, MergedVertexMatching> fromMap = new HashMap<>();
    Map<MergedVertex, MergedVertexMatching> toMap = new HashMap<>();
    Set<MergedVertexMatching> matchingSet;

    public MatchingSet(Set<MergedVertexMatching> matchings) {
        this.matchingSet = matchings;
        for (MergedVertexMatching matching : matchings) {
            fromMap.put(matching.getFrom(), matching);
            toMap.put(matching.getTo(), matching);
        }
    }

    /**
     * 给定两个节点，检查它们是否在一个匹配对中
     */
    public boolean isMatched(MergedVertex from, MergedVertex to) {
        return fromMap.containsKey(from) && fromMap.get(from).getTo().equals(to);
    }

    /**
     * 给定源融合节点集中的一个融合节点，查找它所在的匹配对
     * @param from
     * @return
     */
    public MergedVertexMatching getMatchingByFrom(MergedVertex from) {
        assert  fromMap.containsKey(from);
        if (fromMap.containsKey(from)) {
            return fromMap.get(from);
        }
        return null;
    }

    public MergedVertexMatching getMatchingByTo(MergedVertex to) {
        assert toMap.containsKey(to);
        if (toMap.containsKey(to)) {
            return toMap.get(to);
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        for (MergedVertexMatching mvm : this.matchingSet) {
            ret.append("<" + mvm.getFrom() + ", " + mvm.getTo() + ">\n");
        }
        return ret.toString();
    }
}

class MergedVertexMatching {
    private MergedVertex from;
    private MergedVertex to;

    public MergedVertexMatching(MergedVertex v1, MergedVertex v2) {
        this.from = v1;
        this.to = v2;
    }

    public MergedVertex getFrom() {
        return from;
    }

    public MergedVertex getTo() {
        return to;
    }

    public MergedVertex getOther(MergedVertex v) {
        if (v.equals(from)){
            return to;
        }
        if (v.equals(to)) {
            return from;
        }
        return null;
    }

    /**
     * 将两个融合图中的，给定类型的融合节点构成一个带权二部图，使用
     * org.jgrapht.alg.matching.MaximumWeightBipartiteMatching
     * 算法计算最大匹配
     * @see MaximumWeightBipartiteMatching
     *
     * @return
     */
    public static Set<MergedVertexMatching> matching(Set<MergedVertex> mergedVertices1, Set<MergedVertex> mergedVertices2, StringType type) {
        // 构建一个带权二部图
        // 两部分节点分别是来自两个融合图中的融合节点
        // 边是两个融合节点的相似度，以包含的待融合图节点集合的交集大小计

        // 用于构建的二部图
        org.jgrapht.Graph<MergedVertex, DefaultWeightedEdge> bipartiteGraph = new SimpleWeightedGraph<>(
            DefaultWeightedEdge.class);
//
//        // 二部图的两个节点集，复制出来备用
//        Set<MergedVertex> vertexSet1 = new HashSet<>(mergedVertices1);
//        Set<MergedVertex> vertexSet2 = new HashSet<>(mergedVertices2);
//
//        //System.out.println("Vertex Set Size: " + vertexSet1.size() + " " + vertexSet2.size());
//
//        // 在较小的节点集中加入一组空节点，使得两个节点集大小相同
//
//        Set<MergedVertex> smallerSet =
//            (vertexSet1.size() < vertexSet2.size()) ? vertexSet1 : vertexSet2;
//
//        int howManyShouldBeInserted = Math.abs(vertexSet1.size() - vertexSet2.size());
//
//        for (int i = 0; i < howManyShouldBeInserted; i++) {
//            // 新加入的空节点不属于任何融合图
//            smallerSet.add(new MergedVertex(type, null));
//        }

        //System.out.println("Vertex Set Size: " + vertexSet1.size() + " " + vertexSet2.size());

        // 将这些节点加入新的二部图
        for (MergedVertex mv1 : mergedVertices1) {
            bipartiteGraph.addVertex(mv1);
        }
        for (MergedVertex mv2 : mergedVertices2) {
            bipartiteGraph.addVertex(mv2);
        }

        // 在每一对节点之间加入一条边，权重是待融合节点集的交集大小
        for (MergedVertex vFromG1 : mergedVertices1) {
            for (MergedVertex vFromG2 : mergedVertices2) {
                // 据文档交代，边的操作必须通过图进行
                DefaultWeightedEdge newEdge = bipartiteGraph.addEdge(vFromG1, vFromG2);
                // 计算交集
                Set<Vertex> vInMergedVertex1 = vFromG1.getAllVertex();
                Set<Vertex> vInMergedVertex2 = vFromG2.getAllVertex();
                Set<Vertex> intersect = new HashSet<>(vInMergedVertex1);
                intersect.retainAll(vInMergedVertex2);
                // 边权要求是正数
                int weight = intersect.size() + 1;

                bipartiteGraph.setEdgeWeight(newEdge, (double)weight);
            }
        }

        // 至此，二部图构建完毕，开始寻找最大匹配
        MatchingAlgorithm<MergedVertex, DefaultWeightedEdge> matchingInstance =
            new MaximumWeightBipartiteMatching<>(bipartiteGraph, mergedVertices1, mergedVertices2);
        // matchingResult中的包含了最大匹配中的边
        Matching<MergedVertex, DefaultWeightedEdge> matchingResult = matchingInstance.getMatching();

        // 由于两边节点相等，这个匹配一定是完美匹配
        assert matchingResult.isPerfect();

        //System.out.println(matchingResult.getEdges().size());

        // 将匹配中的边转换成匹配节点对
        Set<MergedVertexMatching> mvMatching = new HashSet<>();
        // 由于没法从edge直接读取两端节点，还得从二部图绕一下
        for (DefaultWeightedEdge edge : matchingResult.getEdges()) {
            // System.out.println("Edge: " + bipartiteGraph.getEdgeSource(edge) + "," + bipartiteGraph.getEdgeTarget(edge) + " " + bipartiteGraph.getEdgeWeight(edge));
            mvMatching.add(new MergedVertexMatching(bipartiteGraph.getEdgeSource(edge), bipartiteGraph.getEdgeTarget(edge)));
        }

        return mvMatching;
    }
}


