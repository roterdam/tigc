package mips32;

import regalloc.TempMap;
import frame.Frame;
import intermediate.*;
import arch.Const;

public class Instruction {
    Frame frame;

    Type type;
    Temp dst = null, src1 = null, src2 = null;
    Const imm = null;
    Label target = null;

    // if special is true, then the instruction is a param passing or a return value assignment
    // when it is executed, $fp points to caller's frame start and $sp points to callee's frame start
    boolean special = false;

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
            LI, LA
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

    public static Instruction SW(Frame frame, Temp value, Temp base, Const offset) {
        return new Instruction(frame, Type.SW, null, base, value, offset, null);
    }

    public static Instruction J(Frame frame, Label target) {
        return new Instruction(frame, Type.J, null, null, null, null, target);
    }

    public static Instruction JAL(Frame frame, Label target) {
        return new Instruction(frame, Type.JAL, null, null, null, null, target);
    }

    public static Instruction JR(Frame frame, Temp src) {
        return new Instruction(frame, Type.JR, null, src, null, null, null);
    }

    public static Instruction LI(Frame frame, Temp dst, Const imm) {
        return new Instruction(frame, Type.LI, dst, null, null, imm, null);
    }

    public static Instruction LA(Frame frame, Temp dst, String name) {
        return new Instruction(frame, Type.LA, dst, null, null, new Const(name), null);
    }

    public String toString(TempMap map) {
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
                s = "beq " + map.get(src1).toString() + ", " + map.get(src2).toString() + ", " + imm.toString();
                break;

            case BNE:
                s = "bne " + map.get(src1).toString() + ", " + map.get(src2).toString() + ", " + imm.toString();
                break;

            case LI:
                s = "li " + map.get(dst).toString() + ", " + imm.toString();
                break;

            case LA:
                s = "la " + map.get(dst).toString() + ", " + imm.toString();
                break;
        }
        return s;
    }
}

