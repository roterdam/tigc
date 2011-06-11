package arch;

import frame.Frame;
import intermediate.Temp;

public abstract class InstructionGenerator {
    public abstract Instruction MOVE(Frame frame, Temp dst, Temp src);
}

