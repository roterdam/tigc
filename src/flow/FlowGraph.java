package flow;

import util.Graph;
import java.util.*;

public class FlowGraph {
    public Graph<BasicBlock> graph = new Graph<BasicBlock>();
    Map<BasicBlock, BasicBlock> next = new HashMap<BasicBlock, BasicBlock>();
    public BasicBlock entry = null;

    public void add(BasicBlock block, boolean entry) {
        graph.addNode(block);
        if (entry)
            this.entry = block;
    }

    public void add(BasicBlock block) {
        if (entry == null)
            add(block, true);
        else
            add(block, false);
    }

    public void addEdge(BasicBlock from, BasicBlock to, boolean isNext) {
        graph.addEdge(from, to);
        if (isNext)
            addNext(from, to);
    }

    public boolean isEdge(BasicBlock from, BasicBlock to) {
        return graph.isDirectedEdge(from, to);
    }

    public void addNext(BasicBlock from, BasicBlock to) {
        next.put(from, to);
    }

    public BasicBlock next(BasicBlock current) {
        if (this.next.containsKey(current))
            return this.next.get(current);
        else
            return null;
    }

    public void removeUnreachableNodes() {
        boolean change = false;
        do
        {
            change = false;
            for (BasicBlock b: new HashSet<BasicBlock>(graph.heads()))
                if (b != entry) {
                    graph.removeNode(b);
                    next.remove(b);
                    change = true;
                }
        } while (change);
    }

    public Set<BasicBlock> nodes() {
        return graph.nodes();
    }

    public Set<BasicBlock> heads() {
        return graph.heads();
    }

    public Set<BasicBlock> tails() {
        return graph.tails();
    }

    public boolean has(BasicBlock b) {
        return graph.hasNode(b);
    }

    public Set<BasicBlock> pred(BasicBlock b) {
        return graph.pred(b);
    }

    public Set<BasicBlock> succ(BasicBlock b) {
        return graph.succ(b);
    }
}

