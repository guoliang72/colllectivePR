package cise.ga;

import cise.graphcomponent.Graph;
import cise.mergeinfo.MergedGraphInfo;
import cise.utils.UtilFunction.CollectionUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class IslandModel {
    public int islandNum = 10;
    public int migrationNum = 30;
    public int migrationGens = 50;
    public int maxGenerations = 1000;
    public int individualsPerIsland = 250;

    public int maxEntropyKeep = 100;
    public final double targetEntropy = 0.0;
    public long totalTime = 0;

    public boolean quiet = false;

    private Collection<Graph> graphSet;

    public IslandModel(Collection<Graph> graphSet) {
        this.graphSet = graphSet;
    }

    public IslandModel(int graphNum, Collection<Graph> graphSet) {
        int totalIndividual = graphNum * 100;
        this.individualsPerIsland = totalIndividual / islandNum;
        this.maxGenerations = graphNum * 100;
        this.migrationGens = graphNum * 5;
        this.migrationNum = (islandNum - 1) * graphNum;
        this.maxEntropyKeep = graphNum * 20;
        this.graphSet = graphSet;
    }




    public IslandModel(int islandNum, int migrationNum, int migrationGens, int maxGenerations, int individualsPerIsland, Collection<Graph> graphSet) {
        this.islandNum = islandNum;
        this.migrationNum = migrationNum;
        this.migrationGens = migrationGens;
        this.maxGenerations = maxGenerations;
        this.individualsPerIsland = individualsPerIsland;
        this.graphSet = graphSet;
    }
    public IslandModel(int islandNum,int graphNum,Collection<Graph> graphSet)
    {
        this.islandNum=islandNum;
        int totalIndividual = graphNum * 100;
        this.individualsPerIsland = totalIndividual / islandNum;
        this.maxGenerations = graphNum * 100;
        this.migrationGens = graphNum * 5;
        this.migrationNum = (islandNum - 1) * graphNum;
        this.maxEntropyKeep = graphNum * 20;
        this.graphSet = graphSet;
    }


    public MergedGraphInfo run() {
        long startTime = System.currentTimeMillis();
        // 创建子种群
        List<GAProcess> subPopulations = new ArrayList<>();
        for (int i = 0; i < islandNum; i++) {
            GAProcess newGA = new GAProcess(individualsPerIsland, 0.2, migrationGens, migrationGens, graphSet,islandNum);
            newGA.parallel = true;
            newGA.quiet = true;
            newGA.initialize();
            subPopulations.add(newGA);
        }

        int currentGens = 0;
        double minEntropy = Double.MAX_VALUE;
        int entropyKeep = 0;
        while (true) {

            // 判断结束
            // 目标熵达到
            if (Math.abs(minEntropy - targetEntropy) < 1e-8) {
                break;
            }
            // 最大代数达到
            if (currentGens >= maxGenerations)
                break;
            // 熵保持了足够多代
            if (entropyKeep >= maxEntropyKeep)
                break;

            if (!quiet)
                System.out.println(currentGens + "+ Start");



            //  挨个跑
            ExecutorService subExecutor = Executors.newFixedThreadPool(islandNum);
            subPopulations.forEach(GA -> subExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    GA.resetProcess();
                    GA.RunForIslandModel();
                    GA.calcAllEntropy();
                    GA.sortByEntropy();
                    GA.selection();
                    // System.out.println("Subpopulation finished, min entropy: " + GA.getMinEntropy());
                }
            }));
            subExecutor.shutdown();
            try {
                subExecutor.awaitTermination(Integer.MAX_VALUE, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
            }

            // 跑完交换个体
            List<Set<MergedGraphInfo>> removeFrom = new ArrayList<>();
            List<Set<MergedGraphInfo>> insertInto = new ArrayList<>();

            for (int i = 0; i < islandNum; i++) {
                // 最佳的个体不会被删除
                removeFrom.add(new HashSet<>(new CollectionUtil<MergedGraphInfo>()
                    .pickRandom(subPopulations.get(i).population.subList(1, subPopulations.get(i).population.size()), migrationNum)));
                insertInto.add(new HashSet<>());

            }
            for (int provider = 0; provider < islandNum; provider++) {
                // 将top的融合方案复制一个出来，而不能是引用
                List<MergedGraphInfo> tops = new ArrayList<>();
                List<MergedGraphInfo> bestIndividuals = subPopulations.get(provider).population.subList(0, migrationNum);
                for (MergedGraphInfo mvinfo : bestIndividuals) {
                    tops.add(mvinfo.getClone());
                }
                Collections.shuffle(tops);
                int moveCnt = 0;
                for (int accepter = 0; accepter < islandNum; accepter++) {
                    if (accepter == provider)
                        continue;
                    for (int i = 0; i < migrationNum / (islandNum - 1); i++) {
                        insertInto.get(accepter).add(tops.get(moveCnt));
                        moveCnt++;
                    }
                }
                assert moveCnt == tops.size() && moveCnt == migrationNum;
            }

            // 先删除再插入
            for (int i = 0; i < islandNum; i++) {

                subPopulations.get(i).population.removeAll(removeFrom.get(i));
                subPopulations.get(i).population.addAll(insertInto.get(i));
                assert
                    subPopulations.get(i).population.size() == subPopulations.get(i).populationSize;
                // 重新计算熵然后排序
                subPopulations.get(i).calcAllEntropy();
                subPopulations.get(i).sortByEntropy();
            }

            // 交换完成
            currentGens += migrationGens;
            // 更新最小熵
            boolean entropyNotChanged = true;
            for (GAProcess ga : subPopulations) {
                double gaMinEntropy = ga.getMinEntropy();
                if (minEntropy - gaMinEntropy > 1e-8) {
                    entropyNotChanged = false;
                    minEntropy = gaMinEntropy;
                }
            }
            if (!quiet)
                System.out.println("Generation " + currentGens + " Finished, minEntropy: " + minEntropy);

            if (entropyNotChanged) {
                entropyKeep += migrationGens;
            }
            else {
                entropyKeep = 0;
            }
        }

        // 返回最小熵个体
        MergedGraphInfo bestResult = null;
        double minEn = Double.MAX_VALUE;
        for (GAProcess ga : subPopulations) {
            if (ga.getMinEntropy() < minEn) {
                bestResult = ga.getBestResult();
            }
        }

        totalTime = System.currentTimeMillis() - startTime;

        if (!quiet) {
            System.out.println("All finished: Best Entropy " + bestResult.getEntropy());
            System.out.println("Time: " + totalTime);
        }
        return bestResult;
    }
}
