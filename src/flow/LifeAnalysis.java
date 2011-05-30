package flow;

import util.Graph;
import java.util.*;
import intermediate.Temp;
import arch.Instruction;

public class LifeAnalysis {
    FlowGraph g;

    Map<BasicBlock, HashSet<Temp>> inBlock = new HashMap<BasicBlock, HashSet<Temp>>(),
        outBlock = new HashMap<BasicBlock, HashSet<Temp>>();
    Map<Instruction, HashSet<Temp>> inIns = new HashMap<Instruction, HashSet<Temp>>(),
        outIns = new HashMap<Instruction, HashSet<Temp>>();

    public LifeAnalysis(FlowGraph g) {
        this.g = g;
        analysis();
    }

    public Set<Temp> in(BasicBlock b) {
        if (inBlock.containsKey(b))
            return inBlock.get(b);
        else
            return new HashSet<Temp>();
    }

    public Set<Temp> out(BasicBlock b) {
        if (outBlock.containsKey(b))
            return outBlock.get(b);
        else
            return new HashSet<Temp>();
    }

    public Set<Temp> in(Instruction i) {
        if (inIns.containsKey(i))
            return inIns.get(i);
        else
            return new HashSet<Temp>();
    }

    public Set<Temp> out(Instruction i) {
        if (outIns.containsKey(i))
            return outIns.get(i);
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
            inBlock.put(b, new HashSet<Temp>());
            outBlock.put(b, new HashSet<Temp>());
        }

        boolean change = false;
        do {
            change = false;
            for (BasicBlock b: blocks) {
                for (BasicBlock n: g.succ(b))
                    if (outBlock.get(b).addAll(inBlock.get(n)))
                        change = true;
                
                HashSet<Temp> t = new HashSet<Temp>(outBlock.get(b));
                t.removeAll(b.def());
                t.addAll(b.use());
                inBlock.put(b, t);
            }
        } while (change);

        for (BasicBlock b: blocks) {
            HashSet<Temp> current = new HashSet<Temp> (outBlock.get(b));

            Iterator<Instruction> iter = b.list.descendingIterator();
            Instruction ins = null;
            while (iter.hasNext()) {
                ins = iter.next();
                outIns.put(ins, new HashSet<Temp>(current));
                current.removeAll(ins.def());
                current.addAll(ins.use());
                inIns.put(ins, new HashSet<Temp>(current));
            }
        }
    }
}

