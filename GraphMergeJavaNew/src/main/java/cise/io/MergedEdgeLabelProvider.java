package cise.io;

import cise.graphcomponent.MergedEdge;
import org.jgrapht.io.ComponentNameProvider;

public class MergedEdgeLabelProvider implements ComponentNameProvider<MergedEdge> {

    @Override
    public String getName(MergedEdge mergedEdge) {
        return mergedEdge.type.value;
    }
}
