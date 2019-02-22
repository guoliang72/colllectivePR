package cise.analyze;

import cise.analyze.Fault.FaultType;
import cise.graphcomponent.Edge;
import cise.graphcomponent.Graph;
import cise.graphcomponent.MergedEdge;
import cise.graphcomponent.MergedVertex;
import cise.graphcomponent.Vertex;
import cise.mergeinfo.MergedGraphInfo;
import cise.mergeinfo.VertexSimilarity;
import cise.utils.StringType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

final class FaultScore {

    public double getNotMergedScore() {
        return notMergedScore;
    }

    public void setNotMergedScore(double notMergedScore) {
        this.notMergedScore = notMergedScore;
    }

    public double getNotSimilarScore() {
        return notSimilarScore;
    }

    public void setNotSimilarScore(double notSimilarScore) {
        this.notSimilarScore = notSimilarScore;
    }

    public double getDifferentEdgeScore() {
        return differentEdgeScore;
    }

    public void setDifferentEdgeScore(double differentEdgeScore) {
        this.differentEdgeScore = differentEdgeScore;
    }

    private double notMergedScore;
    private double notSimilarScore;
    private double differentEdgeScore;

    public FaultScore() {
        notMergedScore = 1.0;
        notSimilarScore = 1.0;
        differentEdgeScore = 1.0;
    }

    public FaultScore(double notMergedScore, double notSimilarScore, double differentEdgeScore) {
        this.notMergedScore = notMergedScore;
        this.notSimilarScore = notSimilarScore;
        this.differentEdgeScore = differentEdgeScore;
    }

    public double getScore() {
        double finalScore = notMergedScore / 3 + notSimilarScore / 3 + differentEdgeScore / 3;
        if (finalScore > 1.0) {
            finalScore = 1.0;
        }
        return finalScore;
    }
}

class Fault {
    enum FaultType {
        edge,
        vertex;
    }

    public FaultType type;

    public Vertex v;

    public Graph g;

    public int line;

    public String message;

    public Fault(FaultType type, Vertex v, String m) {
        this.type = type;
        this.v = v;
        this.message = m;
        this.g = v.graph;
        this.line = v.line;
    }

    @Override
    public String toString() {
        return g.name + " " + line + ": " + v.name + ", " + type + ", " + message;
    }
}

/**
 *
 */
public class BasicFaultLocater {
    public MergedGraphInfo mergedGraphInfo;
    private Map<Vertex, FaultScore> vertexScoreMap = new HashMap<>();

    public BasicFaultLocater(MergedGraphInfo mgInfo) {
        this.mergedGraphInfo = mgInfo;
    }

    public Set<Fault> findFault() {
        Set<Fault> ret = new HashSet<>();
        for (MergedVertex mv : mergedGraphInfo.getMergedGraph().vertexSet()) {
            differentEdge(mv, ret);
            differentWithOthers(mv, ret);
            notMerged(mv, ret);
        }
        return ret;
    }




    private void updateVertexScore(Vertex v, FaultScore score) {
        if (!vertexScoreMap.containsKey(v)) {
            vertexScoreMap.put(v, score);
        }
        else {
            FaultScore currentScore = vertexScoreMap.get(v);
            currentScore.setNotMergedScore(Math.min(currentScore.getNotMergedScore(), score.getNotMergedScore()));
            currentScore.setNotSimilarScore(Math.min(currentScore.getNotSimilarScore(), score.getNotSimilarScore()));
            currentScore.setDifferentEdgeScore(Math.min(currentScore.getDifferentEdgeScore(), score.getDifferentEdgeScore()));
        }
    }

    /**
     * 由节点的融合程度判断
     * 如果一个融合节点包含的节点很少，则这些节点可能是错的
     * @param mv
     * @return
     */
    private void notMerged(MergedVertex mv, Set<Fault> ret) {
        double THRESHOLD = 0.2;
        int graphNum = mergedGraphInfo.getGraphsInfo().getGraphNum();
        // 融合在一起的点很少
        if (mv.getAllVertex().size() <= graphNum * THRESHOLD) {
            for (Vertex v : mv.getAllVertex()) {
                //updateVertexScore(v, new FaultScore((double)(mv.getAllVertex().size())/graphNum, 1.0, 1.0));
                ret.add(new Fault(FaultType.vertex, v, "Not Merged"));
            }
        }
    }

    class VertexPair {
        Vertex v1;
        Vertex v2;
        public VertexPair(Vertex v1, Vertex v2) {
            this.v1 = v1;
            this.v2 = v2;
        }
    }

