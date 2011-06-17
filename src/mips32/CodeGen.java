package mips32;

import intermediate.*;
import util.*;
import java.util.*;
import frame.Frame;
import notifier.Notifier;
import regalloc.*;
import arch.Const;
import symbol.Symbol;
import flow.*;
import java.io.BufferedWriter;

public class CodeGen {
    static class MipsMemStyle {
        Temp base;
        Const offset;

        MipsMemStyle(Temp base, Const offset) {
            this.base = base;
            this.offset = offset;
        }
    }

    Notifier notifier;

    HashMap<Label, ThreeAddressCode> labelMap;
    IR ir;
    Optimizer opt;
    Temp zero, gp, fp, sp, ra, v0, a0, a1;
    int wordLength = 4;

    static class SavePlace {
        LabeledInstruction save, restore;
        LabeledInstruction ret;
        Frame frame;

        public SavePlace(Frame frame, LabeledInstruction save, LabeledInstruction restore, LabeledInstruction ret) {
            this.save = save;
            this.restore = restore;
            this.frame = frame;
            this.ret = ret;
        }
    }

    ArrayList<SavePlace> callSaves;

    public CodeGen(Notifier notifier, IR ir, Optimizer opt) {
        this.notifier = notifier;
        this.ir = ir;
        this.opt = opt;
        ir.wordLength.bind(wordLength);
        labelMap = new HashMap<Label, ThreeAddressCode>();
        
        HashSet<Label> labels = new HashSet<Label>();
        for (IntermediateCode ic: ir.codes) {
            if (ic.label != null)
                labels.add(ic.label);
            if (ic.tac != null) {
                for (Label l: labels)
                    labelMap.put(l, ic.tac);
                labels.clear();
            }
        }
        zero = ir.globalFrame.addLocal();
        fp = ir.globalFrame.addLocal();
        sp = ir.globalFrame.addLocal();
        gp = ir.globalFrame.addLocal();
        ra = ir.globalFrame.addLocal();
        v0 = ir.globalFrame.addLocal();
        a0 = ir.globalFrame.addLocal();
        a1 = ir.globalFrame.addLocal();
        callSaves = new ArrayList<SavePlace>();
    }

    Symbol sym(String s) {
        return Symbol.symbol(s);
    }

    Const processConstAccess(ConstAccess ca) {
        if (ca instanceof UnknownConstAccess) {
            if (ca == ir.wordLength)
                return new Const(wordLength);
            else
                return new Const(((UnknownConstAccess) ca).name);
        } else
            return new Const(ca.value);
    }

    Const processNegConstAccess(ConstAccess ca) {
        if (ca instanceof UnknownConstAccess) {
            if (ca == ir.wordLength)
                return new Const(-wordLength);
            else
                throw new Error("Unexpected UnknownConstAccess");
        } else
            return new Const(-ca.value);
    }

    MipsMemStyle processMemAccess(InstructionList list, Frame frame, MemAccess ma) {
        if (ma.base instanceof Temp && ma.offset instanceof Temp) {
            Temp t = frame.addLocal();
            list.add(Instruction.ADD(frame, t, (Temp) ma.base, (Temp) ma.offset));
            return new MipsMemStyle(t, new Const(0));
        } else if (ma.base instanceof Temp && ma.offset instanceof ConstAccess) {
            return new MipsMemStyle((Temp) ma.base, processConstAccess((ConstAccess) ma.offset));
        } else if (ma.base instanceof ConstAccess && ma.offset instanceof Temp) {
            return new MipsMemStyle((Temp) ma.offset, processConstAccess((ConstAccess) ma.base));
        } else {
            if (ma.base instanceof UnknownConstAccess || ma.offset instanceof UnknownConstAccess)
                throw new Error("Unexpected UnknownConstAccess");
            return new MipsMemStyle(zero, new Const(((ConstAccess) ma.base).value + ((ConstAccess) ma.offset).value));
        }
    }

