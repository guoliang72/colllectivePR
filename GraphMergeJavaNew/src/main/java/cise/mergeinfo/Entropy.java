package cise.mergeinfo;

/**
 * 计算融合图的熵
 */
public interface Entropy {

    /**
     * 对给定的融合图信息，计算融合图的熵
     *
     * @param mergedGraphInfo 融合图信息
     * @return 计算结果
     */
    public double calculateEntropy(MergedGraphInfo mergedGraphInfo);

}
