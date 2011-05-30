package mips32;

import regalloc.TempMap;
import frame.Frame;
import intermediate.*;
import arch.Const;
import java.util.*;

public class Instruction extends arch.Instruction {

    Type type;
    Temp dst = null, src1 = null, src2 = null;
    Const imm = null;
    Label target = null;

    // if special is true, then the instruction is a param passing or a return value assignment
    // when it is executed, $fp points to caller's frame start and $sp points to callee's frame start
    boolean special = false;

    ArrayList<Temp> syscallUse = new ArrayList<Temp>();
    Temp syscallDef = null;

    Temp ra = null;

    static enum Type {
        MOVE, ADD, ADDI,
            SUB, MUL,
            DIV, NEG,
            SLT, SLTI,
            SLE, SEQ,
            SNE, SGT, SGE,
            LW, LB,
            SW, SB, J,
            JAL, JR,
            BEQ, BNE,
            BLT, BGT,
            BLE, BGE,
            LI, LA, SYSCALL
    }

    public Instruction(Frame frame, Type type, Temp dst, Temp src1, Temp src2, Const imm, Label target) {
        this.frame = frame;
        this.type = type;
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
        this.imm = imm;
        this.target = target;
    }

    public static Instruction MOVE(Frame frame, Temp dst, Temp src) {
        return new Instruction(frame, Type.MOVE, dst, src, null, null, null);
    }

    public static Instruction ADD(Frame frame, Temp dst, Temp src1, Temp src2) {
        return new Instruction(frame, Type.ADD, dst, src1, src2, null, null);
    }

    public static Instruction ADDI(Frame frame, Temp dst, Temp src1, Const src2) {
        return new Instruction(frame, Type.ADDI, dst, src1, null, src2, null);
    }

    public static Instruction SUB(Frame frame, Temp dst, Temp src1, Temp src2) {
        return new Instruction(frame, Type.SUB, dst, src1, src2, null, null);
    }

    public static Instruction MUL(Frame frame, Temp dst, Temp src1, Temp src2) {
        return new Instruction(frame, Type.MUL, dst, src1, src2, null, null);
    }

    public static Instruction DIV(Frame frame, Temp dst, Temp src1, Temp src2) {
        return new Instruction(frame, Type.DIV, dst, src1, src2, null, null);
    }

    public static Instruction NEG(Frame frame, Temp dst, Temp src) {
        return new Instruction(frame, Type.NEG, dst, src, null, null, null);
    }

    public static Instruction SLT(Frame frame, Temp dst, Temp src1, Temp src2) {
        return new Instruction(frame, Type.SLT, dst, src1, src2, null, null);
    }

    public static Instruction SLTI(Frame frame, Temp dst, Temp src1, Const src2) {
        return new Instruction(frame, Type.SLTI, dst, src1, null, src2, null);
    }

    public static Instruction SLE(Frame frame, Temp dst, Temp src1, Temp src2) {
        return new Instruction(frame, Type.SLE, dst, src1, src2, null, null);
    }

    public static Instruction SEQ(Frame frame, Temp dst, Temp src1, Temp src2) {
        return new Instruction(frame, Type.SEQ, dst, src1, src2, null, null);
    }

    public static Instruction SNE(Frame frame, Temp dst, Temp src1, Temp src2) {
        return new Instruction(frame, Type.SNE, dst, src1, src2, null, null);
    }

    public static Instruction SGT(Frame frame, Temp dst, Temp src1, Temp src2) {
        return new Instruction(frame, Type.SGT, dst, src1, src2, null, null);
    }

    public static Instruction SGE(Frame frame, Temp dst, Temp src1, Temp src2) {
        return new Instruction(frame, Type.SGE, dst, src1, src2, null, null);
    }

    public static Instruction LW(Frame frame, Temp dst, Temp base, Const offset) {
        return new Instruction(frame, Type.LW, dst, base, null, offset, null);
    }

    public static Instruction LB(Frame frame, Temp dst, Temp base, Const offset) {
        return new Instruction(frame, Type.LB, dst, base, null, offset, null);
    }

