package optimization;

import arch.Instruction;
import intermediate.Temp;
import intermediate.Label;
import flow.FlowGraph;
import flow.DominatingSet;
import flow.BasicBlock;
import flow.FlowGraphGenerator;
import flow.InstructionRewriter;
import arch.InstructionList;
import java.util.*;

public class LoopInvariantCodeMotion {
    FlowGraphGenerator fg;
    InstructionRewriter rewriter;

    public LoopInvariantCodeMotion(FlowGraphGenerator fg, InstructionRewriter rewriter) {
        this.fg = fg;
        this.rewriter = rewriter;
    }

    private boolean doms(DominatingSet dom, Map<Instruction, BasicBlock> instructionFrom,
            Instruction a, Instruction b) {
        BasicBlock ba = instructionFrom.get(a),
                   bb = instructionFrom.get(b);
        if (ba == null || bb == null)
            return false;
        if (ba != bb)
            return dom.get(ba).contains(bb);
        else
            return ba.before(a, b);
    }

    private Set<BasicBlock> reachBlocks(FlowGraph flow, BasicBlock head) {
        Set<BasicBlock> reach = new HashSet<BasicBlock>();
        BasicBlock current = head;
        while (!reach.contains(current)) {
            reach.add(current);
            if (flow.succ(current).size() != 1)
                break;
            current = new ArrayList<BasicBlock>(flow.succ(current)).get(0);
        };
        return reach;
    }

    private InstructionList processLoop(FlowGraph flow, BasicBlock head, BasicBlock tail, DominatingSet dom) {
        Map<Temp, List<Instruction>> definitions = new HashMap<Temp, List<Instruction>>();
        Map<Instruction, BasicBlock> instructionFrom = new HashMap<Instruction, BasicBlock>();
        Map<Temp, Set<Instruction>> useList = new HashMap<Temp, Set<Instruction>>();
        Set<Temp> invariants = new HashSet<Temp>();
        List<Instruction> invariantIns = new ArrayList<Instruction>();
        List<Label> oldPlace = head.labels;

        for (BasicBlock b: dom.get(head)) {
            for (Instruction i: b) {
                instructionFrom.put(i, b);
                for (Temp t: i.def()) {
                    if (!definitions.containsKey(t))
                        definitions.put(t, new ArrayList<Instruction>());
                    definitions.get(t).add(i);
                }
                for (Temp t: i.useList()) {
                    if (!useList.containsKey(t))
                        useList.put(t, new HashSet<Instruction>());
                    useList.get(t).add(i);
                }
            }
        }

        Set<BasicBlock> enumBlocks = new HashSet<BasicBlock>(dom.get(head));
        enumBlocks.retainAll(reachBlocks(flow, head));
        boolean change = false;
        boolean motion = false;
        do {
            change = false;
            for (BasicBlock b: enumBlocks) {
                for (Instruction i: b) {
                    if (!i.isJump() && !i.isLoad() && !i.isStore()
                            && !i.hasSideEffects()) {

                        boolean fail = false;
                        Set<Instruction> defPlace = new HashSet<Instruction>();
                        for (Temp t: i.def()) {
                            defPlace.addAll(definitions.get(t));
                            if (useList.containsKey(t)) {
                                for (Instruction use: useList.get(t))
                                    if (!doms(dom, instructionFrom, i, use)) {
                                        fail = true;
                                        break;
                                    }
                                if (fail)
                                    break;
                            }
                        }

                        if (fail || defPlace.size() != 1)
                            continue;

                        for (Temp t: i.useList()) {
                            if (definitions.containsKey(t) && !invariants.contains(t)) {
                                fail = true;
                                break;
                            }
                        }

                        if (fail)
                            continue;

                        change = true;
                        motion = true;
                        for (Temp t: i.def())
                            invariants.add(t);
                        invariantIns.add(i);

                        b.removeInstruction(i);
                    }
                }
            }
        } while (change);

        if (!motion)
            return null;

        Label newPlace = Label.newLabel();
        Instruction tailIns = tail.getLast();

        InstructionList list = rewriter.rewrite(flow);
        list.redirect(tailIns, oldPlace, newPlace);
        list.replaceLabel(oldPlace, newPlace);
        list.addAllBefore(oldPlace, invariantIns, newPlace);

        return list;
    }

    public InstructionList optimize(InstructionList list) {
        boolean change = false;

        do {
            change = false;

            FlowGraph flow = fg.build(list);
            DominatingSet dom = new DominatingSet(flow);

            for (BasicBlock head: flow.nodes()) {
                for (BasicBlock tail: dom.get(head)) {
                    if (!tail.isInsEmpty() && tail.getLast().isRedirectable()
                            && flow.isEdge(tail, head)) {

                        InstructionList result = processLoop(flow, head, tail, dom);
                        if (result != null) {
                            change = true;
                            list = result;
                            break;
                        }
                    }
                }
                if (change)
                    break;
            }
        } while (change);

        return list;
    }
}
