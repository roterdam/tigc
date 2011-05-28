package mips32;

import regalloc.TempMap;
import frame.Frame;
import intermediate.*;

public class Instruction {
    Frame frame;

    Type type;
    Temp dst = null, src1 = null, src2 = null;
    Const imm = null;

    static enum Type {
        MOVE, ADD, ADDI,
            SUB, MUL,
            DIV, LW, LB,
            SW, SB, J,
            JAL, JR,
            BEQ, BNE,
            LI, LA
    }

    public Instruction(Frame frame, Type type, Temp dst, Temp src1, Temp src2, Const imm) {
        this.frame = frame;
        this.type = type;
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
        this.imm = imm;
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

            case J:
                s = "j " + imm.toString();
                break;

            case JAL:
                s = "jal " + imm.toString();
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