    void fillCallSaves(InstructionList list, LifeAnalysis life) {
        for (SavePlace place: callSaves) {
            LabeledInstruction p = place.ret;
            Instruction i = null;
            while (p != null) {
                if (p.instruction != null) {
                    i = p.instruction;
                    break;
                }
                p = p.next;
            }
            if (i != null) {
                Set<Temp> s = new HashSet<Temp>(life.in(i));
                ArrayList<Temp> saves = new ArrayList<Temp>();
                for (Temp t: place.frame.params)
                    if (s.contains(t))
                        saves.add(t);
                for (Temp t: place.frame.locals)
                    if (s.contains(t))
                        saves.add(t);
                if (place.frame.returnValue != null && s.contains(place.frame.returnValue))
                    saves.add(place.frame.returnValue);

                LabeledInstruction o = place.save.next;
                LabeledInstruction tail = place.save;
                int len = saves.size() * wordLength, off = 0;

                tail.next = new LabeledInstruction(null,
                        Instruction.ADDI(place.frame, sp, sp, new Const(-len)),
                        null);
                tail = tail.next;
                off = len - wordLength;
                for (Temp t: saves) {
                    tail.next = new LabeledInstruction(null,
                            Instruction.SW(place.frame, t, sp, new Const(off)),
                            null);
                    tail = tail.next;
                    off -= wordLength;
                }
                tail.next = o;

                o = place.restore.next;
                tail = place.restore;
                off = len - wordLength;
                for (Temp t: saves) {
                    tail.next = new LabeledInstruction(null,
                            Instruction.LW(place.frame, t, sp, new Const(off)),
                            null);
                    tail = tail.next;
                    off -= wordLength;
                }
                tail.next = new LabeledInstruction(null,
                        Instruction.ADDI(place.frame, sp, sp, new Const(len)),
                        null);
                tail = tail.next;
                tail.next = o;
            }
        }
    }

    public boolean generate(BufferedWriter writer) throws java.io.IOException {
        InstructionList list = new InstructionList();
        for (IntermediateCode ic: ir.codes) {
            if (ic.label != null)
                list.add(ic.label);
            if (ic.tac != null) {
                generate(list, ic.tac);
            }
        }

        FlowGraph graph = Util.buildFlowGraph(list);
        LifeAnalysis life = new LifeAnalysis(graph);
        fillCallSaves(list, life);

        InstructionList nlist = new InstructionList();
        for (LabeledInstruction ins: list) {
            if (ins.label != null || ins.instruction != null)
                nlist.add(ins.label, ins.instruction);
        }
        list = nlist;


        list = opt.optimize(list, zero);

        ArrayList<Register> registers = new ArrayList<Register>();
        registers.add(new Register("$v0"));
        registers.add(new Register("$v1"));
        registers.add(new Register("$a0"));
        registers.add(new Register("$a1"));
        registers.add(new Register("$a2"));
        registers.add(new Register("$a3"));
        registers.add(new Register("$t0"));
        registers.add(new Register("$t1"));
        registers.add(new Register("$t2"));
        registers.add(new Register("$t3"));
        registers.add(new Register("$t4"));
        registers.add(new Register("$t5"));
        registers.add(new Register("$t6"));
        registers.add(new Register("$t7"));
        registers.add(new Register("$s0"));
        registers.add(new Register("$s1"));
        registers.add(new Register("$s2"));
        registers.add(new Register("$s3"));
        registers.add(new Register("$s4"));
        registers.add(new Register("$s5"));
        registers.add(new Register("$s6"));
        registers.add(new Register("$s7"));
        registers.add(new Register("$t8"));
        registers.add(new Register("$t9"));
        registers.add(new Register("$fp"));
        registers.add(new Register("$sp"));
        registers.add(new Register("$ra"));
        Map<Temp, Register> preColor = new HashMap<Temp, Register>();
        preColor.put(v0, new Register("$v0"));
        preColor.put(a0, new Register("$a0"));
        preColor.put(a1, new Register("$a1"));
        preColor.put(fp, new Register("$fp"));
        preColor.put(sp, new Register("$sp"));
        preColor.put(ra, new Register("$ra"));

        Set<Temp> candidates = new HashSet<Temp>();
        for (LabeledInstruction i: list) {
            if (i.instruction != null) {
                if (i.instruction.dst != null)
                    candidates.add(i.instruction.dst);
                if (i.instruction.src1 != null)
                    candidates.add(i.instruction.src1);
                if (i.instruction.src2 != null)
                    candidates.add(i.instruction.src2);
            }
        }
        candidates.removeAll(preColor.keySet());
        candidates.remove(zero);
        candidates.remove(gp);

        Map<Temp, Register> map = null;

        while (true) {        
            graph = Util.buildFlowGraph(list);
            life = new LifeAnalysis(graph);
            Graph<Temp> ig = buildInterferenceGraph(list, life);
            RegAlloc regAlloc = new RegAlloc(ig, registers, new HashMap<Temp, Register>(preColor), candidates);
            if (!regAlloc.color()) {
                notifier.error("Not enough registers");
                return false;
            }

            map = regAlloc.getMap();
            map.put(zero, new Register("$zero"));
            map.put(gp, new Register("$gp"));
            Set<Temp> spills = regAlloc.getSpill();

            if (spills.size() == 0)
                break;

            nlist = new InstructionList();
            for (LabeledInstruction li : list) {
                if (li.label != null)
                    nlist.add(li.label);
                if (li.instruction != null) {
                    if (li.instruction.src1 != null && spills.contains(li.instruction.src1)) {
                        li.instruction.src1.spill(wordLength);
                        Temp t = generateLoadSpill(nlist, li.instruction, li.instruction.src1);
                        candidates.remove(li.instruction.src1);
                        li.instruction.src1 = t;
                    }
                    if (li.instruction.src2 != null && spills.contains(li.instruction.src2)) {
                        li.instruction.src2.spill(wordLength);
                        Temp t = generateLoadSpill(nlist, li.instruction, li.instruction.src2);
                        candidates.remove(li.instruction.src2);
                        li.instruction.src2 = t;
                    }
                    if (li.instruction.dst != null && spills.contains(li.instruction.dst)) {
                        Temp old = li.instruction.dst;
                        old.spill(wordLength);
                        Temp t = li.instruction.frame.addLocal();
                        li.instruction.dst = t;
                        nlist.add(li.instruction);
                        generateStoreSpill(nlist, li.instruction, old, t);
                        candidates.remove(old);
                    } else
                        nlist.add(li.instruction);
                }
            }
            ir.globalFrame.updateFrameSize(wordLength);
            for (Frame f: ir.funcFrames)
                f.updateFrameSize(wordLength);
            list = nlist;
        }
        
        list = opt.finalOptimize(list, map);

        SpimAsm asm = new SpimAsm(list, map, ir);
        asm.output(writer);

        return true;
    }

