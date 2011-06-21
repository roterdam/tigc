package mips32;

import flow.*;
import java.util.*;
import intermediate.Label;
import intermediate.IR;
import optimization.BasicBlockOptimizer;
import optimization.LoopInvariantCodeMotion;
import intermediate.Temp;
import arch.Const;
import frame.Frame;
import regalloc.Register;

public class Optimizer {
    Temp zero = null;
    InstructionList optimize(InstructionList list, Temp zero) {
        this.zero = zero;

        // Peephole optimize
        list = peepHoleOptimize(list);

        // Jump zipping
        list = jumpZipping(list);

        // Loop invariant code motion
        //list = loopInvariantCodeMotion(list);

        // Basic Block Optimize
        list = basicBlockOptimize(list);

        // Remove dead code
        list = removeDeadCode(list);
        
        return list;
    }

    InstructionList finalOptimize(InstructionList list, Map<Temp, Register> map, Set<Temp> usedDisplays) {
        InstructionList ret = new InstructionList();
        for (LabeledInstruction i: list) {
            if (i.label != null)
                ret.add(i.label);

            if (i.instruction != null) {
                boolean dead = false;
                if (i.instruction.type == Instruction.Type.MOVE
                        && map.get(i.instruction.dst) == map.get(i.instruction.src1))
                    dead = true;
                else if (i.instruction.display != null
                        && !usedDisplays.contains(i.instruction.display))
                    dead = true;

                if (!dead)
                    ret.add(i.instruction);
            }
        }
        return ret;
    }

    private boolean isPowerOfTwo(int x){
        if (x == 0)
            return false;
        else if (x < 0)
            return isPowerOfTwo(-x);
        else
            return (x & (x - 1)) == 0;
    }

    private int log2(int x) {
        if (x <= 0)
            return -1;
        for (int c = 0; c < 32; ++c) {
            if (x == (1 << c))
                return c;
        }
        return -1;
    }

