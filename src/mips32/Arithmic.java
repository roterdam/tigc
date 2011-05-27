package mips32;

import intermediate.Temp;
import frame.Frame;
import regalloc.TempMap;

class Arithmic extends Instruction {
    public static enum Op {
        ADD, SUB, MUL
    }

    Temp dst, src1, src2;
    Op op;

    public Arithmic(Frame frame, Op op, Temp src1, Temp src2, Temp dst) {
        super(frame);
        this.op = op;
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
    }

    public String toString(TempMap map) {
        String s = "";
        switch (op) {
            case ADD:
                s = "add";
                break;

            case SUB:
                s = "sub";
                break;

            case MUL:
                s = "mul";
                break;
        }
        return s + " " + map.get(src1) + ", " + map.get(src2) + ", " + map.get(dst);
    }
}