    public static Instruction SW(Frame frame, Temp value, Temp base, Const offset) {
        return new Instruction(frame, Type.SW, null, base, value, offset, null);
    }

    public static Instruction SB(Frame frame, Temp value, Temp base, Const offset) {
        return new Instruction(frame, Type.SB, null, base, value, offset, null);
    }

    public static Instruction J(Frame frame, Label target) {
        return new Instruction(frame, Type.J, null, null, null, null, target);
    }

    public static Instruction JAL(Frame frame, Label target, Temp ra) {
        Instruction ins = new Instruction(frame, Type.JAL, null, null, null, null, target);
        ins.ra = ra;
        return ins;
    }

    public static Instruction JR(Frame frame, Temp src) {
        return new Instruction(frame, Type.JR, null, src, null, null, null);
    }

    public static Instruction BEQ(Frame frame, Temp src1, Temp src2, Label target) {
        return new Instruction(frame, Type.BEQ, null, src1, src2, null, target);
    }

    public static Instruction BNE(Frame frame, Temp src1, Temp src2, Label target) {
        return new Instruction(frame, Type.BNE, null, src1, src2, null, target);
    }

    public static Instruction BLT(Frame frame, Temp src1, Temp src2, Label target) {
        return new Instruction(frame, Type.BLT, null, src1, src2, null, target);
    }
    
    public static Instruction BLE(Frame frame, Temp src1, Temp src2, Label target) {
        return new Instruction(frame, Type.BLE, null, src1, src2, null, target);
    }

    public static Instruction BGT(Frame frame, Temp src1, Temp src2, Label target) {
        return new Instruction(frame, Type.BGT, null, src1, src2, null, target);
    }

    public static Instruction BGE(Frame frame, Temp src1, Temp src2, Label target) {
        return new Instruction(frame, Type.BGE, null, src1, src2, null, target);
    }

    public static Instruction LI(Frame frame, Temp dst, Const imm) {
        return new Instruction(frame, Type.LI, dst, null, null, imm, null);
    }

    public static Instruction LA(Frame frame, Temp dst, String name) {
        return new Instruction(frame, Type.LA, dst, null, null, new Const(name), null);
    }

    public static Instruction SYSCALL(Frame frame, Temp v0, Temp a0, Temp a1, int id) {
        Instruction ret = new Instruction(frame, Type.SYSCALL, null, null, null, null, null);
        ret.syscallUse.add(v0);
        switch (id) {
            case 1:
                ret.syscallUse.add(a0);
                break;

            case 4:
                ret.syscallUse.add(a0);
                break;

            case 5:
                ret.syscallDef = v0;
                break;

            case 8:
                ret.syscallUse.add(a0);
                ret.syscallUse.add(a1);
                break;

            case 9:
                ret.syscallUse.add(a0);
                ret.syscallDef = v0;
        }
        return ret;
    }

    public String toString() {
        Map<Temp, Temp> map = new HashMap<Temp, Temp>();
        if (src1 != null)
            map.put(src1, src1);
        if (src2 != null)
            map.put(src2, src2);
        if (dst != null)
            map.put(dst, dst);
        return toString(map);
    }