    private void differentWithOthers(MergedVertex mv, Set<Fault> ret) {
        // 对于每个被融合节点，计算它和其他节点的 相似度的平均值
        // 以这些平均值尝试进行2聚类，相似度平均值较低的那些则可能是错误

        // 如果融合在一起的节点过少，则直接忽略
        if (mv.getAllVertex().size() < 0.3 * mergedGraphInfo.getGraphsInfo().getGraphNum()) {
            return;
        }

//        // 除了特定类型的节点，其他不考虑节点内容
//        // 需要考虑内容的节点类型
//        StringType[] lst = {new StringType("OP"), new StringType("CONST"), new StringType("FUNCNAME")};
//        Set<StringType> nameIsImportant = new HashSet<>(Arrays.asList(lst));
//
//        Map<String, Set<Graph>> nameToGraphMap = new HashMap<>();
//
//        // 这些类型，需要计算节点内容相似度，其他直接忽略
//        if (!nameIsImportant.contains(mv.type)) {
//            return;
//        }

        double minSim = 1.0;
        double maxSim = 0.0;
        Map<Vertex, Double> similarityForVertex = new HashMap<>();
        for (Vertex v1 : mv.getAllVertex()) {
            double sum = 0.0;
            for (Vertex v2 : mv.getAllVertex()) {

                double similarity = VertexSimilarity.calcSimilarity(v1, v2);
                sum += similarity;
            }
            double avgSim = sum/mv.getAllVertex().size();
            if (avgSim > maxSim)
                maxSim = avgSim;
            if (avgSim < minSim)
                minSim = avgSim;
            similarityForVertex.put(v1, avgSim);
        }

        Map<Vertex, Integer> clusterMap = new HashMap<>();
        double[] coreList = {minSim, maxSim};
        int[] numForCluster = {0, 0};
        while (true) {
            // 计算距离并分类
            for (Vertex v : mv.getAllVertex()) {
                double similarity = similarityForVertex.get(v);
                double d1 = Math.abs(similarity - coreList[0]);
                double d2 = Math.abs(similarity - coreList[1]);
                if (d1 < d2) {
                    clusterMap.put(v, 0);
                }
                else {
                    clusterMap.put(v, 1);
                }
            }

            // 计算新的center
            double[] sumForCluster = {0.0, 0.0};
            for (int i = 0; i < 2; i++) {
                numForCluster[i] = 0;
            }
            for (Entry<Vertex, Integer> entry : clusterMap.entrySet()) {
                sumForCluster[entry.getValue()] += similarityForVertex.get(entry.getKey());
                numForCluster[entry.getValue()]++;
            }

            int changed = 0;
            for (int i = 0; i < 2; i++) {
                if (numForCluster[i] > 0) {
                    double newCore = sumForCluster[i] / numForCluster[i];
                    if (newCore != coreList[i]) {
                        changed++;
                        coreList[i] = newCore;
                    }
                }
            }

            // 稳定了
            if (changed == 0) {
                break;
            }

        }

        // 聚类后
        // 是否形成了两个core
        if (numForCluster[0] * numForCluster[1] == 0) {
            // 会没有吗
            return;
        }

        // 两个聚类差别有多大
        if (Math.abs(coreList[0] - coreList[1]) <= 1e-6) {
            return;
        }

        // 有差别的话，属于数值较小的聚类的节点“与众不同”
        int smaller = coreList[0] < coreList[1] ? 0 : 1;
        // 如果数值较小的聚类的点很多，也就可能是另一种正确解法
        if (numForCluster[smaller] > 0.5 * mergedGraphInfo.getGraphsInfo().getGraphNum()) {
            return;
        }
        for (Entry<Vertex, Integer> entry : clusterMap.entrySet()) {
            if (entry.getValue() == smaller) {
                ret.add(new Fault(FaultType.vertex, entry.getKey(), "Different Content"));
            }
        }

        return;
    }

    private void differentEdge(MergedVertex mv, Set<Fault> ret) {
        // 融合在一起的边很少，则这条边的目标节点可能有问题
        Set<MergedEdge> outgoingEdgeSet = mergedGraphInfo.getMergedGraph().incomingEdgesOf(mv);
        double threshold  = 0.4;
        int graphNum = mergedGraphInfo.getGraphsInfo().getGraphNum();
        if (mv.getAllVertex().size() < graphNum * 0.8) {
            return;
        }
        for (MergedEdge me : outgoingEdgeSet) {
            if (me.type.equals(new StringType("DECL"))){
                continue;
            }
            if (me.getEdgeSet().size() <= mv.getAllVertex().size() * threshold) {
                for (Edge e : me.getEdgeSet()) {
                    if (e.getSource().line <= e.getTarget().line)
                        ret.add(new Fault(FaultType.edge, e.getTarget(), "Different Edge Type: " + me.type));
                }
            }
        }
    }



}