    Temp generateLoadSpill(InstructionList list, Instruction ins, Temp src) {
        int offset = src.spill(wordLength);
        Temp t = ins.frame.addLocal();
        if (ins.special == 2) {
            list.add(Instruction.LW(ins.frame, t, sp, new Const(offset)));
        } else if (src.frame == ir.globalFrame) {
            list.add(Instruction.LW(ins.frame, t, gp, new Const(offset - 0x8000)));
        } else if (ins.frame == src.frame) {
            list.add(Instruction.LW(ins.frame, t, fp, new Const(offset)));
        } else {
            Temp display = src.frame.display;
            if (!display.inMem()) {
                list.add(Instruction.LW(ins.frame, t, display, new Const(offset)));
            } else {
                list.add(Instruction.LW(ins.frame, t, gp, new Const(display.spill(wordLength) - 0x8000)));
                list.add(Instruction.LW(ins.frame, t, t, new Const(offset)));
            }
        }
        return t;
    }

    void generateStoreSpill(InstructionList list, Instruction ins, Temp old, Temp value) {
        int offset = old.spill(wordLength);
        if (ins.special == 1) {
            list.add(Instruction.SW(ins.frame, value, sp, new Const(offset)));
        } else if (old.frame == ir.globalFrame) {
            list.add(Instruction.SW(ins.frame, value, gp, new Const(offset - 0x8000)));
        } else if (ins.frame == old.frame) {
            list.add(Instruction.SW(ins.frame, value, fp, new Const(offset)));
        } else {
            Temp display = old.frame.display;
            if (!display.inMem()) {
                list.add(Instruction.SW(ins.frame, value, display, new Const(offset)));
            } else {
                Temp t = ins.frame.addLocal();
                list.add(Instruction.LW(ins.frame, t, gp, new Const(display.spill(wordLength) - 0x8000)));
                list.add(Instruction.SW(ins.frame, value, t, new Const(offset)));
            }
        }
    }

    boolean isStringConstant(Access access) {
        if (access instanceof UnknownConstAccess && access != ir.wordLength)
            return true;
        else
            return false;
    }

    boolean isZeroConstant(ConstAccess access) {
        if (!(access instanceof UnknownConstAccess) && access.value == 0)
            return true;
        else
            return false;
    }

    Temp toTemp(InstructionList list, Frame frame, Access access) {
        if (access instanceof Temp)
            return (Temp) access;
        else if (access instanceof ConstAccess && isZeroConstant((ConstAccess) access))
            return zero;
        else {
            Temp t = frame.addLocal();
            generate(list, new MoveTAC(frame, access, t));
            return t;
        }
    }

    public void generate(InstructionList list, ThreeAddressCode tac) {
        if (tac instanceof MoveTAC)
            generate(list, (MoveTAC) tac);
        else if (tac instanceof OpTAC)
            generate(list, (OpTAC) tac);
        else if (tac instanceof CallTAC)
            generate(list, (CallTAC) tac);
        else if (tac instanceof CallExternTAC)
            generate(list, (CallExternTAC) tac);
        else if (tac instanceof ReturnTAC)
            generate(list, (ReturnTAC) tac);
        else if (tac instanceof GotoTAC)
            generate(list, (GotoTAC) tac);
        else /*if (tac instanceof BranchTAC)*/
            generate(list, (BranchTAC) tac);
    }

