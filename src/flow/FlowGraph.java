package flow;

import util.Graph;
import java.util.*;

public class FlowGraph {
    public Graph<BasicBlock> graph = new Graph<BasicBlock>();

    public void add(BasicBlock block) {
        graph.addNode(block);
    }

    public void addEdge(BasicBlock from, BasicBlock to) {
        graph.addEdge(from, to);
    }

    public Set<BasicBlock> nodes() {
        return graph.nodes();
    }

    public Set<BasicBlock> pred(BasicBlock b) {
        return graph.pred(b);
    }

    public Set<BasicBlock> succ(BasicBlock b) {
        return graph.succ(b);
    }
}

