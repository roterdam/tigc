package mips32;

import flow.FlowGraph;
import flow.BasicBlock;
import intermediate.Label;
import java.util.*;

public class InstructionRewriter extends flow.InstructionRewriter {
    private void putCode(InstructionList list, BasicBlock block) {
        for (Label l: block.labels)
            list.add(l);
        for (arch.Instruction i: block)
            list.add((Instruction) i);
    }

    private void rewriteRecursive(FlowGraph flow, BasicBlock current, Map<BasicBlock, BasicBlock> head, InstructionList res, Set<BasicBlock> visited) {
        if (current == null || visited.contains(current))
            return;

        if (head.get(current) != current) {
            rewriteRecursive(flow, head.get(current), head, res, visited);
            return;
        }

        Set<BasicBlock> remaining = new HashSet<BasicBlock>();
        while (true) {
            visited.add(current);
            putCode(res, current);

            remaining.addAll(flow.succ(current));

            if (flow.next(current) != null)
                current = flow.next(current);
            else
                break;
        }

        for (BasicBlock b: remaining)
            rewriteRecursive(flow, b, head, res, visited);
    }

    public InstructionList rewrite(FlowGraph flow) {
        Map<BasicBlock, BasicBlock> prev = new HashMap<BasicBlock, BasicBlock>();
        for (BasicBlock b: flow.nodes())
            if (flow.next(b) != null)
                prev.put(flow.next(b), b);

        Map<BasicBlock, BasicBlock> head = new HashMap<BasicBlock, BasicBlock>();
        for (BasicBlock b: flow.nodes()) {
            if (prev.containsKey(b)) {
                BasicBlock h = prev.get(b);
                while (prev.containsKey(h))
                    h = prev.get(h);
                prev.put(b, h);
                head.put(b, h);
            } else
                head.put(b, b);
        }

        InstructionList ret = new InstructionList();
        BasicBlock current = flow.entry;
        Set<BasicBlock> visited = new HashSet<BasicBlock>();
        rewriteRecursive(flow, current, head, ret, visited);
        return ret;
    }
}

