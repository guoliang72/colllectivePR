package cise.ga;

import cise.graphcomponent.Graph;
import cise.graphcomponent.MergedGraph;
import cise.mergeinfo.BasicEntropyCalculator;
import cise.mergeinfo.Entropy;
import cise.mergeinfo.GraphsInfo;
import cise.mergeinfo.MergedGraphInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GAProcess {
    public int populationSize;
    public double mutationRate;
    public int maxGeneration;
    public double targetEntropy = 0;
    // 一个个体最多占总种群数量的多大比例
    public double maxDiversityRate = 0.03;
    public GraphsInfo graphsInfo;

    public int entropyType = 0;
    public List<Double> entropyTrace = new ArrayList<>(maxGeneration);

    public boolean quiet = true;
    public boolean parallel = true;
    public int threadNum = 4;

    private int currentGeneration = 0;

    private int lastEntropySize = 100;
    public double minEntropy = 2147483647.0;
    private int currentEntropyGens = 0;

    public int totalTime = 0;

    List<MergedGraphInfo> population = new ArrayList<>();
    List<MergedGraphInfo> newGeneration = Collections.synchronizedList(new ArrayList<>());

    public GAProcess(int size, double rate, int maxGeneration, int stopCounter, Collection<Graph> graphSet) {
        this.populationSize = size;
        this.mutationRate = rate;
        this.maxGeneration = maxGeneration;
        this.lastEntropySize = stopCounter;
        graphsInfo = new GraphsInfo(graphSet);
    }
    public GAProcess(int size, double rate, int maxGeneration, int stopCounter, Collection<Graph> graphSet,int threadNum) {
        this.populationSize = size;
        this.mutationRate = rate;
        this.maxGeneration = maxGeneration;
        this.lastEntropySize = stopCounter;
        graphsInfo = new GraphsInfo(graphSet);
        this.threadNum=threadNum;
    }

    /**
     * 初始化种群
     */
    public void initialize() {
        // 随机生成size个融合图
        for (int i = 0; i < populationSize; i++) {
            MergedGraphInfo individual = new MergedGraphInfo(graphsInfo);
            individual.randomMergeGraph();
            population.add(individual);
        }
    }

    public void calcAllEntropy() {

        ExecutorService entropyExecutor = Executors.newFixedThreadPool(parallel?threadNum:1);
        population.forEach(mergedGraphInfo -> entropyExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Entropy entropyCalculator = new BasicEntropyCalculator();
                mergedGraphInfo.setEntropy(entropyCalculator.calculateEntropy(mergedGraphInfo));
            }
        }));
        entropyExecutor.shutdown();
        try {
            entropyExecutor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
        }

    }

    public void sortByEntropy() {
        population.sort((o1, o2) -> new Double(o1.getEntropy()).compareTo(o2.getEntropy()));
    }

    public void selection() {
        // 保留前size个个体
        List<MergedGraphInfo> newPopulation = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            newPopulation.add(population.get(i));
        }
        this.population = newPopulation;

    }

    public void crossover(List<MergedGraphInfo> selectList) {
        Collections.shuffle(selectList);
        ExecutorService crossoverExecutor = Executors.newFixedThreadPool(parallel?threadNum:1);
        // Crossover crossover = new BasicCrossover();
        for (int i = 0; i+1 < selectList.size(); i += 2) {
            MergedGraphInfo g1 = selectList.get(i);
            MergedGraphInfo g2 = selectList.get(i+1);
            crossoverExecutor.execute(() -> newGeneration.add(new BasicCrossover().crossover(g1, g2)));
        }
        crossoverExecutor.shutdown();
        try {
            crossoverExecutor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
        }
    }

    public void resetProcess() {
        currentGeneration = 0;
        minEntropy = 2147483647.0;
        currentEntropyGens = 0;
    }

    public boolean isFinish() {
        // 最大代数达到
        if (currentGeneration >= maxGeneration)
            return true;
        if (currentGeneration == 0) {
            return false;
        }
        // 当前最小熵
        double currentEntropy = population.get(0).getEntropy();

        // 目标熵达到
        if (Math.abs(targetEntropy - currentEntropy) < 1e-8) {
            return true;
        }

        // 收敛
        if (Math.abs(currentEntropy - minEntropy) < 1e-8) {
            minEntropy = currentEntropy < minEntropy?currentEntropy:minEntropy;
            currentEntropyGens++;
        }
        else if (currentEntropy < minEntropy) {
            minEntropy = currentEntropy;
            currentEntropyGens = 0;
        }
        else {
            currentEntropy++;
        }
        if (currentEntropyGens >= lastEntropySize) {
            return true;
        }

        return false;
    }

    public int keepDiversity() {
        // 检查种群中的重复个体
        List<MergedGraphInfo> filteredPopulation = new ArrayList<>();

        for (MergedGraphInfo mgInfo : population) {
            int numberInFilteredPopulation = 0;
            int upbound = Math.max((int) (population.size() * maxDiversityRate), 2);
            for (MergedGraphInfo savedMGInfo : filteredPopulation) {
                if (MergedGraph.isSame(mgInfo.getMergedGraph(), savedMGInfo.getMergedGraph())) {
                    numberInFilteredPopulation++;
                }
                if (numberInFilteredPopulation >= upbound) {
                    break;
                }
            }
            // 超过上限，则种群中有过多和mginfo一样的融合方案，不再加入
            if (numberInFilteredPopulation < upbound) {
                filteredPopulation.add(mgInfo);
            }
        }

        int removed = population.size() - filteredPopulation.size();

        // 所有个体经过筛选后，补充随机个体
        while (filteredPopulation.size() < populationSize) {
            MergedGraphInfo newMergedGraph = new MergedGraphInfo(graphsInfo);
            newMergedGraph.randomMergeGraph();
            filteredPopulation.add(newMergedGraph);
        }

        // 用新的种群替换原有种群，没有经过熵计算
        population = filteredPopulation;

        return removed;
    }

    public void runRound2() {


        boolean timing = true;
        long currentTime = System.currentTimeMillis();
        boolean outputFlag = (!quiet) && timing && currentGeneration % 50 == 0;

        // 计算适应度
        calcAllEntropy();
        // 排序
        sortByEntropy();
        // 保留前种群规模个
        selection();

        // 种群多样性
        int removed = keepDiversity();
        // System.out.println(removed + " Individuals removed");
        calcAllEntropy();
        sortByEntropy();

        if (outputFlag)
            System.out.println("Generation: " + currentGeneration + ", min entropy: " + population.get(0).getEntropy() + " Max Entropy: " + population.get(population.size() - 1).getEntropy());

        if (outputFlag) {
            System.out.print("entropy: " + (System.currentTimeMillis() - currentTime) + ", " );
            currentTime = System.currentTimeMillis();
        }

        // 轮盘赌进行200次选择，得到交叉的候选
        Selector rouletteSeletor = new RouletteSelector();
        List<MergedGraphInfo> selectList = new ArrayList<>(rouletteSeletor.multipleSelect(population, populationSize * 2));

        if (outputFlag) {
            System.out.print("selection: " + (System.currentTimeMillis() - currentTime) + ", " + selectList.size() + " ");
            currentTime = System.currentTimeMillis();
        }

        crossover(selectList);

        if (outputFlag) {
            System.out.print("crossover: " + (System.currentTimeMillis() - currentTime) + ", " );
            currentTime = System.currentTimeMillis();
        }

        // 对子代进行变异
        Mutator mutator = new BasicMutator(mutationRate);
        for (MergedGraphInfo mergedGraphInfo : newGeneration) {
            mutator.mutate(mergedGraphInfo);
        }

        //对原种群除熵最低的几个之外也进行变异
        Mutator mutator2 = new BasicMutator(mutationRate);
        double keepBest = 0.1;
        for (int i = (int) (populationSize * keepBest); i < population.size(); i++) {
            mutator2.mutate(population.get(i));
        }

        if (outputFlag) {
            System.out.println("mutation: " + (System.currentTimeMillis() - currentTime) + ", " );
            currentTime = System.currentTimeMillis();
        }

        // 子代加入种群
        population.addAll(newGeneration);
        newGeneration.clear();

        currentGeneration++;
//        // 计算适应度
//        calcAllEntropy();
//        // 排序
//        sortByEntropy();
//        // 保留前种群规模个
//        selection();
//
//        if (outputFlag) {
//            System.out.println("entropy: " + (System.currentTimeMillis() - currentTime) + ", " );
//            currentTime = System.currentTimeMillis();
//        }
    }


    public MergedGraphInfo Run() {

        long startTime = System.currentTimeMillis();

        initialize();
        if (!quiet) {
            System.out.println("Initialize Finish");
        }
//        // 计算适应度
//        calcAllEntropy();
//        // 排序
//        sortByEntropy();
        while (!isFinish()) {
            runRound2();
        }

        calcAllEntropy();
        sortByEntropy();
        selection();

        totalTime = (int) (System.currentTimeMillis() - startTime);

        if (!quiet) {
            StringBuilder res = new StringBuilder();
            res.append("Finished\n");
            res.append("Total generation: " + currentGeneration + "\n");
            res.append(String.format("min entropy: %s\n", population.get(0).getEntropy()));
            res.append("Total time: " + totalTime);
            System.out.println(res);
        }
        return population.get(0);
    }

    public MergedGraphInfo RunForIslandModel() {
        // 没有初始化
        while (!isFinish()) {
            runRound2();
        }
        calcAllEntropy();
        sortByEntropy();
        selection();

        return population.get(0);
    }

    public int getFinalGeneration() {
        return currentGeneration;
    }

    public MergedGraphInfo getBestResult() {return population.get(0); }

    public double getMinEntropy() {
        return population.get(0).getEntropy();
    }

    public List<Double> getEntropyTrace(){
        return entropyTrace;
    }
}
