package cise.mergeinfo;

import cise.graphcomponent.Edge;
import cise.graphcomponent.Graph;
import cise.graphcomponent.MergedEdge;
import cise.graphcomponent.MergedGraph;
import cise.graphcomponent.MergedVertex;
import cise.graphcomponent.Vertex;
import cise.utils.Constants;
import cise.utils.Result;
import cise.utils.StringType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 存放融合图相关信息
 */
public class MergedGraphInfo implements Serializable {

    private PartialMergeSolution partialMergeSolution = PartialMergeSolution.EMPTY;
    private PartialMergeAntiSolution partialMergeAntiSolution = PartialMergeAntiSolution.EMPTY;

    private GraphsInfo graphsInfo;
    private MergedGraph mergedGraph;

    public boolean isChanged = true;

    private Map<StringType, Set<MergedVertex>> typeToVertexSetMap;

  /*
   有一段存疑代码尚未移植，粘贴如下
        // 这个变量和上面的变量是同构的.
        // 对于融合图中的每一个节点, 其对应的set<g_set>. 其中, g_set指的是当前节点中包含的必须融合在一起的一组被融合图节点所在图的集合.
        // 其中, 向量（第2个参数）的位置i代表的是节点描述符向量中对应位置的节点.
        std::map<std::string, std::vector<std::set<g_set>>> type_to_gset_set_vec;
   */

    /**
     * 被融合图中的节点到融合图中节点的映射，即：被融合图中的某个节点属于融合图中的哪个节点
     */
    private Map<Vertex, MergedVertex> vertexToMergedVertexMap;

    /**
     * 被融合图中的边到融合图中边的映射，即：被融合图中的某个边属于融合图中的哪个边
     */
    private Map<Edge, MergedEdge> edgeToMergedEdgeMap;

    /**
     * 融合图的熵
     */
    double entropy = -1;

    /**
     * 构造函数，接收一组待融合图信息作为参数
     *
     * @param graphsInfo 待融合图信息
     */
    public MergedGraphInfo(GraphsInfo graphsInfo) {
        this.graphsInfo = graphsInfo;
        this.mergedGraph = new MergedGraph();

        typeToVertexSetMap = new HashMap<>();
        vertexToMergedVertexMap = new HashMap<>();
        edgeToMergedEdgeMap = new HashMap<>();
    }

    /**
     * 获得一个当前mergedgraphinfo的深度复制
     * @return
     */
    public MergedGraphInfo getClone() {
        MergedGraphInfo ret = new MergedGraphInfo(this.graphsInfo);
        ret.mergedGraph = new MergedGraph();
        // 将当前的所有融合节点复制一个
        for (MergedVertex mv : this.mergedGraph.vertexSet()) {
            MergedVertex newmv = new MergedVertex(mv);
            // 所属图修改
            newmv.mergedGraph = ret.mergedGraph;
            // 添加
            ret.mergedGraph.addVertex(newmv);
        }
        // 和随机生成一样的连边步骤
        ret.trim();
        ret.updateMapping();
        ret.mergeEdges();
        return ret;

    }

    public Set<MergedVertex> getMergedVertexByType(StringType type) {
        if (typeToVertexSetMap.containsKey(type)) {
            return typeToVertexSetMap.get(type);
        }
        return new HashSet<>();
    }

    public MergedGraph getMergedGraph() {
        return mergedGraph;
    }

    public Set<StringType> getAllTypes() {
        return typeToVertexSetMap.keySet();
    }

    public GraphsInfo getGraphsInfo(){
        return this.graphsInfo;
    }

    public double getEntropy() {
        return entropy;
    }

    public void setEntropy(double e) {
        this.entropy = e;
    }

    /**
     * 删除空的融合节点
     */
    public void trim() {
        Set<MergedVertex> mVertexSet = new HashSet<>(mergedGraph.vertexSet());
        for (MergedVertex mv : mVertexSet) {
            if (mv.getAllVertex().isEmpty()) {
                mergedGraph.removeVertex(mv);
            }
        }
    }

    /**
     * 在融合图结构改变后，调用此函数更新融合图信息中的各种映射关系
     */
    public void updateMapping() {
        // 更新typeToVertexSetMap
        typeToVertexSetMap.clear();
        for (MergedVertex mv : mergedGraph.vertexSet()) {
            if (!typeToVertexSetMap.containsKey(mv.type)) {
                typeToVertexSetMap.put(mv.type, new HashSet<>());
            }
            typeToVertexSetMap.get(mv.type).add(mv);
        }

        // 更新vertexToMergedVertexMap
        vertexToMergedVertexMap.clear();
        for (MergedVertex mv : mergedGraph.vertexSet()) {
            for (Vertex v : mv.getAllVertex()) {
                vertexToMergedVertexMap.put(v, mv);
            }
        }
    }

