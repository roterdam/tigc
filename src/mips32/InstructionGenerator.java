package mips32;

import intermediate.Temp;
import frame.Frame;

public class InstructionGenerator extends arch.InstructionGenerator {
    public Instruction MOVE(Frame frame, Temp dst, Temp src) {
        return Instruction.MOVE(frame, dst, src);
    }
}
