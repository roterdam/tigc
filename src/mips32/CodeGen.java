package mips32;

import intermediate.*;
import util.*;
import java.util.*;
import frame.Frame;

public class CodeGen {
    static class MipsMemStyle {
        Temp base;
        Const offset;

        MipsMemStyle(Temp base, Const offset) {
            this.base = base;
            this.offset = offset;
        }
    }

    HashMap<Label, ThreeAddressCode> labelMap;
    IR ir;
    Temp zero, fp, sp, ra, lo, hi;

    public CodeGen(IR ir) {
        this.ir = ir;
        ir.wordLength.bind(4);
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
        ra = ir.globalFrame.addLocal();
        lo = ir.globalFrame.addLocal();
        hi = ir.globalFrame.addLocal();
    }

    Const processConstAccess(ConstAccess ca) {
        if (ca instanceof UnknownConstAccess) {
            if (ca == ir.wordLength)
                return new Const(4);
            else
                return new Const(((UnknownConstAccess) ca).name);
        } else
            return new Const(ca.value);
    }

    MipsMemStyle processMemAccess(InstructionList list, Frame frame, MemAccess ma) {
        if (ma.base instanceof Temp && ma.offset instanceof Temp) {
            Temp t = frame.addLocal();
            list.add(new Instruction(frame, Instruction.Type.ADD, t, (Temp) ma.base, (Temp) ma.offset, null));
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

    public InstructionList generate() {
        InstructionList list = new InstructionList();
        for (IntermediateCode ic: ir.codes) {
            if (ic.tac != null) {
                generate(list, ic.tac);
            }
        }
        return list;
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
    }

    public void generate(InstructionList list, OpTAC tac) {
    }

    public void generate(InstructionList list, CallTAC tac) {
    }

    public void generate(InstructionList list, CallExternTAC tac) {
    }

    public void generate(InstructionList list, ReturnTAC tac) {
    }

    public void generate(InstructionList list, GotoTAC tac) {
    }

    public void generate(InstructionList list, BranchTAC tac) {
    }

}