    private InstructionList peepHoleOptimize(InstructionList list) {
        LabeledInstruction li1 = list.head, li2 = null;
        Map<Label, LabeledInstruction> labelMap = buildLabelMap(list);
        while (li1 != null) {
            while (li1 != null && li1.instruction == null)
                li1 = li1.next;
            if (li1 == null)
                break;

            li2 = li1.next;
            while (li2 != null && li2.instruction == null)
                li2 = li2.next;
            if (li2 == null)
                break;

            Instruction i1 = li1.instruction, i2 = li2.instruction;

            /*
            if (i1.type == Instruction.Type.LI) {
                LabeledInstruction next = nextInstruction(labelMap, li1);
                if (next != null && next.instruction.type == Instruction.Type.MOVE
                        && next.instruction.src1 == i1.dst) {
                    next.instruction.type = Instruction.Type.LI;
                    next.instruction.src1 = null;
                    next.instruction.imm = i1.imm;
                }
            }
            */

            if (i1.type == Instruction.Type.LI && i2.type == Instruction.Type.MUL
                    && i1.imm.value() > 0 && isPowerOfTwo(i1.imm.value())) {
                if (i1.dst == i2.src1) {
                    i2.type = Instruction.Type.SLL;
                    i2.src1 = i2.src2;
                    i2.src2 = null;
                    i2.imm = new Const(log2(i1.imm.value()));
                } else if (i1.dst == i2.src2) {
                    i2.type = Instruction.Type.SLL;
                    i2.src2 = null;
                    i2.imm = new Const(log2(i1.imm.value()));
                }
            } else if (i1.type == Instruction.Type.SLT && i2.type == Instruction.Type.BEQ
                    && i1.dst == i2.src1 && i2.src2 == zero) {
                i2.type = Instruction.Type.BGE;
                i2.src1 = i1.src1;
                i2.src2 = i1.src2;
            } else if (i1.type == Instruction.Type.SLE && i2.type == Instruction.Type.BEQ
                    && i1.dst == i2.src1 && i2.src2 == zero) {
                i2.type = Instruction.Type.BGT;
                i2.src1 = i1.src1;
                i2.src2 = i1.src2;
            } else if (i1.type == Instruction.Type.SEQ && i2.type == Instruction.Type.BEQ
                    && i1.dst == i2.src1 && i2.src2 == zero) {
                i2.type = Instruction.Type.BNE;
                i2.src1 = i1.src1;
                i2.src2 = i1.src2;
            } else if (i1.type == Instruction.Type.SNE && i2.type == Instruction.Type.BEQ
                    && i1.dst == i2.src1 && i2.src2 == zero) {
                i2.type = Instruction.Type.BEQ;
                i2.src1 = i1.src1;
                i2.src2 = i1.src2;
            } else if (i1.type == Instruction.Type.SGT && i2.type == Instruction.Type.BEQ
                    && i1.dst == i2.src1 && i2.src2 == zero) {
                i2.type = Instruction.Type.BLE;
                i2.src1 = i1.src1;
                i2.src2 = i1.src2;
            } else if (i1.type == Instruction.Type.SGE && i2.type == Instruction.Type.BEQ
                    && i1.dst == i2.src1 && i2.src2 == zero) {
                i2.type = Instruction.Type.BLT;
                i2.src1 = i1.src1;
                i2.src2 = i1.src2;
            } else if (i1.type == Instruction.Type.SLT && i2.type == Instruction.Type.BNE
                    && i1.dst == i2.src1 && i2.src2 == zero) {
                i2.type = Instruction.Type.BLT;
                i2.src1 = i1.src1;
                i2.src2 = i1.src2;
            } else if (i1.type == Instruction.Type.SLE && i2.type == Instruction.Type.BNE
                    && i1.dst == i2.src1 && i2.src2 == zero) {
                i2.type = Instruction.Type.BLE;
                i2.src1 = i1.src1;
                i2.src2 = i1.src2;
            } else if (i1.type == Instruction.Type.SEQ && i2.type == Instruction.Type.BNE
                    && i1.dst == i2.src1 && i2.src2 == zero) {
                i2.type = Instruction.Type.BEQ;
                i2.src1 = i1.src1;
                i2.src2 = i1.src2;
            } else if (i1.type == Instruction.Type.SNE && i2.type == Instruction.Type.BNE
                    && i1.dst == i2.src1 && i2.src2 == zero) {
                i2.type = Instruction.Type.BNE;
                i2.src1 = i1.src1;
                i2.src2 = i1.src2;
            } else if (i1.type == Instruction.Type.SGT && i2.type == Instruction.Type.BNE
                    && i1.dst == i2.src1 && i2.src2 == zero) {
                i2.type = Instruction.Type.BGT;
                i2.src1 = i1.src1;
                i2.src2 = i1.src2;
            } else if (i1.type == Instruction.Type.SGE && i2.type == Instruction.Type.BNE
                    && i1.dst == i2.src1 && i2.src2 == zero) {
                i2.type = Instruction.Type.BGE;
                i2.src1 = i1.src1;
                i2.src2 = i1.src2;
            }

            li1 = li2;
        }
        return list;
    }

    private LabeledInstruction nextInstruction(Map<Label, LabeledInstruction> labelMap, LabeledInstruction i) {
        Set<LabeledInstruction> visited = new HashSet<LabeledInstruction>();
        return nextInstructionWorker(labelMap, i, visited);
    }

    private LabeledInstruction nextInstructionWorker(Map<Label, LabeledInstruction> labelMap,
            LabeledInstruction i, Set<LabeledInstruction> visited) {
        if (visited.contains(i))
            return i;
        visited.add(i);

        if (i.instruction != null) {
            if (i.instruction.type != Instruction.Type.J)
                return i;
            else
                return nextInstruction(labelMap, labelMap.get(i.instruction.target));
        } else {
            while (i != null) {
                if (i.instruction != null)
                    return nextInstruction(labelMap, i);
                else
                    i = i.next;
            }
            return null;
        }
    }

    private Label findZip(Map<Label, LabeledInstruction> labelMap, Label target) {
        Set<Label> visited = new HashSet<Label>();
        return findZipWorker(labelMap, target, visited);
    }

