package mips32;

import flow.BasicBlock;
import flow.FlowGraph;
import java.util.*;
import intermediate.Label;

public class FlowGraphGenerator extends flow.FlowGraphGenerator {
    public FlowGraph build(arch.InstructionList olist) {
        if (!(olist instanceof InstructionList))
            return null;

        InstructionList list = (InstructionList) olist;
        HashMap<Label, BasicBlock> labelMap = new HashMap<Label, BasicBlock>();
        HashMap<BasicBlock, BasicBlock> next = new HashMap<BasicBlock, BasicBlock>();
        BasicBlock current = new BasicBlock(), t = null;
        ArrayList<Label> labels = new ArrayList<Label>();
        FlowGraph graph = new FlowGraph();
        ArrayList<BasicBlock> blocks = new ArrayList<BasicBlock>();

        for (LabeledInstruction i: list) {
            if (i.label != null) {
                if (!current.isInsEmpty()) {
                    blocks.add(current);
                    graph.add(current);
                    t = new BasicBlock();
                    next.put(current, t);
                    current = t;
                }

                current.add(i.label);
                labelMap.put(i.label, current);
            }
            if (i.instruction != null) {
                current.add(i.instruction);
                if (i.instruction.isJump()) {
                    blocks.add(current);
                    graph.add(current);
                    t = new BasicBlock();
                    next.put(current, t);
                    current = t;
                } else if (i.instruction.isExit()) {
                    blocks.add(current);
                    graph.add(current);
                    current = new BasicBlock();
                }
            }
        }
        if (current.isEmpty()) {
            blocks.add(current);
            graph.add(current);
        }

        for (BasicBlock b: blocks) {
            if (b.isInsEmpty() || !b.getLast().isJump()) {
                if (next.containsKey(b) && blocks.contains(next.get(b)))
                    graph.addEdge(b, next.get(b), true);
            } else {
                Instruction ins = (Instruction) b.getLast();
                if (ins.type == Instruction.Type.JR) {
                    for (Label p: ins.frame.returns)
                        graph.addEdge(b, labelMap.get(p), false);
                } else {
                    graph.addEdge(b, labelMap.get(ins.target), false);
                    if (ins.isBranch() && next.containsKey(b) && blocks.contains(next.get(b)))
                        graph.addEdge(b, next.get(b), true);
                    else if (ins.type == Instruction.Type.JAL && next.containsKey(b))
                        graph.addNext(b, next.get(b));
                }
            }
        }

        return graph;
    }
}

