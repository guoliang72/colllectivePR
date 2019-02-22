package cise.io;

import cise.graphcomponent.Edge;
import cise.graphcomponent.Graph;
import cise.graphcomponent.Vertex;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jgrapht.io.DOTImporter;
import org.jgrapht.io.GraphImporter;
import org.jgrapht.io.ImportException;

/**
 * 修改PDG结构后，PDG节点内会携带该节点语句的AST作为子图
 * 子图用于判断节点相似度
 *
 * 而dotimport无法简单地处理子图导入，故增加此人工步骤
 *
 */
class GraphModifier {

    public static Set<String> astVertexTypeSet = new HashSet<>(Arrays.asList(new String[]{
        "OP", "ASSIGNOP", "VAR", "ARRVAR", "CONST", "FUNCNAME", "PARAMETER", "EXPRS"
    }));

    public static Set<String> astEdgeTypeSet = new HashSet<>(Arrays.asList(new String[]{
        "AST", "DEFINEVAR", "USEVAR", "VARREF", "SUBSCR", "LEFTVAR", "RIGHTVAR", "OPVAR", "PARAMETER", "PARLIST", "NAME"
    }));

    Set<Vertex> needRemoveVertex = new HashSet<>();
    Set<Edge> needRemoveEdge = new HashSet<>();
    Graph pdg;

    public GraphModifier(Graph origin) {
        pdg = origin;
    }
    /**
     * 提供一个原始PDG，对于其中每个节点，如果其有AST子图，则将他们添加到节点自身的属性内
     * 之后从原PDG中删除这部分AST节点和变
     * @param
     */
    public Graph modifiyPDG() {

        // 对于每个非AST节点
        for (Vertex v : pdg.vertexSet()) {
            if (astVertexTypeSet.contains(v.type.value)) {
                continue;
            }
            // 查看其类型为AST的出边
            // AST类型的出边只存在与PDG节点与其AST子图之间
            for (Edge e : pdg.outgoingEdgesOf(v)) {
                if (!e.type.value.equals("AST")) {
                    continue;
                }
                needRemoveEdge.add(e);
                // 从这个边的目标节点开始的子图就是节点v的AST
                // 将其整个复制到一个新图中。
                Vertex astRoot = pdg.getEdgeTarget(e);
                // 新图
                Graph astSubgraph = new Graph();
                // 递归入图
                addToAstGraphDFS(astRoot, astSubgraph);
                // astSubgraph完成，加入节点v的属性中
                v.astSubgraph = astSubgraph;
                for (Vertex vertex : astSubgraph.vertexSet()) {
                    vertex.graph = astSubgraph;
                }
                for (Edge edge : astSubgraph.edgeSet()) {
                    edge.graph = astSubgraph;
                }
            }


        }

        // 所有节点完成后，从原图中删掉AST节点和边
        pdg.removeAllEdges(needRemoveEdge);
        pdg.removeAllVertices(needRemoveVertex);



        return pdg;

    }

    public void addToAstGraphDFS(Vertex v, Graph subg) {
        // 节点入图
        if (subg.containsVertex(v)) {
            return;
        }
        subg.addVertex(v);
        // 所有子节点入图
        // AST子图与PDG其他部分独立，所以不会牵扯到PDG其他节点
        for (Edge e : pdg.outgoingEdgesOf(v)) {
            Vertex target = pdg.getEdgeTarget(e);
            // 递归子节点加入AST子图
            addToAstGraphDFS(target, subg);
            // 边加入AST子图
            subg.addEdge(v, target, e);
            // 这条边标记删除
            needRemoveEdge.add(e);
        }
        // 这个节点标记删除
        needRemoveVertex.add(v);
    }
}

public class Importer {

    public static Graph importDot(String path){
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(path);
        } catch (FileNotFoundException e) {
            System.out.println("未找到文件: " + path);
        }

        GraphImporter<Vertex, Edge> dotImporter = new DOTImporter<>(
            new DOTVertexProvider(), new DOTEdgeProvider());
        Graph inputGraph = new Graph();
        try {
            dotImporter.importGraph(inputGraph, fileReader);
        } catch (ImportException e) {
            e.printStackTrace();
        }

        File graphFile = new File(path);
        String graphName = graphFile.getName();

        // System.out.println(graphName);

        for (Vertex vertex : inputGraph.vertexSet()) {
            vertex.graph = inputGraph;
        }
        for (Edge edge : inputGraph.edgeSet()) {
            edge.graph = inputGraph;
        }
        inputGraph.name = graphName;

        // 导入后，将图中的AST节点删除，放到对应PDG节点内
        // inputGraph = new GraphModifier(inputGraph).modifiyPDG();


        return inputGraph;
    }

    public static List<Graph> importDotFileInDir(String path) {

        List<Graph> retGraphs = new ArrayList<>();

        File dir = new File(path);
        if (dir.exists()) {
            File[] fileList = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".gv") || name.endsWith(".dot");
                }
            });
            for (File file : fileList) {
                if (file.isFile())
                    retGraphs.add(importDot(file.getAbsolutePath()));
            }
        }
        retGraphs.forEach(graph -> new GraphModifier(graph).modifiyPDG());
        return retGraphs;
    }

    public static List<Graph> importDotFileList(String pathPrefix, int num) {
        List<Graph> retGraphs = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            retGraphs.add(importDot(pathPrefix + i + ".gv"));
        }
        return retGraphs;
    }
}