    public void generate(InstructionList list, MoveTAC tac) {
        if (tac.dst instanceof Temp) {
            if (tac.op1 instanceof Temp) {
                list.add(Instruction.MOVE(tac.frame, (Temp) tac.dst, (Temp) tac.op1));
            } else if (tac.op1 instanceof ConstAccess) {
                if (isStringConstant(tac.op1))
                    list.add(Instruction.LA(tac.frame, (Temp) tac.dst, ((UnknownConstAccess) tac.op1).name));
                else
                    list.add(Instruction.LI(tac.frame, (Temp) tac.dst, processConstAccess((ConstAccess) tac.op1)));
            } else if (tac.op1 instanceof MemAccess) {
                MipsMemStyle m = processMemAccess(list, tac.frame, (MemAccess) tac.op1);
                list.add(Instruction.LW(tac.frame, (Temp) tac.dst, m.base, m.offset));
            }
        } else if (tac.dst instanceof MemAccess) {
            MipsMemStyle m = processMemAccess(list, tac.frame, (MemAccess) tac.dst);
            if (tac.op1 instanceof Temp) {
                list.add(Instruction.SW(tac.frame, (Temp) tac.op1, m.base, m.offset));
            } else if (tac.op1 instanceof ConstAccess) {
                if (isZeroConstant((ConstAccess) tac.op1))
                    list.add(Instruction.SW(tac.frame, zero, m.base, m.offset));
                else {
                    Temp t = tac.frame.addLocal();
                    generate(list, new MoveTAC(tac.frame, tac.op1, t));
                    list.add(Instruction.SW(tac.frame, t, m.base, m.offset));
                }
            } else if (tac.op1 instanceof MemAccess) {
                Temp t = tac.frame.addLocal();
                generate(list, new MoveTAC(tac.frame, tac.op1, t));
                list.add(Instruction.SW(tac.frame, t, m.base, m.offset));
            }
        }
    }

    public void generate(InstructionList list, OpTAC tac) {
        if (tac instanceof BinOpTAC) {
            if (tac.dst instanceof Temp) {
                boolean imm = false;
                if (((BinOpTAC) tac).op == BinOpTAC.BinOp.ADD
                        || ((BinOpTAC) tac).op == BinOpTAC.BinOp.LT
                        || ((BinOpTAC) tac).op == BinOpTAC.BinOp.SUB) {
                    if (tac.op1 instanceof Temp && tac.op2 instanceof ConstAccess) {
                        imm = true;
                        if (((BinOpTAC) tac).op == BinOpTAC.BinOp.ADD)
                            list.add(Instruction.ADDI(tac.frame, (Temp) tac.dst,
                                        (Temp) tac.op1, processConstAccess((ConstAccess) tac.op2)));
                        else if (((BinOpTAC) tac).op == BinOpTAC.BinOp.LT)
                            list.add(Instruction.SLTI(tac.frame, (Temp) tac.dst,
                                        (Temp) tac.op1, processConstAccess((ConstAccess) tac.op2)));
                        else
                            list.add(Instruction.ADDI(tac.frame, (Temp) tac.dst,
                                        (Temp) tac.op1, processNegConstAccess((ConstAccess) tac.op2)));
                    } else if (((BinOpTAC) tac).op == BinOpTAC.BinOp.ADD
                                && tac.op1 instanceof ConstAccess && tac.op2 instanceof Temp) {
                        imm = true;
                        generate(list, new BinOpTAC(tac.frame, BinOpTAC.BinOp.ADD,
                                    tac.op2, tac.op1, (Temp) tac.dst));
                    }
                }
                if (imm == false) {
                    Temp op1 = toTemp(list, tac.frame, tac.op1), op2 = toTemp(list, tac.frame, tac.op2);
                    switch (((BinOpTAC) tac).op) {
                        case ADD:
                            list.add(Instruction.ADD(tac.frame, (Temp) tac.dst, op1, op2));
                            break;

                        case SUB:
                            list.add(Instruction.SUB(tac.frame, (Temp) tac.dst, op1, op2));
                            break;

                        case MUL:
                            list.add(Instruction.MUL(tac.frame, (Temp) tac.dst, op1, op2));
                            break;

                        case DIV:
                            list.add(Instruction.DIV(tac.frame, (Temp) tac.dst, op1, op2));
                            break;

                        case EQ:
                            list.add(Instruction.SEQ(tac.frame, (Temp) tac.dst, op1, op2));
                            break;

                        case NEQ:
                            list.add(Instruction.SNE(tac.frame, (Temp) tac.dst, op1, op2));
                            break;

                        case GT:
                            list.add(Instruction.SGT(tac.frame, (Temp) tac.dst, op1, op2));
                            break;

                        case GEQ:
                            list.add(Instruction.SGE(tac.frame, (Temp) tac.dst, op1, op2));
                            break;

                        case LT:
                            list.add(Instruction.SLT(tac.frame, (Temp) tac.dst, op1, op2));
                            break;

                        case LEQ:
                            list.add(Instruction.SLE(tac.frame, (Temp) tac.dst, op1, op2));
                            break;
                    }
                }
            } else if (tac.dst instanceof MemAccess) {
                Temp t = tac.frame.addLocal();
                generate(list, new BinOpTAC(tac.frame, ((BinOpTAC) tac).op, tac.op1, tac.op2, t));
                MipsMemStyle m = processMemAccess(list, tac.frame, (MemAccess) tac.dst);
                list.add(Instruction.SW(tac.frame, t, m.base, m.offset));
            }
        } else if (tac instanceof UniOpTAC) {
            if (((UniOpTAC) tac).op == UniOpTAC.UniOp.NEG) {
                if (tac.dst instanceof Temp) {
                    if (tac.op1 instanceof Temp) {
                        list.add(Instruction.NEG(tac.frame, (Temp) tac.dst, (Temp) tac.op1));
                    } else if (tac.op1 instanceof ConstAccess) {
                        if (isZeroConstant((ConstAccess) tac.op1))
                            list.add(Instruction.NEG(tac.frame, (Temp) tac.dst, zero));
                        else {
                            list.add(Instruction.LI(tac.frame, (Temp) tac.dst, new Const(-((ConstAccess) tac.op1).value)));
                        }
                    } else if (tac.op1 instanceof MemAccess) {
                        MipsMemStyle m = processMemAccess(list, tac.frame, (MemAccess) tac.op1);
                        Temp t = tac.frame.addLocal();
                        list.add(Instruction.LW(tac.frame, t, m.base, m.offset));
                        list.add(Instruction.NEG(tac.frame, (Temp) tac.dst, t));
                    }
                } else if (tac.dst instanceof MemAccess) {
                    Temp t = tac.frame.addLocal();
                    generate(list, new UniOpTAC(tac.frame, UniOpTAC.UniOp.NEG, tac.op1, t));
                    MipsMemStyle m = processMemAccess(list, tac.frame, (MemAccess) tac.dst);
                    list.add(Instruction.SW(tac.frame, t, m.base, m.offset));
                }
            }
        }
    }

