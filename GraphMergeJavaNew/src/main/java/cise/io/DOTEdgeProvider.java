package cise.io;

import cise.graphcomponent.Edge;
import cise.graphcomponent.Graph;
import cise.graphcomponent.Vertex;
import cise.utils.StringType;
import java.util.Map;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.EdgeProvider;

public class DOTEdgeProvider implements EdgeProvider<Vertex, Edge> {

    /**
     * 边属性映射</br> <ul> <li>label -> type</li> </ul>
     */
    public Edge buildEdge(Vertex from, Vertex to, String label, Map<String, Attribute> map) {
        //System.out.println(map.containsKey("label"));
        return new Edge(from, to, new StringType(label), Graph.DEFAULT);
    }
}