    private Label findBranchZip(Map<Label, LabeledInstruction> labelMap, LabeledInstruction li) {
        if (li.instruction == null)
            return null;

        LabeledInstruction q = nextInstruction(labelMap, li.next);
        if (q == null || !q.instruction.isBranch())
            return null;

        Label branchLabel = null;
        LabeledInstruction t = q.next;
        while (t != null) {
            if (t.label != null) {
                branchLabel = t.label;
                break;
            } else if (t.instruction != null)
                break;
            else
                t = t.next;
        }

        if (branchLabel == null)
            return null;

        Instruction qi = q.instruction, pi = li.instruction;
        if (qi.type == Instruction.Type.BEQ && ((qi.src1 == pi.dst
                        && qi.src2 == zero) || (qi.src1 == zero && qi.src2 == pi.dst)))
            return (pi.imm.value() == 0 ? qi.target : branchLabel);
        else if (qi.type == Instruction.Type.BNE && ((qi.src1 == pi.dst
                        && qi.src2 == zero) || (qi.src1 == zero && qi.src2 == pi.dst)))
            return (pi.imm.value() != 0 ? qi.target : branchLabel);
        else if ((qi.type == Instruction.Type.BLT && qi.src1 == pi.dst && qi.src2 == zero) ||
                (qi.type == Instruction.Type.BGT && qi.src1 == zero && qi.src2 == pi.dst))
            return (pi.imm.value() < 0 ? qi.target : branchLabel);
        else if ((qi.type == Instruction.Type.BLE && qi.src1 == pi.dst && qi.src2 == zero) ||
                (qi.type == Instruction.Type.BGE && qi.src1 == zero && qi.src2 == pi.dst))
            return (pi.imm.value() <= 0 ? qi.target : branchLabel);
        else if ((qi.type == Instruction.Type.BGT && qi.src1 == pi.dst && qi.src2 == zero) ||
                (qi.type == Instruction.Type.BLT && qi.src1 == zero && qi.src2 == pi.dst))
            return (pi.imm.value() > 0 ? qi.target : branchLabel);
        else if ((qi.type == Instruction.Type.BGE && qi.src1 == pi.dst && qi.src2 == zero) ||
                (qi.type == Instruction.Type.BLE && qi.src1 == zero && qi.src2 == pi.dst))
            return (pi.imm.value() >= 0 ? qi.target : branchLabel);
        else
            return null;
    }

    private Label findZipWorker(Map<Label, LabeledInstruction> labelMap, Label target, Set<Label> visited) {
        if (visited.contains(target))
            return target;
        visited.add(target);

        LabeledInstruction p = labelMap.get(target);
        while (p != null) {
            if (p.label != null)
                target = p.label;
            if (p.instruction == null)
                p = p.next;
            else if (p.instruction.type == Instruction.Type.J)
                return findZipWorker(labelMap, p.instruction.target, visited);
            else if (p.instruction.type == Instruction.Type.LI
                    && p.instruction.imm.isBinded()) {
                
                Label newTarget = findBranchZip(labelMap, p);
                if (newTarget == null)
                    break;

                return findZipWorker(labelMap, newTarget, visited);
            } else
                break;
        }
        return target;
    }

    private InstructionList addBranchLabels(InstructionList list) {
        LabeledInstruction i = list.head;
        InstructionList ret = new InstructionList();
        while (i != null) {
            ret.add(i);
            if (i.instruction != null && i.instruction.isBranch()) {
                LabeledInstruction p = i.next;
                boolean nolabel = false;
                while (p != null) {
                    if (p.instruction != null) {
                        if (p.label == null)
                            nolabel = true;
                        break;
                    } else if (p.label != null)
                        break;

                    p = p.next;
                }
                if (nolabel)
                    ret.add(Label.newLabel());
            }

            i = i.next;
        }
        return ret;
    }

    Map<Label, LabeledInstruction> buildLabelMap(InstructionList list) {
        Map<Label, LabeledInstruction> labelMap = new HashMap<Label, LabeledInstruction>();
        for (LabeledInstruction li: list) {
            if (li.label != null)
                labelMap.put(li.label, li);
        }
        return labelMap;
    }

