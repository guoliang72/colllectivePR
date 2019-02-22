package cise.ga;

import cise.mergeinfo.MergedGraphInfo;

public interface Crossover {

    /**
     * 给定两个融合图，通过交叉产生一个新的融合图，不修改原有两个图的信息
     * @param mergedGraph1
     * @param mergedGraph2
     * @return
     */
    public MergedGraphInfo crossover(MergedGraphInfo mergedGraph1, MergedGraphInfo mergedGraph2);
}
