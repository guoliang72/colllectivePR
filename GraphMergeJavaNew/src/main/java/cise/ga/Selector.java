package cise.ga;

import cise.mergeinfo.MergedGraphInfo;
import java.util.Collection;

public interface Selector {
    public Collection<MergedGraphInfo> multipleSelect(Collection<MergedGraphInfo> allMergedGraphInfo, int selectNum);

}
