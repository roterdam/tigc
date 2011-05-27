package mips32;

import intermediate.*;
import frame.Frame;
import regalloc.TempMap;

class Move extends Instruction {
    Temp dst, src;

    public Move(Frame frame, Temp dst, Temp src) {
        super(frame);
        this.dst = dst;
        this.src = src;
    }

    public String toString(TempMap map) {
        return "move " + map.get(dst) + ", " + map.get(src);
    }
}

