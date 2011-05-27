package mips32;

import regalloc.TempMap;
import frame.Frame;

public abstract class Instruction {
    Frame frame;

    public Instruction(Frame frame) {
        this.frame = frame;
    }

    public abstract String toString(TempMap map);
}