    Instruction addSideEffect(Instruction ins) {
        ins.sideEffects = true;
        return ins;
    }

    void addSpecialInstruction(InstructionList list, Frame frame, Temp dst, Access src, int special) {
        if (src instanceof Temp) {
            Instruction ins = Instruction.MOVE(frame, dst, (Temp) src);
            ins.special = special;
            list.add(ins);
        } else if (src instanceof ConstAccess) {
            if (isStringConstant(src)) {
                Instruction ins = Instruction.LA(frame, dst, ((UnknownConstAccess) src).name);
                ins.special = special;
                list.add(ins);
            } else {
                Instruction ins = Instruction.LI(frame, dst, new Const(((ConstAccess) src).value));
                ins.special = special;
                list.add(ins);
            }
        } else if (src instanceof MemAccess) {
            MipsMemStyle m = processMemAccess(list, frame, (MemAccess) src);
            Instruction ins = Instruction.LW(frame, dst, m.base, m.offset);
            ins.special = special;
            list.add(ins);
        }
    }

    public void generate(InstructionList list, CallTAC tac) {
        Frame callee = labelMap.get(tac.place).frame;
        callee.updateFrameSize(wordLength);
        
        Label retLabel = Label.newLabel();
        callee.returns.add(retLabel);

        boolean needsave = false;
        LabeledInstruction save = null, restore = null;
        if (ir.callingGraph.isLoopEdge(tac.frame, callee)) {
            needsave = true;
            save = list.addPlaceHolder();
        }

        list.add(addSideEffect(Instruction.ADDIU(tac.frame, sp, sp, new Const(-wordLength))));

        List<Temp> tParams = new ArrayList<Temp>();
        for (Access t: tac.params) {
            Temp v = tac.frame.addLocal();
            generate(list, new MoveTAC(tac.frame, t, v));
            tParams.add(v);
        }
        Iterator<Temp> iter = tParams.iterator();
        for (Temp t: callee.params)
            addSpecialInstruction(list, tac.frame, t, iter.next(), 1);

        list.add(Instruction.SW(tac.frame, fp, sp, new Const(0)));
        list.add(Instruction.SW(tac.frame, ra, sp, new Const(-wordLength)));
        list.add(Instruction.SW(tac.frame, callee.display, sp, new Const(-2 * wordLength)));
        list.add(Instruction.MOVE(tac.frame, callee.display, sp));
        list.add(Instruction.MOVE(tac.frame, fp, sp));
        list.add(addSideEffect(Instruction.ADDIU(callee, sp, sp, callee.minusFrameSize)));

        list.add(Instruction.JAL(callee, tac.place, ra));

        LabeledInstruction retPlace = list.add(retLabel);
        list.add(addSideEffect(Instruction.ADDIU(callee, sp, sp, callee.frameSize)));
        list.add(addSideEffect(Instruction.LW(callee, fp, sp, new Const(0))));
        list.add(Instruction.LW(tac.frame, callee.display, sp, new Const(-2 * wordLength)));
        list.add(Instruction.LW(tac.frame, ra, sp, new Const(-wordLength)));

        if (callee.returnValue != null)
            addSpecialInstruction(list, tac.frame, tac.actualReturn, callee.returnValue, 2);

        list.add(Instruction.ADDIU(tac.frame, sp, sp, new Const(wordLength)));

        if (needsave) {
            restore = list.addPlaceHolder();
            callSaves.add(new SavePlace(tac.frame, save, restore, retPlace));
        }
    }

