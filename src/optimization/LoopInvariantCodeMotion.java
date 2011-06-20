package optimization;

import arch.Instruction;
import intermediate.Temp;
import flow.FlowGraph;
import flow.DominatingSet;
import flow.BasicBlock;
import java.util.*;

public class LoopInvariantCodeMotion {
    FlowGraph flow;

    public LoopInvariantCodeMotion(FlowGraph flow) {
        this.flow = flow;
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

    private void processLoop(BasicBlock head, DominatingSet dom) {
        Map<Temp, List<Instruction>> definitions = new HashMap<Temp, List<Instruction>>();
        Map<Instruction, BasicBlock> instructionFrom = new HashMap<Instruction, BasicBlock>();
        Map<Temp, Set<Instruction>> useList = new HashMap<Temp, Set<Instruction>>();
        Set<Temp> invariants = new HashSet<Temp>();
        Set<Instruction> invariantIns = new HashSet<Instruction>();
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

        for (BasicBlock b: dom.get(head)) {
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

                    for (Temp t: i.def())
                        invariants.add(t);
                    invariantIns.add(i);
                    System.out.println(i.toString());
                }
            }
        }

    }

    public FlowGraph optimize() {
        DominatingSet dom = new DominatingSet(flow);

        for (BasicBlock head: flow.nodes()) {
            for (BasicBlock tail: dom.get(head)) {
                if (flow.isEdge(tail, head)) {
                    processLoop(head, dom);
                }
            }
        }

        return flow;
    }
}