    private InstructionList jumpZipping(InstructionList list) {
        // Add branch labels
        list = addBranchLabels(list);

        // Build label map
        Map<Label, LabeledInstruction> labelMap = buildLabelMap(list);

        // Jump zipping
        LabeledInstruction li = list.head;
        while (li != null) {
            if (li.instruction != null) {
                if (li.instruction.target != null)
                    li.instruction.target = findZip(labelMap, li.instruction.target);
                else if (li.instruction.type == Instruction.Type.LI) {
                    Label t = findBranchZip(labelMap, li);
                    if (t != null) {
                        LabeledInstruction on = li.next;
                        li.next = new LabeledInstruction(null, Instruction.J(li.instruction.frame, t), on);
                    }
                }
            }
            li = li.next;
        }

        // Rewrite blocks
        FlowGraphGenerator fg = new FlowGraphGenerator();
        FlowGraph flow = fg.build(list);
        list = rewrite(flow);

        // Remove direct jumps
        li = list.head;
        InstructionList nlist = new InstructionList();
        while (li != null) {
            if (li.label != null)
                nlist.add(li.label);
            
            boolean direct = false;
            if (li.instruction != null && li.instruction.type == Instruction.Type.J) {
                LabeledInstruction p = li.next;
                while (p != null) {
                    if (p.label == li.instruction.target) {
                        direct = true;
                        break;
                    } else if (p.instruction != null)
                        break;
                    p = p.next;
                }
            }

            if (!direct && li.instruction != null)
                nlist.add(li.instruction);

            li = li.next;
        }
        list = nlist;

        // Remove useless labels
        Set<Label> usedLabels = new HashSet<Label>();
        Set<Frame> usedFrames = new HashSet<Frame>();
        for (LabeledInstruction lii: list) {
            if (lii.instruction != null) {
                if (lii.instruction.target != null)
                    usedLabels.add(lii.instruction.target);
                usedFrames.add(lii.instruction.frame);
            }
        }
        for (Frame f: usedFrames)
            usedLabels.addAll(f.returns);
        nlist = new InstructionList();
        for (LabeledInstruction lii: list) {
            if (lii.label != null && usedLabels.contains(lii.label))
                nlist.add(lii.label);
            if (lii.instruction != null)
                nlist.add(lii.instruction);
        }
        list = nlist;

        return list;
    }

    private InstructionList basicBlockOptimize(InstructionList list) {
        FlowGraphGenerator fg = new FlowGraphGenerator();
        FlowGraph flow = fg.build(list);
        flow.removeUnreachableNodes();
        LifeAnalysis life = new LifeAnalysis(flow);
        InstructionGenerator gen = new InstructionGenerator();
        for (BasicBlock b: flow.nodes()) {
/*            String s = "";
            for (Temp t: life.out(b))
                s += t.toString() + " ";
            System.out.println("Live variables: " + s);

            System.out.println("");
            System.out.println("Before optimization:");
            for (arch.Instruction i: b)
                System.out.println(i.toString());*/

            BasicBlockOptimizer bbo = new BasicBlockOptimizer(b, life, gen);
            bbo.optimize();

/*            System.out.println("");
            System.out.println("After optimization:");
            for (arch.Instruction i: b)
                System.out.println(i.toString());

            System.out.println("");

            Scanner input = new Scanner(System.in);
            s = input.next();*/
        }
        return rewrite(flow);
    }

    private InstructionList loopInvariantCodeMotion(InstructionList list) {
        LoopInvariantCodeMotion opt = new LoopInvariantCodeMotion(new FlowGraphGenerator(), new InstructionRewriter());
        return (InstructionList) opt.optimize(list);
    }

    private InstructionList removeDeadCode(InstructionList list) {
        FlowGraph flow = null;
        LifeAnalysis life = null;
        InstructionList nlist = null;
        boolean change = false;
        do {
            change = false;
            FlowGraphGenerator fg = new FlowGraphGenerator();
            flow = fg.build(list);
            life = new LifeAnalysis(flow);
            nlist = new InstructionList();
            for (LabeledInstruction i: list) {
                if (i.label != null)
                    nlist.add(i.label);
                if (i.instruction != null) {
                    boolean dead = true;
                    for (Temp t: i.instruction.def())
                        if (life.out(i.instruction).contains(t)) {
                            dead = false;
                            break;
                        }
                    if (!dead || i.instruction.isJump()
                            || i.instruction.isStore() || i.instruction.hasSideEffects())
                        nlist.add(i.instruction);
                    else
                        change = true;
                }
            }
            list = nlist;
        } while (change);
        return list;
    }

    private InstructionList rewrite(FlowGraph flow) {
        InstructionRewriter writer = new InstructionRewriter();
        return writer.rewrite(flow);
    }
}