    public void generate(InstructionList list, CallExternTAC tac) {
        if (tac.place == sym("print")) {

            generate(list, new MoveTAC(tac.frame, tac.param1, a0));
            list.add(Instruction.LI(tac.frame, v0, new Const(4)));
            list.add(Instruction.SYSCALL(tac.frame, v0, a0, a1, 4));

        } else if (tac.place == sym("printi")) {

            generate(list, new MoveTAC(tac.frame, tac.param1, a0));
            list.add(Instruction.LI(tac.frame, v0, new Const(1)));
            list.add(Instruction.SYSCALL(tac.frame, v0, a0, a1, 1));

        } else if (tac.place == sym("flush")) {

        } else if (tac.place == sym("getchar")) {

            generate(list, new CallExternTAC(tac.frame, sym("malloc"), new ConstAccess(2),
                    null, null, a0));
            list.add(Instruction.LI(tac.frame, a1, new Const(2)));
            list.add(Instruction.LI(tac.frame, v0, new Const(8)));
            list.add(Instruction.SYSCALL(tac.frame, v0, a0, a1, 8));
            generate(list, new MoveTAC(tac.frame, a0, (AssignableAccess) tac.dst));

        } else if (tac.place == sym("ord")) {

            Label j = Label.newLabel(), exit = Label.newLabel();
            Temp t1 = toTemp(list, tac.frame, tac.param1), t2 = tac.frame.addLocal();
            list.add(Instruction.LB(tac.frame, t2, t1, new Const(0)));
            list.add(Instruction.BEQ(tac.frame, t2, zero, j));
            generate(list, new MoveTAC(tac.frame, t2, (AssignableAccess) tac.dst));
            list.add(Instruction.J(tac.frame, exit));
            list.add(j);
            generate(list, new MoveTAC(tac.frame, new ConstAccess(-1), (AssignableAccess) tac.dst));
            list.add(exit);

        } else if (tac.place == sym("chr")) {

            Label exit = Label.newLabel(), end = Label.newLabel();
            Temp t1 = tac.frame.addLocal(), t2 = toTemp(list, tac.frame, tac.param1), t256 = tac.frame.addLocal();
            list.add(Instruction.LI(tac.frame, t256, new Const(256)));
            list.add(Instruction.BGE(tac.frame, t2, t256, exit));
            list.add(Instruction.BLT(tac.frame, t2, zero, exit));
            generate(list, new CallExternTAC(tac.frame, sym("malloc"), new ConstAccess(2),
                        null, null, t1));
            list.add(Instruction.SB(tac.frame, t2, t1, new Const(0)));
            list.add(Instruction.SB(tac.frame, zero, t1, new Const(1)));
            generate(list, new MoveTAC(tac.frame, t1, (AssignableAccess) tac.dst));
            list.add(Instruction.J(tac.frame, end));
            list.add(exit);
            generate(list, new CallExternTAC(tac.frame, sym("exit"), new ConstAccess(0), null, null, null));
            list.add(end);

        } else if (tac.place == sym("size")) {

            Temp res = tac.frame.addLocal(),
                 chr = tac.frame.addLocal(),
                 p = tac.frame.addLocal();
            Label start = Label.newLabel();
            generate(list, new MoveTAC(tac.frame, tac.param1, p));
            list.add(Instruction.MOVE(tac.frame, res, zero));
            list.add(start);
            list.add(Instruction.LB(tac.frame, chr, p, new Const(0)));
            list.add(Instruction.ADDIU(tac.frame, p, p, new Const(1)));
            list.add(Instruction.ADDI(tac.frame, res, res, new Const(1)));
            list.add(Instruction.BNE(tac.frame, chr, zero, start));
            list.add(Instruction.ADDI(tac.frame, res, res, new Const(-1)));
            generate(list, new MoveTAC(tac.frame, res, (AssignableAccess) tac.dst));

        } else if (tac.place == sym("substring")) {
            
            Temp res = tac.frame.addLocal(),
                 p = tac.frame.addLocal(),
                 q = tac.frame.addLocal(),
                 size = tac.frame.addLocal(),
                 size1 = tac.frame.addLocal(),
                 t = tac.frame.addLocal();
            Label begin = Label.newLabel(), exit = Label.newLabel();

            generate(list, new MoveTAC(tac.frame, tac.param3, size));
            list.add(Instruction.ADDI(tac.frame, size1, size, new Const(1)));
            generate(list, new CallExternTAC(tac.frame, sym("malloc"), size1, null, null, res));
            generate(list, new BinOpTAC(tac.frame, BinOpTAC.BinOp.ADD, tac.param1, tac.param2, p));
            list.add(Instruction.MOVE(tac.frame, q, res));

            list.add(Instruction.BEQ(tac.frame, size, zero, exit));

            list.add(begin);
            list.add(Instruction.LB(tac.frame, t, p, new Const(0)));
            list.add(Instruction.BEQ(tac.frame, t, zero, exit));
            list.add(Instruction.SB(tac.frame, t, q, new Const(0)));
            list.add(Instruction.ADDIU(tac.frame, q, q, new Const(1)));
            list.add(Instruction.ADDIU(tac.frame, p, p, new Const(1)));
            list.add(Instruction.ADDI(tac.frame, size, size, new Const(-1)));
            list.add(Instruction.BNE(tac.frame, size, zero, begin));

            list.add(exit);
            list.add(Instruction.SB(tac.frame, zero, q, new Const(0)));
            generate(list, new MoveTAC(tac.frame, res, (AssignableAccess) tac.dst));

        } else if (tac.place == sym("concat")) {

            Temp s1 = tac.frame.addLocal(), s2 = tac.frame.addLocal(),
                 size = tac.frame.addLocal(), res = tac.frame.addLocal(),
                 p = tac.frame.addLocal(), q = tac.frame.addLocal(),
                 b1 = toTemp(list, tac.frame, tac.param1), b2 = toTemp(list, tac.frame, tac.param2),
                 t = tac.frame.addLocal();
            Label begin1 = Label.newLabel(), end1 = Label.newLabel(),
                  begin2 = Label.newLabel(), end2 = Label.newLabel();

            generate(list, new CallExternTAC(tac.frame, sym("size"), b1, null, null, s1));
            generate(list, new CallExternTAC(tac.frame, sym("size"), b2, null, null, s2));
            list.add(Instruction.ADD(tac.frame, size, s1, s2));
            list.add(Instruction.ADDI(tac.frame, size, size, new Const(1)));
            generate(list, new CallExternTAC(tac.frame, sym("malloc"), size, null, null, res));
            list.add(Instruction.MOVE(tac.frame, p, b1));
            list.add(Instruction.MOVE(tac.frame, q, res));

            list.add(begin1);
            list.add(Instruction.LB(tac.frame, t, p, new Const(0)));
            list.add(Instruction.SB(tac.frame, t, q, new Const(0)));
            list.add(Instruction.ADDIU(tac.frame, p, p, new Const(1)));
            list.add(Instruction.ADDIU(tac.frame, q, q, new Const(1)));
            list.add(Instruction.BNE(tac.frame, t, zero, begin1));

            list.add(end1);
            list.add(Instruction.MOVE(tac.frame, p, b2));
            list.add(Instruction.ADDIU(tac.frame, q, q, new Const(-1)));

            list.add(begin2);
            list.add(Instruction.LB(tac.frame, t, p, new Const(0)));
            list.add(Instruction.SB(tac.frame, t, q, new Const(0)));
            list.add(Instruction.ADDIU(tac.frame, p, p, new Const(1)));
            list.add(Instruction.ADDIU(tac.frame, q, q, new Const(1)));
            list.add(Instruction.BNE(tac.frame, t, zero, begin2));

            list.add(end2);
            generate(list, new MoveTAC(tac.frame, res, (AssignableAccess) tac.dst));

        } else if (tac.place == sym("not")) {

            Temp t = toTemp(list, tac.frame, tac.param1),
                 res = tac.frame.addLocal();
            list.add(Instruction.SEQ(tac.frame, res, t, zero));
            generate(list, new MoveTAC(tac.frame, res, (AssignableAccess) tac.dst));

        } else if (tac.place == sym("exit")) {

            list.add(Instruction.LI(tac.frame, v0, new Const(10)));
            list.add(Instruction.SYSCALL(tac.frame, v0, a0, a1, 10));

        } else if (tac.place == sym("malloc")) {

            generate(list, new MoveTAC(tac.frame, tac.param1, a0));
            list.add(Instruction.LI(tac.frame, v0, new Const(9)));
            list.add(Instruction.SYSCALL(tac.frame, v0, a0, a1, 9));
            generate(list, new MoveTAC(tac.frame, v0, (AssignableAccess) tac.dst));

        } else if (tac.place == sym("strcmp")) {

            Temp p1 = tac.frame.addLocal(),
                 p2 = tac.frame.addLocal(),
                 t1 = tac.frame.addLocal(),
                 t2 = tac.frame.addLocal();
            Label begin = Label.newLabel(),
                  great = Label.newLabel(),
                  less = Label.newLabel(),
                  equal = Label.newLabel(),
                  end = Label.newLabel();

            generate(list, new MoveTAC(tac.frame, tac.param1, p1));
            generate(list, new MoveTAC(tac.frame, tac.param2, p2));
            
            list.add(begin);
            list.add(Instruction.LB(tac.frame, t1, p1, new Const(0)));
            list.add(Instruction.LB(tac.frame, t2, p2, new Const(0)));
            list.add(Instruction.BGT(tac.frame, t1, t2, great));
            list.add(Instruction.BLT(tac.frame, t1, t2, less));
            list.add(Instruction.BEQ(tac.frame, t1, zero, equal));
            list.add(Instruction.ADDIU(tac.frame, p1, p1, new Const(1)));
            list.add(Instruction.ADDIU(tac.frame, p2, p2, new Const(1)));
            list.add(Instruction.J(tac.frame, begin));

            list.add(great);
            generate(list, new MoveTAC(tac.frame, new ConstAccess(1), (AssignableAccess) tac.dst));
            list.add(Instruction.J(tac.frame, end));

            list.add(less);
            generate(list, new MoveTAC(tac.frame, new ConstAccess(-1), (AssignableAccess) tac.dst));
            list.add(Instruction.J(tac.frame, end));

            list.add(equal);
            generate(list, new MoveTAC(tac.frame, new ConstAccess(0), (AssignableAccess) tac.dst));
            list.add(Instruction.J(tac.frame, end));

            list.add(end);
        } else
            throw new Error("Unknown extern call \"" + tac.place.toString() + "\"");
    }

