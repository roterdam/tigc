package flow;

import util.Graph;
import java.util.*;
import intermediate.Temp;
import arch.Instruction;

public class ReachingDefinition {

    FlowGraph g;

    Map<BasicBlock, Set<Instruction>> inBlock = new HashMap<BasicBlock, Set<Instruction>>(),
        outBlock = new HashMap<BasicBlock, Set<Instruction>>();
    Map<Instruction, Set<Instruction>> inIns = new HashMap<Instruction, Set<Instruction>>();

    public ReachingDefinition(FlowGraph g) {
        this.g = g;
        analysis();
    }

    public Set<Instruction> in(BasicBlock b) {
        if (inBlock.containsKey(b))
            return inBlock.get(b);
        else
            return new HashSet<Instruction>();
    }

    public Set<Instruction> out(BasicBlock b) {
        if (outBlock.containsKey(b))
            return outBlock.get(b);
        else
            return new HashSet<Instruction>();
    }

    public Set<Instruction> in(Instruction i) {
        if (inIns.containsKey(i))
            return inIns.get(i);
        else
            return new HashSet<Instruction>();
    }

    private void explore(BasicBlock b, Set<BasicBlock> visited, LinkedList<BasicBlock> blocks) {
        if (visited.contains(b))
            return;

        blocks.add(b);
        visited.add(b);

        for (BasicBlock n: g.succ(b))
            explore(n, visited, blocks);
    }

    private void analysis() {
        LinkedList<BasicBlock> blocks = new LinkedList<BasicBlock>();
        Set<BasicBlock> visited = new HashSet<BasicBlock>();
        Map<Temp, Set<Instruction>> def = new HashMap<Temp, Set<Instruction>>();

        for (BasicBlock b: g.heads())
            explore(b, visited, blocks);
        for (BasicBlock b: g.nodes()) {
            explore(b, visited, blocks);

            inBlock.put(b, new HashSet<Instruction>());
            outBlock.put(b, new HashSet<Instruction>());

            for (Instruction i: b) {
                for (Temp t: i.def()) {
                    if (!def.containsKey(t))
                        def.put(t, new HashSet<Instruction>());
                    def.get(t).add(i);
                }
            }
        }

        Map<BasicBlock, Set<Instruction>> kill = new HashMap<BasicBlock, Set<Instruction>>(),
            gen = new HashMap<BasicBlock, Set<Instruction>>();
        for (BasicBlock b: blocks) {
            kill.put(b, new HashSet<Instruction>());
            gen.put(b, new HashSet<Instruction>());
            for (Instruction i: b) {
                for (Temp t: i.def())
                    if (def.containsKey(t))
                        kill.get(b).addAll(def.get(t));
                kill.get(b).remove(i);
                gen.get(b).removeAll(kill.get(b));
                gen.get(b).add(i);
            }
        }

        boolean change = false;
        int count = 0;
        do {
            change = false;
            for (BasicBlock b: blocks) {
                int os = inBlock.get(b).size();
                for (BasicBlock p: g.pred(b))
                    if (inBlock.get(b).addAll(outBlock.get(p)))
                        change = true;

                Set<Instruction> t = new HashSet<Instruction>(inBlock.get(b));
                t.removeAll(kill.get(b));
                t.addAll(gen.get(b));
                outBlock.put(b, t);
            }
            ++count;
        } while (change);


        for (BasicBlock b: blocks) {
            Set<Instruction> current = new HashSet<Instruction>(inBlock.get(b));
            for (Instruction i: b) {
                inIns.put(i, new HashSet<Instruction>(current));

                for (Temp t: i.def())
                    if (def.containsKey(t))
                        current.removeAll(def.get(t));
                current.add(i);
            }
        }

        System.out.println(new Integer(count).toString());
    }
}

