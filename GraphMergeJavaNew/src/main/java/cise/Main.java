package cise;

import cise.ga.GAProcess;
import cise.ga.IslandModel;
import cise.graphcomponent.Graph;
import cise.graphcomponent.MergedGraph;
import cise.io.Exporter;
import cise.io.Importer;
import cise.io.MergedGraphIO;
import cise.mergeinfo.MergedGraphInfo;
import cise.mergeinfo.VertexSimilarity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String argv[]) {
        File directory = new File("");//设定为当前文件夹
        String abpath = "";
        try{
            System.out.println(directory.getAbsolutePath());//获取绝对路径
            abpath = directory.getAbsolutePath();
        }catch(Exception e){

        }
        String[] pathPrefixList = {abpath+"/../test/1",abpath+"/../test/2",abpath+"/../test/3", abpath+"/../test/4", abpath+"/../test/5",
                abpath+"/../test/6",abpath+"/../test/7",abpath+"/../test/8", abpath+"/../test/9",abpath+"/../test/10"
        };
///Users/zhangzhifan/PDGMerge/Data/论文_同构图融合效果实验/相同图/程序m-统计药费/5图",
//                "/Users/zhangzhifan/PDGMerge/Data/论文_同构图融合效果实验/相同图/程序m-统计药费/10图",
//                "/Users/zhangzhifan/PDGMerge/Data/论文_同构图融合效果实验/相同图/程序m-统计药费/15图",
//                "/Users/zhangzhifan/PDGMerge/Data/论文_同构图融合效果实验/相同图/程序m-统计药费/20图"

        for (String pathPrefix : pathPrefixList) {
        List<Graph> graphList = Importer.importDotFileInDir(pathPrefix);
        System.out.println(graphList.size());
        int GACnt = 2;
        List<Integer> vNumList = new ArrayList<>();
        List<Double> entropyList = new ArrayList<>();
        System.out.println(Runtime.getRuntime().availableProcessors());
        for (int i = 1; i <= GACnt; i++) {
            VertexSimilarity.reset();
            // 原图节点数：
            System.out.println("PDG Vertex Num: " + graphList.get(0).vertexSet().size());

//            // 普通GA
//            GAProcess ga = new GAProcess(1000, 0.2, 1000, 200, graphList);
//            ga.quiet = false;
//            MergedGraphInfo bestResultInfo = ga.Run();
//            MergedGraph bestResult = bestResultInfo.getMergedGraph();
//            Exporter.exportDot(bestResult, pathPrefix + "\\result" + i + ".res");

            // 并行GA
            int islandNum=10;
            IslandModel ga = new IslandModel(islandNum,graphList.size(), graphList);
            System.out.println("max gen: " + ga.maxGenerations);
            MergedGraphInfo bestResultInfo = ga.run();
            MergedGraph bestResult = bestResultInfo.getMergedGraph();
            Exporter.exportDot(bestResult, pathPrefix + File.pathSeparator + "result" + i + ".res");

            // 最终融合结果节点数：
            //System.out.println("MPDG Vertex Num: " + bestResult.vertexSet().size());
            vNumList.add(bestResult.vertexSet().size());
            entropyList.add(bestResultInfo.getEntropy());
            // 序列化
            MergedGraphIO.MergedGraphExporter.saveMergedGraph(bestResultInfo, pathPrefix);
        }
        System.out.println(vNumList);
        System.out.println(entropyList);

    }
    }

    public static void timeTest() {
        // 时间测试
        String pathPrefix = "E:\\CISE\\Data\\ProgrammingGrids\\Experiment\\同构小规模测试";
        List<Graph> graphList = Importer.importDotFileInDir(pathPrefix);

        // 普通GA
        System.out.println("Normal GA start");
        GAProcess ga = new GAProcess(2000, 0.2, 1000, 200, graphList);
        ga.quiet = true;
        ga.parallel = false;
        ga.Run();
        System.out.println("Normal GA Time: " + ga.totalTime + " Entropy: " + ga.getMinEntropy());

        // 重置
        VertexSimilarity.reset();

        // 并行GA
        System.out.println("Parallel GA start");
        GAProcess pga = new GAProcess(1000, 0.2, 1000, 200, graphList);
        pga.quiet = true;
        pga.parallel = true;
        pga.threadNum = 16;
        pga.Run();
        System.out.println("Parallel GA Time: " + pga.totalTime + " Entropy: " + pga.getMinEntropy());

        // 重置
        VertexSimilarity.reset();

        // 多种群
        System.out.println("MultiPopulation GA start");
        IslandModel islandGA = new IslandModel(graphList);
        islandGA.quiet = true;
        MergedGraphInfo res = islandGA.run();
        System.out.println("MultiPopulation Time: " + islandGA.totalTime + " Entropy: " + res.getEntropy());
    }
}

