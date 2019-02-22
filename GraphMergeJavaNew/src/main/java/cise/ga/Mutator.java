package cise.ga;

import cise.mergeinfo.MergedGraphInfo;
import cise.utils.Result;

public interface Mutator {

    /**
     * 输入一个融合图信息，将其进行变异，变异结果修改原图
     */
    public Result mutate(MergedGraphInfo inputMergedGraph);
}