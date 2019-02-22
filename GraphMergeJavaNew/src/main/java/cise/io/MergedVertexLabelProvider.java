package cise.io;

import cise.graphcomponent.MergedVertex;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.jgrapht.io.ComponentNameProvider;

public class MergedVertexLabelProvider implements ComponentNameProvider<MergedVertex> {

    @Override
    public String getName(MergedVertex mergedVertex) {
        Map<String, List<Integer>> nameToGraphIdMap = new HashMap<>();
        mergedVertex.getAllVertex().forEach(vertex -> {
            if (!nameToGraphIdMap.containsKey(vertex.name)) {
                nameToGraphIdMap.put(vertex.name, new ArrayList<>());
            }
            nameToGraphIdMap.get(vertex.name).add(vertex.graph.id);
        });

        StringBuilder ret = new StringBuilder();

        Set<Entry<String, List<Integer>>> entrySet = nameToGraphIdMap.entrySet();
        for (Entry<String, List<Integer>> entry : entrySet) {
            entry.getValue().sort(Integer::compareTo);
            ret.append(entry.getKey());
            ret.append(" : ");
            ret.append(entry.getValue());
            ret.append("\n");
        }

        return ret.toString().replace("\\", "\\\\").replace("\"", "\\\"");
     }
}