    /**
     * 随机生成一个融合图<br/>
     * 生成的融合图信息被写入自身的mergedGraph成员中
     * <ul>
     *     <li>成员 <code>partialMergeSolution</code> 必须是一个合法的紧凑的融合方案片段</li>
     *     <li>成员 <code>partialMergeAntiSolution</code> 必须是一个合法的无冗余的反融合方案片段</li>
     * </il>
     *
     * @return 表示生成结果
     */
    public Result randomMergeGraph() {
        // 首先生成紧凑的融合方案片段

        // TODO: 暂时略过检查

        // 生成新的空融合图
        this.mergedGraph = new MergedGraph();

        /**
         * 第一步：融合节点
         * 只有相同类型节点才能融合
         *
         */

    /*
     * 为方便阅读，定义如下几个内部类
     */

        /**
         * 预融合节点集。
         * “预定”要融合在一起的节点集合<br/>
         *  在随机生成融合方案时，将这些节点放在一起，表示在融合图中它们应当被融合在同一节点中
         */
        class PreMergeVertexSet {

            public Set<Vertex> vertexSet = new HashSet<>();
            /** 这些节点所属的图的集合，用于检查新加入节点时是否允许 */
            private Set<Graph> graphSet = new HashSet<>();

            /**
             * 判断一个节点是否能加入这个集合<br/>
             * 准则是，这个节点不能与集合中已有节点属于同一个图，且不能与已经存在于这个集合的节点有冲突
             * @param v 要加入的节点
             * @param antiSolution 反融合方案片段
             * @return 是否能够加入
             */
            public boolean canInsert(Vertex v, PartialMergeAntiSolution antiSolution) {
                // 不属于同一个图
                if (graphSet.contains(v.graph)) {
                    return false;
                }
                // 查找与节点v冲突的节点集合
                Set<Vertex> conflictSet = antiSolution.getConflictVertexSet(v);
                for (Vertex vertex : vertexSet) {
                    if (conflictSet.contains(vertex)) {
                        return false;
                    }
                }
                return true;
            }

            /**
             * 插入一个节点
             * @param v 待插入的节点
             */
            public void add(Vertex v) {
                vertexSet.add(v);
                graphSet.add(v.graph);
            }

            public void addAll(Iterable<Vertex> vertexIterable) {
                for (Vertex v : vertexIterable) {
                    add(v);
                }
            }

            /**
             * 预融合节点集大小
             * @return
             */
            public int size() {
                return vertexSet.size();
            }
        }

        /**
         * 对于某特定类型的节点的融合方案。
         * 其中包含了一组预融合结点集合，个数为所有待融合图中，这个类型的节点总数
         */
        class MergeSolutionForType {

            public final StringType vertexType;
            public List<PreMergeVertexSet> preMergeVertexSetList;
            private int size;
            /**
             * 已经添加到融合方案的节点集合
             */
            private Set<Vertex> allVertex;

            public MergeSolutionForType(StringType type, int num) {
                this.vertexType = type;
                size = num;
                preMergeVertexSetList = new ArrayList<>(num);
                for (int i = 0; i < num; i++) {
                    preMergeVertexSetList.add(new PreMergeVertexSet());
                }
                allVertex = new HashSet<>();
            }

            /**
             * 包含的预融合节点集的个数
             */
            public int size() {
                return size;
            }

            /**
             * 已经包含的节点数
             */
            public int vertexNum() {
                return allVertex.size();
            }

            /**
             * 检查给定节点是否已经加入融合方案
             *
             * @param v 节点
             * @return 节点是否已经被添加
             */
            public boolean containsVertex(Vertex v) {
                return allVertex.contains(v);
            }

            /**
             * 将一个来自融合方案片段的节点集合，插入到某个预融合节点集中。 随机选择一个起始位置，之后找到一个合法的预融合节点集
             *
             * @param partialMergeVertexSet
             */
            public Result addVertexSet(PartialMergeVertexSet partialMergeVertexSet) {
                return addVertexSet(partialMergeVertexSet, PartialMergeAntiSolution.EMPTY);
            }

            /**
             * 将一个来自融合方案片段的节点集合，插入到某个预融合节点集中。 随机选择一个起始位置，之后找到一个合法的预融合节点集
             *
             * @param antiSolution 反融合方案片段，用于检查插入
             */
            public Result addVertexSet(PartialMergeVertexSet partialMergeVertexSet,
                PartialMergeAntiSolution antiSolution) {
                // 从list中获得一个随机的起始位置
                int randomIndex = new Random().nextInt(size());
                // 从这个位置的预融合节点集开始尝试插入，失败的话则向后一个继续查找
                for (int offset = 0; offset < size(); offset++) {
                    int realIndex = (randomIndex + offset) % size();
                    PreMergeVertexSet vertexSetCandidate = preMergeVertexSetList.get(realIndex);
                    boolean isValid = true;
                    // 对于每个节点，检查是否能插入
                    for (Vertex v : partialMergeVertexSet.vertexSet) {
                        if (!vertexSetCandidate.canInsert(v, antiSolution)) {
                            isValid = false;
                            break;
                        }
                    }
                    // 确认了当前这个candidate可以，即刻插入
                    if (isValid) {
                        vertexSetCandidate.addAll(partialMergeVertexSet.vertexSet);
                        allVertex.addAll(partialMergeVertexSet.vertexSet);
                        return Result.success();
                    }
                }
                // 便利了所有位置都没能插入
                return Result.failed("未能插入节点集合");
            }

            /**
             * 将一个节点插入某个预融合节点集中。 随机选择一个起始位置，之后遍历找到一个合法的预融合节点集
             *
             *
             * @param v@return 插入结果
             */
            public Result addVertex(Vertex v) {
                return addVertex(v, PartialMergeAntiSolution.EMPTY);
            }

            /**
             * 将一个节点插入某个预融合节点集中。 随机选择一个起始位置，之后便利找到一个合法的预融合节点集
             *
             * @param antiSolution 反融合片段，用于检查插入
             * @return 插入结果
             */
            public Result addVertex(Vertex v, PartialMergeAntiSolution antiSolution) {
                // 大体方法同上
                // 从list中获得一个随机的起始位置
                int randomIndex = new Random().nextInt(size());
                // 从这个位置的预融合节点集开始尝试插入，失败的话则向后一个继续查找
                for (int offset = 0; offset < size(); offset++) {
                    int realIndex = (randomIndex + offset) % size();
                    PreMergeVertexSet vertexSetCandidate = preMergeVertexSetList.get(realIndex);
                    boolean isValid = true;
                    // 对于每个节点，检查是否能插入
                    if (vertexSetCandidate.canInsert(v, antiSolution)) {
                        //System.out.println("Vertex " + v.id +  " Inserted into id :" + realIndex);
                        // 确认了当前这个candidate可以，即刻插入
                        vertexSetCandidate.add(v);
                        allVertex.add(v);
                        return Result.success();
                    }
                }
                // 遍历了所有位置都没能插入
                return Result.failed("未能插入节点");
            }

        }

        /**
         * 内部类定义结束
         */

    /*
     * 在融合方案中插入节点，分为三部
     */
        // 对于待融合图中的每个类型，创建一个融合方案

        StringType entryType = new StringType("ENTRY");

        Map<StringType, MergeSolutionForType> mergeSolutionForTypeMap = new HashMap<>();
        for (StringType type : graphsInfo.vertexTypeSet) {
            // 对 ENTRY 类型固定融合
            if (type.equals(entryType))
                continue;

            mergeSolutionForTypeMap
                .put(type, new MergeSolutionForType(type, graphsInfo.getVertexNumByType(type)));
            if (Constants.DEBUG) {
                //System.out.println("type: " + type + " Count: " + mergeSolutionForTypeMap.get(type).size());
            }
        }

    /*
      3: 随机插入剩余节点
     */
        // graphsInfo 的 typeToVertexSetMap 中，包含了所有待融合图中的节点。
        // 将这些节点中未被融合的点随机插入到对应类型的融合方案中
        for (StringType type : graphsInfo.vertexTypeSet) {
            // entry节点不随机融合
            if (type.equals(entryType))
                continue;

            MergeSolutionForType solutionForType = mergeSolutionForTypeMap.get(type);
            // vertexSet: 一个图中，类型为type的节点集合
            for (Vertex v : graphsInfo.getVertexSetsByType(type)) {
                    // 如果这个节点已经被插入过了，则跳过
                    if (solutionForType.containsVertex(v)) {
                        continue;
                    }
                    // 否则，将其插入
                    // 由于前两步的执行，没被插入过的节点应当不属于任何融合方案片段或反融合方案片段
                    solutionForType.addVertex(v);

            }
        }

    /*
      至此，融合方案分配完成
      之后，根据融合方案构建融合图
     */

        // 首先创建指定融合好的ENTRY节点
        MergedVertex entryMergedVertex = new MergedVertex(entryType, mergedGraph);
        entryMergedVertex.addAllVertex(graphsInfo.getVertexSetsByType(entryType));
        mergedGraph.addVertex(entryMergedVertex);

        // 遍历其他每个类型的融合方案
        for (MergeSolutionForType solutionForType : mergeSolutionForTypeMap.values()) {
            // 遍历每个融合方案中的预融合组，融合组中的节点应当成为一个融合节点
            for (PreMergeVertexSet preMergeVertexSet : solutionForType.preMergeVertexSetList) {
                // 空的节点集跳过
                if (preMergeVertexSet.size() == 0) {
                    continue;
                }
                StringType type = solutionForType.vertexType;
                // 创建新的节点
                MergedVertex newMergedVertex = new MergedVertex(type, mergedGraph);

                // 添加所有节点
                newMergedVertex.addAllVertex(preMergeVertexSet.vertexSet);

                // 将新节点添加到融合图
                mergedGraph.addVertex(newMergedVertex);

//                // 为新节点添加映射
//                // 1: 类型到融合节点映射
//                if (!typeToVertexSetMap.containsKey(type)) {
//                    typeToVertexSetMap.put(type, new HashSet<MergedVertex>());
//                }
//                typeToVertexSetMap.get(type).add(newMergedVertex);
//
//                // 2: 待融合图节点到融合节点的映射
//                for (Vertex v : newMergedVertex.getAllVertex()) {
//                    vertexToMergedVertexMap.put(v, newMergedVertex);
//                }
            }
        }

        trim();

        // 节点融合完成，更新映射
        updateMapping();

        // 更新边融合
        mergeEdges();

        return Result.success();
    }

