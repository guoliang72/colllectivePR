package cise.ga;

import cise.mergeinfo.MergedGraphInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class RouletteSelector implements Selector {

    @Override
    public List<MergedGraphInfo> multipleSelect(
        Collection<MergedGraphInfo> allMergedGraphInfo, int selectNum) {

        class MergedGraphInfoWithComplement {
            MergedGraphInfo mergedGraphInfo;
            double entropyComplement;

            public MergedGraphInfoWithComplement(MergedGraphInfo mergedGraphInfo, double v) {
                this.mergedGraphInfo = mergedGraphInfo;
                entropyComplement = v;
            }
        }

        double maxEntropy = Double.MIN_VALUE;
        double minEntropy = Double.MAX_VALUE;

        for (MergedGraphInfo mergedGraphInfo : allMergedGraphInfo) {
            maxEntropy = Math.max(mergedGraphInfo.getEntropy(), maxEntropy);
            minEntropy = Math.min(mergedGraphInfo.getEntropy(), minEntropy);
        }

        List<MergedGraphInfoWithComplement> wrappedMergedGraphInfoList = new ArrayList<>(allMergedGraphInfo.size());
        double finalMaxEntropy = maxEntropy;
        double finalMinEntropy = minEntropy;
        allMergedGraphInfo.forEach(mergedGraphInfo -> {
            wrappedMergedGraphInfoList.add(new MergedGraphInfoWithComplement(mergedGraphInfo, 1 / mergedGraphInfo.getEntropy()));
        });

        double sumEntropyComplement = 0;
        for (MergedGraphInfoWithComplement info : wrappedMergedGraphInfoList) {
            sumEntropyComplement += info.entropyComplement;
        }


//        //
//        wrappedMergedGraphInfoList.sort(new Comparator<MergedGraphInfoWithComplement>() {
//            @Override
//            public int compare(MergedGraphInfoWithComplement o1, MergedGraphInfoWithComplement o2) {
//                return Double.compare(o1.entropyComplement, o2.entropyComplement);
//            }
//        });

//        for (MergedGraphInfoWithComplement mergedGraphInfoWithComplement : wrappedMergedGraphInfoList) {
//            System.out.println(mergedGraphInfoWithComplement.entropyComplement);
//        }

        // 选择
        List<MergedGraphInfo> ret = new ArrayList<>(selectNum);
        for (int i = 0; i < selectNum; i++) {
            double randomChoson = new Random().nextDouble() * sumEntropyComplement;
            for (MergedGraphInfoWithComplement info : wrappedMergedGraphInfoList) {
                if (randomChoson <= info.entropyComplement) {
                    ret.add(info.mergedGraphInfo);
                    break;
                }
                randomChoson -= info.entropyComplement;
            }
        }
//
//        List<MergedGraphInfo> copy = new ArrayList<>(ret);
//        copy.sort(new Comparator<MergedGraphInfo>() {
//            @Override
//            public int compare(MergedGraphInfo o1, MergedGraphInfo o2) {
//                return Double.compare(o1.getEntropy(), o2.getEntropy());
//            }
//        });
//
//        copy.forEach(mergedGraphInfo -> {
//            System.out.println(mergedGraphInfo.getEntropy());
//        });


        return ret;
    }
}
