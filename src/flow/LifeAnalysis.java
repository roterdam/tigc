package flow;

import util.Graph;
import java.util.*;
import intermediate.Temp;

public class LifeAnalysis {
    FlowGraph g;

    Map<BasicBlock, HashSet<Temp>> in = new HashMap<BasicBlock, HashSet<Temp>>(),
        out = new HashMap<BasicBlock, HashSet<Temp>>();

    public LifeAnalysis(FlowGraph g) {
        this.g = g;
        analysis();
    }

    public Set<Temp> in(BasicBlock b) {
        if (g.has(b))
            return in.get(b);
        else
            return new HashSet<Temp>();
    }

    public Set<Temp> out(BasicBlock b) {
        if (g.has(b))
            return out.get(b);
        else
            return new HashSet<Temp>();
    }

    private void explore(BasicBlock b, Set<BasicBlock> visited, LinkedList<BasicBlock> blocks) {
        if (visited.contains(b))
            return;

        blocks.add(b);
        visited.add(b);

        for (BasicBlock n: g.pred(b))
            explore(n, visited, blocks);
    }

    private void analysis() {
        LinkedList<BasicBlock> blocks = new LinkedList<BasicBlock>();
        Set<BasicBlock> visited = new HashSet<BasicBlock>();
        Set<BasicBlock> tails = g.tails();
        Set<BasicBlock> nodes = g.nodes();
        for (BasicBlock b: tails)
            explore(b, visited, blocks);
        for (BasicBlock b: nodes) {
            explore(b, visited, blocks);
            in.put(b, new HashSet<Temp>());
            out.put(b, new HashSet<Temp>());
        }

        int time = 0;
        boolean change = false;
        do {
            change = false;
            for (BasicBlock b: blocks) {
                for (BasicBlock n: g.succ(b))
                    if (out.get(b).addAll(in.get(n)))
                        change = true;
                
                HashSet<Temp> t = new HashSet<Temp>(out.get(b));
                t.removeAll(b.def());
                t.addAll(b.use());
                in.put(b, t);
            }
            ++time;
        } while (change);
    }
}

