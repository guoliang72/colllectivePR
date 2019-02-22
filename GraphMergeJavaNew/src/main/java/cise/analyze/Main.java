package cise.analyze;

import cise.graphcomponent.Vertex;
import cise.io.MergedGraphIO;
import cise.mergeinfo.MergedGraphInfo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String argv[]) throws IOException {
        // load merged graph
        //String pathPrefix = "/Users/zhangzhifan/PDGMerge/Data/CM/CM3/5/4";
        String inputFile="2018_11_08_22_45_19.out";
        File directory = new File("");//设定为当前文件夹
        String pathPrefix = "";
        try{
            System.out.println(directory.getAbsolutePath());//获取绝对路径
            pathPrefix = directory.getAbsolutePath();
        }catch(Exception e){

        }
        String pathPrefixList = pathPrefix+"/../analyze";
        MergedGraphInfo unserialized = MergedGraphIO.MergedGraphImporter.loadMergedGraph(
                Paths.get(pathPrefix, inputFile).toString());

        File targetFile=new File(pathPrefix+"/"+inputFile+".txt");
        if(targetFile.exists())
        {
            System.out.println("the file already exist");
        }

        System.out.println(unserialized.getEntropy());

        BufferedWriter outputStream = new BufferedWriter(new FileWriter(targetFile));

        outputStream.write(String.valueOf(unserialized.getEntropy()));
        outputStream.newLine();

        BasicFaultLocater bfl = new BasicFaultLocater(unserialized);
        List<Fault> faultVertices = new ArrayList<>(bfl.findFault());
        Map<Vertex, List<Fault>> vfMap = new HashMap<>();
        faultVertices.forEach(fault -> {
            if (!vfMap.containsKey(fault.v)) {
                vfMap.put(fault.v, new ArrayList<>());
            }
            List<Fault> fList = vfMap.get(fault.v);
            fList.add(fault);
        });

        List<Vertex> vList = new LinkedList<>(vfMap.keySet());
        Collections.sort(vList, new Comparator<Vertex>() {
            @Override
            public int compare(Vertex o1, Vertex o2) {
                if (o1.graph.id != o2.graph.id) {
                    return o1.graph.id - o2.graph.id;
                }
                else {
                    return o1.line - o2.line;
                }
            }
        });

        vList.forEach(vertex -> {
            String out1=vertex.graph.name + " " + vertex.line + ": " + vertex.name;
            System.out.println(out1);
            try{
                outputStream.write(out1);
                outputStream.newLine();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            List<Fault> fList = vfMap.get(vertex);
            fList.forEach(Fault->System.out.println("    " + Fault.message));
        });
        outputStream.close();
//        Collections.sort(faultVertices, new Comparator<Fault>() {
//            @Override
//            public int compare(Fault o1, Fault o2) {
//                if (o1.g.id != o2.g.id) {
//                    return o1.g.id - o2.g.id;
//                }
//                else {
//                    if (o1.line != o2.line) {
//                        return o1.line - o2.line;
//                    }
//                    else {
//                        return 0;
//                    }
//                }
//            }
//        });

        // faultVertices.forEach(fault ->System.out.println(fault));


    }
}