    public String toString(Map map) {
        String s = "";
        switch (type) {
            case MOVE:
                s = "move " + map.get(dst).toString() + ", " + map.get(src1).toString();
                break;

            case ADD:
                s = "add " + map.get(dst).toString() + ", " + map.get(src1).toString() + ", " + map.get(src2).toString();
                break;

            case ADDI:
                s = "addi " + map.get(dst).toString() + ", " + map.get(src1).toString() + ", " + imm.toString();
                break;

            case SUB:
                s = "sub " + map.get(dst).toString() + ", " + map.get(src1).toString() + ", " + map.get(src2).toString();
                break;

            case MUL:
                s = "mul " + map.get(dst).toString() + ", " + map.get(src1).toString() + ", " + map.get(src2).toString();
                break;

            case DIV:
                s = "div " + map.get(dst).toString() + ", " + map.get(src1).toString() + ", " + map.get(src2).toString();
                break;

            case NEG:
                s = "neg " + map.get(dst).toString() + ", " + map.get(src1).toString();
                break;

            case SLT:
                s = "slt " + map.get(dst).toString() + ", " + map.get(src1).toString() + ", " + map.get(src2).toString();
                break;

            case SLTI:
                s = "slti " + map.get(dst).toString() + ", " + map.get(src1).toString() + ", " + imm.toString();
                break;
                
            case SLE:
                s = "sle " + map.get(dst).toString() + ", " + map.get(src1).toString() + ", " + map.get(src2).toString();
                break;

            case SEQ:
                s = "seq " + map.get(dst).toString() + ", " + map.get(src1).toString() + ", " + map.get(src2).toString();
                break;

            case SNE:
                s = "sne " + map.get(dst).toString() + ", " + map.get(src1).toString() + ", " + map.get(src2).toString();
                break;

            case SGT:
                s = "sgt " + map.get(dst).toString() + ", " + map.get(src1).toString() + ", " + map.get(src2).toString();
                break;

            case SGE:
                s = "sge " + map.get(dst).toString() + ", " + map.get(src1).toString() + ", " + map.get(src2).toString();
                break;

            case J:
                s = "j " + target.toString();
                break;

            case JAL:
                s = "jal " + target.toString();
                break;

            case JR:
                s = "jr " + map.get(src1).toString();
                break;

            case LW:
                s = "lw " + map.get(dst).toString() + ", " + imm.toString() + "(" + map.get(src1).toString() + ")";
                break;

            case LB:
                s = "lb " + map.get(dst).toString() + ", " + imm.toString() + "(" + map.get(src1).toString() + ")";
                break;

            case SW:
                s = "sw " + map.get(src2).toString() + ", " + imm.toString() + "(" + map.get(src1).toString() + ")";
                break;

            case SB:
                s = "sb " + map.get(src2).toString() + ", " + imm.toString() + "(" + map.get(src1).toString() + ")";
                break;

            case BEQ:
                s = "beq " + map.get(src1).toString() + ", " + map.get(src2).toString() + ", " + target.toString();
                break;

            case BNE:
                s = "bne " + map.get(src1).toString() + ", " + map.get(src2).toString() + ", " + target.toString();
                break;

            case BLT:
                s = "blt " + map.get(src1).toString() + ", " + map.get(src2).toString() + ", " + target.toString();
                break;

            case BLE:
                s = "ble " + map.get(src1).toString() + ", " + map.get(src2).toString() + ", " + target.toString();
                break;

            case BGT:
                s = "bgt " + map.get(src1).toString() + ", " + map.get(src2).toString() + ", " + target.toString();
                break;

            case BGE:
                s = "bge " + map.get(src1).toString() + ", " + map.get(src2).toString() + ", " + target.toString();
                break;

            case LI:
                s = "li " + map.get(dst).toString() + ", " + imm.toString();
                break;

            case LA:
                s = "la " + map.get(dst).toString() + ", " + imm.toString();
                break;

            case SYSCALL:
                s = "syscall";
        }
        return s;
    }

    public Set<Temp> use() {
        HashSet<Temp> ret = new HashSet<Temp>();
        if (type != Type.SYSCALL) {
            if (src1 != null)
                ret.add(src1);
            if (src2 != null)
                ret.add(src2);
        } else
            ret.addAll(syscallUse);
        return ret;
    }

    public Set<Temp> def() {
        HashSet<Temp> ret = new HashSet<Temp>();
        if (type == Type.SYSCALL) {
            if (syscallDef != null)
                ret.add(syscallDef);
        } else if (type == Type.JAL)
            ret.add(ra);
        else if (dst != null)
            ret.add(dst);

        return ret;
    }

    public boolean isJump() {
        if (type == Type.J || type == Type.JR || type == Type.JAL
                || isBranch())
            return true;
        else
            return false;
    }

    public boolean isBranch() {
        if (type == Type.BEQ || type == Type.BNE
                || type == Type.BGT || type == Type.BGE
                || type == Type.BLT || type == Type.BLE)
            return true;
        else
            return false;
    }
}