    public void generate(InstructionList list, ReturnTAC tac) {
        list.add(Instruction.JR(tac.frame, ra));
    }

    public void generate(InstructionList list, GotoTAC tac) {
        list.add(Instruction.J(tac.frame, tac.place));
    }

    public void generate(InstructionList list, BranchTAC tac) {
        Temp t1 = toTemp(list, tac.frame, tac.op1),
             t2 = toTemp(list, tac.frame, tac.op2);
        switch (tac.type) {
            case EQ:
                list.add(Instruction.BEQ(tac.frame, t1, t2, tac.place));
                break;

            case NEQ:
                list.add(Instruction.BNE(tac.frame, t1, t2, tac.place));
                break;

            case LT:
                list.add(Instruction.BLT(tac.frame, t1, t2, tac.place));
                break;

            case LEQ:
                list.add(Instruction.BLE(tac.frame, t1, t2, tac.place));
                break;

            case GT:
                list.add(Instruction.BGT(tac.frame, t1, t2, tac.place));
                break;

            case GEQ:
                list.add(Instruction.BGE(tac.frame, t1, t2, tac.place));
                break;
        }
    }

    private Graph<Temp> buildInterferenceGraph(InstructionList list, LifeAnalysis life) {
        Graph<Temp> graph = new Graph<Temp>();
        graph.addNode(fp);
        graph.addNode(sp);
        graph.addNode(ra);
        graph.addNode(v0);
        graph.addNode(a0);
        graph.addNode(a1);
        boolean first = true;
        for (LabeledInstruction i: list) {
            if (i.instruction != null) {
                if (first) {
                    first = false;
                    for (Temp u: life.in(i.instruction)) {
                        if (u != fp)
                            graph.addUndirectedEdge(fp, u);
                        if (u != sp)
                            graph.addUndirectedEdge(sp, u);
                        if (u != ra)
                            graph.addUndirectedEdge(ra, u);
                    }
                }

                for (Temp u: i.instruction.useList())
                    graph.addNode(u);
                for (Temp t: i.instruction.def()) {
                    graph.addNode(t);
                    for (Temp u: life.out(i.instruction)) {
                        if (t != u)
                            graph.addUndirectedEdge(t, u);
                    }
                }
            }
        }
        for (Temp t: ir.displays)
            graph.addNode(t);
        graph.removeNode(zero);
        graph.removeNode(gp);
        return graph;
    }
}