    public Result mergeEdges() {
        // 清空边
        // 这里使用的边操作来自 JGraphT 库
        Set<MergedEdge> oldEdges = new HashSet<>(mergedGraph.edgeSet());
        mergedGraph.removeAllEdges(oldEdges);

        // 遍历所有待融合图中的所有边
        for (Graph graph : graphsInfo.graphSet) {
            for (Edge edge : graph.edgeSet()) {
                // 获得这条边的类型，源节点和目标节点
                StringType edgeType = edge.type;
                Vertex source = edge.getSource();
                Vertex target = edge.getTarget();
                // 获得源节点和目标节点在融合图中的对应
                MergedVertex mergedSource = vertexToMergedVertexMap.get(source);
                MergedVertex mergedTarget = vertexToMergedVertexMap.get(target);
                if (mergedSource == null || mergedTarget == null) {
                    System.out.println("!?!??!?!?");
                }
                if (!mergedSource.getAllVertex().contains(source) || !mergedTarget.getAllVertex().contains(target)) {
                    System.out.println("??????????");
                }
                // 查找融合图中，在两个节点间已经存在的边
                Set<MergedEdge> srcToTgtEdges = mergedGraph.getAllEdges(mergedSource, mergedTarget);
                if (srcToTgtEdges == null) {
                    srcToTgtEdges = new HashSet<>();
                }
                // 遍历这些边，如果已经存在相同类型的边，则将原图边添加到这个融合边中
                boolean mergedEdgeFound = false;
                for (MergedEdge mergedEdge : srcToTgtEdges) {
                    // 找到一条已经存在的同类型边，则添加
                    if (mergedEdge.type.equals(edgeType)) {
                        mergedEdgeFound = true;
                        mergedEdge.addEdge(edge);
                        // 更新映射
                        edgeToMergedEdgeMap.put(edge, mergedEdge);
                        break;
                    }
                }

                // 不存在相同类型的边，则创建一条
                if (!mergedEdgeFound) {
                    MergedEdge newEdge = new MergedEdge(edgeType, mergedGraph, mergedSource,
                        mergedTarget);
                    newEdge.addEdge(edge);
                    mergedGraph.addEdge(mergedSource, mergedTarget, newEdge);
                    // 更新映射
                    edgeToMergedEdgeMap.put(edge, newEdge);
                }
            }
        }
        return Result.success();
    }

}
